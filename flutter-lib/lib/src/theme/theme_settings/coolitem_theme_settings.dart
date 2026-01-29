import 'package:flutter/material.dart';
import '../theme_extensions/coolitem_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CoolItemThemeExtension getCoolItemLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCoolItemTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CoolItemThemeExtension getCoolItemDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCoolItemTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CoolItemThemeExtension _getCoolItemTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return CoolItemThemeExtension(
    textStyle: textTheme.bodyMedium,
    contentPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
  );
}
