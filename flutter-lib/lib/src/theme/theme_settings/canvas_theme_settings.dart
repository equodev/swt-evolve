import 'package:flutter/material.dart';
import '../theme_extensions/canvas_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CanvasThemeExtension getCanvasLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCanvasTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CanvasThemeExtension getCanvasDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCanvasTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CanvasThemeExtension _getCanvasTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return CanvasThemeExtension(
    defaultWidth: 64.0,
    defaultHeight: 64.0,

    // Colors
    backgroundColor: isDark ? colorScheme.surface : colorSchemeExtension.neutral,
    foregroundColor: colorScheme.onSurface,
  );
}
