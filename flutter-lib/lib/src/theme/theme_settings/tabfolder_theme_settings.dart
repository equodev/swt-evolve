import 'package:flutter/material.dart';
import '../../impl/widget_config.dart';
import '../theme_extensions/tabfolder_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

TabFolderThemeExtension getTabFolderLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTabFolderTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TabFolderThemeExtension getTabFolderDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTabFolderTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TabFolderThemeExtension _getTabFolderTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  
  return TabFolderThemeExtension(
    // Tab bar colors
    tabBarBackgroundColor: colorScheme.surface,
    tabBarBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    
    // Tab colors
    tabBackgroundColor: colorScheme.surface,
    tabSelectedBackgroundColor: colorScheme.surface,
    tabHoverBackgroundColor: colorSchemeExtension.stateDefaultHovered,
    tabDisabledBackgroundColor: colorScheme.surfaceVariant,
    
    // Tab border colors
    tabBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    tabSelectedBorderColor: colorScheme.primary,
    tabFocusRingColor: focusIndicators ? colorSchemeExtension.surfaceBorderFocused : colorSchemeExtension.stateDefaultEnabled,
    tabFocusRingWidth: 2.0,
    tabHoverBorderColor: colorSchemeExtension.surfaceBorderHovered,
    tabDisabledBorderColor: colorSchemeExtension.surfaceBorderDisabled,
    
    // Tab text colors
    tabTextColor: colorScheme.onSurfaceVariant,
    tabSelectedTextColor: colorScheme.primary,
    tabHoverTextColor: colorScheme.onSurface,
    tabDisabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Tab border properties
    tabBorderWidth: 1.0,
    tabSelectedBorderWidth: 2.0,
    tabBorderRadius: 0.0,
    
    // Tab padding
    tabPadding: 8.0,
    
    // Tab typography
    tabTextStyle: baseTextStyle.copyWith(
      fontSize: 12.0,
      fontWeight: FontWeight.normal,
    ),
    tabSelectedTextStyle: baseTextStyle.copyWith(
      fontSize: 12.0,
      fontWeight: FontWeight.w600,
    ),
    
    // Tab content area
    tabContentBackgroundColor: colorScheme.surface,
    tabContentBorderColor: colorSchemeExtension.surfaceBorderEnabled,
  );
}

/// The ring painted over the tab holding keyboard focus.
BoxDecoration getTabFocusRingDecoration(
  TabFolderThemeExtension theme, {
  required bool focused,
  required bool enabled,
}) {
  final ring = theme.tabFocusRingColor;
  return BoxDecoration(
    border: focused && enabled && ring.a > 0 ? Border.all(color: ring, width: theme.tabFocusRingWidth) : null,
  );
}
