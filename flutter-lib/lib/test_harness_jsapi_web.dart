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
///   const flat = window.evolveTest.queryAllStatesByReference(); // the same, nested widgets named only
///   const some = window.evolveTest.queryStates(['Label/9']); // String, the same map for those ids
///   const kind = window.evolveTest.queryStatesOfKind(['Shell/']); // the same, for keys with a prefix
///   const tree = window.evolveTest.queryStatesReachableFrom(['Composite/9']); // those and all they hold
///   const met  = await window.evolveTest.waitForStates(conditionJson, timeoutMs); // see waitForStatesJson
///   const ops  = window.evolveTest.queryPaintOps();         // String, JSON map {swt/id: [op, ...]}
///   await new Promise(r => window.evolveTest.waitForFrame(r));
///   await new Promise(r => window.evolveTest.afterNextChange(r)); // the next frame, or state applied without one
void registerTestQueryJsApi({
  required String Function() styledTextPerfJson,
  required String? Function(int) renderFactsJson,
  required String? Function(int) queryStateJson,
  required String Function() queryAllStatesJson,
  required String Function() queryAllStatesByReferenceJson,
  required String Function(List<String>) queryStatesJson,
  required String Function(List<String>) queryStatesOfKindJson,
  required String Function(List<String>) queryStatesReachableFromJson,
  required Future<String> Function(String condition, int timeoutMs) waitForStatesJson,
  required String Function() queryPaintOpsJson,
  required String Function() queryTreeItemsJson,
  required bool Function(String) expandTreeItem,
  required String Function() queryPrimaryFocus,
  required void Function(void Function()) scheduleFrameSync,
  required void Function(void Function()) afterNextChange,
  required String Function(List<String>) locateJson,
  required String Function(List<String>, String?, bool) nodesJson,
  required String Function(String, bool, String) textsJson,
  required bool Function(String) reveal,
}) {
  // Kept when it exists: an app built on this one may have added its own entries first.
  final api = globalContext.getProperty<JSObject?>('evolveTest'.toJS) ?? JSObject();
  api.setProperty(
    'queryState'.toJS,
    ((JSNumber targetId) => queryStateJson(targetId.toDartInt)?.toJS).toJS,
  );
  api.setProperty(
    'locate'.toJS,
    ((JSArray<JSString> identifiers) =>
        locateJson([for (final id in identifiers.toDart) id.toDart]).toJS).toJS,
  );
  api.setProperty(
    'nodes'.toJS,
    ((JSArray<JSString> prefixes, JSString? within, JSBoolean? paintedText) => nodesJson(
            [for (final p in prefixes.toDart) p.toDart], within?.toDart, paintedText?.toDart ?? false).toJS).toJS,
  );
  api.setProperty(
    'reveal'.toJS,
    ((JSString identifier) => reveal(identifier.toDart).toJS).toJS,
  );
  api.setProperty(
    'texts'.toJS,
    ((JSString text, JSBoolean exact, JSString within) =>
        textsJson(text.toDart, exact.toDart, within.toDart).toJS).toJS,
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
    'queryAllStatesByReference'.toJS,
    (() => queryAllStatesByReferenceJson().toJS).toJS,
  );
  api.setProperty(
    'queryStates'.toJS,
    ((JSArray<JSString> identifiers) => queryStatesJson(
      identifiers.toDart.map((id) => id.toDart).toList(),
    ).toJS).toJS,
  );
  api.setProperty(
    'queryStatesOfKind'.toJS,
    ((JSArray<JSString> prefixes) => queryStatesOfKindJson(
      prefixes.toDart.map((prefix) => prefix.toDart).toList(),
    ).toJS).toJS,
  );
  api.setProperty(
    'queryStatesReachableFrom'.toJS,
    ((JSArray<JSString> identifiers) => queryStatesReachableFromJson(
      identifiers.toDart.map((id) => id.toDart).toList(),
    ).toJS).toJS,
  );
  api.setProperty(
    'waitForStates'.toJS,
    ((JSString condition, JSNumber timeoutMs) =>
        waitForStatesJson(condition.toDart, timeoutMs.toDartInt).then((json) => json.toJS).toJS).toJS,
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
  api.setProperty(
    'afterNextChange'.toJS,
    ((JSFunction onDone) {
      afterNextChange(() => onDone.callAsFunction());
    }).toJS,
  );
  globalContext.setProperty('evolveTest'.toJS, api);
}
