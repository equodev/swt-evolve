import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'clabel_theme_extension.tailor.dart';
part 'clabel_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@TextStyleConverter()
@TextAlignConverter()
@MainAxisAlignmentConverter()
@CrossAxisAlignmentConverter()
class CLabelThemeExtension extends ThemeExtension<CLabelThemeExtension> with _$CLabelThemeExtensionTailorMixin {
  // Text colors
  final Color primaryTextColor;
  final Color disabledTextColor;

  // Background color
  final Color? backgroundColor;

  // Font styles
  final TextStyle? primaryTextStyle;
  final TextStyle? disabledTextStyle;

  // Icon properties
  final double iconSize;
  final double iconTextSpacing;

  // Interactive properties
  final double disabledOpacity;

  // Alignment and positioning
  final TextAlign textAlign;
  final MainAxisAlignment mainAxisAlignment;
  final CrossAxisAlignment crossAxisAlignment;

  const CLabelThemeExtension({
    required this.primaryTextColor,
    required this.disabledTextColor,
    this.backgroundColor,
    this.primaryTextStyle,
    this.disabledTextStyle,
    required this.iconSize,
    required this.iconTextSpacing,
    required this.disabledOpacity,
    required this.textAlign,
    required this.mainAxisAlignment,
    required this.crossAxisAlignment,
  });

  factory CLabelThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$CLabelThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$CLabelThemeExtensionToJson(this);
}
