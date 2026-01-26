// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupThemeExtension _$GroupThemeExtensionFromJson(Map<String, dynamic> json) =>
    GroupThemeExtension(
      backgroundColor: const ColorConverter().fromJson(
        json['backgroundColor'] as String,
      ),
      borderColor: const ColorConverter().fromJson(
        json['borderColor'] as String,
      ),
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
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
};
