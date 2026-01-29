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
    backgroundColor: Color(0xFF1F1F1F)  ,
    borderRadius: 4.0,
    messageTextStyle: textTheme.bodyMedium?.copyWith(
      color: Colors.white,
      fontSize: 12,
    ),
    waitDuration: const Duration(milliseconds: 500),
  );
}
