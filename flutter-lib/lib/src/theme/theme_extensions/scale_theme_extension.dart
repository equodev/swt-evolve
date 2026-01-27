import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'scale_theme_extension.tailor.dart';
part 'scale_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
class ScaleThemeExtension extends ThemeExtension<ScaleThemeExtension> with _$ScaleThemeExtensionTailorMixin {
  // Animation
  final Duration animationDuration;

  // Track colors
  final Color activeTrackColor;
  final Color inactiveTrackColor;
  final Color disabledActiveTrackColor;
  final Color disabledInactiveTrackColor;

  // Thumb colors
  final Color thumbColor;
  final Color thumbHoverColor;
  final Color disabledThumbColor;

  // Tick mark colors
  final Color tickMarkColor;
  final Color disabledTickMarkColor;

  // Overlay colors
  final Color overlayColor;

  // Track sizing
  final double trackHeight;

  // Thumb sizing
  final double thumbRadius;
  final double thumbHoverRadius;

  // Tick mark sizing
  final double tickMarkHeight;
  final double tickMarkWidth;
  final int tickMarkCount;

  // Default minimum sizes (used when no bounds provided)
  final double minWidth;
  final double minHeight;
  final double minVerticalWidth;
  final double minVerticalHeight;

  // Padding
  final double horizontalPadding;

  // Default scale values
  final int defaultMinimum;
  final int defaultMaximum;
  final int defaultSelection;
  final int defaultIncrement;
  final int defaultPageIncrement;

  const ScaleThemeExtension({
    required this.animationDuration,
    required this.activeTrackColor,
    required this.inactiveTrackColor,
    required this.disabledActiveTrackColor,
    required this.disabledInactiveTrackColor,
    required this.thumbColor,
    required this.thumbHoverColor,
    required this.disabledThumbColor,
    required this.tickMarkColor,
    required this.disabledTickMarkColor,
    required this.overlayColor,
    required this.trackHeight,
    required this.thumbRadius,
    required this.thumbHoverRadius,
    required this.tickMarkHeight,
    required this.tickMarkWidth,
    required this.tickMarkCount,
    required this.minWidth,
    required this.minHeight,
    required this.minVerticalWidth,
    required this.minVerticalHeight,
    required this.horizontalPadding,
    required this.defaultMinimum,
    required this.defaultMaximum,
    required this.defaultSelection,
    required this.defaultIncrement,
    required this.defaultPageIncrement,
  });

  factory ScaleThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ScaleThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ScaleThemeExtensionToJson(this);
}
