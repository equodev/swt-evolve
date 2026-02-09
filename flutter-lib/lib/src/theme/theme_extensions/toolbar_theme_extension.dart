import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'toolbar_theme_extension.tailor.dart';
part 'toolbar_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@EdgeInsetsConverter()
@OffsetConverter()
class ToolBarThemeExtension extends ThemeExtension<ToolBarThemeExtension> with _$ToolBarThemeExtensionTailorMixin {
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final Color shadowColor;
  final double shadowOpacity;
  final double shadowBlurRadius;
  final Offset shadowOffset;
  final EdgeInsets itemPadding;
  final Color compositeBackgroundColor;
  final Color toolbarBackgroundColor;
  final double keywordLeftOffset;

  const ToolBarThemeExtension({
    required this.backgroundColor,
    required this.borderColor,
    required this.borderWidth,
    required this.shadowColor,
    required this.shadowOpacity,
    required this.shadowBlurRadius,
    required this.shadowOffset,
    required this.itemPadding,
    required this.compositeBackgroundColor,
    required this.toolbarBackgroundColor,
    this.keywordLeftOffset = 8.0,
  });


  factory ToolBarThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ToolBarThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ToolBarThemeExtensionToJson(this);
}

