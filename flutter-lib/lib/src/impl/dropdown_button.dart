import 'package:flutter/material.dart';

class MaterialDropdownButton extends StatelessWidget {
  final String text;
  final VoidCallback? onPressed;
  final bool enabled;
  final double borderRadius;
  final double height;
  final double minWidth;
  final bool useDarkTheme;

  const MaterialDropdownButton({
    Key? key,
    required this.text,
    this.onPressed,
    this.enabled = true,
    this.borderRadius = 5.0,
    this.height = 30.0,
    this.minWidth = 70.0,
    this.useDarkTheme = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {

    final Color darkButtonColor = const Color(0xFF6366F1);
    final Color darkTextColor = const Color(0xFFFFFFFF);

    final Color lightButtonColor = const Color(0xFF6366F1);
    final Color lightTextColor = const Color(0xFF595858);

    final Color buttonColor = useDarkTheme ? darkButtonColor : lightButtonColor;
    final Color textColor = useDarkTheme ? darkTextColor : lightTextColor;

    return MaterialButton(
      onPressed: enabled ? onPressed : null,
      elevation: 0,
      focusElevation: 0,
      hoverElevation: 0,
      highlightElevation: 0,
      disabledElevation: 0,
      height: height,
      minWidth: minWidth,
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      color: enabled ? buttonColor : buttonColor.withOpacity(0.5),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(borderRadius),
      ),
      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            text,
            style: TextStyle(
              color: textColor.withOpacity(enabled ? 1.0 : 0.5),
              fontWeight: FontWeight.w500,
            ),
          ),
          Icon(
            Icons.arrow_drop_down,
            color: textColor.withOpacity(enabled ? 1.0 : 0.5),
            size: 20,
          ),
        ],
      ),
    );
  }
}