import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';

import 'line_document.dart';
import 'line_editor_render.dart';

export 'line_document.dart' show LineDocument;

/// A text being edited: its lines, the selection, and the range an input method is composing.
@immutable
class LineEditValue {
  const LineEditValue({required this.document, required this.selection, this.composing = TextRange.empty});

  LineEditValue.text(String text, {TextSelection? selection})
      : document = LineDocument(text),
        selection = selection ?? TextSelection.collapsed(offset: text.length),
        composing = TextRange.empty;

  static final LineEditValue empty = LineEditValue(document: LineDocument.empty, selection: const TextSelection.collapsed(offset: 0));

  final LineDocument document;
  final TextSelection selection;
  final TextRange composing;

  String get text => document.text;

  /// This value with [range] replaced by [replacement], the selection placed after it.
  LineEditValue replaced(TextRange range, String replacement, {TextSelection? selection}) {
    final doc = document.replaced(range.start, range.end, replacement);
    return LineEditValue(
      document: doc,
      selection: selection ?? TextSelection.collapsed(offset: range.start + replacement.length),
    );
  }

  /// This value edited into [text], with [selection]: only the one range the two texts differ in is
  /// replaced, so the lines it does not reach keep what was laid out for them.
  LineEditValue editedTo(String text, TextSelection selection) {
    final (:prefix, :suffix) = commonEnds(this.text, text);
    return LineEditValue(
      document: document.editedInto(text, prefix, this.text.length - suffix, text.length - suffix),
      selection: selection.copyWith(
        baseOffset: selection.baseOffset.clamp(0, text.length),
        extentOffset: selection.extentOffset.clamp(0, text.length),
      ),
    );
  }

  LineEditValue copyWith({TextSelection? selection, TextRange? composing}) => LineEditValue(
        document: document,
        selection: selection ?? this.selection,
        composing: composing ?? this.composing,
      );

  /// The value as Flutter's text editing intents carry it; the text is shared, not copied.
  TextEditingValue get asTextEditingValue =>
      TextEditingValue(text: document.text, selection: selection, composing: composing);

  @override
  bool operator ==(Object other) =>
      other is LineEditValue &&
      identical(other.document, document) &&
      other.selection == selection &&
      other.composing == composing;

  @override
  int get hashCode => Object.hash(identityHashCode(document), selection, composing);
}

/// The value of a [LineEditor].
class LineEditorController extends ValueNotifier<LineEditValue> {
  LineEditorController([String text = '']) : super(LineEditValue.text(text, selection: const TextSelection.collapsed(offset: -1)));

  String get text => value.text;

  TextSelection get selection => value.selection;
  set selection(TextSelection selection) => value = value.copyWith(selection: selection, composing: TextRange.empty);
}

/// What an edit becomes before it is shown: [proposed], or [old] to hold it back.
typedef LineEditFormatter = LineEditValue Function(LineEditValue old, LineEditValue proposed);

/// A multi-line editor of plain text in one style that wraps at its width, drawn as a multi-line
/// [TextField]'s [EditableText] draws its text, caret and selection, and edited with the same keys,
/// but laid out only where it shows ([RenderLineEditor]): its cost does not grow with the text.
class LineEditor extends StatefulWidget {
  const LineEditor({
    super.key,
    required this.controller,
    required this.focusNode,
    required this.style,
    this.textAlign = TextAlign.start,
    this.readOnly = false,
    this.enabled = true,
    this.cursorColor,
    this.maxLength,
    this.formatter,
    this.onChanged,
    this.onWordSelected,
    this.handlesPointer = true,
  });

  final LineEditorController controller;
  final FocusNode focusNode;
  final TextStyle style;
  final TextAlign textAlign;
  final bool readOnly;
  final bool enabled;
  final Color? cursorColor;

  /// The most characters the text may have; an edit past it is cut to fit, as TextField's
  /// enforced limit cuts it.
  final int? maxLength;

  final LineEditFormatter? formatter;
  final ValueChanged<String>? onChanged;

  /// A double click selected the word [start, end).
  final void Function(int start, int end)? onWordSelected;

  /// False when the pointer reaches the editor through [LineEditorState.handlePointerDown] and the
  /// others, from a parent that covers more than the text: a TextField takes a press anywhere in
  /// its decoration as a press on the text.
  final bool handlesPointer;

  @override
  State<LineEditor> createState() => LineEditorState();
}

class LineEditorState extends State<LineEditor> with TextInputClient implements TextSelectionDelegate {
  final ScrollController _scrollController = ScrollController();
  final GlobalKey _viewportKey = GlobalKey();
  final ValueNotifier<bool> _cursorVisible = ValueNotifier(false);
  Timer? _blink;
  TextInputConnection? _connection;

  LineEditValue get _value => widget.controller.value;

  RenderLineEditor? get _render => _viewportKey.currentContext?.findRenderObject() as RenderLineEditor?;

  bool get _editable => widget.enabled && !widget.readOnly;

  @override
  void initState() {
    super.initState();
    widget.controller.addListener(_valueChanged);
    widget.focusNode.addListener(_focusChanged);
    if (widget.focusNode.hasFocus) _focusChanged();
  }

  @override
  void didUpdateWidget(LineEditor oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (!identical(oldWidget.controller, widget.controller)) {
      oldWidget.controller.removeListener(_valueChanged);
      widget.controller.addListener(_valueChanged);
    }
    if (!identical(oldWidget.focusNode, widget.focusNode)) {
      oldWidget.focusNode.removeListener(_focusChanged);
      widget.focusNode.addListener(_focusChanged);
    }
    if (oldWidget.readOnly != widget.readOnly || oldWidget.enabled != widget.enabled) {
      if (!_editable) _closeConnection();
      if (_editable && widget.focusNode.hasFocus) _openConnection();
      if (oldWidget.readOnly != widget.readOnly && widget.focusNode.hasFocus) _restartBlink();
    }
  }

  @override
  void dispose() {
    widget.controller.removeListener(_valueChanged);
    widget.focusNode.removeListener(_focusChanged);
    _closeConnection();
    _blink?.cancel();
    _cursorVisible.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  // --- value ---

  LineEditValue? _lastValue;

  void _valueChanged() {
    final value = _value;
    if (value == _lastValue) return;
    _lastValue = value;
    setState(() {});
    if (_connection != null && !_applyingInput) _sendEditingState();
    if (widget.focusNode.hasFocus) _restartBlink();
  }

  /// Shows [proposed], through [LineEditor.formatter] and the length limit, as the user's edit.
  void _userEdit(LineEditValue proposed, {bool reveal = true}) {
    final old = _value;
    var next = proposed;
    if (!identical(next.document, old.document)) {
      if (!_editable) return;
      next = _limited(old, next);
      final format = widget.formatter;
      if (format != null) next = format(old, next);
    }
    if (next == old) return;
    final textChanged = !identical(next.document, old.document) && next.text != old.text;
    widget.controller.value = next;
    if (textChanged) widget.onChanged?.call(next.text);
    if (reveal && (!widget.readOnly || next.selection != old.selection)) _scheduleReveal();
  }

  LineEditValue _limited(LineEditValue old, LineEditValue next) {
    final max = widget.maxLength;
    if (max == null || max <= 0 || next.text.length <= old.text.length) return next;
    final length = next.text.characters.length;
    if (length <= max) return next;
    // TextField's enforced limit keeps the old text when it is already at or past the limit.
    if (old.text.characters.length >= max) return old;
    final overflow = length - max;
    if (!next.document.isEditOf(old.document)) return old;
    final start = old.selection.isValid ? old.selection.start : old.text.length;
    final insertedLength = next.text.length - old.text.length + (old.selection.isValid ? old.selection.end - old.selection.start : 0);
    final inserted = next.text.substring(start, start + insertedLength);
    final kept = inserted.characters.take(math.max(0, inserted.characters.length - overflow)).toString();
    return old.replaced(TextRange(start: start, end: old.selection.isValid ? old.selection.end : start), kept);
  }

  void _setSelection(TextSelection selection, {bool reveal = true}) {
    final clamped = TextSelection(
      baseOffset: selection.baseOffset.clamp(0, _value.text.length),
      extentOffset: selection.extentOffset.clamp(0, _value.text.length),
      affinity: selection.affinity,
      isDirectional: selection.isDirectional,
    );
    _userEdit(_value.copyWith(selection: clamped, composing: TextRange.empty), reveal: reveal);
  }

  /// [current] edited into [target]'s text, with [target]'s selection: the one edit that differs.
  LineEditValue _replacedTo(LineEditValue current, LineEditValue target) {
    final a = current.text, b = target.text;
    var prefix = 0;
    final max = math.min(a.length, b.length);
    while (prefix < max && a.codeUnitAt(prefix) == b.codeUnitAt(prefix)) {
      prefix++;
    }
    var suffix = 0;
    while (suffix < max - prefix && a.codeUnitAt(a.length - 1 - suffix) == b.codeUnitAt(b.length - 1 - suffix)) {
      suffix++;
    }
    return current.replaced(
      TextRange(start: prefix, end: a.length - suffix),
      b.substring(prefix, b.length - suffix),
      selection: target.selection,
    );
  }

  // --- focus, caret blink ---

  void _focusChanged() {
    if (widget.focusNode.hasFocus) {
      if (!_value.selection.isValid) {
        widget.controller.value = _value.copyWith(selection: TextSelection.collapsed(offset: _value.text.length));
      }
      if (_editable) {
        _openConnection();
        _scheduleReveal();
      }
      _restartBlink();
    } else {
      _closeConnection();
      _blink?.cancel();
      _cursorVisible.value = false;
    }
    setState(() {});
  }

  static const Duration _blinkHalfPeriod = Duration(milliseconds: 500);

  /// A read-only editor shows no caret, as EditableText's `showCursor` defaults to `!readOnly`.
  void _restartBlink() {
    _blink?.cancel();
    _cursorVisible.value = !widget.readOnly;
    if (widget.readOnly || !widget.focusNode.hasFocus || !_value.selection.isCollapsed) return;
    _blink = Timer.periodic(_blinkHalfPeriod, (_) => _cursorVisible.value = !_cursorVisible.value);
  }

  // --- revealing the caret, as EditableText scrolls itself to it ---

  static const Duration _revealDuration = Duration(milliseconds: 100);

  bool _revealScheduled = false;

  void _scheduleReveal() {
    if (_revealScheduled) return;
    _revealScheduled = true;
    SchedulerBinding.instance.addPostFrameCallback((_) {
      _revealScheduled = false;
      final render = _render;
      if (render == null || !mounted || !_scrollController.hasClients || !_value.selection.isValid) return;
      final caret = render.getLocalRectForCaret(_value.selection.extent);
      final target = _offsetToReveal(render, caret);
      // Always a new animation, even to where the view is: it ends any reveal still running.
      _scrollController.animateTo(target, duration: _revealDuration, curve: Curves.fastOutSlowIn);
    });
  }

  double _offsetToReveal(RenderLineEditor render, Rect rect) {
    final position = _scrollController.position;
    if (!position.allowImplicitScrolling) return _scrollController.offset;
    final height = render.size.height;
    final expanded = Rect.fromCenter(
      center: rect.center,
      width: rect.width,
      height: math.max(rect.height, render.preferredLineHeight),
    );
    final additional = expanded.height >= height
        ? height / 2 - expanded.center.dy
        : clampDouble(0.0, expanded.bottom - height, expanded.top);
    return clampDouble(additional + _scrollController.offset, position.minScrollExtent, position.maxScrollExtent);
  }

  // --- text input: the input method sees a window of the text around the selection ---

  /// Texts up to this length go to the input method whole, as a TextField sends them.
  static const int _wholeTextLimit = 4096;

  /// The context on either side of the selection an input method gets of a longer text.
  static const int _windowContext = 1024;

  int _windowStart = 0;
  String _windowText = '';
  bool _applyingInput = false;

  void _openConnection() {
    if (_connection != null && _connection!.attached) return;
    _connection = TextInput.attach(
      this,
      TextInputConfiguration(
        inputType: TextInputType.multiline,
        inputAction: TextInputAction.newline,
        readOnly: widget.readOnly,
        enableDeltaModel: false,
      ),
    );
    _sendEditingState();
    _connection!.show();
    SchedulerBinding.instance.addPostFrameCallback((_) => _sendGeometry());
  }

  void _closeConnection() {
    _connection?.close();
    _connection = null;
  }

  TextEditingValue _windowValue() {
    final value = _value;
    final text = value.text;
    if (text.length <= _wholeTextLimit) {
      _windowStart = 0;
      _windowText = text;
      return value.asTextEditingValue;
    }
    final doc = value.document;
    final sel = value.selection.isValid ? value.selection : TextSelection.collapsed(offset: text.length);
    final from = doc.lineStart(doc.lineAt(math.max(0, sel.start - _windowContext)));
    final to = doc.lineEnd(doc.lineAt(math.min(text.length, sel.end + _windowContext)));
    _windowStart = from;
    _windowText = text.substring(from, to);
    TextRange shift(TextRange r) =>
        r.isValid && r.start >= from && r.end <= to ? TextRange(start: r.start - from, end: r.end - from) : TextRange.empty;
    return TextEditingValue(
      text: _windowText,
      selection: sel.copyWith(baseOffset: sel.baseOffset - from, extentOffset: sel.extentOffset - from),
      composing: shift(value.composing),
    );
  }

  void _sendEditingState() => _connection?.setEditingState(_windowValue());

  void _sendGeometry() {
    final render = _render;
    final connection = _connection;
    if (render == null || connection == null || !connection.attached || !render.hasSize) return;
    connection.setEditableSizeAndTransform(render.size, render.getTransformTo(null));
    if (_value.selection.isValid) {
      connection.setCaretRect(render.getLocalRectForCaret(_value.selection.extent));
    }
  }

  @override
  TextEditingValue? get currentTextEditingValue => _windowValue();

  @override
  AutofillScope? get currentAutofillScope => null;

  @override
  void updateEditingValue(TextEditingValue value) {
    final old = _value;
    final from = _windowStart;
    final before = _windowText;
    final after = value.text;
    var proposed = old;
    if (after != before) {
      final (:prefix, :suffix) = commonEnds(before, after);
      proposed = old.replaced(
        TextRange(start: from + prefix, end: from + before.length - suffix),
        after.substring(prefix, after.length - suffix),
      );
    }
    final selection = value.selection.isValid
        ? value.selection.copyWith(baseOffset: value.selection.baseOffset + from, extentOffset: value.selection.extentOffset + from)
        : proposed.selection;
    final composing = value.composing.isValid
        ? TextRange(start: value.composing.start + from, end: value.composing.end + from)
        : TextRange.empty;
    _applyingInput = true;
    try {
      _userEdit(proposed.copyWith(selection: selection, composing: composing));
    } finally {
      _applyingInput = false;
    }
    // What the input method holds no longer matches what is shown: a held edit, or a window that moved.
    final shown = _windowValue();
    if (shown != value) _connection?.setEditingState(shown);
  }

  @override
  void performAction(TextInputAction action) {
    if (action == TextInputAction.newline) _replaceSelection('\n');
  }

  @override
  void performSelector(String selectorName) {
    final intent = intentForMacOSSelector(selectorName);
    final context = primaryFocus?.context;
    if (intent != null && context != null) Actions.invoke(context, intent);
  }

  @override
  void performPrivateCommand(String action, Map<String, dynamic> data) {}

  @override
  void updateFloatingCursor(RawFloatingCursorPoint point) {}

  @override
  void showAutocorrectionPromptRect(int start, int end) {}

  @override
  void connectionClosed() {
    _connection = null;
  }

  @override
  void didChangeInputControl(TextInputControl? oldControl, TextInputControl? newControl) {}

  @override
  void insertContent(KeyboardInsertedContent content) {}

  // --- TextSelectionDelegate, which the text editing actions and the clipboard use ---

  @override
  TextEditingValue get textEditingValue => _value.asTextEditingValue;

  @override
  void userUpdateTextEditingValue(TextEditingValue value, SelectionChangedCause? cause) {
    if (value.text == _value.text) {
      _setSelection(value.selection);
    } else {
      _userEdit(_value.editedTo(value.text, value.selection));
    }
  }

  /// Scrolls [position] into view now, as EditableText does before a keyboard selection change.
  @override
  void bringIntoView(TextPosition position) {
    final render = _render;
    if (render == null || !_scrollController.hasClients) return;
    _scrollController.jumpTo(_offsetToReveal(render, render.getLocalRectForCaret(position)));
  }

  /// A selection the keyboard made: brought into view, then shown.
  void _selectFromKeys(TextSelection selection) {
    bringIntoView(selection.extent);
    _setSelection(selection);
  }

  @override
  void hideToolbar([bool hideHandles = true]) {}

  @override
  bool get cutEnabled => _editable && !_value.selection.isCollapsed;

  @override
  bool get copyEnabled => !_value.selection.isCollapsed;

  @override
  bool get pasteEnabled => _editable;

  @override
  bool get selectAllEnabled {
    final value = _value;
    switch (defaultTargetPlatform) {
      case TargetPlatform.macOS:
        return false;
      case TargetPlatform.iOS:
        return value.text.isNotEmpty && value.selection.isCollapsed;
      case TargetPlatform.android:
      case TargetPlatform.fuchsia:
      case TargetPlatform.linux:
      case TargetPlatform.windows:
        return value.text.isNotEmpty && !(value.selection.start == 0 && value.selection.end == value.text.length);
    }
  }

  @override
  bool get liveTextInputEnabled => false;

  @override
  bool get lookUpEnabled => false;

  @override
  bool get searchWebEnabled => false;

  @override
  bool get shareEnabled => false;

  @override
  void copySelection(SelectionChangedCause cause) {
    final selection = _value.selection;
    if (!selection.isValid || selection.isCollapsed) return;
    Clipboard.setData(ClipboardData(text: selection.textInside(_value.text)));
  }

  @override
  void cutSelection(SelectionChangedCause cause) {
    if (!cutEnabled) return;
    copySelection(cause);
    _replaceSelection('');
  }

  @override
  Future<void> pasteText(SelectionChangedCause cause) async {
    if (!_editable) return;
    final data = await Clipboard.getData(Clipboard.kTextPlain);
    final text = data?.text;
    if (text == null || !mounted) return;
    _replaceSelection(text);
  }

  @override
  void selectAll(SelectionChangedCause cause) {
    _setSelection(TextSelection(baseOffset: 0, extentOffset: _value.text.length));
  }

  void _replaceSelection(String text) {
    final selection = _value.selection;
    if (!selection.isValid) return;
    _userEdit(_value.replaced(TextRange(start: selection.start, end: selection.end), text));
  }

  // --- boundaries, as EditableText finds them ---

  TextBoundary _characterBoundary() => CharacterBoundary(_value.text);
  TextBoundary _wordBoundary() => _LineWordBoundary(_render!, _value.document);
  TextBoundary _linebreak() => LineBoundary(_render!);
  TextBoundary _paragraphBoundary() => ParagraphBoundary(_value.text);
  TextBoundary _documentBoundary() => DocumentBoundary(_value.text);

  TextPosition _moveBeyond(TextPosition extent, bool forward, TextBoundary boundary) {
    final offset = forward
        ? boundary.getTrailingTextBoundaryAt(extent.offset) ?? _value.text.length
        : boundary.getLeadingTextBoundaryAt(extent.offset - 1) ?? 0;
    return TextPosition(offset: offset);
  }

  TextPosition _moveTo(TextPosition extent, bool forward, TextBoundary boundary) {
    final int caretOffset;
    switch (extent.affinity) {
      case TextAffinity.upstream:
        if (extent.offset < 1 && !forward) return const TextPosition(offset: 0);
        caretOffset = math.max(0, extent.offset - 1);
      case TextAffinity.downstream:
        caretOffset = extent.offset;
    }
    return forward
        ? TextPosition(
            offset: boundary.getTrailingTextBoundaryAt(caretOffset) ?? _value.text.length,
            affinity: TextAffinity.upstream,
          )
        : TextPosition(offset: boundary.getLeadingTextBoundaryAt(caretOffset) ?? 0);
  }

  late final _VerticalMove _vertical = _VerticalMove(this);

  late final Map<Type, Action<Intent>> _actions = <Type, Action<Intent>>{
    DoNothingAndStopPropagationTextIntent: DoNothingAction(consumesKey: false),
    ReplaceTextIntent: CallbackAction<ReplaceTextIntent>(onInvoke: (intent) {
      _userEdit(_value.replaced(intent.replacementRange, intent.replacementText));
      return null;
    }),
    UpdateSelectionIntent: CallbackAction<UpdateSelectionIntent>(onInvoke: (intent) {
      _selectFromKeys(intent.newSelection);
      return null;
    }),
    DirectionalFocusIntent: DirectionalFocusAction.forTextField(),
    DeleteCharacterIntent: _DeleteAction<DeleteCharacterIntent>(this, _characterBoundary, _moveBeyond),
    DeleteToNextWordBoundaryIntent: _DeleteAction<DeleteToNextWordBoundaryIntent>(this, _wordBoundary, _moveBeyond),
    DeleteToLineBreakIntent: _DeleteAction<DeleteToLineBreakIntent>(this, _linebreak, _moveTo),
    ExtendSelectionByCharacterIntent: _SelectionAction<ExtendSelectionByCharacterIntent>(
        this, _characterBoundary, _moveBeyond, ignoreNonCollapsedSelection: false),
    ExtendSelectionToNextWordBoundaryIntent: _SelectionAction<ExtendSelectionToNextWordBoundaryIntent>(
        this, _wordBoundary, _moveBeyond, ignoreNonCollapsedSelection: true),
    ExtendSelectionToNextParagraphBoundaryIntent: _SelectionAction<ExtendSelectionToNextParagraphBoundaryIntent>(
        this, _paragraphBoundary, _moveBeyond, ignoreNonCollapsedSelection: true),
    ExtendSelectionToLineBreakIntent: _SelectionAction<ExtendSelectionToLineBreakIntent>(
        this, _linebreak, _moveTo, ignoreNonCollapsedSelection: true),
    ExtendSelectionVerticallyToAdjacentLineIntent: _VerticalAction<ExtendSelectionVerticallyToAdjacentLineIntent>(_vertical),
    ExtendSelectionVerticallyToAdjacentPageIntent: _VerticalAction<ExtendSelectionVerticallyToAdjacentPageIntent>(_vertical),
    ExtendSelectionToNextParagraphBoundaryOrCaretLocationIntent:
        _SelectionAction<ExtendSelectionToNextParagraphBoundaryOrCaretLocationIntent>(
            this, _paragraphBoundary, _moveBeyond, ignoreNonCollapsedSelection: true),
    ExtendSelectionToDocumentBoundaryIntent: _SelectionAction<ExtendSelectionToDocumentBoundaryIntent>(
        this, _documentBoundary, _moveBeyond, ignoreNonCollapsedSelection: true),
    ExtendSelectionToNextWordBoundaryOrCaretLocationIntent:
        _SelectionAction<ExtendSelectionToNextWordBoundaryOrCaretLocationIntent>(
            this, _wordBoundary, _moveBeyond, ignoreNonCollapsedSelection: true),
    ScrollToDocumentBoundaryIntent: CallbackAction<ScrollToDocumentBoundaryIntent>(onInvoke: (intent) {
      bringIntoView(TextPosition(offset: intent.forward ? _value.text.length : 0));
      return null;
    }),
    // A page at a time, as EditableText scrolls itself; a line's scroll is left to what is around it.
    ScrollIntent: CallbackAction<ScrollIntent>(onInvoke: (intent) {
      if (intent.type != ScrollIncrementType.page || !_scrollController.hasClients) return null;
      final position = _scrollController.position;
      if (position.maxScrollExtent == 0.0 && position.minScrollExtent == 0.0) return null;
      final increment = 0.8 * position.viewportDimension;
      final destination = clampDouble(
        position.pixels + (intent.direction == AxisDirection.down ? increment : -increment),
        position.minScrollExtent,
        position.maxScrollExtent,
      );
      if (destination != position.pixels) _scrollController.jumpTo(destination);
      return null;
    }),
    ExpandSelectionToLineBreakIntent: _SelectionAction<ExpandSelectionToLineBreakIntent>(
        this, _linebreak, _moveTo, ignoreNonCollapsedSelection: true, isExpand: true),
    ExpandSelectionToDocumentBoundaryIntent: _SelectionAction<ExpandSelectionToDocumentBoundaryIntent>(
        this, _documentBoundary, _moveTo, ignoreNonCollapsedSelection: true, isExpand: true, extentAtIndex: true),
    SelectAllTextIntent: CallbackAction<SelectAllTextIntent>(onInvoke: (intent) {
      selectAll(intent.cause);
      return null;
    }),
    CopySelectionTextIntent: CallbackAction<CopySelectionTextIntent>(onInvoke: (intent) {
      if (intent.collapseSelection) {
        cutSelection(intent.cause);
      } else {
        copySelection(intent.cause);
      }
      return null;
    }),
    PasteTextIntent: CallbackAction<PasteTextIntent>(onInvoke: (intent) {
      pasteText(intent.cause);
      return null;
    }),
    TransposeCharactersIntent: CallbackAction<TransposeCharactersIntent>(onInvoke: (_) {
      _transpose();
      return null;
    }),
  };

  void _transpose() {
    final selection = _value.selection;
    final text = _value.text;
    if (!selection.isCollapsed || selection.baseOffset == 0 || text.length < 2) return;
    final range = CharacterRange.at(text, selection.baseOffset);
    final atEnd = selection.baseOffset == text.length;
    if (atEnd) range.moveBack();
    final after = range.current;
    if (!range.moveBack() && range.current.isEmpty) return;
    range.moveBack();
    final before = range.current;
    final start = range.stringBeforeLength;
    final end = start + before.length + after.length;
    _userEdit(_value.replaced(TextRange(start: start, end: end), after + before,
        selection: TextSelection.collapsed(offset: end)));
  }

  // --- the mouse ---

  int _clicks = 0;
  Duration? _lastClick;
  Offset? _lastClickPosition;
  TextSelection? _dragOrigin;

  static const Duration _multiClickWindow = Duration(milliseconds: 500);

  /// A secondary press leaves the selection alone: it opens the application's Menu, which the
  /// control around the editor shows, and no menu of its own.
  void handlePointerDown(PointerDownEvent event) {
    if (!widget.enabled || event.buttons & kPrimaryButton == 0) return;
    final render = _render;
    if (render == null) return;
    if (!widget.focusNode.hasFocus) widget.focusNode.requestFocus();
    final local = render.globalToLocal(event.position);
    final position = render.getPositionForPoint(local);
    final last = _lastClick;
    final near = _lastClickPosition != null && (_lastClickPosition! - event.position).distance < kDoubleTapSlop;
    _clicks = last != null && event.timeStamp - last < _multiClickWindow && near ? _clicks + 1 : 1;
    _lastClick = event.timeStamp;
    _lastClickPosition = event.position;
    final shift = HardwareKeyboard.instance.isShiftPressed;
    switch (_clicks) {
      case 1:
        if (shift && _value.selection.isValid) {
          _setSelection(_value.selection.extendTo(position));
          _dragOrigin = TextSelection.collapsed(offset: _value.selection.baseOffset);
        } else {
          _setSelection(TextSelection.fromPosition(position));
          _dragOrigin = TextSelection.fromPosition(position);
        }
      case 2:
        final word = _wordAt(render, position);
        _setSelection(word);
        _dragOrigin = _value.selection;
        widget.onWordSelected?.call(word.start, word.end);
      default:
        final line = _lineRange(position.offset);
        _setSelection(TextSelection(baseOffset: line.start, extentOffset: line.end));
        _dragOrigin = _value.selection;
    }
  }

  void handlePointerMove(PointerMoveEvent event) {
    final origin = _dragOrigin;
    final render = _render;
    if (origin == null || render == null || event.buttons & kPrimaryButton == 0) return;
    final local = render.globalToLocal(event.position);
    // Dragging past the top or bottom scrolls the text, as a TextField's selection drag does.
    if (_scrollController.hasClients) {
      final position = _scrollController.position;
      final overshoot = local.dy < 0 ? local.dy : (local.dy > render.size.height ? local.dy - render.size.height : 0.0);
      if (overshoot != 0) {
        _scrollController.jumpTo(clampDouble(position.pixels + overshoot, position.minScrollExtent, position.maxScrollExtent));
      }
    }
    final position = render.getPositionForPoint(local);
    if (_clicks >= 2) {
      final unit = _clicks == 2 ? _wordAt(render, position) : _lineRange(position.offset);
      final forward = position.offset >= origin.start;
      _setSelection(forward
          ? TextSelection(baseOffset: origin.start, extentOffset: math.max(unit.end, origin.end))
          : TextSelection(baseOffset: origin.end, extentOffset: math.min(unit.start, origin.start)));
    } else {
      _setSelection(TextSelection(baseOffset: origin.baseOffset, extentOffset: position.offset, affinity: position.affinity));
    }
  }

  /// The paragraph [offset] is in, with its line break, as a triple click selects it.
  TextRange _lineRange(int offset) {
    final text = _value.text;
    final boundary = ParagraphBoundary(text);
    final start = boundary.getLeadingTextBoundaryAt(offset == text.length ? offset - 1 : offset) ?? 0;
    final end = boundary.getTrailingTextBoundaryAt(offset) ?? text.length;
    return TextRange(start: start, end: end);
  }

  /// The word a double click at [position] selects, as RenderEditable finds it on a desktop: none
  /// past the end of the text, else the word the layout puts there.
  TextSelection _wordAt(RenderLineEditor render, TextPosition position) {
    final length = _value.text.length;
    if (position.offset >= length) {
      return TextSelection.fromPosition(TextPosition(offset: length, affinity: TextAffinity.upstream));
    }
    final word = render.getWordBoundary(position);
    return TextSelection(baseOffset: word.start, extentOffset: word.end);
  }

  void handlePointerUp(PointerUpEvent event) {
    _dragOrigin = null;
  }

  // --- build ---

  /// The opacity of the theme's primary colour a TextField highlights its selection in, when no
  /// [DefaultSelectionStyle] gives a colour.
  static const double _selectionOpacity = 0.40;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final selectionStyle = DefaultSelectionStyle.of(context);
    final platform = theme.platform;
    final apple = platform == TargetPlatform.iOS || platform == TargetPlatform.macOS;
    final cursorColor = widget.cursorColor ?? selectionStyle.cursorColor ?? theme.colorScheme.primary;
    final selectionColor =
        selectionStyle.selectionColor ?? theme.colorScheme.primary.withOpacity(_selectionOpacity);
    final focused = widget.focusNode.hasFocus;
    final textScaler = MediaQuery.textScalerOf(context);
    final lineHeightScale = MediaQuery.maybeLineHeightScaleFactorOverrideOf(context);
    final strut = StrutStyle.fromTextStyle(widget.style, forceStrutHeight: true).merge(StrutStyle(height: lineHeightScale));
    final devicePixelRatio = MediaQuery.devicePixelRatioOf(context);

    final scrollable = Scrollable(
      controller: _scrollController,
      axisDirection: AxisDirection.down,
      scrollBehavior: ScrollConfiguration.of(context).copyWith(scrollbars: true, overscroll: false),
      viewportBuilder: (context, offset) => LineEditorViewport(
        key: _viewportKey,
        document: _value.document,
        selection: _value.selection.isValid ? _value.selection : null,
        composing: _editable && focused ? _value.composing : TextRange.empty,
        style: widget.style,
        strutStyle: strut,
        textAlign: widget.textAlign,
        textDirection: Directionality.of(context),
        textScaler: textScaler,
        textHeightBehavior: DefaultTextHeightBehavior.maybeOf(context),
        offset: offset,
        showCursor: _cursorVisible,
        cursorColor: cursorColor,
        cursorWidth: 2.0,
        cursorRadius: apple ? const Radius.circular(2.0) : null,
        cursorOffset: apple ? Offset(iOSHorizontalOffset / devicePixelRatio, 0) : Offset.zero,
        paintCursorAboveText: apple,
        selectionColor: focused ? selectionColor : null,
        devicePixelRatio: devicePixelRatio,
        hasFocus: focused,
      ),
    );

    return Actions(
      actions: _actions,
      child: Focus(
        focusNode: widget.focusNode,
        includeSemantics: false,
        child: MouseRegion(
          cursor: widget.enabled ? SystemMouseCursors.text : MouseCursor.defer,
          child: Listener(
            onPointerDown: widget.handlesPointer ? handlePointerDown : null,
            onPointerMove: widget.handlesPointer ? handlePointerMove : null,
            onPointerUp: widget.handlesPointer ? handlePointerUp : null,
            child: NotificationListener<ScrollNotification>(
              onNotification: (_) {
                if (_connection != null) SchedulerBinding.instance.addPostFrameCallback((_) => _sendGeometry());
                return false;
              },
              child: scrollable,
            ),
          ),
        ),
      ),
    );
  }

  /// Used by the tests: the render object.
  @visibleForTesting
  RenderLineEditor? get debugRender => _render;
}

/// Words as a line's own layout breaks it into them, across the lines of [document], each line
/// break a word of its own, as a paragraph of the whole text breaks it.
class _LineWordBoundary extends TextBoundary {
  _LineWordBoundary(this.render, this.document);

  final RenderLineEditor render;
  final LineDocument document;

  TextBoundary _of(int line) => render.wordBoundariesOf(line);

  @override
  int? getLeadingTextBoundaryAt(int position) {
    if (position < 0) return null;
    final clamped = position.clamp(0, document.length);
    final line = document.lineAt(clamped);
    final start = document.lineStart(line);
    final local = clamped - start;
    // A line break is a word of its own.
    if (local >= document.lineEnd(line) - start) return clamped;
    return start + (_of(line).getLeadingTextBoundaryAt(local) ?? 0);
  }

  @override
  int? getTrailingTextBoundaryAt(int position) {
    if (position >= document.length) return null;
    final line = document.lineAt(position.clamp(0, document.length));
    final start = document.lineStart(line);
    final local = position - start;
    final length = document.lineEnd(line) - start;
    if (local >= length) return document.lineEnd(line) + 1;
    final found = _of(line).getTrailingTextBoundaryAt(local);
    if (found != null && found > local) return start + found;
    return document.lineEnd(line);
  }
}

typedef _ApplyBoundary = TextPosition Function(TextPosition extent, bool forward, TextBoundary boundary);

/// EditableText's delete action, over a [LineEditorState].
class _DeleteAction<T extends DirectionalTextEditingIntent> extends ContextAction<T> {
  _DeleteAction(this.state, this.boundary, this.apply);

  final LineEditorState state;
  final TextBoundary Function() boundary;
  final _ApplyBoundary apply;

  @override
  Object? invoke(T intent, [BuildContext? context]) {
    final value = state._value;
    final selection = value.selection;
    if (!selection.isValid) return null;
    final atomic = state._characterBoundary();
    if (!selection.isCollapsed) {
      final range = TextRange(
        start: atomic.getLeadingTextBoundaryAt(selection.start) ?? value.text.length,
        end: atomic.getTrailingTextBoundaryAt(selection.end - 1) ?? 0,
      );
      state._userEdit(value.replaced(range, ''));
      return null;
    }
    final target = apply(selection.base, intent.forward, boundary()).offset;
    final from = intent.forward
        ? atomic.getLeadingTextBoundaryAt(selection.baseOffset) ?? value.text.length
        : atomic.getTrailingTextBoundaryAt(selection.baseOffset - 1) ?? 0;
    final range = TextRange(start: math.min(from, target), end: math.max(from, target));
    if (range.isCollapsed) return null;
    state._userEdit(value.replaced(range, ''));
    return null;
  }

  @override
  bool get isActionEnabled => state._editable && state._value.selection.isValid;
}

/// EditableText's caret and selection moves, over a [LineEditorState].
class _SelectionAction<T extends DirectionalCaretMovementIntent> extends ContextAction<T> {
  _SelectionAction(
    this.state,
    this.boundary,
    this.apply, {
    required this.ignoreNonCollapsedSelection,
    this.isExpand = false,
    this.extentAtIndex = false,
  });

  final LineEditorState state;
  final TextBoundary Function() boundary;
  final _ApplyBoundary apply;
  final bool ignoreNonCollapsedSelection;
  final bool isExpand;
  final bool extentAtIndex;

  bool _atWrapUpstream(TextPosition position) {
    final text = state._value.text;
    final end = TextPosition(offset: state._render!.getLineAtOffset(position).end, affinity: TextAffinity.upstream);
    return end == position && end.offset != text.length && text.codeUnitAt(position.offset) != 0x0A;
  }

  bool _atWrapDownstream(TextPosition position) {
    final text = state._value.text;
    final start = TextPosition(offset: state._render!.getLineAtOffset(position).start);
    return start == position && start.offset != 0 && text.codeUnitAt(position.offset - 1) != 0x0A;
  }

  @override
  Object? invoke(T intent, [BuildContext? context]) {
    final selection = state._value.selection;
    if (!selection.isValid || state._render == null) return null;
    final collapse = intent.collapseSelection;
    if (!selection.isCollapsed && !ignoreNonCollapsedSelection && collapse) {
      state._selectFromKeys(TextSelection.collapsed(offset: intent.forward ? selection.end : selection.start));
      return null;
    }
    var extent = selection.extent;
    if (intent.continuesAtWrap) {
      if (intent.forward && _atWrapUpstream(extent)) {
        extent = TextPosition(offset: extent.offset);
      } else if (!intent.forward && _atWrapDownstream(extent)) {
        extent = TextPosition(offset: extent.offset, affinity: TextAffinity.upstream);
      }
    }
    final targetBase = isExpand &&
        (intent.forward ? selection.baseOffset > selection.extentOffset : selection.baseOffset < selection.extentOffset);
    final newExtent = apply(targetBase ? selection.base : extent, intent.forward, boundary());
    final next = collapse || (!isExpand && newExtent.offset == selection.baseOffset)
        ? TextSelection.fromPosition(newExtent)
        : isExpand
            ? selection.expandTo(newExtent, extentAtIndex || selection.isCollapsed)
            : selection.extendTo(newExtent);
    final collapseToBase = intent.collapseAtReversal &&
        (selection.baseOffset - selection.extentOffset) * (selection.baseOffset - next.extentOffset) < 0;
    state._selectFromKeys(collapseToBase ? TextSelection.fromPosition(selection.base) : next);
    return null;
  }

  @override
  bool get isActionEnabled => state._value.selection.isValid;
}

/// The run of vertical moves a caret makes, keeping the x it started at, as EditableText's
/// [VerticalCaretMovementRun] keeps it.
class _VerticalMove {
  _VerticalMove(this.state);

  final LineEditorState state;
  double? _x;
  TextSelection? _runSelection;

  void invoke(DirectionalCaretMovementIntent intent) {
    final render = state._render;
    final value = state._value;
    if (render == null || !value.selection.isValid) return;
    if (_runSelection != value.selection) _x = null;
    final extent = value.selection.extent;
    final x = _x ??= render.caretX(extent);
    TextPosition? next;
    if (intent is ExtendSelectionVerticallyToAdjacentPageIntent) {
      final rows = math.max(1, (render.size.height / render.preferredLineHeight).floor());
      next = render.positionOnAdjacentRow(extent, x, intent.forward ? rows : -rows);
    } else {
      next = render.positionOnAdjacentRow(extent, x, intent.forward ? 1 : -1);
    }
    next ??= intent.forward ? TextPosition(offset: value.text.length) : const TextPosition(offset: 0);
    final selection = intent.collapseSelection ? TextSelection.fromPosition(next) : value.selection.extendTo(next);
    state._selectFromKeys(selection);
    if (state._value.selection == selection) {
      _runSelection = selection;
    } else {
      _x = null;
    }
  }
}

class _VerticalAction<T extends DirectionalCaretMovementIntent> extends ContextAction<T> {
  _VerticalAction(this.move);

  final _VerticalMove move;

  @override
  Object? invoke(T intent, [BuildContext? context]) {
    move.invoke(intent);
    return null;
  }

  @override
  bool get isActionEnabled => move.state._value.selection.isValid;
}
