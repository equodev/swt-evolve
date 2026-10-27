import 'dart:js_interop';
import 'dart:js_interop_unsafe';

/// Test-only (web): exposes the same query/frame-sync logic [registerTestQueryChannel]
/// wires up for Java, directly on `window.evolveTest` — so a browser-side driver
/// (Playwright etc.) can assert on live Dart state without a Java process or the comm
/// WebSocket in the loop at all. Inert in production: nothing calls these unless a test
/// does.
///
/// JS usage:
///   const json = window.evolveTest.queryState(targetId);   // String | null, JSON-encoded V*
///   const all  = window.evolveTest.queryAllStates();        // String, JSON map {swt/id: V*}
///   const some = window.evolveTest.queryStates(['Label/9']); // String, the same map for those ids
///   const ops  = window.evolveTest.queryPaintOps();         // String, JSON map {swt/id: [op, ...]}
///   await new Promise(r => window.evolveTest.waitForFrame(r));
void registerTestQueryJsApi({
  required String Function() styledTextPerfJson,
  required String? Function(int) renderFactsJson,
  required String? Function(int) queryStateJson,
  required String Function() queryAllStatesJson,
  required String Function(List<String>) queryStatesJson,
  required String Function() queryPaintOpsJson,
  required String Function() queryTreeItemsJson,
  required bool Function(String) expandTreeItem,
  required String Function() queryPrimaryFocus,
  required void Function(void Function()) scheduleFrameSync,
}) {
  final api = JSObject();
  api.setProperty(
    'queryState'.toJS,
    ((JSNumber targetId) => queryStateJson(targetId.toDartInt)?.toJS).toJS,
  );
  api.setProperty(
    'renderFacts'.toJS,
    ((JSNumber targetId) => renderFactsJson(targetId.toDartInt)?.toJS).toJS,
  );
  api.setProperty(
    'styledTextPerf'.toJS,
    (() => styledTextPerfJson().toJS).toJS,
  );
  api.setProperty(
    'queryAllStates'.toJS,
    (() => queryAllStatesJson().toJS).toJS,
  );
  api.setProperty(
    'queryStates'.toJS,
    ((JSArray<JSString> identifiers) => queryStatesJson(
      identifiers.toDart.map((id) => id.toDart).toList(),
    ).toJS).toJS,
  );
  api.setProperty(
    'queryPaintOps'.toJS,
    (() => queryPaintOpsJson().toJS).toJS,
  );
  api.setProperty(
    'queryTreeItems'.toJS,
    (() => queryTreeItemsJson().toJS).toJS,
  );
  api.setProperty(
    'expandTreeItem'.toJS,
    ((JSString itemIdentifier) => expandTreeItem(
      itemIdentifier.toDart,
    ).toJS).toJS,
  );
  api.setProperty(
    'queryPrimaryFocus'.toJS,
    (() => queryPrimaryFocus().toJS).toJS,
  );
  api.setProperty(
    'waitForFrame'.toJS,
    ((JSFunction onDone) {
      scheduleFrameSync(() => onDone.callAsFunction());
    }).toJS,
  );
  globalContext.setProperty('evolveTest'.toJS, api);
}
