// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trayitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTrayItem _$VTrayItemFromJson(Map<String, dynamic> json) => VTrayItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?;

Map<String, dynamic> _$VTrayItemToJson(VTrayItem instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'image': ?instance.image,
  'text': ?instance.text,
};
