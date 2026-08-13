import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../gen/event.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../theme/theme_extensions/text_theme_extension.dart';
import '../theme/theme_settings/text_theme_settings.dart';
import 'key_forwarding.dart';
import 'key_mapping.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'utils/pending_text_echoes.dart';

class TextImpl<T extends TextSwt, V extends VText>
    extends ScrollableImpl<T, V> with PendingTextEchoes {
  late TextEditingController _controller;

  FocusNode? _focusNode;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _controller.addListener(_updateCaretPosition);
    _focusNode = FocusNode();
    _focusNode!.addListener(_handleFocusChange);
    // Initialize caret position
    WidgetsBinding.instance.addPostFrameCallback((_) => _updateCaretPosition());
  }

  @override
  void extraSetState() {
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
        final newSelection = TextSelection(
          baseOffset: state.selection!.x,
          extentOffset: state.selection!.y,
        );
        _controller.selection = newSelection;
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

  @override
  Widget build(BuildContext context) {
    final widgetTheme = Theme.of(context).extension<TextThemeExtension>();
    if (widgetTheme == null) {
      return const SizedBox.shrink();
    }

    final textAlign = getTextAlignFromStyle(state.style, TextAlign.left);
    final isMultiLine = hasStyle(state.style, SWT.MULTI);
    final enabled = state.enabled ?? false;
    final editable = state.editable ?? true;
    final hasValidBounds = hasBounds(state.bounds);

    final textField = _buildTextField(
      context,
      widgetTheme,
      enabled,
      textAlign,
      isMultiLine,
      hasValidBounds,
    );

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
        displayLevelKeyForwardingActive
            ? fieldWrapper
            : _forwardShortcutKeys(fieldWrapper),
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

  Widget _buildTextField(
    BuildContext context,
    TextThemeExtension widgetTheme,
    bool enabled,
    TextAlign textAlign,
    bool isMultiLine,
    bool hasValidBounds,
  ) {
    final textStyle = getTextFieldTextStyle(context, state, widgetTheme);
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
    final echoChar = (isPassword || isMultiLine)
        ? null
        : _obscuringCharacter(state.echoCharacter);
    final isObscured = isPassword || echoChar != null;
    final singleLine = !isMultiLine || isPassword;
    final shouldExpand = hasValidBounds && !singleLine;

    if (hasValidBounds) {
      if (singleLine) {
        final isSearch = hasStyle(state.style, SWT.SEARCH);
        InputBorder noGap(InputBorder? b) =>
            (!isSearch && b is OutlineInputBorder) ? b.copyWith(gapPadding: 0.0) : b!;
        decoration = decoration.copyWith(
          isDense: false,
          contentPadding: const EdgeInsets.symmetric(horizontal: 3.0),
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
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 3.0,
            vertical: 4.0,
          ),
        );
      }
    }

    final cursorColor = getForegroundColor(
      foreground: state.foreground,
      defaultColor: widgetTheme.textColor,
    );

    final textField = TextField(
      controller: _controller,
      focusNode: _focusNode,
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
      onChanged: _handleTextChanged,
      onSubmitted: _handleSubmitted,
      onTapOutside: (_) {},
      cursorColor: cursorColor,
    );
    return DoubleClickWordSelector(
      controller: _controller,
      focusNode: _focusNode,
      text: state.text ?? '',
      onWordSelected: (start, end) {
        state.selection = VPoint()
          ..x = start
          ..y = end;
        widget.sendMouseMouseDoubleClick(
          state,
          VEvent()
            ..button = 1
            ..count = 2
            ..start = start
            ..end = end,
        );
      },
      child: textField,
    );
  }

  Widget _buildTextFieldWrapper(
    Widget textField,
    TextThemeExtension widgetTheme,
    bool isMultiLine,
    bool hasValidBounds,
  ) {
    final constraints = getConstraintsFromBounds(state.bounds);

    if (hasValidBounds && constraints != null) {
      return ConstrainedBox(constraints: constraints, child: textField);
    }

    return IntrinsicWidth(child: textField);
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
  }

  void _handleSubmitted(String value) {
    widget.sendSelectionDefaultSelection(state, null);
  }

  void _handleFocusChange() {
    if (_focusNode!.hasFocus) {
      // Record the pre-edit text so a later stale echo of it is ignored rather than applied over
      // in-progress typing (no keystroke records this value, so the echo guard can't recognise it).
      seedTextEchoBaseline(_controller.text);
      widget.sendFocusFocusIn(state, null);
    } else {
      clearSentTextEchoes();
      widget.sendFocusFocusOut(state, null);
    }
  }

  void _updateCaretPosition() {
    if (!mounted) return;

    // Get the character index position of the cursor
    final cursorOffset = _controller.selection.baseOffset;
    if (cursorOffset < 0) return;

    // Only update if position actually changed
    if (state.caretPosition == null || state.caretPosition != cursorOffset) {
      state.caretPosition = cursorOffset;

      // Send the caret position to Java via Verify event
      var e = VEvent()
        ..start = cursorOffset
        ..end = cursorOffset
        ..text = "";
      widget.sendVerifyVerify(state, e);
    }
  }

  @override
  void dispose() {
    _controller.removeListener(_updateCaretPosition);
    _controller.dispose();
    _focusNode?.removeListener(_handleFocusChange);
    _focusNode?.dispose();
    super.dispose();
  }
}