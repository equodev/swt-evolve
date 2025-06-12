// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expanditem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpandItemValue _$ExpandItemValueFromJson(Map<String, dynamic> json) =>
    ExpandItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..image = json['image'] as String?
      ..expanded = json['expanded'] as bool?
      ..height = (json['height'] as num?)?.toInt();

Map<String, dynamic> _$ExpandItemValueToJson(ExpandItemValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'expanded': instance.expanded,
      'height': instance.height,
    };
