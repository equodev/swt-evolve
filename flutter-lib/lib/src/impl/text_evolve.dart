import 'package:flutter/material.dart';
import '../gen/event.dart';
import '../gen/swt.dart';
import '../gen/text.dart';
import '../gen/widget.dart';
import '../impl/scrollable_evolve.dart';
import '../styles.dart';
import 'color_utils.dart';

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
    TextAlign textAlign = _getTextAlignment();
    bool isMultiLine = state.style.has(SWT.MULTI);

    Alignment alignment;
    if (textAlign == TextAlign.center) {
      alignment = Alignment.center;
    } else if (textAlign == TextAlign.right) {
      alignment = Alignment.centerRight;
    } else {
      alignment = Alignment.centerLeft;
    }

    Widget textField = _StyledTextField(
      controller: _controller,
      focusNode: _focusNode,
      enabled: state.editable ?? true,
      obscureText: state.style.has(SWT.PASSWORD),
      readOnly: state.style.has(SWT.READ_ONLY),
      maxLines: isMultiLine ? null : 1,
      textAlign: textAlign,
      style: _getTextStyle(),
      decoration: _getInputDecoration(),
      keyboardType: isMultiLine ? TextInputType.multiline : TextInputType.text,
      maxLength: state.textLimit,
      onChanged: _handleTextChanged,
      onSubmitted: _handleSubmitted,
      height: isMultiLine ? state.bounds?.height.toDouble() : null,
    );
    if (isMultiLine) {
      return textField;
    }
    return Align(
      alignment: alignment,
      child: IntrinsicWidth(
        child: textField,
      ),
    );
  }

  TextAlign _getTextAlignment() {
    if (state.style.has(SWT.CENTER)) return TextAlign.center;
    if (state.style.has(SWT.RIGHT)) return TextAlign.right;
    return TextAlign.left;
  }

  TextStyle _getTextStyle() {
    return TextStyle(
      color: getForeground(),
      fontSize: 12,
    );
  }

  InputDecoration _getInputDecoration() {
    final iconColor = getIconColor();
    final hintColor = getHintColor();
    final bgColor = getBackground();

    return InputDecoration(
      hintText: state.message,
      hintStyle: TextStyle(color: hintColor),
      isDense: true,
      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      border: _getBorder(),
      enabledBorder: _getBorder(),
      focusedBorder: _getBorder(isFocused: true),
      fillColor: bgColor,
      filled: true,
      counterText: '',
      prefixIcon: state.style.has(SWT.SEARCH) || state.style.has(SWT.ICON_SEARCH)
          ? Icon(Icons.search, size: 16, color: iconColor)
          : null,
      prefixIconConstraints: const BoxConstraints(
        minHeight: 32,
        minWidth: 32,
      ),
      suffixIcon: (state.style.has(SWT.SEARCH) && state.text != null && state.text!.isNotEmpty)
          || state.style.has(SWT.ICON_CANCEL)
          ? IconButton(
        icon: Icon(Icons.clear, size: 16, color: iconColor),
        padding: EdgeInsets.zero,
        constraints: const BoxConstraints(
          minHeight: 32,
          minWidth: 32,
        ),
        onPressed: () {
          _controller.clear();
          var e = VEvent()..detail = SWT.ICON_CANCEL;
          widget.sendSelectionDefaultSelection(state, e);
        },
      )
          : null,
      suffixIconConstraints: const BoxConstraints(
        minHeight: 32,
        minWidth: 32,
      ),
    );
  }

  InputBorder _getBorder({bool isFocused = false}) {
    return OutlineInputBorder(
      borderRadius: BorderRadius.circular(4),
      borderSide: BorderSide(
        color: isFocused ? getBorderColorFocused() : getBorderColor(),
        width: 1,
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

class _StyledTextField extends StatelessWidget {
  final TextEditingController controller;
  final FocusNode? focusNode;
  final bool enabled;
  final bool obscureText;
  final bool readOnly;
  final int? maxLines;
  final TextAlign textAlign;
  final TextStyle style;
  final InputDecoration decoration;
  final TextInputType keyboardType;
  final int? maxLength;
  final ValueChanged<String>? onChanged;
  final ValueChanged<String>? onSubmitted;
  final double? height;

  const _StyledTextField({
    Key? key,
    required this.controller,
    this.focusNode,
    this.enabled = true,
    this.obscureText = false,
    this.readOnly = false,
    this.maxLines,
    this.textAlign = TextAlign.left,
    required this.style,
    required this.decoration,
    this.keyboardType = TextInputType.text,
    this.maxLength,
    this.onChanged,
    this.onSubmitted,
    this.height,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: height ?? 32,
      child: TextField(
        controller: controller,
        focusNode: focusNode,
        enabled: enabled,
        obscureText: obscureText,
        readOnly: readOnly,
        maxLines: maxLines,
        textAlign: textAlign,
        textAlignVertical: TextAlignVertical.center,
        style: style,
        decoration: decoration,
        keyboardType: keyboardType,
        maxLength: maxLength,
        onChanged: onChanged,
        onSubmitted: onSubmitted,
        cursorColor: getForeground(),
      ),
    );
  }
}
