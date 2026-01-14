import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'table_theme_extension.tailor.dart';
part 'table_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
@CurveConverter()
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
  final double headerHeight;
  
  // Spacing and padding
  final EdgeInsets rowPadding;
  final EdgeInsets headerPadding;
  final EdgeInsets cellPadding;
  
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
    required this.headerBackgroundColor,
    required this.headerTextColor,
    required this.headerBorderColor,
    required this.rowSeparatorColor,
    required this.borderColor,
    required this.linesColor,
    required this.rowHeight,
    required this.headerHeight,
    required this.rowPadding,
    required this.headerPadding,
    required this.cellPadding,
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
  });

  factory TableThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TableThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TableThemeExtensionToJson(this);
}

