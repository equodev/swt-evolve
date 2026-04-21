import 'package:flutter/material.dart';
import '../theme_extensions/display_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

DisplayThemeExtension getDisplayLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getDisplayTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
  );
}

DisplayThemeExtension getDisplayDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getDisplayTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
  );
}

DisplayThemeExtension _getDisplayTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
}) {
  final titleBarText = colorScheme.onSurface;
  final titleBarHover = colorScheme.onSurface.withOpacity(isDark ? 0.08 : 0.06);

  return DisplayThemeExtension(
    titleBarColor: colorScheme.surfaceContainerHigh,
    titleBarHoverColor: titleBarHover,
    titleBarTextColor: titleBarText,
    titleBarHeight: 30.0,
    toolWindowTitleBarHeight: 22.0,
    titleBarBorderRadius: 6.0,

    closeButtonColor: colorScheme.onSurface.withOpacity(0.7),
    closeButtonHoverColor: colorScheme.error.withOpacity(isDark ? 0.9 : 1.0),
    closeButtonHoverIconColor: colorScheme.onError,
    minimizeButtonColor: colorScheme.onSurface.withOpacity(0.7),
    minimizeButtonHoverColor: titleBarHover,
    maximizeButtonColor: colorScheme.onSurface.withOpacity(0.7),
    maximizeButtonHoverColor: titleBarHover,
    titleBarButtonSize: 20.0,
    titleBarButtonBorderRadius: 3.0,
    titleBarButtonAnimationDuration: const Duration(milliseconds: 100),

    dialogBorderColor: colorScheme.outlineVariant,
    dialogBorderWidth: 1.0,
    dialogBorderRadius: 6.0,
    dialogShadowColor: colorScheme.shadow.withOpacity(isDark ? 0.5 : 0.18),
    dialogShadowBlurRadius: 20.0,
    dialogShadowSpreadRadius: 0.0,
    dialogShadowOffsetX: 0.0,
    dialogShadowOffsetY: 4.0,
    dialogBackgroundColor: colorScheme.surface,

    modalOverlayColor: colorScheme.scrim.withOpacity(0.32),

    titleTextStyle: textTheme.bodyMedium?.copyWith(
      color: titleBarText,
      fontWeight: FontWeight.w500,
    ),
    toolWindowTitleTextStyle: textTheme.bodySmall?.copyWith(
      color: titleBarText,
      fontWeight: FontWeight.w500,
    ),
  );
}
