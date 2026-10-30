import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'clabel_theme_extension.tailor.dart';

@TailorMixin()
class CLabelThemeExtension extends ThemeExtension<CLabelThemeExtension> with _$CLabelThemeExtensionTailorMixin {
  // Text colors
  final Color primaryTextColor;
  final Color disabledTextColor;

  // Background color
  final Color? backgroundColor;

  // Font styles
  final TextStyle? primaryTextStyle;
  final TextStyle? disabledTextStyle;

  // Icon properties
  final double iconSize;
  final double iconTextSpacing;

  // Margin
  final double defaultMargin;

  // Interactive properties
  final double disabledOpacity;

  // Alignment and positioning
  final TextAlign textAlign;
  final MainAxisAlignment mainAxisAlignment;
  final CrossAxisAlignment crossAxisAlignment;

  final double minTextContrast;

  const CLabelThemeExtension({
    required this.primaryTextColor,
    required this.disabledTextColor,
    this.backgroundColor,
    this.primaryTextStyle,
    this.disabledTextStyle,
    required this.iconSize,
    required this.iconTextSpacing,
    required this.defaultMargin,
    required this.disabledOpacity,
    required this.textAlign,
    required this.mainAxisAlignment,
    required this.crossAxisAlignment,
    required this.minTextContrast,
  });
}
