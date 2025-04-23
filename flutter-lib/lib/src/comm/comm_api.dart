
import '../swt/widget.dart';

typedef CommCallback<V> = void Function(V payload);

class EquoCommService {
  static void on<V extends WidgetValue>(
      String userEventActionId, CommCallback<V> onSuccess) => throw UnsupportedError("EquoComm.on");
  static void onRaw(
      String userEventActionId, CommCallback<Object?> onSuccess) => throw UnsupportedError("EquoComm.on");
  static Future send(String userEventActionId) => throw UnsupportedError("EquoComm.send");
  // We want payload to be an Object or dynamic so we can pass a jsinterop later
  static Future sendPayload(String userEventActionId, Object payload) => throw UnsupportedError("EquoComm.sendPayload");
  static Future setPort(int port) => throw UnsupportedError("EquoComm.setPort");
  static void remove(String eventName) => throw UnsupportedError("EquoComm.remove");
}