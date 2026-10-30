import 'dart:async';
import 'dart:typed_data';

import '../gen/widget.dart';

// FutureOr so an awaiting handler can be awaited — see EquoCommBase's apply queue.
typedef CommCallback<V> = FutureOr<void> Function(V payload);

/// Gives the handler for a name under a routed base channel, or null: see `EquoCommBase.onRouted`.
typedef ChannelRouter = FutureOr<void> Function(Object? payload)? Function(String name);

class EquoCommService {
  static Object onRaw(
          String userEventActionId, CommCallback<Object?> onSuccess) =>
      throw UnsupportedError("EquoComm.on");
  static Object onRouted(String base, ChannelRouter resolve) =>
      throw UnsupportedError("EquoComm.onRouted");
  static void onFirstGcMessage(int id, void Function() arrived) =>
      throw UnsupportedError("EquoComm.onFirstGcMessage");
  static void forgetHeld(String owner) => throw UnsupportedError("EquoComm.forgetHeld");
  static void cancelFirstGcMessage(int id, void Function() arrived) =>
      throw UnsupportedError("EquoComm.cancelFirstGcMessage");
  static Future send(String userEventActionId) =>
      throw UnsupportedError("EquoComm.send");
  // We want payload to be an Object or dynamic so we can pass a jsinterop later
  static Future sendPayload(String userEventActionId, Object payload) =>
      throw UnsupportedError("EquoComm.sendPayload");
  // Raw-bytes API: the body goes on the wire verbatim, neither encoded nor decoded.
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
  static FutureOr<void> deliverEntry(String channel, Object? payload) =>
      throw UnsupportedError("EquoComm.deliverEntry");
}
