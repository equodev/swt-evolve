// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tooltip_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TooltipThemeExtension _$TooltipThemeExtensionFromJson(
  Map<String, dynamic> json,
) => TooltipThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  messageTextStyle: const TextStyleConverter().fromJson(
    json['messageTextStyle'] as Map<String, dynamic>?,
  ),
  waitDuration: Duration(microseconds: (json['waitDuration'] as num).toInt()),
);

Map<String, dynamic> _$TooltipThemeExtensionToJson(
  TooltipThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'borderRadius': instance.borderRadius,
  'messageTextStyle': const TextStyleConverter().toJson(
    instance.messageTextStyle,
  ),
  'waitDuration': instance.waitDuration.inMicroseconds,
};
