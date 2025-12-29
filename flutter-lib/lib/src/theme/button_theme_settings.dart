import 'package:flutter/material.dart';
import 'button_theme_extension.dart';
import '../gen/button.dart';
import '../gen/control.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../impl/color_utils.dart';
import '../impl/utils/font_utils.dart';
import '../impl/widget_config.dart';

ButtonThemeExtension getButtonLightTheme() {
  return ButtonThemeExtension(
    buttonPressDelay: const Duration(milliseconds: 150),
    enableTapAnimation: true,
    
    // Push Button colors
    pushButtonColor: const Color(0xFF4F46E5),
    pushButtonTextColor: const Color(0xFFFFFFFF),
    pushButtonHoverColor: const Color(0xFF4338CA),
    pushButtonDisabledColor: const Color(0xFFE5E7EB),
    pushButtonBorderColor: const Color(0xFF4F46E5),
    
    // Secondary Button colors
    secondaryButtonColor: const Color(0xFFF2F2FE), 
    secondaryButtonHoverColor: const Color(0xFFE5E7EB), 
    secondaryButtonTextColor: const Color(0xFF4F46E5), 
    secondaryButtonBorderColor: const Color(0xFF4F46E5), 
    
    // Push Button dimensions
    pushButtonHeight: 32.0,
    pushButtonMinWidth: 80.0,
    pushButtonBorderRadius: 4.0,
    pushButtonBorderWidth: 1.0,
    pushButtonPadding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 6.0),
    pushButtonElevation: 0.0,
    pushButtonFontSize: 14.0,
    pushButtonFontWeight: FontWeight.w500,
    pushButtonFontFamily: null,
    pushButtonLetterSpacing: 0.0,
    
    // Selectable Button colors
    selectableButtonColor: const Color(0xFF4F46E5),
    
    // Radio Button colors
    radioButtonSelectedColor: const Color(0xFF4F46E5),
    radioButtonHoverColor: const Color(0xFFF3F4F6),
    radioButtonBorderColor: const Color(0xFF9CA3AF),
    radioButtonTextColor: const Color(0xFF111827),
    
    // Radio Button dimensions
    radioButtonSize: 20.0,
    radioButtonInnerSize: 8.0,
    radioButtonBorderWidth: 2.0,
    radioButtonSelectedBorderWidth: 6.0,
    radioButtonPadding: const EdgeInsets.all(4.0),
    radioButtonTextSpacing: 8.0,
    radioButtonHoverWhole: true,
    radioButtonFontSize: 14.0,
    radioButtonFontWeight: FontWeight.w400,
    radioButtonFontFamily: null,
    radioButtonLetterSpacing: 0.0,
    
    // Dropdown Button colors
    dropdownButtonColor: const Color(0xFFFFFFFF),
    dropdownButtonTextColor: const Color(0xFF111827),
    dropdownButtonHoverColor: const Color(0xFFF9FAFB),
    dropdownButtonBorderColor: const Color(0xFFD1D5DB),
    dropdownButtonIconColor: const Color(0xFF6B7280),
    
    // Dropdown Button dimensions
    dropdownButtonHeight: 36.0,
    dropdownButtonMinWidth: 100.0,
    dropdownButtonBorderRadius: 4.0,
    dropdownButtonBorderWidth: 1.0,
    dropdownButtonPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
    dropdownButtonIconSize: 20.0,
    dropdownButtonFontSize: 14.0,
    dropdownButtonFontWeight: FontWeight.w400,
    dropdownButtonFontFamily: null,
    dropdownButtonLetterSpacing: 0.0,
    
    // Checkbox colors
    checkboxColor: const Color(0xFFFFFFFF),
    checkboxSelectedColor: const Color(0xFF4F46E5),
    checkboxBorderColor: const Color(0xFFD1D5DB),
    checkboxCheckmarkColor: const Color(0xFFFFFFFF),
    checkboxTextColor: const Color(0xFF111827),
    checkboxHoverColor: const Color(0xFFF3F4F6),
    
    // Checkbox dimensions
    checkboxSize: 18.0,
    checkboxBorderRadius: 2.0,
    checkboxBorderWidth: 2.0,
    checkboxPadding: const EdgeInsets.all(4.0),
    checkboxTextSpacing: 8.0,
    checkboxFontSize: 14.0,
    checkboxFontWeight: FontWeight.w400,
    checkboxFontFamily: null,
    checkboxLetterSpacing: 0.0,
    checkboxCheckmarkSizeMultiplier: 0.7,
    checkboxGrayedMarginMultiplier: 0.17,
    checkboxGrayedBorderRadius: 1.0,
    disabledBackgroundOpacity: 0.12,
    disabledForegroundOpacity: 0.38,
    imageTextSpacing: 8.0,
    buttonImageSizeMultiplier: 0.6,
  );
}

ButtonThemeExtension getButtonDarkTheme() {
  return ButtonThemeExtension(
    buttonPressDelay: const Duration(milliseconds: 150),
    enableTapAnimation: true,
    
    // Push Button colors
    pushButtonColor: const Color(0xFF4F46E5),
    pushButtonTextColor: const Color(0xFFFFFFFF),
    pushButtonHoverColor: const Color(0xFF4338CA),
    pushButtonDisabledColor: const Color(0xFF374151),
    pushButtonBorderColor: const Color(0xFF4F46E5),
    
    // Secondary Button colors
    secondaryButtonColor: const Color(0xFFF2F2FE), 
    secondaryButtonHoverColor: const Color(0xFF374151), 
    secondaryButtonTextColor: const Color(0xFF4F46E5), 
    secondaryButtonBorderColor: const Color(0xFF4F46E5), 
    
    // Push Button dimensions
    pushButtonHeight: 32.0,
    pushButtonMinWidth: 80.0,
    pushButtonBorderRadius: 4.0,
    pushButtonBorderWidth: 1.0,
    pushButtonPadding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 6.0),
    pushButtonElevation: 0.0,
    pushButtonFontSize: 14.0,
    pushButtonFontWeight: FontWeight.w500,
    pushButtonFontFamily: null,
    pushButtonLetterSpacing: 0.0,
    
    // Selectable Button colors
    selectableButtonColor: const Color(0xFF4F46E5),
    
    // Radio Button colors
    radioButtonSelectedColor: const Color(0xFF4F46E5),
    radioButtonHoverColor: const Color(0xFF374151),
    radioButtonBorderColor: const Color(0xFF6B7280),
    radioButtonTextColor: const Color(0xFFF9FAFB),
    
    // Radio Button dimensions
    radioButtonSize: 20.0,
    radioButtonInnerSize: 8.0,
    radioButtonBorderWidth: 2.0,
    radioButtonSelectedBorderWidth: 6.0,
    radioButtonPadding: const EdgeInsets.all(4.0),
    radioButtonTextSpacing: 8.0,
    radioButtonHoverWhole: true,
    radioButtonFontSize: 14.0,
    radioButtonFontWeight: FontWeight.w400,
    radioButtonFontFamily: null,
    radioButtonLetterSpacing: 0.0,
    
    // Dropdown Button colors
    dropdownButtonColor: const Color(0xFF1F2937),
    dropdownButtonTextColor: const Color(0xFFF9FAFB),
    dropdownButtonHoverColor: const Color(0xFF374151),
    dropdownButtonBorderColor: const Color(0xFF4B5563),
    dropdownButtonIconColor: const Color(0xFF9CA3AF),
    
    // Dropdown Button dimensions
    dropdownButtonHeight: 36.0,
    dropdownButtonMinWidth: 100.0,
    dropdownButtonBorderRadius: 4.0,
    dropdownButtonBorderWidth: 1.0,
    dropdownButtonPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
    dropdownButtonIconSize: 20.0,
    dropdownButtonFontSize: 14.0,
    dropdownButtonFontWeight: FontWeight.w400,
    dropdownButtonFontFamily: null,
    dropdownButtonLetterSpacing: 0.0,
    
    // Checkbox colors
    checkboxColor: const Color(0xFF1F2937),
    checkboxSelectedColor: const Color(0xFF4F46E5),
    checkboxBorderColor: const Color(0xFF4B5563),
    checkboxCheckmarkColor: const Color(0xFFFFFFFF),
    checkboxTextColor: const Color(0xFFF9FAFB),
    checkboxHoverColor: const Color(0xFF374151),
    
    // Checkbox dimensions
    checkboxSize: 18.0,
    checkboxBorderRadius: 2.0,
    checkboxBorderWidth: 2.0,
    checkboxPadding: const EdgeInsets.all(4.0),
    checkboxTextSpacing: 8.0,
    checkboxFontSize: 14.0,
    checkboxFontWeight: FontWeight.w400,
    checkboxFontFamily: null,
    checkboxLetterSpacing: 0.0,
    checkboxCheckmarkSizeMultiplier: 0.7,
    checkboxGrayedMarginMultiplier: 0.17,
    checkboxGrayedBorderRadius: 1.0,
    disabledBackgroundOpacity: 0.12,
    disabledForegroundOpacity: 0.38,
    imageTextSpacing: 8.0,
    buttonImageSizeMultiplier: 0.6,
  );
}

Color getButtonBackgroundColor(
  VButton state,
  ButtonThemeExtension widgetTheme,
  ColorScheme colorScheme,
  Color defaultThemeColor,
) {
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.background != null) {
    return colorFromVColor(state.background, defaultColor: defaultThemeColor);
  }
  
  return defaultThemeColor;
}

Color getButtonForegroundColor(
  VButton state,
  ButtonThemeExtension widgetTheme,
  ColorScheme colorScheme,
  Color defaultThemeColor,
) {
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.foreground != null) {
    return colorFromVColor(state.foreground, defaultColor: defaultThemeColor);
  }
  
  return defaultThemeColor;
}

TextStyle getButtonTextStyle(
  BuildContext context,
  VButton state,
  ButtonThemeExtension widgetTheme,
  ColorScheme colorScheme,
  TextTheme textTheme,
  Color textColor,
  double fontSize,
  FontWeight fontWeight,
  String? fontFamily,
  double letterSpacing,
) {
  final useSwtFonts = getConfigFlags().use_swt_fonts ?? false;
  
  if (useSwtFonts && state.font != null) {
    return FontUtils.textStyleFromVFont(
      state.font,
      context,
      color: textColor,
    );
  }
  
  return TextStyle(
    color: textColor,
    fontSize: fontSize,
    fontWeight: fontWeight,
    fontFamily: fontFamily,
    letterSpacing: letterSpacing,
  );
}

Color getDisabledBackgroundColor(
  ButtonThemeExtension widgetTheme,
  ColorScheme colorScheme,
) {
  return colorScheme.onSurface.withOpacity(widgetTheme.disabledBackgroundOpacity);
}

Color getDisabledForegroundColor(
  ButtonThemeExtension widgetTheme,
  ColorScheme colorScheme,
) {
  return colorScheme.onSurface.withOpacity(widgetTheme.disabledForegroundOpacity);
}