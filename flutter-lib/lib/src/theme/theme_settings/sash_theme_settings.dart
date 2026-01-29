import 'package:flutter/material.dart';
import '../theme_extensions/sash_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

SashThemeExtension getSashLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getSashTheme(
    colorScheme: colorScheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

SashThemeExtension getSashDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getSashTheme(
    colorScheme: colorScheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

SashThemeExtension _getSashTheme({
  required ColorScheme colorScheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return SashThemeExtension(
    backgroundColor: colorScheme.surface,
    sashColor: colorScheme.outline,
    sashHoverColor: colorSchemeExtension.primaryHovered,
    sashCenterOpacity: 0.5,
    sashCenterHoverOpacity: 0.8,
    hitAreaSize: 5.0,
    defaultSize: 5.0,
  );
}
