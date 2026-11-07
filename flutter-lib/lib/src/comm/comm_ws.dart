import 'dart:async';
import 'dart:ffi';
import 'dart:io';
import 'dart:isolate';
import 'dart:typed_data';

import 'package:meta/meta.dart';

import '../gen/widget.dart';
import 'comm_api.dart';
import 'comm_frame.dart';
import 'widget_wire.dart' show WidgetWireWriter;
import '../testing/boot_trace.dart';

/// Desktop comm: a `dart:io` WebSocket speaking the binary frame protocol. All
/// protocol logic lives in [EquoCommBase]; this class only owns the socket.
class _DesktopComm extends EquoCommBase {
  WebSocket? _socket;

  _DesktopComm({required String host, required int port}) {
    BootTrace.markOnce('comm.connect');
    WebSocket.connect("ws://$host:$port").then((ws) {
      _socket = ws;
      markOpen();
      ws.listen(
        (event) {
          if (event is List<int>) {
            receiveBinary(event is Uint8List ? event : Uint8List.fromList(event));
          }
        },
        onDone: () => print("[comm_ws] WS onDone"),
        onError: (err, st) => print("[comm_ws] WS onError $err $st"),
      );
    });
  }

  @override
  void rawSend(Uint8List frame) => _socket?.add(frame);
}

/// Static facade over the desktop transport. Construction is the only part that
/// differs from the web facade (which builds a [_DesktopComm]'s browser counterpart);
/// everything else delegates to [EquoCommBase].
class EquoCommService {
  static int port = 0;
  static EquoCommBase? _impl;

  static EquoCommBase get _comm => _impl ??= _create();

  /// The live transport, so a widget test can deliver an inbound frame through
  /// [EquoCommBase.receiveBinary]. Read-only: it cannot alter production wiring.
  @visibleForTesting
  static EquoCommBase get commForTesting => _comm;

  static EquoCommBase _create() {
    final p = _getPort();
    return p != 0 ? _DesktopComm(host: "localhost", port: p) : NoComm();
  }

  /// See [EquoCommBase.on].
  static Object on(int type, int id, int channel, CommCallback<dynamic> onSuccess) =>
      _comm.on(type, id, channel, onSuccess);

  /// See [EquoCommBase.onWidgetOp].
  static void onWidgetOp(int type, int channel, WidgetOpHandler handler) =>
      _comm.onWidgetOp(type, channel, handler);

  /// See [EquoCommBase.replayWidgetOps].
  static void replayWidgetOps(int id) => _comm.replayWidgetOps(id);

  /// See [EquoCommBase.forgetWidgetOps].
  static void forgetWidgetOps(int id) => _comm.forgetWidgetOps(id);

  /// See [EquoCommBase.ensureWidgetOps].
  static void ensureWidgetOps(void Function() register) => _comm.ensureWidgetOps(register);

  /// See [EquoCommBase.deliverEscape].
  static FutureOr<void> deliverEscape(Uint8List bytes, int headerAt, int start, int end) =>
      _comm.deliverEscape(bytes, headerAt, start, end);

  /// See [EquoCommBase.onFirstGcMessage].
  static void onFirstGcMessage(int id, void Function() arrived) => _comm.onFirstGcMessage(id, arrived);

  /// See [EquoCommBase.cancelFirstGcMessage].
  static void cancelFirstGcMessage(int id, void Function() arrived) =>
      _comm.cancelFirstGcMessage(id, arrived);

  /// See [EquoCommBase.sendTo].
  static void sendTo(int type, int id, int channel, [Object? payload]) =>
      _comm.sendTo(type, id, channel, payload);

  /// See [EquoCommBase.sendFrame].
  static void sendFrame(Uint8List frame) => _comm.sendFrame(frame);

  /// See [EquoCommBase.sendWritten].
  static void sendWritten(WidgetWireWriter w) => _comm.sendWritten(w);

  /// See [EquoCommBase.sendOn].
  static void sendOn(int channel, [Object? payload]) => _comm.sendOn(channel, payload);

  /// See [EquoCommBase.onBytesAt].
  static void onBytesAt(int channel, BytesAtHandler handler) => _comm.onBytesAt(channel, handler);

  /// See [EquoCommBase.onBytes].
  static void onBytes(int type, int id, int channel, FutureOr<void> Function(Uint8List) callback) =>
      _comm.onBytes(type, id, channel, callback);

  /// See [EquoCommBase.onArrival].
  static void onArrival(int type, int id, int channel, bool Function(Uint8List) handler) =>
      _comm.onArrival(type, id, channel, handler);

  /// See [EquoCommBase.remove].
  static void remove(int type, int id, int channel, [Object? token]) => _comm.remove(type, id, channel, token);

  /// Delivers [payload] on the channel these numbers name as if it had arrived in a batch: an entry
  /// of a GC's binary frame.
  static FutureOr<void> deliverEntry(int type, int id, int channel, Object? payload) =>
      _comm.deliverEntry(type, id, channel, payload);

  /// Registers the callback fired when the socket comes back after a drop. Never fires on
  /// desktop: the socket is a local link to the JVM that owns this window, so it only ends
  /// when that JVM does — there is nothing to reconnect to.
  static void onReconnect(void Function() callback) =>
      _comm.onReconnected = callback;

  static Future setPort(int p) async {
    port = p;
    if (p != 0) _comm; // trigger connection with the configured port
  }

  static int _getPort() => port != 0
      ? port
      : const int.fromEnvironment("equo.comm_port", defaultValue: 0);
}
