// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupThemeExtension _$GroupThemeExtensionFromJson(
  Map<String, dynamic> json,
) => GroupThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  padding: const EdgeInsetsConverter().fromJson(
    json['padding'] as Map<String, dynamic>,
  ),
  margin: const EdgeInsetsConverter().fromJson(
    json['margin'] as Map<String, dynamic>,
  ),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  foregroundColor: const ColorConverter().fromJson(
    json['foregroundColor'] as String,
  ),
  textStyle: const TextStyleConverter().fromJson(
    json['textStyle'] as Map<String, dynamic>?,
  ),
  titleHorizontalOffset: (json['titleHorizontalOffset'] as num).toDouble(),
  titleLabelPadding: (json['titleLabelPadding'] as num).toDouble(),
  shadowHighlightColor: const ColorConverter().fromJson(
    json['shadowHighlightColor'] as String,
  ),
  shadowDarkColor: const ColorConverter().fromJson(
    json['shadowDarkColor'] as String,
  ),
  shadowOutOpacity: (json['shadowOutOpacity'] as num).toDouble(),
  shadowOutOpacityAlt: (json['shadowOutOpacityAlt'] as num).toDouble(),
  shadowOutBlurRadius: (json['shadowOutBlurRadius'] as num).toDouble(),
  shadowOutBlurRadiusAlt: (json['shadowOutBlurRadiusAlt'] as num).toDouble(),
  shadowOutElevation: (json['shadowOutElevation'] as num).toDouble(),
  shadowSecondaryElevation: (json['shadowSecondaryElevation'] as num)
      .toDouble(),
  shadowEtchedOpacity: (json['shadowEtchedOpacity'] as num).toDouble(),
  shadowEtchedBlurRadius: (json['shadowEtchedBlurRadius'] as num).toDouble(),
  shadowInBorderFactor: (json['shadowInBorderFactor'] as num).toDouble(),
  shadowInBgFactor: (json['shadowInBgFactor'] as num).toDouble(),
);

Map<String, dynamic> _$GroupThemeExtensionToJson(
  GroupThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'margin': const EdgeInsetsConverter().toJson(instance.margin),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'foregroundColor': const ColorConverter().toJson(instance.foregroundColor),
  'textStyle': ?const TextStyleConverter().toJson(instance.textStyle),
  'titleHorizontalOffset': instance.titleHorizontalOffset,
  'titleLabelPadding': instance.titleLabelPadding,
  'shadowHighlightColor': const ColorConverter().toJson(
    instance.shadowHighlightColor,
  ),
  'shadowDarkColor': const ColorConverter().toJson(instance.shadowDarkColor),
  'shadowOutOpacity': instance.shadowOutOpacity,
  'shadowOutOpacityAlt': instance.shadowOutOpacityAlt,
  'shadowOutBlurRadius': instance.shadowOutBlurRadius,
  'shadowOutBlurRadiusAlt': instance.shadowOutBlurRadiusAlt,
  'shadowOutElevation': instance.shadowOutElevation,
  'shadowSecondaryElevation': instance.shadowSecondaryElevation,
  'shadowEtchedOpacity': instance.shadowEtchedOpacity,
  'shadowEtchedBlurRadius': instance.shadowEtchedBlurRadius,
  'shadowInBorderFactor': instance.shadowInBorderFactor,
  'shadowInBgFactor': instance.shadowInBgFactor,
};
