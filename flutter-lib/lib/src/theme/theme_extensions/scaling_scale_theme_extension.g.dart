// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scaling_scale_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScalingScaleThemeExtension _$ScalingScaleThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ScalingScaleThemeExtension(
  sliderWidth: (json['sliderWidth'] as num).toDouble(),
  sliderTrackHeight: (json['sliderTrackHeight'] as num).toDouble(),
  thumbRadius: (json['thumbRadius'] as num).toDouble(),
  overlayRadius: (json['overlayRadius'] as num).toDouble(),
  chevronIconSize: (json['chevronIconSize'] as num).toDouble(),
  chevronPadding: const EdgeInsetsConverter().fromJson(
    json['chevronPadding'] as Map<String, dynamic>,
  ),
  chevronIconColor: const ColorConverter().fromJson(
    json['chevronIconColor'] as String,
  ),
  labelTextSize: (json['labelTextSize'] as num).toDouble(),
  labelTextColor: const ColorConverter().fromJson(
    json['labelTextColor'] as String,
  ),
  trailingGap: (json['trailingGap'] as num).toDouble(),
  sliderMin: (json['sliderMin'] as num).toDouble(),
  sliderMax: (json['sliderMax'] as num).toDouble(),
  expandTooltip: json['expandTooltip'] as String,
  collapseTooltip: json['collapseTooltip'] as String,
  resetTooltip: json['resetTooltip'] as String,
);

Map<String, dynamic> _$ScalingScaleThemeExtensionToJson(
  ScalingScaleThemeExtension instance,
) => <String, dynamic>{
  'sliderWidth': instance.sliderWidth,
  'sliderTrackHeight': instance.sliderTrackHeight,
  'thumbRadius': instance.thumbRadius,
  'overlayRadius': instance.overlayRadius,
  'chevronIconSize': instance.chevronIconSize,
  'chevronPadding': const EdgeInsetsConverter().toJson(instance.chevronPadding),
  'chevronIconColor': const ColorConverter().toJson(instance.chevronIconColor),
  'labelTextSize': instance.labelTextSize,
  'labelTextColor': const ColorConverter().toJson(instance.labelTextColor),
  'trailingGap': instance.trailingGap,
  'sliderMin': instance.sliderMin,
  'sliderMax': instance.sliderMax,
  'expandTooltip': instance.expandTooltip,
  'collapseTooltip': instance.collapseTooltip,
  'resetTooltip': instance.resetTooltip,
};
