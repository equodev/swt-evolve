import 'dart:js_interop';

@JS()
extension type EquoCommService._(JSObject _) implements JSObject {
  external static void on(
      String userEventActionId, JSFunction onSuccessCallback);
  external static JSPromise send(String userEventActionId);
  @JS("send")
  external static JSPromise sendPayload(String userEventActionId, JSAny? payload);
  external static void remove(String eventName);
}

@JS()
extension type JSON._(JSObject _) implements JSObject {
  external static JSObject parse(String json);
}
