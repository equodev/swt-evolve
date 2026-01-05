import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/point.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../theme/theme_extensions/text_theme_extension.dart';
import '../theme/theme_settings/text_theme_settings.dart';
import 'utils/widget_utils.dart';

class TextImpl<T extends TextSwt, V extends VText>
    extends ScrollableImpl<T, V> {
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
    if (_controller.text != newText) {
      _controller.text = newText;
      if (state.textLimit != null) {
        _controller.value = _controller.value.copyWith(
          text: _controller.text,
          selection: TextSelection.collapsed(offset: _controller.text.length),
          composing: TextRange.empty,
        );
      }
    }

    // Update controller selection if it changed from Java side
    if (state.selection != null) {
      final newSelection = TextSelection(
        baseOffset: state.selection!.x,
        extentOffset: state.selection!.y,
      );
      if (_controller.selection != newSelection) {
        _controller.selection = newSelection;
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
    final enabled = state.editable ?? true;
    final hasValidBounds = hasBounds(state.bounds);

    final textField = _buildTextField(context, widgetTheme, enabled, textAlign, isMultiLine, hasValidBounds);

    return _buildTextFieldWrapper(textField, widgetTheme, isMultiLine, hasValidBounds);
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
    final decoration = getInputDecoration(
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

    final cursorColor = getForegroundColor(
      foreground: state.foreground,
      defaultColor: widgetTheme.textColor,
    );

    final shouldExpand = hasValidBounds && isMultiLine;

    return TextField(
      controller: _controller,
      focusNode: _focusNode,
      enabled: enabled,
      obscureText: hasStyle(state.style, SWT.PASSWORD),
      readOnly: hasStyle(state.style, SWT.READ_ONLY),
      maxLines: isMultiLine ? (shouldExpand ? null : null) : 1,
      expands: shouldExpand,
      textAlign: textAlign,
      textAlignVertical: TextAlignVertical.top,
      style: textStyle,
      decoration: decoration,
      keyboardType: isMultiLine ? TextInputType.multiline : TextInputType.text,
      maxLength: state.textLimit,
      onChanged: _handleTextChanged,
      onSubmitted: _handleSubmitted,
      cursorColor: cursorColor,
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
      return ConstrainedBox(
        constraints: constraints,
        child: textField,
      );
    }
    
    if (isMultiLine) {
      final minHeight = widgetTheme.contentPadding.vertical + 
                       (widgetTheme.fontSize * widgetTheme.lineHeight);
      return Container(
        constraints: BoxConstraints(
          minHeight: minHeight,
        ),
        child: textField,
      );
    }
    
    final minHeight = widgetTheme.contentPadding.vertical + 
                     (widgetTheme.fontSize * widgetTheme.lineHeight);
    return IntrinsicWidth(
      child: Container(
        constraints: BoxConstraints(
          minHeight: minHeight,
        ),
        child: textField,
      ),
    );
  }

  void _handleTextChanged(String value) {
    print("Text ${state.id} text changed to: '$value'");
    state.text = value;
    var e = VEvent()..text = value;
    widget.sendModifyModify(state, e);
    widget.sendVerifyVerify(state, e);
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

    // Only update if position actually changed
    if (state.caretPosition == null || state.caretPosition != cursorOffset) {
      state.caretPosition = cursorOffset;

      // Send the caret position to Java via Verify event
      var e = VEvent()..start = cursorOffset;
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

