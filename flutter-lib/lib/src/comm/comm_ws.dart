import 'dart:async';
import 'dart:ffi';
import 'dart:io';
import 'dart:isolate';
import 'dart:typed_data';

import 'package:meta/meta.dart';

import '../gen/widget.dart';
import 'comm_api.dart';
import 'comm_frame.dart';

/// Desktop comm: a `dart:io` WebSocket speaking the binary frame protocol. All
/// protocol logic lives in [EquoCommBase]; this class only owns the socket.
class _DesktopComm extends EquoCommBase {
  WebSocket? _socket;

  _DesktopComm({required String host, required int port}) {
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

@Native<Void Function(Int32, Int64, Pointer<Void>)>(symbol: 'equo_comm_attach')
external void _attach(int id, int port, Pointer<Void> postCObject);

@Native<Void Function(Int32, Pointer<Uint8>, IntPtr)>(symbol: 'equo_comm_send', isLeaf: true)
external void _send(int id, Pointer<Uint8> frame, int length);

/// Desktop comm with a JVM in this process: frames reach this isolate's port from the native bridge,
/// and go back through a call into it, with no socket in between. Java names such a channel with a
/// negative port.
class _InProcessComm extends EquoCommBase {
  final int _id;
  final ReceivePort _port = ReceivePort('equo.comm');

  _InProcessComm(this._id) {
    _port.listen((message) {
      if (message is Uint8List) receiveBinary(message);
    });
    _attach(_id, _port.sendPort.nativePort, NativeApi.postCObject.cast());
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) => _send(_id, frame.address, frame.length);
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
    if (p < 0) return _InProcessComm(-p);
    return p != 0 ? _DesktopComm(host: "localhost", port: p) : NoComm();
  }

  static Object onRaw(String userEventActionId, CommCallback<dynamic> onSuccess) =>
      _comm.on(userEventActionId, onSuccess);

  /// See [EquoCommBase.onRouted]; [remove] with the base and the token returned undoes it.
  static Object onRouted(String base, ChannelRouter resolve) => _comm.onRouted(base, resolve);

  /// See [EquoCommBase.onFirstGcMessage].
  static void onFirstGcMessage(int id, void Function() arrived) => _comm.onFirstGcMessage(id, arrived);

  /// See [EquoCommBase.forgetHeld].
  static void forgetHeld(String owner) => _comm.forgetHeld(owner);

  /// See [EquoCommBase.cancelFirstGcMessage].
  static void cancelFirstGcMessage(int id, void Function() arrived) =>
      _comm.cancelFirstGcMessage(id, arrived);

  static Future send(String userEventActionId) => _comm.send(userEventActionId);

  static Future sendPayload(String userEventActionId, Object payload) =>
      _comm.send(userEventActionId, payload);

  static Future sendBytes(String userEventActionId, Uint8List bytes) =>
      _comm.sendBytes(userEventActionId, bytes);

  static void onBytes(
          String userEventActionId, FutureOr<void> Function(Uint8List) callback) =>
      _comm.onBytes(userEventActionId, callback);

  static void onArrival(
          String userEventActionId, bool Function(Uint8List) handler) =>
      _comm.onArrival(userEventActionId, handler);

  static void remove(eventName, [Object? token]) => _comm.remove(eventName, token);

  /// Delivers [payload] on [channel] as if it had arrived in a batch: an entry of a GC's binary frame.
  static FutureOr<void> deliverEntry(String channel, Object? payload) =>
      _comm.deliverEntry(channel, payload);

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
