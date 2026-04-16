import 'package:flutter/material.dart';
import '../theme_extensions/composite_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/composite.dart';
import '../../impl/utils/widget_utils.dart';

CompositeThemeExtension getCompositeLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCompositeTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CompositeThemeExtension getCompositeDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCompositeTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CompositeThemeExtension _getCompositeTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return CompositeThemeExtension(
    backgroundColor: colorScheme.surface,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    borderColor: colorScheme.outline,
    focusedBorderColor: colorScheme.primary,
    borderWidth: 0.0,
    borderRadius: 0.0,
    contentPadding: 0.0,
    workbenchAreaGapColor: colorSchemeExtension.surfaceToolbar,
    panelBorderColor: colorSchemeExtension.compositePanelBorderColor,
    panelBorderWidth: 1.0,
    panelBorderRadius: 4.0,
    panelChildGap: 5.0,
    panelShadowColor: colorScheme.shadow.withOpacity(0),
    panelShadowBlurRadius: 8.0,
    panelShadowDx: 0.0,
    panelShadowDy: 2.0,
  );
}

Color getCompositeBackgroundColor(
  VComposite state,
  CompositeThemeExtension widgetTheme, {
  required bool isEnabled,
  bool isMain = false,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledBackgroundColor;
  }

  final defaultColor = isMain
      ? widgetTheme.workbenchAreaGapColor
      : widgetTheme.backgroundColor;

  return getBackgroundColor(
    background: state.background,
    defaultColor: defaultColor,
  ) ?? defaultColor;
}
