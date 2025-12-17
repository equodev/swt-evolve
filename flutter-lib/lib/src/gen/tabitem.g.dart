// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tabitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTabItem _$VTabItemFromJson(Map<String, dynamic> json) => VTabItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..control = json['control'] == null
      ? null
      : VControl.fromJson(json['control'] as Map<String, dynamic>)
  ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$VTabItemToJson(VTabItem instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': instance.image,
      'text': instance.text,
      'control': instance.control,
      'toolTipText': instance.toolTipText,
    };
