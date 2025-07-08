// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rectangle.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VRectangle _$VRectangleFromJson(Map<String, dynamic> json) => VRectangle()
  ..x = (json['x'] as num).toInt()
  ..y = (json['y'] as num).toInt()
  ..width = (json['width'] as num).toInt()
  ..height = (json['height'] as num).toInt();

Map<String, dynamic> _$VRectangleToJson(VRectangle instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };
