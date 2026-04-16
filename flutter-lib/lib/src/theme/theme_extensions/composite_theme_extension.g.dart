// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'composite_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CompositeThemeExtension _$CompositeThemeExtensionFromJson(
  Map<String, dynamic> json,
) => CompositeThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  focusedBorderColor: const ColorConverter().fromJson(
    json['focusedBorderColor'] as String,
  ),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  contentPadding: (json['contentPadding'] as num).toDouble(),
  workbenchAreaGapColor: const ColorConverter().fromJson(
    json['workbenchAreaGapColor'] as String,
  ),
  panelBorderColor: const ColorConverter().fromJson(
    json['panelBorderColor'] as String,
  ),
  panelBorderWidth: (json['panelBorderWidth'] as num).toDouble(),
  panelBorderRadius: (json['panelBorderRadius'] as num).toDouble(),
  panelChildGap: (json['panelChildGap'] as num).toDouble(),
  panelShadowColor: const ColorConverter().fromJson(
    json['panelShadowColor'] as String,
  ),
  panelShadowBlurRadius: (json['panelShadowBlurRadius'] as num).toDouble(),
  panelShadowDx: (json['panelShadowDx'] as num).toDouble(),
  panelShadowDy: (json['panelShadowDy'] as num).toDouble(),
);

Map<String, dynamic> _$CompositeThemeExtensionToJson(
  CompositeThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'focusedBorderColor': const ColorConverter().toJson(
    instance.focusedBorderColor,
  ),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'contentPadding': instance.contentPadding,
  'workbenchAreaGapColor': const ColorConverter().toJson(
    instance.workbenchAreaGapColor,
  ),
  'panelBorderColor': const ColorConverter().toJson(instance.panelBorderColor),
  'panelBorderWidth': instance.panelBorderWidth,
  'panelBorderRadius': instance.panelBorderRadius,
  'panelChildGap': instance.panelChildGap,
  'panelShadowColor': const ColorConverter().toJson(instance.panelShadowColor),
  'panelShadowBlurRadius': instance.panelShadowBlurRadius,
  'panelShadowDx': instance.panelShadowDx,
  'panelShadowDy': instance.panelShadowDy,
};
