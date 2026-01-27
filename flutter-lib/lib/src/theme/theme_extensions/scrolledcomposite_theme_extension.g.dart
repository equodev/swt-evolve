// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scrolledcomposite_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScrolledCompositeThemeExtension _$ScrolledCompositeThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ScrolledCompositeThemeExtension(
  animationDuration: Duration(
    microseconds: (json['animationDuration'] as num).toInt(),
  ),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  scrollbarThumbColor: const ColorConverter().fromJson(
    json['scrollbarThumbColor'] as String,
  ),
  scrollbarThumbHoverColor: const ColorConverter().fromJson(
    json['scrollbarThumbHoverColor'] as String,
  ),
  scrollbarTrackColor: const ColorConverter().fromJson(
    json['scrollbarTrackColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  focusedBorderColor: const ColorConverter().fromJson(
    json['focusedBorderColor'] as String,
  ),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  scrollbarThickness: (json['scrollbarThickness'] as num).toDouble(),
  scrollbarRadius: (json['scrollbarRadius'] as num).toDouble(),
  scrollbarMinThumbLength: (json['scrollbarMinThumbLength'] as num).toDouble(),
  defaultMinWidth: (json['defaultMinWidth'] as num).toDouble(),
  defaultMinHeight: (json['defaultMinHeight'] as num).toDouble(),
  contentPadding: (json['contentPadding'] as num).toDouble(),
);

Map<String, dynamic> _$ScrolledCompositeThemeExtensionToJson(
  ScrolledCompositeThemeExtension instance,
) => <String, dynamic>{
  'animationDuration': instance.animationDuration.inMicroseconds,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'scrollbarThumbColor': const ColorConverter().toJson(
    instance.scrollbarThumbColor,
  ),
  'scrollbarThumbHoverColor': const ColorConverter().toJson(
    instance.scrollbarThumbHoverColor,
  ),
  'scrollbarTrackColor': const ColorConverter().toJson(
    instance.scrollbarTrackColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'focusedBorderColor': const ColorConverter().toJson(
    instance.focusedBorderColor,
  ),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'scrollbarThickness': instance.scrollbarThickness,
  'scrollbarRadius': instance.scrollbarRadius,
  'scrollbarMinThumbLength': instance.scrollbarMinThumbLength,
  'defaultMinWidth': instance.defaultMinWidth,
  'defaultMinHeight': instance.defaultMinHeight,
  'contentPadding': instance.contentPadding,
};
