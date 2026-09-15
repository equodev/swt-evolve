// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taskitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTaskItem _$VTaskItemFromJson(Map<String, dynamic> json) => VTaskItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?;

Map<String, dynamic> _$VTaskItemToJson(VTaskItem instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'image': ?instance.image,
  'text': ?instance.text,
};
