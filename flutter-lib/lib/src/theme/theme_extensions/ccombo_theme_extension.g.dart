// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ccombo_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CComboThemeExtension _$CComboThemeExtensionFromJson(
  Map<String, dynamic> json,
) => CComboThemeExtension(
  animationDuration: Duration(
    microseconds: (json['animationDuration'] as num).toInt(),
  ),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  hoverBackgroundColor: const ColorConverter().fromJson(
    json['hoverBackgroundColor'] as String,
  ),
  textColor: const ColorConverter().fromJson(json['textColor'] as String),
  disabledTextColor: const ColorConverter().fromJson(
    json['disabledTextColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  disabledBorderColor: const ColorConverter().fromJson(
    json['disabledBorderColor'] as String,
  ),
  iconColor: const ColorConverter().fromJson(json['iconColor'] as String),
  disabledIconColor: const ColorConverter().fromJson(
    json['disabledIconColor'] as String,
  ),
  selectedItemBackgroundColor: const ColorConverter().fromJson(
    json['selectedItemBackgroundColor'] as String,
  ),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  itemHeight: (json['itemHeight'] as num).toDouble(),
  iconSize: (json['iconSize'] as num).toDouble(),
  textFieldPadding: const EdgeInsetsConverter().fromJson(
    json['textFieldPadding'] as Map<String, dynamic>,
  ),
  textStyle: const TextStyleConverter().fromJson(
    json['textStyle'] as Map<String, dynamic>?,
  ),
  dividerColor: const ColorConverter().fromJson(json['dividerColor'] as String),
  dividerHeight: (json['dividerHeight'] as num).toDouble(),
  dividerThickness: (json['dividerThickness'] as num).toDouble(),
);

Map<String, dynamic> _$CComboThemeExtensionToJson(
  CComboThemeExtension instance,
) => <String, dynamic>{
  'animationDuration': instance.animationDuration.inMicroseconds,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'hoverBackgroundColor': const ColorConverter().toJson(
    instance.hoverBackgroundColor,
  ),
  'textColor': const ColorConverter().toJson(instance.textColor),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'disabledBorderColor': const ColorConverter().toJson(
    instance.disabledBorderColor,
  ),
  'iconColor': const ColorConverter().toJson(instance.iconColor),
  'disabledIconColor': const ColorConverter().toJson(
    instance.disabledIconColor,
  ),
  'selectedItemBackgroundColor': const ColorConverter().toJson(
    instance.selectedItemBackgroundColor,
  ),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'itemHeight': instance.itemHeight,
  'iconSize': instance.iconSize,
  'textFieldPadding': const EdgeInsetsConverter().toJson(
    instance.textFieldPadding,
  ),
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
  'dividerColor': const ColorConverter().toJson(instance.dividerColor),
  'dividerHeight': instance.dividerHeight,
  'dividerThickness': instance.dividerThickness,
};
