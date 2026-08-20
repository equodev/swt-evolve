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
    // Matches TextThemeExtension's own borderColor treatment (same colorScheme.outline token,
    // same dark-mode dimming) -- a SWT.BORDER Composite and a SWT.BORDER Text should read as the
    // same outline weight, not one full-strength and the other faded.
    borderColor: isDark
        ? colorScheme.outline.withOpacity(0.38)
        : colorSchemeExtension.surfaceBorderEnabled,
    focusedBorderColor: colorSchemeExtension.surfaceBorderFocused,
    // A SWT.BORDER Composite draws this outline (see composite_evolve.dart); 0-width/0-radius
    // would have made that border invisible and square. Mirrors the existing panelBorderWidth/
    // panelBorderRadius values below, which already establish this theme's visible-border weight.
    borderWidth: 1.0,
    // Matches TextThemeExtension's own focusedBorderWidth (2.0): a SWT.BORDER Composite's
    // outline should thicken on focus by the same amount a SWT.BORDER Text's does.
    focusedBorderWidth: 2.0,
    borderRadius: 4.0,
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
  Color? parentBackground,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledBackgroundColor;
  }

  final defaultColor = parentBackground ??
      (isMain ? widgetTheme.workbenchAreaGapColor : widgetTheme.backgroundColor);

  return getBackgroundColor(
    background: state.background,
    defaultColor: defaultColor,
  ) ?? defaultColor;
}
