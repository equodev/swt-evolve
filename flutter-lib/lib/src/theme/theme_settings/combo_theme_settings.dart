import 'package:flutter/material.dart';
import '../theme_extensions/combo_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/combo.dart';
import '../../gen/control.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/widget_utils.dart';
import '../../impl/widget_config.dart';

ComboThemeExtension getComboLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getComboTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ComboThemeExtension getComboDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getComboTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ComboThemeExtension _getComboTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ComboThemeExtension(
    backgroundColor: colorScheme.surface,
    textColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    borderColor: colorSchemeExtension.primaryBorder,
    iconColor: colorScheme.onSurfaceVariant,
    selectedItemBackgroundColor: colorScheme.onSecondaryContainer,
    hoverBackgroundColor: colorSchemeExtension.secondaryBold,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    disabledBorderColor: colorSchemeExtension.surfaceBorderDisabled,
    borderRadius: 4.0,
    borderWidth: 1.0,
    textFieldPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
    itemPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
    iconSpacing: 8.0,
    dividerHeight: 1.0,
    dividerThickness: 1.0,
    iconSize: 20.0,
    textStyle: textTheme.bodyMedium,
    itemTextStyle: textTheme.bodyMedium,
    dividerColor: colorScheme.outline,
    animationDuration: const Duration(milliseconds: 150),
  );
}

Color getComboBackgroundColor(
  VCombo state,
  ComboThemeExtension widgetTheme, {
  bool? enabled,
}) {
  final isEnabled = enabled ?? (state.enabled ?? true);
  
  if (!isEnabled) {
    return widgetTheme.disabledBackgroundColor;
  }
  
  return getBackgroundColor(
    background: state.background,
    defaultColor: widgetTheme.backgroundColor,
  ) ?? widgetTheme.backgroundColor;
}

Color getComboTextColor(
  VCombo state,
  ComboThemeExtension widgetTheme, {
  bool? enabled,
}) {
  final isEnabled = enabled ?? (state.enabled ?? true);
  
  if (!isEnabled) {
    return widgetTheme.disabledTextColor;
  }
  
  return getForegroundColor(
    foreground: state.foreground,
    defaultColor: widgetTheme.textColor,
  );
}

Color getComboBorderColor(
  VCombo state,
  ComboThemeExtension widgetTheme, {
  bool? enabled,
}) {
  final isEnabled = enabled ?? (state.enabled ?? true);
  
  if (!isEnabled) {
    return widgetTheme.disabledBorderColor;
  }
  
  return widgetTheme.borderColor;
}

Color getComboIconColor(
  ComboThemeExtension widgetTheme, {
  bool? enabled,
}) {
  final isEnabled = enabled ?? true;
  
  if (!isEnabled) {
    return widgetTheme.disabledTextColor;
  }
  
  return widgetTheme.iconColor;
}

