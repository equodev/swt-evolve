// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tableeditor.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTableEditor _$VTableEditorFromJson(Map<String, dynamic> json) => VTableEditor()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..column = (json['column'] as num?)?.toInt()
  ..editor = json['editor'] == null
      ? null
      : VControl.fromJson(json['editor'] as Map<String, dynamic>)
  ..item = json['item'] == null
      ? null
      : VTableItem.fromJson(json['item'] as Map<String, dynamic>);

Map<String, dynamic> _$VTableEditorToJson(VTableEditor instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'column': instance.column,
      'editor': instance.editor,
      'item': instance.item,
    };
