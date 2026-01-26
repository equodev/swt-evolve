import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'expandbar_theme_extension.tailor.dart';
part 'expandbar_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@EdgeInsetsConverter()
@TextStyleConverter()
@CurveConverter()
class ExpandBarThemeExtension extends ThemeExtension<ExpandBarThemeExtension> with _$ExpandBarThemeExtensionTailorMixin {
  // Colors
  final Color backgroundColor;
  final Color borderColor;
  final Color foregroundColor;
  final Color foregroundExpandedColor;
  final Color headerBackgroundColor;
  final Color headerBackgroundExpandedColor;
  final Color headerBackgroundHoveredColor;
  final Color contentBackgroundColor;
  final Color iconColor;
  final Color iconExpandedColor;
  final Color disabledForegroundColor;
  final Color disabledBackgroundColor;

  // Border
  final double borderWidth;
  final double borderRadius;
  final double itemBorderRadius;

  // Padding
  final EdgeInsets headerPadding;
  final EdgeInsets contentPadding;
  final EdgeInsets containerPadding;

  // Spacing
  final double itemSpacing;

  // Typography
  final TextStyle? headerTextStyle;

  // Animation
  final Duration animationDuration;
  final Curve animationCurve;

  // Icon
  final double iconSize;

  const ExpandBarThemeExtension({
    required this.backgroundColor,
    required this.borderColor,
    required this.foregroundColor,
    required this.foregroundExpandedColor,
    required this.headerBackgroundColor,
    required this.headerBackgroundExpandedColor,
    required this.headerBackgroundHoveredColor,
    required this.contentBackgroundColor,
    required this.iconColor,
    required this.iconExpandedColor,
    required this.disabledForegroundColor,
    required this.disabledBackgroundColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.itemBorderRadius,
    required this.headerPadding,
    required this.contentPadding,
    required this.containerPadding,
    required this.itemSpacing,
    this.headerTextStyle,
    required this.animationDuration,
    required this.animationCurve,
    required this.iconSize,
  });

  factory ExpandBarThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ExpandBarThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ExpandBarThemeExtensionToJson(this);
}
