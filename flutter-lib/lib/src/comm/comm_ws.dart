import 'dart:convert';
import 'dart:io';

import '../gen/widgets.dart';
import '../gen/widget.dart';
import 'comm_api.dart';

class EquoCommService {
  static int port = 0;
  static final impl = EquoComm(host: "localhost", port: _getPort());

  static void onRaw(String userEventActionId, CommCallback<dynamic> onSuccess) {
    // print("comm ws onraw: $userEventActionId");
    void callback(dynamic payload) {
      // print("comm ws on callback: ${payload ?? 'null'}");
      onSuccess(payload);
      // print("comm ws on callback after onSuccess");
    }

    impl.on(userEventActionId, callback);
    // print("comm chromium on after: $userEventActionId");
  }

  static void on<V extends VWidget>(
      String userEventActionId, CommCallback<V> onSuccess) {
    // print("comm ws on: $userEventActionId");
    void callback(dynamic payload) {
      // print("comm ws on callback: ${payload ?? 'null'}");
      var map = jsonDecode(payload);
      // JSObject o = impl.JSON.parse(payload);
      // print("comm chromium on jsCallback after parse: $map");
      var widgetValue = mapWidgetValue(map);
      // print("comm ws on callback after map: $widgetValue");
      onSuccess(widgetValue as V);
      // print("comm ws on callback after onSucess");
    }

    impl.on(userEventActionId, callback);
    // print("comm chromium on after: $userEventActionId");
  }

  static Future send(String userEventActionId) {
    return impl.send(userEventActionId);
  }

  static Future sendPayload(String userEventActionId, Object payload) {
    // print("comm ws sendPayload");
    final fut = impl.send(userEventActionId, payload);
    // print("comm ws after sendPayload");
    return fut;
  }

  static void remove(eventName) {
    impl.remove(eventName);
  }

  static Future setPort(int port) async {
    EquoCommService.port = port;
    await EquoCommService.impl.ws;
  }

  static int _getPort() {
    final p = port != 0
        ? port
        : const int.fromEnvironment("equo.comm_port", defaultValue: 0);
    return p;
  }
}

typedef OnSuccessCallback<T> = void Function(T response);
typedef OnErrorCallback = void Function(SDKCommError error);
typedef Payload = dynamic;

class SendArgs {
  bool sequential;
  SendArgs({required this.sequential});
}

class CallbackArgs {
  bool once;
  CallbackArgs({required this.once});
}

class SDKCommError {
  int? code;
  String message;
  SDKCommError({this.code, required this.message});
}

class UserEvent {
  late String actionId;
  Payload? payload;
  SDKCommError? error;
  UserEvent({required this.actionId, this.payload, this.error});
}

class SDKMessage extends UserEvent {
  String? callbackId;
  SDKMessage(
      {this.callbackId, required super.actionId, super.payload, super.error});
}

class UserEventCallback {
  String? id;
  OnSuccessCallback<dynamic> onSuccess;
  OnErrorCallback? onError;
  CallbackArgs? args;
  UserEventCallback(
      {this.id, required this.onSuccess, this.onError, this.args});
}

class EquoComm {
  Map<String, UserEventCallback> userEventCallbacks = {};
  Future<WebSocket> ws;

  EquoComm({String? host, int? port})
      : ws = WebSocket.connect("ws://$host:$port") {
    ws.then((websocket) {
      websocket.listen((event) {
        //print("WS onData $event");
        _receiveMessage(event);
      }, onDone: () {
        print("WS onDone");
      }, onError: (err, st) {
        print("WS onError $err $st");
      });
    });
  }

  void _receiveMessage(dynamic event) {
    final message = _processMessage(event);
    if (message != null) {
      final actionId = message.actionId;
      if (userEventCallbacks.containsKey(actionId)) {
        final callback = userEventCallbacks[actionId];
        if (callback!.args?.once ?? false) {
          userEventCallbacks.remove(actionId);
        }
        if (message.error == null) {
          if (message.callbackId != null) {
            Future(() async {
              callback.onSuccess(message.payload!);
              sendToJava(
                  UserEvent(actionId: message.callbackId!, payload: null));
            }).catchError((error) {
              var userError = SDKCommError(code: -1, message: '');
              if (error is String) {
                userError.message = error;
                sendToJava(UserEvent(
                    actionId: message.callbackId!,
                    payload: userError,
                    error: SDKCommError(code: 1, message: '')));
              } else if (error != null) {
                if (error is SDKCommError && error.code != null) {
                  userError.code = error.code;
                }
                userError.message = jsonEncode(error);
                sendToJava(UserEvent(
                    actionId: message.callbackId!,
                    payload: userError,
                    error: SDKCommError(code: 1, message: '')));
              }
            });
          } else {
            Future(() async {
              callback.onSuccess(message.payload);
            }).catchError((error) {
              throw error; // Log it
            });
          }
        } else if (message.error != null && callback.onError != null) {
          Future(() async {
            callback.onError!(message.error!);
          }).catchError((error) {
            throw error; // Log it
          });
        }
      } else {
        if (message.callbackId != null) {
          final ERROR_CALLBACK_DOES_NOT_EXIST =
              'An event handler does not exist for the user event \'${message.actionId}\'';
          final error =
              SDKCommError(code: 255, message: ERROR_CALLBACK_DOES_NOT_EXIST);
          /*sendToJava(UserEvent(
              actionId: message.callbackId!,
              payload: error,
              error: SDKCommError(code: 1, message: '')));*/
        }
      }
    }
  }

  SDKMessage? _processMessage(dynamic event) {
    if (event == null) {
      return null;
    }
    try {
      final json = jsonDecode(event);
      if (json['error'] != null) {
        return null;
      }
      return SDKMessage(
        actionId: json['actionId'],
        payload: json['payload'],
        error: json['error'] != null
            ? SDKCommError(
                code: json['error']['code'], message: json['error']['message'])
            : null,
        callbackId: json['callbackId'],
      );
    } on FormatException {
      // Handle non-JSON messages (e.g., WebSocket close messages)
      print('Received non-JSON message, ignoring: $event');
      return null;
    }
  }

  Future sendToJava(UserEvent userEvent,
      [UserEventCallback? callback, SendArgs? args]) {
    final event = jsonEncode({
      'actionId': userEvent.actionId,
      'payload': userEvent.payload,
      'error': userEvent.error,
      'callbackId': callback?.id,
    });
    print("ws about to send: $event");
    return ws.then((ws) {
      // print("ws sent");
      ws.add(event);
    });
  }

  Future send<T>(String actionId, [Payload? payload, SendArgs? args]) {
    final userEvent = UserEvent(actionId: actionId, payload: payload);
    return sendToJava(userEvent, null, args);
  }

  void on(
      String userEventActionId, OnSuccessCallback<dynamic> onSuccessCallback,
      [OnErrorCallback? onErrorCallback, CallbackArgs? args]) {
    final callback = UserEventCallback(
        onSuccess: onSuccessCallback, onError: onErrorCallback, args: args);
    userEventCallbacks[userEventActionId] = callback;
  }

  void remove(String userEventActionId) {
    userEventCallbacks.remove(userEventActionId);
  }
}
