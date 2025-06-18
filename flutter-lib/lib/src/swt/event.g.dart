// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Event _$EventFromJson(Map<String, dynamic> json) => Event()
  ..type = (json['type'] as num?)?.toInt()
  ..detail = (json['detail'] as num?)?.toInt()
  ..index = (json['index'] as num?)?.toInt()
  ..x = (json['x'] as num?)?.toInt()
  ..y = (json['y'] as num?)?.toInt()
  ..width = (json['width'] as num?)?.toInt()
  ..height = (json['height'] as num?)?.toInt();

Map<String, dynamic> _$EventToJson(Event instance) => <String, dynamic>{
      'type': instance.type,
      'detail': instance.detail,
      'index': instance.index,
      'x': instance.x,
      'y': instance.y,
      'width': instance.width,
      'height': instance.height,
    };
