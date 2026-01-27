import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'slider_theme_extension.tailor.dart';
part 'slider_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
class SliderThemeExtension extends ThemeExtension<SliderThemeExtension> with _$SliderThemeExtensionTailorMixin {
  // Track colors
  final Color activeTrackColor;
  final Color inactiveTrackColor;
  final Color disabledActiveTrackColor;
  final Color disabledInactiveTrackColor;

  // Thumb colors
  final Color thumbColor;
  final Color disabledThumbColor;

  // Overlay colors
  final Color overlayColor;

  // Track sizing
  final double trackHeight;
  final double trackBorderRadius;

  // Thumb sizing
  final double thumbRadius;

  // Default minimum sizes (used when no bounds provided)
  final double minWidth;
  final double minHeight;
  final double minVerticalWidth;
  final double minVerticalHeight;

  // Default slider values
  final int minimum;
  final int maximum;
  final int selection;
  final int increment;
  final int pageIncrement;
  final int thumb;

  const SliderThemeExtension({
    required this.activeTrackColor,
    required this.inactiveTrackColor,
    required this.disabledActiveTrackColor,
    required this.disabledInactiveTrackColor,
    required this.thumbColor,
    required this.disabledThumbColor,
    required this.overlayColor,
    required this.trackHeight,
    required this.trackBorderRadius,
    required this.thumbRadius,
    required this.minWidth,
    required this.minHeight,
    required this.minVerticalWidth,
    required this.minVerticalHeight,
    required this.minimum,
    required this.maximum,
    required this.selection,
    required this.increment,
    required this.pageIncrement,
    required this.thumb,
  });

  factory SliderThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$SliderThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$SliderThemeExtensionToJson(this);
}
