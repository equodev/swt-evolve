import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'ctabitem_theme_extension.tailor.dart';

@TailorMixin()
class CTabItemThemeExtension extends ThemeExtension<CTabItemThemeExtension> with _$CTabItemThemeExtensionTailorMixin {
  // Tab item text colors
  final Color tabItemTextColor;
  final Color tabItemSelectedTextColor;
  final Color tabItemDisabledTextColor;

  // Selected tab of the folder that holds focus
  final Color tabItemActiveTextColor;
  final FontWeight tabItemActiveFontWeight;
  
  // Tab item typography
  final TextStyle? tabItemTextStyle;
  final TextStyle? tabItemSelectedTextStyle;
  
  // Tab item padding
  final double tabItemHorizontalPadding;
  final double tabItemVerticalPadding;
  
  // Tab item image spacing
  final double tabItemImageTextSpacing;

  final double minTextContrast;

  const CTabItemThemeExtension({
    required this.tabItemTextColor,
    required this.tabItemSelectedTextColor,
    required this.tabItemDisabledTextColor,
    required this.tabItemActiveTextColor,
    required this.tabItemActiveFontWeight,
    this.tabItemTextStyle,
    this.tabItemSelectedTextStyle,
    required this.tabItemHorizontalPadding,
    required this.tabItemVerticalPadding,
    required this.tabItemImageTextSpacing,
    required this.minTextContrast,
  });
}

