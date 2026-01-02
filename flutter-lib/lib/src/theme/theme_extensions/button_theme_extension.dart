import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'button_theme_extension.tailor.dart';
part 'button_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
class ButtonThemeExtension extends ThemeExtension<ButtonThemeExtension> with _$ButtonThemeExtensionTailorMixin {
  final Duration buttonPressDelay;
  final bool enableTapAnimation;
  
  final Color splashColor;
  final Color highlightColor;
 
  final Color pushButtonColor;
  final Color selectableButtonColor;
  final Color toggleButtonColor;
  final Color toggleButtonBorderColor;
  final Color dropdownButtonColor;
  final Color checkboxColor;
  
  // Push Button colors
  final Color pushButtonTextColor;
  final Color pushButtonHoverColor;
  final Color pushButtonPressedColor;
  final Color pushButtonDisabledColor;
  final Color pushButtonBorderColor;
  
  // Secondary Button colors
  final Color secondaryButtonColor;
  final Color secondaryButtonHoverColor;
  final Color secondaryButtonPressedColor;
  final Color secondaryButtonTextColor;
  final Color secondaryButtonBorderColor;
  
  // Radio Button colors
  final Color radioButtonSelectedColor;
  final Color radioButtonHoverColor;
  final Color radioButtonSelectedHoverColor;
  final Color radioButtonBorderColor;
  final Color radioButtonTextColor;
  
  // Dropdown Button colors
  final Color dropdownButtonTextColor;
  final Color dropdownButtonHoverColor;
  final Color dropdownButtonBorderColor;
  final Color dropdownButtonIconColor;
  
  // Checkbox colors
  final Color checkboxSelectedColor;
  final Color checkboxBorderColor;
  final Color checkboxCheckmarkColor;
  final Color checkboxTextColor;
  final Color checkboxHoverColor;
  
  // Border radius
  final double pushButtonBorderRadius;
  final double radioButtonBorderRadius;
  final double dropdownButtonBorderRadius;
  final double checkboxBorderRadius;
  final double checkboxGrayedBorderRadius;
  
  // Border width
  final double pushButtonBorderWidth;
  final double radioButtonBorderWidth;
  final double radioButtonSelectedBorderWidth;
  final double dropdownButtonBorderWidth;
  final double checkboxBorderWidth;
  
  // Font styles
  final TextStyle? pushButtonFontStyle;
  
  final TextStyle? radioButtonFontStyle;
  
  final TextStyle? dropdownButtonFontStyle;
  
  final TextStyle? checkboxFontStyle;
  
  // Radio Button sizes
  final double radioButtonSize;
  final double radioButtonInnerSize;
  final double radioButtonTextSpacing;
  
  // Dropdown Button sizes
  final double dropdownButtonIconSize;
  
  // Checkbox sizes
  final double checkboxCheckmarkSizeMultiplier;
  final double checkboxGrayedMarginMultiplier;
  
  // Common sizes
  final double imageTextSpacing;
  
  // Disabled colors
  final Color disabledBackgroundColor;
  final Color disabledForegroundColor;
  
  const ButtonThemeExtension({
    required this.buttonPressDelay,
    required this.enableTapAnimation,
    required this.splashColor,
    required this.highlightColor,
    required this.pushButtonColor,
    required this.selectableButtonColor,
    required this.toggleButtonColor,
    required this.toggleButtonBorderColor,
    required this.dropdownButtonColor,
    required this.checkboxColor,
    required this.pushButtonTextColor,
    required this.pushButtonHoverColor,
    required this.pushButtonPressedColor,
    required this.pushButtonDisabledColor,
    required this.pushButtonBorderColor,
    required this.secondaryButtonColor,
    required this.secondaryButtonHoverColor,
    required this.secondaryButtonPressedColor,
    required this.secondaryButtonTextColor,
    required this.secondaryButtonBorderColor,
    required this.radioButtonSelectedColor,
    required this.radioButtonHoverColor,
    required this.radioButtonSelectedHoverColor,
    required this.radioButtonBorderColor,
    required this.radioButtonTextColor,
    required this.dropdownButtonTextColor,
    required this.dropdownButtonHoverColor,
    required this.dropdownButtonBorderColor,
    required this.dropdownButtonIconColor,
    required this.checkboxSelectedColor,
    required this.checkboxBorderColor,
    required this.checkboxCheckmarkColor,
    required this.checkboxTextColor,
    required this.checkboxHoverColor,
    required this.pushButtonBorderRadius,
    required this.radioButtonBorderRadius,
    required this.dropdownButtonBorderRadius,
    required this.checkboxBorderRadius,
    required this.checkboxGrayedBorderRadius,
    required this.pushButtonBorderWidth,
    required this.radioButtonBorderWidth,
    required this.radioButtonSelectedBorderWidth,
    required this.dropdownButtonBorderWidth,
    required this.checkboxBorderWidth,
    this.pushButtonFontStyle,
    this.radioButtonFontStyle,
    this.dropdownButtonFontStyle,
    this.checkboxFontStyle,
    required this.radioButtonSize,
    required this.radioButtonInnerSize,
    required this.radioButtonTextSpacing,
    required this.dropdownButtonIconSize,
    required this.checkboxCheckmarkSizeMultiplier,
    required this.checkboxGrayedMarginMultiplier,
    required this.imageTextSpacing,
    required this.disabledBackgroundColor,
    required this.disabledForegroundColor,
  });

  factory ButtonThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ButtonThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ButtonThemeExtensionToJson(this);
}

