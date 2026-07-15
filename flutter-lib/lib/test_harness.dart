import 'dart:convert';

import 'package:flutter/widgets.dart';

import 'src/comm/comm.dart';
import 'src/gen/widget.dart';
import 'src/testing/tree_test_registry.dart';
import 'test_harness_iframe_stub.dart'
    if (dart.library.js_interop) 'test_harness_iframe_web.dart';
import 'test_harness_jsapi_stub.dart'
    if (dart.library.js_interop) 'test_harness_jsapi_web.dart';

/// Test-only support: a dormant comm channel that lets a Java integration test
/// read the *live* Flutter state of any rendered SWT widget by id, and send it
/// back for assertions. It is inert in production — nothing ever sends on these
/// channels there — so it is safe to register unconditionally from `main()`.
///
/// Protocol (JSON):
///   request  on `evolve.test.query`         : { "queryId": int, "targetId": int }
///   response on `evolve.test.queryResponse`  : { "queryId": int, "found": bool,
///                                                "state": the widget's `V*.toJson()` or null }
///
/// `targetId` is the SWT widget id (Java `widget.hashCode()` == `V*.id`). The
/// returned `state` is exactly what the widget's [WidgetSwtState] currently
/// holds — i.e. what Flutter would paint — so a test can assert that a state
/// pushed from Java actually reached and updated the rendered widget.
void registerTestQueryChannel() {
  EquoCommService.onRaw("evolve.test.query", (dynamic req) {
    final map = (req as Map).cast<String, dynamic>();
    final int queryId = (map['queryId'] as num).toInt();
    final int targetId = (map['targetId'] as num).toInt();
    final dynamic vstate = _findStateById(targetId);
    EquoCommService.sendPayload("evolve.test.queryResponse", {
      'queryId': queryId,
      'found': vstate != null,
      'state': vstate?.toJson(),
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

/// Debug description of whichever [FocusNode] currently holds primary keyboard focus --
/// diagnostic-only, for tests narrowing down why a key press did not reach the widget they
/// expected.
String queryPrimaryFocus() {
  return FocusManager.instance.primaryFocus?.toString() ?? '<none>';
}

/// The widget's live `V*.toJson()` as a JSON string, or null if not mounted. Shared by
/// the Java-facing comm channel above and the JS-facing window API (see
/// test_harness_jsapi_web.dart).
String? queryStateJson(int targetId) {
  final dynamic vstate = _findStateById(targetId);
  if (vstate == null) return null;
  return jsonEncode(vstate.toJson());
}

/// Every mounted SWT widget's live `V*.toJson()`, keyed by its `{swt}/{id}` semantics
/// identifier, as a JSON-encoded object.
String queryAllStatesJson() {
  final root = WidgetsBinding.instance.rootElement;
  final Map<String, dynamic> all = {};
  if (root == null) return jsonEncode(all);
  void visit(Element el) {
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null) {
        all['${vstate.swt}/${vstate.id}'] = vstate.toJson();
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return jsonEncode(all);
}

/// Runs [onSynced] once the next frame has been built + painted (see frameSync above).
void scheduleFrameSync(void Function() onSynced) {
  WidgetsBinding.instance.addPostFrameCallback((_) => onSynced());
  WidgetsBinding.instance.scheduleFrame();
}

/// Walk the live element tree and return the `V*` value object of the
/// [WidgetSwtState] whose `state.id` matches [targetId], or null if not mounted.
dynamic _findStateById(int targetId) {
  final root = WidgetsBinding.instance.rootElement;
  if (root == null) return null;
  dynamic result;
  void visit(Element el) {
    if (result != null) return;
    if (el is StatefulElement && el.state is WidgetSwtState) {
      final dynamic vstate = (el.state as WidgetSwtState).state;
      if (vstate != null && vstate.id == targetId) {
        result = vstate;
        return;
      }
    }
    el.visitChildren(visit);
  }

  root.visitChildren(visit);
  return result;
}
