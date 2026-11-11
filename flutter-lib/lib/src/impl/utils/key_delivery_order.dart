import 'dart:async';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// Hands each key to the framework in the order the platform delivered it among pointer events.
///
/// A desktop engine runs a key's platform messages as soon as they arrive, while it posts a pointer
/// packet to its task queue. A key that follows a click in the platform's queue then reaches the
/// framework first: a Shift released right after a shift-click clears the click's Shift and
/// reaches Java ahead of it. Each key message is run one event-loop turn later instead, which puts
/// it behind the pointer packets the engine had already posted.
///
/// The web needs none of it: the browser dispatches keys and pointers in order, and a key's answer
/// must be given while its DOM event is being dispatched. Nor does a UI isolate on a thread of its
/// own: the engine posts its keys to the same queue as its pointer packets, in the platform's order,
/// and a turn's delay would put a key behind the pointer events that followed it.
class KeyDeliveryOrder {
  KeyDeliveryOrder._();

  /// Installs the ordering on a desktop client whose UI isolate shares the platform thread
  /// ([uiThreadOwn] false), once the framework has installed its own key handlers (after its first
  /// keyboard-state sync).
  static Future<void> install({required bool uiThreadOwn}) async {
    if (kIsWeb || uiThreadOwn) return;
    await HardwareKeyboard.instance.syncKeyboardState();
    _replaceHandlers();
  }

  /// Replaces the framework's key handlers with ones that delegate to them a turn later.
  static void _replaceHandlers() {
    final manager = ServicesBinding.instance.keyEventManager;
    ServicesBinding.instance.platformDispatcher.onKeyData = (ui.KeyData data) {
      _later(() => manager.handleKeyData(data));
      // The framework answers key data only through the raw message that follows it.
      return false;
    };
    SystemChannels.keyEvent.setMessageHandler((dynamic message) {
      final answer = Completer<Map<String, dynamic>>();
      _later(() => answer.complete(manager.handleRawKeyMessage(message)));
      return answer.future;
    });
  }

  /// Puts the framework's own key handlers back.
  @visibleForTesting
  static void uninstall() {
    final manager = ServicesBinding.instance.keyEventManager;
    ServicesBinding.instance.platformDispatcher.onKeyData = manager.handleKeyData;
    SystemChannels.keyEvent.setMessageHandler(manager.handleRawKeyMessage);
  }

  /// One timer per message, so each lands at its own place among the pointer packets.
  static void _later(void Function() run) => Timer.run(run);
}
