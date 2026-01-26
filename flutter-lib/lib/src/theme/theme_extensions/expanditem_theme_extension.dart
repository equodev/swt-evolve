import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'expanditem_theme_extension.tailor.dart';
part 'expanditem_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@EdgeInsetsConverter()
@TextStyleConverter()
@CurveConverter()
class ExpandItemThemeExtension extends ThemeExtension<ExpandItemThemeExtension> with _$ExpandItemThemeExtensionTailorMixin {
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

  // Padding
  final EdgeInsets headerPadding;
  final EdgeInsets contentPadding;

  // Typography
  final TextStyle? headerTextStyle;

  // Animation
  final Duration animationDuration;
  final Curve animationCurve;

  // Icon
  final double iconSize;

  // Image spacing
  final double imageTextSpacing;

  const ExpandItemThemeExtension({
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
    required this.headerPadding,
    required this.contentPadding,
    this.headerTextStyle,
    required this.animationDuration,
    required this.animationCurve,
    required this.iconSize,
    required this.imageTextSpacing,
  });

  factory ExpandItemThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ExpandItemThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ExpandItemThemeExtensionToJson(this);
}
