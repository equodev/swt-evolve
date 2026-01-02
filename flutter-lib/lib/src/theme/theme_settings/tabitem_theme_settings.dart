import 'package:flutter/material.dart';
import '../theme_extensions/tabitem_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

TabItemThemeExtension getTabItemLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTabItemTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TabItemThemeExtension getTabItemDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTabItemTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TabItemThemeExtension _getTabItemTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  
  return TabItemThemeExtension(
    // Text colors
    textColor: colorScheme.onSurfaceVariant,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Icon properties
    iconSize: 16.0,
    
    // Padding and spacing
    containerPadding: const EdgeInsets.only(right: 2.0),
    imagePadding: const EdgeInsets.only(bottom: 1.0, right: 3.0),
    textPadding: const EdgeInsets.only(bottom: 2.0),
    imageTextSpacing: 0.0,
    
    // Typography
    textStyle: baseTextStyle.copyWith(
      fontSize: 12.0,
      fontWeight: FontWeight.normal,
    ),
  );
}

