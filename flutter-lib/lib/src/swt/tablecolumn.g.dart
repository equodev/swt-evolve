// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tablecolumn.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TableColumnValue _$TableColumnValueFromJson(Map<String, dynamic> json) =>
    TableColumnValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..image = json['image'] as String?
      ..alignment = (json['alignment'] as num?)?.toInt()
      ..moveable = json['moveable'] as bool?
      ..resizable = json['resizable'] as bool?
      ..toolTipText = json['toolTipText'] as String?
      ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$TableColumnValueToJson(TableColumnValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'alignment': instance.alignment,
      'moveable': instance.moveable,
      'resizable': instance.resizable,
      'toolTipText': instance.toolTipText,
      'width': instance.width,
    };
