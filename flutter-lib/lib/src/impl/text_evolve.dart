import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../theme/text_theme_extension.dart';
import '../theme/text_theme_settings.dart';

class TextImpl<T extends TextSwt, V extends VText>
    extends ScrollableImpl<T, V> {
  late TextEditingController _controller;
  FocusNode? _focusNode;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: state.text);
    _focusNode = FocusNode();
    _focusNode!.addListener(_handleFocusChange);
  }

  @override
  void extraSetState() {
    String newText = state.text ?? "";
    // Only update controller if text actually changed from external source
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
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final widgetTheme = Theme.of(context).extension<TextThemeExtension>();

    final textAlign = getTextAlign(state.style);
    final isMultiLine = (state.style & SWT.MULTI) != 0;
    final enabled = state.editable ?? true;
    final textStyle = getTextStyle(context, state, widgetTheme, colorScheme, textTheme);
    final decoration = getInputDecoration(
      context,
      state,
      widgetTheme,
      colorScheme,
      _controller,
      () {
        _controller.clear();
        var e = VEvent()..detail = SWT.ICON_CANCEL;
        widget.sendSelectionDefaultSelection(state, e);
      },
    );
    final height = getTextFieldHeight(state, widgetTheme, isMultiLine);
    final cursorColor = getCursorColor(state, widgetTheme, colorScheme, textTheme);

    final hasBounds = state.bounds != null && state.bounds!.width > 0 && state.bounds!.height > 0;

    final shouldExpand = hasBounds && isMultiLine;

    Widget textField = TextField(
      controller: _controller,
      focusNode: _focusNode,
      enabled: enabled,
      obscureText: (state.style & SWT.PASSWORD) != 0,
      readOnly: (state.style & SWT.READ_ONLY) != 0,
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

    if (hasBounds) {
      return ConstrainedBox(
        constraints: BoxConstraints(
          minWidth: state.bounds!.width.toDouble(),
          maxWidth: state.bounds!.width.toDouble(),
          minHeight: state.bounds!.height.toDouble(),
          maxHeight: state.bounds!.height.toDouble(),
        ),
        child: SizedBox(
          width: state.bounds!.width.toDouble(),
          height: state.bounds!.height.toDouble(),
          child: textField,
        ),
      );
    }
    
    if (isMultiLine) {
      return Container(
        height: height,
        child: textField,
      );
    }
    
    return Align(
      alignment: Alignment.center,
      child: IntrinsicWidth(
        child: Container(
          height: height,
          child: textField,
        ),
      ),
    );
  }

  void _handleTextChanged(String value) {
    print("Text ${state.id} text changed to: '$value'");
    // Don't call setState here to avoid rebuilding and losing cursor position
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

  @override
  void dispose() {
    _controller.dispose();
    _focusNode?.removeListener(_handleFocusChange);
    _focusNode?.dispose();
    super.dispose();
  }
}

