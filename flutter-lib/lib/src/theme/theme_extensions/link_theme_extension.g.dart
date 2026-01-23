// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'link_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LinkThemeExtension _$LinkThemeExtensionFromJson(Map<String, dynamic> json) =>
    LinkThemeExtension(
      textColor: const ColorConverter().fromJson(json['textColor'] as String),
      linkTextColor: const ColorConverter().fromJson(
        json['linkTextColor'] as String,
      ),
      linkHoverTextColor: const ColorConverter().fromJson(
        json['linkHoverTextColor'] as String,
      ),
      disabledTextColor: const ColorConverter().fromJson(
        json['disabledTextColor'] as String,
      ),
      backgroundColor: _$JsonConverterFromJson<String, Color>(
        json['backgroundColor'],
        const ColorConverter().fromJson,
      ),
      hoverBackgroundColor: _$JsonConverterFromJson<String, Color>(
        json['hoverBackgroundColor'],
        const ColorConverter().fromJson,
      ),
      textStyle: const TextStyleConverter().fromJson(
        json['textStyle'] as Map<String, dynamic>?,
      ),
      linkTextStyle: const TextStyleConverter().fromJson(
        json['linkTextStyle'] as Map<String, dynamic>?,
      ),
      disabledTextStyle: const TextStyleConverter().fromJson(
        json['disabledTextStyle'] as Map<String, dynamic>?,
      ),
      padding: const EdgeInsetsConverter().fromJson(
        json['padding'] as Map<String, dynamic>,
      ),
      minHeight: (json['minHeight'] as num).toDouble(),
      disabledOpacity: (json['disabledOpacity'] as num).toDouble(),
      hoverAnimationDuration: Duration(
        microseconds: (json['hoverAnimationDuration'] as num).toInt(),
      ),
      linkDecoration: const TextDecorationConverter().fromJson(
        json['linkDecoration'] as String,
      ),
      linkHoverDecoration: const TextDecorationConverter().fromJson(
        json['linkHoverDecoration'] as String,
      ),
      textAlign: const TextAlignConverter().fromJson(
        (json['textAlign'] as num).toInt(),
      ),
    );

Map<String, dynamic> _$LinkThemeExtensionToJson(
  LinkThemeExtension instance,
) => <String, dynamic>{
  'textColor': const ColorConverter().toJson(instance.textColor),
  'linkTextColor': const ColorConverter().toJson(instance.linkTextColor),
  'linkHoverTextColor': const ColorConverter().toJson(
    instance.linkHoverTextColor,
  ),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'backgroundColor': _$JsonConverterToJson<String, Color>(
    instance.backgroundColor,
    const ColorConverter().toJson,
  ),
  'hoverBackgroundColor': _$JsonConverterToJson<String, Color>(
    instance.hoverBackgroundColor,
    const ColorConverter().toJson,
  ),
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
  'linkTextStyle': const TextStyleConverter().toJson(instance.linkTextStyle),
  'disabledTextStyle': const TextStyleConverter().toJson(
    instance.disabledTextStyle,
  ),
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'minHeight': instance.minHeight,
  'disabledOpacity': instance.disabledOpacity,
  'hoverAnimationDuration': instance.hoverAnimationDuration.inMicroseconds,
  'linkDecoration': const TextDecorationConverter().toJson(
    instance.linkDecoration,
  ),
  'linkHoverDecoration': const TextDecorationConverter().toJson(
    instance.linkHoverDecoration,
  ),
  'textAlign': const TextAlignConverter().toJson(instance.textAlign),
};

Value? _$JsonConverterFromJson<Json, Value>(
  Object? json,
  Value? Function(Json json) fromJson,
) => json == null ? null : fromJson(json as Json);

Json? _$JsonConverterToJson<Json, Value>(
  Value? value,
  Json? Function(Value value) toJson,
) => value == null ? null : toJson(value);
