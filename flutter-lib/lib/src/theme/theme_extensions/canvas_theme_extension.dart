import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'canvas_theme_extension.tailor.dart';
part 'canvas_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
class CanvasThemeExtension extends ThemeExtension<CanvasThemeExtension> with _$CanvasThemeExtensionTailorMixin {
  // Default sizing (SWT Control.java: DEFAULT_WIDTH / DEFAULT_HEIGHT = 64)
  final double defaultWidth;
  final double defaultHeight;

  // The Canvas itself, when the application sets no background of its own.
  final Color backgroundColor;
  final Color foregroundColor;

  // One slot per color a GC paints with, so the theme can tell them apart without the drawing
  // side having to guess a role from the color it was handed.
  final Color fillColor;
  final Color strokeColor;
  final Color lineColor;
  final Color pointColor;
  final Color textColor;
  final Color textBackgroundColor;
  final Color focusColor;
  final Color gradientStartColor;
  final Color gradientEndColor;
  final Color patternStartColor;
  final Color patternEndColor;
  final Color imageTintColor;

  const CanvasThemeExtension({
    required this.defaultWidth,
    required this.defaultHeight,
    required this.backgroundColor,
    required this.foregroundColor,
    required this.fillColor,
    required this.strokeColor,
    required this.lineColor,
    required this.pointColor,
    required this.textColor,
    required this.textBackgroundColor,
    required this.focusColor,
    required this.gradientStartColor,
    required this.gradientEndColor,
    required this.patternStartColor,
    required this.patternEndColor,
    required this.imageTintColor,
  });

  factory CanvasThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CanvasThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CanvasThemeExtensionToJson(this);
}
