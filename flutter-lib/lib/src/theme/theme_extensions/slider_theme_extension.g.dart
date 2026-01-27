// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'slider_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SliderThemeExtension _$SliderThemeExtensionFromJson(
  Map<String, dynamic> json,
) => SliderThemeExtension(
  activeTrackColor: const ColorConverter().fromJson(
    json['activeTrackColor'] as String,
  ),
  inactiveTrackColor: const ColorConverter().fromJson(
    json['inactiveTrackColor'] as String,
  ),
  disabledActiveTrackColor: const ColorConverter().fromJson(
    json['disabledActiveTrackColor'] as String,
  ),
  disabledInactiveTrackColor: const ColorConverter().fromJson(
    json['disabledInactiveTrackColor'] as String,
  ),
  thumbColor: const ColorConverter().fromJson(json['thumbColor'] as String),
  disabledThumbColor: const ColorConverter().fromJson(
    json['disabledThumbColor'] as String,
  ),
  overlayColor: const ColorConverter().fromJson(json['overlayColor'] as String),
  trackHeight: (json['trackHeight'] as num).toDouble(),
  trackBorderRadius: (json['trackBorderRadius'] as num).toDouble(),
  thumbRadius: (json['thumbRadius'] as num).toDouble(),
  minWidth: (json['minWidth'] as num).toDouble(),
  minHeight: (json['minHeight'] as num).toDouble(),
  minVerticalWidth: (json['minVerticalWidth'] as num).toDouble(),
  minVerticalHeight: (json['minVerticalHeight'] as num).toDouble(),
  minimum: (json['minimum'] as num).toInt(),
  maximum: (json['maximum'] as num).toInt(),
  selection: (json['selection'] as num).toInt(),
  increment: (json['increment'] as num).toInt(),
  pageIncrement: (json['pageIncrement'] as num).toInt(),
  thumb: (json['thumb'] as num).toInt(),
);

Map<String, dynamic> _$SliderThemeExtensionToJson(
  SliderThemeExtension instance,
) => <String, dynamic>{
  'activeTrackColor': const ColorConverter().toJson(instance.activeTrackColor),
  'inactiveTrackColor': const ColorConverter().toJson(
    instance.inactiveTrackColor,
  ),
  'disabledActiveTrackColor': const ColorConverter().toJson(
    instance.disabledActiveTrackColor,
  ),
  'disabledInactiveTrackColor': const ColorConverter().toJson(
    instance.disabledInactiveTrackColor,
  ),
  'thumbColor': const ColorConverter().toJson(instance.thumbColor),
  'disabledThumbColor': const ColorConverter().toJson(
    instance.disabledThumbColor,
  ),
  'overlayColor': const ColorConverter().toJson(instance.overlayColor),
  'trackHeight': instance.trackHeight,
  'trackBorderRadius': instance.trackBorderRadius,
  'thumbRadius': instance.thumbRadius,
  'minWidth': instance.minWidth,
  'minHeight': instance.minHeight,
  'minVerticalWidth': instance.minVerticalWidth,
  'minVerticalHeight': instance.minVerticalHeight,
  'minimum': instance.minimum,
  'maximum': instance.maximum,
  'selection': instance.selection,
  'increment': instance.increment,
  'pageIncrement': instance.pageIncrement,
  'thumb': instance.thumb,
};
