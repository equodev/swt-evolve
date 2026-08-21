import 'package:flutter/services.dart';

/// Gives a canvas-painted editor somewhere for the platform to compose text.
///
/// An editor that reads only the raw key stream cannot see composed input at all: the OS delivers
/// the composed character through the text-input pipeline, and without a connection open there is
/// no pipeline to deliver it to. On a dead-key layout the raw stream then carries only the
/// committing key — a space on U.S. International — so the quote the user asked for arrives as a
/// space. Accented characters and every CJK IME fail the same way.
///
/// Only *committed compositions* are reported, through [onComposedText]. Ordinary keystrokes reach
/// the editor through the raw key stream, and reporting those here too would insert them twice.
/// While [isComposing] is true the raw key stream must stand down: the keys that drive a
/// composition carry characters of their own that must stay out of the document.
class ComposedTextInput with TextInputClient {
  ComposedTextInput({required this.onComposedText});

  final void Function(String text) onComposedText;

  static const TextInputConfiguration _configuration = TextInputConfiguration(
    inputType: TextInputType.multiline,
    inputAction: TextInputAction.newline,
    autocorrect: false,
    enableSuggestions: false,
    enableIMEPersonalizedLearning: false,
  );

  TextInputConnection? _connection;
  bool _composing = false;

  /// True from the start of a composition until it commits or is abandoned.
  bool get isComposing => _composing;

  void attach() {
    if (_connection?.attached ?? false) return;
    _composing = false;
    _connection = TextInput.attach(this, _configuration)
      ..setEditingState(const TextEditingValue())
      ..show();
  }

  void detach() {
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
    if (value.composing.isValid) {
      _composing = true;
      return;
    }
    final committed = _composing ? value.text : '';
    _composing = false;
    // The buffer exists only to catch a composition, and the document — not it — holds the text.
    // Left filled, it would prefix itself onto whatever the next composition commits.
    _connection?.setEditingState(const TextEditingValue());
    if (committed.isNotEmpty) onComposedText(committed);
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
