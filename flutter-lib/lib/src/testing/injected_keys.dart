import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

/// A test driver's keyboard, injected at the framework: each key goes to [HardwareKeyboard] and to
/// the focus system's key message handler, the two places the engine's key path ends in, and a key
/// no handler takes is turned into the edit or action a desktop text input plugin would make of it.
///
/// Keys are named by their AWT virtual key code, the vocabulary of the Java driver that sends them.
/// Physical, logical and character follow the Linux embedder: the logical key of a letter is its
/// lower case, and a key whose keysym has a code point carries it as its character on the press.
class InjectedKeys {
  InjectedKeys._();

  static const int _vkEnter = 10;

  /// AWT virtual key code to the physical and logical key, and the character pressed without Shift.
  static final Map<int, (PhysicalKeyboardKey, LogicalKeyboardKey, String?)> _keys = {
    8: (PhysicalKeyboardKey.backspace, LogicalKeyboardKey.backspace, '\b'),
    9: (PhysicalKeyboardKey.tab, LogicalKeyboardKey.tab, '\t'),
    _vkEnter: (PhysicalKeyboardKey.enter, LogicalKeyboardKey.enter, '\r'),
    16: (PhysicalKeyboardKey.shiftLeft, LogicalKeyboardKey.shiftLeft, null),
    17: (PhysicalKeyboardKey.controlLeft, LogicalKeyboardKey.controlLeft, null),
    18: (PhysicalKeyboardKey.altLeft, LogicalKeyboardKey.altLeft, null),
    27: (PhysicalKeyboardKey.escape, LogicalKeyboardKey.escape, '\x1b'),
    32: (PhysicalKeyboardKey.space, LogicalKeyboardKey.space, ' '),
    33: (PhysicalKeyboardKey.pageUp, LogicalKeyboardKey.pageUp, null),
    34: (PhysicalKeyboardKey.pageDown, LogicalKeyboardKey.pageDown, null),
    35: (PhysicalKeyboardKey.end, LogicalKeyboardKey.end, null),
    36: (PhysicalKeyboardKey.home, LogicalKeyboardKey.home, null),
    37: (PhysicalKeyboardKey.arrowLeft, LogicalKeyboardKey.arrowLeft, null),
    38: (PhysicalKeyboardKey.arrowUp, LogicalKeyboardKey.arrowUp, null),
    39: (PhysicalKeyboardKey.arrowRight, LogicalKeyboardKey.arrowRight, null),
    40: (PhysicalKeyboardKey.arrowDown, LogicalKeyboardKey.arrowDown, null),
    44: (PhysicalKeyboardKey.comma, LogicalKeyboardKey.comma, ','),
    45: (PhysicalKeyboardKey.minus, LogicalKeyboardKey.minus, '-'),
    46: (PhysicalKeyboardKey.period, LogicalKeyboardKey.period, '.'),
    47: (PhysicalKeyboardKey.slash, LogicalKeyboardKey.slash, '/'),
    59: (PhysicalKeyboardKey.semicolon, LogicalKeyboardKey.semicolon, ';'),
    61: (PhysicalKeyboardKey.equal, LogicalKeyboardKey.equal, '='),
    106: (PhysicalKeyboardKey.numpadMultiply, LogicalKeyboardKey.numpadMultiply, '*'),
    107: (PhysicalKeyboardKey.numpadAdd, LogicalKeyboardKey.numpadAdd, '+'),
    109: (PhysicalKeyboardKey.numpadSubtract, LogicalKeyboardKey.numpadSubtract, '-'),
    127: (PhysicalKeyboardKey.delete, LogicalKeyboardKey.delete, '\x7f'),
    155: (PhysicalKeyboardKey.insert, LogicalKeyboardKey.insert, null),
    157: (PhysicalKeyboardKey.metaLeft, LogicalKeyboardKey.metaLeft, null),
    for (var i = 0; i < 10; i++) 48 + i: (_digits[i], LogicalKeyboardKey(0x30 + i), String.fromCharCode(0x30 + i)),
    for (var i = 0; i < 26; i++) 65 + i: (_letters[i], LogicalKeyboardKey(0x61 + i), String.fromCharCode(0x61 + i)),
    for (var i = 0; i < 12; i++) 112 + i: (_functionKeys[i].$1, _functionKeys[i].$2, null),
  };

  static const List<PhysicalKeyboardKey> _digits = [
    PhysicalKeyboardKey.digit0, PhysicalKeyboardKey.digit1, PhysicalKeyboardKey.digit2, PhysicalKeyboardKey.digit3,
    PhysicalKeyboardKey.digit4, PhysicalKeyboardKey.digit5, PhysicalKeyboardKey.digit6, PhysicalKeyboardKey.digit7,
    PhysicalKeyboardKey.digit8, PhysicalKeyboardKey.digit9,
  ];

  static const List<PhysicalKeyboardKey> _letters = [
    PhysicalKeyboardKey.keyA, PhysicalKeyboardKey.keyB, PhysicalKeyboardKey.keyC, PhysicalKeyboardKey.keyD,
    PhysicalKeyboardKey.keyE, PhysicalKeyboardKey.keyF, PhysicalKeyboardKey.keyG, PhysicalKeyboardKey.keyH,
    PhysicalKeyboardKey.keyI, PhysicalKeyboardKey.keyJ, PhysicalKeyboardKey.keyK, PhysicalKeyboardKey.keyL,
    PhysicalKeyboardKey.keyM, PhysicalKeyboardKey.keyN, PhysicalKeyboardKey.keyO, PhysicalKeyboardKey.keyP,
    PhysicalKeyboardKey.keyQ, PhysicalKeyboardKey.keyR, PhysicalKeyboardKey.keyS, PhysicalKeyboardKey.keyT,
    PhysicalKeyboardKey.keyU, PhysicalKeyboardKey.keyV, PhysicalKeyboardKey.keyW, PhysicalKeyboardKey.keyX,
    PhysicalKeyboardKey.keyY, PhysicalKeyboardKey.keyZ,
  ];

  static const List<(PhysicalKeyboardKey, LogicalKeyboardKey)> _functionKeys = [
    (PhysicalKeyboardKey.f1, LogicalKeyboardKey.f1), (PhysicalKeyboardKey.f2, LogicalKeyboardKey.f2),
    (PhysicalKeyboardKey.f3, LogicalKeyboardKey.f3), (PhysicalKeyboardKey.f4, LogicalKeyboardKey.f4),
    (PhysicalKeyboardKey.f5, LogicalKeyboardKey.f5), (PhysicalKeyboardKey.f6, LogicalKeyboardKey.f6),
    (PhysicalKeyboardKey.f7, LogicalKeyboardKey.f7), (PhysicalKeyboardKey.f8, LogicalKeyboardKey.f8),
    (PhysicalKeyboardKey.f9, LogicalKeyboardKey.f9), (PhysicalKeyboardKey.f10, LogicalKeyboardKey.f10),
    (PhysicalKeyboardKey.f11, LogicalKeyboardKey.f11), (PhysicalKeyboardKey.f12, LogicalKeyboardKey.f12),
  ];

  /// Shifted characters of the unshifted ones the fixtures type, on a US layout.
  static const Map<String, String> _shifted = {
    '1': '!', '2': '@', '3': '#', '4': r'$', '5': '%', '6': '^', '7': '&', '8': '*', '9': '(', '0': ')',
    ',': '<', '.': '>', '/': '?', ';': ':', '=': '+', '-': '_',
  };

  /// Presses ([down]) or releases the key [vk] at [timeStamp]. Answers whether a handler took it,
  /// or an error for a key it has no mapping for.
  static Map<String, Object?> key(int vk, bool down, Duration timeStamp) {
    final mapped = _keys[vk];
    if (mapped == null) return {'error': 'no mapping for virtual key $vk'};
    final (physical, logical, plain) = mapped;
    final keyboard = HardwareKeyboard.instance;
    if (down && keyboard.physicalKeysPressed.contains(physical)) return {'error': 'virtual key $vk is already down'};
    if (!down && !keyboard.physicalKeysPressed.contains(physical)) return {'handled': false};
    final String? typed = down ? _platformCharacter(vk, character(plain, keyboard.isShiftPressed), keyboard) : null;
    final KeyEvent event = down
        ? KeyDownEvent(physicalKey: physical, logicalKey: logical, character: typed, timeStamp: timeStamp)
        : KeyUpEvent(physicalKey: physical, logicalKey: logical, timeStamp: timeStamp);
    var handled = keyboard.handleKeyEvent(event);
    final messageHandler = ServicesBinding.instance.keyEventManager.keyMessageHandler;
    if (messageHandler != null) handled = messageHandler(KeyMessage([event], null)) || handled;
    if (down && !handled) textInput(vk, typed, keyboard);
    return {'handled': handled};
  }

  /// The physical and logical key of the AWT virtual key [vk], and the character it types without
  /// Shift; null for a key with no mapping.
  static (PhysicalKeyboardKey, LogicalKeyboardKey, String?)? mapping(int vk) => _keys[vk];

  /// The character [plain] makes with Shift held when [shift], on a US layout.
  /// The character the macOS embedder gives a key where it differs from Linux's: Backspace is DEL, a
  /// Shift+Tab is the backtab control character, and forward Delete has none (its function-key code
  /// point is left out).
  static String? _platformCharacter(int vk, String? linux, HardwareKeyboard keyboard) {
    if (defaultTargetPlatform != TargetPlatform.macOS) return linux;
    return switch (vk) {
      8 => '\x7f',
      9 when keyboard.isShiftPressed => '\x19',
      127 => null,
      _ => linux,
    };
  }

  static String? character(String? plain, bool shift) {
    if (plain == null || !shift) return plain;
    final code = plain.codeUnitAt(0);
    if (code >= 0x61 && code <= 0x7a) return plain.toUpperCase();
    return _shifted[plain] ?? plain;
  }

  /// What a desktop text input plugin does with a key the framework left unhandled, for the client
  /// that holds the text input connection: Enter performs the field's action, a multi-line field
  /// taking a new line as well. A printable character without Control, Alt or Meta is committed as
  /// text. On macOS a key with no character becomes the editing commands Cocoa's text system binds it
  /// to, which a Flutter client takes as selectors, and Command keeps the key from the text system.
  static void textInput(int vk, String? character, HardwareKeyboard keyboard) {
    final client = _control.client;
    final config = _control.configuration;
    if (client == null || config == null) return;
    final cocoa = defaultTargetPlatform == TargetPlatform.macOS;
    if (vk == _vkEnter && !(cocoa && keyboard.isMetaPressed)) {
      if (config.inputType == TextInputType.multiline && config.inputAction == TextInputAction.newline) {
        _insert('\n');
      }
      client.performAction(config.inputAction);
      return;
    }
    if (cocoa && !keyboard.isControlPressed) {
      final selectors = _cocoaSelectors(vk, keyboard);
      if (selectors != null) {
        for (final selector in selectors) {
          client.performSelector(selector);
        }
        return;
      }
      // Any other Command key is a key equivalent, which the text system does not type.
      if (keyboard.isMetaPressed) return;
    }
    if (character == null || character.codeUnitAt(0) < 0x20 || character.codeUnitAt(0) == 0x7f) return;
    if (keyboard.isControlPressed || keyboard.isMetaPressed || (!cocoa && keyboard.isAltPressed)) return;
    _insert(character);
  }

  /// The selectors Cocoa's standard key bindings send for [vk] with the modifiers held; null for a key
  /// they bind to nothing.
  static List<String>? _cocoaSelectors(int vk, HardwareKeyboard keyboard) {
    final shift = keyboard.isShiftPressed, option = keyboard.isAltPressed, command = keyboard.isMetaPressed;
    String modified(String base) => shift ? '${base.substring(0, base.length - 1)}AndModifySelection:' : base;
    if (command) {
      return switch (vk) {
        37 => [modified('moveToLeftEndOfLine:')],
        39 => [modified('moveToRightEndOfLine:')],
        38 => [modified('moveToBeginningOfDocument:')],
        40 => [modified('moveToEndOfDocument:')],
        8 => ['deleteToBeginningOfLine:'],
        _ => null,
      };
    }
    return switch (vk) {
      37 => [modified(option ? 'moveWordLeft:' : 'moveLeft:')],
      39 => [modified(option ? 'moveWordRight:' : 'moveRight:')],
      38 => [modified(option ? 'moveParagraphBackward:' : 'moveUp:')],
      40 => [modified(option ? 'moveParagraphForward:' : 'moveDown:')],
      36 => [shift ? 'moveToBeginningOfDocumentAndModifySelection:' : 'scrollToBeginningOfDocument:'],
      35 => [shift ? 'moveToEndOfDocumentAndModifySelection:' : 'scrollToEndOfDocument:'],
      33 => [shift ? 'pageUpAndModifySelection:' : 'scrollPageUp:'],
      34 => [shift ? 'pageDownAndModifySelection:' : 'scrollPageDown:'],
      8 => [option ? 'deleteWordBackward:' : 'deleteBackward:'],
      127 => [option ? 'deleteWordForward:' : 'deleteForward:'],
      9 => [shift ? 'insertBacktab:' : 'insertTab:'],
      27 => ['cancelOperation:'],
      _ => null,
    };
  }

  /// Commits [text] over the selection of the value the client holds now: the state it last sent can
  /// be older than an edit it has already applied.
  /// Commits [text] over the selection of the value the client holds now: the state it last sent can
  /// be older than an edit it has already applied.
  static void _insert(String text) {
    final value = _control.client?.currentTextEditingValue ?? _control.value;
    if (value == null) return;
    final selection = value.selection.isValid ? value.selection : TextSelection.collapsed(offset: value.text.length);
    // The typed text replaces the selection and leaves the caret after it, as a plugin's insertText does.
    final edited = TextEditingValue(
      text: value.text.replaceRange(selection.start, selection.end, text),
      selection: TextSelection.collapsed(offset: selection.start + text.length),
    );
    _control.value = edited;
    TextInput.updateEditingValue(edited);
  }

  static final _InjectedTextControl _control = _InjectedTextControl();

  /// Starts following the text input connection, as a platform plugin does; before the first field
  /// connects, since a control learns of a client only as it attaches.
  static void watch() => TextInput.setInputControl(_control);
}

/// Follows the text input connection the way the platform's plugin does: the client, its
/// configuration and the editing state the framework last sent it.
class _InjectedTextControl with TextInputControl {
  TextInputClient? client;
  TextInputConfiguration? configuration;
  TextEditingValue? value;

  @override
  void attach(TextInputClient client, TextInputConfiguration configuration) {
    this.client = client;
    this.configuration = configuration;
    value = client.currentTextEditingValue;
  }

  @override
  void detach(TextInputClient client) {
    if (!identical(this.client, client)) return;
    this.client = null;
    configuration = null;
    value = null;
  }

  @override
  void updateConfig(TextInputConfiguration configuration) => this.configuration = configuration;

  @override
  void setEditingState(TextEditingValue value) => this.value = value;
}
