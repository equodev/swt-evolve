import 'package:flutter/material.dart';
import '../../impl/utils/font_utils.dart';
import '../../impl/widget_config.dart';
import '../theme_extensions/ccombo_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/ccombo.dart';
import '../../gen/swt.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/widget_utils.dart';
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
    focusedBorderColor: focusIndicators ? colorSchemeExtension.surfaceBorderFocused : colorSchemeExtension.primaryBorder,
    itemHoverBackgroundColor: colorSchemeExtension.stateDefaultEnabled,
    disabledBorderColor: colorScheme.outlineVariant,
    
    // Icon colors
    iconColor: colorScheme.onSurfaceVariant,
    disabledIconColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Selection colors
    selectedItemBackgroundColor: colorScheme.primary,
    
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

    minTextContrast: kMinTextContrast,
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
    return isActive ? widgetTheme.borderColor : widgetTheme.backgroundColor.withOpacity(0);
  }
  
  return isActive ? widgetTheme.borderColor : widgetTheme.backgroundColor.withOpacity(0);
}

// Helper to get border width based on style
double getCComboBorderWidth(VCCombo state, CComboThemeExtension widgetTheme) {
  final styleBits = StyleBits(state.style);
  if (styleBits.has(SWT.BORDER)) {
    return 3.0; // Original BORDER style
  }
  return widgetTheme.borderWidth;
}

Color getCComboItemBackgroundColor(CComboThemeExtension widgetTheme, bool isSelected) {
  return isSelected ? widgetTheme.selectedItemBackgroundColor : Colors.transparent;
}

/// The height a CCombo's list lays each item out in: the theme's item height, or one line of [style]
/// plus the item's vertical padding when that is taller, rounded up to a whole pixel.
/// CComboListSizes.java mirrors this rule for `getItemHeight()`.
double ccomboItemExtent(CComboThemeExtension theme, TextStyle style) {
  final line = FontUtils.measureTextWidget('Ag', style).height + theme.textFieldPadding.vertical;
  return (line > theme.itemHeight ? line : theme.itemHeight).ceilToDouble();
}

/// A list entry's style. The open list has a single highlighted item, which hover moves, so hover and
/// focus paint nothing of their own. Every entry is [extent] high, the list's [ccomboItemExtent],
/// whatever the platform's tap target and density, so Java can answer where each one is.
ButtonStyle getCComboEntryStyle(
  CComboThemeExtension widgetTheme,
  TextStyle textStyle,
  double width,
  double extent,
  bool Function() isHighlighted,
) {
  return MenuItemButton.styleFrom(
    foregroundColor: textStyle.color,
    minimumSize: Size(width, extent),
    maximumSize: Size(double.infinity, extent),
    tapTargetSize: MaterialTapTargetSize.shrinkWrap,
    visualDensity: VisualDensity.standard,
    padding: widgetTheme.textFieldPadding,
    overlayColor: Colors.transparent,
  ).copyWith(
    backgroundColor: WidgetStateProperty.resolveWith(
      (_) => getCComboItemBackgroundColor(widgetTheme, isHighlighted()),
    ),
  );
}
