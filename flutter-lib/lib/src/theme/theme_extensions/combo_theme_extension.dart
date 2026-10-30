import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'combo_theme_extension.tailor.dart';

@TailorMixin()
class ComboThemeExtension extends ThemeExtension<ComboThemeExtension> with _$ComboThemeExtensionTailorMixin {
  // Colors
  final Color backgroundColor;
  final Color textColor;
  final Color borderColor;
  final Color iconColor;
  final Color selectedItemBackgroundColor;
  final Color hoverBackgroundColor;
  final Color disabledBackgroundColor;
  final Color disabledTextColor;
  final Color disabledBorderColor;
  final Color focusedBorderColor;

  // Border radius
  final double borderRadius;
  
  // Border width
  final double borderWidth;
  
  // Padding
  final EdgeInsets textFieldPadding;
  final EdgeInsets itemPadding;

  // Sizes
  final double iconSize;
  final double iconSpacing;
  
  // Font styles
  final TextStyle? textStyle;
  final TextStyle? itemTextStyle;

  // Divider
  final Color dividerColor;
  final double dividerHeight;
  final double dividerThickness;

  // Animation
  final Duration animationDuration;

  final double minTextContrast;

  const ComboThemeExtension({
    required this.backgroundColor,
    required this.textColor,
    required this.borderColor,
    required this.focusedBorderColor,
    required this.iconColor,
    required this.selectedItemBackgroundColor,
    required this.hoverBackgroundColor,
    required this.disabledBackgroundColor,
    required this.disabledTextColor,
    required this.disabledBorderColor,
    required this.borderRadius,
    required this.borderWidth,
    required this.textFieldPadding,
    required this.itemPadding,
    required this.dividerThickness,
    required this.iconSize,
    required this.iconSpacing,
    this.textStyle,
    this.itemTextStyle,
    required this.dividerColor,
    required this.dividerHeight,
    required this.animationDuration,
    required this.minTextContrast,
  });
}

