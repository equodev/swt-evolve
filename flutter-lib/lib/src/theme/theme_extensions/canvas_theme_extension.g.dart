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
);

Map<String, dynamic> _$CanvasThemeExtensionToJson(
  CanvasThemeExtension instance,
) => <String, dynamic>{
  'defaultWidth': instance.defaultWidth,
  'defaultHeight': instance.defaultHeight,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'foregroundColor': const ColorConverter().toJson(instance.foregroundColor),
};
