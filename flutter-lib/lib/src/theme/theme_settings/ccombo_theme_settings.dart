import 'package:flutter/material.dart';
import '../theme_extensions/ccombo_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/ccombo.dart';
import '../../gen/swt.dart';
import '../../impl/color_utils.dart';
import '../../styles.dart';

CComboThemeExtension getCComboLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCComboTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CComboThemeExtension getCComboDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCComboTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CComboThemeExtension _getCComboTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return CComboThemeExtension(
    // Animation
    animationDuration: const Duration(milliseconds: 200),
    
    // Background colors
    backgroundColor: colorScheme.surface,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    hoverBackgroundColor: colorScheme.primary,
    
    // Text colors
    textColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Border colors
    borderColor: colorSchemeExtension.primaryBorder,
    disabledBorderColor: colorScheme.outlineVariant,
    
    // Icon colors
    iconColor: colorScheme.onSurfaceVariant,
    disabledIconColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Selection colors
    selectedItemBackgroundColor: colorScheme.onSecondaryContainer,
    
    // Border styling
    borderWidth: 1.0,
    borderRadius: 4.0,
    
    // Sizing
    itemHeight: 32.0,
    iconSize: 20.0,
    textFieldPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
    
    // Font styles
    textStyle: textTheme.bodyMedium?.copyWith(fontSize: 12),
    
    // Divider
    dividerColor: colorScheme.outline,
    dividerHeight: 1.0,
    dividerThickness: 1.0,
  );
}

// Helper to get border color based on state and style
Color getCComboBorderColor(
  VCCombo state,
  CComboThemeExtension widgetTheme, {
  required bool isActive,
  required bool isEnabled,
}) {
  if (!isEnabled) return widgetTheme.disabledBorderColor;
  
  final styleBits = StyleBits(state.style);
  if (styleBits.has(SWT.BORDER) || styleBits.has(SWT.FLAT)) {
    return isActive ? widgetTheme.borderColor : Colors.transparent;
  }
  
  return isActive ? widgetTheme.borderColor : Colors.transparent;
}

// Helper to get border width based on style
double getCComboBorderWidth(VCCombo state, CComboThemeExtension widgetTheme) {
  final styleBits = StyleBits(state.style);
  if (styleBits.has(SWT.BORDER)) {
    return 3.0; // Original BORDER style
  }
  return widgetTheme.borderWidth;
}