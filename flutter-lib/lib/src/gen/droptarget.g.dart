// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'droptarget.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VDropTarget _$VDropTargetFromJson(Map<String, dynamic> json) => VDropTarget()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0;

Map<String, dynamic> _$VDropTargetToJson(VDropTarget instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
    };
