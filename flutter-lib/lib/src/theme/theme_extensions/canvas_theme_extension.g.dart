// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'canvas_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CanvasThemeExtension _$CanvasThemeExtensionFromJson(
  Map<String, dynamic> json,
) => CanvasThemeExtension(
  defaultWidth: (json['defaultWidth'] as num).toDouble(),
  defaultHeight: (json['defaultHeight'] as num).toDouble(),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  foregroundColor: const ColorConverter().fromJson(
    json['foregroundColor'] as String,
  ),
  fillColor: const ColorConverter().fromJson(json['fillColor'] as String),
  strokeColor: const ColorConverter().fromJson(json['strokeColor'] as String),
  lineColor: const ColorConverter().fromJson(json['lineColor'] as String),
  pointColor: const ColorConverter().fromJson(json['pointColor'] as String),
  textColor: const ColorConverter().fromJson(json['textColor'] as String),
  textBackgroundColor: const ColorConverter().fromJson(
    json['textBackgroundColor'] as String,
  ),
  focusColor: const ColorConverter().fromJson(json['focusColor'] as String),
  gradientStartColor: const ColorConverter().fromJson(
    json['gradientStartColor'] as String,
  ),
  gradientEndColor: const ColorConverter().fromJson(
    json['gradientEndColor'] as String,
  ),
  patternStartColor: const ColorConverter().fromJson(
    json['patternStartColor'] as String,
  ),
  patternEndColor: const ColorConverter().fromJson(
    json['patternEndColor'] as String,
  ),
  imageTintColor: const ColorConverter().fromJson(
    json['imageTintColor'] as String,
  ),
);

Map<String, dynamic> _$CanvasThemeExtensionToJson(
  CanvasThemeExtension instance,
) => <String, dynamic>{
  'defaultWidth': instance.defaultWidth,
  'defaultHeight': instance.defaultHeight,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'foregroundColor': const ColorConverter().toJson(instance.foregroundColor),
  'fillColor': const ColorConverter().toJson(instance.fillColor),
  'strokeColor': const ColorConverter().toJson(instance.strokeColor),
  'lineColor': const ColorConverter().toJson(instance.lineColor),
  'pointColor': const ColorConverter().toJson(instance.pointColor),
  'textColor': const ColorConverter().toJson(instance.textColor),
  'textBackgroundColor': const ColorConverter().toJson(
    instance.textBackgroundColor,
  ),
  'focusColor': const ColorConverter().toJson(instance.focusColor),
  'gradientStartColor': const ColorConverter().toJson(
    instance.gradientStartColor,
  ),
  'gradientEndColor': const ColorConverter().toJson(instance.gradientEndColor),
  'patternStartColor': const ColorConverter().toJson(
    instance.patternStartColor,
  ),
  'patternEndColor': const ColorConverter().toJson(instance.patternEndColor),
  'imageTintColor': const ColorConverter().toJson(instance.imageTintColor),
};
