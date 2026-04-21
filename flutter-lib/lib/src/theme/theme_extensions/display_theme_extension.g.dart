// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'display_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DisplayThemeExtension _$DisplayThemeExtensionFromJson(
  Map<String, dynamic> json,
) => DisplayThemeExtension(
  titleBarColor: const ColorConverter().fromJson(
    json['titleBarColor'] as String,
  ),
  titleBarHoverColor: const ColorConverter().fromJson(
    json['titleBarHoverColor'] as String,
  ),
  titleBarTextColor: const ColorConverter().fromJson(
    json['titleBarTextColor'] as String,
  ),
  titleBarHeight: (json['titleBarHeight'] as num).toDouble(),
  toolWindowTitleBarHeight: (json['toolWindowTitleBarHeight'] as num)
      .toDouble(),
  titleBarBorderRadius: (json['titleBarBorderRadius'] as num).toDouble(),
  closeButtonColor: const ColorConverter().fromJson(
    json['closeButtonColor'] as String,
  ),
  closeButtonHoverColor: const ColorConverter().fromJson(
    json['closeButtonHoverColor'] as String,
  ),
  closeButtonHoverIconColor: const ColorConverter().fromJson(
    json['closeButtonHoverIconColor'] as String,
  ),
  minimizeButtonColor: const ColorConverter().fromJson(
    json['minimizeButtonColor'] as String,
  ),
  minimizeButtonHoverColor: const ColorConverter().fromJson(
    json['minimizeButtonHoverColor'] as String,
  ),
  maximizeButtonColor: const ColorConverter().fromJson(
    json['maximizeButtonColor'] as String,
  ),
  maximizeButtonHoverColor: const ColorConverter().fromJson(
    json['maximizeButtonHoverColor'] as String,
  ),
  titleBarButtonSize: (json['titleBarButtonSize'] as num).toDouble(),
  titleBarButtonBorderRadius: (json['titleBarButtonBorderRadius'] as num)
      .toDouble(),
  titleBarButtonAnimationDuration: Duration(
    microseconds: (json['titleBarButtonAnimationDuration'] as num).toInt(),
  ),
  dialogBorderColor: const ColorConverter().fromJson(
    json['dialogBorderColor'] as String,
  ),
  dialogBorderWidth: (json['dialogBorderWidth'] as num).toDouble(),
  dialogBorderRadius: (json['dialogBorderRadius'] as num).toDouble(),
  dialogShadowColor: const ColorConverter().fromJson(
    json['dialogShadowColor'] as String,
  ),
  dialogShadowBlurRadius: (json['dialogShadowBlurRadius'] as num).toDouble(),
  dialogShadowSpreadRadius: (json['dialogShadowSpreadRadius'] as num)
      .toDouble(),
  dialogShadowOffsetX: (json['dialogShadowOffsetX'] as num).toDouble(),
  dialogShadowOffsetY: (json['dialogShadowOffsetY'] as num).toDouble(),
  dialogBackgroundColor: const ColorConverter().fromJson(
    json['dialogBackgroundColor'] as String,
  ),
  modalOverlayColor: const ColorConverter().fromJson(
    json['modalOverlayColor'] as String,
  ),
  titleTextStyle: const TextStyleConverter().fromJson(
    json['titleTextStyle'] as Map<String, dynamic>?,
  ),
  toolWindowTitleTextStyle: const TextStyleConverter().fromJson(
    json['toolWindowTitleTextStyle'] as Map<String, dynamic>?,
  ),
);

Map<String, dynamic> _$DisplayThemeExtensionToJson(
  DisplayThemeExtension instance,
) => <String, dynamic>{
  'titleBarColor': const ColorConverter().toJson(instance.titleBarColor),
  'titleBarHoverColor': const ColorConverter().toJson(
    instance.titleBarHoverColor,
  ),
  'titleBarTextColor': const ColorConverter().toJson(
    instance.titleBarTextColor,
  ),
  'titleBarHeight': instance.titleBarHeight,
  'toolWindowTitleBarHeight': instance.toolWindowTitleBarHeight,
  'titleBarBorderRadius': instance.titleBarBorderRadius,
  'closeButtonColor': const ColorConverter().toJson(instance.closeButtonColor),
  'closeButtonHoverColor': const ColorConverter().toJson(
    instance.closeButtonHoverColor,
  ),
  'closeButtonHoverIconColor': const ColorConverter().toJson(
    instance.closeButtonHoverIconColor,
  ),
  'minimizeButtonColor': const ColorConverter().toJson(
    instance.minimizeButtonColor,
  ),
  'minimizeButtonHoverColor': const ColorConverter().toJson(
    instance.minimizeButtonHoverColor,
  ),
  'maximizeButtonColor': const ColorConverter().toJson(
    instance.maximizeButtonColor,
  ),
  'maximizeButtonHoverColor': const ColorConverter().toJson(
    instance.maximizeButtonHoverColor,
  ),
  'titleBarButtonSize': instance.titleBarButtonSize,
  'titleBarButtonBorderRadius': instance.titleBarButtonBorderRadius,
  'titleBarButtonAnimationDuration':
      instance.titleBarButtonAnimationDuration.inMicroseconds,
  'dialogBorderColor': const ColorConverter().toJson(
    instance.dialogBorderColor,
  ),
  'dialogBorderWidth': instance.dialogBorderWidth,
  'dialogBorderRadius': instance.dialogBorderRadius,
  'dialogShadowColor': const ColorConverter().toJson(
    instance.dialogShadowColor,
  ),
  'dialogShadowBlurRadius': instance.dialogShadowBlurRadius,
  'dialogShadowSpreadRadius': instance.dialogShadowSpreadRadius,
  'dialogShadowOffsetX': instance.dialogShadowOffsetX,
  'dialogShadowOffsetY': instance.dialogShadowOffsetY,
  'dialogBackgroundColor': const ColorConverter().toJson(
    instance.dialogBackgroundColor,
  ),
  'modalOverlayColor': const ColorConverter().toJson(
    instance.modalOverlayColor,
  ),
  'titleTextStyle': ?const TextStyleConverter().toJson(instance.titleTextStyle),
  'toolWindowTitleTextStyle': ?const TextStyleConverter().toJson(
    instance.toolWindowTitleTextStyle,
  ),
};
