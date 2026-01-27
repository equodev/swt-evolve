import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'scrolledcomposite_theme_extension.tailor.dart';
part 'scrolledcomposite_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
class ScrolledCompositeThemeExtension extends ThemeExtension<ScrolledCompositeThemeExtension> with _$ScrolledCompositeThemeExtensionTailorMixin {
  // Animation
  final Duration animationDuration;

  // Background colors
  final Color backgroundColor;
  final Color disabledBackgroundColor;

  // Scrollbar colors
  final Color scrollbarThumbColor;
  final Color scrollbarThumbHoverColor;
  final Color scrollbarTrackColor;

  // Border colors
  final Color borderColor;
  final Color focusedBorderColor;

  // Border styling
  final double borderWidth;
  final double borderRadius;

  // Scrollbar styling
  final double scrollbarThickness;
  final double scrollbarRadius;
  final double scrollbarMinThumbLength;

  // Default minimum sizes
  final double defaultMinWidth;
  final double defaultMinHeight;

  // Padding
  final double contentPadding;

  const ScrolledCompositeThemeExtension({
    required this.animationDuration,
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.scrollbarThumbColor,
    required this.scrollbarThumbHoverColor,
    required this.scrollbarTrackColor,
    required this.borderColor,
    required this.focusedBorderColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.scrollbarThickness,
    required this.scrollbarRadius,
    required this.scrollbarMinThumbLength,
    required this.defaultMinWidth,
    required this.defaultMinHeight,
    required this.contentPadding,
  });

  factory ScrolledCompositeThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ScrolledCompositeThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ScrolledCompositeThemeExtensionToJson(this);
}
