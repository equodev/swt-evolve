import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'ccombo_theme_extension.tailor.dart';
part 'ccombo_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class CComboThemeExtension extends ThemeExtension<CComboThemeExtension> with _$CComboThemeExtensionTailorMixin {
  // Animation
  final Duration animationDuration;
  
  // Background colors
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color hoverBackgroundColor;
  
  // Text colors
  final Color textColor;
  final Color disabledTextColor;
  
  // Border colors
  final Color borderColor;
  final Color disabledBorderColor;
  
  // Icon colors
  final Color iconColor;
  final Color disabledIconColor;
  
  // Selection colors
  final Color selectedItemBackgroundColor;
  
  // Border styling
  final double borderWidth;
  final double borderRadius;
  
  // Sizing
  final double itemHeight;
  final double iconSize;
  final EdgeInsets textFieldPadding;
  
  // Font style
  final TextStyle? textStyle;
  
  // Divider
  final Color dividerColor;
  final double dividerHeight;
  final double dividerThickness;
  
  const CComboThemeExtension({
    required this.animationDuration,
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.hoverBackgroundColor,
    required this.textColor,
    required this.disabledTextColor,
    required this.borderColor,
    required this.disabledBorderColor,
    required this.iconColor,
    required this.disabledIconColor,
    required this.selectedItemBackgroundColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.itemHeight,
    required this.iconSize,
    required this.textFieldPadding,
    this.textStyle,
    required this.dividerColor,
    required this.dividerHeight,
    required this.dividerThickness,
  });

  factory CComboThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CComboThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CComboThemeExtensionToJson(this);
}