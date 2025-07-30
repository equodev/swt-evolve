import 'package:flutter/material.dart';

class MaterialSeparator extends StatelessWidget {
  final double height;
  final double thickness;
  final double width;
  final Color color;

  const MaterialSeparator({
    Key? key,
    this.height = 24,
    this.thickness = 1,
    this.width = 20,
    this.color = Colors.white54,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: height,
      child: VerticalDivider(
        color: color,
        thickness: thickness,
        width: width,
      ),
    );
  }
}
