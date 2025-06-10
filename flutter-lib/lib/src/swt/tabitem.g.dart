// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tabitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TabItemValue _$TabItemValueFromJson(Map<String, dynamic> json) => TabItemValue()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
      .toList()
  ..style = (json['style'] as num).toInt()
  ..text = json['text'] as String?
  ..image = json['image'] as String?
  ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$TabItemValueToJson(TabItemValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'toolTipText': instance.toolTipText,
    };
