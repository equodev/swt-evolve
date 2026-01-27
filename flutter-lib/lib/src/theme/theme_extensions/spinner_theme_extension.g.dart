// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'spinner_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SpinnerThemeExtension _$SpinnerThemeExtensionFromJson(
  Map<String, dynamic> json,
) => SpinnerThemeExtension(
  animationDuration: Duration(
    microseconds: (json['animationDuration'] as num).toInt(),
  ),
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  buttonBackgroundColor: const ColorConverter().fromJson(
    json['buttonBackgroundColor'] as String,
  ),
  buttonHoverColor: const ColorConverter().fromJson(
    json['buttonHoverColor'] as String,
  ),
  buttonPressedColor: const ColorConverter().fromJson(
    json['buttonPressedColor'] as String,
  ),
  textColor: const ColorConverter().fromJson(json['textColor'] as String),
  disabledTextColor: const ColorConverter().fromJson(
    json['disabledTextColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  focusedBorderColor: const ColorConverter().fromJson(
    json['focusedBorderColor'] as String,
  ),
  disabledBorderColor: const ColorConverter().fromJson(
    json['disabledBorderColor'] as String,
  ),
  iconColor: const ColorConverter().fromJson(json['iconColor'] as String),
  iconHoverColor: const ColorConverter().fromJson(
    json['iconHoverColor'] as String,
  ),
  disabledIconColor: const ColorConverter().fromJson(
    json['disabledIconColor'] as String,
  ),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  minWidth: (json['minWidth'] as num).toDouble(),
  minHeight: (json['minHeight'] as num).toDouble(),
  buttonWidth: (json['buttonWidth'] as num).toDouble(),
  iconSize: (json['iconSize'] as num).toDouble(),
  textFieldPadding: const EdgeInsetsConverter().fromJson(
    json['textFieldPadding'] as Map<String, dynamic>,
  ),
  textStyle: const TextStyleConverter().fromJson(
    json['textStyle'] as Map<String, dynamic>?,
  ),
  defaultMinimum: (json['defaultMinimum'] as num).toInt(),
  defaultMaximum: (json['defaultMaximum'] as num).toInt(),
  defaultSelection: (json['defaultSelection'] as num).toInt(),
  defaultIncrement: (json['defaultIncrement'] as num).toInt(),
  defaultPageIncrement: (json['defaultPageIncrement'] as num).toInt(),
  defaultDigits: (json['defaultDigits'] as num).toInt(),
  defaultTextLimit: (json['defaultTextLimit'] as num).toInt(),
);

Map<String, dynamic> _$SpinnerThemeExtensionToJson(
  SpinnerThemeExtension instance,
) => <String, dynamic>{
  'animationDuration': instance.animationDuration.inMicroseconds,
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'buttonBackgroundColor': const ColorConverter().toJson(
    instance.buttonBackgroundColor,
  ),
  'buttonHoverColor': const ColorConverter().toJson(instance.buttonHoverColor),
  'buttonPressedColor': const ColorConverter().toJson(
    instance.buttonPressedColor,
  ),
  'textColor': const ColorConverter().toJson(instance.textColor),
  'disabledTextColor': const ColorConverter().toJson(
    instance.disabledTextColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'focusedBorderColor': const ColorConverter().toJson(
    instance.focusedBorderColor,
  ),
  'disabledBorderColor': const ColorConverter().toJson(
    instance.disabledBorderColor,
  ),
  'iconColor': const ColorConverter().toJson(instance.iconColor),
  'iconHoverColor': const ColorConverter().toJson(instance.iconHoverColor),
  'disabledIconColor': const ColorConverter().toJson(
    instance.disabledIconColor,
  ),
  'borderWidth': instance.borderWidth,
  'borderRadius': instance.borderRadius,
  'minWidth': instance.minWidth,
  'minHeight': instance.minHeight,
  'buttonWidth': instance.buttonWidth,
  'iconSize': instance.iconSize,
  'textFieldPadding': const EdgeInsetsConverter().toJson(
    instance.textFieldPadding,
  ),
  'textStyle': const TextStyleConverter().toJson(instance.textStyle),
  'defaultMinimum': instance.defaultMinimum,
  'defaultMaximum': instance.defaultMaximum,
  'defaultSelection': instance.defaultSelection,
  'defaultIncrement': instance.defaultIncrement,
  'defaultPageIncrement': instance.defaultPageIncrement,
  'defaultDigits': instance.defaultDigits,
  'defaultTextLimit': instance.defaultTextLimit,
};
