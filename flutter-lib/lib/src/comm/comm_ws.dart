import 'dart:io';
import 'dart:typed_data';

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

/// Static facade over the desktop transport. Construction is the only part that
/// differs from the web facade (which builds a [_DesktopComm]'s browser counterpart);
/// everything else delegates to [EquoCommBase].
class EquoCommService {
  static int port = 0;
  static EquoCommBase? _impl;

  static EquoCommBase get _comm => _impl ??= _create();

  static EquoCommBase _create() {
    final p = _getPort();
    return p != 0 ? _DesktopComm(host: "localhost", port: p) : NoComm();
  }

  static void onRaw(String userEventActionId, CommCallback<dynamic> onSuccess) =>
      _comm.on(userEventActionId, onSuccess);

  static Object on<V extends VWidget>(
          String userEventActionId, CommCallback<V> onSuccess) =>
      _comm.onWidget<V>(userEventActionId, onSuccess);

  static Future send(String userEventActionId) => _comm.send(userEventActionId);

  static Future sendPayload(String userEventActionId, Object payload) =>
      _comm.send(userEventActionId, payload);

  static Future sendBytes(String userEventActionId, Uint8List bytes) =>
      _comm.sendBytes(userEventActionId, bytes);

  static void onBytes(
          String userEventActionId, void Function(Uint8List) callback) =>
      _comm.onBytes(userEventActionId, callback);

  static void remove(eventName, [Object? token]) => _comm.remove(eventName, token);

  static Future setPort(int p) async {
    port = p;
    if (p != 0) _comm; // trigger connection with the configured port
  }

  static int _getPort() => port != 0
      ? port
      : const int.fromEnvironment("equo.comm_port", defaultValue: 0);
}
