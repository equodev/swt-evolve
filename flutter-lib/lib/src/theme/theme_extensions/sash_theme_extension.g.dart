// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sash_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SashThemeExtension _$SashThemeExtensionFromJson(Map<String, dynamic> json) =>
    SashThemeExtension(
      backgroundColor: const ColorConverter().fromJson(
        json['backgroundColor'] as String,
      ),
      sashColor: const ColorConverter().fromJson(json['sashColor'] as String),
      sashHoverColor: const ColorConverter().fromJson(
        json['sashHoverColor'] as String,
      ),
      sashCenterOpacity: (json['sashCenterOpacity'] as num).toDouble(),
      sashCenterHoverOpacity: (json['sashCenterHoverOpacity'] as num)
          .toDouble(),
      hitAreaSize: (json['hitAreaSize'] as num).toDouble(),
      defaultSize: (json['defaultSize'] as num).toDouble(),
    );

Map<String, dynamic> _$SashThemeExtensionToJson(
  SashThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'sashColor': const ColorConverter().toJson(instance.sashColor),
  'sashHoverColor': const ColorConverter().toJson(instance.sashHoverColor),
  'sashCenterOpacity': instance.sashCenterOpacity,
  'sashCenterHoverOpacity': instance.sashCenterHoverOpacity,
  'hitAreaSize': instance.hitAreaSize,
  'defaultSize': instance.defaultSize,
};
