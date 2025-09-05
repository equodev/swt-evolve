import 'package:flutter/material.dart';

class StyledGroup extends StatelessWidget {
  final String title;
  final Widget? child;
  final bool useDarkTheme;
  final Color borderColor;
  final Color backgroundColor;
  final Color textColor;

  const StyledGroup({
    Key? key,
    required this.title,
    this.child,
    required this.useDarkTheme,
    required this.borderColor,
    required this.backgroundColor,
    required this.textColor,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(10.0),
      decoration: BoxDecoration(
        border: Border.all(color: borderColor),
        borderRadius: BorderRadius.circular(4.0),
        color: backgroundColor,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          if (title.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(left: 8.0, top: 4.0),
              child: Text(
                title,
                style: TextStyle(
                  color: textColor,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          if (child != null)
            Padding(
              padding: const EdgeInsets.fromLTRB(8.0, 12.0, 8.0, 8.0),
              child: child,
            ),
        ],
      ),
    );
  }
}
