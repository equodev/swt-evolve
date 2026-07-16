// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tree.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTree _$VTreeFromJson(Map<String, dynamic> json) => VTree()
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
  ..cursor = json['cursor'] == null
      ? null
      : VCursor.fromJson(json['cursor'] as Map<String, dynamic>)
  ..dragDetect = json['dragDetect'] as bool?
  ..dragSource = json['dragSource'] as bool?
  ..dropTargetId = (json['dropTargetId'] as num?)?.toInt()
  ..enabled = json['enabled'] as bool?
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..menu = json['menu'] == null
      ? null
      : VMenu.fromJson(json['menu'] as Map<String, dynamic>)
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..redraw = json['redraw'] as bool?
  ..region = json['region'] == null
      ? null
      : VRegion.fromJson(json['region'] as Map<String, dynamic>)
  ..textDirection = (json['textDirection'] as num?)?.toInt()
  ..toolTipText = json['toolTipText'] as String?
  ..touchEnabled = json['touchEnabled'] as bool?
  ..visible = json['visible'] as bool?
  ..horizontalBar = json['horizontalBar'] == null
      ? null
      : VScrollBar.fromJson(json['horizontalBar'] as Map<String, dynamic>)
  ..scrollbarsMode = (json['scrollbarsMode'] as num?)?.toInt()
  ..verticalBar = json['verticalBar'] == null
      ? null
      : VScrollBar.fromJson(json['verticalBar'] as Map<String, dynamic>)
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
      ?.map((e) => VTreeColumn.fromJson(e as Map<String, dynamic>))
      .toList()
  ..editors = (json['editors'] as List<dynamic>?)
      ?.map((e) => VTreeEditor.fromJson(e as Map<String, dynamic>))
      .toList()
  ..headerBackground = json['headerBackground'] == null
      ? null
      : VColor.fromJson(json['headerBackground'] as Map<String, dynamic>)
  ..headerForeground = json['headerForeground'] == null
      ? null
      : VColor.fromJson(json['headerForeground'] as Map<String, dynamic>)
  ..headerVisible = json['headerVisible'] as bool?
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VTreeItem.fromJson(e as Map<String, dynamic>))
      .toList()
  ..linesVisible = json['linesVisible'] as bool?
  ..selection = (json['selection'] as List<dynamic>?)
      ?.map((e) => VTreeItem.fromJson(e as Map<String, dynamic>))
      .toList()
  ..sortColumn = json['sortColumn'] == null
      ? null
      : VTreeColumn.fromJson(json['sortColumn'] as Map<String, dynamic>)
  ..sortDirection = (json['sortDirection'] as num?)?.toInt()
  ..topItem = json['topItem'] == null
      ? null
      : VTreeItem.fromJson(json['topItem'] as Map<String, dynamic>);

Map<String, dynamic> _$VTreeToJson(VTree instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'background': ?instance.background,
  'backgroundImage': ?instance.backgroundImage,
  'bounds': ?instance.bounds,
  'capture': ?instance.capture,
  'cursor': ?instance.cursor,
  'dragDetect': ?instance.dragDetect,
  'dragSource': ?instance.dragSource,
  'dropTargetId': ?instance.dropTargetId,
  'enabled': ?instance.enabled,
  'font': ?instance.font,
  'foreground': ?instance.foreground,
  'menu': ?instance.menu,
  'orientation': ?instance.orientation,
  'redraw': ?instance.redraw,
  'region': ?instance.region,
  'textDirection': ?instance.textDirection,
  'toolTipText': ?instance.toolTipText,
  'touchEnabled': ?instance.touchEnabled,
  'visible': ?instance.visible,
  'horizontalBar': ?instance.horizontalBar,
  'scrollbarsMode': ?instance.scrollbarsMode,
  'verticalBar': ?instance.verticalBar,
  'backgroundMode': ?instance.backgroundMode,
  'children': ?instance.children,
  'layoutDeferred': ?instance.layoutDeferred,
  'tabList': ?instance.tabList,
  'columnOrder': ?instance.columnOrder,
  'columns': ?instance.columns,
  'editors': ?instance.editors,
  'headerBackground': ?instance.headerBackground,
  'headerForeground': ?instance.headerForeground,
  'headerVisible': ?instance.headerVisible,
  'items': ?instance.items,
  'linesVisible': ?instance.linesVisible,
  'selection': ?instance.selection,
  'sortColumn': ?instance.sortColumn,
  'sortDirection': ?instance.sortDirection,
  'topItem': ?instance.topItem,
};
