import 'package:flutter/material.dart';

class SeparatorLabel extends StatelessWidget {
  final Axis direction;
  final bool useDarkTheme;

  const SeparatorLabel({
    Key? key,
    required this.direction,
    required this.useDarkTheme,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final color = useDarkTheme ?  const Color(0xFFFFFFFF) : const Color(
        0xFF333232);
    return Container(
      width: direction == Axis.vertical ? 1 : double.infinity,
      height: direction == Axis.horizontal ? 1 : double.infinity,
      color: color,
    );
  }
}

class TextLabel extends StatelessWidget {
  final String text;
  final TextAlign alignment;
  final bool wrap;
  final bool useDarkTheme;
  final bool vertical;

  const TextLabel({
    Key? key,
    required this.text,
    required this.alignment,
    required this.wrap,
    required this.useDarkTheme,
    required this.vertical,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final textColor = useDarkTheme ? const Color(0xFFFFFFFF) : const Color(
        0xFF333232);
    final textWidget = Text(
      text,
      textAlign: alignment,
      softWrap: wrap,
      overflow: wrap ? TextOverflow.clip : TextOverflow.ellipsis,
      style: TextStyle(
        color: textColor,
        fontSize: 14,
        fontWeight: FontWeight.w400,
      ),
    );

    if (vertical) {
      return RotatedBox(
        quarterTurns: 3,
        child: textWidget,
      );
    }

    return textWidget;
  }
}