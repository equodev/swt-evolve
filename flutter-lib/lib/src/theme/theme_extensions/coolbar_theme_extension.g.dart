// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coolbar_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoolBarThemeExtension _$CoolBarThemeExtensionFromJson(
  Map<String, dynamic> json,
) => CoolBarThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  padding: const EdgeInsetsConverter().fromJson(
    json['padding'] as Map<String, dynamic>,
  ),
  animationDuration: Duration(
    microseconds: (json['animationDuration'] as num).toInt(),
  ),
);

Map<String, dynamic> _$CoolBarThemeExtensionToJson(
  CoolBarThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'animationDuration': instance.animationDuration.inMicroseconds,
};
