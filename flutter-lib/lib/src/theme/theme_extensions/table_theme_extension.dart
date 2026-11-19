import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';

part 'table_theme_extension.tailor.dart';

@TailorMixin()
class TableThemeExtension extends ThemeExtension<TableThemeExtension> with _$TableThemeExtensionTailorMixin {
  // Background colors
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color hoverBackgroundColor;
  final Color selectedBackgroundColor;
  final Color alternateRowBackgroundColor;
  
  // Row colors
  final Color rowTextColor;
  final Color rowSelectedTextColor;
  final Color rowDisabledTextColor;
  final Color rowHoverBackgroundColor;
  final Color rowSelectedBorderColor;
  final double rowSelectedBorderWidth;

  /// The colour an owner-drawn row's own fill is painted in: the application colour that fills most
  /// of the Table's rows. Null paints every owner-drawn fill as the application gives it.
  final Color? ownerDrawnRowFillColor;

  /// The colour an owner-drawn row's hover fill is painted in: the application colour an unselected
  /// row is repainted in instead of its own fill. Null, or no [ownerDrawnRowFillColor], paints it as
  /// the application gives it.
  final Color? ownerDrawnRowHoverFillColor;

  // Drag and drop
  final double dropFeedbackLineWidth;
  final int dropFeedbackHighlightAlpha;
  final double dragImageOpacity;
  
  // Header colors
  final Color headerBackgroundColor;
  final Color headerTextColor;
  final Color headerBorderColor;
  final Color rowSeparatorColor;
  
  // Border colors
  final Color borderColor;
  final Color linesColor;
  
  // Sizes
  final double rowHeight;

  /// Grows the content of the cell under the pointer, the same feedback a hovered ToolItem gives.
  final bool cellHoverZoomEnabled;
  final double cellHoverZoomScale;
  final Duration cellHoverZoomDuration;
  final double headerHeight;
  
  // Spacing and padding
  final EdgeInsets rowPadding;
  final EdgeInsets headerPadding;
  final EdgeInsets cellPadding;
  // A cell's padding when the application turned native item heights off (VTable.compactRows).
  final EdgeInsets compactCellPadding;

  // Header properties
  final double headerBorderWidth;
  
  // Font styles
  final TextStyle? rowTextStyle;
  final TextStyle? headerTextStyle;
  
  // Animation properties
  final Duration hoverAnimationDuration;
  final Curve hoverAnimationCurve;
  final Duration selectionAnimationDuration;
  final Curve selectionAnimationCurve;
  
  // Border properties
  final double borderWidth;
  final double borderRadius;
  final double linesWidth;
  
  // Column colors
  final Color columnTextColor;
  final Color columnBackgroundColor;
  final Color columnDraggingBackgroundColor;
  final Color columnBorderColor;
  final Color columnRightBorderColor;
  final Color columnResizeHandleColor;
  
  // Column properties
  final EdgeInsets columnPadding;
  final double columnBorderWidth;
  final double columnResizeHandleWidth;
  final EdgeInsets columnResizeHandleMargin;
  final double columnDefaultWidth;
  final double columnMinWidth;
  final double columnMaxWidth;
  final double columnDragThreshold;
  
  // Event defaults
  final double eventDefaultWidth;
  final double eventDefaultHeight;
  final int eventDefaultX;
  final int eventDefaultY;
  final int eventDefaultDetail;

  final double minTextContrast;

  const TableThemeExtension({
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.hoverBackgroundColor,
    required this.selectedBackgroundColor,
    required this.alternateRowBackgroundColor,
    required this.rowTextColor,
    required this.rowSelectedTextColor,
    required this.rowDisabledTextColor,
    required this.rowHoverBackgroundColor,
    required this.rowSelectedBorderColor,
    required this.rowSelectedBorderWidth,
    required this.dropFeedbackLineWidth,
    required this.dropFeedbackHighlightAlpha,
    required this.dragImageOpacity,
    required this.headerBackgroundColor,
    required this.headerTextColor,
    required this.headerBorderColor,
    required this.rowSeparatorColor,
    required this.borderColor,
    required this.linesColor,
    required this.rowHeight,
    required this.cellHoverZoomEnabled,
    required this.cellHoverZoomScale,
    required this.cellHoverZoomDuration,
    required this.headerHeight,
    required this.rowPadding,
    required this.headerPadding,
    required this.cellPadding,
    required this.compactCellPadding,
    required this.headerBorderWidth,
    this.rowTextStyle,
    this.headerTextStyle,
    required this.hoverAnimationDuration,
    required this.hoverAnimationCurve,
    required this.selectionAnimationDuration,
    required this.selectionAnimationCurve,
    required this.borderWidth,
    required this.borderRadius,
    required this.linesWidth,
    required this.columnTextColor,
    required this.columnBackgroundColor,
    required this.columnDraggingBackgroundColor,
    required this.columnBorderColor,
    required this.columnRightBorderColor,
    required this.columnResizeHandleColor,
    required this.columnPadding,
    required this.columnBorderWidth,
    required this.columnResizeHandleWidth,
    required this.columnResizeHandleMargin,
    required this.columnDefaultWidth,
    required this.columnMinWidth,
    required this.columnMaxWidth,
    required this.columnDragThreshold,
    required this.eventDefaultWidth,
    required this.eventDefaultHeight,
    required this.eventDefaultX,
    required this.eventDefaultY,
    required this.eventDefaultDetail,
    required this.minTextContrast,
    this.ownerDrawnRowFillColor,
    this.ownerDrawnRowHoverFillColor,
  });

  /// The theme a Table's rows are laid out with: a compact Table's cells take [compactCellPadding].
  TableThemeExtension forRows({required bool compact}) =>
      compact ? copyWith(cellPadding: compactCellPadding) : this;
}

