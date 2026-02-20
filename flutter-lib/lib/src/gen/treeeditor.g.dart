// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'treeeditor.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTreeEditor _$VTreeEditorFromJson(Map<String, dynamic> json) => VTreeEditor()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..column = (json['column'] as num?)?.toInt()
  ..editor = json['editor'] == null
      ? null
      : VControl.fromJson(json['editor'] as Map<String, dynamic>)
  ..item = json['item'] == null
      ? null
      : VTreeItem.fromJson(json['item'] as Map<String, dynamic>);

Map<String, dynamic> _$VTreeEditorToJson(VTreeEditor instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'column': instance.column,
      'editor': instance.editor,
      'item': instance.item,
    };
