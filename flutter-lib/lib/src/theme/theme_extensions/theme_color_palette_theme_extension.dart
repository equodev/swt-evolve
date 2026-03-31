import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

import 'json_converters.dart';

part 'theme_color_palette_theme_extension.tailor.dart';
part 'theme_color_palette_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@EdgeInsetsConverter()
class ThemeColorPaletteThemeExtension
    extends ThemeExtension<ThemeColorPaletteThemeExtension>
    with _$ThemeColorPaletteThemeExtensionTailorMixin {
  final double swatchSize;
  final double swatchBorderRadius;
  final double swatchSpacing;
  final double trailingGapAfterSwatches;
  final EdgeInsets chevronPadding;
  final double chevronIconSize;
  final Color swatchBorderColor;
  final Color chevronIconColor;
  final String expandTooltip;
  final String collapseTooltip;
  final String sampleHexCsv;

  const ThemeColorPaletteThemeExtension({
    required this.swatchSize,
    required this.swatchBorderRadius,
    required this.swatchSpacing,
    required this.trailingGapAfterSwatches,
    required this.chevronPadding,
    required this.chevronIconSize,
    required this.swatchBorderColor,
    required this.chevronIconColor,
    required this.expandTooltip,
    required this.collapseTooltip,
    required this.sampleHexCsv,
  });

  factory ThemeColorPaletteThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ThemeColorPaletteThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ThemeColorPaletteThemeExtensionToJson(this);
}
