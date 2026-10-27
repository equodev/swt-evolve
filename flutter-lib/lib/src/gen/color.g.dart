// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'color.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VColor _$VColorFromJson(Map<String, dynamic> json) => VColor()
  ..alpha = (json['a'] as num).toInt()
  ..blue = (json['b'] as num).toInt()
  ..green = (json['g'] as num).toInt()
  ..red = (json['r'] as num).toInt();

Map<String, dynamic> _$VColorToJson(VColor instance) => <String, dynamic>{
  'a': instance.alpha,
  'b': instance.blue,
  'g': instance.green,
  'r': instance.red,
};
