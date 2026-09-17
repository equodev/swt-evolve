// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tooltip_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TooltipThemeExtension _$TooltipThemeExtensionFromJson(
  Map<String, dynamic> json,
) => TooltipThemeExtension(
  waitDuration: Duration(microseconds: (json['waitDuration'] as num).toInt()),
  fadeInDuration: Duration(
    microseconds: (json['fadeInDuration'] as num).toInt(),
  ),
  fadeOutDuration: Duration(
    microseconds: (json['fadeOutDuration'] as num).toInt(),
  ),
  slideOffsetY: (json['slideOffsetY'] as num).toDouble(),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  informationBackgroundColor: const ColorConverter().fromJson(
    json['informationBackgroundColor'] as String,
  ),
  warningBackgroundColor: const ColorConverter().fromJson(
    json['warningBackgroundColor'] as String,
  ),
  errorBackgroundColor: const ColorConverter().fromJson(
    json['errorBackgroundColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  balloonBorderRadius: (json['balloonBorderRadius'] as num).toDouble(),
  textColor: const ColorConverter().fromJson(json['textColor'] as String),
  titleTextStyle: const TextStyleConverter().fromJson(
    json['titleTextStyle'] as Map<String, dynamic>?,
  ),
  messageTextStyle: const TextStyleConverter().fromJson(
    json['messageTextStyle'] as Map<String, dynamic>?,
  ),
  messageMaxLines: (json['messageMaxLines'] as num).toInt(),
  titleMessageSpacing: (json['titleMessageSpacing'] as num).toDouble(),
  informationIconColor: const ColorConverter().fromJson(
    json['informationIconColor'] as String,
  ),
  warningIconColor: const ColorConverter().fromJson(
    json['warningIconColor'] as String,
  ),
  errorIconColor: const ColorConverter().fromJson(
    json['errorIconColor'] as String,
  ),
  iconSize: (json['iconSize'] as num).toDouble(),
  iconSpacing: (json['iconSpacing'] as num).toDouble(),
  padding: const EdgeInsetsConverter().fromJson(
    json['padding'] as Map<String, dynamic>,
  ),
  hoverPadding: const EdgeInsetsConverter().fromJson(
    json['hoverPadding'] as Map<String, dynamic>,
  ),
  pointerOffsetX: (json['pointerOffsetX'] as num).toDouble(),
  pointerOffsetY: (json['pointerOffsetY'] as num).toDouble(),
  screenMargin: (json['screenMargin'] as num).toDouble(),
  minWidth: (json['minWidth'] as num).toDouble(),
  maxWidth: (json['maxWidth'] as num).toDouble(),
  minHeight: (json['minHeight'] as num).toDouble(),
  shadowColor: const ColorConverter().fromJson(json['shadowColor'] as String),
  shadowBlurRadius: (json['shadowBlurRadius'] as num).toDouble(),
  shadowOffsetY: (json['shadowOffsetY'] as num).toDouble(),
  richMaxWidth: (json['richMaxWidth'] as num).toDouble(),
  richIconSize: (json['richIconSize'] as num).toDouble(),
  richPadding: const EdgeInsetsConverter().fromJson(
    json['richPadding'] as Map<String, dynamic>,
  ),
  richBorderRadius: (json['richBorderRadius'] as num).toDouble(),
  richBackgroundColor: const ColorConverter().fromJson(
    json['richBackgroundColor'] as String,
  ),
  richGap: (json['richGap'] as num).toDouble(),
  richTitleBodySpacing: (json['richTitleBodySpacing'] as num).toDouble(),
  richTitleTextStyle: const TextStyleConverter().fromJson(
    json['richTitleTextStyle'] as Map<String, dynamic>?,
  ),
  richBodyTextStyle: const TextStyleConverter().fromJson(
    json['richBodyTextStyle'] as Map<String, dynamic>?,
  ),
  richShortcutTextStyle: const TextStyleConverter().fromJson(
    json['richShortcutTextStyle'] as Map<String, dynamic>?,
  ),
  richShortcutBackgroundColor: const ColorConverter().fromJson(
    json['richShortcutBackgroundColor'] as String,
  ),
  richShortcutBorderColor: const ColorConverter().fromJson(
    json['richShortcutBorderColor'] as String,
  ),
  richShortcutPadding: const EdgeInsetsConverter().fromJson(
    json['richShortcutPadding'] as Map<String, dynamic>,
  ),
  richShortcutBorderRadius: (json['richShortcutBorderRadius'] as num)
      .toDouble(),
);

Map<String, dynamic> _$TooltipThemeExtensionToJson(
  TooltipThemeExtension instance,
) => <String, dynamic>{
  'waitDuration': instance.waitDuration.inMicroseconds,
  'fadeInDuration': instance.fadeInDuration.inMicroseconds,
  'fadeOutDuration': instance.fadeOutDuration.inMicroseconds,
  'slideOffsetY': instance.slideOffsetY,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'informationBackgroundColor': const ColorConverter().toJson(
    instance.informationBackgroundColor,
  ),
  'warningBackgroundColor': const ColorConverter().toJson(
    instance.warningBackgroundColor,
  ),
  'errorBackgroundColor': const ColorConverter().toJson(
    instance.errorBackgroundColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'balloonBorderRadius': instance.balloonBorderRadius,
  'textColor': const ColorConverter().toJson(instance.textColor),
  'titleTextStyle': ?const TextStyleConverter().toJson(instance.titleTextStyle),
  'messageTextStyle': ?const TextStyleConverter().toJson(
    instance.messageTextStyle,
  ),
  'messageMaxLines': instance.messageMaxLines,
  'titleMessageSpacing': instance.titleMessageSpacing,
  'informationIconColor': const ColorConverter().toJson(
    instance.informationIconColor,
  ),
  'warningIconColor': const ColorConverter().toJson(instance.warningIconColor),
  'errorIconColor': const ColorConverter().toJson(instance.errorIconColor),
  'iconSize': instance.iconSize,
  'iconSpacing': instance.iconSpacing,
  'padding': const EdgeInsetsConverter().toJson(instance.padding),
  'hoverPadding': const EdgeInsetsConverter().toJson(instance.hoverPadding),
  'pointerOffsetX': instance.pointerOffsetX,
  'pointerOffsetY': instance.pointerOffsetY,
  'screenMargin': instance.screenMargin,
  'minWidth': instance.minWidth,
  'maxWidth': instance.maxWidth,
  'minHeight': instance.minHeight,
  'shadowColor': const ColorConverter().toJson(instance.shadowColor),
  'shadowBlurRadius': instance.shadowBlurRadius,
  'shadowOffsetY': instance.shadowOffsetY,
  'richMaxWidth': instance.richMaxWidth,
  'richIconSize': instance.richIconSize,
  'richPadding': const EdgeInsetsConverter().toJson(instance.richPadding),
  'richBorderRadius': instance.richBorderRadius,
  'richBackgroundColor': const ColorConverter().toJson(
    instance.richBackgroundColor,
  ),
  'richGap': instance.richGap,
  'richTitleBodySpacing': instance.richTitleBodySpacing,
  'richTitleTextStyle': ?const TextStyleConverter().toJson(
    instance.richTitleTextStyle,
  ),
  'richBodyTextStyle': ?const TextStyleConverter().toJson(
    instance.richBodyTextStyle,
  ),
  'richShortcutTextStyle': ?const TextStyleConverter().toJson(
    instance.richShortcutTextStyle,
  ),
  'richShortcutBackgroundColor': const ColorConverter().toJson(
    instance.richShortcutBackgroundColor,
  ),
  'richShortcutBorderColor': const ColorConverter().toJson(
    instance.richShortcutBorderColor,
  ),
  'richShortcutPadding': const EdgeInsetsConverter().toJson(
    instance.richShortcutPadding,
  ),
  'richShortcutBorderRadius': instance.richShortcutBorderRadius,
};
