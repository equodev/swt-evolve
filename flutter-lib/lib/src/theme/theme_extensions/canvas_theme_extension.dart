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

  // Colors
  final Color backgroundColor;
  final Color foregroundColor;

  const CanvasThemeExtension({
    required this.defaultWidth,
    required this.defaultHeight,
    required this.backgroundColor,
    required this.foregroundColor,
  });

  factory CanvasThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CanvasThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CanvasThemeExtensionToJson(this);
}
