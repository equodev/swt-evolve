import 'dart:convert';

import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter/widgets.dart';

import 'src/comm/comm.dart';
import 'src/gen/widget.dart';
import 'src/testing/tree_test_registry.dart';
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
/// Protocol (JSON):
///   request  on `evolve.test.query`         : { "queryId": int, "targetId": int }
///   response on `evolve.test.queryResponse`  : { "queryId": int, "found": bool,
///                                                "state": the widget's `V*.toJson()` or null,
///                                                "render": render-tree facts or null }
///
/// `targetId` is the SWT widget id (Java `widget.hashCode()` == `V*.id`). The
/// returned `state` is exactly what the widget's [WidgetSwtState] currently
/// holds — i.e. what Flutter would paint — so a test can assert that a state
/// pushed from Java actually reached and updated the rendered widget. `render`
/// reports what only the rendered tree can answer; see [_renderFacts].
void registerTestQueryChannel() {
  EquoCommService.onRaw("evolve.test.query", (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int queryId = (map['queryId'] as num).toInt();
    final int targetId = (map['targetId'] as num).toInt();
    final Map<String, dynamic>? vstate = _findStateById(targetId);
    EquoCommService.sendPayload("evolve.test.queryResponse", {
      'queryId': queryId,
      'found': vstate != null,
      'state': vstate,
      'render': _renderFacts(targetId),
    });
  });
  // Frame barrier: ack once the next frame has been built + painted, so a test can wait for pushed
  // state to actually render instead of pumping an arbitrary number of times. We schedule a frame
  // unconditionally (the pushed state may or may not have dirtied anything) and ack from the
  // post-frame callback, which runs after build/layout/paint of that frame.
  EquoCommService.onRaw("evolve.test.frameSync", (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int syncId = (map['syncId'] as num).toInt();
    scheduleFrameSync(() {
      EquoCommService.sendPayload("evolve.test.frameSynced", {
        'syncId': syncId,
      });
    });
  });

  // Result channel for the debug UI-thread runner (see test_harness_actions.dart `openPref` /
  // `listPrefPages`). Java answers a `listPrefPages` request here; we cache the latest payload so a
  // follow-up `lastPrefPages()` eval can read it (a single eval can't span the async Java round-trip).
  EquoCommService.onRaw("swt.evolve.test.runUiResponse", (dynamic resp) {
    if (resp is Map) _lastRunUiResponse = resp.cast<String, dynamic>();
  });

  // Relay embedded-iframe load pings to Java (web only; no-op elsewhere).
  registerIframeLoadProbe();

  // Same query/frame-sync logic, but reachable directly from `window` (web only; no-op
  // elsewhere) — lets a browser-side driver (Playwright etc.) assert on live Dart state
  // without going through Java/the comm WebSocket at all. See test_harness_jsapi_web.dart.
  registerTestQueryJsApi(
    queryStateJson: queryStateJson,
    queryAllStatesJson: queryAllStatesJson,
    queryTreeItemsJson: queryTreeItemsJson,
    expandTreeItem: expandTreeItem,
    queryPrimaryFocus: queryPrimaryFocus,
    scheduleFrameSync: scheduleFrameSync,
  );
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

/// The widget's live `V*.toJson()` as a JSON string, or null if not mounted. Shared by
/// the Java-facing comm channel above and the JS-facing window API (see
/// test_harness_jsapi_web.dart).
String? queryStateJson(int targetId) {
  final Map<String, dynamic>? vstate = _findStateById(targetId);
  if (vstate == null) return null;
  return jsonEncode(vstate);
}

/// Every mounted SWT widget's live `V*.toJson()`, keyed by its `{swt}/{id}` semantics
/// identifier, as a JSON-encoded object. Items rendered inline by their parent (VTreeItem,
/// VTabItem, VExpandItem, ...) have no [WidgetSwtState] of their own, but they ARE nested in
/// the parent's serialized `items` — so the JSON maps are walked instead of the V objects
/// (no dynamic member dispatch, which dart2js can't resolve for a field only some V* have).
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
        addWithItems(vstate.toJson() as Map<String, dynamic>);
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return jsonEncode(all);
}

/// The serialized node's `items` children, or empty when it has none. `toJson()` (no
/// `explicitToJson`) leaves nested V objects raw — they only become maps inside `jsonEncode`,
/// via `toEncodable` — so raw items are serialized here the same way.
List<Map<String, dynamic>> _itemsOf(Map<String, dynamic> node) {
  final dynamic items = node['items'];
  if (items is! List) return const [];
  final List<Map<String, dynamic>> result = [];
  for (final dynamic item in items) {
    if (item == null) continue;
    if (item is Map<String, dynamic>) {
      result.add(item);
      continue;
    }
    try {
      result.add((item as dynamic).toJson() as Map<String, dynamic>);
    } catch (_) {
      // not a serializable value object — skip
    }
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
Map<String, dynamic>? _renderFacts(int targetId) {
  final bool? takesInput = _takesInput(targetId);
  if (takesInput == null) return null;
  return {'takesInput': takesInput};
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
      if (vstate != null && matches(vstate.toJson() as Map<String, dynamic>)) {
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

/// Walk the live element tree and return the serialized (`toJson`) state of the
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
        result = findIn(vstate.toJson() as Map<String, dynamic>);
        if (result != null) return;
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return result;
}
