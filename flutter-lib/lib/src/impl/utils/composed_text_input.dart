import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// The editor currently able to compose, if any. Shared by both key routes to Java: the focused
/// control's handler and the Display's forwarder.
ComposedTextInput? _activeComposition;

/// Answered once per key: asking clears the commit it answers. Safe because exactly one route
/// forwards a given key.
bool compositionOwnsKey(String character) =>
    _activeComposition?.swallowsKey(character) ?? false;

/// macOS hands the platform's text input only the keys Flutter leaves unhandled, so it composes
/// nothing unless the editor lets them through.
bool get _textInputSeesUnhandledKeysOnly => !kIsWeb && defaultTargetPlatform == TargetPlatform.macOS;

/// Whether [event] belongs to the platform's text input rather than to Java: a dead key, which
/// arrives with no character and opens a composition, and every key while one is open. SWT Cocoa
/// sends no KeyDown for either; the composition reaches Java through [ComposedTextInput]. Asking
/// changes nothing, so both key routes may ask about the same key.
bool compositionTakesKey(KeyEvent event) {
  final composition = _activeComposition;
  if (composition == null || !_textInputSeesUnhandledKeysOnly || event is KeyUpEvent) return false;
  return composition.isComposing || (event is KeyDownEvent && _isDeadKey(event));
}

bool _isDeadKey(KeyDownEvent event) {
  if ((event.character ?? '').isNotEmpty) return false;
  final keys = HardwareKeyboard.instance;
  if (keys.isMetaPressed || keys.isControlPressed) return false;
  final label = event.logicalKey.keyLabel;
  return label.length == 1 && label.trim().isNotEmpty;
}

/// Gives a canvas-painted editor somewhere for the platform to compose text.
///
/// An editor that reads only the raw key stream cannot see composed input at all: the OS delivers
/// the composed character through the text-input pipeline, and without a connection open there is
/// no pipeline to deliver it to. On a dead-key layout the raw stream then carries only the
/// committing key — a space on U.S. International — so the quote the user asked for arrives as a
/// space. Accented characters and every CJK IME fail the same way.
///
/// Only compositions are reported: [onComposing] with the text being composed and the caret inside
/// it, then [onComposedText] with the text that ends it. Ordinary keystrokes reach the editor
/// through the raw key stream, and reporting those here too would insert them twice. While
/// [isComposing] is true the raw key stream must stand down: the keys that drive a composition
/// carry characters of their own that must stay out of the document.
class ComposedTextInput with TextInputClient {
  ComposedTextInput({required this.onComposedText, this.onComposing});

  final void Function(String text) onComposedText;
  final void Function(String text, int caret)? onComposing;

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
    _settled = '';
    _connection = TextInput.attach(this, _configuration)
      ..setEditingState(const TextEditingValue())
      ..show();
  }

  void detach() {
    if (identical(_activeComposition, this)) _activeComposition = null;
    _connection?.close();
    _connection = null;
    _composing = false;
  }

  @override
  TextEditingValue? get currentTextEditingValue => const TextEditingValue();

  @override
  AutofillScope? get currentAutofillScope => null;

  @override
  void updateEditingValue(TextEditingValue value) {
    if (_textInputSeesUnhandledKeysOnly) {
      _takeUnhandledKeysText(value);
      return;
    }
    if (value.composing.isValid) {
      _composing = true;
      onComposing?.call(value.text, value.selection.baseOffset.clamp(0, value.text.length));
      return;
    }
    final committed = _composing ? value.text : '';
    _composing = false;
    // The buffer exists only to catch a composition, and the document — not it — holds the text.
    // Left filled, it would prefix itself onto whatever the next composition commits.
    _connection?.setEditingState(const TextEditingValue());
    if (committed.isNotEmpty) {
      _justCommitted = committed;
      onComposedText(committed);
    }
  }

  /// The buffer's text outside any composition, as far as it has been reported.
  String _settled = '';

  /// On macOS the text input only sees the keys left to it, so every character it settles is
  /// theirs, composed or not: a dead key followed by a letter it does not combine with commits the
  /// accent, then inserts the letter. The buffer is not cleared there, since a clear can cross an
  /// insertion already on its way and have it repeat what was reported; only what it gains is.
  void _takeUnhandledKeysText(TextEditingValue value) {
    final composing = value.composing;
    final settled = composing.isValid ? value.text.substring(0, composing.start) : value.text;
    final wasComposing = _composing;
    _composing = composing.isValid;
    final gained = settled.length > _settled.length && settled.startsWith(_settled)
        ? settled.substring(_settled.length)
        : '';
    _settled = settled;
    if (gained.isNotEmpty) onComposedText(gained);
    if (_composing) {
      final composed = composing.textInside(value.text);
      onComposing?.call(
          composed, (value.selection.baseOffset - composing.start).clamp(0, composed.length));
    } else if (wasComposing && gained.isEmpty) {
      onComposing?.call('', 0);
    }
  }

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
