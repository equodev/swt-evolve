// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ctabitem_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CTabItemThemeExtension _$CTabItemThemeExtensionFromJson(
  Map<String, dynamic> json,
) => CTabItemThemeExtension(
  tabItemTextColor: const ColorConverter().fromJson(
    json['tabItemTextColor'] as String,
  ),
  tabItemSelectedTextColor: const ColorConverter().fromJson(
    json['tabItemSelectedTextColor'] as String,
  ),
  tabItemDisabledTextColor: const ColorConverter().fromJson(
    json['tabItemDisabledTextColor'] as String,
  ),
  tabItemTextStyle: const TextStyleConverter().fromJson(
    json['tabItemTextStyle'] as Map<String, dynamic>?,
  ),
  tabItemSelectedTextStyle: const TextStyleConverter().fromJson(
    json['tabItemSelectedTextStyle'] as Map<String, dynamic>?,
  ),
  tabItemHorizontalPadding: (json['tabItemHorizontalPadding'] as num)
      .toDouble(),
  tabItemVerticalPadding: (json['tabItemVerticalPadding'] as num).toDouble(),
  tabItemImageTextSpacing: (json['tabItemImageTextSpacing'] as num).toDouble(),
);

Map<String, dynamic> _$CTabItemThemeExtensionToJson(
  CTabItemThemeExtension instance,
) => <String, dynamic>{
  'tabItemTextColor': const ColorConverter().toJson(instance.tabItemTextColor),
  'tabItemSelectedTextColor': const ColorConverter().toJson(
    instance.tabItemSelectedTextColor,
  ),
  'tabItemDisabledTextColor': const ColorConverter().toJson(
    instance.tabItemDisabledTextColor,
  ),
  'tabItemTextStyle': const TextStyleConverter().toJson(
    instance.tabItemTextStyle,
  ),
  'tabItemSelectedTextStyle': const TextStyleConverter().toJson(
    instance.tabItemSelectedTextStyle,
  ),
  'tabItemHorizontalPadding': instance.tabItemHorizontalPadding,
  'tabItemVerticalPadding': instance.tabItemVerticalPadding,
  'tabItemImageTextSpacing': instance.tabItemImageTextSpacing,
};
