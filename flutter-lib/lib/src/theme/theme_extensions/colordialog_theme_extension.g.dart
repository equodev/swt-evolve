// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'colordialog_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ColorDialogThemeExtension _$ColorDialogThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ColorDialogThemeExtension(
  titleStyle: const TextStyleConverter().fromJson(
    json['titleStyle'] as Map<String, dynamic>?,
  ),
  labelStyle: const TextStyleConverter().fromJson(
    json['labelStyle'] as Map<String, dynamic>?,
  ),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  maxWidth: (json['maxWidth'] as num).toDouble(),
  padding: const EdgeInsetsConverter().fromJson(
    json['padding'] as Map<String, dynamic>,
  ),
  titleContentSpacing: (json['titleContentSpacing'] as num).toDouble(),
  sectionSpacing: (json['sectionSpacing'] as num).toDouble(),
  contentButtonsSpacing: (json['contentButtonsSpacing'] as num).toDouble(),
  buttonSpacing: (json['buttonSpacing'] as num).toDouble(),
  saturationValueHeight: (json['saturationValueHeight'] as num).toDouble(),
  thumbRadius: (json['thumbRadius'] as num).toDouble(),
  thumbOutlineRadius: (json['thumbOutlineRadius'] as num).toDouble(),
  thumbStrokeWidth: (json['thumbStrokeWidth'] as num).toDouble(),
  thumbColor: const ColorConverter().fromJson(json['thumbColor'] as String),
  thumbOutlineColor: const ColorConverter().fromJson(
    json['thumbOutlineColor'] as String,
  ),
  hueSliderHeight: (json['hueSliderHeight'] as num).toDouble(),
  hueThumbWidth: (json['hueThumbWidth'] as num).toDouble(),
  hueThumbStrokeWidth: (json['hueThumbStrokeWidth'] as num).toDouble(),
  hueThumbColor: const ColorConverter().fromJson(
    json['hueThumbColor'] as String,
  ),
  swatchSize: (json['swatchSize'] as num).toDouble(),
  swatchSpacing: (json['swatchSpacing'] as num).toDouble(),
  swatchBorderRadius: (json['swatchBorderRadius'] as num).toDouble(),
  swatchBorderWidth: (json['swatchBorderWidth'] as num).toDouble(),
  swatchBorderColor: const ColorConverter().fromJson(
    json['swatchBorderColor'] as String,
  ),
  selectedSwatchBorderWidth: (json['selectedSwatchBorderWidth'] as num)
      .toDouble(),
  selectedSwatchBorderColor: const ColorConverter().fromJson(
    json['selectedSwatchBorderColor'] as String,
  ),
  previewWidth: (json['previewWidth'] as num).toDouble(),
  previewHeight: (json['previewHeight'] as num).toDouble(),
  previewBorderRadius: (json['previewBorderRadius'] as num).toDouble(),
  previewBorderColor: const ColorConverter().fromJson(
    json['previewBorderColor'] as String,
  ),
  previewSpacing: (json['previewSpacing'] as num).toDouble(),
  hexFieldWidth: (json['hexFieldWidth'] as num).toDouble(),
);

Map<String, dynamic> _$ColorDialogThemeExtensionToJson(
  ColorDialogThemeExtension instance,
) => <String, dynamic>{
  'titleStyle': ?const TextStyleConverter().toJson(instance.titleStyle),
  'labelStyle': ?const TextStyleConverter().toJson(instance.labelStyle),
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'borderRadius': instance.borderRadius,
  'maxWidth': instance.maxWidth,
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'titleContentSpacing': instance.titleContentSpacing,
  'sectionSpacing': instance.sectionSpacing,
  'contentButtonsSpacing': instance.contentButtonsSpacing,
  'buttonSpacing': instance.buttonSpacing,
  'saturationValueHeight': instance.saturationValueHeight,
  'thumbRadius': instance.thumbRadius,
  'thumbOutlineRadius': instance.thumbOutlineRadius,
  'thumbStrokeWidth': instance.thumbStrokeWidth,
  'thumbColor': const ColorConverter().toJson(instance.thumbColor),
  'thumbOutlineColor': const ColorConverter().toJson(
    instance.thumbOutlineColor,
  ),
  'hueSliderHeight': instance.hueSliderHeight,
  'hueThumbWidth': instance.hueThumbWidth,
  'hueThumbStrokeWidth': instance.hueThumbStrokeWidth,
  'hueThumbColor': const ColorConverter().toJson(instance.hueThumbColor),
  'swatchSize': instance.swatchSize,
  'swatchSpacing': instance.swatchSpacing,
  'swatchBorderRadius': instance.swatchBorderRadius,
  'swatchBorderWidth': instance.swatchBorderWidth,
  'swatchBorderColor': const ColorConverter().toJson(
    instance.swatchBorderColor,
  ),
  'selectedSwatchBorderWidth': instance.selectedSwatchBorderWidth,
  'selectedSwatchBorderColor': const ColorConverter().toJson(
    instance.selectedSwatchBorderColor,
  ),
  'previewWidth': instance.previewWidth,
  'previewHeight': instance.previewHeight,
  'previewBorderRadius': instance.previewBorderRadius,
  'previewBorderColor': const ColorConverter().toJson(
    instance.previewBorderColor,
  ),
  'previewSpacing': instance.previewSpacing,
  'hexFieldWidth': instance.hexFieldWidth,
};
