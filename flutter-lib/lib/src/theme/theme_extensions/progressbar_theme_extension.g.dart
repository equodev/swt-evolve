// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'progressbar_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProgressBarThemeExtension _$ProgressBarThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ProgressBarThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  progressColor: const ColorConverter().fromJson(
    json['progressColor'] as String,
  ),
  disabledProgressColor: const ColorConverter().fromJson(
    json['disabledProgressColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  defaultWidth: (json['defaultWidth'] as num).toDouble(),
  defaultHeight: (json['defaultHeight'] as num).toDouble(),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  indeterminateDuration: Duration(
    microseconds: (json['indeterminateDuration'] as num).toInt(),
  ),
  indeterminateBarWidthFactor: (json['indeterminateBarWidthFactor'] as num)
      .toDouble(),
  indeterminateCurve: const CurveConverter().fromJson(
    json['indeterminateCurve'] as String,
  ),
  indeterminateEnableSizeAnimation:
      json['indeterminateEnableSizeAnimation'] as bool,
  indeterminateSizeStartA: (json['indeterminateSizeStartA'] as num).toDouble(),
  indeterminateSizeMidA: (json['indeterminateSizeMidA'] as num).toDouble(),
  indeterminateSizeEndA: (json['indeterminateSizeEndA'] as num).toDouble(),
  indeterminateSpeedFirstA: (json['indeterminateSpeedFirstA'] as num)
      .toDouble(),
  indeterminateSpeedSecondA: (json['indeterminateSpeedSecondA'] as num)
      .toDouble(),
  indeterminateSizeStartB: (json['indeterminateSizeStartB'] as num).toDouble(),
  indeterminateSizeMidB: (json['indeterminateSizeMidB'] as num).toDouble(),
  indeterminateSizeEndB: (json['indeterminateSizeEndB'] as num).toDouble(),
  indeterminateSpeedFirstB: (json['indeterminateSpeedFirstB'] as num)
      .toDouble(),
  indeterminateSpeedSecondB: (json['indeterminateSpeedSecondB'] as num)
      .toDouble(),
);

Map<String, dynamic> _$ProgressBarThemeExtensionToJson(
  ProgressBarThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'progressColor': const ColorConverter().toJson(instance.progressColor),
  'disabledProgressColor': const ColorConverter().toJson(
    instance.disabledProgressColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'defaultWidth': instance.defaultWidth,
  'defaultHeight': instance.defaultHeight,
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'indeterminateDuration': instance.indeterminateDuration.inMicroseconds,
  'indeterminateBarWidthFactor': instance.indeterminateBarWidthFactor,
  'indeterminateCurve': const CurveConverter().toJson(
    instance.indeterminateCurve,
  ),
  'indeterminateEnableSizeAnimation': instance.indeterminateEnableSizeAnimation,
  'indeterminateSizeStartA': instance.indeterminateSizeStartA,
  'indeterminateSizeMidA': instance.indeterminateSizeMidA,
  'indeterminateSizeEndA': instance.indeterminateSizeEndA,
  'indeterminateSpeedFirstA': instance.indeterminateSpeedFirstA,
  'indeterminateSpeedSecondA': instance.indeterminateSpeedSecondA,
  'indeterminateSizeStartB': instance.indeterminateSizeStartB,
  'indeterminateSizeMidB': instance.indeterminateSizeMidB,
  'indeterminateSizeEndB': instance.indeterminateSizeEndB,
  'indeterminateSpeedFirstB': instance.indeterminateSpeedFirstB,
  'indeterminateSpeedSecondB': instance.indeterminateSpeedSecondB,
};
