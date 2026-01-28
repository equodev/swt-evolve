import 'package:flutter/material.dart';
import '../theme_extensions/menu_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

MenuThemeExtension getMenuLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getMenuTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

MenuThemeExtension getMenuDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getMenuTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

MenuThemeExtension _getMenuTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return MenuThemeExtension(
    // Animation
    animationDuration: const Duration(milliseconds: 150),

    // Background colors
    backgroundColor: colorScheme.surface,
    menuBarBackgroundColor: colorScheme.surface,
    popupBackgroundColor: colorScheme.surface,
    hoverBackgroundColor: isDark
        ? Colors.white.withOpacity(0.08)
        : Colors.black.withOpacity(0.04),
    disabledBackgroundColor: colorScheme.surfaceVariant,

    // Text colors
    textColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,

    // Border colors
    borderColor: colorScheme.outline,
    menuBarBorderColor: colorScheme.outlineVariant,

    // Border styling
    borderWidth: 1.0,
    borderRadius: 4.0,
    disabledBorderColor: colorScheme.outlineVariant,

    // Sizing
    menuBarHeight: 28.0,
    minMenuWidth: 120.0,
    maxMenuWidth: 300.0,
    menuBarItemPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 4.0),
    popupPadding: const EdgeInsets.symmetric(vertical: 4.0),
    menuBarItemMargin: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 4.0),  
    // Elevation
    popupElevation: 8.0,

    // Font style
    textStyle: textTheme.bodyMedium?.copyWith(fontSize: 14),
  );
}
