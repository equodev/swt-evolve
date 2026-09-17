// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'toast_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ToastThemeExtension _$ToastThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ToastThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  titleTextStyle: const TextStyleConverter().fromJson(
    json['titleTextStyle'] as Map<String, dynamic>?,
  ),
  messageTextStyle: const TextStyleConverter().fromJson(
    json['messageTextStyle'] as Map<String, dynamic>?,
  ),
  messageMaxLines: (json['messageMaxLines'] as num).toInt(),
  titleMessageSpacing: (json['titleMessageSpacing'] as num).toDouble(),
  infoColor: const ColorConverter().fromJson(json['infoColor'] as String),
  successColor: const ColorConverter().fromJson(json['successColor'] as String),
  warningColor: const ColorConverter().fromJson(json['warningColor'] as String),
  errorColor: const ColorConverter().fromJson(json['errorColor'] as String),
  accentBarWidth: (json['accentBarWidth'] as num).toDouble(),
  iconSize: (json['iconSize'] as num).toDouble(),
  iconSpacing: (json['iconSpacing'] as num).toDouble(),
  closeIconColor: const ColorConverter().fromJson(
    json['closeIconColor'] as String,
  ),
  closeIconSize: (json['closeIconSize'] as num).toDouble(),
  padding: const EdgeInsetsConverter().fromJson(
    json['padding'] as Map<String, dynamic>,
  ),
  width: (json['width'] as num).toDouble(),
  margin: const EdgeInsetsConverter().fromJson(
    json['margin'] as Map<String, dynamic>,
  ),
  spacing: (json['spacing'] as num).toDouble(),
  maxVisible: (json['maxVisible'] as num).toInt(),
  slideInDuration: Duration(
    microseconds: (json['slideInDuration'] as num).toInt(),
  ),
  fadeOutDuration: Duration(
    microseconds: (json['fadeOutDuration'] as num).toInt(),
  ),
  displayDuration: Duration(
    microseconds: (json['displayDuration'] as num).toInt(),
  ),
  slideOffsetX: (json['slideOffsetX'] as num).toDouble(),
  shadowColor: const ColorConverter().fromJson(json['shadowColor'] as String),
  shadowBlurRadius: (json['shadowBlurRadius'] as num).toDouble(),
  shadowOffsetY: (json['shadowOffsetY'] as num).toDouble(),
);

Map<String, dynamic> _$ToastThemeExtensionToJson(
  ToastThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'titleTextStyle': ?const TextStyleConverter().toJson(instance.titleTextStyle),
  'messageTextStyle': ?const TextStyleConverter().toJson(
    instance.messageTextStyle,
  ),
  'messageMaxLines': instance.messageMaxLines,
  'titleMessageSpacing': instance.titleMessageSpacing,
  'infoColor': const ColorConverter().toJson(instance.infoColor),
  'successColor': const ColorConverter().toJson(instance.successColor),
  'warningColor': const ColorConverter().toJson(instance.warningColor),
  'errorColor': const ColorConverter().toJson(instance.errorColor),
  'accentBarWidth': instance.accentBarWidth,
  'iconSize': instance.iconSize,
  'iconSpacing': instance.iconSpacing,
  'closeIconColor': const ColorConverter().toJson(instance.closeIconColor),
  'closeIconSize': instance.closeIconSize,
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'width': instance.width,
  'margin': const EdgeInsetsConverter().toJson(instance.margin),
  'spacing': instance.spacing,
  'maxVisible': instance.maxVisible,
  'slideInDuration': instance.slideInDuration.inMicroseconds,
  'fadeOutDuration': instance.fadeOutDuration.inMicroseconds,
  'displayDuration': instance.displayDuration.inMicroseconds,
  'slideOffsetX': instance.slideOffsetX,
  'shadowColor': const ColorConverter().toJson(instance.shadowColor),
  'shadowBlurRadius': instance.shadowBlurRadius,
  'shadowOffsetY': instance.shadowOffsetY,
};
