import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'tabitem_theme_extension.tailor.dart';
part 'tabitem_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class TabItemThemeExtension extends ThemeExtension<TabItemThemeExtension> with _$TabItemThemeExtensionTailorMixin {
  // Text colors
  final Color textColor;
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
  
  const TabItemThemeExtension({
    required this.textColor,
    required this.disabledTextColor,
    required this.iconSize,
    required this.containerPadding,
    required this.imagePadding,
    required this.textPadding,
    required this.imageTextSpacing,
    this.textStyle,
  });

  factory TabItemThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TabItemThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TabItemThemeExtensionToJson(this);
}

