// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'touch.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTouch _$VTouchFromJson(Map<String, dynamic> json) => VTouch()
  ..id = (json['id'] as num?)?.toInt()
  ..primary = json['primary'] as bool?
  ..source = json['source'] == null
      ? null
      : VTouchSource.fromJson(json['source'] as Map<String, dynamic>)
  ..state = (json['state'] as num?)?.toInt()
  ..x = (json['x'] as num?)?.toInt()
  ..y = (json['y'] as num?)?.toInt();

Map<String, dynamic> _$VTouchToJson(VTouch instance) => <String, dynamic>{
  'id': ?instance.id,
  'primary': ?instance.primary,
  'source': ?instance.source,
  'state': ?instance.state,
  'x': ?instance.x,
  'y': ?instance.y,
};
