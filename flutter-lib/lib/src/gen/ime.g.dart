// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ime.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VIME _$VIMEFromJson(Map<String, dynamic> json) => VIME()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0;

Map<String, dynamic> _$VIMEToJson(VIME instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
};
