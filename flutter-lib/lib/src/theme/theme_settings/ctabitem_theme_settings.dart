import 'package:flutter/material.dart';
import '../../impl/utils/widget_utils.dart';
import '../theme_extensions/ctabitem_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CTabItemThemeExtension getCTabItemLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCTabItemTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CTabItemThemeExtension getCTabItemDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCTabItemTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CTabItemThemeExtension _getCTabItemTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  // A selected tab, focused or not, stands out by weight, background and frame, not by its text colour.
  final textColor = colorScheme.onSurfaceVariant;

  return CTabItemThemeExtension(
    // Tab item text colors
    tabItemTextColor: textColor,
    tabItemSelectedTextColor: textColor,
    tabItemDisabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    tabItemActiveTextColor: textColor,
    tabItemActiveFontWeight: FontWeight.w700,
    
    // Tab item typography
    tabItemTextStyle: baseTextStyle.copyWith(
      fontSize: 12.0,
      fontWeight: FontWeight.normal,
    ),
    tabItemSelectedTextStyle: baseTextStyle.copyWith(
      fontSize: 12.0,
      fontWeight: FontWeight.w600,
    ),
    
    // Tab item padding
    tabItemHorizontalPadding: 2.0,
    tabItemVerticalPadding: 0.0,
    
    // Tab item image spacing
    tabItemImageTextSpacing: 3.0,

    minTextContrast: kMinTextContrast,
  );
}

/// A tab label's theme colour; a selected tab whose folder has no focus takes the unselected colour.
Color getCTabItemTextColor(
  CTabItemThemeExtension theme, {
  required bool selected,
  required bool enabled,
  required bool active,
  bool dimmed = false,
}) {
  if (!enabled) return theme.tabItemDisabledTextColor;
  if (active) return theme.tabItemActiveTextColor;
  if (dimmed) return theme.tabItemTextColor;
  return selected ? theme.tabItemSelectedTextColor : theme.tabItemTextColor;
}

TextStyle getCTabItemTextStyle(CTabItemThemeExtension theme, TextStyle style, {required bool active}) =>
    active ? style.copyWith(fontWeight: theme.tabItemActiveFontWeight) : style;
