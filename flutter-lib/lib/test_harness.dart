import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'src/impl/gc_evolve.dart';
import 'src/impl/gcdrawer_evolve.dart';

import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter/widgets.dart';

import 'src/comm/comm.dart';
import 'src/impl/utils/dnd_session.dart' show DndNegotiationState, DndSession;
import 'src/comm/comm_frame.dart' show EquoCommBase;
import 'src/comm/widget_wire.dart' show WireVisitable;
import 'src/impl/styledtext_evolve.dart' show styledTextPerfSnapshot;
import 'src/gen/widget.dart';
import 'src/testing/frame_stats.dart';
import 'src/testing/wire_stats.dart';
import 'src/testing/render_facts.dart';
import 'src/testing/tree_test_registry.dart';
import 'src/gen/widgets.dart' show WireChannel;
import 'test_harness_iframe_stub.dart'
    if (dart.library.js_interop) 'test_harness_iframe_web.dart';
import 'test_harness_jsapi_stub.dart'
    if (dart.library.js_interop) 'test_harness_jsapi_web.dart';

part 'test_harness_actions.dart';

/// Test-only support: a dormant comm channel that lets a Java integration test
/// read the *live* Flutter state of any rendered SWT widget by id, and send it
/// back for assertions. It is inert in production — nothing ever sends on these
/// channels there — so it is safe to register unconditionally from `main()`.
///
/// Protocol (tagged values):
///   request  on `evolve.test.query`         : { "queryId": int, "targetId": int }
///   response on `evolve.test.queryResponse`  : { "queryId": int, "found": bool,
///                                                "state": the widget's [describeState] or null,
///                                                "render": render-tree facts or null,
///                                                "dropAnswer": a drop target's last answer or null }
///
/// `targetId` is the SWT widget id (Java `widget.hashCode()` == `V*.id`). The
/// returned `state` is exactly what the widget's [WidgetSwtState] currently
/// holds — i.e. what Flutter would paint — so a test can assert that a state
/// pushed from Java actually reached and updated the rendered widget. `render`
/// reports what only the rendered tree can answer; see [_renderFacts].
/// What the client holds of the drop target [id]'s last answer to a drag, as it decoded it from
/// Java; null when it has none.
Map<String, int>? _dropAnswer(int id) {
  final answer = DndSession.stateFor(id);
  if (identical(answer, DndNegotiationState.none)) return null;
  return {'detail': answer.detail, 'feedback': answer.feedback, 'currentDataTypeId': answer.currentDataTypeId};
}

void registerTestQueryChannel() {
  // StyledText counters, reset on read so two calls bracket an action.
  EquoCommService.on(0, 0, WireChannel.evolveTestStyledTextPerf, (dynamic req) {
    final perfId = req is Map ? req['perfId'] : null;
    _reply(WireChannel.evolveTestStyledTextPerfResponse, {'perfId': perfId, 'perf': styledTextPerfSnapshot()});
  });
  // Engine frame timings: `evolve.test.frameCost` {} (clears them) answers on
  // `evolve.test.frameCostResponse` { "frames": [{ "build": ms, "raster": ms }] }.
  EquoCommService.on(0, 0, WireChannel.evolveTestFrameCost, (dynamic req) {
    final costId = req is Map ? req['costId'] : null;
    // Registered on first use: this runs from main() before the binding exists.
    _recordFrameTimings();
    final frames = [
      for (final t in _frameTimings)
        {
          'build': t.buildDuration.inMicroseconds / 1000.0,
          'raster': t.rasterDuration.inMicroseconds / 1000.0,
        },
    ];
    _frameTimings.clear();
    _reply(WireChannel.evolveTestFrameCostResponse, {'costId': costId, 'frames': frames});
  });
  // A drag the client makes as a user's pointer would: `evolve.test.drag` {dragId, from, to, steps,
  // intervalMs} answers on `evolve.test.dragDone` {dragId, moves, elapsedMicros} once released.
  EquoCommService.on(0, 0, WireChannel.evolveTestDrag, (dynamic req) async {
    final map = (req as Map).cast<String, dynamic>();
    final dragId = map['dragId'];
    final result = await dragByIds(map['from'] as String, map['to'] as String,
        (map['steps'] as num).toInt(), (map['intervalMs'] as num).toInt());
    _reply(WireChannel.evolveTestDragDone, {'dragId': dragId, ...result});
  });
  EquoCommService.on(0, 0, WireChannel.evolveTestQuery, (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int queryId = (map['queryId'] as num).toInt();
    final int targetId = (map['targetId'] as num).toInt();
    final Map<String, dynamic>? vstate = _findStateById(targetId);
    _reply(WireChannel.evolveTestQueryResponse, {
      'queryId': queryId,
      'found': vstate != null,
      'state': vstate,
      'render': _renderFacts(targetId),
      'dropAnswer': _dropAnswer(targetId),
    });
  });
  // Frame barrier: ack once what was pushed before it is built + painted, so a test can wait for
  // pushed state to actually render instead of pumping an arbitrary number of times. Checked once the
  // work already queued has run: with a frame to come, the ack is sent from its post-frame callback,
  // which runs after build/layout/paint; with none, nothing pushed changed what is painted, and the
  // ack goes at once rather than after a frame that would only draw the same picture again.
  EquoCommService.on(0, 0, WireChannel.evolveTestFrameSync, (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int syncId = (map['syncId'] as num).toInt();
    // Where the wait went, on one clock: queued behind the frames that came before this request,
    // then until the frame began, then building and painting it.
    final clock = EquoCommBase.clock;
    final received = EquoCommBase.lastReceivedMicros;
    final arrived = clock.elapsedMicroseconds;
    var began = arrived;
    void ack() {
      final painted = clock.elapsedMicroseconds;
      _reply(WireChannel.evolveTestFrameSynced, {
        'syncId': syncId,
        'queuedMicros': arrived - received,
        'toVsyncMicros': began - arrived,
        'vsyncToPaintedMicros': painted - began,
      });
    }

    Timer.run(() {
      final scheduler = SchedulerBinding.instance;
      if (!scheduler.hasScheduledFrame && scheduler.schedulerPhase == SchedulerPhase.idle) {
        began = clock.elapsedMicroseconds;
        ack();
        return;
      }
      scheduler.scheduleFrameCallback((_) => began = clock.elapsedMicroseconds);
      scheduleFrameSync(ack);
    });
  });

  // Frame statistics from the engine's own pipeline, the only rendering measurement comparable to a
  // native toolkit's: a Java-side timer sees serialization, not the frame reaching the screen.
  EquoCommService.on(0, 0, WireChannel.evolveTestFrameStats, (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int statsId = (map['statsId'] as num).toInt();
    final String action = (map['action'] as String?) ?? 'snapshot';
    Map<String, dynamic> stats;
    try {
      if (action == 'start') {
        FrameStats.start();
      } else if (action == 'stop') {
        FrameStats.stop();
      }
      // A ping times the transport: answering it must not cost anything of its own.
      stats = action == 'ping' ? const <String, dynamic>{} : FrameStats.snapshot();
    } catch (e) {
      // Always answer: the comm layer isolates handler errors, so throwing here would leave the
      // caller waiting on a timeout with nothing to diagnose.
      stats = <String, dynamic>{'error': '$e'};
    }
    _reply(WireChannel.evolveTestFrameStatsResponse, {
      'statsId': statsId,
      'stats': stats,
    });
  });

  // Receive-side cost of widget state: `evolve.test.wireStats` {statsId, action: start|stop} answers on
  // `evolve.test.wireStatsResponse` {statsId, stats}; see [WireStats].
  EquoCommService.on(0, 0, WireChannel.evolveTestWireStats, (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int statsId = (map['statsId'] as num).toInt();
    final Map<String, dynamic> stats;
    if (map['action'] == 'start') {
      WireStats.start();
      stats = const <String, dynamic>{};
    } else {
      stats = WireStats.stop();
    }
    _reply(WireChannel.evolveTestWireStatsResponse, {'statsId': statsId, 'stats': stats});
  });

  // Result channel for the debug UI-thread runner (see test_harness_actions.dart `openPref` /
  // `listPrefPages`). Java answers a `listPrefPages` request here; we cache the latest payload so a
  // follow-up `lastPrefPages()` eval can read it (a single eval can't span the async Java round-trip).
  EquoCommService.on(0, 0, WireChannel.swtEvolveTestRunUiResponse, (dynamic resp) {
    if (resp is Map) _lastRunUiResponse = resp.cast<String, dynamic>();
  });

  // Relay embedded-iframe load pings to Java (web only; no-op elsewhere).
  registerIframeLoadProbe();

  // Same query/frame-sync logic, but reachable directly from `window` (web only; no-op
  // elsewhere) — lets a browser-side driver (Playwright etc.) assert on live Dart state
  // without going through Java/the comm WebSocket at all. See test_harness_jsapi_web.dart.
  registerTestQueryJsApi(
    styledTextPerfJson: () => jsonEncode(styledTextPerfSnapshot()),
    renderFactsJson: (int targetId) {
      final facts = _renderFacts(targetId);
      return facts == null ? null : jsonEncode(facts);
    },
    queryStateJson: queryStateJson,
    queryAllStatesJson: queryAllStatesJson,
    queryStatesJson: queryStatesJson,
    queryPaintOpsJson: queryPaintOpsJson,
    queryTreeItemsJson: queryTreeItemsJson,
    expandTreeItem: expandTreeItem,
    queryPrimaryFocus: queryPrimaryFocus,
    scheduleFrameSync: scheduleFrameSync,
  );
}

/// The frames the engine has reported since they were last collected, newest last.
final List<FrameTiming> _frameTimings = <FrameTiming>[];

bool _timingsRegistered = false;

void _recordFrameTimings() {
  if (_timingsRegistered) return;
  _timingsRegistered = true;
  SchedulerBinding.instance.addTimingsCallback((List<FrameTiming> timings) {
    _frameTimings.addAll(timings);
    if (_frameTimings.length > 240) {
      _frameTimings.removeRange(0, _frameTimings.length - 240);
    }
  });
}

/// Which SWT widget currently holds primary keyboard focus, as its `{swt}/{id}` semantics
/// identifier -- for tests narrowing down why a key press did not reach the widget they expected,
/// and for the ones that wait on focus actually moving before typing.
///
/// The node's own `toString()` cannot be used: a release web build is minified, so every FocusNode
/// renders as the same `Instance of 'minified:XX'` and two different nodes compare equal.
String queryPrimaryFocus() {
  final FocusNode? node = FocusManager.instance.primaryFocus;
  if (node == null) return '<none>';
  final BuildContext? context = node.context;
  if (context == null || !context.mounted) return '<detached>';
  String? owner;
  context.visitAncestorElements((Element el) {
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null) {
        owner = '${vstate.swt}/${vstate.id}';
        return false;
      }
    }
    return true;
  });
  return owner ?? '<unowned>';
}

/// The widget's live [describeState] as a JSON string, or null if not mounted. Shared by
/// the Java-facing comm channel above and the JS-facing window API (see
/// test_harness_jsapi_web.dart).
String? queryStateJson(int targetId) {
  final Map<String, dynamic>? vstate = _findStateById(targetId);
  if (vstate == null) return null;
  return jsonEncode(vstate);
}

/// The live [describeState] of the widgets named by [identifiers] (`{swt}/{id}`), keyed the same
/// way, as a JSON-encoded object; an identifier with no mounted widget is left out. One walk of the
/// tree, and only the requested states are encoded, so asking for a few widgets does not pay for
/// serializing all of them the way [queryAllStatesJson] does.
String queryStatesJson(List<String> identifiers) {
  final wanted = identifiers.toSet();
  final Map<String, dynamic> found = {};
  final root = WidgetsBinding.instance.rootElement;
  if (root == null || wanted.isEmpty) return jsonEncode(found);
  void addWithItems(Map<String, dynamic> node) {
    final key = '${node['swt']}/${node['id']}';
    if (wanted.contains(key)) found[key] = node;
    for (final item in _itemsOf(node)) {
      addWithItems(item);
    }
  }

  void visit(Element el) {
    if (found.length == wanted.length) return;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null) {
        addWithItems(describeState(vstate));
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return jsonEncode(found);
}

/// Every mounted SWT widget's live [describeState], keyed by its `{swt}/{id}` semantics
/// identifier, as a JSON-encoded object. Items rendered inline by their parent (VTreeItem,
/// VTabItem, VExpandItem, ...) have no [WidgetSwtState] of their own, but they ARE nested in
/// the parent's description, so the descriptions are walked rather than the State objects.
String queryAllStatesJson() {
  final root = WidgetsBinding.instance.rootElement;
  final Map<String, dynamic> all = {};
  if (root == null) return jsonEncode(all);
  void addWithItems(Map<String, dynamic> node) {
    all['${node['swt']}/${node['id']}'] = node;
    for (final item in _itemsOf(node)) {
      addWithItems(item);
    }
  }

  void visit(Element el) {
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null) {
        addWithItems(describeState(vstate));
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return jsonEncode(all);
}

/// Every mounted GC's paint ops, keyed by `{swt}#{position in the widget tree}`, as a
/// JSON-encoded object of string lists.
///
/// A GC's draw calls are not part of any widget's serialized state — they arrive on their own comm
/// channel and end up in the drawer's shape list — so a control that paints through a PaintListener
/// describes itself as an empty Canvas to [queryAllStatesJson]. This is the assertable form of what
/// it actually drew, which a screenshot can otherwise only show as pixels.
///
/// Keyed by tree position rather than by widget id on purpose: ids are assigned per run, so an id
/// would make two runs of the same snippet disagree on content that is identical. Callers that need
/// the id have it from [queryAllStatesJson].
String queryPaintOpsJson() {
  final root = WidgetsBinding.instance.rootElement;
  final Map<String, List<String>> all = {};
  if (root == null) return jsonEncode(all);
  int seen = 0;

  void visit(Element el) {
    if (el is StatefulElement && el.state is GCImpl) {
      final GCImpl gc = el.state as GCImpl;
      final dynamic vstate = gc.state;
      if (vstate != null) {
        final List<String> ops = [for (final Shape s in gc.paintOps) s.describe()];
        // The key carries where this GC sits on screen, because its ops are in its own
        // coordinates: without the origin a reader cannot place them on a screenshot, and a
        // control that moved would describe itself exactly as one that did not.
        final RenderObject? box = el.findRenderObject();
        final Offset origin = box is RenderBox && box.hasSize
            ? box.localToGlobal(Offset.zero)
            : Offset.zero;
        if (ops.isNotEmpty) {
          all['${vstate.swt}#${seen++}@${origin.dx.round()},${origin.dy.round()}'] = ops;
        }
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return jsonEncode(all);
}

/// [value]'s state as the harness reports it: `swt` and `id`, then every property it holds keyed
/// by the number the wire gives it, which the reader names from the generated wire table. A value it holds is
/// described the same way, a widget met again inside its own description by identity only.
Map<String, dynamic> describeState(Object value) =>
    value is WireVisitable ? _describe(value, {}) : <String, dynamic>{};

Map<String, dynamic> _describe(WireVisitable value, Set<Object> seen) {
  final out = <String, dynamic>{};
  if (value is VWidget) {
    out['swt'] = value.swt;
    out['id'] = value.id;
    if (!seen.add(value)) return out;
  }
  value.visitWire((key, v) {
    if (v != null) out['$key'] = _plain(v, seen);
  });
  return out;
}

Object? _plain(Object? v, Set<Object> seen) => switch (v) {
      null || num() || bool() || String() => v,
      Uint8List() => base64Encode(v),
      List() => [for (final e in v) _plain(e, seen)],
      Map() => {for (final e in v.entries) '${e.key}': _plain(e.value, seen)},
      WireVisitable() => _describe(v, seen),
      _ => '$v',
    };

/// The widgets described inside [node]'s properties: its items, its children.
List<Map<String, dynamic>> _itemsOf(Map<String, dynamic> node) {
  final List<Map<String, dynamic>> result = [];
  void collect(Object? v) {
    if (v is List) {
      v.forEach(collect);
    } else if (v is Map<String, dynamic> && v.containsKey('swt') && v.containsKey('id')) {
      result.add(v);
    }
  }

  for (final entry in node.entries) {
    if (entry.key != 'swt' && entry.key != 'id') collect(entry.value);
  }
  return result;
}

/// Runs [onSynced] once the next frame has been built + painted (see frameSync above).
void scheduleFrameSync(void Function() onSynced) {
  WidgetsBinding.instance.addPostFrameCallback((_) => onSynced());
  WidgetsBinding.instance.scheduleFrame();
}

/// What only the rendered tree can answer about [targetId], or null when it is not mounted.
///
/// Keys:
///   `takesInput` — whether the widget can be reached by a pointer or by focus traversal where it
///     stands. This is the enforcement itself, not the flag behind it: a disabled control is
///     wrapped in `ExcludeFocus` + `IgnorePointer` (`ControlImpl.blockWhenDisabled`), and one under
///     a disabled ancestor is covered by that ancestor's pair. So it reports SWT's `isEnabled()` as
///     the user meets it, without asking the code under test what it computed. A widget can still
///     decline a gesture on its own (an Item's `build()` drops its `onTap`); that is not a refusal
///     by the tree and is not reported here — read the widget's own `enabled` off `state` for it.
///   `rect` — the widget's rendered bounds in global (page) coordinates, where a test aims input.
///   `facts` — what the widget says it paints, when its State is a [RenderFactsSource].
Map<String, dynamic>? _renderFacts(int targetId) {
  final bool? takesInput = _takesInput(targetId);
  if (takesInput == null) return null;
  final Element? host = _hostElementById(targetId);
  final RenderObject? box = host?.renderObject;
  final State? state = host is StatefulElement ? host.state : null;
  final Map<String, double>? rect = box is RenderBox && box.hasSize ? _globalRect(box) : null;
  return {
    'takesInput': takesInput,
    if (rect != null) 'rect': rect,
    if (state is RenderFactsSource) 'facts': (state as RenderFactsSource).debugRenderFacts(),
  };
}

/// Null when a degenerate transform (e.g. a zero scale) leaves no finite position; JSON has no NaN.
Map<String, double>? _globalRect(RenderBox box) {
  final Offset origin = box.localToGlobal(Offset.zero);
  if (!origin.isFinite) return null;
  return {
    'x': origin.dx,
    'y': origin.dy,
    'width': box.size.width,
    'height': box.size.height,
  };
}

/// The mounted [WidgetSwtState] Element rendering the widget with [targetId], or null.
Element? _hostElementById(int targetId) {
  final root = WidgetsBinding.instance.rootElement;
  if (root == null) return null;
  Element? found;
  void visit(Element el) {
    if (found != null) return;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic v = (el.state as WidgetSwtState).state;
      if (v != null && v.id == targetId) {
        found = el;
        return;
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return found;
}

/// True when nothing between the root and [targetId]'s rendered position refuses pointers or
/// focus, null when it is not mounted.
bool? _takesInput(int targetId) {
  final root = WidgetsBinding.instance.rootElement;
  if (root == null) return null;
  // The widget's own element is where it stands. An Item drawn inline by its owner's build() has
  // none, and stands where its owner does — so that is the fallback, not the first choice.
  return _takesInputWhere(root, (json) => json['id'] == targetId) ??
      _takesInputWhere(root, (json) => _findItemById(json, targetId) != null);
}

bool _refusesInput(Widget w) =>
    (w is IgnorePointer && w.ignoring) || (w is ExcludeFocus && w.excluding);

/// Walks down from [root] to the first [WidgetSwtState] whose serialized state satisfies [matches],
/// answering whether input reaches it: no refusing wrapper on the way down, and none in the
/// wrapper chain the widget puts above its own content.
bool? _takesInputWhere(
    Element root, bool Function(Map<String, dynamic> json) matches) {
  bool? answer;

  void visit(Element el, bool refused) {
    if (answer != null) return;
    if (_refusesInput(el.widget)) refused = true;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null && matches(describeState(vstate))) {
        answer = !refused && !_ownWrapperRefuses(el);
        return;
      }
    }
    final bool inherited = refused;
    el.visitChildren((child) => visit(child, inherited));
  }

  visit(root, false);
  return answer;
}

/// Whether the widget's own build() blocked itself. Only the single-child wrapper chain directly
/// under [el] is inspected: `blockWhenDisabled` is applied at the top of every build path that uses
/// it, while anything past the first branching widget is content — including sibling overlays that
/// legitimately ignore pointers.
bool _ownWrapperRefuses(Element el) {
  for (Element? e = _onlyChildOf(el); e != null; e = _onlyChildOf(e)) {
    if (e is StatefulElement && e.state is WidgetSwtState) return false;
    if (_refusesInput(e.widget)) return true;
  }
  return false;
}

/// [el]'s single child, or null when it has none or branches.
Element? _onlyChildOf(Element el) {
  final List<Element> children = [];
  el.visitChildren(children.add);
  return children.length == 1 ? children.first : null;
}

/// The item with [targetId] anywhere under [node]'s `items`, or null.
Map<String, dynamic>? _findItemById(Map<String, dynamic> node, int targetId) {
  for (final item in _itemsOf(node)) {
    if (item['id'] == targetId) return item;
    final Map<String, dynamic>? nested = _findItemById(item, targetId);
    if (nested != null) return nested;
  }
  return null;
}

/// Walk the live element tree and return the [describeState] of the
/// [WidgetSwtState] whose `state.id` matches [targetId], or null if not mounted.
/// Inline items are matched through their parent's serialized `items` (see
/// [queryAllStatesJson]).
Map<String, dynamic>? _findStateById(int targetId) {
  final root = WidgetsBinding.instance.rootElement;
  if (root == null) return null;
  Map<String, dynamic>? result;
  Map<String, dynamic>? findIn(Map<String, dynamic> node) {
    if (node['id'] == targetId) return node;
    for (final item in _itemsOf(node)) {
      final Map<String, dynamic>? nested = findIn(item);
      if (nested != null) return nested;
    }
    return null;
  }

  void visit(Element el) {
    if (result != null) return;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null) {
        result = findIn(describeState(vstate));
        if (result != null) return;
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return result;
}

/// A reply to the test harness. One that cannot be sent still answers, with what went wrong, under
/// the ids the harness waits on.
void _reply(int channel, Map<String, Object?> payload) {
  try {
    EquoCommService.sendOn(channel, payload);
  } catch (e) {
    EquoCommService.sendOn(channel, {
      for (final entry in payload.entries)
        if (entry.key.endsWith('Id')) entry.key: entry.value,
      'error': '$e',
    });
  }
}
