import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'coolbar_theme_extension.tailor.dart';
part 'coolbar_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@EdgeInsetsConverter()
@DurationConverter()
class CoolBarThemeExtension extends ThemeExtension<CoolBarThemeExtension>
    with _$CoolBarThemeExtensionTailorMixin {
  final Color backgroundColor;
  final Color borderColor;
  final double borderWidth;
  final double borderRadius;
  final EdgeInsets padding;
  final Duration animationDuration;

  const CoolBarThemeExtension({
    required this.backgroundColor,
    required this.borderColor,
    required this.borderWidth,
    required this.borderRadius,
    required this.padding,
    required this.animationDuration,
  });

  factory CoolBarThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CoolBarThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CoolBarThemeExtensionToJson(this);
}
