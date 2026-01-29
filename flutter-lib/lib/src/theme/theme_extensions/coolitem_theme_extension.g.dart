// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coolitem_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoolItemThemeExtension _$CoolItemThemeExtensionFromJson(
  Map<String, dynamic> json,
) => CoolItemThemeExtension(
  textStyle: const TextStyleConverter().fromJson(
    json['textStyle'] as Map<String, dynamic>?,
  ),
  contentPadding: const EdgeInsetsConverter().fromJson(
    json['contentPadding'] as Map<String, dynamic>,
  ),
);

Map<String, dynamic> _$CoolItemThemeExtensionToJson(
  CoolItemThemeExtension instance,
) => <String, dynamic>{
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
  'contentPadding': const EdgeInsetsConverter().toJson(instance.contentPadding),
};
