// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'text.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TextValue _$TextValueFromJson(Map<String, dynamic> json) => TextValue()
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
  ..doubleClickEnabled = json['doubleClickEnabled'] as bool?
  ..echoChar = (json['echoChar'] as num?)?.toInt()
  ..editable = json['editable'] as bool?
  ..message = json['message'] as String?
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..tabs = (json['tabs'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..textLimit = (json['textLimit'] as num?)?.toInt()
  ..topIndex = (json['topIndex'] as num?)?.toInt();

Map<String, dynamic> _$TextValueToJson(TextValue instance) => <String, dynamic>{
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
      'doubleClickEnabled': instance.doubleClickEnabled,
      'echoChar': instance.echoChar,
      'editable': instance.editable,
      'message': instance.message,
      'orientation': instance.orientation,
      'tabs': instance.tabs,
      'text': instance.text,
      'textLimit': instance.textLimit,
      'topIndex': instance.topIndex,
    };
