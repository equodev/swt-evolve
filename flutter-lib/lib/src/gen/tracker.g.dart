// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tracker.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTracker _$VTrackerFromJson(Map<String, dynamic> json) => VTracker()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..cursor = json['cursor'] == null
      ? null
      : VCursor.fromJson(json['cursor'] as Map<String, dynamic>)
  ..rectangles = (json['rectangles'] as List<dynamic>?)
      ?.map((e) => VRectangle.fromJson(e as Map<String, dynamic>))
      .toList()
  ..stippled = json['stippled'] as bool?;

Map<String, dynamic> _$VTrackerToJson(VTracker instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'cursor': ?instance.cursor,
  'rectangles': ?instance.rectangles,
  'stippled': ?instance.stippled,
};
