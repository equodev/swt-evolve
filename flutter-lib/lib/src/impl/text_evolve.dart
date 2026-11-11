import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:ui' show SemanticsInputType;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart' show PipelineOwner;
import 'package:flutter/services.dart';
import '../comm/comm.dart';
import '../gen/event.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart' show WireKey;
import '../impl/scrollable_evolve.dart';
import '../theme/theme_extensions/text_theme_extension.dart';
import '../theme/theme_settings/text_theme_settings.dart';
import 'key_forwarding.dart';
import 'shell_evolve.dart';
import 'key_mapping.dart';
import 'menu_evolve.dart' show MenuImpl, OpenMenus;
import 'utils/hover_arbiter.dart';
import 'utils/key_edit_order.dart';
import 'utils/at_rest.dart';
import 'utils/double_tap_detector.dart';
import 'utils/line_editor.dart';
import 'utils/text_undo.dart';
import 'utils/text_utils.dart';
import 'utils/veto_gate.dart';
import 'utils/widget_utils.dart';
import 'utils/client_caret_report.dart';
import 'utils/pending_text_echoes.dart';
import 'control_evolve.dart' show ControlImpl;

class TextImpl<T extends TextSwt, V extends VText>
    extends ScrollableImpl<T, V> with PendingTextEchoes, ClientCaretReport, AtRestHandover implements UndoableTextField {
  late TextEditingController _controller;

  /// The text of a multi-line field, which a [LineEditor] edits: laid out a line at a time and only
  /// where it shows, so a long text costs what its visible lines cost.
  LineEditorController? _lines;

  final GlobalKey<LineEditorState> _lineEditor = GlobalKey<LineEditorState>();

  /// Whether this field should be a [LineEditor]: every multi-line field with bounds but a password
  /// one. Assistive technology, and the tests that drive the field through it, use the TextField's
  /// own semantics, so it stays a TextField for them.
  bool get _wantsLines =>
      hasStyle(state.style, SWT.MULTI) &&
      !hasStyle(state.style, SWT.PASSWORD) &&
      hasBounds(state.bounds) &&
      !WidgetsBinding.instance.semanticsEnabled;

  /// Which editor the last build showed. Bounds arrive and semantics turn on after the field is
  /// made, so it can change; each editor's controller is updated only while it shows, so the value
  /// is carried over when it does ([_chooseEditor]).
  bool? _shownLines;

  /// Whether this field is a [LineEditor].
  bool get _usesLines => _shownLines ?? _wantsLines;

  void _chooseEditor() {
    final lines = _wantsLines;
    final shown = _shownLines;
    _shownLines = lines;
    if (shown == null || shown == lines) return;
    fromJava(() {
      if (lines) {
        final selection = _controller.selection;
        _lineController.value = _lineController.value.editedTo(
          _controller.text,
          selection.isValid ? selection : TextSelection.collapsed(offset: _controller.text.length),
        );
      } else {
        _controller.value = TextEditingValue(text: _lineController.text, selection: _lineController.selection);
      }
    });
  }

  LineEditorController get _lineController =>
      _lines ??= LineEditorController(state.text ?? '')..addListener(_updateCaretPosition);

  /// The text on display, whichever editor shows it.
  String get _currentText => _usesLines ? _lineController.text : _controller.text;

  final FocusNode _focusNode = FocusNode();
  /// Stands in for [_focusNode] while the field is drawn at rest: focus arriving here, by traversal or
  /// from Java, mounts the TextField and is handed to it.
  final FocusNode _restFocus = FocusNode(debugLabel: 'Text at rest');

  /// Whether the field shows no text, which decides whether the decoration shows the hint.
  final ValueNotifier<bool> _isEmpty = ValueNotifier(true);

  /// Once focused, a field keeps its TextField: what it shows of a long value is where its editing
  /// scrolled it, which only the TextField knows.
  bool _everFocused = false;

  /// Whether the TextField is mounted. A field nobody is interacting with is drawn without it, with
  /// the same decoration around its text; mounting it is a hover, a focus or a tap away.
  bool get _live => !_restable || _everFocused || handingOver || hovered || _focusNode.hasFocus || forcedLive;

  /// The fields drawn at rest: single-line text shown as it is.
  bool get _restable =>
      !hasStyle(state.style, SWT.MULTI) &&
      !hasStyle(state.style, SWT.PASSWORD) &&
      _obscuringCharacter(state.echoCharacter) == null &&
      hasBounds(state.bounds);

  @override
  FocusNode get swtFocusNode => _usesLines || _live ? _focusNode : _restFocus;


  // A VerifyListener runs before the character is displayed, so edits are held un-rendered.
  final VetoGate _modifyGate = VetoGate.modify();

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _isEmpty.value = _controller.text.isEmpty;
    _controller.addListener(_updateCaretPosition);
    _controller.addListener(_trackEmptiness);
    _focusNode.addListener(_handleFocusChange);
    _restFocus.addListener(_handleRestFocus);
    KeyEditOrder.instance.registerEditor(_focusNode, _takesTyping);
    _modifyGate.attach(state.wireType, state.id);
    _modifyGate.onUnmatched = _revertOptimisticEdit;
    // Initialize caret position
    WidgetsBinding.instance.addPostFrameCallback((_) => fromJava(_updateCaretPosition));
    // What the API did before this field was shown is nothing its undo has seen.
    _apiEditsSeen = _lastApiEdit(state.edits);
  }

  // --- undo, as the platform's control keeps it (see TextUndo) ---

  final TextEditKindDispatcher _kinds = TextEditKindDispatcher();

  /// The window this field is in, whose undo Cocoa's fields share.
  ShellImpl? _shell;

  TextUndo get _undo => TextUndo.of(this, _shell, TextUndoFlavor.current);

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _shell = context.findAncestorStateOfType<ShellImpl>();
  }

  /// While an undo applies, its edit is no edit of the user's to record.
  bool _applyingUndo = false;

  /// What the field held when an input method began composing: the composition is recorded once,
  /// as the text it commits.
  ({String text, TextSelection selection})? _compositionBase;

  /// The number of the last API edit undo has been told about.
  int _apiEditsSeen = 0;

  static int _lastApiEdit(List<int>? edits) {
    var last = 0;
    if (edits == null) return last;
    for (var i = 0; i + 2 < edits.length; i += 3) {
      last = math.max(last, edits[i]);
    }
    return last;
  }

  /// Hands undo the API edits it has not seen: [VText.edits] holds number, kind and start per
  /// edit, [VText.editTexts] the text each replaced and inserted.
  void _applyApiEdits() {
    final edits = state.edits;
    final texts = state.editTexts ?? const <String>[];
    if (edits == null) return;
    for (var i = 0, t = 0; i + 2 < edits.length; i += 3, t += 2) {
      final number = edits[i];
      if (number <= _apiEditsSeen) continue;
      _apiEditsSeen = number;
      final edit = TextApiEdit.of(edits[i + 1]);
      if (edit == null) continue;
      _undo.api(this, edit, edits[i + 2], t < texts.length ? texts[t] : '', t + 1 < texts.length ? texts[t + 1] : '');
    }
  }

  @override
  String get undoText => _currentText;

  @override
  bool get undoSingleLine => !hasStyle(state.style, SWT.MULTI);

  @override
  bool get undoFocused => _focusNode.hasFocus;

  @override
  bool get undoReadOnly => !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY);

  @override
  bool get undoAlive => mounted;

  @override
  void applyUndo(int start, int end, String replacement, TextSelection selection) {
    _applyingUndo = true;
    try {
      if (_usesLines) {
        final old = _lineController.value;
        final next = old.replaced(TextRange(start: start, end: end), replacement, selection: selection);
        if (identical(_gateLines(old, next), next)) {
          _lineController.value = next;
          _handleLinesChanged(next.text);
        }
      } else {
        final old = _controller.value;
        final next = TextEditingValue(text: old.text.replaceRange(start, end, replacement), selection: selection);
        if (identical(_gateEdit(old, next), next)) {
          _controller.value = next;
          _handleTextChanged(next.text);
        }
      }
    } finally {
      _applyingUndo = false;
    }
  }

  /// What records the edit from [oldText] to [newText] once it is shown, or null when it is none of
  /// the user's: an undo, a composition still going, or no change of the text.
  VoidCallback? _undoRecorder(String oldText, TextSelection oldSelection, String newText,
      TextSelection newSelection, TextRange composing, {int? start, int? end, int? inserted}) {
    if (_applyingUndo) return null;
    if (composing.isValid && !composing.isCollapsed) {
      _compositionBase ??= (text: oldText, selection: oldSelection);
      return null;
    }
    final base = _compositionBase;
    _compositionBase = null;
    final fromText = base?.text ?? oldText;
    final fromSelection = base?.selection ?? oldSelection;
    if (fromText == newText) return null;
    final int at;
    final String deleted, added;
    if (base == null && start != null && end != null && inserted != null) {
      at = start;
      deleted = fromText.substring(start, end);
      added = newText.substring(start, start + inserted);
    } else {
      (at, deleted, added) = _editBetween(fromText, fromSelection, newText, newSelection);
    }
    final fromCaret = !fromSelection.isValid || fromSelection.isCollapsed;
    final endsAtCaret = fromSelection.isValid && at + deleted.length == fromSelection.end;
    final kind = _kinds.kindOfEdit(deleted, added,
        composing: base != null, fromCaret: fromCaret, endsAtCaret: endsAtCaret);
    return () {
      if (mounted) _undo.edited(this, at, deleted, added, kind, fromCaret: fromCaret);
    };
  }

  /// The one range [from] and [to] differ in, placed where the selections say the edit was: an
  /// edit happens at the selection, so a run of equal characters around it does not move it.
  static (int, String, String) _editBetween(
      String from, TextSelection fromSelection, String to, TextSelection toSelection) {
    final max = math.min(from.length, to.length);
    var prefixLimit = max;
    var suffixLimit = max;
    if (fromSelection.isValid && toSelection.isValid) {
      prefixLimit = math.min(prefixLimit, math.min(fromSelection.start, toSelection.start));
      suffixLimit = math.min(suffixLimit, math.min(from.length - fromSelection.end, to.length - toSelection.end));
    }
    var prefix = 0;
    while (prefix < prefixLimit && from.codeUnitAt(prefix) == to.codeUnitAt(prefix)) {
      prefix++;
    }
    var suffix = 0;
    suffixLimit = math.min(suffixLimit, max - prefix);
    while (suffix < suffixLimit &&
        from.codeUnitAt(from.length - 1 - suffix) == to.codeUnitAt(to.length - 1 - suffix)) {
      suffix++;
    }
    return (prefix, from.substring(prefix, from.length - suffix), to.substring(prefix, to.length - suffix));
  }

  /// The field with its platform's undo keys, and with what its edits are made by watched.
  Widget _withUndo(Widget field) {
    return Shortcuts(
      shortcuts: textUndoShortcuts(TextUndoFlavor.current),
      child: Actions(
        dispatcher: _kinds,
        actions: <Type, Action<Intent>>{
          TextUndoIntent: CallbackAction<TextUndoIntent>(onInvoke: (_) {
            _undo.undo(this);
            return null;
          }),
          TextRedoIntent: CallbackAction<TextRedoIntent>(onInvoke: (_) {
            _undo.redo(this);
            return null;
          }),
          TextNoUndoIntent: DoNothingAction(),
        },
        child: Focus(
          canRequestFocus: false,
          skipTraversal: true,
          onKeyEvent: (node, event) {
            if (event is KeyDownEvent || event is KeyRepeatEvent) _kinds.keyDown(event);
            return KeyEventResult.ignored;
          },
          child: field,
        ),
      ),
    );
  }

  /// Holds an edit while the field is vetoable: the proposal goes to Java as the Modify, the
  /// controller keeps the old value until the verdict. Selection-only changes and the
  /// single-line newline path (submit semantics, see [_handleTextChanged]) stay optimistic.
  TextEditingValue _gateEdit(TextEditingValue oldValue, TextEditingValue newValue) {
    if (newValue.text == oldValue.text) return newValue;
    final isSingleLineExpand =
        hasBounds(state.bounds) && !hasStyle(state.style, SWT.MULTI);
    if (isSingleLineExpand && newValue.text.contains('\n')) return newValue;
    final record = _undoRecorder(
        oldValue.text, oldValue.selection, newValue.text, newValue.selection, newValue.composing);
    if (!_modifyGate.armed) {
      record?.call();
      return newValue;
    }
    _holdEdit(oldValue, newValue, () => _controller.value = newValue, record);
    return oldValue;
  }

  /// The Modify Java is deciding on; one at a time, so that Java never decides on, or commits, an
  /// edit made on top of one it may veto.
  _ProposedEdit? _inFlight;

  /// The edits made while [_inFlight] waits for its verdict, oldest first. The field shows none of
  /// them yet; each is made on the text the verdicts before it leave.
  final List<_EditAtSelection> _held = [];

  /// Proposes the edit from [oldValue] to [newValue], or holds it while another one is in flight.
  /// [apply] shows [newValue] once allowed; [record] then takes the undo step.
  void _holdEdit(TextEditingValue oldValue, TextEditingValue newValue, void Function() apply, VoidCallback? record) {
    if (_inFlight == null) {
      _proposeModify(_ProposedEdit(newValue.text, newValue.selection.baseOffset, apply, record));
    } else {
      _held.add(_EditAtSelection.between(oldValue.text, oldValue.selection, newValue.text, newValue.selection));
    }
  }

  /// Sends [proposal] to Java as the Modify it decides on, shows it once allowed, then proposes the
  /// next held edit.
  void _proposeModify(_ProposedEdit proposal) {
    _inFlight = proposal;
    var sent = false;
    var answered = false;
    _modifyGate.propose((doit) {
      if (!identical(_inFlight, proposal)) return;
      _inFlight = null;
      if (!doit) {
        // The held edit was never rendered, so it just drops; stop suppressing Java's echoes.
        clearSentTextEchoes();
      } else if (mounted) {
        proposal.apply();
        state.text = proposal.text;
        proposal.record?.call();
      }
      answered = true;
      // A verdict that came before its own Modify went out waits for it, to keep the wire order.
      if (sent) _proposeHeld();
    });
    _sendModify(proposal.text, proposal.start);
    sent = true;
    if (answered) _proposeHeld();
  }

  /// Proposes the oldest held edit on what the field now shows; applies them all when Java no
  /// longer answers.
  void _proposeHeld() {
    while (_held.isNotEmpty && _inFlight == null && mounted) {
      final edit = _held.removeAt(0);
      final (fromText, fromSelection) =
          _usesLines ? (_lineController.text, _lineController.selection) : (_controller.text, _controller.selection);
      final (text, selection) = edit.on(fromText, fromSelection);
      if (text == fromText) continue;
      final record = _undoRecorder(fromText, fromSelection, text, selection, TextRange.empty);
      final void Function() apply = _usesLines
          ? () => _lineController.value = _lineController.value.editedTo(text, selection)
          : () => _controller.value = TextEditingValue(text: text, selection: selection);
      if (_modifyGate.armed) {
        _proposeModify(_ProposedEdit(text, selection.baseOffset, apply, record));
      } else {
        apply();
        state.text = text;
        record?.call();
        _sendModify(text, selection.baseOffset);
      }
    }
  }

  void _sendModify(String text, int start) {
    recordSentText(text);
    widget.sendModifyModify(
      state,
      VEvent()
        ..text = text
        ..start = start,
    );
    KeyEditOrder.instance.editApplied();
  }

  bool get _isReadOnly => !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY);

  /// Rejection for an edit that was never held (the embedded backend never arms the gate), so it is
  /// already on screen. Clearing the echo guard lets Java's corrective push through.
  void _revertOptimisticEdit(bool doit) {
    if (doit) return;
    clearSentTextEchoes();
    if (!mounted) return;
    final authoritative = state.text ?? '';
    if (_usesLines) {
      final lines = _lineController;
      if (lines.text != authoritative) {
        lines.value = lines.value.editedTo(authoritative, TextSelection.collapsed(offset: authoritative.length));
      }
      return;
    }
    if (_controller.text != authoritative) {
      fromJava(() => _controller.value = _controller.value.copyWith(
            text: authoritative,
            selection: TextSelection.collapsed(offset: authoritative.length),
            composing: TextRange.empty,
          ));
    }
  }

  /// What this field shows through its controller rather than its build: a change naming only
  /// these is handed to the controller, which rebuilds the editable text and nothing else.
  static const _controllerProperties = {WireKey.k_Text_text, WireKey.k_Text_selection, WireKey.k_Text_edits};

  @override
  void setValue(V value) {
    if (mounted && (lastChange?.isOnly(_controllerProperties) ?? false)) {
      extraSetState();
      return;
    }
    super.setValue(value);
  }

  @override
  void extraSetState() => fromJava(_applyJavaState);

  void _applyJavaState() {
    _applyApiEdits();
    if (_usesLines) {
      _pushToLines();
      return;
    }
    String newText = state.text ?? "";
    // A Java value push may be the echo of a Modify this field sent moments ago. If the user
    // has typed further in the meantime, resetting the controller to the echoed (older) text
    // silently drops those keystrokes (the mid-word lost character on slow machines). See
    // PendingTextEchoes: our own in-flight echoes are ignored, external changes fall through.
    if (isStaleTextEcho(newText, _controller.text)) return;
    bool textChanged = _controller.text != newText;

    if (textChanged) {
      final cursorOffset = _isReadOnly ? 0 : newText.length;

      _controller.value = _controller.value.copyWith(
        text: newText,
        selection: TextSelection.collapsed(offset: cursorOffset),
        composing: TextRange.empty,
      );

      if (state.selection != null) {
        // The selection Java last sent may belong to a longer text.
        final end = newText.length;
        _controller.selection = TextSelection(
          baseOffset: state.selection!.x.clamp(0, end),
          extentOffset: state.selection!.y.clamp(0, end),
        );
      }
    }

    final sel = state.selection;
    if (_selectionPushed && sel != null && sel.x != sel.y && sel.y <= _controller.text.length) {
      final desired = TextSelection(baseOffset: sel.x, extentOffset: sel.y);
      if (_controller.selection != desired) {
        _controller.selection = desired;
      }
    }
  }

  /// Whether this push carries the selection. Java leaves out one the client moved itself, typing
  /// included, so the range it pushed before is no longer the field's and is not put back.
  bool get _selectionPushed => lastChange?.touches(WireKey.k_Text_selection) ?? true;

  void _trackEmptiness() => _isEmpty.value = _controller.text.isEmpty;

  /// What Java pushed, shown in the [LineEditor], as [extraSetState] shows it in the TextField.
  void _pushToLines() {
    final lines = _lineController;
    final newText = state.text ?? '';
    if (isStaleTextEcho(newText, lines.text)) return;
    if (lines.text != newText) {
      var selection = TextSelection.collapsed(offset: _isReadOnly ? 0 : newText.length);
      final pushed = state.selection;
      if (pushed != null) selection = TextSelection(baseOffset: pushed.x, extentOffset: pushed.y);
      lines.value = lines.value.editedTo(newText, selection);
    }
    final sel = state.selection;
    if (_selectionPushed && sel != null && sel.x != sel.y && sel.y <= lines.text.length) {
      final desired = TextSelection(baseOffset: sel.x, extentOffset: sel.y);
      if (lines.selection != desired) lines.selection = desired;
    }
  }

  /// [_gateEdit] for the [LineEditor]: while the field is vetoable the edit goes to Java as the
  /// Modify and the field keeps showing the old value until the verdict.
  LineEditValue _gateLines(LineEditValue oldValue, LineEditValue newValue) {
    if (identical(newValue.document, oldValue.document) || newValue.text == oldValue.text) return newValue;
    final doc = newValue.document;
    final direct = doc.isEditOf(oldValue.document);
    final record = _undoRecorder(
        oldValue.text, oldValue.selection, newValue.text, newValue.selection, newValue.composing,
        start: direct ? doc.editStart : null,
        end: direct ? doc.editEnd : null,
        inserted: direct ? doc.insertedLength : null);
    if (!_modifyGate.armed) {
      record?.call();
      return newValue;
    }
    _holdEdit(TextEditingValue(text: oldValue.text, selection: oldValue.selection),
        TextEditingValue(text: newValue.text, selection: newValue.selection), () => _lineController.value = newValue, record);
    return oldValue;
  }

  void _handleLinesChanged(String value) {
    state.text = value;
    _sendModify(value, _lineController.selection.baseOffset);
  }

  /// A multi-line field drawn by a [LineEditor] in the decoration a TextField puts around its text.
  Widget _buildLineEditor(
    BuildContext context,
    TextThemeExtension widgetTheme,
    bool enabled,
    TextAlign textAlign,
  ) {
    final textStyle = getTextFieldTextStyle(context, state, widgetTheme);
    final decorationTheme = InputDecorationTheme.of(context);
    final own = _decoration(context, widgetTheme, true, true);
    // What TextField makes of its decoration and of its style.
    final decoration = own.applyDefaults(decorationTheme).copyWith(
          enabled: enabled,
          hintMaxLines: own.hintMaxLines ?? decorationTheme.hintMaxLines,
        );
    final inputStyle = Theme.of(context).textTheme.bodyLarge!;
    final style = inputStyle
        .merge(TextStyle(color: enabled ? inputStyle.color : inputStyle.color?.withOpacity(0.38)))
        .merge(textStyle);
    final lines = _lineController;
    final editor = RepaintBoundary(
      child: LineEditor(
        key: _lineEditor,
        controller: lines,
        focusNode: _focusNode,
        style: style,
        textAlign: textAlign,
        readOnly: _isReadOnly,
        enabled: enabled,
        cursorColor: getForegroundColor(
          foreground: state.foreground,
          defaultColor: widgetTheme.textColor,
          context: context,
          minContrast: widgetTheme.minTextContrast,
        ),
        maxLength: state.textLimit,
        formatter: _gateLines,
        onChanged: _handleLinesChanged,
        handlesPointer: false,
        // The MouseDoubleClick itself is the press's (ControlImpl); the word it selects reaches Java
        // as a selection report, through _updateCaretPosition.
        onWordSelected: (start, end) {
          state.selection = VPoint()
            ..x = start
            ..y = end;
        },
      ),
    );
    // A press anywhere in the decoration is a press on the text, as a TextField takes it.
    return MouseRegion(
      cursor: enabled ? SystemMouseCursors.text : SystemMouseCursors.basic,
      child: Listener(
        onPointerDown: (e) {
          // The application Menu opens through ControlImpl.applyMenu(): the editor never sees the
          // secondary press, which focuses the field and leaves its caret, as a native Text does.
          if (ControlImpl.isMenuDetectTrigger(ControlImpl.swtButtonOf(e.buttons))) {
            keepCaretOnSecondaryPress(lines.selection, (caret) => lines.selection = caret, lines.text.length);
            if (!_focusNode.hasFocus) _focusNode.requestFocus();
            return;
          }
          _lineEditor.currentState?.handlePointerDown(e);
        },
        onPointerMove: (e) => _lineEditor.currentState?.handlePointerMove(e),
        onPointerUp: (e) => _lineEditor.currentState?.handlePointerUp(e),
        child: ListenableBuilder(
          listenable: Listenable.merge([_focusNode, lines]),
          builder: (context, child) => InputDecorator(
            decoration: decoration,
            baseStyle: textStyle,
            textAlign: textAlign,
            textAlignVertical: TextAlignVertical.top,
            isHovering: hovered,
            isFocused: _focusNode.hasFocus,
            isEmpty: lines.text.isEmpty,
            expands: true,
            child: child,
          ),
          child: editor,
        ),
      ),
    );
  }

  /// Hands the focus over even when the field is already live: a hover in the frame that requested
  /// the node at rest mounts the TextField before the focus lands there, and the node at rest then
  /// unmounts with the focus, which goes back to whatever held it before.
  void _handleRestFocus() {
    if (_restFocus.hasPrimaryFocus) _handOver();
  }

  /// Mounts the TextField and gives it the focus the field at rest received, with the caret at
  /// [tapped] when a press brought it, else with the selection Java holds.
  void _handOver([int? tapped, int clicks = 1]) => handOver(() {
        final end = _controller.text.length;
        if (tapped != null) {
          _controller.selection = _clickSelection(tapped.clamp(0, end), clicks);
          _selectDragFromRest(tapped);
        } else {
          fromJava(() => _controller.selection = _javaSelection(end));
        }
        _focusNode.requestFocus();
      });

  /// What getSelection() answers in Java, within a text [end] characters long.
  TextSelection _javaSelection(int end) {
    final selection = state.selection;
    if (selection != null) {
      return TextSelection(baseOffset: selection.x.clamp(0, end), extentOffset: selection.y.clamp(0, end));
    }
    return TextSelection.collapsed(offset: (state.caretPosition ?? 0).clamp(0, end));
  }

  /// A press puts the caret where it landed; a press that opens the context menu leaves it where Java
  /// has it, as a native Text does.
  void _handOverOnPress(PointerDownEvent event) {
    final clicks = _clicks.registerTap(position: event.position);
    final text = _atRestText.currentContext?.findRenderObject();
    final menu = ControlImpl.isMenuDetectTrigger(ControlImpl.swtButtonOf(event.buttons));
    final tapped = !menu && text is _RenderTextAtRest ? text.offsetAt(event.position) : null;
    _restPress = tapped == null ? null : (pointer: event.pointer, offset: tapped);
    _restDragTo = null;
    _handOver(tapped, clicks);
  }

  /// What the [clicks]-th click of a sequence at [offset] selects, as the field's own clicks do: the
  /// caret there, the word under it on a second click, the whole line on a third. A double click
  /// whose presses both come before the field is mounted reaches the field at rest twice.
  TextSelection _clickSelection(int offset, int clicks) {
    final text = _controller.text;
    if (clicks >= 3) return TextSelection(baseOffset: 0, extentOffset: text.length);
    if (clicks == 2) {
      final (start, end) = getWordBoundaries(text, offset);
      _wordSelected(start, end);
      return TextSelection(baseOffset: start, extentOffset: end);
    }
    return TextSelection.collapsed(offset: offset);
  }

  void _wordSelected(int start, int end) {
    state.selection = VPoint()
      ..x = start
      ..y = end;
  }

  /// The clicks on this field, the one on the field at rest included: the TextField it hands over to
  /// is mounted after that press, so a second click pairs with it here.
  final DoubleTapDetector _clicks = DoubleTapDetector(slop: 20.0);

  /// The press on the field at rest, until its release, and where its drag got to. The TextField is
  /// mounted after the press and never sees that pointer, so the drag selects from here.
  ({int pointer, int offset})? _restPress;
  Offset? _restDragTo;

  void _dragFromRest(PointerMoveEvent event) {
    final press = _restPress;
    if (press == null || press.pointer != event.pointer) return;
    _restDragTo = event.position;
    _selectDragFromRest(press.offset);
  }

  void _releaseFromRest(PointerEvent event) {
    if (_restPress?.pointer == event.pointer) _restPress = null;
  }

  /// Selects from [base] to where the drag from the field at rest got to, once the TextField is there.
  void _selectDragFromRest(int base) {
    final to = _restDragTo;
    final editable = _focusNode.context?.findAncestorStateOfType<EditableTextState>()?.renderEditable;
    if (to == null || editable == null || !editable.attached) return;
    final end = _controller.text.length;
    _controller.selection = TextSelection(
        baseOffset: base.clamp(0, end), extentOffset: editable.getPositionForPoint(to).offset.clamp(0, end));
  }

  /// The text of the field at rest, which maps a press to the offset the caret goes to.
  final GlobalKey _atRestText = GlobalKey();

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TextThemeExtension>();
    if (widgetTheme == null) {
      return const SizedBox.shrink();
    }
    // Text builds its own tree and never routes through ControlImpl.wrap().
    if (hidden) {
      return const SizedBox.shrink();
    }

    final textAlign = getTextAlignFromStyle(state.style, TextAlign.left);
    final isMultiLine = hasStyle(state.style, SWT.MULTI);
    final enabled = state.enabled ?? false;
    final editable = state.editable ?? true;
    final hasValidBounds = hasBounds(state.bounds);

    _chooseEditor();
    final textField = _usesLines
        ? _buildLineEditor(context, widgetTheme, enabled, textAlign)
        : _live
        ? _buildTextField(
            context,
            widgetTheme,
            enabled,
            textAlign,
            isMultiLine,
            hasValidBounds,
          )
        : _buildAtRest(context, widgetTheme, enabled, textAlign);

    final fieldWrapper = _buildTextFieldWrapper(
      _withUndo(textField),
      widgetTheme,
      isMultiLine,
      hasValidBounds,
    );
    return tagSemantics(
      wrapDnd(
        // When a whole-tree Display forwards keys from its single top-level handler, that handler
        // already delivers shortcuts to Java while a Text has focus, so skip the per-field wrapper
        // to avoid double-dispatch. In the embedded backend (no whole-tree Display) it is the only
        // path — Text builds its own field via [wrapDnd] and never routes through [ControlImpl.wrap].
        MouseRegion(
          onEnter: (_) => setHovered(true),
          onExit: (_) => setHovered(false),
          child: trackPointer(
            displayLevelKeyForwardingActive ? fieldWrapper : _forwardShortcutKeys(fieldWrapper),
          ),
        ),
      ),
    );
  }

  /// Forwards shortcut keystrokes the text field itself does not consume (e.g. Ctrl/Cmd+S) to Java
  /// as SWT.KeyDown, so an app's Display key filter or KeyListener — the mechanism Eclipse RCP
  /// command bindings use — sees them even while a Text has focus. Ordinary typing and the field's own
  /// editing shortcuts are consumed by the TextField first and never bubble up here, so this does
  /// not double-process input. The wrapper never takes focus itself; it only observes keys bubbling
  /// from the field. Only used in the embedded backend (see the [build] call site).
  Widget _forwardShortcutKeys(Widget child) {
    return Focus(
      canRequestFocus: false,
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent) {
          final vEvent = mapNewKeyEventToSwt(event);
          if (vEvent.keyCode != 0 || vEvent.character != 0) {
            widget.sendKeyKeyDown(state, vEvent);
          }
        }
        return KeyEventResult.ignored;
      },
      child: child,
    );
  }

  InputDecoration _decoration(
    BuildContext context,
    TextThemeExtension widgetTheme,
    bool isMultiLine,
    bool hasValidBounds,
  ) {
    var decoration = getInputDecoration(
      context,
      state,
      widgetTheme,
      _controller,
      () {
        _controller.clear();
        var e = VEvent()..detail = SWT.ICON_CANCEL;
        widget.sendSelectionDefaultSelection(state, e);
      },
    );

    final hasBorderStyle = hasStyle(state.style, SWT.BORDER);
    final parentBg = ParentBackgroundScope.backgroundOf(context);
    if (parentBg != null && _isReadOnly && !hasBorderStyle) {
      decoration = decoration.copyWith(filled: true, fillColor: parentBg);
    }

    final isPassword = hasStyle(state.style, SWT.PASSWORD);
    final singleLine = !isMultiLine || isPassword;

    if (hasValidBounds) {
      if (singleLine) {
        final isSearch = hasStyle(state.style, SWT.SEARCH);
        InputBorder noGap(InputBorder? b) =>
            (!isSearch && b is OutlineInputBorder) ? b.copyWith(gapPadding: 0.0) : b!;
        decoration = decoration.copyWith(
          isDense: false,
          contentPadding: singleLineTextInset,
          border: noGap(decoration.border),
          enabledBorder: noGap(decoration.enabledBorder),
          focusedBorder: noGap(decoration.focusedBorder),
          disabledBorder: noGap(decoration.disabledBorder),
          constraints: BoxConstraints.tightFor(
            height: state.bounds!.height.toDouble(),
          ),
        );
      } else {
        decoration = decoration.copyWith(
          contentPadding: singleLineTextInset.copyWith(top: 4.0, bottom: 4.0),
        );
      }
    }

    return decoration;
  }

  Widget _buildTextField(
    BuildContext context,
    TextThemeExtension widgetTheme,
    bool enabled,
    TextAlign textAlign,
    bool isMultiLine,
    bool hasValidBounds,
  ) {
    final textStyle = getTextFieldTextStyle(context, state, widgetTheme);
    final decoration = _decoration(context, widgetTheme, isMultiLine, hasValidBounds);
    final isPassword = hasStyle(state.style, SWT.PASSWORD);
    final echoChar = (isPassword || isMultiLine)
        ? null
        : _obscuringCharacter(state.echoCharacter);
    final isObscured = isPassword || echoChar != null;
    final singleLine = !isMultiLine || isPassword;
    final shouldExpand = hasValidBounds && !singleLine;

    final cursorColor = getForegroundColor(
      foreground: state.foreground,
      defaultColor: widgetTheme.textColor,
      context: context,
      minContrast: widgetTheme.minTextContrast,
    );

    final textField = TextField(
      controller: _controller,
      focusNode: _focusNode,
      // The focus a field at rest hands over keeps the selection it hands over with, as a native
      // single-line Text keeps its selection when it takes focus.
      selectAllOnFocus: handingOver || _focusLentToMenu ? false : null,
      enabled: enabled,
      obscureText: isObscured,
      obscuringCharacter: echoChar ?? _defaultObscuringCharacter,
      enableSuggestions: !isObscured,
      autocorrect: !isObscured,
      readOnly: _isReadOnly,
      maxLines: singleLine ? 1 : null,
      expands: shouldExpand,
      textAlignVertical:
          singleLine ? TextAlignVertical.center : TextAlignVertical.top,
      textAlign: textAlign,
      style: textStyle,
      decoration: decoration,
      keyboardType: isMultiLine ? TextInputType.multiline : TextInputType.text,
      // Return with Flutter's default `done` restarts the input connection, and on the web the
      // restarted one has no input element until the field lays out again: typing after it is lost.
      textInputAction: isMultiLine ? null : TextInputAction.unspecified,
      maxLength: state.textLimit,
      // SWT rejects the keystroke at the limit; Flutter's default on web lets over-limit
      // text stand until composition ends, which briefly renders it.
      maxLengthEnforcement: MaxLengthEnforcement.enforced,
      inputFormatters: [TextInputFormatter.withFunction(_gateEdit)],
      onChanged: _handleTextChanged,
      // Flutter's default here also unfocuses the field. An SWT Text keeps the focus on Return:
      // Java goes on routing keys to it, so the next keystroke typed has to land in it too.
      onEditingComplete: _controller.clearComposing,
      onSubmitted: _handleSubmitted,
      onTapOutside: (_) {},
      cursorColor: cursorColor,
      // No Flutter toolbar on a right-click: SWT's edit field opens the platform's own menu, which an
      // application Menu replaces and MenuDetect can cancel, and Flutter's toolbar follows neither.
      contextMenuBuilder: null,
    );
    return DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      clicks: _clicks,
      // The MouseDoubleClick itself is the press's (ControlImpl); the word it selects reaches Java
      // as a selection report, through _updateCaretPosition.
      onWordSelected: _wordSelected,
      onSecondaryPress: () => keepCaretOnSecondaryPress(
          _controller.selection, (caret) => _controller.selection = caret, _controller.text.length),
      child: textField,
    );
  }

  /// The field at rest: the decoration a TextField puts around its text, around the text drawn the
  /// way its EditableText draws it unfocused.
  Widget _buildAtRest(
    BuildContext context,
    TextThemeExtension widgetTheme,
    bool enabled,
    TextAlign textAlign,
  ) {
    final textStyle = getTextFieldTextStyle(context, state, widgetTheme);
    final decorationTheme = InputDecorationTheme.of(context);
    final own = _decoration(context, widgetTheme, false, true);
    // What TextField makes of its decoration and of its style.
    final decoration = own.applyDefaults(decorationTheme).copyWith(
          enabled: enabled,
          hintMaxLines: own.hintMaxLines ?? decorationTheme.hintMaxLines ?? 1,
        );
    final inputStyle = Theme.of(context).textTheme.bodyLarge!;
    final style = inputStyle
        .merge(TextStyle(color: enabled ? inputStyle.color : inputStyle.color?.withOpacity(0.38)))
        .merge(textStyle);
    final editable = enabled && (state.editable ?? true);
    // A text field to assistive technology, as the TextField is: tapping or focusing it hands over.
    return Semantics(
      container: true,
      textField: true,
      enabled: enabled,
      readOnly: !editable,
      inputType: SemanticsInputType.text,
      maxValueLength: state.textLimit,
      onTap: editable ? _handOver : null,
      child: Focus(
        focusNode: _restFocus,
        // On the press, not the tap: a press that arrives without a hover first brings one, which
        // mounts the TextField before the release and would take the tap with it.
        child: Listener(
          behavior: HitTestBehavior.opaque,
          onPointerDown: _handOverOnPress,
          onPointerMove: _dragFromRest,
          onPointerUp: _releaseFromRest,
          onPointerCancel: _releaseFromRest,
          child: ValueListenableBuilder<bool>(
            valueListenable: _isEmpty,
            builder: (context, isEmpty, text) => InputDecorator(
              decoration: decoration,
              baseStyle: textStyle,
              textAlign: textAlign,
              textAlignVertical: TextAlignVertical.center,
              isEmpty: isEmpty,
              child: text,
            ),
            child: RepaintBoundary(
              child: ValueListenableBuilder<TextEditingValue>(
                valueListenable: _controller,
                builder: (context, value, _) => Semantics(
                  value: value.text,
                  currentValueLength: value.text.length,
                  child: _TextAtRest(key: _atRestText, text: value.text, style: style, textAlign: textAlign),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTextFieldWrapper(
    Widget textField,
    TextThemeExtension widgetTheme,
    bool isMultiLine,
    bool hasValidBounds,
  ) {
    final constraints = getConstraintsFromBounds(state.bounds);

    // The wrapper's type must not depend on whether the bounds have arrived: a cell editor is
    // created, focused, and only then placed, and swapping the wrapper when they land remounts the
    // field, closing its input connection and dropping the keyboard. The constraints are tight, so
    // the nested IntrinsicWidth changes no layout.
    return ConstrainedBox(
      constraints: hasValidBounds && constraints != null
          ? constraints
          : const BoxConstraints(),
      child: IntrinsicWidth(child: textField),
    );
  }

  static String? _obscuringCharacter(int? echoCharacter) {
    if (echoCharacter == null || echoCharacter == 0) return null;
    final mask = String.fromCharCode(echoCharacter);
    return mask.length == 1 ? mask : _defaultObscuringCharacter;
  }

  static const String _defaultObscuringCharacter = '•';

  void _handleTextChanged(String value) {
    final isSingleLineExpand =
        hasBounds(state.bounds) && !hasStyle(state.style, SWT.MULTI);
    if (isSingleLineExpand && value.contains('\n')) {
      final clean = value.replaceAll('\n', '');
      _controller.value = _controller.value.copyWith(
        text: clean,
        selection: TextSelection.collapsed(offset: clean.length),
        composing: TextRange.empty,
      );
      _handleSubmitted(clean);
      return;
    }
    state.text = value;
    recordSentText(value);
    var e = VEvent()
      ..text = value
      ..start = _controller.selection.baseOffset;
    widget.sendModifyModify(state, e);
    KeyEditOrder.instance.editApplied();
  }

  bool _takesTyping() => (state.editable ?? true) && !hasStyle(state.style, SWT.READ_ONLY);

  void _handleSubmitted(String value) {
    // Shift+Return is only a key in a single-line Text (Cocoa, GTK and the desktop embedders do not
    // submit on it); the web's input element submits on any Enter.
    if (HardwareKeyboard.instance.isShiftPressed) return;
    widget.sendSelectionDefaultSelection(state, null);
  }

  void _handleFocusChange() {
    if (_focusNode.hasFocus) {
      _everFocused = true;
      if (handingOver) {
        handedOver();
        // Builds the TextField without what only the handover needed.
        setState(() {});
      }
      // Record the pre-edit text so a later stale echo of it is ignored rather than applied over
      // in-progress typing (no keystroke records this value, so the echo guard can't recognise it).
      seedTextEchoBaseline(_currentText);
      _undo.focused(this);
      widget.sendFocusFocusIn(state, null);
      // After the TextField has read selectAllOnFocus for this focus.
      if (_focusLentToMenu) WidgetsBinding.instance.addPostFrameCallback((_) => _endMenuLoan());
    } else {
      // Java re-arms per focus (DisplayBridge.setFocus).
      _modifyGate.armed = false;
      _inFlight = null;
      _held.clear();
      clearSentTextEchoes();
      widget.sendFocusFocusOut(state, null);
      if (OpenMenus.any || OpenMenus.opening) _lendFocusToMenu();
    }
  }

  /// True while a menu holds the focus this field had: a native Text keeps the focus under its
  /// menu, so getting it back when the menu closes selects nothing.
  bool _focusLentToMenu = false;

  void _lendFocusToMenu() {
    if (_focusLentToMenu) return;
    setState(() => _focusLentToMenu = true);
    FocusManager.instance.addListener(_followFocusAfterMenu);
  }

  /// The focus went on from the menu to another control: the next focus this field gets is its own.
  void _followFocusAfterMenu() {
    final focus = FocusManager.instance.primaryFocus;
    if (focus == _focusNode || focus?.context?.findAncestorStateOfType<MenuImpl>() != null) return;
    _endMenuLoan();
  }

  void _endMenuLoan() {
    FocusManager.instance.removeListener(_followFocusAfterMenu);
    if (mounted && _focusLentToMenu) setState(() => _focusLentToMenu = false);
  }

  @override
  int? get reportedCaret => state.caretPosition;

  @override
  set reportedCaret(int? caret) => state.caretPosition = caret;

  @override
  void sendCaretReport(VEvent event) => widget.sendVerifyVerify(state, event);

  /// The caret is the selection's moving end, in whichever controller holds the text.
  void _updateCaretPosition() => reportSelection(_usesLines ? _lineController.selection : _controller.selection);

  @override
  void dispose() {
    _modifyGate.detach();
    _controller.removeListener(_updateCaretPosition);
    _controller.removeListener(_trackEmptiness);
    _controller.dispose();
    _lines?.dispose();
    _isEmpty.dispose();
    _focusNode.removeListener(_handleFocusChange);
    FocusManager.instance.removeListener(_followFocusAfterMenu);
    KeyEditOrder.instance.unregisterEditor(_focusNode);
    _focusNode.dispose();
    _restFocus.removeListener(_handleRestFocus);
    _restFocus.dispose();
    super.dispose();
  }
}

/// An edit placed by the selection it was made at, so that it can be made on another text: what it
/// removed around the selection, what it inserted there, and where it left the selection.
class _EditAtSelection {
  _EditAtSelection._(this.removedBefore, this.removedAfter, this.inserted, this.base, this.extent);

  factory _EditAtSelection.between(String from, TextSelection fromSelection, String to, TextSelection toSelection) {
    final (at, deleted, added) = TextImpl._editBetween(from, fromSelection, to, toSelection);
    final selection = fromSelection.isValid ? fromSelection : TextSelection.collapsed(offset: at + deleted.length);
    final after = toSelection.isValid ? toSelection : TextSelection.collapsed(offset: at + added.length);
    return _EditAtSelection._(selection.start - at, at + deleted.length - selection.end, added,
        after.baseOffset - at, after.extentOffset - at);
  }

  final int removedBefore;
  final int removedAfter;
  final String inserted;

  /// The selection after the edit, from where it inserted.
  final int base;
  final int extent;

  (String, TextSelection) on(String text, TextSelection selection) {
    final at = selection.isValid ? selection : TextSelection.collapsed(offset: text.length);
    final start = (at.start - removedBefore).clamp(0, text.length);
    final end = (at.end + removedAfter).clamp(start, text.length);
    final edited = text.replaceRange(start, end, inserted);
    return (
      edited,
      TextSelection(
          baseOffset: (start + base).clamp(0, edited.length), extentOffset: (start + extent).clamp(0, edited.length)),
    );
  }
}

/// A Modify sent to Java and not yet answered.
class _ProposedEdit {
  _ProposedEdit(this.text, this.start, this.apply, this.record);

  final String text;

  /// The caret offset sent with [text].
  final int start;

  /// Shows [text] in the field.
  final void Function() apply;

  /// Takes the undo step, once shown.
  final VoidCallback? record;
}

/// One line of text laid out and painted the way an unfocused, unscrolled single-line EditableText
/// lays out and paints it.
class _TextAtRest extends LeafRenderObjectWidget {
  const _TextAtRest({super.key, required this.text, required this.style, required this.textAlign});

  final String text;
  final TextStyle style;
  final TextAlign textAlign;

  @override
  _RenderTextAtRest createRenderObject(BuildContext context) => _RenderTextAtRest()..configure(this, context);

  @override
  void updateRenderObject(BuildContext context, _RenderTextAtRest renderObject) =>
      renderObject.configure(this, context);
}

class _RenderTextAtRest extends RenderBox {
  /// What RenderEditable keeps free for the caret: its gap and a TextField's cursor width.
  static const _caretMargin = 1.0 + 2.0;

  final TextPainter _painter = TextPainter(maxLines: 1);
  double? _laidOutAt;

  /// The scaler last given, which [TextPainter.textScaler] does not hand back as it was given.
  TextScaler? _textScaler;

  void configure(_TextAtRest widget, BuildContext context) {
    final text = TextSpan(text: widget.text, style: widget.style);
    final strut = StrutStyle.fromTextStyle(widget.style, forceStrutHeight: true)
        .merge(StrutStyle(height: MediaQuery.maybeLineHeightScaleFactorOverrideOf(context)));
    final textDirection = Directionality.of(context);
    final textScaler = MediaQuery.textScalerOf(context);
    final textHeightBehavior = DefaultTextHeightBehavior.maybeOf(context);
    if (_painter.text == text &&
        _painter.textAlign == widget.textAlign &&
        _painter.strutStyle == strut &&
        _painter.textDirection == textDirection &&
        _textScaler == textScaler &&
        _painter.textHeightBehavior == textHeightBehavior) {
      return;
    }
    final comparison = _painter.text?.compareTo(text) ?? RenderComparison.layout;
    final textAlone = _painter.text is TextSpan &&
        (_painter.text! as TextSpan).style == widget.style &&
        _painter.textAlign == widget.textAlign &&
        _painter.strutStyle == strut &&
        _painter.textDirection == textDirection &&
        _textScaler == textScaler &&
        _painter.textHeightBehavior == textHeightBehavior;
    _painter
      ..text = text
      ..textAlign = widget.textAlign
      ..strutStyle = strut
      ..textDirection = textDirection
      ..textScaler = textScaler
      ..textHeightBehavior = textHeightBehavior;
    _textScaler = textScaler;
    if (comparison == RenderComparison.paint && _laidOutAt != null) {
      markNeedsPaint();
    } else if (textAlone && hasSize) {
      // This box's size is the width it is given and the height of its strut, and its baseline is
      // the strut's: a new text alone changes none of them, so it is laid out where it is painted,
      // without a layout that would lay out the field around it again.
      _laidOutAt = null;
      markNeedsPaint();
    } else {
      _laidOutAt = null;
      markNeedsLayout();
    }
  }

  void _layout(double maxWidth) {
    final minWidth = (maxWidth - _caretMargin).clamp(0.0, double.infinity);
    if (_laidOutAt == minWidth) return;
    _painter.layout(minWidth: minWidth, maxWidth: double.infinity);
    _laidOutAt = minWidth;
  }

  @override
  void performLayout() {
    _layout(constraints.maxWidth);
    size = Size(constraints.maxWidth, constraints.constrainHeight(_painter.height));
  }

  @override
  Size computeDryLayout(BoxConstraints constraints) {
    _layout(constraints.maxWidth);
    final size = Size(constraints.maxWidth, constraints.constrainHeight(_painter.height));
    if (hasSize) _layout(this.size.width);
    return size;
  }

  @override
  double? computeDryBaseline(BoxConstraints constraints, TextBaseline baseline) {
    _layout(constraints.maxWidth);
    final distance = _painter.computeDistanceToActualBaseline(baseline);
    if (hasSize) _layout(size.width);
    return distance;
  }

  @override
  double computeDistanceToActualBaseline(TextBaseline baseline) {
    _layout(size.width);
    return _painter.computeDistanceToActualBaseline(baseline);
  }

  @override
  double computeMinIntrinsicWidth(double height) => computeMaxIntrinsicWidth(height);

  @override
  double computeMaxIntrinsicWidth(double height) {
    _painter.layout();
    final width = _painter.maxIntrinsicWidth + _caretMargin;
    _laidOutAt = null;
    return width;
  }

  @override
  double computeMinIntrinsicHeight(double width) => computeMaxIntrinsicHeight(width);

  @override
  double computeMaxIntrinsicHeight(double width) {
    _layout(width);
    return _painter.height;
  }

  @override
  void attach(PipelineOwner owner) {
    super.attach(owner);
    PaintingBinding.instance.systemFonts.addListener(_fontsChanged);
  }

  @override
  void detach() {
    PaintingBinding.instance.systemFonts.removeListener(_fontsChanged);
    super.detach();
  }

  /// A font that finishes loading changes how the text lays out, as it does for an EditableText.
  void _fontsChanged() {
    _painter.markNeedsLayout();
    _laidOutAt = null;
    markNeedsLayout();
  }

  @override
  bool hitTestSelf(Offset position) => true;

  /// The offset in the text the caret goes to for a press at [global].
  int offsetAt(Offset global) {
    _layout(size.width);
    return _painter.getPositionForOffset(globalToLocal(global)).offset;
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    _layout(size.width);
    if (_painter.width + _caretMargin > size.width) {
      context.pushClipRect(needsCompositing, offset, Offset.zero & size,
          (context, offset) => _painter.paint(context.canvas, offset));
    } else {
      _painter.paint(context.canvas, offset);
    }
  }

  @override
  void dispose() {
    _painter.dispose();
    super.dispose();
  }
}
