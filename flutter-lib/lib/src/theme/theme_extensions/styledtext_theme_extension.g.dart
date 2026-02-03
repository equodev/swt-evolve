// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'styledtext_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StyledTextThemeExtension _$StyledTextThemeExtensionFromJson(
  Map<String, dynamic> json,
) => StyledTextThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  foregroundColor: const ColorConverter().fromJson(
    json['foregroundColor'] as String,
  ),
);

Map<String, dynamic> _$StyledTextThemeExtensionToJson(
  StyledTextThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'foregroundColor': const ColorConverter().toJson(instance.foregroundColor),
};
