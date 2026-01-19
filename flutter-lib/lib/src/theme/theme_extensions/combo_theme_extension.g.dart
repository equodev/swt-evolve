// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'combo_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ComboThemeExtension _$ComboThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ComboThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  textColor: const ColorConverter().fromJson(json['textColor'] as String),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  iconColor: const ColorConverter().fromJson(json['iconColor'] as String),
  selectedItemBackgroundColor: const ColorConverter().fromJson(
    json['selectedItemBackgroundColor'] as String,
  ),
  hoverBackgroundColor: const ColorConverter().fromJson(
    json['hoverBackgroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  disabledTextColor: const ColorConverter().fromJson(
    json['disabledTextColor'] as String,
  ),
  disabledBorderColor: const ColorConverter().fromJson(
    json['disabledBorderColor'] as String,
  ),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  textFieldPadding: const EdgeInsetsConverter().fromJson(
    json['textFieldPadding'] as Map<String, dynamic>,
  ),
  itemPadding: const EdgeInsetsConverter().fromJson(
    json['itemPadding'] as Map<String, dynamic>,
  ),
  dividerThickness: (json['dividerThickness'] as num).toDouble(),
  height: (json['height'] as num).toDouble(),
  itemHeight: (json['itemHeight'] as num).toDouble(),
  iconSize: (json['iconSize'] as num).toDouble(),
  textStyle: const TextStyleConverter().fromJson(
    json['textStyle'] as Map<String, dynamic>?,
  ),
  itemTextStyle: const TextStyleConverter().fromJson(
    json['itemTextStyle'] as Map<String, dynamic>?,
  ),
  dividerColor: const ColorConverter().fromJson(json['dividerColor'] as String),
  dividerHeight: (json['dividerHeight'] as num).toDouble(),
  animationDuration: Duration(
    microseconds: (json['animationDuration'] as num).toInt(),
  ),
);

Map<String, dynamic> _$ComboThemeExtensionToJson(
  ComboThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'textColor': const ColorConverter().toJson(instance.textColor),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'iconColor': const ColorConverter().toJson(instance.iconColor),
  'selectedItemBackgroundColor': const ColorConverter().toJson(
    instance.selectedItemBackgroundColor,
  ),
  'hoverBackgroundColor': const ColorConverter().toJson(
    instance.hoverBackgroundColor,
  ),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'disabledBorderColor': const ColorConverter().toJson(
    instance.disabledBorderColor,
  ),
  'borderRadius': instance.borderRadius,
  'borderWidth': instance.borderWidth,
  'textFieldPadding': const EdgeInsetsConverter().toJson(
    instance.textFieldPadding,
  ),
  'itemPadding': const EdgeInsetsConverter().toJson(instance.itemPadding),
  'height': instance.height,
  'itemHeight': instance.itemHeight,
  'iconSize': instance.iconSize,
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
  'itemTextStyle': const TextStyleConverter().toJson(instance.itemTextStyle),
  'dividerColor': const ColorConverter().toJson(instance.dividerColor),
  'dividerHeight': instance.dividerHeight,
  'dividerThickness': instance.dividerThickness,
  'animationDuration': instance.animationDuration.inMicroseconds,
};
