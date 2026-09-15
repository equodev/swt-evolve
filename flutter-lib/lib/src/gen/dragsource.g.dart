// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dragsource.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VDragSource _$VDragSourceFromJson(Map<String, dynamic> json) => VDragSource()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0;

Map<String, dynamic> _$VDragSourceToJson(VDragSource instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
    };
