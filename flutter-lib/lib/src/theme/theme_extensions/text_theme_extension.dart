import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'text_theme_extension.tailor.dart';
part 'text_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@EdgeInsetsConverter()
@FontWeightConverter()
@CurveConverter()
class TextThemeExtension extends ThemeExtension<TextThemeExtension> with _$TextThemeExtensionTailorMixin {
  // Text colors
  final Color textColor;
  final Color disabledTextColor;
  final Color placeholderColor;
  final Color helperTextColor;
  final Color errorTextColor;
  
  // Background colors
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color hoverBackgroundColor;
  final Color focusedBackgroundColor;
  
  // Border colors
  final Color borderColor;
  final Color hoverBorderColor;
  final Color focusedBorderColor;
  final Color errorBorderColor;
  final Color disabledBorderColor;
  
  // Border properties
  final double borderRadius;
  final double borderWidth;
  final double hoverBorderWidth;
  final double focusedBorderWidth;
  final double errorBorderWidth;
  
  // Padding and spacing
  final EdgeInsets contentPadding;
  
  // Typography
  final double fontSize;
  final FontWeight fontWeight;
  final String? fontFamily;
  final double letterSpacing;
  final double lineHeight;
  
  // Helper text
  final double helperTextFontSize;
  final double helperTextSpacing;
  
  // Interactive properties
  final Duration focusAnimationDuration;
  final Curve focusAnimationCurve;
  final Duration hoverAnimationDuration;
  final Curve hoverAnimationCurve;
  
  // Password field
  final Color passwordToggleColor;
  final Color passwordToggleHoverColor;
  final double passwordToggleSize;

  const TextThemeExtension({
    required this.textColor,
    required this.disabledTextColor,
    required this.placeholderColor,
    required this.helperTextColor,
    required this.errorTextColor,
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.hoverBackgroundColor,
    required this.focusedBackgroundColor,
    required this.borderColor,
    required this.hoverBorderColor,
    required this.focusedBorderColor,
    required this.errorBorderColor,
    required this.disabledBorderColor,
    required this.borderRadius,
    required this.borderWidth,
    required this.hoverBorderWidth,
    required this.focusedBorderWidth,
    required this.errorBorderWidth,
    required this.contentPadding,
    required this.fontSize,
    required this.fontWeight,
    this.fontFamily,
    required this.letterSpacing,
    required this.lineHeight,
    required this.helperTextFontSize,
    required this.helperTextSpacing,
    required this.focusAnimationDuration,
    required this.focusAnimationCurve,
    required this.hoverAnimationDuration,
    required this.hoverAnimationCurve,
    required this.passwordToggleColor,
    required this.passwordToggleHoverColor,
    required this.passwordToggleSize,
  });

  factory TextThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TextThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TextThemeExtensionToJson(this);
}
