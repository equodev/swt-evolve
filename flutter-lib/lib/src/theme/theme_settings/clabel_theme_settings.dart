import 'package:flutter/material.dart';
import '../theme_extensions/clabel_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CLabelThemeExtension getCLabelLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCLabelTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CLabelThemeExtension getCLabelDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCLabelTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CLabelThemeExtension _getCLabelTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();

  return CLabelThemeExtension(
    primaryTextColor: colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,

    backgroundColor: null,

    // Font styles
    primaryTextStyle: baseTextStyle.copyWith(
      color: colorScheme.onSurface,
      fontSize: 12,
    ),
    disabledTextStyle: baseTextStyle.copyWith(
      color: colorScheme.onSurface.withOpacity(0.38),
      fontSize: 12,
    ),

    // Icon properties
    iconSize: 16.0,
    iconTextSpacing: 4.0,

    // Interactive properties
    disabledOpacity: 0.5,

    // Alignment and positioning
    textAlign: TextAlign.left,
    mainAxisAlignment: MainAxisAlignment.start,
    crossAxisAlignment: CrossAxisAlignment.center,
  );
}
