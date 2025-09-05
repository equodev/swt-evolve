import 'dart:convert';
import 'dart:js_interop';

import '../gen/widget.dart';
import '../gen/widgets.dart';

import 'comm_api.dart';
import '../EquoCommService.dart' as impl;

class EquoCommService {
  static int port = 0;

  static void onRaw(String userEventActionId, CommCallback<Object?> onSuccess) {
    // print("comm chromium on: $userEventActionId");
    void jsCallback(JSAny? payload) {
      print("comm chromium on jsCallback");
      // onSuccess(payload.toString());
      onSuccess(payload.dartify());
      print("comm chromium on jsCallback after onSuccess");
    }

    impl.EquoCommService.on(userEventActionId, jsCallback.toJS);
    // print("comm chromium on after: $userEventActionId");
  }

  static void on<V extends VWidget>(
      String userEventActionId, CommCallback<V> onSuccess) {
    // print("comm chromium on: $userEventActionId");
    void jsCallback(String? payload) {
      // print("comm chromium on jsCallback: $payload");
      var map = jsonDecode(payload!);
      // JSObject o = impl.JSON.parse(payload);
      // print("comm chromium on jsCallback after parse: $map");
      var widgetValue = mapWidgetValue(map);
      // print("comm chromium on jsCallback after map: $widgetValue");
      onSuccess(widgetValue as V);
      // print("comm chromium on jsCallback after onSucess");
    }

    impl.EquoCommService.on(userEventActionId, jsCallback.toJS);
    // print("comm chromium on after: $userEventActionId");
  }

  static Future send(String userEventActionId) {
    print("comm chromium send");
    final promise = impl.EquoCommService.send(userEventActionId);
    print("comm chromium after send $promise");
    // return (port != 0) ? Future.value() : promise.toDart;
    return Future.delayed(const Duration(milliseconds: 1));
    // return Future.value();
  }

  static Future sendPayload(String userEventActionId, Object payload) {
    print("comm chromium sendPayload");
    final payloadJS = switch (payload) {
      num i => i.toJS,
      bool b => b.toJS,
      String str => str.toJS,
      _ => jsonEncode(payload).toJS,
    };
    print("json: $payloadJS");
    final promise =
        impl.EquoCommService.sendPayload(userEventActionId, payloadJS);
    print("comm chromium after sendPauload $promise");
    return Future.delayed(const Duration(milliseconds: 2));
    // print("comm_chromium after sendPayload");
  }

  static void remove(String eventName) {
    impl.EquoCommService.remove(eventName);
  }

  static Future setPort(int p) {
    port = p;
    return Future.value();
  }
}
