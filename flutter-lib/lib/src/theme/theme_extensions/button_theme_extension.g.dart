// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'button_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ButtonThemeExtension _$ButtonThemeExtensionFromJson(
  Map<String, dynamic> json,
) => ButtonThemeExtension(
  buttonPressDelay: Duration(
    microseconds: (json['buttonPressDelay'] as num).toInt(),
  ),
  enableTapAnimation: json['enableTapAnimation'] as bool,
  splashColor: const ColorConverter().fromJson(json['splashColor'] as String),
  highlightColor: const ColorConverter().fromJson(
    json['highlightColor'] as String,
  ),
  pushButtonColor: const ColorConverter().fromJson(
    json['pushButtonColor'] as String,
  ),
  selectableButtonColor: const ColorConverter().fromJson(
    json['selectableButtonColor'] as String,
  ),
  toggleButtonColor: const ColorConverter().fromJson(
    json['toggleButtonColor'] as String,
  ),
  toggleButtonBorderColor: const ColorConverter().fromJson(
    json['toggleButtonBorderColor'] as String,
  ),
  dropdownButtonColor: const ColorConverter().fromJson(
    json['dropdownButtonColor'] as String,
  ),
  checkboxColor: const ColorConverter().fromJson(
    json['checkboxColor'] as String,
  ),
  pushButtonTextColor: const ColorConverter().fromJson(
    json['pushButtonTextColor'] as String,
  ),
  pushButtonHoverColor: const ColorConverter().fromJson(
    json['pushButtonHoverColor'] as String,
  ),
  pushButtonPressedColor: const ColorConverter().fromJson(
    json['pushButtonPressedColor'] as String,
  ),
  pushButtonDisabledColor: const ColorConverter().fromJson(
    json['pushButtonDisabledColor'] as String,
  ),
  pushButtonBorderColor: const ColorConverter().fromJson(
    json['pushButtonBorderColor'] as String,
  ),
  secondaryButtonColor: const ColorConverter().fromJson(
    json['secondaryButtonColor'] as String,
  ),
  secondaryButtonHoverColor: const ColorConverter().fromJson(
    json['secondaryButtonHoverColor'] as String,
  ),
  secondaryButtonPressedColor: const ColorConverter().fromJson(
    json['secondaryButtonPressedColor'] as String,
  ),
  secondaryButtonTextColor: const ColorConverter().fromJson(
    json['secondaryButtonTextColor'] as String,
  ),
  secondaryButtonBorderColor: const ColorConverter().fromJson(
    json['secondaryButtonBorderColor'] as String,
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
  radioButtonTextColor: const ColorConverter().fromJson(
    json['radioButtonTextColor'] as String,
  ),
  dropdownButtonTextColor: const ColorConverter().fromJson(
    json['dropdownButtonTextColor'] as String,
  ),
  dropdownButtonHoverColor: const ColorConverter().fromJson(
    json['dropdownButtonHoverColor'] as String,
  ),
  dropdownButtonBorderColor: const ColorConverter().fromJson(
    json['dropdownButtonBorderColor'] as String,
  ),
  dropdownButtonIconColor: const ColorConverter().fromJson(
    json['dropdownButtonIconColor'] as String,
  ),
  checkboxSelectedColor: const ColorConverter().fromJson(
    json['checkboxSelectedColor'] as String,
  ),
  checkboxBorderColor: const ColorConverter().fromJson(
    json['checkboxBorderColor'] as String,
  ),
  checkboxCheckmarkColor: const ColorConverter().fromJson(
    json['checkboxCheckmarkColor'] as String,
  ),
  checkboxTextColor: const ColorConverter().fromJson(
    json['checkboxTextColor'] as String,
  ),
  checkboxHoverColor: const ColorConverter().fromJson(
    json['checkboxHoverColor'] as String,
  ),
  pushButtonBorderRadius: (json['pushButtonBorderRadius'] as num).toDouble(),
  radioButtonBorderRadius: (json['radioButtonBorderRadius'] as num).toDouble(),
  dropdownButtonBorderRadius: (json['dropdownButtonBorderRadius'] as num)
      .toDouble(),
  checkboxBorderRadius: (json['checkboxBorderRadius'] as num).toDouble(),
  checkboxGrayedBorderRadius: (json['checkboxGrayedBorderRadius'] as num)
      .toDouble(),
  pushButtonBorderWidth: (json['pushButtonBorderWidth'] as num).toDouble(),
  radioButtonBorderWidth: (json['radioButtonBorderWidth'] as num).toDouble(),
  radioButtonSelectedBorderWidth:
      (json['radioButtonSelectedBorderWidth'] as num).toDouble(),
  dropdownButtonBorderWidth: (json['dropdownButtonBorderWidth'] as num)
      .toDouble(),
  checkboxBorderWidth: (json['checkboxBorderWidth'] as num).toDouble(),
  pushButtonFontStyle: const TextStyleConverter().fromJson(
    json['pushButtonFontStyle'] as Map<String, dynamic>?,
  ),
  radioButtonFontStyle: const TextStyleConverter().fromJson(
    json['radioButtonFontStyle'] as Map<String, dynamic>?,
  ),
  dropdownButtonFontStyle: const TextStyleConverter().fromJson(
    json['dropdownButtonFontStyle'] as Map<String, dynamic>?,
  ),
  checkboxFontStyle: const TextStyleConverter().fromJson(
    json['checkboxFontStyle'] as Map<String, dynamic>?,
  ),
  radioButtonSize: (json['radioButtonSize'] as num).toDouble(),
  radioButtonInnerSize: (json['radioButtonInnerSize'] as num).toDouble(),
  radioButtonTextSpacing: (json['radioButtonTextSpacing'] as num).toDouble(),
  dropdownButtonIconSize: (json['dropdownButtonIconSize'] as num).toDouble(),
  checkboxSize: (json['checkboxSize'] as num).toDouble(),
  checkboxCheckmarkSizeMultiplier:
      (json['checkboxCheckmarkSizeMultiplier'] as num).toDouble(),
  checkboxGrayedMarginMultiplier:
      (json['checkboxGrayedMarginMultiplier'] as num).toDouble(),
  imageTextSpacing: (json['imageTextSpacing'] as num).toDouble(),
  pushButtonPadding: const EdgeInsetsConverter().fromJson(
    json['pushButtonPadding'] as Map<String, dynamic>,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  disabledForegroundColor: const ColorConverter().fromJson(
    json['disabledForegroundColor'] as String,
  ),
);

Map<String, dynamic> _$ButtonThemeExtensionToJson(
  ButtonThemeExtension instance,
) => <String, dynamic>{
  'buttonPressDelay': instance.buttonPressDelay.inMicroseconds,
  'enableTapAnimation': instance.enableTapAnimation,
  'splashColor': const ColorConverter().toJson(instance.splashColor),
  'highlightColor': const ColorConverter().toJson(instance.highlightColor),
  'pushButtonColor': const ColorConverter().toJson(instance.pushButtonColor),
  'selectableButtonColor': const ColorConverter().toJson(
    instance.selectableButtonColor,
  ),
  'toggleButtonColor': const ColorConverter().toJson(
    instance.toggleButtonColor,
  ),
  'toggleButtonBorderColor': const ColorConverter().toJson(
    instance.toggleButtonBorderColor,
  ),
  'dropdownButtonColor': const ColorConverter().toJson(
    instance.dropdownButtonColor,
  ),
  'checkboxColor': const ColorConverter().toJson(instance.checkboxColor),
  'pushButtonTextColor': const ColorConverter().toJson(
    instance.pushButtonTextColor,
  ),
  'pushButtonHoverColor': const ColorConverter().toJson(
    instance.pushButtonHoverColor,
  ),
  'pushButtonPressedColor': const ColorConverter().toJson(
    instance.pushButtonPressedColor,
  ),
  'pushButtonDisabledColor': const ColorConverter().toJson(
    instance.pushButtonDisabledColor,
  ),
  'pushButtonBorderColor': const ColorConverter().toJson(
    instance.pushButtonBorderColor,
  ),
  'secondaryButtonColor': const ColorConverter().toJson(
    instance.secondaryButtonColor,
  ),
  'secondaryButtonHoverColor': const ColorConverter().toJson(
    instance.secondaryButtonHoverColor,
  ),
  'secondaryButtonPressedColor': const ColorConverter().toJson(
    instance.secondaryButtonPressedColor,
  ),
  'secondaryButtonTextColor': const ColorConverter().toJson(
    instance.secondaryButtonTextColor,
  ),
  'secondaryButtonBorderColor': const ColorConverter().toJson(
    instance.secondaryButtonBorderColor,
  ),
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
  'radioButtonTextColor': const ColorConverter().toJson(
    instance.radioButtonTextColor,
  ),
  'dropdownButtonTextColor': const ColorConverter().toJson(
    instance.dropdownButtonTextColor,
  ),
  'dropdownButtonHoverColor': const ColorConverter().toJson(
    instance.dropdownButtonHoverColor,
  ),
  'dropdownButtonBorderColor': const ColorConverter().toJson(
    instance.dropdownButtonBorderColor,
  ),
  'dropdownButtonIconColor': const ColorConverter().toJson(
    instance.dropdownButtonIconColor,
  ),
  'checkboxSelectedColor': const ColorConverter().toJson(
    instance.checkboxSelectedColor,
  ),
  'checkboxBorderColor': const ColorConverter().toJson(
    instance.checkboxBorderColor,
  ),
  'checkboxCheckmarkColor': const ColorConverter().toJson(
    instance.checkboxCheckmarkColor,
  ),
  'checkboxTextColor': const ColorConverter().toJson(
    instance.checkboxTextColor,
  ),
  'checkboxHoverColor': const ColorConverter().toJson(
    instance.checkboxHoverColor,
  ),
  'pushButtonBorderRadius': instance.pushButtonBorderRadius,
  'radioButtonBorderRadius': instance.radioButtonBorderRadius,
  'dropdownButtonBorderRadius': instance.dropdownButtonBorderRadius,
  'checkboxBorderRadius': instance.checkboxBorderRadius,
  'checkboxGrayedBorderRadius': instance.checkboxGrayedBorderRadius,
  'pushButtonBorderWidth': instance.pushButtonBorderWidth,
  'radioButtonBorderWidth': instance.radioButtonBorderWidth,
  'radioButtonSelectedBorderWidth': instance.radioButtonSelectedBorderWidth,
  'dropdownButtonBorderWidth': instance.dropdownButtonBorderWidth,
  'checkboxBorderWidth': instance.checkboxBorderWidth,
  'pushButtonFontStyle': const TextStyleConverter().toJson(
    instance.pushButtonFontStyle,
  ),
  'radioButtonFontStyle': const TextStyleConverter().toJson(
    instance.radioButtonFontStyle,
  ),
  'dropdownButtonFontStyle': const TextStyleConverter().toJson(
    instance.dropdownButtonFontStyle,
  ),
  'checkboxFontStyle': const TextStyleConverter().toJson(
    instance.checkboxFontStyle,
  ),
  'radioButtonSize': instance.radioButtonSize,
  'radioButtonInnerSize': instance.radioButtonInnerSize,
  'radioButtonTextSpacing': instance.radioButtonTextSpacing,
  'dropdownButtonIconSize': instance.dropdownButtonIconSize,
  'checkboxSize': instance.checkboxSize,
  'checkboxCheckmarkSizeMultiplier': instance.checkboxCheckmarkSizeMultiplier,
  'checkboxGrayedMarginMultiplier': instance.checkboxGrayedMarginMultiplier,
  'imageTextSpacing': instance.imageTextSpacing,
  'pushButtonPadding': const EdgeInsetsConverter().toJson(
    instance.pushButtonPadding,
  ),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'disabledForegroundColor': const ColorConverter().toJson(
    instance.disabledForegroundColor,
  ),
};
