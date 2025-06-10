// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ime.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IMEValue _$IMEValueFromJson(Map<String, dynamic> json) => IMEValue()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
      .toList()
  ..style = (json['style'] as num).toInt()
  ..compositionOffset = (json['compositionOffset'] as num?)?.toInt();

Map<String, dynamic> _$IMEValueToJson(IMEValue instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'compositionOffset': instance.compositionOffset,
    };
