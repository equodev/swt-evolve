// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'theme_color_palette_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ThemeColorPaletteThemeExtension _$ThemeColorPaletteThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ThemeColorPaletteThemeExtension(
  swatchSize: (json['swatchSize'] as num).toDouble(),
  swatchBorderRadius: (json['swatchBorderRadius'] as num).toDouble(),
  swatchSpacing: (json['swatchSpacing'] as num).toDouble(),
  trailingGapAfterSwatches: (json['trailingGapAfterSwatches'] as num)
      .toDouble(),
  chevronPadding: const EdgeInsetsConverter().fromJson(
    json['chevronPadding'] as Map<String, dynamic>,
  ),
  chevronIconSize: (json['chevronIconSize'] as num).toDouble(),
  swatchBorderColor: const ColorConverter().fromJson(
    json['swatchBorderColor'] as String,
  ),
  chevronIconColor: const ColorConverter().fromJson(
    json['chevronIconColor'] as String,
  ),
  expandTooltip: json['expandTooltip'] as String,
  collapseTooltip: json['collapseTooltip'] as String,
  sampleHexCsv: json['sampleHexCsv'] as String,
);

Map<String, dynamic> _$ThemeColorPaletteThemeExtensionToJson(
  ThemeColorPaletteThemeExtension instance,
) => <String, dynamic>{
  'swatchSize': instance.swatchSize,
  'swatchBorderRadius': instance.swatchBorderRadius,
  'swatchSpacing': instance.swatchSpacing,
  'trailingGapAfterSwatches': instance.trailingGapAfterSwatches,
  'chevronPadding': const EdgeInsetsConverter().toJson(instance.chevronPadding),
  'chevronIconSize': instance.chevronIconSize,
  'swatchBorderColor': const ColorConverter().toJson(
    instance.swatchBorderColor,
  ),
  'chevronIconColor': const ColorConverter().toJson(instance.chevronIconColor),
  'expandTooltip': instance.expandTooltip,
  'collapseTooltip': instance.collapseTooltip,
  'sampleHexCsv': instance.sampleHexCsv,
};
