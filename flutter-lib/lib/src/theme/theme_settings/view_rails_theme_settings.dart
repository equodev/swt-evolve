import 'package:flutter/material.dart';
import '../theme_extensions/view_rails_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ViewRailsThemeExtension getViewRailsLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getViewRailsTheme(colorScheme: colorScheme, textTheme: textTheme);
}

ViewRailsThemeExtension getViewRailsDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getViewRailsTheme(colorScheme: colorScheme, textTheme: textTheme);
}

ViewRailsThemeExtension _getViewRailsTheme({required ColorScheme colorScheme, required TextTheme textTheme}) {
  return ViewRailsThemeExtension(
    buttonSizes: const [28, 34, 42],
    outerPaddings: const [4, 5, 5],
    innerPaddings: const [1, 1, 1],
    iconSizes: const [16, 20, 24],
    minHeight: 20,
    endPadding: 6,
    buttonSpacing: 2,
    buttonRadius: 7,
    activeBackground: colorScheme.primary.withValues(alpha: 0.16),
    shownBackground: colorScheme.onSurface.withValues(alpha: 0.08),
    hoverBackground: colorScheme.onSurface.withValues(alpha: 0.06),
    idleOpacity: 0.75,
    draggingOpacity: 0.375,
    tooltipWait: const Duration(milliseconds: 500),
    letterStyle: (textTheme.bodyMedium ?? const TextStyle()).copyWith(
      fontSize: 13,
      fontWeight: FontWeight.w600,
      color: colorScheme.onSurface,
    ),
    indicatorColor: colorScheme.primary,
    indicatorWidth: 3,
    indicatorRadius: 2,
    indicatorOffset: 5,
    indicatorInset: 7,
    separatorColor: colorScheme.outlineVariant,
    separatorWidth: 22,
    separatorThickness: 1,
    separatorSpacing: 4,
    dropBackground: colorScheme.primary.withValues(alpha: 0.16),
    dropLineWidth: 2,
    dropRoom: 32,
  );
}
