// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ctabitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CTabItemValue _$CTabItemValueFromJson(Map<String, dynamic> json) =>
    CTabItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..image = json['image'] as String?
      ..showClose = json['showClose'] as bool?
      ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$CTabItemValueToJson(CTabItemValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'showClose': instance.showClose,
      'toolTipText': instance.toolTipText,
    };
