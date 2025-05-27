import 'package:flutter/material.dart';

class StyledTextField extends StatelessWidget {
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
  final bool useDarkTheme;

  const StyledTextField({
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
    required this.useDarkTheme,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 32,
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
        cursorColor: useDarkTheme ? Colors.white : Colors.black,
      ),
    );
  }
}