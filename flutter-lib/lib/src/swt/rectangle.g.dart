// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rectangle.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RectangleValue _$RectangleValueFromJson(Map<String, dynamic> json) =>
    RectangleValue()
      ..x = (json['x'] as num).toInt()
      ..y = (json['y'] as num).toInt()
      ..width = (json['width'] as num).toInt()
      ..height = (json['height'] as num).toInt();

Map<String, dynamic> _$RectangleValueToJson(RectangleValue instance) =>
    <String, dynamic>{
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };
