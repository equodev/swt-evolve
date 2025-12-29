import 'package:flutter/material.dart';

class ButtonThemeExtension extends ThemeExtension<ButtonThemeExtension> {
  final Duration buttonPressDelay;
  final bool enableTapAnimation;
 
  final Color? pushButtonColor;
  final Color? selectableButtonColor;
  final Color? dropdownButtonColor;
  final Color? checkboxColor;
  
  // Push Button colors
  final Color pushButtonTextColor;
  final Color pushButtonHoverColor;
  final Color pushButtonDisabledColor;
  final Color pushButtonBorderColor;
  
  // Secondary Button colors
  final Color secondaryButtonColor;
  final Color secondaryButtonHoverColor;
  final Color secondaryButtonTextColor;
  final Color secondaryButtonBorderColor;
  
  // Radio Button colors
  final Color radioButtonSelectedColor;
  final Color radioButtonHoverColor;
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
  
  // Push Button dimensions
  final double pushButtonHeight;
  final double pushButtonMinWidth;
  final double pushButtonBorderRadius;
  final double pushButtonBorderWidth;
  final EdgeInsets pushButtonPadding;
  final double pushButtonElevation;
  final double pushButtonFontSize;
  final FontWeight pushButtonFontWeight;
  final String? pushButtonFontFamily;
  final double pushButtonLetterSpacing;
  
  // Radio Button dimensions
  final double radioButtonSize;
  final double radioButtonInnerSize;
  final double radioButtonBorderWidth;
  final double radioButtonSelectedBorderWidth;
  final EdgeInsets radioButtonPadding;
  final double radioButtonTextSpacing;
  final bool radioButtonHoverWhole;
  final double radioButtonFontSize;
  final FontWeight radioButtonFontWeight;
  final String? radioButtonFontFamily;
  final double radioButtonLetterSpacing;
  
  // Dropdown Button dimensions
  final double dropdownButtonHeight;
  final double dropdownButtonMinWidth;
  final double dropdownButtonBorderRadius;
  final double dropdownButtonBorderWidth;
  final EdgeInsets dropdownButtonPadding;
  final double dropdownButtonIconSize;
  final double dropdownButtonFontSize;
  final FontWeight dropdownButtonFontWeight;
  final String? dropdownButtonFontFamily;
  final double dropdownButtonLetterSpacing;
  
  // Checkbox dimensions
  final double checkboxSize;
  final double checkboxBorderRadius;
  final double checkboxBorderWidth;
  final EdgeInsets checkboxPadding;
  final double checkboxTextSpacing;
  final double checkboxFontSize;
  final FontWeight checkboxFontWeight;
  final String? checkboxFontFamily;
  final double checkboxLetterSpacing;
  final double checkboxCheckmarkSizeMultiplier;
  final double checkboxGrayedMarginMultiplier;
  final double checkboxGrayedBorderRadius;
  
  // Common values
  final double disabledBackgroundOpacity;
  final double disabledForegroundOpacity;
  final double imageTextSpacing;
  final double buttonImageSizeMultiplier;
  
  const ButtonThemeExtension({
    required this.buttonPressDelay,
    required this.enableTapAnimation,
    this.pushButtonColor,
    this.selectableButtonColor,
    this.dropdownButtonColor,
    this.checkboxColor,
    required this.pushButtonTextColor,
    required this.pushButtonHoverColor,
    required this.pushButtonDisabledColor,
    required this.pushButtonBorderColor,
    required this.secondaryButtonColor,
    required this.secondaryButtonHoverColor,
    required this.secondaryButtonTextColor,
    required this.secondaryButtonBorderColor,
    required this.radioButtonSelectedColor,
    required this.radioButtonHoverColor,
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
    required this.pushButtonHeight,
    required this.pushButtonMinWidth,
    required this.pushButtonBorderRadius,
    required this.pushButtonBorderWidth,
    required this.pushButtonPadding,
    required this.pushButtonElevation,
    required this.pushButtonFontSize,
    required this.pushButtonFontWeight,
    this.pushButtonFontFamily,
    required this.pushButtonLetterSpacing,
    required this.radioButtonSize,
    required this.radioButtonInnerSize,
    required this.radioButtonBorderWidth,
    required this.radioButtonSelectedBorderWidth,
    required this.radioButtonPadding,
    required this.radioButtonTextSpacing,
    required this.radioButtonHoverWhole,
    required this.radioButtonFontSize,
    required this.radioButtonFontWeight,
    this.radioButtonFontFamily,
    required this.radioButtonLetterSpacing,
    required this.dropdownButtonHeight,
    required this.dropdownButtonMinWidth,
    required this.dropdownButtonBorderRadius,
    required this.dropdownButtonBorderWidth,
    required this.dropdownButtonPadding,
    required this.dropdownButtonIconSize,
    required this.dropdownButtonFontSize,
    required this.dropdownButtonFontWeight,
    this.dropdownButtonFontFamily,
    required this.dropdownButtonLetterSpacing,
    required this.checkboxSize,
    required this.checkboxBorderRadius,
    required this.checkboxBorderWidth,
    required this.checkboxPadding,
    required this.checkboxTextSpacing,
    required this.checkboxFontSize,
    required this.checkboxFontWeight,
    this.checkboxFontFamily,
    required this.checkboxLetterSpacing,
    required this.checkboxCheckmarkSizeMultiplier,
    required this.checkboxGrayedMarginMultiplier,
    required this.checkboxGrayedBorderRadius,
    required this.disabledBackgroundOpacity,
    required this.disabledForegroundOpacity,
    required this.imageTextSpacing,
    required this.buttonImageSizeMultiplier,
  });

  @override
  ThemeExtension<ButtonThemeExtension> copyWith({
    Duration? buttonPressDelay,
    bool? enableTapAnimation,
    Color? pushButtonColor,
    Color? selectableButtonColor,
    Color? dropdownButtonColor,
    Color? checkboxColor,
    Color? pushButtonTextColor,
    Color? pushButtonHoverColor,
    Color? pushButtonDisabledColor,
    Color? pushButtonBorderColor,
    Color? secondaryButtonColor,
    Color? secondaryButtonHoverColor,
    Color? secondaryButtonTextColor,
    Color? secondaryButtonBorderColor,
    Color? radioButtonSelectedColor,
    Color? radioButtonHoverColor,
    Color? radioButtonBorderColor,
    Color? radioButtonTextColor,
    Color? dropdownButtonTextColor,
    Color? dropdownButtonHoverColor,
    Color? dropdownButtonBorderColor,
    Color? dropdownButtonIconColor,
    Color? checkboxSelectedColor,
    Color? checkboxBorderColor,
    Color? checkboxCheckmarkColor,
    Color? checkboxTextColor,
    Color? checkboxHoverColor,
    double? pushButtonHeight,
    double? pushButtonMinWidth,
    double? pushButtonBorderRadius,
    double? pushButtonBorderWidth,
    EdgeInsets? pushButtonPadding,
    double? pushButtonElevation,
    double? pushButtonFontSize,
    FontWeight? pushButtonFontWeight,
    String? pushButtonFontFamily,
    double? pushButtonLetterSpacing,
    double? radioButtonSize,
    double? radioButtonInnerSize,
    double? radioButtonBorderWidth,
    double? radioButtonSelectedBorderWidth,
    EdgeInsets? radioButtonPadding,
    double? radioButtonTextSpacing,
    bool? radioButtonHoverWhole,
    double? radioButtonFontSize,
    FontWeight? radioButtonFontWeight,
    String? radioButtonFontFamily,
    double? dropdownButtonHeight,
    double? dropdownButtonMinWidth,
    double? dropdownButtonBorderRadius,
    double? dropdownButtonBorderWidth,
    EdgeInsets? dropdownButtonPadding,
    double? dropdownButtonIconSize,
    double? dropdownButtonFontSize,
    FontWeight? dropdownButtonFontWeight,
    String? dropdownButtonFontFamily,
    double? dropdownButtonLetterSpacing,
    double? checkboxSize,
    double? checkboxBorderRadius,
    double? checkboxBorderWidth,
    EdgeInsets? checkboxPadding,
    double? checkboxTextSpacing,
    double? checkboxFontSize,
    FontWeight? checkboxFontWeight,
    String? checkboxFontFamily,
    double? checkboxLetterSpacing,
    double? checkboxCheckmarkSizeMultiplier,
    double? checkboxGrayedMarginMultiplier,
    double? checkboxGrayedBorderRadius,
    double? disabledBackgroundOpacity,
    double? disabledForegroundOpacity,
    double? imageTextSpacing,
    double? buttonImageSizeMultiplier,
    double? radioButtonLetterSpacing,
  }) {
    return ButtonThemeExtension(
      buttonPressDelay: buttonPressDelay ?? this.buttonPressDelay,
      enableTapAnimation: enableTapAnimation ?? this.enableTapAnimation,
      pushButtonColor: pushButtonColor ?? this.pushButtonColor,
      selectableButtonColor: selectableButtonColor ?? this.selectableButtonColor,
      dropdownButtonColor: dropdownButtonColor ?? this.dropdownButtonColor,
      checkboxColor: checkboxColor ?? this.checkboxColor,
      pushButtonTextColor: pushButtonTextColor ?? this.pushButtonTextColor,
      pushButtonHoverColor: pushButtonHoverColor ?? this.pushButtonHoverColor,
      pushButtonDisabledColor: pushButtonDisabledColor ?? this.pushButtonDisabledColor,
      pushButtonBorderColor: pushButtonBorderColor ?? this.pushButtonBorderColor,
      secondaryButtonColor: secondaryButtonColor ?? this.secondaryButtonColor,
      secondaryButtonHoverColor: secondaryButtonHoverColor ?? this.secondaryButtonHoverColor,
      secondaryButtonTextColor: secondaryButtonTextColor ?? this.secondaryButtonTextColor,
      secondaryButtonBorderColor: secondaryButtonBorderColor ?? this.secondaryButtonBorderColor,
      radioButtonSelectedColor: radioButtonSelectedColor ?? this.radioButtonSelectedColor,
      radioButtonHoverColor: radioButtonHoverColor ?? this.radioButtonHoverColor,
      radioButtonBorderColor: radioButtonBorderColor ?? this.radioButtonBorderColor,
      radioButtonTextColor: radioButtonTextColor ?? this.radioButtonTextColor,
      dropdownButtonTextColor: dropdownButtonTextColor ?? this.dropdownButtonTextColor,
      dropdownButtonHoverColor: dropdownButtonHoverColor ?? this.dropdownButtonHoverColor,
      dropdownButtonBorderColor: dropdownButtonBorderColor ?? this.dropdownButtonBorderColor,
      dropdownButtonIconColor: dropdownButtonIconColor ?? this.dropdownButtonIconColor,
      checkboxSelectedColor: checkboxSelectedColor ?? this.checkboxSelectedColor,
      checkboxBorderColor: checkboxBorderColor ?? this.checkboxBorderColor,
      checkboxCheckmarkColor: checkboxCheckmarkColor ?? this.checkboxCheckmarkColor,
      checkboxTextColor: checkboxTextColor ?? this.checkboxTextColor,
      checkboxHoverColor: checkboxHoverColor ?? this.checkboxHoverColor,
      pushButtonHeight: pushButtonHeight ?? this.pushButtonHeight,
      pushButtonMinWidth: pushButtonMinWidth ?? this.pushButtonMinWidth,
      pushButtonBorderRadius: pushButtonBorderRadius ?? this.pushButtonBorderRadius,
      pushButtonBorderWidth: pushButtonBorderWidth ?? this.pushButtonBorderWidth,
      pushButtonPadding: pushButtonPadding ?? this.pushButtonPadding,
      pushButtonElevation: pushButtonElevation ?? this.pushButtonElevation,
      pushButtonFontSize: pushButtonFontSize ?? this.pushButtonFontSize,
      pushButtonFontWeight: pushButtonFontWeight ?? this.pushButtonFontWeight,
      pushButtonFontFamily: pushButtonFontFamily ?? this.pushButtonFontFamily,
      pushButtonLetterSpacing: pushButtonLetterSpacing ?? this.pushButtonLetterSpacing,
      radioButtonSize: radioButtonSize ?? this.radioButtonSize,
      radioButtonInnerSize: radioButtonInnerSize ?? this.radioButtonInnerSize,
      radioButtonBorderWidth: radioButtonBorderWidth ?? this.radioButtonBorderWidth,
      radioButtonSelectedBorderWidth: radioButtonSelectedBorderWidth ?? this.radioButtonSelectedBorderWidth,
      radioButtonPadding: radioButtonPadding ?? this.radioButtonPadding,
      radioButtonTextSpacing: radioButtonTextSpacing ?? this.radioButtonTextSpacing,
      radioButtonHoverWhole: radioButtonHoverWhole ?? this.radioButtonHoverWhole,
      radioButtonFontSize: radioButtonFontSize ?? this.radioButtonFontSize,
      radioButtonFontWeight: radioButtonFontWeight ?? this.radioButtonFontWeight,
      radioButtonFontFamily: radioButtonFontFamily ?? this.radioButtonFontFamily,
      radioButtonLetterSpacing: radioButtonLetterSpacing ?? this.radioButtonLetterSpacing,
      dropdownButtonHeight: dropdownButtonHeight ?? this.dropdownButtonHeight,
      dropdownButtonMinWidth: dropdownButtonMinWidth ?? this.dropdownButtonMinWidth,
      dropdownButtonBorderRadius: dropdownButtonBorderRadius ?? this.dropdownButtonBorderRadius,
      dropdownButtonBorderWidth: dropdownButtonBorderWidth ?? this.dropdownButtonBorderWidth,
      dropdownButtonPadding: dropdownButtonPadding ?? this.dropdownButtonPadding,
      dropdownButtonIconSize: dropdownButtonIconSize ?? this.dropdownButtonIconSize,
      dropdownButtonFontSize: dropdownButtonFontSize ?? this.dropdownButtonFontSize,
      dropdownButtonFontWeight: dropdownButtonFontWeight ?? this.dropdownButtonFontWeight,
      dropdownButtonFontFamily: dropdownButtonFontFamily ?? this.dropdownButtonFontFamily,
      dropdownButtonLetterSpacing: dropdownButtonLetterSpacing ?? this.dropdownButtonLetterSpacing,
      checkboxSize: checkboxSize ?? this.checkboxSize,
      checkboxBorderRadius: checkboxBorderRadius ?? this.checkboxBorderRadius,
      checkboxBorderWidth: checkboxBorderWidth ?? this.checkboxBorderWidth,
      checkboxPadding: checkboxPadding ?? this.checkboxPadding,
      checkboxTextSpacing: checkboxTextSpacing ?? this.checkboxTextSpacing,
      checkboxFontSize: checkboxFontSize ?? this.checkboxFontSize,
      checkboxFontWeight: checkboxFontWeight ?? this.checkboxFontWeight,
      checkboxFontFamily: checkboxFontFamily ?? this.checkboxFontFamily,
      checkboxLetterSpacing: checkboxLetterSpacing ?? this.checkboxLetterSpacing,
      checkboxCheckmarkSizeMultiplier: checkboxCheckmarkSizeMultiplier ?? this.checkboxCheckmarkSizeMultiplier,
      checkboxGrayedMarginMultiplier: checkboxGrayedMarginMultiplier ?? this.checkboxGrayedMarginMultiplier,
      checkboxGrayedBorderRadius: checkboxGrayedBorderRadius ?? this.checkboxGrayedBorderRadius,
      disabledBackgroundOpacity: disabledBackgroundOpacity ?? this.disabledBackgroundOpacity,
      disabledForegroundOpacity: disabledForegroundOpacity ?? this.disabledForegroundOpacity,
      imageTextSpacing: imageTextSpacing ?? this.imageTextSpacing,
      buttonImageSizeMultiplier: buttonImageSizeMultiplier ?? this.buttonImageSizeMultiplier,
    );
  }

  @override
  ThemeExtension<ButtonThemeExtension> lerp(
    ThemeExtension<ButtonThemeExtension>? other,
    double t,
  ) {
    if (other is! ButtonThemeExtension) {
      return this;
    }

    return ButtonThemeExtension(
      buttonPressDelay: Duration(
        milliseconds: (buttonPressDelay.inMilliseconds * (1 - t) + 
                     other.buttonPressDelay.inMilliseconds * t).round(),
      ),
      enableTapAnimation: t < 0.5 ? enableTapAnimation : other.enableTapAnimation,
      pushButtonColor: Color.lerp(pushButtonColor, other.pushButtonColor, t),
      selectableButtonColor: Color.lerp(selectableButtonColor, other.selectableButtonColor, t),
      dropdownButtonColor: Color.lerp(dropdownButtonColor, other.dropdownButtonColor, t),
      checkboxColor: Color.lerp(checkboxColor, other.checkboxColor, t),
      pushButtonTextColor: Color.lerp(pushButtonTextColor, other.pushButtonTextColor, t)!,
      pushButtonHoverColor: Color.lerp(pushButtonHoverColor, other.pushButtonHoverColor, t)!,
      pushButtonDisabledColor: Color.lerp(pushButtonDisabledColor, other.pushButtonDisabledColor, t)!,
      pushButtonBorderColor: Color.lerp(pushButtonBorderColor, other.pushButtonBorderColor, t)!,
      secondaryButtonColor: Color.lerp(secondaryButtonColor, other.secondaryButtonColor, t)!,
      secondaryButtonHoverColor: Color.lerp(secondaryButtonHoverColor, other.secondaryButtonHoverColor, t)!,
      secondaryButtonTextColor: Color.lerp(secondaryButtonTextColor, other.secondaryButtonTextColor, t)!,
      secondaryButtonBorderColor: Color.lerp(secondaryButtonBorderColor, other.secondaryButtonBorderColor, t)!,
      radioButtonSelectedColor: Color.lerp(radioButtonSelectedColor, other.radioButtonSelectedColor, t)!,
      radioButtonHoverColor: Color.lerp(radioButtonHoverColor, other.radioButtonHoverColor, t)!,
      radioButtonBorderColor: Color.lerp(radioButtonBorderColor, other.radioButtonBorderColor, t)!,
      radioButtonTextColor: Color.lerp(radioButtonTextColor, other.radioButtonTextColor, t)!,
      dropdownButtonTextColor: Color.lerp(dropdownButtonTextColor, other.dropdownButtonTextColor, t)!,
      dropdownButtonHoverColor: Color.lerp(dropdownButtonHoverColor, other.dropdownButtonHoverColor, t)!,
      dropdownButtonBorderColor: Color.lerp(dropdownButtonBorderColor, other.dropdownButtonBorderColor, t)!,
      dropdownButtonIconColor: Color.lerp(dropdownButtonIconColor, other.dropdownButtonIconColor, t)!,
      checkboxSelectedColor: Color.lerp(checkboxSelectedColor, other.checkboxSelectedColor, t)!,
      checkboxBorderColor: Color.lerp(checkboxBorderColor, other.checkboxBorderColor, t)!,
      checkboxCheckmarkColor: Color.lerp(checkboxCheckmarkColor, other.checkboxCheckmarkColor, t)!,
      checkboxTextColor: Color.lerp(checkboxTextColor, other.checkboxTextColor, t)!,
      checkboxHoverColor: Color.lerp(checkboxHoverColor, other.checkboxHoverColor, t)!,
      pushButtonHeight: (pushButtonHeight * (1 - t) + other.pushButtonHeight * t),
      pushButtonMinWidth: (pushButtonMinWidth * (1 - t) + other.pushButtonMinWidth * t),
      pushButtonBorderRadius: (pushButtonBorderRadius * (1 - t) + other.pushButtonBorderRadius * t),
      pushButtonBorderWidth: (pushButtonBorderWidth * (1 - t) + other.pushButtonBorderWidth * t),
      pushButtonPadding: EdgeInsets.lerp(pushButtonPadding, other.pushButtonPadding, t)!,
      pushButtonElevation: (pushButtonElevation * (1 - t) + other.pushButtonElevation * t),
      pushButtonFontSize: (pushButtonFontSize * (1 - t) + other.pushButtonFontSize * t),
      pushButtonFontWeight: FontWeight.lerp(pushButtonFontWeight, other.pushButtonFontWeight, t)!,
      pushButtonFontFamily: t < 0.5 ? pushButtonFontFamily : other.pushButtonFontFamily,
      pushButtonLetterSpacing: (pushButtonLetterSpacing * (1 - t) + other.pushButtonLetterSpacing * t),
      radioButtonSize: (radioButtonSize * (1 - t) + other.radioButtonSize * t),
      radioButtonInnerSize: (radioButtonInnerSize * (1 - t) + other.radioButtonInnerSize * t),
      radioButtonBorderWidth: (radioButtonBorderWidth * (1 - t) + other.radioButtonBorderWidth * t),
      radioButtonSelectedBorderWidth: (radioButtonSelectedBorderWidth * (1 - t) + other.radioButtonSelectedBorderWidth * t),
      radioButtonPadding: EdgeInsets.lerp(radioButtonPadding, other.radioButtonPadding, t)!,
      radioButtonTextSpacing: (radioButtonTextSpacing * (1 - t) + other.radioButtonTextSpacing * t),
      radioButtonHoverWhole: t < 0.5 ? radioButtonHoverWhole : other.radioButtonHoverWhole,
      radioButtonFontSize: (radioButtonFontSize * (1 - t) + other.radioButtonFontSize * t),
      radioButtonFontWeight: FontWeight.lerp(radioButtonFontWeight, other.radioButtonFontWeight, t)!,
      radioButtonFontFamily: t < 0.5 ? radioButtonFontFamily : other.radioButtonFontFamily,
      radioButtonLetterSpacing: (radioButtonLetterSpacing * (1 - t) + other.radioButtonLetterSpacing * t),
      dropdownButtonHeight: (dropdownButtonHeight * (1 - t) + other.dropdownButtonHeight * t),
      dropdownButtonMinWidth: (dropdownButtonMinWidth * (1 - t) + other.dropdownButtonMinWidth * t),
      dropdownButtonBorderRadius: (dropdownButtonBorderRadius * (1 - t) + other.dropdownButtonBorderRadius * t),
      dropdownButtonBorderWidth: (dropdownButtonBorderWidth * (1 - t) + other.dropdownButtonBorderWidth * t),
      dropdownButtonPadding: EdgeInsets.lerp(dropdownButtonPadding, other.dropdownButtonPadding, t)!,
      dropdownButtonIconSize: (dropdownButtonIconSize * (1 - t) + other.dropdownButtonIconSize * t),
      dropdownButtonFontSize: (dropdownButtonFontSize * (1 - t) + other.dropdownButtonFontSize * t),
      dropdownButtonFontWeight: FontWeight.lerp(dropdownButtonFontWeight, other.dropdownButtonFontWeight, t)!,
      dropdownButtonFontFamily: t < 0.5 ? dropdownButtonFontFamily : other.dropdownButtonFontFamily,
      dropdownButtonLetterSpacing: (dropdownButtonLetterSpacing * (1 - t) + other.dropdownButtonLetterSpacing * t),
      checkboxSize: (checkboxSize * (1 - t) + other.checkboxSize * t),
      checkboxBorderRadius: (checkboxBorderRadius * (1 - t) + other.checkboxBorderRadius * t),
      checkboxBorderWidth: (checkboxBorderWidth * (1 - t) + other.checkboxBorderWidth * t),
      checkboxPadding: EdgeInsets.lerp(checkboxPadding, other.checkboxPadding, t)!,
      checkboxTextSpacing: (checkboxTextSpacing * (1 - t) + other.checkboxTextSpacing * t),
      checkboxFontSize: (checkboxFontSize * (1 - t) + other.checkboxFontSize * t),
      checkboxFontWeight: FontWeight.lerp(checkboxFontWeight, other.checkboxFontWeight, t)!,
      checkboxFontFamily: t < 0.5 ? checkboxFontFamily : other.checkboxFontFamily,
      checkboxLetterSpacing: (checkboxLetterSpacing * (1 - t) + other.checkboxLetterSpacing * t),
      checkboxCheckmarkSizeMultiplier: (checkboxCheckmarkSizeMultiplier * (1 - t) + other.checkboxCheckmarkSizeMultiplier * t),
      checkboxGrayedMarginMultiplier: (checkboxGrayedMarginMultiplier * (1 - t) + other.checkboxGrayedMarginMultiplier * t),
      checkboxGrayedBorderRadius: (checkboxGrayedBorderRadius * (1 - t) + other.checkboxGrayedBorderRadius * t),
      disabledBackgroundOpacity: (disabledBackgroundOpacity * (1 - t) + other.disabledBackgroundOpacity * t),
      disabledForegroundOpacity: (disabledForegroundOpacity * (1 - t) + other.disabledForegroundOpacity * t),
      imageTextSpacing: (imageTextSpacing * (1 - t) + other.imageTextSpacing * t),
      buttonImageSizeMultiplier: (buttonImageSizeMultiplier * (1 - t) + other.buttonImageSizeMultiplier * t),
    );
  }
}