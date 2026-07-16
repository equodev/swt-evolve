import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../theme/theme_extensions/text_theme_extension.dart';
import '../theme/theme_settings/text_theme_settings.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';

class TextImpl<T extends TextSwt, V extends VText>
    extends ScrollableImpl<T, V> {
  late TextEditingController _controller;

  /// Texts sent to Java (Modify) whose value-push echo hasn't come back yet — see extraSetState.
  final List<String> _pendingEchoes = [];
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
    // silently drops those keystrokes (the mid-word lost character on slow machines). Locally
    // sent texts are remembered and their echoes ignored.
    final int echoIndex = _pendingEchoes.indexOf(newText);
    if (echoIndex >= 0) {
      _pendingEchoes.removeRange(0, echoIndex + 1);
      if (_controller.text != newText) {
        return;
      }
    }
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

    return tagSemantics(
      wrapDnd(
        _buildTextFieldWrapper(
          textField,
          widgetTheme,
          isMultiLine,
          hasValidBounds,
        ),
      ),
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

    final parentBg = ParentBackgroundScope.backgroundOf(context);
    if (parentBg != null) {
      decoration = decoration.copyWith(filled: true, fillColor: parentBg);
    }

    final isPassword = hasStyle(state.style, SWT.PASSWORD);
    final singleLine = !isMultiLine || isPassword;
    final shouldExpand = hasValidBounds && !singleLine;

    if (hasValidBounds) {
      final hPadding = widgetTheme.contentPadding.left;
      decoration = decoration.copyWith(
        contentPadding: EdgeInsets.symmetric(horizontal: hPadding),
      );
      if (singleLine) {
        decoration = decoration.copyWith(
          isDense: false,
          constraints: BoxConstraints.tightFor(
            height: state.bounds!.height.toDouble(),
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
      obscureText: isPassword,
      readOnly:
          !(state.editable ?? true) || hasStyle(state.style, SWT.READ_ONLY),
      maxLines: singleLine ? 1 : null,
      expands: shouldExpand,
      textAlignVertical: TextAlignVertical.center,
      textAlign: textAlign,
      style: textStyle,
      decoration: decoration,
      keyboardType: isMultiLine ? TextInputType.multiline : TextInputType.text,
      maxLength: state.textLimit,
      onChanged: _handleTextChanged,
      onSubmitted: _handleSubmitted,
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
    _pendingEchoes.add(value);
    var e = VEvent()
      ..text = value
      ..start = _controller.selection.baseOffset;
    widget.sendModifyModify(state, e);
  }

  void _handleSubmitted(String value) {
    widget.sendSelectionDefaultSelection(state, null);
  }

  void _handleFocusChange() {
    print("Text ${state.id} focus changed: hasFocus=${_focusNode!.hasFocus}");
    if (_focusNode!.hasFocus) {
      widget.sendFocusFocusIn(state, null);
    } else {
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