import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import 'menu_evolve.dart';

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
  } else if (_modifierBit(key) != 0) {
    return (keyCode: _modifierBit(key), character: 0);
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
  // A keypad key types its character, which every native's KeyDown carries.
  } else if (key == LogicalKeyboardKey.numpad0) {
    return (keyCode: SWT.KEYPAD_0, character: 0x30);
  } else if (key == LogicalKeyboardKey.numpad1) {
    return (keyCode: SWT.KEYPAD_1, character: 0x31);
  } else if (key == LogicalKeyboardKey.numpad2) {
    return (keyCode: SWT.KEYPAD_2, character: 0x32);
  } else if (key == LogicalKeyboardKey.numpad3) {
    return (keyCode: SWT.KEYPAD_3, character: 0x33);
  } else if (key == LogicalKeyboardKey.numpad4) {
    return (keyCode: SWT.KEYPAD_4, character: 0x34);
  } else if (key == LogicalKeyboardKey.numpad5) {
    return (keyCode: SWT.KEYPAD_5, character: 0x35);
  } else if (key == LogicalKeyboardKey.numpad6) {
    return (keyCode: SWT.KEYPAD_6, character: 0x36);
  } else if (key == LogicalKeyboardKey.numpad7) {
    return (keyCode: SWT.KEYPAD_7, character: 0x37);
  } else if (key == LogicalKeyboardKey.numpad8) {
    return (keyCode: SWT.KEYPAD_8, character: 0x38);
  } else if (key == LogicalKeyboardKey.numpad9) {
    return (keyCode: SWT.KEYPAD_9, character: 0x39);
  } else if (key == LogicalKeyboardKey.numpadMultiply) {
    return (keyCode: SWT.KEYPAD_MULTIPLY, character: 0x2a); // '*'
  } else if (key == LogicalKeyboardKey.numpadAdd) {
    return (keyCode: SWT.KEYPAD_ADD, character: 0x2b); // '+'
  } else if (key == LogicalKeyboardKey.numpadSubtract) {
    return (keyCode: SWT.KEYPAD_SUBTRACT, character: 0x2d); // '-'
  } else if (key == LogicalKeyboardKey.numpadDecimal) {
    return (keyCode: SWT.KEYPAD_DECIMAL, character: 0x2e); // '.'
  } else if (key == LogicalKeyboardKey.numpadDivide) {
    return (keyCode: SWT.KEYPAD_DIVIDE, character: 0x2f); // '/'
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

/// The SWT modifier a modifier key stands for, or 0 for any other key.
int _modifierBit(LogicalKeyboardKey key) {
  if (key == LogicalKeyboardKey.shiftLeft ||
      key == LogicalKeyboardKey.shiftRight ||
      key == LogicalKeyboardKey.shift) {
    return SWT.SHIFT;
  }
  if (key == LogicalKeyboardKey.controlLeft ||
      key == LogicalKeyboardKey.controlRight ||
      key == LogicalKeyboardKey.control) {
    return SWT.CTRL;
  }
  if (key == LogicalKeyboardKey.altLeft ||
      key == LogicalKeyboardKey.altRight ||
      key == LogicalKeyboardKey.alt) {
    return SWT.ALT;
  }
  if (key == LogicalKeyboardKey.metaLeft ||
      key == LogicalKeyboardKey.metaRight ||
      key == LogicalKeyboardKey.meta) {
    return SWT.COMMAND;
  }
  return 0;
}

/// Maps a Flutter [KeyEvent] to a [VEvent] with
/// SWT-compatible keyCode, character and stateMask values.
///
/// A modifier key's own event carries SWT's state before the key changed it, which
/// [HardwareKeyboard] has already applied, so that bit is flipped back. A key a pop-up held carries
/// the modifiers of when it was typed.
VEvent mapNewKeyEventToSwt(KeyEvent event) {
  final mapped = _mapLogicalKey(event.logicalKey, _characterOf(event));

  final mods = OpenMenus.modifiersWhenTyped ?? KeyModifiers.now();
  int stateMask = 0;
  if (mods.control) stateMask |= SWT.CTRL;
  if (mods.shift) stateMask |= SWT.SHIFT;
  if (mods.alt) stateMask |= SWT.ALT;
  if (mods.meta) stateMask |= SWT.COMMAND;
  final own = _modifierBit(event.logicalKey);
  if (own != 0) {
    stateMask = event is KeyUpEvent ? stateMask | own : stateMask & ~own;
  }

  return VEvent()
    ..keyCode = mapped.keyCode
    ..character = mapped.character
    ..stateMask = stateMask;
}

/// The character each key's last press typed. A [KeyUpEvent] carries none, and a release reports
/// what its press typed: Shift+Z releases 'Z'. Kept past the release, since one event is mapped
/// more than once.
final Map<PhysicalKeyboardKey, String> _pressedCharacters = {};

String? _characterOf(KeyEvent event) {
  if (event is KeyUpEvent) return _pressedCharacters[event.physicalKey];
  final character = event.character;
  if (character == null) {
    _pressedCharacters.remove(event.physicalKey);
  } else {
    _pressedCharacters[event.physicalKey] = character;
  }
  return character;
}

/// DOM `KeyboardEvent.key` names for the non-printable keys, so they share the SWT table above.
/// Printable keys need no entry: their `key` *is* the character.
const Map<String, LogicalKeyboardKey> _domNamedKeys = {
  'Enter': LogicalKeyboardKey.enter,
  'Escape': LogicalKeyboardKey.escape,
  'Backspace': LogicalKeyboardKey.backspace,
  'Delete': LogicalKeyboardKey.delete,
  'Tab': LogicalKeyboardKey.tab,
  'ArrowUp': LogicalKeyboardKey.arrowUp,
  'ArrowDown': LogicalKeyboardKey.arrowDown,
  'ArrowLeft': LogicalKeyboardKey.arrowLeft,
  'ArrowRight': LogicalKeyboardKey.arrowRight,
  'Home': LogicalKeyboardKey.home,
  'End': LogicalKeyboardKey.end,
  'PageUp': LogicalKeyboardKey.pageUp,
  'PageDown': LogicalKeyboardKey.pageDown,
  'Insert': LogicalKeyboardKey.insert,
  'F1': LogicalKeyboardKey.f1,
  'F2': LogicalKeyboardKey.f2,
  'F3': LogicalKeyboardKey.f3,
  'F4': LogicalKeyboardKey.f4,
  'F5': LogicalKeyboardKey.f5,
  'F6': LogicalKeyboardKey.f6,
  'F7': LogicalKeyboardKey.f7,
  'F8': LogicalKeyboardKey.f8,
  'F9': LogicalKeyboardKey.f9,
  'F10': LogicalKeyboardKey.f10,
  'F11': LogicalKeyboardKey.f11,
  'F12': LogicalKeyboardKey.f12,
};

/// Maps a DOM `KeyboardEvent.key` to a [VEvent], for keys that arrive as DOM events instead of
/// through Flutter's keyboard — those typed inside a Browser's iframe. A modifier pressed on its
/// own has no SWT keyCode and yields an empty event, which the caller drops.
VEvent mapDomKeyToSwt(String key,
    {required bool ctrl,
    required bool shift,
    required bool alt,
    required bool meta}) {
  final named = _domNamedKeys[key];
  final ({int keyCode, int character}) mapped;
  if (named != null) {
    mapped = _mapLogicalKey(named, null);
  } else if (key.length == 1) {
    mapped = (
      keyCode: key.toLowerCase().codeUnitAt(0),
      character: key.codeUnitAt(0)
    );
  } else {
    mapped = (keyCode: 0, character: 0);
  }

  int stateMask = 0;
  if (ctrl) stateMask |= SWT.CTRL;
  if (shift) stateMask |= SWT.SHIFT;
  if (alt) stateMask |= SWT.ALT;
  if (meta) stateMask |= SWT.COMMAND;

  return VEvent()
    ..keyCode = mapped.keyCode
    ..character = mapped.character
    ..stateMask = stateMask;
}
