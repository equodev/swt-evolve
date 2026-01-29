import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'sash_theme_extension.tailor.dart';
part 'sash_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
class SashThemeExtension extends ThemeExtension<SashThemeExtension>
    with _$SashThemeExtensionTailorMixin {
  final Color backgroundColor;
  final Color sashColor;
  final Color sashHoverColor;
  final double sashCenterOpacity;
  final double sashCenterHoverOpacity;
  final double hitAreaSize;
  final double defaultSize;

  const SashThemeExtension({
    required this.backgroundColor,
    required this.sashColor,
    required this.sashHoverColor,
    required this.sashCenterOpacity,
    required this.sashCenterHoverOpacity,
    required this.hitAreaSize,
    required this.defaultSize,
  });

  factory SashThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$SashThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$SashThemeExtensionToJson(this);
}
