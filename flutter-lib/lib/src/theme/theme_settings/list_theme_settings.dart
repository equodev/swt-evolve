import '../../impl/utils/font_utils.dart';
import '../../impl/utils/widget_utils.dart';
import '../../impl/widget_config.dart';
import '../../gen/color.dart';
import 'package:flutter/material.dart';
import '../theme_extensions/list_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';

ListThemeExtension getListLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getListTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ListThemeExtension getListDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getListTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

ListThemeExtension _getListTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return ListThemeExtension(
    // Background colors
    backgroundColor: colorScheme.surface,
    selectedItemBackgroundColor: colorScheme.onSecondaryContainer,
    hoverItemBackgroundColor: colorScheme.primary,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    
    // Text colors
    textColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    selectedItemTextColor: textTheme.bodyMedium?.color ?? colorScheme.onSurface,
    disabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Border colors
    borderColor: colorScheme.outline,
    focusedBorderColor: focusIndicators ? colorSchemeExtension.surfaceBorderFocused : colorScheme.primary,
    
    // Border styling
    borderWidth: 1.0,
    borderRadius: 0.0, // Lists typically have sharp corners
    
    // Sizing
    itemHeight: 24.0,
    itemPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 2.0),
    
    // Font style
    textStyle: textTheme.bodyMedium,
    
    // Animation
    animationDuration: const Duration(milliseconds: 150),

    minTextContrast: kMinTextContrast,
  );
}

/// The height a List lays each item out in: the theme's item height, or one line of [style] plus
/// the item's vertical padding when that is taller. ListSizes.java mirrors this rule.
double listItemExtent(ListThemeExtension theme, TextStyle style) =>
    listItemExtentForLine(theme, FontUtils.measureTextWidget('Ag', style).height);

/// [listItemExtent] for a measured [line] height, rounded up to a whole pixel as Java's
/// `ListSizes.getItemHeight` does, so both report and lay out the same pitch.
@visibleForTesting
double listItemExtentForLine(ListThemeExtension theme, double line) {
  final extent = line + theme.itemPadding.vertical > theme.itemHeight
      ? line + theme.itemPadding.vertical
      : theme.itemHeight;
  return extent.ceilToDouble();
}

Color getListItemTextColor(BuildContext context, ListThemeExtension widgetTheme,
    bool isSelected, bool enabled,
    {VColor? foreground}) {
  if (!enabled) return widgetTheme.disabledTextColor;
  final defaultColor =
      isSelected ? widgetTheme.selectedItemTextColor : widgetTheme.textColor;
  return getForegroundColor(
      context: context,
      foreground: foreground,
      defaultColor: defaultColor,
      minContrast: widgetTheme.minTextContrast);
}