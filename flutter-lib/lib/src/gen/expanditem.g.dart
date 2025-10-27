// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expanditem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VExpandItem _$VExpandItemFromJson(Map<String, dynamic> json) => VExpandItem()
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
  ..expanded = json['expanded'] as bool?
  ..height = (json['height'] as num?)?.toInt();

Map<String, dynamic> _$VExpandItemToJson(VExpandItem instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': instance.image,
      'text': instance.text,
      'control': instance.control,
      'expanded': instance.expanded,
      'height': instance.height,
    };
