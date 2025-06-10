// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'toolitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ToolItemValue _$ToolItemValueFromJson(Map<String, dynamic> json) =>
    ToolItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..image = json['image'] as String?
      ..enabled = json['enabled'] as bool?
      ..selection = json['selection'] as bool?
      ..toolTipText = json['toolTipText'] as String?
      ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$ToolItemValueToJson(ToolItemValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'enabled': instance.enabled,
      'selection': instance.selection,
      'toolTipText': instance.toolTipText,
      'width': instance.width,
    };
