// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tree.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TreeValue _$TreeValueFromJson(Map<String, dynamic> json) => TreeValue()
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
  ..headerVisible = json['headerVisible'] as bool?
  ..itemCount = (json['itemCount'] as num?)?.toInt()
  ..linesVisible = json['linesVisible'] as bool?
  ..sortDirection = (json['sortDirection'] as num?)?.toInt();

Map<String, dynamic> _$TreeValueToJson(TreeValue instance) => <String, dynamic>{
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
      'headerVisible': instance.headerVisible,
      'itemCount': instance.itemCount,
      'linesVisible': instance.linesVisible,
      'sortDirection': instance.sortDirection,
    };
