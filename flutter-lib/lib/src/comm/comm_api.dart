import 'dart:async';
import 'dart:typed_data';

import '../gen/widget.dart';

// FutureOr so an awaiting handler can be awaited — see EquoCommBase's apply queue.
typedef CommCallback<V> = FutureOr<void> Function(V payload);

class EquoCommService {
  static Object on<V extends VWidget>(
          String userEventActionId, CommCallback<V> onSuccess) =>
      throw UnsupportedError("EquoComm.on");
  static Object onRaw(
          String userEventActionId, CommCallback<Object?> onSuccess) =>
      throw UnsupportedError("EquoComm.on");
  static Future send(String userEventActionId) =>
      throw UnsupportedError("EquoComm.send");
  // We want payload to be an Object or dynamic so we can pass a jsinterop later
  static Future sendPayload(String userEventActionId, Object payload) =>
      throw UnsupportedError("EquoComm.sendPayload");
  // Bench-only raw-bytes API. Bypasses JSON encode/decode. Production code
  // should not depend on this; it exists to measure transport floor cost.
  static Future sendBytes(String userEventActionId, Uint8List bytes) =>
      throw UnsupportedError("EquoComm.sendBytes");
  static void onBytes(
          String userEventActionId, FutureOr<void> Function(Uint8List) callback) =>
      throw UnsupportedError("EquoComm.onBytes");
  static void onArrival(
          String userEventActionId, bool Function(Uint8List) handler) =>
      throw UnsupportedError("EquoComm.onArrival");
  static Future setPort(int port) => throw UnsupportedError("EquoComm.setPort");
  static void onReconnect(void Function() callback) =>
      throw UnsupportedError("EquoComm.onReconnect");
  static void remove(String eventName, [Object? token]) =>
      throw UnsupportedError("EquoComm.remove");
}
