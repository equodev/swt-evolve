import 'package:flutter/material.dart';
import '../theme_extensions/table_theme_extension.dart';
import '../theme_extensions/color_scheme_extension.dart';
import '../../gen/table.dart';
import '../../gen/tableitem.dart';
import '../../gen/tablecolumn.dart';
import '../../gen/swt.dart';
import '../../gen/color.dart';
import '../../gen/font.dart';
import '../../impl/color_utils.dart';
import '../../impl/utils/font_utils.dart';
import '../../impl/utils/widget_utils.dart';
import '../../impl/widget_config.dart';

TableThemeExtension getTableLightTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTableTheme(
    isDark: false,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TableThemeExtension getTableDarkTheme({
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  return _getTableTheme(
    isDark: true,
    colorScheme: colorScheme,
    textTheme: textTheme,
    colorSchemeExtension: colorSchemeExtension,
  );
}

TableThemeExtension _getTableTheme({
  required bool isDark,
  required ColorScheme colorScheme,
  required TextTheme textTheme,
  required ColorSchemeExtension colorSchemeExtension,
}) {
  final baseTextStyle = textTheme.bodyMedium ?? const TextStyle();
  final headerTextStyle = textTheme.titleSmall ?? baseTextStyle;
  
  return TableThemeExtension(
    // Background colors
    backgroundColor: colorScheme.surface,
    disabledBackgroundColor: colorScheme.surfaceVariant,
    hoverBackgroundColor: colorScheme.surfaceVariant,
    selectedBackgroundColor: colorScheme.primaryContainer.withOpacity(0.2),
    alternateRowBackgroundColor: isDark 
        ? colorScheme.surfaceVariant.withOpacity(0.5)
        : colorScheme.surfaceVariant.withOpacity(0.3),
    
    // Row colors
    rowTextColor: colorScheme.onSurface,
    rowSelectedTextColor: colorScheme.onSurface,
    rowDisabledTextColor: colorSchemeExtension.onSurfaceVariantDisabled,
    rowHoverBackgroundColor: colorScheme.surfaceVariant,
    rowSelectedBorderColor: colorScheme.primary,
    rowSelectedBorderWidth: 2.0,
    
    // Header colors
    headerBackgroundColor: colorScheme.surfaceVariant,
    headerTextColor: isDark 
        ? colorScheme.onSurface.withOpacity(0.6)
        : colorScheme.onSurface.withOpacity(0.7),
    headerBorderColor: colorScheme.outline,
    rowSeparatorColor: isDark
        ? colorScheme.outlineVariant.withOpacity(0.5)
        : colorScheme.outlineVariant.withOpacity(0.3),
    
    // Border colors
    borderColor: colorScheme.outline,
    linesColor: colorScheme.outlineVariant,
    
    // Sizes - calculated dynamically based on text and font
    rowHeight: 0.0,
    headerHeight: 0.0,
    
    // Spacing and padding
    rowPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
    headerPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
    cellPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
    
    // Header properties
    headerBorderWidth: 2.0,
    
    // Font styles
    rowTextStyle: baseTextStyle.copyWith(color: colorScheme.onSurface),
    headerTextStyle: headerTextStyle.copyWith(color: colorScheme.onSurface),
    
    // Animation properties
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    selectionAnimationDuration: const Duration(milliseconds: 200),
    selectionAnimationCurve: Curves.easeInOut,
    
    // Border properties
    borderWidth: 2.0,
    borderRadius: 0.0,
    linesWidth: 1.0,
    
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
    
    // Event defaults
    eventDefaultWidth: 100.0,
    eventDefaultHeight: 20.0,
    eventDefaultX: 0,
    eventDefaultY: 0,
    eventDefaultDetail: 0,
  );
}

Color getTableBackgroundColor(
  VTable state,
  TableThemeExtension widgetTheme,
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

Color getTableRowTextColor(
  VTableItem state,
  TableThemeExtension widgetTheme,
  bool selected,
  bool enabled,
) {
  final defaultColor = enabled
      ? (selected
          ? widgetTheme.rowSelectedTextColor
          : widgetTheme.rowTextColor)
      : widgetTheme.rowDisabledTextColor;
  
  return getForegroundColor(
    foreground: state.foreground,
    defaultColor: defaultColor,
  );
}

Color getTableRowBackgroundColor(
  VTableItem state,
  TableThemeExtension widgetTheme,
  bool selected,
  bool hovered,
  bool enabled,
  bool isAlternateRow,
) {
  final defaultColor = enabled
      ? (selected
          ? widgetTheme.selectedBackgroundColor
          : (hovered
              ? widgetTheme.rowHoverBackgroundColor
              : (isAlternateRow
                  ? widgetTheme.alternateRowBackgroundColor
                  : Colors.transparent)))
      : widgetTheme.disabledBackgroundColor;
  
  return getBackgroundColor(
    background: state.background,
    defaultColor: defaultColor,
  ) ?? defaultColor;
}

Color getTableHeaderTextColor(
  VTable state,
  TableThemeExtension widgetTheme,
) {
  final defaultColor = widgetTheme.headerTextColor;
  
  return getForegroundColor(
    foreground: state.headerForeground,
    defaultColor: defaultColor,
  );
}

Color getTableHeaderBackgroundColor(
  VTable state,
  TableThemeExtension widgetTheme,
) {
  final defaultColor = widgetTheme.headerBackgroundColor;
  
  return getBackgroundColor(
    background: state.headerBackground,
    defaultColor: defaultColor,
  ) ?? defaultColor;
}

