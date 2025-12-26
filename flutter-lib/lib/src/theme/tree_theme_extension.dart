import 'package:flutter/material.dart';

class TreeThemeExtension extends ThemeExtension<TreeThemeExtension> {
  final Color backgroundColor;
  final Color disabledBackgroundColor;
  final Color hoverBackgroundColor;
  final Color selectedBackgroundColor;
  
  final Color itemTextColor;
  final Color itemSelectedTextColor;
  final Color itemDisabledTextColor;
  final Color itemHoverBackgroundColor;
  final Color itemSelectedBorderColor;
  final double itemSelectedBorderWidth;
  
  final Color headerBackgroundColor;
  final Color headerTextColor;
  final Color headerBorderColor;
  
  final Color expandIconColor;
  final Color expandIconHoverColor;
  final Color expandIconDisabledColor;
  
  final Color itemIconColor;
  final Color itemIconSelectedColor;
  final Color itemIconDisabledColor;
  
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
  
  final Color badgeBackgroundColor;
  final Color badgeTextColor;
  final Color badgeBorderColor;
  
  final double itemHeight;
  final double itemIndent;
  final double expandIconSize;
  final double itemIconSize;
  final double checkboxSize;
  final double badgeSize;
  final double badgeBorderRadius;
  final double badgeBorderWidth;
  
  final EdgeInsets itemPadding;
  final double expandIconSpacing;
  final double itemIconSpacing;
  final double checkboxSpacing;
  final double badgeSpacing;
  
  final double headerHeight;
  final EdgeInsets headerPadding;
  final double headerBorderWidth;
  final double headerColumnBorderVerticalMargin;
  
  final double fontSize;
  final FontWeight fontWeight;
  final String? fontFamily;
  final double letterSpacing;
  final double lineHeight;
  
  final double headerFontSize;
  final FontWeight headerFontWeight;
  final String? headerFontFamily;
  final double headerLetterSpacing;
  
  final double badgeFontSize;
  final FontWeight badgeFontWeight;
  
  final Duration hoverAnimationDuration;
  final Curve hoverAnimationCurve;
  final Duration selectionAnimationDuration;
  final Curve selectionAnimationCurve;
  
  final double borderWidth;
  final Color borderColor;
  final double borderRadius;
  
  final Color columnTextColor;
  final Color columnBackgroundColor;
  final Color columnDraggingBackgroundColor;
  final Color columnBorderColor;
  final Color columnRightBorderColor;
  final Color columnResizeHandleColor;
  
  final EdgeInsets columnPadding;
  final double columnBorderWidth;
  final double columnResizeHandleWidth;
  final EdgeInsets columnResizeHandleMargin;
  final double columnDefaultWidth;
  final double columnMinWidth;
  final double columnMaxWidth;
  final double columnDragThreshold;
  
  final double columnFontSize;
  final FontWeight columnFontWeight;
  
  final EdgeInsets cellPadding;
  final EdgeInsets cellMultiColumnPadding;
  
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
    required this.fontSize,
    required this.fontWeight,
    this.fontFamily,
    required this.letterSpacing,
    required this.lineHeight,
    required this.headerFontSize,
    required this.headerFontWeight,
    this.headerFontFamily,
    required this.headerLetterSpacing,
    required this.badgeFontSize,
    required this.badgeFontWeight,
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
    required this.columnFontSize,
    required this.columnFontWeight,
    required this.cellPadding,
    required this.cellMultiColumnPadding,
    required this.eventDefaultWidth,
    required this.eventDefaultHeight,
    required this.eventDefaultX,
    required this.eventDefaultY,
    required this.eventDefaultDetail,
  });

  @override
  ThemeExtension<TreeThemeExtension> copyWith({
    Color? backgroundColor,
    Color? disabledBackgroundColor,
    Color? hoverBackgroundColor,
    Color? selectedBackgroundColor,
    Color? itemTextColor,
    Color? itemSelectedTextColor,
    Color? itemDisabledTextColor,
    Color? itemHoverBackgroundColor,
    Color? itemSelectedBorderColor,
    double? itemSelectedBorderWidth,
    Color? headerBackgroundColor,
    Color? headerTextColor,
    Color? headerBorderColor,
    Color? expandIconColor,
    Color? expandIconHoverColor,
    Color? expandIconDisabledColor,
    Color? itemIconColor,
    Color? itemIconSelectedColor,
    Color? itemIconDisabledColor,
    Color? checkboxColor,
    Color? checkboxSelectedColor,
    Color? checkboxBorderColor,
    Color? checkboxCheckmarkColor,
    Color? checkboxDisabledColor,
    double? checkboxBorderWidth,
    double? checkboxBorderRadius,
    double? checkboxCheckmarkSizeMultiplier,
    double? checkboxGrayedMarginMultiplier,
    double? checkboxGrayedBorderRadius,
    Color? badgeBackgroundColor,
    Color? badgeTextColor,
    Color? badgeBorderColor,
    double? itemHeight,
    double? itemIndent,
    double? expandIconSize,
    double? itemIconSize,
    double? checkboxSize,
    double? badgeSize,
    double? badgeBorderRadius,
    double? badgeBorderWidth,
    EdgeInsets? itemPadding,
    double? expandIconSpacing,
    double? itemIconSpacing,
    double? checkboxSpacing,
    double? badgeSpacing,
    double? headerHeight,
    EdgeInsets? headerPadding,
    double? headerBorderWidth,
    double? headerColumnBorderVerticalMargin,
    double? fontSize,
    FontWeight? fontWeight,
    String? fontFamily,
    double? letterSpacing,
    double? lineHeight,
    double? headerFontSize,
    FontWeight? headerFontWeight,
    String? headerFontFamily,
    double? headerLetterSpacing,
    double? badgeFontSize,
    FontWeight? badgeFontWeight,
    Duration? hoverAnimationDuration,
    Curve? hoverAnimationCurve,
    Duration? selectionAnimationDuration,
    Curve? selectionAnimationCurve,
    double? borderWidth,
    Color? borderColor,
    double? borderRadius,
    Color? columnTextColor,
    Color? columnBackgroundColor,
    Color? columnDraggingBackgroundColor,
    Color? columnBorderColor,
    Color? columnRightBorderColor,
    Color? columnResizeHandleColor,
    EdgeInsets? columnPadding,
    double? columnBorderWidth,
    double? columnResizeHandleWidth,
    EdgeInsets? columnResizeHandleMargin,
    double? columnDefaultWidth,
    double? columnMinWidth,
    double? columnMaxWidth,
    double? columnDragThreshold,
    double? columnFontSize,
    FontWeight? columnFontWeight,
    EdgeInsets? cellPadding,
    EdgeInsets? cellMultiColumnPadding,
    double? eventDefaultWidth,
    double? eventDefaultHeight,
    int? eventDefaultX,
    int? eventDefaultY,
    int? eventDefaultDetail,
  }) {
    return TreeThemeExtension(
      backgroundColor: backgroundColor ?? this.backgroundColor,
      disabledBackgroundColor: disabledBackgroundColor ?? this.disabledBackgroundColor,
      hoverBackgroundColor: hoverBackgroundColor ?? this.hoverBackgroundColor,
      selectedBackgroundColor: selectedBackgroundColor ?? this.selectedBackgroundColor,
      itemTextColor: itemTextColor ?? this.itemTextColor,
      itemSelectedTextColor: itemSelectedTextColor ?? this.itemSelectedTextColor,
      itemDisabledTextColor: itemDisabledTextColor ?? this.itemDisabledTextColor,
      itemHoverBackgroundColor: itemHoverBackgroundColor ?? this.itemHoverBackgroundColor,
      itemSelectedBorderColor: itemSelectedBorderColor ?? this.itemSelectedBorderColor,
      itemSelectedBorderWidth: itemSelectedBorderWidth ?? this.itemSelectedBorderWidth,
      headerBackgroundColor: headerBackgroundColor ?? this.headerBackgroundColor,
      headerTextColor: headerTextColor ?? this.headerTextColor,
      headerBorderColor: headerBorderColor ?? this.headerBorderColor,
      expandIconColor: expandIconColor ?? this.expandIconColor,
      expandIconHoverColor: expandIconHoverColor ?? this.expandIconHoverColor,
      expandIconDisabledColor: expandIconDisabledColor ?? this.expandIconDisabledColor,
      itemIconColor: itemIconColor ?? this.itemIconColor,
      itemIconSelectedColor: itemIconSelectedColor ?? this.itemIconSelectedColor,
      itemIconDisabledColor: itemIconDisabledColor ?? this.itemIconDisabledColor,
      checkboxColor: checkboxColor ?? this.checkboxColor,
      checkboxSelectedColor: checkboxSelectedColor ?? this.checkboxSelectedColor,
      checkboxBorderColor: checkboxBorderColor ?? this.checkboxBorderColor,
      checkboxCheckmarkColor: checkboxCheckmarkColor ?? this.checkboxCheckmarkColor,
      checkboxDisabledColor: checkboxDisabledColor ?? this.checkboxDisabledColor,
      checkboxBorderWidth: checkboxBorderWidth ?? this.checkboxBorderWidth,
      checkboxBorderRadius: checkboxBorderRadius ?? this.checkboxBorderRadius,
      checkboxCheckmarkSizeMultiplier: checkboxCheckmarkSizeMultiplier ?? this.checkboxCheckmarkSizeMultiplier,
      checkboxGrayedMarginMultiplier: checkboxGrayedMarginMultiplier ?? this.checkboxGrayedMarginMultiplier,
      checkboxGrayedBorderRadius: checkboxGrayedBorderRadius ?? this.checkboxGrayedBorderRadius,
      badgeBackgroundColor: badgeBackgroundColor ?? this.badgeBackgroundColor,
      badgeTextColor: badgeTextColor ?? this.badgeTextColor,
      badgeBorderColor: badgeBorderColor ?? this.badgeBorderColor,
      itemHeight: itemHeight ?? this.itemHeight,
      itemIndent: itemIndent ?? this.itemIndent,
      expandIconSize: expandIconSize ?? this.expandIconSize,
      itemIconSize: itemIconSize ?? this.itemIconSize,
      checkboxSize: checkboxSize ?? this.checkboxSize,
      badgeSize: badgeSize ?? this.badgeSize,
      badgeBorderRadius: badgeBorderRadius ?? this.badgeBorderRadius,
      badgeBorderWidth: badgeBorderWidth ?? this.badgeBorderWidth,
      itemPadding: itemPadding ?? this.itemPadding,
      expandIconSpacing: expandIconSpacing ?? this.expandIconSpacing,
      itemIconSpacing: itemIconSpacing ?? this.itemIconSpacing,
      checkboxSpacing: checkboxSpacing ?? this.checkboxSpacing,
      badgeSpacing: badgeSpacing ?? this.badgeSpacing,
      headerHeight: headerHeight ?? this.headerHeight,
      headerPadding: headerPadding ?? this.headerPadding,
      headerBorderWidth: headerBorderWidth ?? this.headerBorderWidth,
      headerColumnBorderVerticalMargin: headerColumnBorderVerticalMargin ?? this.headerColumnBorderVerticalMargin,
      fontSize: fontSize ?? this.fontSize,
      fontWeight: fontWeight ?? this.fontWeight,
      fontFamily: fontFamily ?? this.fontFamily,
      letterSpacing: letterSpacing ?? this.letterSpacing,
      lineHeight: lineHeight ?? this.lineHeight,
      headerFontSize: headerFontSize ?? this.headerFontSize,
      headerFontWeight: headerFontWeight ?? this.headerFontWeight,
      headerFontFamily: headerFontFamily ?? this.headerFontFamily,
      headerLetterSpacing: headerLetterSpacing ?? this.headerLetterSpacing,
      badgeFontSize: badgeFontSize ?? this.badgeFontSize,
      badgeFontWeight: badgeFontWeight ?? this.badgeFontWeight,
      hoverAnimationDuration: hoverAnimationDuration ?? this.hoverAnimationDuration,
      hoverAnimationCurve: hoverAnimationCurve ?? this.hoverAnimationCurve,
      selectionAnimationDuration: selectionAnimationDuration ?? this.selectionAnimationDuration,
      selectionAnimationCurve: selectionAnimationCurve ?? this.selectionAnimationCurve,
      borderWidth: borderWidth ?? this.borderWidth,
      borderColor: borderColor ?? this.borderColor,
      borderRadius: borderRadius ?? this.borderRadius,
      columnTextColor: columnTextColor ?? this.columnTextColor,
      columnBackgroundColor: columnBackgroundColor ?? this.columnBackgroundColor,
      columnDraggingBackgroundColor: columnDraggingBackgroundColor ?? this.columnDraggingBackgroundColor,
      columnBorderColor: columnBorderColor ?? this.columnBorderColor,
      columnRightBorderColor: columnRightBorderColor ?? this.columnRightBorderColor,
      columnResizeHandleColor: columnResizeHandleColor ?? this.columnResizeHandleColor,
      columnPadding: columnPadding ?? this.columnPadding,
      columnBorderWidth: columnBorderWidth ?? this.columnBorderWidth,
      columnResizeHandleWidth: columnResizeHandleWidth ?? this.columnResizeHandleWidth,
      columnResizeHandleMargin: columnResizeHandleMargin ?? this.columnResizeHandleMargin,
      columnDefaultWidth: columnDefaultWidth ?? this.columnDefaultWidth,
      columnMinWidth: columnMinWidth ?? this.columnMinWidth,
      columnMaxWidth: columnMaxWidth ?? this.columnMaxWidth,
      columnDragThreshold: columnDragThreshold ?? this.columnDragThreshold,
      columnFontSize: columnFontSize ?? this.columnFontSize,
      columnFontWeight: columnFontWeight ?? this.columnFontWeight,
      cellPadding: cellPadding ?? this.cellPadding,
      cellMultiColumnPadding: cellMultiColumnPadding ?? this.cellMultiColumnPadding,
      eventDefaultWidth: eventDefaultWidth ?? this.eventDefaultWidth,
      eventDefaultHeight: eventDefaultHeight ?? this.eventDefaultHeight,
      eventDefaultX: eventDefaultX ?? this.eventDefaultX,
      eventDefaultY: eventDefaultY ?? this.eventDefaultY,
      eventDefaultDetail: eventDefaultDetail ?? this.eventDefaultDetail,
    );
  }

  @override
  ThemeExtension<TreeThemeExtension> lerp(
    ThemeExtension<TreeThemeExtension>? other,
    double t,
  ) {
    if (other is! TreeThemeExtension) {
      return this;
    }

    return TreeThemeExtension(
      backgroundColor: Color.lerp(backgroundColor, other.backgroundColor, t)!,
      disabledBackgroundColor: Color.lerp(disabledBackgroundColor, other.disabledBackgroundColor, t)!,
      hoverBackgroundColor: Color.lerp(hoverBackgroundColor, other.hoverBackgroundColor, t)!,
      selectedBackgroundColor: Color.lerp(selectedBackgroundColor, other.selectedBackgroundColor, t)!,
      itemTextColor: Color.lerp(itemTextColor, other.itemTextColor, t)!,
      itemSelectedTextColor: Color.lerp(itemSelectedTextColor, other.itemSelectedTextColor, t)!,
      itemDisabledTextColor: Color.lerp(itemDisabledTextColor, other.itemDisabledTextColor, t)!,
      itemHoverBackgroundColor: Color.lerp(itemHoverBackgroundColor, other.itemHoverBackgroundColor, t)!,
      itemSelectedBorderColor: Color.lerp(itemSelectedBorderColor, other.itemSelectedBorderColor, t)!,
      itemSelectedBorderWidth: (itemSelectedBorderWidth * (1 - t) + other.itemSelectedBorderWidth * t),
      headerBackgroundColor: Color.lerp(headerBackgroundColor, other.headerBackgroundColor, t)!,
      headerTextColor: Color.lerp(headerTextColor, other.headerTextColor, t)!,
      headerBorderColor: Color.lerp(headerBorderColor, other.headerBorderColor, t)!,
      expandIconColor: Color.lerp(expandIconColor, other.expandIconColor, t)!,
      expandIconHoverColor: Color.lerp(expandIconHoverColor, other.expandIconHoverColor, t)!,
      expandIconDisabledColor: Color.lerp(expandIconDisabledColor, other.expandIconDisabledColor, t)!,
      itemIconColor: Color.lerp(itemIconColor, other.itemIconColor, t)!,
      itemIconSelectedColor: Color.lerp(itemIconSelectedColor, other.itemIconSelectedColor, t)!,
      itemIconDisabledColor: Color.lerp(itemIconDisabledColor, other.itemIconDisabledColor, t)!,
      checkboxColor: Color.lerp(checkboxColor, other.checkboxColor, t)!,
      checkboxSelectedColor: Color.lerp(checkboxSelectedColor, other.checkboxSelectedColor, t)!,
      checkboxBorderColor: Color.lerp(checkboxBorderColor, other.checkboxBorderColor, t)!,
      checkboxCheckmarkColor: Color.lerp(checkboxCheckmarkColor, other.checkboxCheckmarkColor, t)!,
      checkboxDisabledColor: Color.lerp(checkboxDisabledColor, other.checkboxDisabledColor, t)!,
      checkboxBorderWidth: (checkboxBorderWidth * (1 - t) + other.checkboxBorderWidth * t),
      checkboxBorderRadius: (checkboxBorderRadius * (1 - t) + other.checkboxBorderRadius * t),
      checkboxCheckmarkSizeMultiplier: (checkboxCheckmarkSizeMultiplier * (1 - t) + other.checkboxCheckmarkSizeMultiplier * t),
      checkboxGrayedMarginMultiplier: (checkboxGrayedMarginMultiplier * (1 - t) + other.checkboxGrayedMarginMultiplier * t),
      checkboxGrayedBorderRadius: (checkboxGrayedBorderRadius * (1 - t) + other.checkboxGrayedBorderRadius * t),
      badgeBackgroundColor: Color.lerp(badgeBackgroundColor, other.badgeBackgroundColor, t)!,
      badgeTextColor: Color.lerp(badgeTextColor, other.badgeTextColor, t)!,
      badgeBorderColor: Color.lerp(badgeBorderColor, other.badgeBorderColor, t)!,
      itemHeight: (itemHeight * (1 - t) + other.itemHeight * t),
      itemIndent: (itemIndent * (1 - t) + other.itemIndent * t),
      expandIconSize: (expandIconSize * (1 - t) + other.expandIconSize * t),
      itemIconSize: (itemIconSize * (1 - t) + other.itemIconSize * t),
      checkboxSize: (checkboxSize * (1 - t) + other.checkboxSize * t),
      badgeSize: (badgeSize * (1 - t) + other.badgeSize * t),
      badgeBorderRadius: (badgeBorderRadius * (1 - t) + other.badgeBorderRadius * t),
      badgeBorderWidth: (badgeBorderWidth * (1 - t) + other.badgeBorderWidth * t),
      itemPadding: EdgeInsets.lerp(itemPadding, other.itemPadding, t)!,
      expandIconSpacing: (expandIconSpacing * (1 - t) + other.expandIconSpacing * t),
      itemIconSpacing: (itemIconSpacing * (1 - t) + other.itemIconSpacing * t),
      checkboxSpacing: (checkboxSpacing * (1 - t) + other.checkboxSpacing * t),
      badgeSpacing: (badgeSpacing * (1 - t) + other.badgeSpacing * t),
      headerHeight: (headerHeight * (1 - t) + other.headerHeight * t),
      headerPadding: EdgeInsets.lerp(headerPadding, other.headerPadding, t)!,
      headerBorderWidth: (headerBorderWidth * (1 - t) + other.headerBorderWidth * t),
      headerColumnBorderVerticalMargin: (headerColumnBorderVerticalMargin * (1 - t) + other.headerColumnBorderVerticalMargin * t),
      fontSize: (fontSize * (1 - t) + other.fontSize * t),
      fontWeight: FontWeight.lerp(fontWeight, other.fontWeight, t)!,
      fontFamily: t < 0.5 ? fontFamily : other.fontFamily,
      letterSpacing: (letterSpacing * (1 - t) + other.letterSpacing * t),
      lineHeight: (lineHeight * (1 - t) + other.lineHeight * t),
      headerFontSize: (headerFontSize * (1 - t) + other.headerFontSize * t),
      headerFontWeight: FontWeight.lerp(headerFontWeight, other.headerFontWeight, t)!,
      headerFontFamily: t < 0.5 ? headerFontFamily : other.headerFontFamily,
      headerLetterSpacing: (headerLetterSpacing * (1 - t) + other.headerLetterSpacing * t),
      badgeFontSize: (badgeFontSize * (1 - t) + other.badgeFontSize * t),
      badgeFontWeight: FontWeight.lerp(badgeFontWeight, other.badgeFontWeight, t)!,
      hoverAnimationDuration: Duration(
        milliseconds: (hoverAnimationDuration.inMilliseconds * (1 - t) + 
                     other.hoverAnimationDuration.inMilliseconds * t).round(),
      ),
      hoverAnimationCurve: t < 0.5 ? hoverAnimationCurve : other.hoverAnimationCurve,
      selectionAnimationDuration: Duration(
        milliseconds: (selectionAnimationDuration.inMilliseconds * (1 - t) + 
                     other.selectionAnimationDuration.inMilliseconds * t).round(),
      ),
      selectionAnimationCurve: t < 0.5 ? selectionAnimationCurve : other.selectionAnimationCurve,
      borderWidth: (borderWidth * (1 - t) + other.borderWidth * t),
      borderColor: Color.lerp(borderColor, other.borderColor, t)!,
      borderRadius: (borderRadius * (1 - t) + other.borderRadius * t),
      columnTextColor: Color.lerp(columnTextColor, other.columnTextColor, t)!,
      columnBackgroundColor: Color.lerp(columnBackgroundColor, other.columnBackgroundColor, t)!,
      columnDraggingBackgroundColor: Color.lerp(columnDraggingBackgroundColor, other.columnDraggingBackgroundColor, t)!,
      columnBorderColor: Color.lerp(columnBorderColor, other.columnBorderColor, t)!,
      columnRightBorderColor: Color.lerp(columnRightBorderColor, other.columnRightBorderColor, t)!,
      columnResizeHandleColor: Color.lerp(columnResizeHandleColor, other.columnResizeHandleColor, t)!,
      columnPadding: EdgeInsets.lerp(columnPadding, other.columnPadding, t)!,
      columnBorderWidth: (columnBorderWidth * (1 - t) + other.columnBorderWidth * t),
      columnResizeHandleWidth: (columnResizeHandleWidth * (1 - t) + other.columnResizeHandleWidth * t),
      columnResizeHandleMargin: EdgeInsets.lerp(columnResizeHandleMargin, other.columnResizeHandleMargin, t)!,
      columnDefaultWidth: (columnDefaultWidth * (1 - t) + other.columnDefaultWidth * t),
      columnMinWidth: (columnMinWidth * (1 - t) + other.columnMinWidth * t),
      columnMaxWidth: (columnMaxWidth * (1 - t) + other.columnMaxWidth * t),
      columnDragThreshold: (columnDragThreshold * (1 - t) + other.columnDragThreshold * t),
      columnFontSize: (columnFontSize * (1 - t) + other.columnFontSize * t),
      columnFontWeight: FontWeight.lerp(columnFontWeight, other.columnFontWeight, t)!,
      cellPadding: EdgeInsets.lerp(cellPadding, other.cellPadding, t)!,
      cellMultiColumnPadding: EdgeInsets.lerp(cellMultiColumnPadding, other.cellMultiColumnPadding, t)!,
      eventDefaultWidth: (eventDefaultWidth * (1 - t) + other.eventDefaultWidth * t),
      eventDefaultHeight: (eventDefaultHeight * (1 - t) + other.eventDefaultHeight * t),
      eventDefaultX: (eventDefaultX * (1 - t) + other.eventDefaultX * t).round(),
      eventDefaultY: (eventDefaultY * (1 - t) + other.eventDefaultY * t).round(),
      eventDefaultDetail: (eventDefaultDetail * (1 - t) + other.eventDefaultDetail * t).round(),
    );
  }
}

