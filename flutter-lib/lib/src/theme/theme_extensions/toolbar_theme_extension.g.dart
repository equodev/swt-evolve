// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'toolbar_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ToolBarThemeExtension _$ToolBarThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ToolBarThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  shadowColor: const ColorConverter().fromJson(json['shadowColor'] as String),
  shadowOpacity: (json['shadowOpacity'] as num).toDouble(),
  shadowBlurRadius: (json['shadowBlurRadius'] as num).toDouble(),
  shadowOffset: const OffsetConverter().fromJson(
    json['shadowOffset'] as Map<String, dynamic>,
  ),
  itemPadding: const EdgeInsetsConverter().fromJson(
    json['itemPadding'] as Map<String, dynamic>,
  ),
  compositeBackgroundColor: const ColorConverter().fromJson(
    json['compositeBackgroundColor'] as String,
  ),
  toolbarBackgroundColor: const ColorConverter().fromJson(
    json['toolbarBackgroundColor'] as String,
  ),
);

Map<String, dynamic> _$ToolBarThemeExtensionToJson(
  ToolBarThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'borderWidth': instance.borderWidth,
  'shadowColor': const ColorConverter().toJson(instance.shadowColor),
  'shadowOpacity': instance.shadowOpacity,
  'shadowBlurRadius': instance.shadowBlurRadius,
  'shadowOffset': const OffsetConverter().toJson(instance.shadowOffset),
  'itemPadding': const EdgeInsetsConverter().toJson(instance.itemPadding),
  'compositeBackgroundColor': const ColorConverter().toJson(
    instance.compositeBackgroundColor,
  ),
  'toolbarBackgroundColor': const ColorConverter().toJson(
    instance.toolbarBackgroundColor,
  ),
};
