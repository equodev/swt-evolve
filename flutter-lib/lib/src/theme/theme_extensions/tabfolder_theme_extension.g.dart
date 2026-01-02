// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tabfolder_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TabFolderThemeExtension _$TabFolderThemeExtensionFromJson(
        Map<String, dynamic> json) =>
    TabFolderThemeExtension(
      tabBarBackgroundColor: const ColorConverter()
          .fromJson(json['tabBarBackgroundColor'] as String),
      tabBarBorderColor:
          const ColorConverter().fromJson(json['tabBarBorderColor'] as String),
      tabBackgroundColor:
          const ColorConverter().fromJson(json['tabBackgroundColor'] as String),
      tabSelectedBackgroundColor: const ColorConverter()
          .fromJson(json['tabSelectedBackgroundColor'] as String),
      tabHoverBackgroundColor: const ColorConverter()
          .fromJson(json['tabHoverBackgroundColor'] as String),
      tabDisabledBackgroundColor: const ColorConverter()
          .fromJson(json['tabDisabledBackgroundColor'] as String),
      tabBorderColor:
          const ColorConverter().fromJson(json['tabBorderColor'] as String),
      tabSelectedBorderColor: const ColorConverter()
          .fromJson(json['tabSelectedBorderColor'] as String),
      tabHoverBorderColor: const ColorConverter()
          .fromJson(json['tabHoverBorderColor'] as String),
      tabDisabledBorderColor: const ColorConverter()
          .fromJson(json['tabDisabledBorderColor'] as String),
      tabTextColor:
          const ColorConverter().fromJson(json['tabTextColor'] as String),
      tabSelectedTextColor: const ColorConverter()
          .fromJson(json['tabSelectedTextColor'] as String),
      tabHoverTextColor:
          const ColorConverter().fromJson(json['tabHoverTextColor'] as String),
      tabDisabledTextColor: const ColorConverter()
          .fromJson(json['tabDisabledTextColor'] as String),
      tabBorderWidth: (json['tabBorderWidth'] as num).toDouble(),
      tabSelectedBorderWidth:
          (json['tabSelectedBorderWidth'] as num).toDouble(),
      tabBorderRadius: (json['tabBorderRadius'] as num).toDouble(),
      tabPadding: (json['tabPadding'] as num).toDouble(),
      tabTextStyle: const TextStyleConverter()
          .fromJson(json['tabTextStyle'] as Map<String, dynamic>?),
      tabSelectedTextStyle: const TextStyleConverter()
          .fromJson(json['tabSelectedTextStyle'] as Map<String, dynamic>?),
      tabContentBackgroundColor: const ColorConverter()
          .fromJson(json['tabContentBackgroundColor'] as String),
      tabContentBorderColor: const ColorConverter()
          .fromJson(json['tabContentBorderColor'] as String),
    );

Map<String, dynamic> _$TabFolderThemeExtensionToJson(
        TabFolderThemeExtension instance) =>
    <String, dynamic>{
      'tabBarBackgroundColor':
          const ColorConverter().toJson(instance.tabBarBackgroundColor),
      'tabBarBorderColor':
          const ColorConverter().toJson(instance.tabBarBorderColor),
      'tabBackgroundColor':
          const ColorConverter().toJson(instance.tabBackgroundColor),
      'tabSelectedBackgroundColor':
          const ColorConverter().toJson(instance.tabSelectedBackgroundColor),
      'tabHoverBackgroundColor':
          const ColorConverter().toJson(instance.tabHoverBackgroundColor),
      'tabDisabledBackgroundColor':
          const ColorConverter().toJson(instance.tabDisabledBackgroundColor),
      'tabBorderColor': const ColorConverter().toJson(instance.tabBorderColor),
      'tabSelectedBorderColor':
          const ColorConverter().toJson(instance.tabSelectedBorderColor),
      'tabHoverBorderColor':
          const ColorConverter().toJson(instance.tabHoverBorderColor),
      'tabDisabledBorderColor':
          const ColorConverter().toJson(instance.tabDisabledBorderColor),
      'tabTextColor': const ColorConverter().toJson(instance.tabTextColor),
      'tabSelectedTextColor':
          const ColorConverter().toJson(instance.tabSelectedTextColor),
      'tabHoverTextColor':
          const ColorConverter().toJson(instance.tabHoverTextColor),
      'tabDisabledTextColor':
          const ColorConverter().toJson(instance.tabDisabledTextColor),
      'tabBorderWidth': instance.tabBorderWidth,
      'tabSelectedBorderWidth': instance.tabSelectedBorderWidth,
      'tabBorderRadius': instance.tabBorderRadius,
      'tabPadding': instance.tabPadding,
      'tabTextStyle': const TextStyleConverter().toJson(instance.tabTextStyle),
      'tabSelectedTextStyle':
          const TextStyleConverter().toJson(instance.tabSelectedTextStyle),
      'tabContentBackgroundColor':
          const ColorConverter().toJson(instance.tabContentBackgroundColor),
      'tabContentBorderColor':
          const ColorConverter().toJson(instance.tabContentBorderColor),
    };
