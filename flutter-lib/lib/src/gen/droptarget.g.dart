// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'droptarget.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VDropTarget _$VDropTargetFromJson(Map<String, dynamic> json) => VDropTarget()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..control = json['control'] == null
      ? null
      : VControl.fromJson(json['control'] as Map<String, dynamic>);

Map<String, dynamic> _$VDropTargetToJson(VDropTarget instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'control': instance.control,
    };
