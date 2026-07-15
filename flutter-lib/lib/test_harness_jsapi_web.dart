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
///   await new Promise(r => window.evolveTest.waitForFrame(r));
void registerTestQueryJsApi({
  required String? Function(int) queryStateJson,
  required String Function() queryAllStatesJson,
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
    'queryAllStates'.toJS,
    (() => queryAllStatesJson().toJS).toJS,
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
