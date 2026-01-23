// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'clabel_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CLabelThemeExtension _$CLabelThemeExtensionFromJson(
  Map<String, dynamic> json,
) => CLabelThemeExtension(
  primaryTextColor: const ColorConverter().fromJson(
    json['primaryTextColor'] as String,
  ),
  disabledTextColor: const ColorConverter().fromJson(
    json['disabledTextColor'] as String,
  ),
  backgroundColor: _$JsonConverterFromJson<String, Color>(
    json['backgroundColor'],
    const ColorConverter().fromJson,
  ),
  primaryTextStyle: const TextStyleConverter().fromJson(
    json['primaryTextStyle'] as Map<String, dynamic>?,
  ),
  disabledTextStyle: const TextStyleConverter().fromJson(
    json['disabledTextStyle'] as Map<String, dynamic>?,
  ),
  iconSize: (json['iconSize'] as num).toDouble(),
  iconTextSpacing: (json['iconTextSpacing'] as num).toDouble(),
  disabledOpacity: (json['disabledOpacity'] as num).toDouble(),
  textAlign: const TextAlignConverter().fromJson(
    (json['textAlign'] as num).toInt(),
  ),
  mainAxisAlignment: const MainAxisAlignmentConverter().fromJson(
    (json['mainAxisAlignment'] as num).toInt(),
  ),
  crossAxisAlignment: const CrossAxisAlignmentConverter().fromJson(
    (json['crossAxisAlignment'] as num).toInt(),
  ),
);

Map<String, dynamic> _$CLabelThemeExtensionToJson(
  CLabelThemeExtension instance,
) => <String, dynamic>{
  'primaryTextColor': const ColorConverter().toJson(instance.primaryTextColor),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'backgroundColor': _$JsonConverterToJson<String, Color>(
    instance.backgroundColor,
    const ColorConverter().toJson,
  ),
  'primaryTextStyle': const TextStyleConverter().toJson(
    instance.primaryTextStyle,
  ),
  'disabledTextStyle': const TextStyleConverter().toJson(
    instance.disabledTextStyle,
  ),
  'iconSize': instance.iconSize,
  'iconTextSpacing': instance.iconTextSpacing,
  'disabledOpacity': instance.disabledOpacity,
  'textAlign': const TextAlignConverter().toJson(instance.textAlign),
  'mainAxisAlignment': const MainAxisAlignmentConverter().toJson(
    instance.mainAxisAlignment,
  ),
  'crossAxisAlignment': const CrossAxisAlignmentConverter().toJson(
    instance.crossAxisAlignment,
  ),
};

Value? _$JsonConverterFromJson<Json, Value>(
  Object? json,
  Value? Function(Json json) fromJson,
) => json == null ? null : fromJson(json as Json);

Json? _$JsonConverterToJson<Json, Value>(
  Value? value,
  Json? Function(Value value) toJson,
) => value == null ? null : toJson(value);
