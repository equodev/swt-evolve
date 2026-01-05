import 'package:flutter/material.dart';
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
  
  return CTabItemThemeExtension(
    // Tab item text colors
    tabItemTextColor: colorScheme.onSurfaceVariant,
    tabItemSelectedTextColor: isDark ? Colors.white : Colors.black,
    tabItemDisabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
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
    tabItemVerticalPadding: 2.0,
    
    // Tab item image spacing
    tabItemImageTextSpacing: 3.0,
  );
}

