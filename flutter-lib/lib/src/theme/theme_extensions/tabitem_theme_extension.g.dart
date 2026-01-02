// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tabitem_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TabItemThemeExtension _$TabItemThemeExtensionFromJson(
        Map<String, dynamic> json) =>
    TabItemThemeExtension(
      textColor: const ColorConverter().fromJson(json['textColor'] as String),
      disabledTextColor:
          const ColorConverter().fromJson(json['disabledTextColor'] as String),
      iconSize: (json['iconSize'] as num).toDouble(),
      containerPadding: const EdgeInsetsConverter()
          .fromJson(json['containerPadding'] as Map<String, dynamic>),
      imagePadding: const EdgeInsetsConverter()
          .fromJson(json['imagePadding'] as Map<String, dynamic>),
      textPadding: const EdgeInsetsConverter()
          .fromJson(json['textPadding'] as Map<String, dynamic>),
      imageTextSpacing: (json['imageTextSpacing'] as num).toDouble(),
      textStyle: const TextStyleConverter()
          .fromJson(json['textStyle'] as Map<String, dynamic>?),
    );

Map<String, dynamic> _$TabItemThemeExtensionToJson(
        TabItemThemeExtension instance) =>
    <String, dynamic>{
      'textColor': const ColorConverter().toJson(instance.textColor),
      'disabledTextColor':
          const ColorConverter().toJson(instance.disabledTextColor),
      'iconSize': instance.iconSize,
      'containerPadding':
          const EdgeInsetsConverter().toJson(instance.containerPadding),
      'imagePadding': const EdgeInsetsConverter().toJson(instance.imagePadding),
      'textPadding': const EdgeInsetsConverter().toJson(instance.textPadding),
      'imageTextSpacing': instance.imageTextSpacing,
      'textStyle': const TextStyleConverter().toJson(instance.textStyle),
    };
