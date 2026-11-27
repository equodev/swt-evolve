import 'package:flutter/material.dart';
import '../theme_extensions/filedialog_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

FileDialogThemeExtension getFileDialogLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getFileDialogTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

FileDialogThemeExtension getFileDialogDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getFileDialogTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

FileDialogThemeExtension _getFileDialogTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return FileDialogThemeExtension(
    titleStyle: (textTheme.titleMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurface,
      fontWeight: FontWeight.w600,
    ),
    messageStyle: (textTheme.bodyMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurfaceVariant,
    ),
    labelStyle: (textTheme.bodyMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurfaceVariant,
    ),
    pathStyle: (textTheme.bodyMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurface,
    ),
    backgroundColor: colorScheme.surface,
    borderRadius: 12.0,
    width: 520.0,
    height: 380.0,
    promptWidth: 320.0,
    padding: const EdgeInsets.fromLTRB(24, 24, 24, 20),
    titleContentSpacing: 12.0,
    sectionSpacing: 8.0,
    contentButtonsSpacing: 20.0,
    buttonSpacing: 8.0,
    iconColor: colorScheme.onSurfaceVariant,
    disabledIconColor: colorScheme.onSurface.withOpacity(0.38),
    iconSize: 20.0,
    iconButtonSize: 32.0,
    folderIconColor: colorScheme.primary,
    fileIconColor: colorScheme.onSurfaceVariant,
    rowIconSize: 18.0,
    rowIconSpacing: 8.0,
    // The List widget's selection and hover overlays.
    selectedOverlayOpacity: 0.3,
    hoverOverlayOpacity: 0.1,
    progressColor: colorScheme.primary,
  );
}
