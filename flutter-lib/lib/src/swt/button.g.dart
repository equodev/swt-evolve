// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'button.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ButtonValue _$ButtonValueFromJson(Map<String, dynamic> json) => ButtonValue()
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
  ..alignment = (json['alignment'] as num?)?.toInt()
  ..grayed = json['grayed'] as bool?
  ..selection = json['selection'] as bool?
  ..text = json['text'] as String?
  ..image = json['image'] as String?;

Map<String, dynamic> _$ButtonValueToJson(ButtonValue instance) =>
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
      'alignment': instance.alignment,
      'grayed': instance.grayed,
      'selection': instance.selection,
      'text': instance.text,
      'image': instance.image,
    };
