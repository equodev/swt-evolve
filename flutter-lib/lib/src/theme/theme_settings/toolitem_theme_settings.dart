import 'package:flutter/material.dart';
import '../theme_extensions/toolitem_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ToolItemThemeExtension getToolItemLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getToolItemTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ToolItemThemeExtension getToolItemDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getToolItemTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ToolItemThemeExtension _getToolItemTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ToolItemThemeExtension(
    enabledColor: textTheme.bodyMedium!.color!,
    disabledColor: colorSchemeExtension.onSurfaceVariantDisabled,
    hoverColor: colorSchemeExtension.stateDefaultHovered,
    selectedBackgroundColor: colorSchemeExtension.stateDefaultHovered,
    separatorColor: colorScheme.outline,
    dropdownIconColor: colorScheme.onSurfaceVariant,
    borderRadius: 4.0,
    separatorWidth: 20.0,
    separatorThickness: 1.0,
    separatorIndent: 8.0,
    defaultIconSize: 24.0,
    iconSize: 16.0,
    emptyButtonSize: 16.0,
    dropdownArrowSize: 12.0,
    buttonPadding: const EdgeInsets.all(4.0),
    textPadding: const EdgeInsets.symmetric(horizontal: 8.0),
    splashOpacity: 0.3,
    highlightOpacity: 0.2,
    separatorOpacity: 0.3,
    separatorBarWidth: 1.0,
    separatorBarMargin: const EdgeInsets.symmetric(vertical: 4.0),
    tooltipMargin: const EdgeInsets.symmetric(horizontal: 20),
    tooltipWaitDuration: const Duration(seconds: 1),
    segmentSelectedBackgroundColor: colorScheme.primary,
    segmentInnerColor: const Color(0xFF5959EB),
    segmentUnselectedBackgroundColor: isDark
        ? const Color(0xFF3A3A3A)
        : const Color(0xFFE0E0E0),
    segmentSelectedTextColor: Colors.white,
    segmentUnselectedTextColor: colorScheme.onSurfaceVariant,
    segmentBorderRadius: 8.0,
    segmentPadding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 1.0),
    loadingIndicatorSizeFactor: 0.5,
    loadingIndicatorStrokeWidth: 2.0,
    tooltipPreferBelow: false,
    tooltipVerticalOffset: 0.0,
    segmentAnimationDuration: const Duration(milliseconds: 200),
    segmentKeywordText: 'Keyword',
    segmentDebugText: 'Debug',
    specialDropdownTooltipText: 'Create new Test Case',
    specialDropdownBackgroundColor: const Color(0xFF5959EB),
    specialDropdownTextColor: Colors.white,
    specialDropdownSeparatorColor: Colors.white,
    specialDropdownArrowColor: Colors.white,
    specialDropdownPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 10.0),
    specialDropdownItemSpacing: 2.0,
    fontStyle: textTheme.bodyMedium,
  );
}

