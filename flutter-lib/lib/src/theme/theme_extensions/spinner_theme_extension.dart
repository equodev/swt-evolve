import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'spinner_theme_extension.tailor.dart';
part 'spinner_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
class SpinnerThemeExtension extends ThemeExtension<SpinnerThemeExtension> with _$SpinnerThemeExtensionTailorMixin {
  // Animation
  final Duration animationDuration;

  // Background colors
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color buttonBackgroundColor;
  final Color buttonHoverColor;
  final Color buttonPressedColor;

  // Text colors
  final Color textColor;
  final Color disabledTextColor;

  // Border colors
  final Color borderColor;
  final Color focusedBorderColor;
  final Color disabledBorderColor;

  // Icon colors
  final Color iconColor;
  final Color iconHoverColor;
  final Color disabledIconColor;

  // Border styling
  final double borderWidth;
  final double borderRadius;

  // Sizing
  final double minWidth;
  final double minHeight;
  final double buttonWidth;
  final double iconSize;
  final EdgeInsets textFieldPadding;

  // Font style
  final TextStyle? textStyle;

  // Default spinner values
  final int defaultMinimum;
  final int defaultMaximum;
  final int defaultSelection;
  final int defaultIncrement;
  final int defaultPageIncrement;
  final int defaultDigits;
  final int defaultTextLimit;

  const SpinnerThemeExtension({
    required this.animationDuration,
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.buttonBackgroundColor,
    required this.buttonHoverColor,
    required this.buttonPressedColor,
    required this.textColor,
    required this.disabledTextColor,
    required this.borderColor,
    required this.focusedBorderColor,
    required this.disabledBorderColor,
    required this.iconColor,
    required this.iconHoverColor,
    required this.disabledIconColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.minWidth,
    required this.minHeight,
    required this.buttonWidth,
    required this.iconSize,
    required this.textFieldPadding,
    this.textStyle,
    required this.defaultMinimum,
    required this.defaultMaximum,
    required this.defaultSelection,
    required this.defaultIncrement,
    required this.defaultPageIncrement,
    required this.defaultDigits,
    required this.defaultTextLimit,
  });

  factory SpinnerThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$SpinnerThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$SpinnerThemeExtensionToJson(this);
}
