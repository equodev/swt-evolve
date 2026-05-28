import 'package:flutter/material.dart';
import '../theme_extensions/message_box_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

MessageBoxThemeExtension getMessageBoxLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getMessageBoxTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

MessageBoxThemeExtension getMessageBoxDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getMessageBoxTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

MessageBoxThemeExtension _getMessageBoxTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return MessageBoxThemeExtension(
    titleStyle: (textTheme.titleMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurface,
      fontWeight: FontWeight.w600,
    ),
    messageStyle: (textTheme.bodyMedium ?? const TextStyle()).copyWith(
      color: colorScheme.onSurfaceVariant,
    ),
    backgroundColor: colorScheme.surface,
    iconErrorColor: colorScheme.error,
    iconWarningColor: colorSchemeExtension.warning,
    iconInfoColor: colorScheme.primary,
    iconQuestionColor: colorScheme.secondary,
    borderRadius: 12.0,
    minWidth: 280.0,
    maxWidth: 480.0,
    padding: const EdgeInsets.fromLTRB(24, 24, 24, 20),
    iconTitleSpacing: 10.0,
    titleMessageSpacing: 12.0,
    messageIndent: 34.0,
    contentButtonsSpacing: 20.0,
    buttonSpacing: 8.0,
  );
}
