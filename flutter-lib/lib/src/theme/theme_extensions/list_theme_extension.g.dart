// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'list_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ListThemeExtension _$ListThemeExtensionFromJson(Map<String, dynamic> json) =>
    ListThemeExtension(
      backgroundColor: const ColorConverter().fromJson(
        json['backgroundColor'] as String,
      ),
      selectedItemBackgroundColor: const ColorConverter().fromJson(
        json['selectedItemBackgroundColor'] as String,
      ),
      hoverItemBackgroundColor: const ColorConverter().fromJson(
        json['hoverItemBackgroundColor'] as String,
      ),
      disabledBackgroundColor: const ColorConverter().fromJson(
        json['disabledBackgroundColor'] as String,
      ),
      textColor: const ColorConverter().fromJson(json['textColor'] as String),
      selectedItemTextColor: const ColorConverter().fromJson(
        json['selectedItemTextColor'] as String,
      ),
      disabledTextColor: const ColorConverter().fromJson(
        json['disabledTextColor'] as String,
      ),
      borderColor: const ColorConverter().fromJson(
        json['borderColor'] as String,
      ),
      focusedBorderColor: const ColorConverter().fromJson(
        json['focusedBorderColor'] as String,
      ),
      borderWidth: (json['borderWidth'] as num).toDouble(),
      borderRadius: (json['borderRadius'] as num).toDouble(),
      itemHeight: (json['itemHeight'] as num).toDouble(),
      itemPadding: const EdgeInsetsConverter().fromJson(
        json['itemPadding'] as Map<String, dynamic>,
      ),
      textStyle: const TextStyleConverter().fromJson(
        json['textStyle'] as Map<String, dynamic>?,
      ),
      animationDuration: Duration(
        microseconds: (json['animationDuration'] as num).toInt(),
      ),
    );

Map<String, dynamic> _$ListThemeExtensionToJson(
  ListThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'selectedItemBackgroundColor': const ColorConverter().toJson(
    instance.selectedItemBackgroundColor,
  ),
  'hoverItemBackgroundColor': const ColorConverter().toJson(
    instance.hoverItemBackgroundColor,
  ),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'textColor': const ColorConverter().toJson(instance.textColor),
  'selectedItemTextColor': const ColorConverter().toJson(
    instance.selectedItemTextColor,
  ),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'focusedBorderColor': const ColorConverter().toJson(
    instance.focusedBorderColor,
  ),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'itemHeight': instance.itemHeight,
  'itemPadding': const EdgeInsetsConverter().toJson(instance.itemPadding),
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
  'animationDuration': instance.animationDuration.inMicroseconds,
};
