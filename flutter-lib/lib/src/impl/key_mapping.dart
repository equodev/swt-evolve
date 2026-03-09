import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/swt.dart';

/// Shared key+character mapping from a [LogicalKeyboardKey] and the optional
/// character string produced by the event.  Returns SWT-compatible
/// [keyCode] and [character] values.
({int keyCode, int character}) _mapLogicalKey(
    LogicalKeyboardKey key, String? eventCharacter) {
  if (key == LogicalKeyboardKey.enter) {
    return (keyCode: SWT.CR, character: SWT.CR);
  } else if (key == LogicalKeyboardKey.numpadEnter) {
    return (keyCode: SWT.KEYPAD_CR, character: SWT.CR);
  } else if (key == LogicalKeyboardKey.escape) {
    return (keyCode: SWT.ESC, character: SWT.ESC);
  } else if (key == LogicalKeyboardKey.backspace) {
    return (keyCode: SWT.BS, character: SWT.BS);
  } else if (key == LogicalKeyboardKey.delete) {
    return (keyCode: SWT.DEL, character: SWT.DEL);
  } else if (key == LogicalKeyboardKey.tab) {
    return (keyCode: SWT.TAB, character: SWT.TAB);
  } else if (key == LogicalKeyboardKey.arrowUp) {
    return (keyCode: SWT.ARROW_UP, character: 0);
  } else if (key == LogicalKeyboardKey.arrowDown) {
    return (keyCode: SWT.ARROW_DOWN, character: 0);
  } else if (key == LogicalKeyboardKey.arrowLeft) {
    return (keyCode: SWT.ARROW_LEFT, character: 0);
  } else if (key == LogicalKeyboardKey.arrowRight) {
    return (keyCode: SWT.ARROW_RIGHT, character: 0);
  } else if (key == LogicalKeyboardKey.home) {
    return (keyCode: SWT.HOME, character: 0);
  } else if (key == LogicalKeyboardKey.end) {
    return (keyCode: SWT.END, character: 0);
  } else if (key == LogicalKeyboardKey.pageUp) {
    return (keyCode: SWT.PAGE_UP, character: 0);
  } else if (key == LogicalKeyboardKey.pageDown) {
    return (keyCode: SWT.PAGE_DOWN, character: 0);
  } else if (key == LogicalKeyboardKey.insert) {
    return (keyCode: SWT.INSERT, character: 0);
  } else if (key == LogicalKeyboardKey.f1) {
    return (keyCode: SWT.F1, character: 0);
  } else if (key == LogicalKeyboardKey.f2) {
    return (keyCode: SWT.F2, character: 0);
  } else if (key == LogicalKeyboardKey.f3) {
    return (keyCode: SWT.F3, character: 0);
  } else if (key == LogicalKeyboardKey.f4) {
    return (keyCode: SWT.F4, character: 0);
  } else if (key == LogicalKeyboardKey.f5) {
    return (keyCode: SWT.F5, character: 0);
  } else if (key == LogicalKeyboardKey.f6) {
    return (keyCode: SWT.F6, character: 0);
  } else if (key == LogicalKeyboardKey.f7) {
    return (keyCode: SWT.F7, character: 0);
  } else if (key == LogicalKeyboardKey.f8) {
    return (keyCode: SWT.F8, character: 0);
  } else if (key == LogicalKeyboardKey.f9) {
    return (keyCode: SWT.F9, character: 0);
  } else if (key == LogicalKeyboardKey.f10) {
    return (keyCode: SWT.F10, character: 0);
  } else if (key == LogicalKeyboardKey.f11) {
    return (keyCode: SWT.F11, character: 0);
  } else if (key == LogicalKeyboardKey.f12) {
    return (keyCode: SWT.F12, character: 0);
  } else if (key == LogicalKeyboardKey.numpad0) {
    return (keyCode: SWT.KEYPAD_0, character: 0);
  } else if (key == LogicalKeyboardKey.numpad1) {
    return (keyCode: SWT.KEYPAD_1, character: 0);
  } else if (key == LogicalKeyboardKey.numpad2) {
    return (keyCode: SWT.KEYPAD_2, character: 0);
  } else if (key == LogicalKeyboardKey.numpad3) {
    return (keyCode: SWT.KEYPAD_3, character: 0);
  } else if (key == LogicalKeyboardKey.numpad4) {
    return (keyCode: SWT.KEYPAD_4, character: 0);
  } else if (key == LogicalKeyboardKey.numpad5) {
    return (keyCode: SWT.KEYPAD_5, character: 0);
  } else if (key == LogicalKeyboardKey.numpad6) {
    return (keyCode: SWT.KEYPAD_6, character: 0);
  } else if (key == LogicalKeyboardKey.numpad7) {
    return (keyCode: SWT.KEYPAD_7, character: 0);
  } else if (key == LogicalKeyboardKey.numpad8) {
    return (keyCode: SWT.KEYPAD_8, character: 0);
  } else if (key == LogicalKeyboardKey.numpad9) {
    return (keyCode: SWT.KEYPAD_9, character: 0);
  } else if (key == LogicalKeyboardKey.numpadMultiply) {
    return (keyCode: SWT.KEYPAD_MULTIPLY, character: 0);
  } else if (key == LogicalKeyboardKey.numpadAdd) {
    return (keyCode: SWT.KEYPAD_ADD, character: 0);
  } else if (key == LogicalKeyboardKey.numpadSubtract) {
    return (keyCode: SWT.KEYPAD_SUBTRACT, character: 0);
  } else if (key == LogicalKeyboardKey.numpadDecimal) {
    return (keyCode: SWT.KEYPAD_DECIMAL, character: 0);
  } else if (key == LogicalKeyboardKey.numpadDivide) {
    return (keyCode: SWT.KEYPAD_DIVIDE, character: 0);
  } else {
    final label = key.keyLabel;
    if (label.length == 1) {
      final kc = label.toLowerCase().codeUnitAt(0);
      final ch = (eventCharacter != null && eventCharacter.isNotEmpty)
          ? eventCharacter.codeUnitAt(0)
          : kc;
      return (keyCode: kc, character: ch);
    }
    return (keyCode: 0, character: 0);
  }
}

/// Maps a Flutter [RawKeyEvent] (legacy API) to a [VEvent] with
/// SWT-compatible keyCode, character and stateMask values.
VEvent mapKeyEventToSwt(RawKeyEvent event) {
  final mapped = _mapLogicalKey(event.logicalKey, event.character);

  int stateMask = 0;
  if (event.data.isControlPressed) stateMask |= SWT.CTRL;
  if (event.data.isShiftPressed) stateMask |= SWT.SHIFT;
  if (event.data.isAltPressed) stateMask |= SWT.ALT;
  if (event.data.isMetaPressed) stateMask |= SWT.COMMAND;

  return VEvent()
    ..keyCode = mapped.keyCode
    ..character = mapped.character
    ..stateMask = stateMask;
}

/// Maps a Flutter [KeyEvent] to a [VEvent] with
/// SWT-compatible keyCode, character and stateMask values.
VEvent mapNewKeyEventToSwt(KeyEvent event) {
  final mapped = _mapLogicalKey(event.logicalKey, event.character);

  final kb = HardwareKeyboard.instance;
  int stateMask = 0;
  if (kb.isControlPressed) stateMask |= SWT.CTRL;
  if (kb.isShiftPressed) stateMask |= SWT.SHIFT;
  if (kb.isAltPressed) stateMask |= SWT.ALT;
  if (kb.isMetaPressed) stateMask |= SWT.COMMAND;

  return VEvent()
    ..keyCode = mapped.keyCode
    ..character = mapped.character
    ..stateMask = stateMask;
}
