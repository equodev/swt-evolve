// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'table_theme_extension.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TableThemeExtension _$TableThemeExtensionFromJson(
  Map<String, dynamic> json,
) => TableThemeExtension(
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
  alternateRowBackgroundColor: const ColorConverter().fromJson(
    json['alternateRowBackgroundColor'] as String,
  ),
  rowTextColor: const ColorConverter().fromJson(json['rowTextColor'] as String),
  rowSelectedTextColor: const ColorConverter().fromJson(
    json['rowSelectedTextColor'] as String,
  ),
  rowDisabledTextColor: const ColorConverter().fromJson(
    json['rowDisabledTextColor'] as String,
  ),
  rowHoverBackgroundColor: const ColorConverter().fromJson(
    json['rowHoverBackgroundColor'] as String,
  ),
  rowSelectedBorderColor: const ColorConverter().fromJson(
    json['rowSelectedBorderColor'] as String,
  ),
  rowSelectedBorderWidth: (json['rowSelectedBorderWidth'] as num).toDouble(),
  headerBackgroundColor: const ColorConverter().fromJson(
    json['headerBackgroundColor'] as String,
  ),
  headerTextColor: const ColorConverter().fromJson(
    json['headerTextColor'] as String,
  ),
  headerBorderColor: const ColorConverter().fromJson(
    json['headerBorderColor'] as String,
  ),
  rowSeparatorColor: const ColorConverter().fromJson(
    json['rowSeparatorColor'] as String,
  ),
  borderColor: const ColorConverter().fromJson(json['borderColor'] as String),
  linesColor: const ColorConverter().fromJson(json['linesColor'] as String),
  rowHeight: (json['rowHeight'] as num).toDouble(),
  headerHeight: (json['headerHeight'] as num).toDouble(),
  rowPadding: const EdgeInsetsConverter().fromJson(
    json['rowPadding'] as Map<String, dynamic>,
  ),
  headerPadding: const EdgeInsetsConverter().fromJson(
    json['headerPadding'] as Map<String, dynamic>,
  ),
  cellPadding: const EdgeInsetsConverter().fromJson(
    json['cellPadding'] as Map<String, dynamic>,
  ),
  headerBorderWidth: (json['headerBorderWidth'] as num).toDouble(),
  rowTextStyle: const TextStyleConverter().fromJson(
    json['rowTextStyle'] as Map<String, dynamic>?,
  ),
  headerTextStyle: const TextStyleConverter().fromJson(
    json['headerTextStyle'] as Map<String, dynamic>?,
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
  borderRadius: (json['borderRadius'] as num).toDouble(),
  linesWidth: (json['linesWidth'] as num).toDouble(),
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
  eventDefaultWidth: (json['eventDefaultWidth'] as num).toDouble(),
  eventDefaultHeight: (json['eventDefaultHeight'] as num).toDouble(),
  eventDefaultX: (json['eventDefaultX'] as num).toInt(),
  eventDefaultY: (json['eventDefaultY'] as num).toInt(),
  eventDefaultDetail: (json['eventDefaultDetail'] as num).toInt(),
);

Map<String, dynamic> _$TableThemeExtensionToJson(
  TableThemeExtension instance,
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
  'alternateRowBackgroundColor': const ColorConverter().toJson(
    instance.alternateRowBackgroundColor,
  ),
  'rowTextColor': const ColorConverter().toJson(instance.rowTextColor),
  'rowSelectedTextColor': const ColorConverter().toJson(
    instance.rowSelectedTextColor,
  ),
  'rowDisabledTextColor': const ColorConverter().toJson(
    instance.rowDisabledTextColor,
  ),
  'rowHoverBackgroundColor': const ColorConverter().toJson(
    instance.rowHoverBackgroundColor,
  ),
  'rowSelectedBorderColor': const ColorConverter().toJson(
    instance.rowSelectedBorderColor,
  ),
  'rowSelectedBorderWidth': instance.rowSelectedBorderWidth,
  'headerBackgroundColor': const ColorConverter().toJson(
    instance.headerBackgroundColor,
  ),
  'headerTextColor': const ColorConverter().toJson(instance.headerTextColor),
  'headerBorderColor': const ColorConverter().toJson(
    instance.headerBorderColor,
  ),
  'rowSeparatorColor': const ColorConverter().toJson(
    instance.rowSeparatorColor,
  ),
  'borderColor': const ColorConverter().toJson(instance.borderColor),
  'linesColor': const ColorConverter().toJson(instance.linesColor),
  'rowHeight': instance.rowHeight,
  'headerHeight': instance.headerHeight,
  'rowPadding': const EdgeInsetsConverter().toJson(instance.rowPadding),
  'headerPadding': const EdgeInsetsConverter().toJson(instance.headerPadding),
  'cellPadding': const EdgeInsetsConverter().toJson(instance.cellPadding),
  'headerBorderWidth': instance.headerBorderWidth,
  'rowTextStyle': const TextStyleConverter().toJson(instance.rowTextStyle),
  'headerTextStyle': const TextStyleConverter().toJson(
    instance.headerTextStyle,
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
  'borderRadius': instance.borderRadius,
  'linesWidth': instance.linesWidth,
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
  'eventDefaultWidth': instance.eventDefaultWidth,
  'eventDefaultHeight': instance.eventDefaultHeight,
  'eventDefaultX': instance.eventDefaultX,
  'eventDefaultY': instance.eventDefaultY,
  'eventDefaultDetail': instance.eventDefaultDetail,
};
