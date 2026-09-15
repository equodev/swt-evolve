// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'controleditor.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VControlEditor _$VControlEditorFromJson(Map<String, dynamic> json) =>
    VControlEditor()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..seq = (json['_s'] as num?)?.toInt() ?? 0
      ..style = (json['style'] as num?)?.toInt() ?? 0;

Map<String, dynamic> _$VControlEditorToJson(VControlEditor instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
    };
