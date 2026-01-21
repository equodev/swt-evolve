import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'list_theme_extension.tailor.dart';
part 'list_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@EdgeInsetsConverter()
@TextStyleConverter()
@DurationConverter()
class ListThemeExtension extends ThemeExtension<ListThemeExtension> with _$ListThemeExtensionTailorMixin {
  // Background colors
  final Color backgroundColor;
  final Color selectedItemBackgroundColor;
  final Color hoverItemBackgroundColor;
  final Color disabledBackgroundColor;
  
  // Text colors
  final Color textColor;
  final Color selectedItemTextColor;
  final Color disabledTextColor;
  
  // Border colors
  final Color borderColor;
  final Color focusedBorderColor;
  
  // Border styling
  final double borderWidth;
  final double borderRadius;
  
  // Sizing
  final double itemHeight;
  final EdgeInsets itemPadding;
  
  // Font style
  final TextStyle? textStyle;
  
  // Animation
  final Duration animationDuration;
  
  const ListThemeExtension({
    required this.backgroundColor,
    required this.selectedItemBackgroundColor,
    required this.hoverItemBackgroundColor,
    required this.disabledBackgroundColor,
    required this.textColor,
    required this.selectedItemTextColor,
    required this.disabledTextColor,
    required this.borderColor,
    required this.focusedBorderColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.itemHeight,
    required this.itemPadding,
    this.textStyle,
    required this.animationDuration,
  });

  factory ListThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ListThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ListThemeExtensionToJson(this);
}