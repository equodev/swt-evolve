// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'color.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VColor _$VColorFromJson(Map<String, dynamic> json) => VColor()
  ..alpha = (json['alpha'] as num).toInt()
  ..blue = (json['blue'] as num).toInt()
  ..green = (json['green'] as num).toInt()
  ..red = (json['red'] as num).toInt();

Map<String, dynamic> _$VColorToJson(VColor instance) => <String, dynamic>{
      'alpha': instance.alpha,
      'blue': instance.blue,
      'green': instance.green,
      'red': instance.red,
    };
