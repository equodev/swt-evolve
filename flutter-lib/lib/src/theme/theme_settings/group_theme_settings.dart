import 'package:flutter/material.dart';
import '../theme_extensions/group_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

GroupThemeExtension getGroupLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getGroupTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

GroupThemeExtension getGroupDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getGroupTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

GroupThemeExtension _getGroupTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final borderColor = colorScheme.outlineVariant;
  final shadowDark = colorScheme.outlineVariant;

  return GroupThemeExtension(
    backgroundColor: colorScheme.surface,
    borderColor: borderColor,
    foregroundColor: colorScheme.onSurfaceVariant,
    textStyle: (textTheme.labelSmall ?? const TextStyle()).copyWith(
      fontWeight: FontWeight.w500,
      color: colorScheme.onSurfaceVariant,
      letterSpacing: 0.4,
    ),
    padding: const EdgeInsets.all(0.0),
    margin: const EdgeInsets.all(0.0),
    borderWidth: 1.0,
    borderRadius: 8.0,
    titleHorizontalOffset: 12.0,
    titleLabelPadding: 4.0,
    shadowHighlightColor: colorScheme.surfaceContainerHigh,
    shadowDarkColor: shadowDark,
    shadowOutOpacity: 0.10,
    shadowOutOpacityAlt: 0.04,
    shadowOutBlurRadius: 6.0,
    shadowOutBlurRadiusAlt: 2.0,
    shadowOutElevation: 2.0,
    shadowSecondaryElevation: 1.0,
    shadowEtchedOpacity: 0.06,
    shadowEtchedBlurRadius: 3.0,
    shadowInBorderFactor: 0.25,
    shadowInBgFactor: 0.03,
  );
}
