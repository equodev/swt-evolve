import 'dart:convert';

import 'package:flutter/foundation.dart';

import '../comm/comm.dart';

/// Keyboard focus asked for by Java -- `Control.setFocus()`, a JFace cell editor activating, a
/// part activation. The render side owns the real focus, so the request has to cross the bridge or
/// the control never receives a keystroke.
///
/// It travels on one Display-wide channel and is held until a control claims it, rather than being
/// delivered on the target's own channel: Java can focus a control it created in the same
/// event-loop pass, and that control only exists client-side one frame after its parent's state
/// push, so a per-widget listener would not be registered yet when the request lands.
class FocusRequests extends ChangeNotifier {
  FocusRequests._();

  static final FocusRequests instance = FocusRequests._();

  static const String channel = 'swt.evolve.focus';

  static bool _listening = false;

  int? _pendingId;

  /// Subscribes the process-wide listener. Idempotent.
  static void listen() {
    if (_listening) return;
    _listening = true;
    EquoCommService.onRaw(channel, (dynamic args) {
      final decoded = args is String ? jsonDecode(args) : args;
      if (decoded is! Map) return;
      final id = (decoded['id'] as num?)?.toInt();
      if (id != null) instance.request(id);
    });
  }

  void request(int id) {
    _pendingId = id;
    notifyListeners();
  }

  /// Whether [id] is the outstanding request, consuming it if so. A request is honoured once, so a
  /// later rebuild cannot pull focus back from wherever the user has moved it since.
  bool claim(int? id) {
    if (id == null || _pendingId != id) return false;
    _pendingId = null;
    return true;
  }

  @visibleForTesting
  void reset() => _pendingId = null;
}
