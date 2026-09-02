import 'dart:async';

import 'package:flutter/foundation.dart';

/// Undoes a StyledText's local caret navigation when Java's `ST.VerifyKey` listeners rejected the
/// key that caused it.
///
/// SWT runs a `VerifyKeyListener` before the widget acts on the key, so a vetoed key must leave the
/// editor untouched. JFace's `ContentAssistant` is built on that: while its proposal popup is up it
/// vetoes the arrow keys, drives the popup with them, and expects the editor's caret to stay where
/// it was. The client applies navigation optimistically, so a vetoed arrow moved the caret anyway
/// and the next typed character was inserted at that wrong offset.
///
/// **The caret still moves immediately.** Withholding it until the verdict was the obvious design
/// and the wrong one: a JDT editor keeps an `ST.VerifyKey` listener attached for its whole life, not
/// just while a popup is up, so every arrow key would have paid a round trip. Navigation is applied
/// as before and *undone* on a rejection, which costs nothing on the path that is not vetoed.
///
/// Text-changing keys need no entry of their own — Java drops the Modify a vetoed key produced and
/// re-pushes, and the client re-bases on that snapshot because the text differs. A caret-only key
/// produces no Modify and no differing text, which is why it needs this.
///
/// **Correlation is by order, checked by keyCode.** Java answers only the keys this gate can undo,
/// in the same dispatch hop, so the control's channel is ordered. It cannot answer *every* key:
/// Eclipse consumes plenty of keystrokes before they reach the widget (a command binding on the
/// Display filter chain), and a queue holding those drifts out of step with the answers -- measured
/// on a real Eclipse IDE at 482 keys forwarded against 423 answered. Each verdict therefore names
/// its keyCode, and a mismatch drops the queue rather than undoing a move on a guess.
class VerifyKeyGate {
  /// How long an unanswered key stays correlatable. Past this the queue is dropped rather than
  /// grown: a verdict that never arrived must not make a later key undo the wrong move.
  static const Duration _timeout = Duration(milliseconds: 400);

  /// Whether Java reports an `ST.VerifyKey` listener on this editor, so a key of ours may still be
  /// rejected. Java publishes it when the listener set becomes non-empty and again when it empties.
  bool get armed => _armed;
  bool _armed = false;

  set armed(bool value) {
    if (_armed == value) return;
    _armed = value;
    if (!value) _drop();
  }

  final List<_OutstandingKey> _outstanding = [];
  Timer? _expiry;

  /// Records a caret-only key forwarded to Java. [undo] restores what it changed locally.
  void record(int keyCode, VoidCallback? undo) {
    if (!_armed) return;
    _outstanding.add(_OutstandingKey(keyCode, undo));
    _expiry ??= Timer(_timeout, _drop);
  }

  /// Java's answer for the oldest outstanding key. A rejection runs that key's undo; an answer
  /// naming a different key means the two streams are out of step, so nothing is undone.
  void verdict(bool doit, int? keyCode) {
    if (_outstanding.isEmpty) return;
    if (keyCode != null && _outstanding.first.keyCode != keyCode) {
      _drop();
      return;
    }
    final entry = _outstanding.removeAt(0);
    if (_outstanding.isEmpty) {
      _expiry?.cancel();
      _expiry = null;
    }
    if (!doit) entry.undo?.call();
  }

  void dispose() => _drop();

  void _drop() {
    _expiry?.cancel();
    _expiry = null;
    _outstanding.clear();
  }
}

class _OutstandingKey {
  _OutstandingKey(this.keyCode, this.undo);

  final int keyCode;
  final VoidCallback? undo;
}
