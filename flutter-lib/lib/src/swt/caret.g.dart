// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'caret.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CaretValue _$CaretValueFromJson(Map<String, dynamic> json) => CaretValue()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
      .toList()
  ..style = (json['style'] as num).toInt()
  ..bounds = json['bounds'] == null
      ? null
      : RectangleValue.fromJson(json['bounds'] as Map<String, dynamic>)
  ..visible = json['visible'] as bool?;

Map<String, dynamic> _$CaretValueToJson(CaretValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'bounds': instance.bounds,
      'visible': instance.visible,
    };
