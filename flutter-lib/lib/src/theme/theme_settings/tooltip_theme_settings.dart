import 'package:flutter/material.dart';
import '../theme_extensions/tooltip_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

TooltipThemeExtension getTooltipLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTooltipTheme(
    colorScheme: colorScheme,
    textTheme: textTheme,
  );
}

TooltipThemeExtension getTooltipDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTooltipTheme(
    colorScheme: colorScheme,
    textTheme: textTheme,
  );
}

TooltipThemeExtension _getTooltipTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
}) {
  return TooltipThemeExtension(
    backgroundColor: colorScheme.inverseSurface,
    borderRadius: 4.0,
    messageTextStyle: textTheme.bodyMedium?.copyWith(
      color: colorScheme.onInverseSurface,
      fontSize: 12,
    ),
    waitDuration: const Duration(milliseconds: 500),
  );
}
