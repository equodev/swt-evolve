import 'dart:async';
import 'dart:typed_data';

import '../gen/widget.dart';
import 'widget_wire.dart' show WidgetWireWriter;

// FutureOr so an awaiting handler can be awaited — see EquoCommBase's apply queue.
typedef CommCallback<V> = FutureOr<void> Function(V payload);

/// Handles one channel for every widget that answers it: [id] is the widget the frame addresses and
/// its payload is `frame[start..end]`. Returns false when no widget with that id listens yet, and
/// the frame is held until one does (see `EquoCommBase.replayWidgetOps`).
typedef WidgetOpHandler = FutureOr<bool> Function(int id, Uint8List frame, int start, int end);

/// Handles a channel's frames read where they lie: the payload is `data[start..end]`.
typedef BytesAtHandler = FutureOr<void> Function(Uint8List data, int start, int end);

class EquoCommService {
  static Object on(int type, int id, int channel, CommCallback<Object?> onSuccess) =>
      throw UnsupportedError("EquoComm.on");
  static void onFirstGcMessage(int id, void Function() arrived) =>
      throw UnsupportedError("EquoComm.onFirstGcMessage");
  static void cancelFirstGcMessage(int id, void Function() arrived) =>
      throw UnsupportedError("EquoComm.cancelFirstGcMessage");
  static void sendTo(int type, int id, int channel, [Object? payload]) =>
      throw UnsupportedError("EquoComm.sendTo");
  static void sendOn(int channel, [Object? payload]) =>
      throw UnsupportedError("EquoComm.sendOn");
  static void sendFrame(Uint8List frame) => throw UnsupportedError("EquoComm.sendFrame");
  static void sendWritten(WidgetWireWriter w) => throw UnsupportedError("EquoComm.sendWritten");
  static void onBytesAt(int channel, BytesAtHandler handler) =>
      throw UnsupportedError("EquoComm.onBytesAt");
  static void onBytes(int type, int id, int channel, FutureOr<void> Function(Uint8List) callback) =>
      throw UnsupportedError("EquoComm.onBytes");
  static void onArrival(int type, int id, int channel, bool Function(Uint8List) handler) =>
      throw UnsupportedError("EquoComm.onArrival");
  static Future setPort(int port) => throw UnsupportedError("EquoComm.setPort");
  static void onReconnect(void Function() callback) =>
      throw UnsupportedError("EquoComm.onReconnect");
  static void remove(int type, int id, int channel, [Object? token]) =>
      throw UnsupportedError("EquoComm.remove");
  static FutureOr<void> deliverEntry(int type, int id, int channel, Object? payload) =>
      throw UnsupportedError("EquoComm.deliverEntry");
  static void onWidgetOp(int type, int channel, WidgetOpHandler handler) =>
      throw UnsupportedError("EquoComm.onWidgetOp");
  static void replayWidgetOps(int id) => throw UnsupportedError("EquoComm.replayWidgetOps");

  static void forgetWidgetOps(int id) => throw UnsupportedError("EquoComm.forgetWidgetOps");
  static void ensureWidgetOps(void Function() register) =>
      throw UnsupportedError("EquoComm.ensureWidgetOps");
  static FutureOr<void> deliverEscape(Uint8List bytes, int headerAt, int start, int end) =>
      throw UnsupportedError("EquoComm.deliverEscape");
}
