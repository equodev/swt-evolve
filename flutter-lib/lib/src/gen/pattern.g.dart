// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pattern.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VPattern _$VPatternFromJson(Map<String, dynamic> json) => VPattern()
  ..color1 = json['color1'] == null
      ? null
      : VColor.fromJson(json['color1'] as Map<String, dynamic>)
  ..color2 = json['color2'] == null
      ? null
      : VColor.fromJson(json['color2'] as Map<String, dynamic>)
  ..endX = (json['endX'] as num?)?.toDouble()
  ..endY = (json['endY'] as num?)?.toDouble()
  ..startX = (json['startX'] as num?)?.toDouble()
  ..startY = (json['startY'] as num?)?.toDouble();

Map<String, dynamic> _$VPatternToJson(VPattern instance) => <String, dynamic>{
  'color1': ?instance.color1,
  'color2': ?instance.color2,
  'endX': ?instance.endX,
  'endY': ?instance.endY,
  'startX': ?instance.startX,
  'startY': ?instance.startY,
};
