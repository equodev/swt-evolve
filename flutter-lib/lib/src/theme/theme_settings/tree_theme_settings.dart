import 'package:flutter/material.dart';
import '../theme_extensions/tree_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/tree.dart';
import '../../gen/treeitem.dart';
import '../../gen/swt.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/font_utils.dart';
import '../../impl/utils/widget_utils.dart';
import '../../impl/widget_config.dart';

TreeThemeExtension getTreeLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTreeTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TreeThemeExtension getTreeDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTreeTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TreeThemeExtension _getTreeTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  final headerTextStyle = textTheme.titleSmall ?? baseTextStyle;
  final badgeTextStyle = textTheme.labelSmall ?? baseTextStyle;
  final columnTextStyle = textTheme.titleSmall ?? baseTextStyle;
  
  return TreeThemeExtension(
    // Background colors
    backgroundColor: colorScheme.surface,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    hoverBackgroundColor: colorScheme.surfaceVariant,
    selectedBackgroundColor: colorScheme.primaryContainer.withOpacity(0.2),
    
    // Item colors
    itemTextColor: colorScheme.onSurface,
    itemSelectedTextColor: colorScheme.onSurface,
    itemDisabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    itemHoverBackgroundColor: colorScheme.surfaceVariant,
    itemSelectedBorderColor: colorScheme.primary,
    itemSelectedBorderWidth: 2.0,
    
    // Header colors
    headerBackgroundColor: colorScheme.surfaceVariant,
    headerTextColor: colorScheme.onSurface,
    headerBorderColor: colorScheme.outline,
    
    // Icon colors
    expandIconColor: colorScheme.onSurfaceVariant,
    expandIconHoverColor: colorScheme.onSurface,
    expandIconDisabledColor: colorSchemeExtension.onSurfaceVariantDisabled,
    itemIconColor: colorScheme.onSurfaceVariant,
    itemIconSelectedColor: colorScheme.onSurfaceVariant,
    itemIconDisabledColor: colorSchemeExtension.onSurfaceVariantDisabled,
    
    // Checkbox colors
    checkboxColor: colorScheme.surface,
    checkboxSelectedColor: colorScheme.primary,
    checkboxBorderColor: colorScheme.outline,
    checkboxCheckmarkColor: colorScheme.onPrimary,
    checkboxDisabledColor: colorScheme.surfaceVariant,
    checkboxBorderWidth: 2.0,
    checkboxBorderRadius: 4.0,
    checkboxCheckmarkSizeMultiplier: 0.7,
    checkboxGrayedMarginMultiplier: 0.3,
    checkboxGrayedBorderRadius: 2.0,
    
    // Badge colors
    badgeBackgroundColor: colorScheme.primaryContainer,
    badgeTextColor: colorScheme.onPrimaryContainer,
    badgeBorderColor: colorScheme.primary,
    
    // Sizes
    itemHeight: 24.0,
    itemIndent: 16.0,
    expandIconSize: 16.0,
    itemIconSize: 16.0,
    checkboxSize: 18.0,
    badgeSize: 18.0,
    badgeBorderRadius: 4.0,
    badgeBorderWidth: 1.0,
    
    // Spacing and padding
    itemPadding: const EdgeInsets.symmetric(horizontal: 6.0, vertical: 2.0),
    expandIconSpacing: 4.0,
    itemIconSpacing: 4.0,
    checkboxSpacing: 4.0,
    badgeSpacing: 8.0,
    
    // Header properties
    headerHeight: 32.0,
    headerPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6.0),
    headerBorderWidth: 2.0,
    headerColumnBorderVerticalMargin: 8.0,
    
    // Font styles
    itemTextStyle: baseTextStyle.copyWith(color: colorScheme.onSurface),
    headerTextStyle: headerTextStyle.copyWith(color: colorScheme.onSurface),
    badgeTextStyle: badgeTextStyle.copyWith(color: colorScheme.onPrimaryContainer),
    columnTextStyle: columnTextStyle.copyWith(color: colorScheme.onSurface),
    
    // Animation properties
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    selectionAnimationDuration: const Duration(milliseconds: 200),
    selectionAnimationCurve: Curves.easeInOut,
    
    // Border properties
    borderWidth: 1.0,
    borderColor: colorScheme.outline,
    borderRadius: 0.0,
    
    // Column colors
    columnTextColor: colorScheme.onSurface,
    columnBackgroundColor: colorScheme.surfaceVariant,
    columnDraggingBackgroundColor: colorScheme.surfaceVariant.withOpacity(0.8),
    columnBorderColor: colorScheme.outline,
    columnRightBorderColor: colorScheme.outlineVariant,
    columnResizeHandleColor: colorScheme.onSurfaceVariant,
    
    // Column properties
    columnPadding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 6.0),
    columnBorderWidth: 1.0,
    columnResizeHandleWidth: 4.0,
    columnResizeHandleMargin: const EdgeInsets.symmetric(horizontal: 1.5),
    columnDefaultWidth: 100.0,
    columnMinWidth: 50.0,
    columnMaxWidth: 500.0,
    columnDragThreshold: 10.0,
    
    // Cell properties
    cellPadding: const EdgeInsets.symmetric(vertical: 2.0, horizontal: 4.0),
    cellMultiColumnPadding: const EdgeInsets.symmetric(vertical: 2.0, horizontal: 4.0),
    
    // Event defaults
    eventDefaultWidth: 100.0,
    eventDefaultHeight: 20.0,
    eventDefaultX: 0,
    eventDefaultY: 0,
    eventDefaultDetail: 0,
  );
}

Color getTreeBackgroundColor(
  VTree state,
  TreeThemeExtension widgetTheme,
) {
  final enabled = state.enabled ?? true;
  final defaultColor = enabled
      ? widgetTheme.backgroundColor
      : widgetTheme.disabledBackgroundColor;
  
  return getBackgroundColor(
    background: state.background,
    defaultColor: defaultColor,
  ) ?? defaultColor;
}

Color getTreeItemTextColor(
  VTreeItem state,
  TreeThemeExtension widgetTheme,
  bool selected,
  bool enabled,
) {
  final defaultColor = enabled
      ? (selected
          ? widgetTheme.itemSelectedTextColor
          : widgetTheme.itemTextColor)
      : widgetTheme.itemDisabledTextColor;
  
  return getForegroundColor(
    foreground: state.foreground,
    defaultColor: defaultColor,
  );
}

Color getTreeItemBackgroundColor(
  VTreeItem state,
  TreeThemeExtension widgetTheme,
  bool selected,
  bool hovered,
  bool enabled,
) {
  final defaultColor = enabled
      ? (selected
          ? widgetTheme.selectedBackgroundColor
          : (hovered
              ? widgetTheme.itemHoverBackgroundColor
              : Colors.transparent))
      : widgetTheme.disabledBackgroundColor;
  
  return getBackgroundColor(
    background: state.background,
    defaultColor: defaultColor,
  ) ?? defaultColor;
}

