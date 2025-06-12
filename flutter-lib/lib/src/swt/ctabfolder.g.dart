// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ctabfolder.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CTabFolderValue _$CTabFolderValueFromJson(Map<String, dynamic> json) =>
    CTabFolderValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..orientation = (json['orientation'] as num?)?.toInt()
      ..textDirection = (json['textDirection'] as num?)?.toInt()
      ..bounds = json['bounds'] == null
          ? null
          : RectangleValue.fromJson(json['bounds'] as Map<String, dynamic>)
      ..dragDetect = json['dragDetect'] as bool?
      ..enabled = json['enabled'] as bool?
      ..layoutData = json['layoutData']
      ..menu = json['menu'] == null
          ? null
          : MenuValue.fromJson(json['menu'] as Map<String, dynamic>)
      ..toolTipText = json['toolTipText'] as String?
      ..touchEnabled = json['touchEnabled'] as bool?
      ..visible = json['visible'] as bool?
      ..backgroundMode = (json['backgroundMode'] as num?)?.toInt()
      ..layout = json['layout'] == null
          ? null
          : LayoutValue.fromJson(json['layout'] as Map<String, dynamic>)
      ..layoutDeferred = json['layoutDeferred'] as bool?
      ..borderVisible = json['borderVisible'] as bool?
      ..minimized = json['minimized'] as bool?
      ..minimizeVisible = json['minimizeVisible'] as bool?
      ..minimumCharacters = (json['minimumCharacters'] as num?)?.toInt()
      ..maximized = json['maximized'] as bool?
      ..maximizeVisible = json['maximizeVisible'] as bool?
      ..mRUVisible = json['mRUVisible'] as bool?
      ..selectionIndex = (json['selectionIndex'] as num?)?.toInt()
      ..simple = json['simple'] as bool?
      ..single = json['single'] as bool?
      ..tabHeight = (json['tabHeight'] as num?)?.toInt()
      ..tabPosition = (json['tabPosition'] as num?)?.toInt()
      ..unselectedCloseVisible = json['unselectedCloseVisible'] as bool?
      ..unselectedImageVisible = json['unselectedImageVisible'] as bool?
      ..highlightEnabled = json['highlightEnabled'] as bool?
      ..items = (json['items'] as List<dynamic>?)
          ?.map((e) => CTabItemValue.fromJson(e as Map<String, dynamic>))
          .toList();

Map<String, dynamic> _$CTabFolderValueToJson(CTabFolderValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'orientation': instance.orientation,
      'textDirection': instance.textDirection,
      'bounds': instance.bounds,
      'dragDetect': instance.dragDetect,
      'enabled': instance.enabled,
      'layoutData': instance.layoutData,
      'menu': instance.menu,
      'toolTipText': instance.toolTipText,
      'touchEnabled': instance.touchEnabled,
      'visible': instance.visible,
      'backgroundMode': instance.backgroundMode,
      'layout': instance.layout,
      'layoutDeferred': instance.layoutDeferred,
      'borderVisible': instance.borderVisible,
      'minimized': instance.minimized,
      'minimizeVisible': instance.minimizeVisible,
      'minimumCharacters': instance.minimumCharacters,
      'maximized': instance.maximized,
      'maximizeVisible': instance.maximizeVisible,
      'mRUVisible': instance.mRUVisible,
      'selectionIndex': instance.selectionIndex,
      'simple': instance.simple,
      'single': instance.single,
      'tabHeight': instance.tabHeight,
      'tabPosition': instance.tabPosition,
      'unselectedCloseVisible': instance.unselectedCloseVisible,
      'unselectedImageVisible': instance.unselectedImageVisible,
      'highlightEnabled': instance.highlightEnabled,
      'items': instance.items,
    };
