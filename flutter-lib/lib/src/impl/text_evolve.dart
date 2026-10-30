import 'dart:async';
import 'dart:convert';
import 'dart:ui' show SemanticsInputType;

import 'package:flutter/gestures.dart' show kSecondaryMouseButton;
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
import 'key_mapping.dart';
import 'utils/key_edit_order.dart';
import 'utils/at_rest.dart';
import 'utils/text_utils.dart';
import 'utils/veto_gate.dart';
import 'utils/widget_utils.dart';
import 'utils/client_caret_report.dart';
import 'utils/pending_text_echoes.dart';
import 'utils/hover_arbiter.dart';
import 'control_evolve.dart' show ControlImpl;

class TextImpl<T extends TextSwt, V extends VText>
    extends ScrollableImpl<T, V> with PendingTextEchoes, ClientCaretReport, AtRestHandover {
  late TextEditingController _controller;

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
  FocusNode get swtFocusNode => _live ? _focusNode : _restFocus;


  // A VerifyListener runs before the character is displayed, so edits are held un-rendered.
  final VetoGate _modifyGate = VetoGate('modify');

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
    _modifyGate.attach(state.swt, state.id);
    _modifyGate.onUnmatched = _revertOptimisticEdit;
    // Initialize caret position
    WidgetsBinding.instance.addPostFrameCallback((_) => fromJava(_updateCaretPosition));
  }

  /// Holds an edit while the field is vetoable: the proposal goes to Java as the Modify, the
  /// controller keeps the old value until the verdict. Selection-only changes and the
  /// single-line newline path (submit semantics, see [_handleTextChanged]) stay optimistic.
  TextEditingValue _gateEdit(TextEditingValue oldValue, TextEditingValue newValue) {
    if (!_modifyGate.armed) return newValue;
    if (newValue.text == oldValue.text) return newValue;
    final isSingleLineExpand =
        hasBounds(state.bounds) && !hasStyle(state.style, SWT.MULTI);
    if (isSingleLineExpand && newValue.text.contains('\n')) return newValue;
    _modifyGate.propose((doit) {
      if (!doit) {
        // The held edit was never rendered, so it just drops; stop suppressing Java's echoes.
        clearSentTextEchoes();
        return;
      }
      if (!mounted) return;
      _controller.value = newValue;
      state.text = newValue.text;
    });
    recordSentText(newValue.text);
    widget.sendModifyModify(
      state,
      VEvent()
        ..text = newValue.text
        ..start = newValue.selection.baseOffset,
    );
    KeyEditOrder.instance.editApplied();
    return oldValue;
  }

  /// Rejection for an edit that was never held (the embedded backend never arms the gate), so it is
  /// already on screen. Clearing the echo guard lets Java's corrective push through.
  void _revertOptimisticEdit(bool doit) {
    if (doit) return;
    clearSentTextEchoes();
    if (!mounted) return;
    final authoritative = state.text ?? '';
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
  static const _controllerProperties = {WireKey.k_Text_text, WireKey.k_Text_selection};

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
    String newText = state.text ?? "";
    // A Java value push may be the echo of a Modify this field sent moments ago. If the user
    // has typed further in the meantime, resetting the controller to the echoed (older) text
    // silently drops those keystrokes (the mid-word lost character on slow machines). See
    // PendingTextEchoes: our own in-flight echoes are ignored, external changes fall through.
    if (isStaleTextEcho(newText, _controller.text)) return;
    bool textChanged = _controller.text != newText;

    if (textChanged) {
      final isReadOnly =
          !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY);
      final cursorOffset = isReadOnly ? 0 : newText.length;

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
    if (sel != null && sel.x != sel.y && sel.y <= _controller.text.length) {
      final desired = TextSelection(baseOffset: sel.x, extentOffset: sel.y);
      if (_controller.selection != desired) {
        _controller.selection = desired;
      }
    }
  }

  void _trackEmptiness() => _isEmpty.value = _controller.text.isEmpty;

  void _handleRestFocus() {
    if (_restFocus.hasPrimaryFocus && !_live) _handOver();
  }

  /// Mounts the TextField and gives it the focus the field at rest received, with the caret at
  /// [tapped] when a press brought it, else with the selection Java holds.
  void _handOver([int? tapped]) => handOver(() {
        final end = _controller.text.length;
        if (tapped != null) {
          _controller.selection = TextSelection.collapsed(offset: tapped.clamp(0, end));
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
    final text = _atRestText.currentContext?.findRenderObject();
    final menu = ControlImpl.isMenuDetectTrigger(ControlImpl.swtButtonOf(event.buttons));
    _handOver(!menu && text is _RenderTextAtRest ? text.offsetAt(event.position) : null);
  }

  /// The text of the field at rest, which maps a press to the offset the caret goes to.
  final GlobalKey _atRestText = GlobalKey();

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TextThemeExtension>();
    if (widgetTheme == null) {
      return const SizedBox.shrink();
    }
    // Text builds its own tree and never routes through ControlImpl.wrap(), which is
    // otherwise the only place that honors state.visible == false.
    if (state.visible != null && !state.visible!) {
      return const SizedBox.shrink();
    }

    final textAlign = getTextAlignFromStyle(state.style, TextAlign.left);
    final isMultiLine = hasStyle(state.style, SWT.MULTI);
    final enabled = state.enabled ?? false;
    final editable = state.editable ?? true;
    final hasValidBounds = hasBounds(state.bounds);

    final textField = _live
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
      textField,
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

    final isReadOnly =
        !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY);
    final hasBorderStyle = hasStyle(state.style, SWT.BORDER);
    final parentBg = ParentBackgroundScope.backgroundOf(context);
    if (parentBg != null && isReadOnly && !hasBorderStyle) {
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
    final isReadOnly =
        !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY);
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
      selectAllOnFocus: handingOver ? false : null,
      enabled: enabled,
      obscureText: isObscured,
      obscuringCharacter: echoChar ?? _defaultObscuringCharacter,
      enableSuggestions: !isObscured,
      autocorrect: !isObscured,
      readOnly: isReadOnly,
      maxLines: singleLine ? 1 : null,
      expands: shouldExpand,
      textAlignVertical:
          singleLine ? TextAlignVertical.center : TextAlignVertical.top,
      textAlign: textAlign,
      style: textStyle,
      decoration: decoration,
      keyboardType: isMultiLine ? TextInputType.multiline : TextInputType.text,
      maxLength: state.textLimit,
      // SWT rejects the keystroke at the limit; Flutter's default on web lets over-limit
      // text stand until composition ends, which briefly renders it.
      maxLengthEnforcement: MaxLengthEnforcement.enforced,
      inputFormatters: [TextInputFormatter.withFunction(_gateEdit)],
      onChanged: _handleTextChanged,
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
      // The MouseDoubleClick itself is the press's (ControlImpl); the word it selects reaches Java
      // as a selection report, through _updateCaretPosition.
      onWordSelected: (start, end) {
        state.selection = VPoint()
          ..x = start
          ..y = end;
      },
      child: Listener(
        onPointerDown: (e) {
          if (!ControlImpl.isMenuDetectTrigger(ControlImpl.swtButtonOf(e.buttons))) return;
          // A field that never held a caret has Java's, which the focus this press brings would
          // otherwise move to the end.
          final selection = _controller.selection;
          _selectionBeforeSecondary = selection.isValid
              ? selection
              : TextSelection.collapsed(
                  offset: (state.caretPosition ?? 0).clamp(0, _controller.text.length));
        },
        onPointerUp: (e) {
          final before = _selectionBeforeSecondary;
          _selectionBeforeSecondary = null;
          if (before == null) return;
          // The field's own secondary-tap recognizer wins the gesture arena over the one
          // ControlImpl.applyMenu() adds, so the release opens the application Menu here.
          if (state.menu != null) openContextMenu(e.position);
          // Flutter's field places the caret on a secondary tap, which the gesture delivers after
          // this release; a native Text leaves it where it was, as on Win32 and GTK.
          scheduleMicrotask(() {
            if (mounted && _controller.selection != before) _controller.selection = before;
          });
        },
        child: textField,
      ),
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

  /// The selection when a secondary press began, put back once its click is done.
  TextSelection? _selectionBeforeSecondary;

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
      seedTextEchoBaseline(_controller.text);
      widget.sendFocusFocusIn(state, null);
    } else {
      // Java re-arms per focus (DisplayBridge.setFocus).
      _modifyGate.armed = false;
      clearSentTextEchoes();
      widget.sendFocusFocusOut(state, null);
    }
  }

  @override
  int? get reportedCaret => state.caretPosition;

  @override
  set reportedCaret(int? caret) => state.caretPosition = caret;

  @override
  void sendCaretReport(VEvent event) => widget.sendVerifyVerify(state, event);

  void _updateCaretPosition() => reportCaret(_controller);

  @override
  void dispose() {
    _modifyGate.detach();
    _controller.removeListener(_updateCaretPosition);
    _controller.removeListener(_trackEmptiness);
    _controller.dispose();
    _isEmpty.dispose();
    _focusNode.removeListener(_handleFocusChange);
    KeyEditOrder.instance.unregisterEditor(_focusNode);
    _focusNode.dispose();
    _restFocus.removeListener(_handleRestFocus);
    _restFocus.dispose();
    super.dispose();
  }
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
