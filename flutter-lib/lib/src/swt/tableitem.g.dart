// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tableitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TableItemValue _$TableItemValueFromJson(Map<String, dynamic> json) =>
    TableItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..image = json['image'] as String?
      ..checked = json['checked'] as bool?
      ..grayed = json['grayed'] as bool?
      ..imageIndent = (json['imageIndent'] as num?)?.toInt()
      ..texts =
          (json['texts'] as List<dynamic>?)?.map((e) => e as String?).toList();

Map<String, dynamic> _$TableItemValueToJson(TableItemValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'checked': instance.checked,
      'grayed': instance.grayed,
      'imageIndent': instance.imageIndent,
      'texts': instance.texts,
    };
