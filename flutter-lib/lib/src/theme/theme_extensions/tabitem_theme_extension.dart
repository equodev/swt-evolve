import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'tabitem_theme_extension.tailor.dart';

@TailorMixin()
class TabItemThemeExtension extends ThemeExtension<TabItemThemeExtension> with _$TabItemThemeExtensionTailorMixin {
  // Text colors
  final Color textColor;
  final Color selectedTextColor;
  final Color disabledTextColor;
  
  // Icon properties
  final double iconSize;
  
  // Padding and spacing
  final EdgeInsets containerPadding;
  final EdgeInsets imagePadding;
  final EdgeInsets textPadding;
  final double imageTextSpacing;
  
  // Typography
  final TextStyle? textStyle;

  final double minTextContrast;

  const TabItemThemeExtension({
    required this.textColor,
    required this.selectedTextColor,
    required this.disabledTextColor,
    required this.iconSize,
    required this.containerPadding,
    required this.imagePadding,
    required this.textPadding,
    required this.imageTextSpacing,
    this.textStyle,
    required this.minTextContrast,
  });
}

