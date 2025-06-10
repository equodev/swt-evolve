// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'combo.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ComboValue _$ComboValueFromJson(Map<String, dynamic> json) => ComboValue()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
      .toList()
  ..style = (json['style'] as num).toInt()
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
  ..items = (json['items'] as List<dynamic>?)?.map((e) => e as String).toList()
  ..listVisible = json['listVisible'] as bool?
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..selectionIndex = (json['selectionIndex'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..textLimit = (json['textLimit'] as num?)?.toInt()
  ..visibleItemCount = (json['visibleItemCount'] as num?)?.toInt();

Map<String, dynamic> _$ComboValueToJson(ComboValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
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
      'items': instance.items,
      'listVisible': instance.listVisible,
      'orientation': instance.orientation,
      'selectionIndex': instance.selectionIndex,
      'text': instance.text,
      'textLimit': instance.textLimit,
      'visibleItemCount': instance.visibleItemCount,
    };
