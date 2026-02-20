// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'controleditor.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VControlEditor _$VControlEditorFromJson(Map<String, dynamic> json) =>
    VControlEditor()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..style = (json['style'] as num).toInt()
      ..editor = json['editor'] == null
          ? null
          : VControl.fromJson(json['editor'] as Map<String, dynamic>);

Map<String, dynamic> _$VControlEditorToJson(VControlEditor instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'editor': instance.editor,
    };
