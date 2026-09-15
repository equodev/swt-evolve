// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tracker.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTracker _$VTrackerFromJson(Map<String, dynamic> json) => VTracker()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0;

Map<String, dynamic> _$VTrackerToJson(VTracker instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
};
