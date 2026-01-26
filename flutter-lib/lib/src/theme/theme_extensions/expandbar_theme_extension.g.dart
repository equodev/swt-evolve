// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expandbar_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpandBarThemeExtension _$ExpandBarThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ExpandBarThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  foregroundColor: const ColorConverter().fromJson(
    json['foregroundColor'] as String,
  ),
  foregroundExpandedColor: const ColorConverter().fromJson(
    json['foregroundExpandedColor'] as String,
  ),
  headerBackgroundColor: const ColorConverter().fromJson(
    json['headerBackgroundColor'] as String,
  ),
  headerBackgroundExpandedColor: const ColorConverter().fromJson(
    json['headerBackgroundExpandedColor'] as String,
  ),
  headerBackgroundHoveredColor: const ColorConverter().fromJson(
    json['headerBackgroundHoveredColor'] as String,
  ),
  contentBackgroundColor: const ColorConverter().fromJson(
    json['contentBackgroundColor'] as String,
  ),
  iconColor: const ColorConverter().fromJson(json['iconColor'] as String),
  iconExpandedColor: const ColorConverter().fromJson(
    json['iconExpandedColor'] as String,
  ),
  disabledForegroundColor: const ColorConverter().fromJson(
    json['disabledForegroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  itemBorderRadius: (json['itemBorderRadius'] as num).toDouble(),
  headerPadding: const EdgeInsetsConverter().fromJson(
    json['headerPadding'] as Map<String, dynamic>,
  ),
  contentPadding: const EdgeInsetsConverter().fromJson(
    json['contentPadding'] as Map<String, dynamic>,
  ),
  containerPadding: const EdgeInsetsConverter().fromJson(
    json['containerPadding'] as Map<String, dynamic>,
  ),
  itemSpacing: (json['itemSpacing'] as num).toDouble(),
  headerTextStyle: const TextStyleConverter().fromJson(
    json['headerTextStyle'] as Map<String, dynamic>?,
  ),
  animationDuration: Duration(
    microseconds: (json['animationDuration'] as num).toInt(),
  ),
  animationCurve: const CurveConverter().fromJson(
    json['animationCurve'] as String,
  ),
  iconSize: (json['iconSize'] as num).toDouble(),
);

Map<String, dynamic> _$ExpandBarThemeExtensionToJson(
  ExpandBarThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'foregroundColor': const ColorConverter().toJson(instance.foregroundColor),
  'foregroundExpandedColor': const ColorConverter().toJson(
    instance.foregroundExpandedColor,
  ),
  'headerBackgroundColor': const ColorConverter().toJson(
    instance.headerBackgroundColor,
  ),
  'headerBackgroundExpandedColor': const ColorConverter().toJson(
    instance.headerBackgroundExpandedColor,
  ),
  'headerBackgroundHoveredColor': const ColorConverter().toJson(
    instance.headerBackgroundHoveredColor,
  ),
  'contentBackgroundColor': const ColorConverter().toJson(
    instance.contentBackgroundColor,
  ),
  'iconColor': const ColorConverter().toJson(instance.iconColor),
  'iconExpandedColor': const ColorConverter().toJson(
    instance.iconExpandedColor,
  ),
  'disabledForegroundColor': const ColorConverter().toJson(
    instance.disabledForegroundColor,
  ),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'itemBorderRadius': instance.itemBorderRadius,
  'headerPadding': const EdgeInsetsConverter().toJson(instance.headerPadding),
  'contentPadding': const EdgeInsetsConverter().toJson(instance.contentPadding),
  'containerPadding': const EdgeInsetsConverter().toJson(
    instance.containerPadding,
  ),
  'itemSpacing': instance.itemSpacing,
  'headerTextStyle': const TextStyleConverter().toJson(
    instance.headerTextStyle,
  ),
  'animationDuration': instance.animationDuration.inMicroseconds,
  'animationCurve': const CurveConverter().toJson(instance.animationCurve),
  'iconSize': instance.iconSize,
};
