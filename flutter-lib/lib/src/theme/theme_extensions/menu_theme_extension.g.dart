// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'menu_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MenuThemeExtension _$MenuThemeExtensionFromJson(Map<String, dynamic> json) =>
    MenuThemeExtension(
      animationDuration: Duration(
        microseconds: (json['animationDuration'] as num).toInt(),
      ),
      backgroundColor: const ColorConverter().fromJson(
        json['backgroundColor'] as String,
      ),
      menuBarBackgroundColor: const ColorConverter().fromJson(
        json['menuBarBackgroundColor'] as String,
      ),
      popupBackgroundColor: const ColorConverter().fromJson(
        json['popupBackgroundColor'] as String,
      ),
      hoverBackgroundColor: const ColorConverter().fromJson(
        json['hoverBackgroundColor'] as String,
      ),
      disabledBackgroundColor: const ColorConverter().fromJson(
        json['disabledBackgroundColor'] as String,
      ),
      textColor: const ColorConverter().fromJson(json['textColor'] as String),
      disabledTextColor: const ColorConverter().fromJson(
        json['disabledTextColor'] as String,
      ),
      borderColor: const ColorConverter().fromJson(
        json['borderColor'] as String,
      ),
      menuBarBorderColor: const ColorConverter().fromJson(
        json['menuBarBorderColor'] as String,
      ),
      borderWidth: (json['borderWidth'] as num).toDouble(),
      borderRadius: (json['borderRadius'] as num).toDouble(),
      disabledBorderColor: const ColorConverter().fromJson(
        json['disabledBorderColor'] as String,
      ),
      menuBarHeight: (json['menuBarHeight'] as num).toDouble(),
      minMenuWidth: (json['minMenuWidth'] as num).toDouble(),
      maxMenuWidth: (json['maxMenuWidth'] as num).toDouble(),
      menuBarItemPadding: const EdgeInsetsConverter().fromJson(
        json['menuBarItemPadding'] as Map<String, dynamic>,
      ),
      popupPadding: const EdgeInsetsConverter().fromJson(
        json['popupPadding'] as Map<String, dynamic>,
      ),
      popupElevation: (json['popupElevation'] as num).toDouble(),
      menuBarItemMargin: const EdgeInsetsConverter().fromJson(
        json['menuBarItemMargin'] as Map<String, dynamic>,
      ),
      textStyle: const TextStyleConverter().fromJson(
        json['textStyle'] as Map<String, dynamic>?,
      ),
    );

Map<String, dynamic> _$MenuThemeExtensionToJson(
  MenuThemeExtension instance,
) => <String, dynamic>{
  'animationDuration': instance.animationDuration.inMicroseconds,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'menuBarBackgroundColor': const ColorConverter().toJson(
    instance.menuBarBackgroundColor,
  ),
  'popupBackgroundColor': const ColorConverter().toJson(
    instance.popupBackgroundColor,
  ),
  'hoverBackgroundColor': const ColorConverter().toJson(
    instance.hoverBackgroundColor,
  ),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'textColor': const ColorConverter().toJson(instance.textColor),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'menuBarBorderColor': const ColorConverter().toJson(
    instance.menuBarBorderColor,
  ),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'disabledBorderColor': const ColorConverter().toJson(
    instance.disabledBorderColor,
  ),
  'menuBarHeight': instance.menuBarHeight,
  'minMenuWidth': instance.minMenuWidth,
  'maxMenuWidth': instance.maxMenuWidth,
  'menuBarItemPadding': const EdgeInsetsConverter().toJson(
    instance.menuBarItemPadding,
  ),
  'popupPadding': const EdgeInsetsConverter().toJson(instance.popupPadding),
  'menuBarItemMargin': const EdgeInsetsConverter().toJson(
    instance.menuBarItemMargin,
  ),
  'popupElevation': instance.popupElevation,
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
};
