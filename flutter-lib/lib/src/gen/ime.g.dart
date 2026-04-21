// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ime.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VIME _$VIMEFromJson(Map<String, dynamic> json) => VIME()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..compositionOffset = (json['compositionOffset'] as num?)?.toInt()
  ..ranges = (json['ranges'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..styles = (json['styles'] as List<dynamic>?)
      ?.map((e) => VTextStyle.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VIMEToJson(VIME instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'compositionOffset': ?instance.compositionOffset,
  'ranges': ?instance.ranges,
  'styles': ?instance.styles,
};
