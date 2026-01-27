import 'package:flutter/material.dart';
import '../theme_extensions/spinner_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/spinner.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/widget_utils.dart';

SpinnerThemeExtension getSpinnerLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getSpinnerTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

SpinnerThemeExtension getSpinnerDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getSpinnerTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

SpinnerThemeExtension _getSpinnerTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return SpinnerThemeExtension(
    // Animation
    animationDuration: const Duration(milliseconds: 150),

    // Background colors
    backgroundColor: colorScheme.surface,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    buttonBackgroundColor: colorScheme.surface,
    buttonHoverColor: colorScheme.surfaceVariant,
    buttonPressedColor: colorScheme.secondaryContainer,

    // Text colors
    textColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,

    // Border colors
    borderColor: colorScheme.outline,
    focusedBorderColor: colorScheme.primary,
    disabledBorderColor: colorScheme.outlineVariant,

    // Icon colors
    iconColor: colorScheme.primary,
    iconHoverColor: colorScheme.primaryContainer,
    disabledIconColor: colorSchemeExtension.onSurfaceVariantDisabled,

    // Border styling
    borderWidth: 1.0,
    borderRadius: 4.0,

    // Sizing
    minWidth: 80.0,
    minHeight: 32.0,
    buttonWidth: 20.0,
    iconSize: 16.0,
    textFieldPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 8.0),

    // Font styles
    textStyle: textTheme.bodyMedium?.copyWith(fontSize: 14),

    // Default spinner values
    defaultMinimum: 0,
    defaultMaximum: 100,
    defaultSelection: 0,
    defaultIncrement: 1,
    defaultPageIncrement: 10,
    defaultDigits: 0,
    defaultTextLimit: 10,
  );
}

// Helper to get background color with SWT support
Color getSpinnerBackgroundColor(
  VSpinner state,
  SpinnerThemeExtension widgetTheme, {
  required bool isEnabled,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledBackgroundColor;
  }

  return getBackgroundColor(
    background: state.background,
    defaultColor: widgetTheme.backgroundColor,
  ) ?? widgetTheme.backgroundColor;
}

// Helper to get text color with SWT support
Color getSpinnerTextColor(
  VSpinner state,
  SpinnerThemeExtension widgetTheme, {
  required bool isEnabled,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledTextColor;
  }

  return getForegroundColor(
    foreground: state.foreground,
    defaultColor: widgetTheme.textColor,
  );
}

// Helper to get border color based on state
Color getSpinnerBorderColor(
  VSpinner state,
  SpinnerThemeExtension widgetTheme, {
  required bool isEnabled,
  required bool isFocused,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledBorderColor;
  }
  if (isFocused) {
    return widgetTheme.focusedBorderColor;
  }
  return widgetTheme.borderColor;
}

// Helper to get icon color based on state
Color getSpinnerIconColor(
  SpinnerThemeExtension widgetTheme, {
  required bool isEnabled,
  required bool isHovered,
}) {
  if (!isEnabled) {
    return widgetTheme.disabledIconColor;
  }
  if (isHovered) {
    return widgetTheme.iconHoverColor;
  }
  return widgetTheme.iconColor;
}

// Helper to calculate spinner size based on bounds
Size getSpinnerSize(
  VSpinner state,
  SpinnerThemeExtension widgetTheme,
) {
  if (hasBounds(state.bounds)) {
    return Size(
      state.bounds!.width.toDouble(),
      state.bounds!.height.toDouble(),
    );
  }
  return Size(widgetTheme.minWidth, widgetTheme.minHeight);
}
