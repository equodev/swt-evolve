// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'message_box_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MessageBoxThemeExtension _$MessageBoxThemeExtensionFromJson(
  Map<String, dynamic> json,
) => MessageBoxThemeExtension(
  titleStyle: const TextStyleConverter().fromJson(
    json['titleStyle'] as Map<String, dynamic>?,
  ),
  messageStyle: const TextStyleConverter().fromJson(
    json['messageStyle'] as Map<String, dynamic>?,
  ),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  iconErrorColor: const ColorConverter().fromJson(
    json['iconErrorColor'] as String,
  ),
  iconWarningColor: const ColorConverter().fromJson(
    json['iconWarningColor'] as String,
  ),
  iconInfoColor: const ColorConverter().fromJson(
    json['iconInfoColor'] as String,
  ),
  iconQuestionColor: const ColorConverter().fromJson(
    json['iconQuestionColor'] as String,
  ),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  minWidth: (json['minWidth'] as num).toDouble(),
  maxWidth: (json['maxWidth'] as num).toDouble(),
  padding: const EdgeInsetsConverter().fromJson(
    json['padding'] as Map<String, dynamic>,
  ),
  iconTitleSpacing: (json['iconTitleSpacing'] as num).toDouble(),
  titleMessageSpacing: (json['titleMessageSpacing'] as num).toDouble(),
  messageIndent: (json['messageIndent'] as num).toDouble(),
  contentButtonsSpacing: (json['contentButtonsSpacing'] as num).toDouble(),
  buttonSpacing: (json['buttonSpacing'] as num).toDouble(),
);

Map<String, dynamic> _$MessageBoxThemeExtensionToJson(
  MessageBoxThemeExtension instance,
) => <String, dynamic>{
  'titleStyle': ?const TextStyleConverter().toJson(instance.titleStyle),
  'messageStyle': ?const TextStyleConverter().toJson(instance.messageStyle),
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'iconErrorColor': const ColorConverter().toJson(instance.iconErrorColor),
  'iconWarningColor': const ColorConverter().toJson(instance.iconWarningColor),
  'iconInfoColor': const ColorConverter().toJson(instance.iconInfoColor),
  'iconQuestionColor': const ColorConverter().toJson(
    instance.iconQuestionColor,
  ),
  'borderRadius': instance.borderRadius,
  'minWidth': instance.minWidth,
  'maxWidth': instance.maxWidth,
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'iconTitleSpacing': instance.iconTitleSpacing,
  'titleMessageSpacing': instance.titleMessageSpacing,
  'messageIndent': instance.messageIndent,
  'contentButtonsSpacing': instance.contentButtonsSpacing,
  'buttonSpacing': instance.buttonSpacing,
};
