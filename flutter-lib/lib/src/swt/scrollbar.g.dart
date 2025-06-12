// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scrollbar.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScrollBarValue _$ScrollBarValueFromJson(Map<String, dynamic> json) =>
    ScrollBarValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..enabled = json['enabled'] as bool?
      ..increment = (json['increment'] as num?)?.toInt()
      ..maximum = (json['maximum'] as num?)?.toInt()
      ..minimum = (json['minimum'] as num?)?.toInt()
      ..pageIncrement = (json['pageIncrement'] as num?)?.toInt()
      ..selection = (json['selection'] as num?)?.toInt()
      ..thumb = (json['thumb'] as num?)?.toInt()
      ..visible = json['visible'] as bool?;

Map<String, dynamic> _$ScrollBarValueToJson(ScrollBarValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'enabled': instance.enabled,
      'increment': instance.increment,
      'maximum': instance.maximum,
      'minimum': instance.minimum,
      'pageIncrement': instance.pageIncrement,
      'selection': instance.selection,
      'thumb': instance.thumb,
      'visible': instance.visible,
    };
