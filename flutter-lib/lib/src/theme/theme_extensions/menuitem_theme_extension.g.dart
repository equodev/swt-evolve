// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'menuitem_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MenuItemThemeExtension _$MenuItemThemeExtensionFromJson(
  Map<String, dynamic> json,
) => MenuItemThemeExtension(
  animationDuration: Duration(
    microseconds: (json['animationDuration'] as num).toInt(),
  ),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  hoverBackgroundColor: const ColorConverter().fromJson(
    json['hoverBackgroundColor'] as String,
  ),
  selectedBackgroundColor: const ColorConverter().fromJson(
    json['selectedBackgroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  textColor: const ColorConverter().fromJson(json['textColor'] as String),
  disabledTextColor: const ColorConverter().fromJson(
    json['disabledTextColor'] as String,
  ),
  acceleratorTextColor: const ColorConverter().fromJson(
    json['acceleratorTextColor'] as String,
  ),
  iconColor: const ColorConverter().fromJson(json['iconColor'] as String),
  disabledIconColor: const ColorConverter().fromJson(
    json['disabledIconColor'] as String,
  ),
  separatorColor: const ColorConverter().fromJson(
    json['separatorColor'] as String,
  ),
  checkboxColor: const ColorConverter().fromJson(
    json['checkboxColor'] as String,
  ),
  checkboxSelectedColor: const ColorConverter().fromJson(
    json['checkboxSelectedColor'] as String,
  ),
  checkboxHoverColor: const ColorConverter().fromJson(
    json['checkboxHoverColor'] as String,
  ),
  checkboxBorderColor: const ColorConverter().fromJson(
    json['checkboxBorderColor'] as String,
  ),
  checkboxCheckmarkColor: const ColorConverter().fromJson(
    json['checkboxCheckmarkColor'] as String,
  ),
  radioButtonColor: const ColorConverter().fromJson(
    json['radioButtonColor'] as String,
  ),
  radioButtonSelectedColor: const ColorConverter().fromJson(
    json['radioButtonSelectedColor'] as String,
  ),
  radioButtonHoverColor: const ColorConverter().fromJson(
    json['radioButtonHoverColor'] as String,
  ),
  radioButtonSelectedHoverColor: const ColorConverter().fromJson(
    json['radioButtonSelectedHoverColor'] as String,
  ),
  radioButtonBorderColor: const ColorConverter().fromJson(
    json['radioButtonBorderColor'] as String,
  ),
  radioButtonInnerColor: const ColorConverter().fromJson(
    json['radioButtonInnerColor'] as String,
  ),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  itemHeight: (json['itemHeight'] as num).toDouble(),
  minItemWidth: (json['minItemWidth'] as num).toDouble(),
  iconSize: (json['iconSize'] as num).toDouble(),
  separatorHeight: (json['separatorHeight'] as num).toDouble(),
  separatorMargin: (json['separatorMargin'] as num).toDouble(),
  itemPadding: const EdgeInsetsConverter().fromJson(
    json['itemPadding'] as Map<String, dynamic>,
  ),
  iconTextSpacing: (json['iconTextSpacing'] as num).toDouble(),
  textAcceleratorSpacing: (json['textAcceleratorSpacing'] as num).toDouble(),
  checkboxSize: (json['checkboxSize'] as num).toDouble(),
  checkboxBorderRadius: (json['checkboxBorderRadius'] as num).toDouble(),
  checkboxBorderWidth: (json['checkboxBorderWidth'] as num).toDouble(),
  checkboxCheckmarkSize: (json['checkboxCheckmarkSize'] as num).toDouble(),
  radioButtonSize: (json['radioButtonSize'] as num).toDouble(),
  radioButtonBorderWidth: (json['radioButtonBorderWidth'] as num).toDouble(),
  radioButtonInnerSize: (json['radioButtonInnerSize'] as num).toDouble(),
  disabledOpacity: (json['disabledOpacity'] as num).toDouble(),
  textStyle: const TextStyleConverter().fromJson(
    json['textStyle'] as Map<String, dynamic>?,
  ),
  acceleratorTextStyle: const TextStyleConverter().fromJson(
    json['acceleratorTextStyle'] as Map<String, dynamic>?,
  ),
);

Map<String, dynamic> _$MenuItemThemeExtensionToJson(
  MenuItemThemeExtension instance,
) => <String, dynamic>{
  'animationDuration': instance.animationDuration.inMicroseconds,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'hoverBackgroundColor': const ColorConverter().toJson(
    instance.hoverBackgroundColor,
  ),
  'selectedBackgroundColor': const ColorConverter().toJson(
    instance.selectedBackgroundColor,
  ),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'textColor': const ColorConverter().toJson(instance.textColor),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'acceleratorTextColor': const ColorConverter().toJson(
    instance.acceleratorTextColor,
  ),
  'iconColor': const ColorConverter().toJson(instance.iconColor),
  'disabledIconColor': const ColorConverter().toJson(
    instance.disabledIconColor,
  ),
  'separatorColor': const ColorConverter().toJson(instance.separatorColor),
  'checkboxColor': const ColorConverter().toJson(instance.checkboxColor),
  'checkboxSelectedColor': const ColorConverter().toJson(
    instance.checkboxSelectedColor,
  ),
  'checkboxHoverColor': const ColorConverter().toJson(
    instance.checkboxHoverColor,
  ),
  'checkboxBorderColor': const ColorConverter().toJson(
    instance.checkboxBorderColor,
  ),
  'checkboxCheckmarkColor': const ColorConverter().toJson(
    instance.checkboxCheckmarkColor,
  ),
  'radioButtonColor': const ColorConverter().toJson(instance.radioButtonColor),
  'radioButtonSelectedColor': const ColorConverter().toJson(
    instance.radioButtonSelectedColor,
  ),
  'radioButtonHoverColor': const ColorConverter().toJson(
    instance.radioButtonHoverColor,
  ),
  'radioButtonSelectedHoverColor': const ColorConverter().toJson(
    instance.radioButtonSelectedHoverColor,
  ),
  'radioButtonBorderColor': const ColorConverter().toJson(
    instance.radioButtonBorderColor,
  ),
  'radioButtonInnerColor': const ColorConverter().toJson(
    instance.radioButtonInnerColor,
  ),
  'borderRadius': instance.borderRadius,
  'itemHeight': instance.itemHeight,
  'minItemWidth': instance.minItemWidth,
  'iconSize': instance.iconSize,
  'separatorHeight': instance.separatorHeight,
  'separatorMargin': instance.separatorMargin,
  'itemPadding': const EdgeInsetsConverter().toJson(instance.itemPadding),
  'iconTextSpacing': instance.iconTextSpacing,
  'textAcceleratorSpacing': instance.textAcceleratorSpacing,
  'checkboxSize': instance.checkboxSize,
  'checkboxBorderRadius': instance.checkboxBorderRadius,
  'checkboxBorderWidth': instance.checkboxBorderWidth,
  'checkboxCheckmarkSize': instance.checkboxCheckmarkSize,
  'radioButtonSize': instance.radioButtonSize,
  'radioButtonBorderWidth': instance.radioButtonBorderWidth,
  'radioButtonInnerSize': instance.radioButtonInnerSize,
  'disabledOpacity': instance.disabledOpacity,
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
  'acceleratorTextStyle': const TextStyleConverter().toJson(
    instance.acceleratorTextStyle,
  ),
};
