// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'table.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTable _$VTableFromJson(Map<String, dynamic> json) => VTable()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..backgroundImage = json['backgroundImage'] == null
      ? null
      : VImage.fromJson(json['backgroundImage'] as Map<String, dynamic>)
  ..bounds = json['bounds'] == null
      ? null
      : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>)
  ..capture = json['capture'] as bool?
  ..dragDetect = json['dragDetect'] as bool?
  ..enabled = json['enabled'] as bool?
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..redraw = json['redraw'] as bool?
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..visible = json['visible'] as bool?
  ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
  ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..layoutDeferred = json['layoutDeferred'] as bool?
  ..tabList = (json['tabList'] as List<dynamic>?)
      ?.map((e) => VControl.fromJson(e as Map<String, dynamic>))
      .toList()
  ..columnOrder = (json['columnOrder'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..columns = (json['columns'] as List<dynamic>?)
      ?.map((e) => VTableColumn.fromJson(e as Map<String, dynamic>))
      .toList()
  ..headerBackground = json['headerBackground'] == null
      ? null
      : VColor.fromJson(json['headerBackground'] as Map<String, dynamic>)
  ..headerForeground = json['headerForeground'] == null
      ? null
      : VColor.fromJson(json['headerForeground'] as Map<String, dynamic>)
  ..headerVisible = json['headerVisible'] as bool?
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VTableItem.fromJson(e as Map<String, dynamic>))
      .toList()
  ..linesVisible = json['linesVisible'] as bool?
  ..selection = (json['selection'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..sortColumn = json['sortColumn'] == null
      ? null
      : VTableColumn.fromJson(json['sortColumn'] as Map<String, dynamic>)
  ..sortDirection = (json['sortDirection'] as num?)?.toInt()
  ..topIndex = (json['topIndex'] as num?)?.toInt();

Map<String, dynamic> _$VTableToJson(VTable instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'background': instance.background,
      'backgroundImage': instance.backgroundImage,
      'bounds': instance.bounds,
      'capture': instance.capture,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'foreground': instance.foreground,
      'orientation': instance.orientation,
      'redraw': instance.redraw,
      'textDirection': instance.textDirection,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'visible': instance.visible,
      'scrollbarsMode': instance.scrollbarsMode,
      'backgroundMode': instance.backgroundMode,
      'children': instance.children,
      'layoutDeferred': instance.layoutDeferred,
      'tabList': instance.tabList,
      'columnOrder': instance.columnOrder,
      'columns': instance.columns,
      'headerBackground': instance.headerBackground,
      'headerForeground': instance.headerForeground,
      'headerVisible': instance.headerVisible,
      'items': instance.items,
      'linesVisible': instance.linesVisible,
      'selection': instance.selection,
      'sortColumn': instance.sortColumn,
      'sortDirection': instance.sortDirection,
      'topIndex': instance.topIndex,
    };
