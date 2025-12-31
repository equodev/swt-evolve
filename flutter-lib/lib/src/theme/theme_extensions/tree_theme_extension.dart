import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'tree_theme_extension.tailor.dart';
part 'tree_theme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
@DurationConverter()
@TextStyleConverter()
@EdgeInsetsConverter()
@CurveConverter()
class TreeThemeExtension extends ThemeExtension<TreeThemeExtension> with _$TreeThemeExtensionTailorMixin {
  // Background colors
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color hoverBackgroundColor;
  final Color selectedBackgroundColor;
  
  // Item colors
  final Color itemTextColor;
  final Color itemSelectedTextColor;
  final Color itemDisabledTextColor;
  final Color itemHoverBackgroundColor;
  final Color itemSelectedBorderColor;
  final double itemSelectedBorderWidth;
  
  // Header colors
  final Color headerBackgroundColor;
  final Color headerTextColor;
  final Color headerBorderColor;
  
  // Icon colors
  final Color expandIconColor;
  final Color expandIconHoverColor;
  final Color expandIconDisabledColor;
  final Color itemIconColor;
  final Color itemIconSelectedColor;
  final Color itemIconDisabledColor;
  
  // Checkbox colors
  final Color checkboxColor;
  final Color checkboxSelectedColor;
  final Color checkboxBorderColor;
  final Color checkboxCheckmarkColor;
  final Color checkboxDisabledColor;
  final double checkboxBorderWidth;
  final double checkboxBorderRadius;
  final double checkboxCheckmarkSizeMultiplier;
  final double checkboxGrayedMarginMultiplier;
  final double checkboxGrayedBorderRadius;
  
  // Badge colors
  final Color badgeBackgroundColor;
  final Color badgeTextColor;
  final Color badgeBorderColor;
  
  // Sizes
  final double itemHeight;
  final double itemIndent;
  final double expandIconSize;
  final double itemIconSize;
  final double checkboxSize;
  final double badgeSize;
  final double badgeBorderRadius;
  final double badgeBorderWidth;
  
  // Spacing and padding
  final EdgeInsets itemPadding;
  final double expandIconSpacing;
  final double itemIconSpacing;
  final double checkboxSpacing;
  final double badgeSpacing;
  
  // Header properties
  final double headerHeight;
  final EdgeInsets headerPadding;
  final double headerBorderWidth;
  final double headerColumnBorderVerticalMargin;
  
  // Font styles
  final TextStyle? itemTextStyle;
  final TextStyle? headerTextStyle;
  final TextStyle? badgeTextStyle;
  final TextStyle? columnTextStyle;
  
  // Animation properties
  final Duration hoverAnimationDuration;
  final Curve hoverAnimationCurve;
  final Duration selectionAnimationDuration;
  final Curve selectionAnimationCurve;
  
  // Border properties
  final double borderWidth;
  final Color borderColor;
  final double borderRadius;
  
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
  
  // Cell properties
  final EdgeInsets cellPadding;
  final EdgeInsets cellMultiColumnPadding;
  
  // Event defaults
  final double eventDefaultWidth;
  final double eventDefaultHeight;
  final int eventDefaultX;
  final int eventDefaultY;
  final int eventDefaultDetail;
  
  const TreeThemeExtension({
    required this.backgroundColor,
    required this.disabledBackgroundColor,
    required this.hoverBackgroundColor,
    required this.selectedBackgroundColor,
    required this.itemTextColor,
    required this.itemSelectedTextColor,
    required this.itemDisabledTextColor,
    required this.itemHoverBackgroundColor,
    required this.itemSelectedBorderColor,
    required this.itemSelectedBorderWidth,
    required this.headerBackgroundColor,
    required this.headerTextColor,
    required this.headerBorderColor,
    required this.expandIconColor,
    required this.expandIconHoverColor,
    required this.expandIconDisabledColor,
    required this.itemIconColor,
    required this.itemIconSelectedColor,
    required this.itemIconDisabledColor,
    required this.checkboxColor,
    required this.checkboxSelectedColor,
    required this.checkboxBorderColor,
    required this.checkboxCheckmarkColor,
    required this.checkboxDisabledColor,
    required this.checkboxBorderWidth,
    required this.checkboxBorderRadius,
    required this.checkboxCheckmarkSizeMultiplier,
    required this.checkboxGrayedMarginMultiplier,
    required this.checkboxGrayedBorderRadius,
    required this.badgeBackgroundColor,
    required this.badgeTextColor,
    required this.badgeBorderColor,
    required this.itemHeight,
    required this.itemIndent,
    required this.expandIconSize,
    required this.itemIconSize,
    required this.checkboxSize,
    required this.badgeSize,
    required this.badgeBorderRadius,
    required this.badgeBorderWidth,
    required this.itemPadding,
    required this.expandIconSpacing,
    required this.itemIconSpacing,
    required this.checkboxSpacing,
    required this.badgeSpacing,
    required this.headerHeight,
    required this.headerPadding,
    required this.headerBorderWidth,
    required this.headerColumnBorderVerticalMargin,
    this.itemTextStyle,
    this.headerTextStyle,
    this.badgeTextStyle,
    this.columnTextStyle,
    required this.hoverAnimationDuration,
    required this.hoverAnimationCurve,
    required this.selectionAnimationDuration,
    required this.selectionAnimationCurve,
    required this.borderWidth,
    required this.borderColor,
    required this.borderRadius,
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
    required this.cellPadding,
    required this.cellMultiColumnPadding,
    required this.eventDefaultWidth,
    required this.eventDefaultHeight,
    required this.eventDefaultX,
    required this.eventDefaultY,
    required this.eventDefaultDetail,
  });

  factory TreeThemeExtension.fromJson(Map<String, dynamic> json) =>
      _$TreeThemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$TreeThemeExtensionToJson(this);
}
