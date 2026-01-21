import 'package:flutter/material.dart';
import '../theme_extensions/list_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ListThemeExtension getListLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getListTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ListThemeExtension getListDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getListTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ListThemeExtension _getListTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ListThemeExtension(
    // Background colors
    backgroundColor: colorScheme.surface,
    selectedItemBackgroundColor: colorScheme.onSecondaryContainer,
    hoverItemBackgroundColor: colorScheme.primary,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    
    // Text colors
    textColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    selectedItemTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Border colors
    borderColor: colorScheme.outline,
    focusedBorderColor: colorScheme.primary,
    
    // Border styling
    borderWidth: 1.0,
    borderRadius: 0.0, // Lists typically have sharp corners
    
    // Sizing
    itemHeight: 24.0,
    itemPadding: const EdgeInsets.symmetric(horizontal: 8.0),
    
    // Font style
    textStyle: textTheme.bodyMedium,
    
    // Animation
    animationDuration: const Duration(milliseconds: 150),
  );
}