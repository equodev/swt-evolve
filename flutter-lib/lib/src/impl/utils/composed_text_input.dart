import 'package:flutter/services.dart';

/// The editor currently able to compose, if any. Shared by both key routes to Java: the focused
/// control's handler and the Display's forwarder.
ComposedTextInput? _activeComposition;

/// Answered once per key: asking clears the commit it answers. Safe because exactly one route
/// forwards a given key.
bool compositionOwnsKey(String character) =>
    _activeComposition?.swallowsKey(character) ?? false;

/// Gives a canvas-painted editor somewhere for the platform to compose text.
///
/// An editor that reads only the raw key stream cannot see composed input at all: the OS delivers
/// the composed character through the text-input pipeline, and without a connection open there is
/// no pipeline to deliver it to. On a dead-key layout the raw stream then carries only the
/// committing key — a space on U.S. International — so the quote the user asked for arrives as a
/// space. Accented characters and every CJK IME fail the same way.
///
/// Compositions are reported: [onComposing] with the text being composed and the caret inside it,
/// then [onComposedText] with the text that ends it. So is text inserted with no key held, through
/// [onComposedText]. Ordinary keystrokes reach the editor through the raw key stream, and reporting
/// those here too would insert them twice. While
/// [isComposing] is true the raw key stream must stand down: the keys that drive a composition
/// carry characters of their own that must stay out of the document.
///
/// Both callbacks also get how many characters before the caret the text replaces. The buffer keeps
/// the text the last key typed, so that the platform can replace it: macOS's press-and-hold accent
/// menu replaces the letter it was opened on.
class ComposedTextInput with TextInputClient {
  ComposedTextInput({required this.onComposedText, this.onComposing});

  final void Function(String text, int replacing) onComposedText;
  final void Function(String text, int caret, int replacing)? onComposing;

  static const TextInputConfiguration _configuration = TextInputConfiguration(
    inputType: TextInputType.multiline,
    inputAction: TextInputAction.newline,
    autocorrect: false,
    enableSuggestions: false,
    enableIMEPersonalizedLearning: false,
  );

  TextInputConnection? _connection;
  bool _composing = false;
  String? _justCommitted;
  // What the platform's buffer holds: nothing, or the text the last key typed.
  TextEditingValue _buffer = TextEditingValue.empty;

  /// True from the start of a composition until it commits or is abandoned.
  bool get isComposing => _composing;

  /// Also drops the key that ends a composition, which carries the composed character itself. It is
  /// matched by character, not timing: the platform does not order the commit and the keydown.
  bool swallowsKey(String character) {
    if (_composing) return true;
    final committed = _justCommitted;
    // A key with no character (a modifier, or "Process") cannot be the committing key.
    if (committed == null || character.isEmpty) return false;
    // Any character key answers the commit; a later one is real typing.
    _justCommitted = null;
    return character == committed;
  }

  void attach() {
    _activeComposition = this;
    if (_connection?.attached ?? false) return;
    _composing = false;
    _buffer = TextEditingValue.empty;
    _connection = TextInput.attach(this, _configuration)
      ..setEditingState(_buffer)
      ..show();
  }

  void detach() {
    if (identical(_activeComposition, this)) _activeComposition = null;
    _connection?.close();
    _connection = null;
    _composing = false;
    _buffer = TextEditingValue.empty;
  }

  /// A key the editor took from the raw key stream. The editor handles it, so the platform never
  /// sees its text: it is put in the buffer here, for the platform to replace. A key that types
  /// nothing, or types with Ctrl or Cmd held, empties the buffer.
  void keyTyped(String character, {bool shortcut = false}) {
    if (_composing) return;
    final text = !shortcut && _isPrintable(character) ? character : '';
    if (text == _buffer.text) return;
    _buffer = TextEditingValue(text: text, selection: TextSelection.collapsed(offset: text.length));
    _connection?.setEditingState(_buffer);
  }

  static bool _isPrintable(String character) =>
      character.isNotEmpty && character.runes.every((r) => r >= 0x20 && r != 0x7f);

  @override
  TextEditingValue? get currentTextEditingValue => _buffer;

  @override
  AutofillScope? get currentAutofillScope => null;

  @override
  void updateEditingValue(TextEditingValue value) {
    // What the platform kept of the buffer, what it replaced at its end, and what it put there.
    final kept = _keptLength(_buffer.text, value.text);
    final replacing = _buffer.text.length - kept;
    final text = value.text.substring(kept);
    if (value.composing.isValid) {
      _composing = true;
      onComposing?.call(text, (value.selection.baseOffset - kept).clamp(0, text.length), replacing);
      return;
    }
    final endsComposition = _composing;
    final committed = endsComposition || _isKeylessInsert(value) ? text : '';
    _composing = false;
    // The document, not the buffer, holds the text. Only what the last key typed stays, for the
    // platform to replace; anything older would only widen what its ranges can reach.
    final typedByKey = !endsComposition &&
        replacing == 0 &&
        text.isNotEmpty &&
        value.selection.extentOffset == value.text.length &&
        _keyIsDown();
    _buffer = typedByKey
        ? TextEditingValue(text: text, selection: TextSelection.collapsed(offset: text.length))
        : TextEditingValue.empty;
    _connection?.setEditingState(_buffer);
    if (committed.isNotEmpty) {
      // Only a composition's committing key carries the committed text again.
      if (endsComposition) _justCommitted = committed;
      onComposedText(committed, replacing);
    }
  }

  /// The length of the common start of [before] and [after], never splitting a surrogate pair.
  static int _keptLength(String before, String after) {
    final max = before.length < after.length ? before.length : after.length;
    var i = 0;
    while (i < max && before.codeUnitAt(i) == after.codeUnitAt(i)) {
      i++;
    }
    if (i > 0 && i < before.length && _isHighSurrogate(before.codeUnitAt(i - 1))) i--;
    return i;
  }

  static bool _isHighSurrogate(int unit) => unit >= 0xD800 && unit <= 0xDBFF;

  /// Text inserted into the empty buffer leaves the caret at its end. With the caret inside it, the
  /// text is a composition the web engine reported without its range (it does so when the caret
  /// starts inside the composed text); the buffer is cleared below, which cancels that composition,
  /// and the browser then delivers its text again as an insert of its own.
  static bool _isKeylessInsert(TextEditingValue value) =>
      value.selection.extentOffset == value.text.length && !_keyIsDown();

  /// Whether a non-modifier key is held. The platform reports a key's text between its press and
  /// its release, and that key already reached the editor through the raw key stream. Text inserted
  /// with no key held has no other way in. On macOS web a key pressed with Cmd held gets no release
  /// until the engine synthesizes one, up to 2 s later; text inserted before that is dropped.
  static bool _keyIsDown() =>
      HardwareKeyboard.instance.logicalKeysPressed.any((key) => !_modifierKeys.contains(key));

  static final Set<LogicalKeyboardKey> _modifierKeys = {
    LogicalKeyboardKey.shiftLeft,
    LogicalKeyboardKey.shiftRight,
    LogicalKeyboardKey.controlLeft,
    LogicalKeyboardKey.controlRight,
    LogicalKeyboardKey.altLeft,
    LogicalKeyboardKey.altRight,
    LogicalKeyboardKey.metaLeft,
    LogicalKeyboardKey.metaRight,
    LogicalKeyboardKey.altGraph,
    LogicalKeyboardKey.capsLock,
    LogicalKeyboardKey.numLock,
    LogicalKeyboardKey.scrollLock,
    LogicalKeyboardKey.fn,
    LogicalKeyboardKey.fnLock,
  };

  @override
  void connectionClosed() {
    _connection = null;
    _composing = false;
  }

  @override
  void performAction(TextInputAction action) {}

  @override
  void performPrivateCommand(String action, Map<String, dynamic> data) {}

  @override
  void updateFloatingCursor(RawFloatingCursorPoint point) {}

  @override
  void showAutocorrectionPromptRect(int start, int end) {}
}
