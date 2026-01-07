// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tree_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TreeThemeExtension _$TreeThemeExtensionFromJson(
  Map<String, dynamic> json,
) => TreeThemeExtension(
  backgroundColor: const ColorConverter().fromJson(
    json['backgroundColor'] as String,
  ),
  disabledBackgroundColor: const ColorConverter().fromJson(
    json['disabledBackgroundColor'] as String,
  ),
  hoverBackgroundColor: const ColorConverter().fromJson(
    json['hoverBackgroundColor'] as String,
  ),
  selectedBackgroundColor: const ColorConverter().fromJson(
    json['selectedBackgroundColor'] as String,
  ),
  itemTextColor: const ColorConverter().fromJson(
    json['itemTextColor'] as String,
  ),
  itemSelectedTextColor: const ColorConverter().fromJson(
    json['itemSelectedTextColor'] as String,
  ),
  itemDisabledTextColor: const ColorConverter().fromJson(
    json['itemDisabledTextColor'] as String,
  ),
  itemHoverBackgroundColor: const ColorConverter().fromJson(
    json['itemHoverBackgroundColor'] as String,
  ),
  itemSelectedBorderColor: const ColorConverter().fromJson(
    json['itemSelectedBorderColor'] as String,
  ),
  itemSelectedBorderWidth: (json['itemSelectedBorderWidth'] as num).toDouble(),
  headerBackgroundColor: const ColorConverter().fromJson(
    json['headerBackgroundColor'] as String,
  ),
  headerTextColor: const ColorConverter().fromJson(
    json['headerTextColor'] as String,
  ),
  headerBorderColor: const ColorConverter().fromJson(
    json['headerBorderColor'] as String,
  ),
  expandIconColor: const ColorConverter().fromJson(
    json['expandIconColor'] as String,
  ),
  expandIconHoverColor: const ColorConverter().fromJson(
    json['expandIconHoverColor'] as String,
  ),
  expandIconDisabledColor: const ColorConverter().fromJson(
    json['expandIconDisabledColor'] as String,
  ),
  itemIconColor: const ColorConverter().fromJson(
    json['itemIconColor'] as String,
  ),
  itemIconSelectedColor: const ColorConverter().fromJson(
    json['itemIconSelectedColor'] as String,
  ),
  itemIconDisabledColor: const ColorConverter().fromJson(
    json['itemIconDisabledColor'] as String,
  ),
  checkboxColor: const ColorConverter().fromJson(
    json['checkboxColor'] as String,
  ),
  checkboxSelectedColor: const ColorConverter().fromJson(
    json['checkboxSelectedColor'] as String,
  ),
  checkboxBorderColor: const ColorConverter().fromJson(
    json['checkboxBorderColor'] as String,
  ),
  checkboxCheckmarkColor: const ColorConverter().fromJson(
    json['checkboxCheckmarkColor'] as String,
  ),
  checkboxDisabledColor: const ColorConverter().fromJson(
    json['checkboxDisabledColor'] as String,
  ),
  checkboxBorderWidth: (json['checkboxBorderWidth'] as num).toDouble(),
  checkboxBorderRadius: (json['checkboxBorderRadius'] as num).toDouble(),
  checkboxCheckmarkSizeMultiplier:
      (json['checkboxCheckmarkSizeMultiplier'] as num).toDouble(),
  checkboxGrayedMarginMultiplier:
      (json['checkboxGrayedMarginMultiplier'] as num).toDouble(),
  checkboxGrayedBorderRadius: (json['checkboxGrayedBorderRadius'] as num)
      .toDouble(),
  badgeBackgroundColor: const ColorConverter().fromJson(
    json['badgeBackgroundColor'] as String,
  ),
  badgeTextColor: const ColorConverter().fromJson(
    json['badgeTextColor'] as String,
  ),
  badgeBorderColor: const ColorConverter().fromJson(
    json['badgeBorderColor'] as String,
  ),
  itemHeight: (json['itemHeight'] as num).toDouble(),
  itemIndent: (json['itemIndent'] as num).toDouble(),
  expandIconSize: (json['expandIconSize'] as num).toDouble(),
  itemIconSize: (json['itemIconSize'] as num).toDouble(),
  checkboxSize: (json['checkboxSize'] as num).toDouble(),
  badgeSize: (json['badgeSize'] as num).toDouble(),
  badgeBorderRadius: (json['badgeBorderRadius'] as num).toDouble(),
  badgeBorderWidth: (json['badgeBorderWidth'] as num).toDouble(),
  itemPadding: const EdgeInsetsConverter().fromJson(
    json['itemPadding'] as Map<String, dynamic>,
  ),
  expandIconSpacing: (json['expandIconSpacing'] as num).toDouble(),
  itemIconSpacing: (json['itemIconSpacing'] as num).toDouble(),
  checkboxSpacing: (json['checkboxSpacing'] as num).toDouble(),
  badgeSpacing: (json['badgeSpacing'] as num).toDouble(),
  headerHeight: (json['headerHeight'] as num).toDouble(),
  headerPadding: const EdgeInsetsConverter().fromJson(
    json['headerPadding'] as Map<String, dynamic>,
  ),
  headerBorderWidth: (json['headerBorderWidth'] as num).toDouble(),
  headerColumnBorderVerticalMargin:
      (json['headerColumnBorderVerticalMargin'] as num).toDouble(),
  itemTextStyle: const TextStyleConverter().fromJson(
    json['itemTextStyle'] as Map<String, dynamic>?,
  ),
  headerTextStyle: const TextStyleConverter().fromJson(
    json['headerTextStyle'] as Map<String, dynamic>?,
  ),
  badgeTextStyle: const TextStyleConverter().fromJson(
    json['badgeTextStyle'] as Map<String, dynamic>?,
  ),
  columnTextStyle: const TextStyleConverter().fromJson(
    json['columnTextStyle'] as Map<String, dynamic>?,
  ),
  hoverAnimationDuration: Duration(
    microseconds: (json['hoverAnimationDuration'] as num).toInt(),
  ),
  hoverAnimationCurve: const CurveConverter().fromJson(
    json['hoverAnimationCurve'] as String,
  ),
  selectionAnimationDuration: Duration(
    microseconds: (json['selectionAnimationDuration'] as num).toInt(),
  ),
  selectionAnimationCurve: const CurveConverter().fromJson(
    json['selectionAnimationCurve'] as String,
  ),
  borderWidth: (json['borderWidth'] as num).toDouble(),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  borderRadius: (json['borderRadius'] as num).toDouble(),
  columnTextColor: const ColorConverter().fromJson(
    json['columnTextColor'] as String,
  ),
  columnBackgroundColor: const ColorConverter().fromJson(
    json['columnBackgroundColor'] as String,
  ),
  columnDraggingBackgroundColor: const ColorConverter().fromJson(
    json['columnDraggingBackgroundColor'] as String,
  ),
  columnBorderColor: const ColorConverter().fromJson(
    json['columnBorderColor'] as String,
  ),
  columnRightBorderColor: const ColorConverter().fromJson(
    json['columnRightBorderColor'] as String,
  ),
  columnResizeHandleColor: const ColorConverter().fromJson(
    json['columnResizeHandleColor'] as String,
  ),
  columnPadding: const EdgeInsetsConverter().fromJson(
    json['columnPadding'] as Map<String, dynamic>,
  ),
  columnBorderWidth: (json['columnBorderWidth'] as num).toDouble(),
  columnResizeHandleWidth: (json['columnResizeHandleWidth'] as num).toDouble(),
  columnResizeHandleMargin: const EdgeInsetsConverter().fromJson(
    json['columnResizeHandleMargin'] as Map<String, dynamic>,
  ),
  columnDefaultWidth: (json['columnDefaultWidth'] as num).toDouble(),
  columnMinWidth: (json['columnMinWidth'] as num).toDouble(),
  columnMaxWidth: (json['columnMaxWidth'] as num).toDouble(),
  columnDragThreshold: (json['columnDragThreshold'] as num).toDouble(),
  cellPadding: const EdgeInsetsConverter().fromJson(
    json['cellPadding'] as Map<String, dynamic>,
  ),
  cellMultiColumnPadding: const EdgeInsetsConverter().fromJson(
    json['cellMultiColumnPadding'] as Map<String, dynamic>,
  ),
  eventDefaultWidth: (json['eventDefaultWidth'] as num).toDouble(),
  eventDefaultHeight: (json['eventDefaultHeight'] as num).toDouble(),
  eventDefaultX: (json['eventDefaultX'] as num).toInt(),
  eventDefaultY: (json['eventDefaultY'] as num).toInt(),
  eventDefaultDetail: (json['eventDefaultDetail'] as num).toInt(),
);

Map<String, dynamic> _$TreeThemeExtensionToJson(
  TreeThemeExtension instance,
) => <String, dynamic>{
  'backgroundColor': const ColorConverter().toJson(instance.backgroundColor),
  'disabledBackgroundColor': const ColorConverter().toJson(
    instance.disabledBackgroundColor,
  ),
  'hoverBackgroundColor': const ColorConverter().toJson(
    instance.hoverBackgroundColor,
  ),
  'selectedBackgroundColor': const ColorConverter().toJson(
    instance.selectedBackgroundColor,
  ),
  'itemTextColor': const ColorConverter().toJson(instance.itemTextColor),
  'itemSelectedTextColor': const ColorConverter().toJson(
    instance.itemSelectedTextColor,
  ),
  'itemDisabledTextColor': const ColorConverter().toJson(
    instance.itemDisabledTextColor,
  ),
  'itemHoverBackgroundColor': const ColorConverter().toJson(
    instance.itemHoverBackgroundColor,
  ),
  'itemSelectedBorderColor': const ColorConverter().toJson(
    instance.itemSelectedBorderColor,
  ),
  'itemSelectedBorderWidth': instance.itemSelectedBorderWidth,
  'headerBackgroundColor': const ColorConverter().toJson(
    instance.headerBackgroundColor,
  ),
  'headerTextColor': const ColorConverter().toJson(instance.headerTextColor),
  'headerBorderColor': const ColorConverter().toJson(
    instance.headerBorderColor,
  ),
  'expandIconColor': const ColorConverter().toJson(instance.expandIconColor),
  'expandIconHoverColor': const ColorConverter().toJson(
    instance.expandIconHoverColor,
  ),
  'expandIconDisabledColor': const ColorConverter().toJson(
    instance.expandIconDisabledColor,
  ),
  'itemIconColor': const ColorConverter().toJson(instance.itemIconColor),
  'itemIconSelectedColor': const ColorConverter().toJson(
    instance.itemIconSelectedColor,
  ),
  'itemIconDisabledColor': const ColorConverter().toJson(
    instance.itemIconDisabledColor,
  ),
  'checkboxColor': const ColorConverter().toJson(instance.checkboxColor),
  'checkboxSelectedColor': const ColorConverter().toJson(
    instance.checkboxSelectedColor,
  ),
  'checkboxBorderColor': const ColorConverter().toJson(
    instance.checkboxBorderColor,
  ),
  'checkboxCheckmarkColor': const ColorConverter().toJson(
    instance.checkboxCheckmarkColor,
  ),
  'checkboxDisabledColor': const ColorConverter().toJson(
    instance.checkboxDisabledColor,
  ),
  'checkboxBorderWidth': instance.checkboxBorderWidth,
  'checkboxBorderRadius': instance.checkboxBorderRadius,
  'checkboxCheckmarkSizeMultiplier': instance.checkboxCheckmarkSizeMultiplier,
  'checkboxGrayedMarginMultiplier': instance.checkboxGrayedMarginMultiplier,
  'checkboxGrayedBorderRadius': instance.checkboxGrayedBorderRadius,
  'badgeBackgroundColor': const ColorConverter().toJson(
    instance.badgeBackgroundColor,
  ),
  'badgeTextColor': const ColorConverter().toJson(instance.badgeTextColor),
  'badgeBorderColor': const ColorConverter().toJson(instance.badgeBorderColor),
  'itemHeight': instance.itemHeight,
  'itemIndent': instance.itemIndent,
  'expandIconSize': instance.expandIconSize,
  'itemIconSize': instance.itemIconSize,
  'checkboxSize': instance.checkboxSize,
  'badgeSize': instance.badgeSize,
  'badgeBorderRadius': instance.badgeBorderRadius,
  'badgeBorderWidth': instance.badgeBorderWidth,
  'itemPadding': const EdgeInsetsConverter().toJson(instance.itemPadding),
  'expandIconSpacing': instance.expandIconSpacing,
  'itemIconSpacing': instance.itemIconSpacing,
  'checkboxSpacing': instance.checkboxSpacing,
  'badgeSpacing': instance.badgeSpacing,
  'headerHeight': instance.headerHeight,
  'headerPadding': const EdgeInsetsConverter().toJson(instance.headerPadding),
  'headerBorderWidth': instance.headerBorderWidth,
  'headerColumnBorderVerticalMargin': instance.headerColumnBorderVerticalMargin,
  'itemTextStyle': const TextStyleConverter().toJson(instance.itemTextStyle),
  'headerTextStyle': const TextStyleConverter().toJson(
    instance.headerTextStyle,
  ),
  'badgeTextStyle': const TextStyleConverter().toJson(instance.badgeTextStyle),
  'columnTextStyle': const TextStyleConverter().toJson(
    instance.columnTextStyle,
  ),
  'hoverAnimationDuration': instance.hoverAnimationDuration.inMicroseconds,
  'hoverAnimationCurve': const CurveConverter().toJson(
    instance.hoverAnimationCurve,
  ),
  'selectionAnimationDuration':
      instance.selectionAnimationDuration.inMicroseconds,
  'selectionAnimationCurve': const CurveConverter().toJson(
    instance.selectionAnimationCurve,
  ),
  'borderWidth': instance.borderWidth,
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'borderRadius': instance.borderRadius,
  'columnTextColor': const ColorConverter().toJson(instance.columnTextColor),
  'columnBackgroundColor': const ColorConverter().toJson(
    instance.columnBackgroundColor,
  ),
  'columnDraggingBackgroundColor': const ColorConverter().toJson(
    instance.columnDraggingBackgroundColor,
  ),
  'columnBorderColor': const ColorConverter().toJson(
    instance.columnBorderColor,
  ),
  'columnRightBorderColor': const ColorConverter().toJson(
    instance.columnRightBorderColor,
  ),
  'columnResizeHandleColor': const ColorConverter().toJson(
    instance.columnResizeHandleColor,
  ),
  'columnPadding': const EdgeInsetsConverter().toJson(instance.columnPadding),
  'columnBorderWidth': instance.columnBorderWidth,
  'columnResizeHandleWidth': instance.columnResizeHandleWidth,
  'columnResizeHandleMargin': const EdgeInsetsConverter().toJson(
    instance.columnResizeHandleMargin,
  ),
  'columnDefaultWidth': instance.columnDefaultWidth,
  'columnMinWidth': instance.columnMinWidth,
  'columnMaxWidth': instance.columnMaxWidth,
  'columnDragThreshold': instance.columnDragThreshold,
  'cellPadding': const EdgeInsetsConverter().toJson(instance.cellPadding),
  'cellMultiColumnPadding': const EdgeInsetsConverter().toJson(
    instance.cellMultiColumnPadding,
  ),
  'eventDefaultWidth': instance.eventDefaultWidth,
  'eventDefaultHeight': instance.eventDefaultHeight,
  'eventDefaultX': instance.eventDefaultX,
  'eventDefaultY': instance.eventDefaultY,
  'eventDefaultDetail': instance.eventDefaultDetail,
};
