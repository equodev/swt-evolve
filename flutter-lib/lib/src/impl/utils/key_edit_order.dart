import 'dart:async';

import 'package:flutter/widgets.dart';

/// Keeps the key events Java sees behind the edit a typed character makes.
///
/// A native text field applies a typed character while it handles the key press, so SWT reports
/// KeyDown, Verify, Modify and only then KeyUp. Flutter edits a field after the framework has
/// answered the KeyDown, while the KeyUp is delivered as soon as it happens: on a busy machine the
/// KeyUp overtakes the edit. So once a character is typed into a field that reports its edits, the
/// key events after it are held until that edit lands.
class KeyEditOrder {
  KeyEditOrder({this.timeout = const Duration(milliseconds: 250)});

  static final KeyEditOrder instance = KeyEditOrder();

  /// How long a held event waits for an edit that may never come: a key the field ignores, a
  /// text limit already reached.
  final Duration timeout;

  final List<_Held> _held = [];

  /// The fields that report their edits, each with whether it takes typing right now.
  final Map<FocusNode, bool Function()> _editors = {};

  void registerEditor(FocusNode node, bool Function() takesTyping) => _editors[node] = takesTyping;

  void unregisterEditor(FocusNode node) => _editors.remove(node);

  /// Whether the focused widget is a field that reports its edits and takes typing.
  bool get editorFocused => _editors[FocusManager.instance.primaryFocus]?.call() ?? false;

  /// Characters typed into a field that reports its edits, and the edits it reported.
  int _typed = 0;
  int _edited = 0;
  Timer? _timer;

  /// Sends [send] now, or once the edits of every character typed before it have landed. [typed]:
  /// this is a KeyDown that types a character into a field that reports its edits.
  void forward({required bool typed, required void Function() send}) {
    final need = _typed;
    if (typed) _typed++;
    if (_held.isEmpty && _edited >= need) {
      send();
      return;
    }
    _held.add(_Held(need, send));
    _timer ??= Timer(timeout, _giveUp);
  }

  /// A field applied a typed character.
  void editApplied() {
    if (_edited < _typed) _edited++;
    _flush();
  }

  void _giveUp() {
    _timer = null;
    if (_held.isNotEmpty && _edited < _held.first.need) _edited = _held.first.need;
    _flush();
  }

  void _flush() {
    while (_held.isNotEmpty && _held.first.need <= _edited) {
      _held.removeAt(0).send();
    }
    _timer?.cancel();
    _timer = _held.isEmpty ? null : Timer(timeout, _giveUp);
  }
}

class _Held {
  _Held(this.need, this.send);

  final int need;
  final void Function() send;
}
