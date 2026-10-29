import 'package:flutter/material.dart';
import '../../impl/utils/widget_utils.dart';
import '../theme_extensions/expanditem_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ExpandItemThemeExtension getExpandItemLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getExpandItemTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ExpandItemThemeExtension getExpandItemDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getExpandItemTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ExpandItemThemeExtension _getExpandItemTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ExpandItemThemeExtension(
    // Colors
    backgroundColor: colorScheme.surface,
    borderColor: colorScheme.outline,
    foregroundColor: colorScheme.onSurface,
    foregroundExpandedColor: colorScheme.onSurface,
    headerBackgroundColor: colorScheme.surfaceContainerHigh,
    headerBackgroundExpandedColor: colorScheme.onSecondaryContainer,
    headerBackgroundHoveredColor: colorScheme.surfaceContainerLow,
    contentBackgroundColor: colorScheme.surface,
    iconColor: colorScheme.onSurfaceVariant,
    iconExpandedColor: colorScheme.onSurfaceVariant,
    disabledForegroundColor: colorSchemeExtension.onSurfaceVariantDisabled,
    disabledBackgroundColor: colorScheme.surfaceVariant,

    // Border
    borderWidth: 1.0,
    borderRadius: 4.0,

    // Padding. A header's horizontal insets and image gap are the ones SWT's ExpandItem reserves
    // around its text (TEXT_INSET), so a bar packed to its headers fits their text.
    headerPadding: const EdgeInsets.symmetric(horizontal: 6.0, vertical: 8.0),
    contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),

    // Typography
    headerTextStyle: textTheme.bodyMedium,

    // Animation
    animationDuration: const Duration(milliseconds: 200),
    animationCurve: Curves.easeInOut,

    // Icon
    iconSize: 24.0,

    // Image spacing
    imageTextSpacing: 6.0,

    minTextContrast: kMinTextContrast,
  );
}
