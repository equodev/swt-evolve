import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/scrollable_impl.dart';
import '../swt/text.dart';
import '../swt/swt.dart';
import 'widget_config.dart';
import 'package:swtflutter/src/styles.dart';
import 'styledtextfield.dart';

class TextImpl<T extends TextSwt<V>, V extends TextValue>
    extends ScrollableImpl<T, V> {
  late TextEditingController _controller;
  final bool useDarkTheme = getCurrentTheme();
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
    _controller.text = state.text ?? "";
    if (state.textLimit != null) {
      _controller.value = _controller.value.copyWith(
        text: _controller.text,
        selection: TextSelection.collapsed(offset: _controller.text.length),
        composing: TextRange.empty,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    TextAlign textAlign = _getTextAlignment();

    Alignment alignment;
    if (textAlign == TextAlign.center) {
      alignment = Alignment.center;
    } else if (textAlign == TextAlign.right) {
      alignment = Alignment.centerRight;
    } else {
      alignment = Alignment.centerLeft;
    }

    return Align(
      alignment: alignment,
      child: IntrinsicWidth(
        child: StyledTextField(
          controller: _controller,
          focusNode: _focusNode,
          enabled: state.editable ?? true,
          obscureText: state.style.has(SWT.PASSWORD),
          readOnly: state.style.has(SWT.READ_ONLY),
          maxLines: state.style.has(SWT.MULTI) ? null : 1,
          textAlign: textAlign,
          style: _getTextStyle(),
          decoration: _getInputDecoration(),
          keyboardType: state.style.has(SWT.MULTI)
              ? TextInputType.multiline
              : TextInputType.text,
          maxLength: state.textLimit,
          onChanged: _handleTextChanged,
          onSubmitted: _handleSubmitted,
          useDarkTheme: useDarkTheme,
        ),
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
      color: useDarkTheme ? Colors.white : Colors.black,
      fontSize: 12,
    );
  }

  InputDecoration _getInputDecoration() {
    return InputDecoration(
      hintText: state.message,
      isDense: true,
      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      border: _getBorder(),
      enabledBorder: _getBorder(),
      focusedBorder: _getBorder(isFocused: true),
      fillColor: useDarkTheme ? const Color(0xFF1E1E1E) : Colors.white,
      filled: true,
      prefixIcon:
          state.style.has(SWT.SEARCH) || state.style.has(SWT.ICON_SEARCH)
              ? const Icon(Icons.search, size: 16)
              : null,
      prefixIconConstraints: const BoxConstraints(
        minHeight: 32,
        minWidth: 32,
      ),
      suffixIcon: (state.style.has(SWT.SEARCH) &&
                  state.text != null &&
                  state.text!.isNotEmpty) ||
              state.style.has(SWT.ICON_CANCEL)
          ? IconButton(
              icon: const Icon(Icons.clear, size: 16),
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(
                minHeight: 32,
                minWidth: 32,
              ),
              onPressed: () {
                _controller.clear();
                widget.sendSelectionDefaultSelection(state, SWT.ICON_CANCEL);
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
        color: isFocused
            ? (useDarkTheme ? Colors.white : Colors.black)
            : (useDarkTheme ? Colors.grey[700]! : Colors.grey[300]!),
        width: 1,
      ),
    );
  }

  void _handleTextChanged(String value) {
    widget.sendModifyModify(state, null);
    widget.sendVerifyVerify(state, null);
  }

  void _handleSubmitted(String value) {
    widget.sendSelectionDefaultSelection(state, null);
  }

  void _handleFocusChange() {
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
