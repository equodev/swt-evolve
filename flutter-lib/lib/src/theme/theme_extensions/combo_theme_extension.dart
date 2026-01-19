import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'combo_theme_extension.tailor.dart';
part 'combo_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
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

  // Border radius
  final double borderRadius;
  
  // Border width
  final double borderWidth;
  
  // Padding
  final EdgeInsets textFieldPadding;
  final EdgeInsets itemPadding;

  // Sizes
  final double height;
  final double itemHeight;
  final double iconSize;
  
  // Font styles
  final TextStyle? textStyle;
  final TextStyle? itemTextStyle;

  // Divider
  final Color dividerColor;
  final double dividerHeight;
  final double dividerThickness;

  // Animation
  final Duration animationDuration;

  const ComboThemeExtension({
    required this.backgroundColor,
    required this.textColor,
    required this.borderColor,
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
    required this.height,
    required this.itemHeight,
    required this.iconSize,
    this.textStyle,
    this.itemTextStyle,
    required this.dividerColor,
    required this.dividerHeight,
    required this.animationDuration,
  });

  factory ComboThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ComboThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ComboThemeExtensionToJson(this);
}

