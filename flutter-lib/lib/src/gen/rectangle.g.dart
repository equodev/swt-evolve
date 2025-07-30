// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rectangle.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VRectangle _$VRectangleFromJson(Map<String, dynamic> json) => VRectangle()
  ..height = (json['height'] as num).toInt()
  ..width = (json['width'] as num).toInt()
  ..x = (json['x'] as num).toInt()
  ..y = (json['y'] as num).toInt();

Map<String, dynamic> _$VRectangleToJson(VRectangle instance) =>
    <String, dynamic>{
      'height': instance.height,
      'width': instance.width,
      'x': instance.x,
      'y': instance.y,
    };
