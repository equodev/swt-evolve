import 'package:flutter/material.dart';
import '../theme_extensions/label_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

LabelThemeExtension getLabelLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getLabelTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

LabelThemeExtension getLabelDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getLabelTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

LabelThemeExtension _getLabelTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  
  return LabelThemeExtension(
    primaryTextColor: colorScheme.onSurface,
    secondaryTextColor: colorScheme.onSurfaceVariant,
    errorTextColor: colorScheme.error,
    warningTextColor: colorSchemeExtension.warning,
    successTextColor: colorSchemeExtension.success,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    linkTextColor: colorScheme.primary,
    linkHoverTextColor: colorSchemeExtension.primaryHovered,
    
    backgroundColor: Colors.white,
    hoverBackgroundColor: colorSchemeExtension.stateDefaultHovered,
    selectedBackgroundColor: colorSchemeExtension.stateDefaultPressed,
    
    borderColor: null,
    focusBorderColor: null,
    
    // Font styles
    primaryTextStyle: baseTextStyle.copyWith(color: colorScheme.onSurface),
    secondaryTextStyle: baseTextStyle.copyWith(color: colorScheme.onSurfaceVariant),
    errorTextStyle: baseTextStyle.copyWith(color: colorScheme.error),
    warningTextStyle: baseTextStyle.copyWith(color: const Color(0xFFF59E0B)),
    successTextStyle: baseTextStyle.copyWith(color: const Color(0xFF059669)),
    disabledTextStyle: baseTextStyle.copyWith(color: colorScheme.onSurface.withOpacity(0.38)),
    linkTextStyle: baseTextStyle.copyWith(
      color: colorScheme.primary,
      decoration: TextDecoration.underline,
    ),
    
    // Spacing and padding
    padding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 2.0),
    margin: EdgeInsets.zero,
    iconTextSpacing: 8.0,
    
    // Border properties
    borderRadius: 0.0,
    borderWidth: 0.0,
    focusBorderWidth: 0.0,
    
    // Icon properties
    iconSize: 16.0,
    smallIconSize: 14.0,
    largeIconSize: 20.0,
    
    // Interactive properties
    isSelectable: false,
    showTooltip: false,
    disabledOpacity: 0.6,
    hoverAnimationDuration: const Duration(milliseconds: 150),
    
    // Alignment and positioning
    textAlign: TextAlign.start,
    mainAxisAlignment: MainAxisAlignment.start,
    crossAxisAlignment: CrossAxisAlignment.center,
    
    // Decoration properties
    textDecoration: TextDecoration.none,
    hoverTextDecoration: TextDecoration.none,
    decorationThickness: 1.0,
  );
}
