import 'package:flutter/material.dart';
import '../theme_extensions/button_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/button.dart';
import '../../gen/control.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/widget_utils.dart';
import '../../impl/widget_config.dart';

ButtonThemeExtension getButtonLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getButtonTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ButtonThemeExtension getButtonDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getButtonTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ButtonThemeExtension _getButtonTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ButtonThemeExtension(
    // Timing
    buttonPressDelay: const Duration(milliseconds: 150),
    enableTapAnimation: true,
    
    // InkWell colors
    splashColor: Colors.transparent,
    highlightColor: Colors.transparent,
    
    pushButtonColor: colorScheme.primary,
    pushButtonTextColor: colorScheme.onPrimary,
    pushButtonHoverColor: colorSchemeExtension.primaryHovered,
    pushButtonPressedColor: colorScheme.primaryContainer,
    pushButtonDisabledColor: colorScheme.surfaceVariant,
    pushButtonBorderColor: colorSchemeExtension.primaryBorder,
    
    secondaryButtonColor: colorScheme.secondary,
    secondaryButtonHoverColor: colorScheme.secondaryContainer,
    secondaryButtonPressedColor: colorSchemeExtension.secondaryPressed,
    secondaryButtonTextColor: colorScheme.onSecondary,
    secondaryButtonBorderColor: colorSchemeExtension.secondaryBorder,
    
    // Selectable Button colors
    selectableButtonColor: colorScheme.primary,
    
    // Toggle Button colors
    toggleButtonColor: colorScheme.primaryContainer,
    toggleButtonBorderColor: colorScheme.outline,
    
    // Radio Button colors
    radioButtonSelectedColor: colorScheme.primary,
    radioButtonHoverColor: colorScheme.surfaceVariant,
    radioButtonBorderColor: colorScheme.outline,
    radioButtonTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    
    // Dropdown Button colors
    dropdownButtonColor: colorScheme.surface,
    dropdownButtonTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    dropdownButtonHoverColor: colorScheme.surfaceVariant,
    dropdownButtonBorderColor: colorScheme.outline,
    dropdownButtonIconColor: colorScheme.onSurfaceVariant,
    
    // Checkbox colors
    checkboxColor: colorScheme.surface,
    checkboxSelectedColor: colorScheme.primary,
    checkboxBorderColor: colorScheme.outline,
    checkboxCheckmarkColor: colorScheme.onPrimary,
    checkboxTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    checkboxHoverColor: colorScheme.surfaceVariant,
    
    // Border radius
    pushButtonBorderRadius: 4.0,
    radioButtonBorderRadius: 20.0,
    dropdownButtonBorderRadius: 4.0,
    checkboxBorderRadius: 2.0,
    checkboxGrayedBorderRadius: 1.0,
    
    // Border width
    pushButtonBorderWidth: 1.0,
    radioButtonBorderWidth: 2.0,
    radioButtonSelectedBorderWidth: 6.0,
    dropdownButtonBorderWidth: 1.0,
    checkboxBorderWidth: 2.0,
    
    pushButtonFontStyle: textTheme.bodyMedium,
    radioButtonFontStyle: textTheme.bodyMedium,
    dropdownButtonFontStyle: textTheme.bodyMedium,
    checkboxFontStyle: textTheme.bodyMedium,
    
    // Radio Button sizes
    radioButtonSize: 20.0,
    radioButtonInnerSize: 8.0,
    radioButtonTextSpacing: 8.0,
    
    // Dropdown Button sizes
    dropdownButtonIconSize: 24.0,
    
    // Checkbox sizes
    checkboxCheckmarkSizeMultiplier: 0.7,
    checkboxGrayedMarginMultiplier: 0.17,
    
    // Common sizes
    imageTextSpacing: 8.0,
    
    // Disabled colors
    disabledBackgroundColor: colorScheme.surfaceVariant,
    disabledForegroundColor: colorSchemeExtension.onSurfaceVariantDisabled,
  );
}


Color getButtonBackgroundColor(
  VButton state,
  ButtonThemeExtension widgetTheme,
  Color? defaultThemeColor, {
  bool? enabled,
  bool? isPrimary,
}) {
  final isEnabled = enabled ?? (state.enabled ?? true);
  final isPrimaryButton = isPrimary ?? (state.primary ?? false);
  
  if (!isEnabled) {
    return widgetTheme.pushButtonDisabledColor;
  }
  
  Color finalDefaultColor = defaultThemeColor ?? (isPrimaryButton
      ? widgetTheme.pushButtonColor
      : widgetTheme.secondaryButtonColor);
  
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.background != null) {
    return colorFromVColor(state.background, defaultColor: finalDefaultColor);
  }
  
  return finalDefaultColor;
}

Color getPushButtonHoverBackgroundColor(
  VButton state,
  ButtonThemeExtension widgetTheme, {
  bool? enabled,
  bool? isPrimary,
}) {
  final isEnabled = enabled ?? (state.enabled ?? true);
  final isPrimaryButton = isPrimary ?? (state.primary ?? false);
  
  if (!isEnabled) {
    return widgetTheme.pushButtonDisabledColor;
  }
  
  final defaultColor = isPrimaryButton
      ? widgetTheme.pushButtonHoverColor
      : widgetTheme.secondaryButtonHoverColor;
  
  return getButtonBackgroundColor(
    state,
    widgetTheme,
    defaultColor,
    enabled: isEnabled,
    isPrimary: isPrimaryButton,
  );
}

Color getPushButtonBorderColor(
  VButton state,
  ButtonThemeExtension widgetTheme, {
  bool? isPrimary,
}) {
  final isPrimaryButton = isPrimary ?? (state.primary ?? false);
  return isPrimaryButton
      ? widgetTheme.pushButtonBorderColor
      : widgetTheme.secondaryButtonBorderColor;
}

double getCheckboxSize(VButton state, double? fontSize) {
  if (hasBounds(state.bounds)) {
    return (state.bounds!.height.toDouble() * 0.7).clamp(16.0, 24.0);
  }
  
  if (fontSize != null) {
    return (fontSize * 1.2).clamp(16.0, 24.0);
  }
  
  return 18.0;
}

