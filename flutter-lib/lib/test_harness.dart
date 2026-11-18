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
import 'src/comm/v_registry.dart';
import 'src/comm/widget_key.dart';
import 'src/comm/wire_names.dart';
import 'src/comm/widget_wire.dart' show WireVisitable;
import 'src/impl/styledtext_evolve.dart' show styledTextPerfSnapshot;
import 'src/impl/utils/deferred_fonts.dart';
import 'src/gen/widget.dart';
import 'src/testing/boot_trace.dart';
import 'src/testing/frame_stats.dart';
import 'src/testing/injected_keys.dart';
import 'src/testing/wire_stats.dart';
import 'src/testing/render_facts.dart';
import 'src/testing/tree_test_registry.dart';
import 'src/gen/widgets.dart' show WireChannel, WireType;
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
  // With an `op` it is one step of a pointer instead, see [pointerOp]. The gesture runs apart from
  // the queue frames are applied in, which waits on a handler that awaits: what Java answers while
  // it lasts is applied as it arrives, as it is during a user's.
  EquoCommService.on(0, 0, WireChannel.evolveTestDrag, (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final dragId = map['dragId'];
    unawaited(() async {
      final result = map['op'] != null
          ? await pointerOp(map)
          : await dragByIds(map['from'] as String, map['to'] as String, (map['steps'] as num).toInt(),
              (map['intervalMs'] as num).toInt());
      _reply(WireChannel.evolveTestDragDone, {'dragId': dragId, ...result});
    }());
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
  // ack goes at once rather than after a frame that would only draw the same picture again. With
  // `fonts`, the ack waits for every deferred font and the frame that lays text out again in them
  // (a test reading laid-out text); without, it is a paint barrier.
  EquoCommService.on(0, 0, WireChannel.evolveTestFrameSync, (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int syncId = (map['syncId'] as num).toInt();
    final bool afterFonts = map['fonts'] == true;
    // Where the wait went, on one clock: queued behind the frames that came before this request,
    // then until the frame began, then building and painting it.
    final clock = EquoCommBase.clock;
    final received = EquoCommBase.lastReceivedMicros;
    final arrived = clock.elapsedMicroseconds;
    BootTrace.markOnce('frameSync.received');
    var began = arrived;
    void ack() {
      final painted = clock.elapsedMicroseconds;
      BootTrace.markOnce('frameSync.answered');
      _reply(WireChannel.evolveTestFrameSynced, {
        'syncId': syncId,
        'queuedMicros': arrived - received,
        'toVsyncMicros': began - arrived,
        'vsyncToPaintedMicros': painted - began,
        // On the clock [pointerOp] stamps its input with, so input to painted needs no other clock.
        'lastFrameAtMicros': lastFrameAtMicros(),
      });
    }

    if (afterFonts) {
      DeferredFonts.all().then((_) {
        SchedulerBinding.instance.scheduleFrameCallback((_) => began = clock.elapsedMicroseconds);
        scheduleFrameSync(ack);
      });
      return;
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
  EquoCommService.on(0, 0, WireChannel.evolveTestFrameStats, (dynamic req) async {
    final map = (req as Map).cast<String, dynamic>();
    final int statsId = (map['statsId'] as num).toInt();
    final String action = (map['action'] as String?) ?? 'snapshot';
    Map<String, dynamic> stats;
    try {
      if (action == 'start') {
        FrameStats.start();
      } else if (action == 'stop') {
        await FrameStats.stop();
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
  // `evolve.test.wireStatsResponse` {statsId, stats}; see [WireStats]. `stats.styledText` holds the
  // StyledText counters over the same span.
  EquoCommService.on(0, 0, WireChannel.evolveTestWireStats, (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int statsId = (map['statsId'] as num).toInt();
    final Map<String, dynamic> stats;
    if (map['action'] == 'start') {
      WireStats.start();
      styledTextPerfSnapshot();
      stats = const <String, dynamic>{};
    } else {
      stats = {...WireStats.stop(), 'styledText': styledTextPerfSnapshot()};
    }
    _reply(WireChannel.evolveTestWireStatsResponse, {'statsId': statsId, 'stats': stats});
  });

  // Where the client's start went: `evolve.test.bootTrace` {traceId} answers on
  // `evolve.test.bootTraceResponse` {traceId, steps: [{name, depth, startUs, endUs}]}.
  EquoCommService.on(0, 0, WireChannel.evolveTestBootTrace, (dynamic req) async {
    final traceId = req is Map ? req['traceId'] : null;
    _reply(WireChannel.evolveTestBootTraceResponse, {'traceId': traceId, 'steps': await BootTrace.snapshot()});
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
    queryAllStatesByReferenceJson: () => queryAllStatesJson(byReference: true),
    queryStatesJson: queryStatesJson,
    queryStatesOfKindJson: queryStatesOfKindJson,
    queryStatesReachableFromJson: queryStatesReachableFromJson,
    waitForStatesJson: waitForStatesJson,
    queryPaintOpsJson: queryPaintOpsJson,
    queryTreeItemsJson: queryTreeItemsJson,
    expandTreeItem: expandTreeItem,
    queryPrimaryFocus: queryPrimaryFocus,
    // What a page test reads is laid out text, so its frame is one with every font in.
    scheduleFrameSync: (onSynced) => DeferredFonts.all().then((_) => scheduleFrameSync(onSynced)),
    afterNextFrame: (onFramed) => SchedulerBinding.instance.addPostFrameCallback((_) => onFramed()),
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
/// way, as a JSON-encoded object; an identifier the harness does not report is left out. Only the
/// asked-for widgets are described; see [_Reported] for which widgets there are and how they nest.
String queryStatesJson(List<String> identifiers) {
  final reported = _Reported();
  final Map<String, dynamic> found = {};
  for (final identifier in identifiers) {
    final node = _nodeNamed(identifier);
    if (node != null && reported.includes(node)) found[identifier] = reported.describe(node);
  }
  return jsonEncode(found);
}

/// [queryStatesJson] for the widgets named by [identifiers] and every reported widget they hold, at
/// any depth: a widget's subtree (its children, items, their controls) without the rest of the
/// window.
String queryStatesReachableFromJson(List<String> identifiers) {
  final reported = _Reported();
  final Map<String, dynamic> found = {};
  final visited = <int>{};
  final pending = [
    for (final identifier in identifiers)
      if (_nodeNamed(identifier) case final node?) node,
  ];
  while (pending.isNotEmpty) {
    final node = pending.removeLast();
    if (!visited.add(node.key) || !reported.includes(node)) continue;
    found[_keyOf(node)] = reported.describe(node);
    for (final key in node.references) {
      final held = VRegistry.instance.nodeAt(key);
      if (held != null) pending.add(held);
    }
  }
  return jsonEncode(found);
}

/// [queryStatesJson] for every widget whose `{swt}/{id}` starts with one of [prefixes], in the
/// order the client received them.
String queryStatesOfKindJson(List<String> prefixes) =>
    jsonEncode(_Reported().describeWhere((key) => prefixes.any(key.startsWith)));

/// Waits, inside the page, until [conditionJson] holds, and answers once: `{"met": true|false,
/// "states": {...}}`, the states being the widgets that matched (as [queryStatesJson] keys and
/// describes them) when it was met, or when [timeoutMs] ran out.
///
/// The condition is checked when the wait starts, after every frame, and every
/// [_waitSafetyInterval] for a change that reaches the registry without a frame. A caller that
/// polled from outside the page paid a round trip and a serialization per check instead.
///
/// The condition, as JSON:
/// - `kinds`: `{swt}/` prefixes, or whole `{swt}/{id}` keys, of the widgets considered;
/// - `except`: keys left out;
/// - `where`: clauses all of which a widget meets to match, each
///   `{"keys": [property numbers], "op": ..., "values": [...]}`: the property is the first of
///   `keys` the widget holds (a name is numbered per class). `op` is `eq`, `notEq`, `startsWith`,
///   `contains`, `eqIgnoringMnemonic` (any of `values`), `present`, or `absentOrEmpty`;
/// - `expect`: `some` (a widget matches) or `none` (none does);
/// - `painted`: with `some`, met only once a match is painted on screen. The answer then also has
///   `painted`: each match's rectangle, whether it is on screen, and whether input reaches it.
Future<String> waitForStatesJson(String conditionJson, int timeoutMs) {
  final condition = _StateCondition(jsonDecode(conditionJson) as Map<String, dynamic>);
  final done = Completer<String>();
  Timer? safety;
  Timer? deadline;
  void finish(bool met, _Reported reported, List<VNode> matching, Map<int, _PaintFacts>? painted) {
    if (done.isCompleted) return;
    safety?.cancel();
    deadline?.cancel();
    done.complete(jsonEncode({
      'met': met,
      'states': {for (final node in matching) _keyOf(node): reported.describe(node)},
      if (painted != null)
        'painted': {
          for (final node in matching)
            if (painted[node.key] case final facts?) _keyOf(node): facts.toJson()
        },
    }));
  }

  bool check() {
    if (done.isCompleted) return true;
    final reported = _Reported();
    final matching = condition.matching(reported);
    if (condition.expectNone) {
      if (matching.isEmpty) finish(true, reported, matching, null);
      return matching.isEmpty;
    }
    if (matching.isEmpty) return false;
    if (!condition.painted) {
      finish(true, reported, matching, null);
      return true;
    }
    // Where each match is painted, read off the render tree only once the state matches.
    final painted = _paintFactsOf({for (final node in matching) node.key});
    if (!painted.values.any((facts) => facts.onScreen)) return false;
    finish(true, reported, matching, painted);
    return true;
  }

  void afterEachFrame() {
    if (check()) return;
    SchedulerBinding.instance.addPostFrameCallback((_) => afterEachFrame());
  }

  if (!check()) {
    SchedulerBinding.instance.addPostFrameCallback((_) => afterEachFrame());
    safety = Timer.periodic(_waitSafetyInterval, (_) => check());
    deadline = Timer(Duration(milliseconds: timeoutMs), () {
      final reported = _Reported();
      final matching = condition.matching(reported);
      finish(false, reported, matching,
          condition.painted ? _paintFactsOf({for (final node in matching) node.key}) : null);
    });
  }
  return done.future;
}

/// Where a mounted widget is painted and whether input reaches it there.
class _PaintFacts {
  _PaintFacts(this.rect, this.takesInput);

  final Map<String, double>? rect;
  final bool takesInput;

  /// Laid out with a size, at least partly inside the view.
  bool get onScreen {
    final r = rect;
    if (r == null || r['width']! <= 0 || r['height']! <= 0) return false;
    final view = WidgetsBinding.instance.platformDispatcher.implicitView;
    if (view == null) return true;
    final size = view.physicalSize / view.devicePixelRatio;
    return r['x']! + r['width']! > 0 && r['y']! + r['height']! > 0 && r['x']! < size.width && r['y']! < size.height;
  }

  Map<String, Object?> toJson() => {'rect': rect, 'takesInput': takesInput, 'onScreen': onScreen};
}

/// [_PaintFacts] for the mounted widgets with [keys], in one walk of the rendered tree that stops
/// once it has met them all. A widget with no element of its own (an item drawn inline) has none.
Map<int, _PaintFacts> _paintFactsOf(Set<int> keys) {
  final found = <int, _PaintFacts>{};
  final root = WidgetsBinding.instance.rootElement;
  if (root == null || keys.isEmpty) return found;
  void visit(Element el, bool refused) {
    if (found.length == keys.length) return;
    if (_refusesInput(el.widget)) refused = true;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate is VWidget) {
        final key = VRegistry.keyOf(vstate);
        if (keys.contains(key) && !found.containsKey(key)) {
          final box = el.renderObject;
          found[key] = _PaintFacts(box is RenderBox && box.hasSize && box.attached ? _globalRect(box) : null,
              !refused && !_ownWrapperRefuses(el));
        }
      }
    }
    final inherited = refused;
    el.visitChildren((child) => visit(child, inherited));
  }

  visit(root, false);
  return found;
}

/// How often a wait re-checks without a frame: state can change without anything redrawing.
const Duration _waitSafetyInterval = Duration(milliseconds: 200);

String _keyOf(VNode node) => '${node.value.swt}/${node.value.id}';

/// A condition of [waitForStatesJson].
class _StateCondition {
  _StateCondition(Map<String, dynamic> json)
      : kinds = [for (final k in json['kinds'] as List? ?? const []) k as String],
        except = {for (final k in json['except'] as List? ?? const []) k as String},
        clauses = [for (final c in json['where'] as List? ?? const []) _Clause(c as Map<String, dynamic>)],
        expectNone = json['expect'] == 'none',
        painted = json['painted'] == true;

  final List<String> kinds;
  /// Met only once a match is painted on screen; the answer then says where each match is.
  final bool painted;
  final Set<String> except;
  final List<_Clause> clauses;
  final bool expectNone;

  List<VNode> matching(_Reported reported) {
    final found = <VNode>[];
    for (final node in VRegistry.instance.nodes) {
      final key = _keyOf(node);
      if (!kinds.any((kind) => kind.endsWith('/') ? key.startsWith(kind) : key == kind)) continue;
      if (except.contains(key) || !reported.includes(node)) continue;
      if (clauses.every((clause) => clause.holds(node.value))) found.add(node);
    }
    return found;
  }
}

class _Clause {
  _Clause(Map<String, dynamic> json)
      : keys = {for (final k in json['keys'] as List) (k as num).toInt()},
        op = json['op'] as String,
        values = [for (final v in json['values'] as List? ?? const []) '$v'];

  final Set<int> keys;
  final String op;
  final List<String> values;

  bool holds(VWidget widget) {
    Object? value;
    var held = false;
    widget.visitWire((key, v) {
      if (!held && keys.contains(key) && v != null) {
        value = v;
        held = true;
      }
    });
    final text = held ? '$value' : null;
    switch (op) {
      case 'present':
        return held;
      case 'absentOrEmpty':
        return text == null || text.isEmpty;
      case 'eq':
        return text != null && values.contains(text);
      case 'notEq':
        return text == null || !values.contains(text);
      case 'startsWith':
        return text != null && values.any(text.startsWith);
      case 'contains':
        return text != null && values.any(text.contains);
      case 'eqIgnoringMnemonic':
        return text != null && values.contains(text.replaceAll('&', ''));
    }
    throw ArgumentError('unknown condition op "$op"');
  }
}

/// The widget [identifier] (`{swt}/{id}`) names in the registry, or null.
VNode? _nodeNamed(String identifier) {
  final slash = identifier.lastIndexOf('/');
  if (slash <= 0) return null;
  final type = WireNames.typeNumbers[identifier.substring(0, slash)];
  final id = int.tryParse(identifier.substring(slash + 1));
  return type == null || id == null ? null : VRegistry.instance.nodeAt(WidgetKey.of(type, id));
}

/// Which widgets the state queries report, read from what the client holds ([VRegistry]) rather
/// than from the rendered tree, so a query costs what it describes.
///
/// A widget is reported when a mounted widget renders it, or when a reported widget holds it
/// without anything rendering it on its own (an item its parent draws inline). Inside a
/// description, a mounted widget is named by `swt` and `id` only: it is reported under its own key.
class _Reported {
  final VRegistry _registry = VRegistry.instance;
  Set<int>? _held;

  static bool _mounted(VNode node) => node.mounts > 0;

  bool includes(VNode node) =>
      WidgetKey.typeOfKey(node.key) != WireType.gc &&
      (_mounted(node) || (_held ??= _heldByMounted()).contains(node.key));

  /// The widgets reached from the mounted ones through what they hold, past any not mounted.
  Set<int> _heldByMounted() {
    final held = <int>{};
    final pending = [for (final node in _registry.nodes) if (_mounted(node)) node];
    while (pending.isNotEmpty) {
      for (final key in pending.removeLast().references) {
        final node = _registry.nodeAt(key);
        if (node != null && !_mounted(node) && held.add(key)) pending.add(node);
      }
    }
    return held;
  }

  bool byReference(VWidget value) {
    final node = _registry.nodeAt(VRegistry.keyOf(value));
    return node != null && identical(node.value, value) && _mounted(node);
  }

  Map<String, dynamic> describe(VNode node) => describeState(node.value, byReference: byReference);

  /// Every reported widget whose `{swt}/{id}` satisfies [wanted], described, keyed by it.
  Map<String, dynamic> describeWhere(bool Function(String key) wanted) {
    final Map<String, dynamic> found = {};
    for (final node in _registry.nodes) {
      final key = '${node.value.swt}/${node.value.id}';
      if (wanted(key) && includes(node)) found[key] = describe(node);
    }
    return found;
  }
}

/// Every mounted SWT widget's live [describeState], keyed by its `{swt}/{id}` semantics
/// identifier, as a JSON-encoded object. Items rendered inline by their parent (VTreeItem,
/// VTabItem, VExpandItem, ...) have no [WidgetSwtState] of their own, but they ARE nested in
/// the parent's description, so the descriptions are walked rather than the State objects.
///
/// [byReference] answers every widget [queryStatesJson] reports, each described once; without it
/// each description carries its whole subtree, which is what a failure's evidence wants.
String queryAllStatesJson({bool byReference = false}) {
  if (byReference) return jsonEncode(_Reported().describeWhere((_) => true));
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
/// described the same way, a widget met again inside its own description by identity only, and so
/// is a nested widget [byReference] accepts.
Map<String, dynamic> describeState(Object value, {bool Function(VWidget value)? byReference}) =>
    value is WireVisitable
        ? _describe(value, {}, byReference ?? _never, nested: false)
        : <String, dynamic>{};

bool _never(VWidget value) => false;

Map<String, dynamic> _describe(
    WireVisitable value, Set<Object> seen, bool Function(VWidget value) byReference,
    {bool nested = true}) {
  final out = <String, dynamic>{};
  if (value is VWidget) {
    out['swt'] = value.swt;
    out['id'] = value.id;
    if (nested && byReference(value)) return out;
    if (!seen.add(value)) return out;
  }
  value.visitWire((key, v) {
    if (v != null) out['$key'] = _plain(v, seen, byReference);
  });
  return out;
}

Object? _plain(Object? v, Set<Object> seen, bool Function(VWidget value) byReference) =>
    switch (v) {
      null || num() || bool() || String() => v,
      Uint8List() => base64Encode(v),
      List() => [for (final e in v) _plain(e, seen, byReference)],
      Map() => {
          for (final e in v.entries)
            '${e.key}': _plain(e.value, seen, byReference)
        },
      WireVisitable() => _describe(v, seen, byReference),
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
  final reported = _Reported();
  return _takesInputWhere(root, (vstate) => vstate.id == targetId) ??
      _takesInputWhere(
          root,
          (vstate) =>
              _findItemById(describeState(vstate, byReference: reported.byReference), targetId) != null);
}

bool _refusesInput(Widget w) =>
    (w is IgnorePointer && w.ignoring) || (w is ExcludeFocus && w.excluding);

/// Walks down from [root] to the first [WidgetSwtState] whose state satisfies [matches],
/// answering whether input reaches it: no refusing wrapper on the way down, and none in the
/// wrapper chain the widget puts above its own content.
bool? _takesInputWhere(Element root, bool Function(dynamic vstate) matches) {
  bool? answer;

  void visit(Element el, bool refused) {
    if (answer != null) return;
    if (_refusesInput(el.widget)) refused = true;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null && matches(vstate)) {
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

/// The [describeState] of the widget with [targetId], or null when the harness does not report it
/// (see [_Reported]).
Map<String, dynamic>? _findStateById(int targetId) {
  final node = VRegistry.instance.nodeById(targetId);
  return node == null || !_Reported().includes(node) ? null : describeState(node.value);
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
