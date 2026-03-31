import 'package:flutter/material.dart';
import '../theme_extensions/ctabfolder_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

CTabFolderThemeExtension getCTabFolderLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCTabFolderTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CTabFolderThemeExtension getCTabFolderDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getCTabFolderTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

CTabFolderThemeExtension _getCTabFolderTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyLarge ?? const TextStyle();
  final selectedTextColor = colorScheme.onSurface;
  final highlightColor = colorSchemeExtension.ctabFolderHighlightColor;
  final unselectedColor = colorSchemeExtension.ctabFolderUnselectedColor;

  return CTabFolderThemeExtension(
    // Tab bar colors
    tabBarBackgroundColor: unselectedColor,
    tabBarBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    
    // Tab colors
    tabBackgroundColor: unselectedColor,
    tabSelectedBackgroundColor: unselectedColor,
    tabHoverBackgroundColor: colorSchemeExtension.stateDefaultHovered,
    tabDisabledBackgroundColor: colorScheme.surfaceVariant,
    
    // Tab border colors
    tabBorderColor: colorSchemeExtension.stateDefaultEnabled,
    tabSelectedBorderColor: colorScheme.primary,
    tabHoverBorderColor: colorSchemeExtension.surfaceBorderHovered,
    tabDisabledBorderColor: colorSchemeExtension.surfaceBorderDisabled,
    
    // Tab text colors
    tabTextColor: colorScheme.onSurfaceVariant,
    tabSelectedTextColor: selectedTextColor,
    tabHoverTextColor: colorScheme.onSurface,
    tabDisabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    tabUnselectedTextOpacity: 1.0,
    
    // Tab close button
    tabCloseButtonColor: colorScheme.onSurface,
    tabCloseButtonSelectedOpacity: 0.9,
    tabCloseButtonUnselectedOpacity: 0.7,
    
    // Highlight color (for selected tab top border)
    tabHighlightColor: highlightColor,
    
    // Tab border properties
    tabBorderWidth: 0.0,
    tabSelectedBorderWidth: 1.0,
    tabHighlightBorderWidth: 2.0,
    tabBorderRadius: 3.0,
    
    // Tab padding
    tabHorizontalPadding: 6.0,
    tabVerticalPadding: 0.0,
    
    // Tab typography
    tabTextStyle: baseTextStyle,
    tabSelectedTextStyle: baseTextStyle,
    
    // Tab content area
    tabContentBackgroundColor: colorScheme.surface,
    tabContentBorderColor: colorSchemeExtension.surfaceBorderEnabled,
    
    // Tab sizes
    tabIconSize: 16.0,
    tabCloseIconSize: 16.0,
    controlButtonSize: 14.0,
    
    // Tab spacing
    tabIconTextSpacing: 14.0,
    tabCloseButtonSpacing: 14.0,
    
    // Tab icon/text padding
    tabIconBottomPadding: 1.0,
    tabTextBottomPadding: 2.0,
    tabCloseIconBottomPadding: 1.0,
    
    // Control buttons
    controlButtonColor: colorScheme.onSurface,
    controlButtonHoverColor: colorSchemeExtension.stateDefaultHovered,
    controlButtonHorizontalPadding: 2.0,
    controlButtonSpacing: 2.0,
    controlButtonScale: 1,
    
    // Hover reveal animation
    hoverRevealDuration: const Duration(milliseconds: 250),
    hoverHideDuration: const Duration(milliseconds: 500),
    
    // Scrollbar
    tabScrollbarThickness: 2.0,
    
    // Top right controls overlay shadow
    topRightControlsShadowColor: colorScheme.shadow,
    topRightControlsShadowOpacity: 0.1,
    topRightControlsShadowBlurRadius: 4.0,
    topRightControlsShadowOffset: const Offset(0, 2),
    
    // Top right controls padding
    topRightControlsPadding: const EdgeInsets.symmetric(horizontal: 4.0),
    
    // Scrollbar hide delay
    scrollbarHideDelay: const Duration(milliseconds: 800),
  );
}

Color getCTabTextColor(
  CTabFolderThemeExtension widgetTheme,
  bool isSelected,
  bool enabled, {
  Color? resolvedSelectionForeground,
}) {
  if (!enabled) return widgetTheme.tabDisabledTextColor;
  if (isSelected) return resolvedSelectionForeground ?? widgetTheme.tabSelectedTextColor;
  return widgetTheme.tabTextColor.withOpacity(widgetTheme.tabUnselectedTextOpacity);
}

Color getCTabBackgroundColor(
  CTabFolderThemeExtension widgetTheme,
  bool isSelected,
  bool enabled, {
  Color? resolvedSelectionBackground,
  bool useDefaultTheme = false,
}) {
  if (!enabled) return widgetTheme.tabDisabledBackgroundColor;
  if (isSelected) {
    if (useDefaultTheme) return widgetTheme.tabSelectedBackgroundColor;
    return resolvedSelectionBackground ?? widgetTheme.tabSelectedBackgroundColor;
  }
  return widgetTheme.tabBackgroundColor;
}

Color getCTabBorderColor(CTabFolderThemeExtension widgetTheme, bool enabled) {
  return enabled ? widgetTheme.tabBorderColor : widgetTheme.tabDisabledBorderColor;
}

TextStyle? getCTabTextStyle(
  CTabFolderThemeExtension widgetTheme,
  bool isSelected,
  bool enabled,
) {
  return (isSelected && enabled)
      ? widgetTheme.tabSelectedTextStyle
      : widgetTheme.tabTextStyle;
}

Color getCTabCloseButtonColor(CTabFolderThemeExtension widgetTheme, bool isSelected) {
  return widgetTheme.tabCloseButtonColor.withOpacity(
    isSelected
        ? widgetTheme.tabCloseButtonSelectedOpacity
        : widgetTheme.tabCloseButtonUnselectedOpacity,
  );
}
