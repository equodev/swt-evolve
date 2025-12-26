import 'package:flutter/material.dart';
import 'tree_theme_extension.dart';
import '../gen/tree.dart';
import '../gen/treeitem.dart';
import '../gen/swt.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../impl/color_utils.dart';
import '../impl/utils/font_utils.dart';
import '../impl/widget_config.dart';

TreeThemeExtension getTreeLightTheme() {
  return TreeThemeExtension(
    backgroundColor: const Color(0xFFFFFFFF),
    disabledBackgroundColor: const Color(0xFFF9FAFB),
    hoverBackgroundColor: const Color(0xFFF3F4F6),
    selectedBackgroundColor: const Color(0xFFE8E8FF),
    
    itemTextColor: const Color(0xFF111827),
    itemSelectedTextColor: const Color(0xFF111827),
    itemDisabledTextColor: const Color(0xFF9CA3AF),
    itemHoverBackgroundColor: const Color(0xFFF0F0FD),
    itemSelectedBorderColor: const Color(0xFFA9A9F7),
    itemSelectedBorderWidth: 2.0,
    
    headerBackgroundColor: const Color(0xFFF3F4F6),
    headerTextColor: const Color(0xFF111827),
    headerBorderColor: const Color(0xFFD1D5DB),
    
    expandIconColor: const Color(0xFF6B7280),
    expandIconHoverColor: const Color(0xFF374151),
    expandIconDisabledColor: const Color(0xFF9CA3AF),
    
    itemIconColor: const Color(0xFF6B7280),
    itemIconSelectedColor: const Color(0xFF6B7280),
    itemIconDisabledColor: const Color(0xFF9CA3AF),
    
    checkboxColor: const Color(0xFFFFFFFF),
    checkboxSelectedColor: const Color(0xFF4F46E5),
    checkboxBorderColor: const Color(0xFFD1D5DB),
    checkboxCheckmarkColor: const Color(0xFFFFFFFF),
    checkboxDisabledColor: const Color(0xFFE5E7EB),
    checkboxBorderWidth: 2.0,
    checkboxBorderRadius: 4.0,
    checkboxCheckmarkSizeMultiplier: 0.7,
    checkboxGrayedMarginMultiplier: 0.3,
    checkboxGrayedBorderRadius: 2.0,
    
    badgeBackgroundColor: const Color(0xFFE0F2FE),
    badgeTextColor: const Color(0xFF0369A1),
    badgeBorderColor: const Color(0xFF7DD3FC),
    
    itemHeight: 24.0,
    itemIndent: 16.0,
    expandIconSize: 16.0,
    itemIconSize: 16.0,
    checkboxSize: 18.0,
    badgeSize: 18.0,
    badgeBorderRadius: 4.0,
    badgeBorderWidth: 1.0,
    
    itemPadding: const EdgeInsets.symmetric(horizontal: 6.0, vertical: 2.0),
    expandIconSpacing: 4.0,
    itemIconSpacing: 4.0,
    checkboxSpacing: 4.0,
    badgeSpacing: 8.0,
    
    headerHeight: 32.0,
    headerPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6.0),
    headerBorderWidth: 2.0,
    headerColumnBorderVerticalMargin: 8.0,
    
    fontSize: 14.0,
    fontWeight: FontWeight.w400,
    fontFamily: null,
    letterSpacing: 0.0,
    lineHeight: 1.4,
    
    headerFontSize: 14.0,
    headerFontWeight: FontWeight.w500,
    headerFontFamily: null,
    headerLetterSpacing: 0.0,
    
    badgeFontSize: 11.0,
    badgeFontWeight: FontWeight.w500,
    
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    selectionAnimationDuration: const Duration(milliseconds: 200),
    selectionAnimationCurve: Curves.easeInOut,
    
    borderWidth: 1.0,
    borderColor: const Color(0xFFE5E7EB),
    borderRadius: 0.0,
    
    columnTextColor: const Color(0xFF111827),
    columnBackgroundColor: const Color(0xFFF3F4F6),
    columnDraggingBackgroundColor: const Color(0xFFE5E7EB),
    columnBorderColor: const Color(0xFF9CA3AF),
    columnRightBorderColor: const Color(0xFFD1D5DB),
    columnResizeHandleColor: const Color(0xFF9CA3AF),
    
    columnPadding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 6.0),
    columnBorderWidth: 1.0,
    columnResizeHandleWidth: 4.0,
    columnResizeHandleMargin: const EdgeInsets.symmetric(horizontal: 1.5),
    columnDefaultWidth: 100.0,
    columnMinWidth: 50.0,
    columnMaxWidth: 500.0,
    columnDragThreshold: 10.0,
    
    columnFontSize: 14.0,
    columnFontWeight: FontWeight.w500,
    
    cellPadding: const EdgeInsets.symmetric(vertical: 2.0, horizontal: 4.0),
    cellMultiColumnPadding: const EdgeInsets.symmetric(vertical: 2.0, horizontal: 4.0),
    
    eventDefaultWidth: 100.0,
    eventDefaultHeight: 20.0,
    eventDefaultX: 0,
    eventDefaultY: 0,
    eventDefaultDetail: 0,
  );
}

TreeThemeExtension getTreeDarkTheme() {
  return TreeThemeExtension(
    backgroundColor: const Color(0xFF1E1E1E),
    disabledBackgroundColor: const Color(0xFF2B2B2B),
    hoverBackgroundColor: const Color(0xFF2B2B2B),
    selectedBackgroundColor: const Color(0xFF3C3C3C),
    
    itemTextColor: const Color(0xFFF9FAFB),
    itemSelectedTextColor: const Color(0xFFF9FAFB),
    itemDisabledTextColor: const Color(0xFF6B7280),
    itemHoverBackgroundColor: const Color(0xFF3A3A5A),
    itemSelectedBorderColor: const Color(0xFFA9A9F7),
    itemSelectedBorderWidth: 2.0,
    
    headerBackgroundColor: const Color(0xFF2B2B2B),
    headerTextColor: const Color(0xFFF9FAFB),
    headerBorderColor: const Color(0xFF4B5563),
    
    expandIconColor: const Color(0xFF9CA3AF),
    expandIconHoverColor: const Color(0xFFD1D5DB),
    expandIconDisabledColor: const Color(0xFF6B7280),
    
    itemIconColor: const Color(0xFF9CA3AF),
    itemIconSelectedColor: const Color(0xFF9CA3AF),
    itemIconDisabledColor: const Color(0xFF6B7280),
    
    checkboxColor: const Color(0xFF1F2937),
    checkboxSelectedColor: const Color(0xFF4F46E5),
    checkboxBorderColor: const Color(0xFF4B5563),
    checkboxCheckmarkColor: const Color(0xFFFFFFFF),
    checkboxDisabledColor: const Color(0xFF374151),
    checkboxBorderWidth: 2.0,
    checkboxBorderRadius: 4.0,
    checkboxCheckmarkSizeMultiplier: 0.7,
    checkboxGrayedMarginMultiplier: 0.3,
    checkboxGrayedBorderRadius: 2.0,
    
    badgeBackgroundColor: const Color(0xFF1E3A5F),
    badgeTextColor: const Color(0xFF7DD3FC),
    badgeBorderColor: const Color(0xFF0369A1),
    
    itemHeight: 24.0,
    itemIndent: 16.0,
    expandIconSize: 16.0,
    itemIconSize: 16.0,
    checkboxSize: 18.0,
    badgeSize: 18.0,
    badgeBorderRadius: 4.0,
    badgeBorderWidth: 1.0,
    
    itemPadding: const EdgeInsets.symmetric(horizontal: 6.0, vertical: 2.0),
    expandIconSpacing: 4.0,
    itemIconSpacing: 4.0,
    checkboxSpacing: 4.0,
    badgeSpacing: 8.0,
    
    headerHeight: 32.0,
    headerPadding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6.0),
    headerBorderWidth: 2.0,
    headerColumnBorderVerticalMargin: 8.0,
    
    fontSize: 14.0,
    fontWeight: FontWeight.w400,
    fontFamily: null,
    letterSpacing: 0.0,
    lineHeight: 1.4,
    
    headerFontSize: 14.0,
    headerFontWeight: FontWeight.w500,
    headerFontFamily: null,
    headerLetterSpacing: 0.0,
    
    badgeFontSize: 11.0,
    badgeFontWeight: FontWeight.w500,
    
    hoverAnimationDuration: const Duration(milliseconds: 150),
    hoverAnimationCurve: Curves.easeInOut,
    selectionAnimationDuration: const Duration(milliseconds: 200),
    selectionAnimationCurve: Curves.easeInOut,
    
    borderWidth: 1.0,
    borderColor: const Color(0xFF4B5563),
    borderRadius: 0.0,
    
    columnTextColor: const Color(0xFFF9FAFB),
    columnBackgroundColor: const Color(0xFF2B2B2B),
    columnDraggingBackgroundColor: const Color(0xFF4A4A4A),
    columnBorderColor: const Color(0xFF4B5563),
    columnRightBorderColor: const Color(0xFF374151),
    columnResizeHandleColor: const Color(0xFF6B7280),
    
    columnPadding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 6.0),
    columnBorderWidth: 1.0,
    columnResizeHandleWidth: 4.0,
    columnResizeHandleMargin: const EdgeInsets.symmetric(horizontal: 1.5),
    columnDefaultWidth: 100.0,
    columnMinWidth: 50.0,
    columnMaxWidth: 500.0,
    columnDragThreshold: 10.0,
    
    columnFontSize: 14.0,
    columnFontWeight: FontWeight.w500,
    
    cellPadding: const EdgeInsets.symmetric(vertical: 2.0, horizontal: 4.0),
    cellMultiColumnPadding: const EdgeInsets.symmetric(vertical: 2.0, horizontal: 4.0),
    
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
  ColorScheme colorScheme,
) {
  final enabled = state.enabled ?? true;
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.background != null) {
    return colorFromVColor(state.background, defaultColor: enabled
        ? widgetTheme.backgroundColor
        : widgetTheme.disabledBackgroundColor);
  }
  
  return enabled
      ? widgetTheme.backgroundColor
      : widgetTheme.disabledBackgroundColor;
}

Color getTreeItemTextColor(
  VTreeItem state,
  TreeThemeExtension widgetTheme,
  ColorScheme colorScheme,
  bool selected,
  bool enabled,
) {
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.foreground != null) {
    return colorFromVColor(state.foreground, defaultColor: enabled
        ? (selected
            ? widgetTheme.itemSelectedTextColor
            : widgetTheme.itemTextColor)
        : widgetTheme.itemDisabledTextColor);
  }
  
  return enabled
      ? (selected
          ? widgetTheme.itemSelectedTextColor
          : widgetTheme.itemTextColor)
      : widgetTheme.itemDisabledTextColor;
}

Color getTreeItemBackgroundColor(
  VTreeItem state,
  TreeThemeExtension widgetTheme,
  ColorScheme colorScheme,
  bool selected,
  bool hovered,
  bool enabled,
) {
  final useSwtColors = getConfigFlags().use_swt_colors ?? false;
  
  if (useSwtColors && state.background != null) {
    return colorFromVColor(state.background, defaultColor: enabled
        ? (selected
            ? widgetTheme.selectedBackgroundColor
            : (hovered
                ? widgetTheme.itemHoverBackgroundColor
                : Colors.transparent))
        : widgetTheme.disabledBackgroundColor);
  }
  
  return enabled
      ? (selected
          ? widgetTheme.selectedBackgroundColor
          : (hovered
              ? widgetTheme.itemHoverBackgroundColor
              : Colors.transparent))
      : widgetTheme.disabledBackgroundColor;
}

TextStyle getTreeItemTextStyle(
  BuildContext context,
  VTreeItem state,
  TreeThemeExtension widgetTheme,
  ColorScheme colorScheme,
  TextTheme textTheme,
  Color textColor,
  VFont? treeFont,
) {
  final useSwtFonts = getConfigFlags().use_swt_fonts ?? false;
  
  if (useSwtFonts && state.font != null) {
    return FontUtils.textStyleFromVFont(
      state.font,
      context,
      color: textColor,
    );
  }
  
  if (useSwtFonts && treeFont != null) {
    return FontUtils.textStyleFromVFont(
      treeFont,
      context,
      color: textColor,
    );
  }
  
  return TextStyle(
    color: textColor,
    fontSize: widgetTheme.fontSize,
    fontWeight: widgetTheme.fontWeight,
    fontFamily: widgetTheme.fontFamily,
    letterSpacing: widgetTheme.letterSpacing,
    height: widgetTheme.lineHeight,
  );
}

