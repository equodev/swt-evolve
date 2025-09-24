// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'treecolumn.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTreeColumn _$VTreeColumnFromJson(Map<String, dynamic> json) => VTreeColumn()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..alignment = (json['alignment'] as num?)?.toInt()
  ..moveable = json['moveable'] as bool?
  ..resizable = json['resizable'] as bool?
  ..toolTipText = json['toolTipText'] as String?
  ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$VTreeColumnToJson(VTreeColumn instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': instance.image,
      'text': instance.text,
      'alignment': instance.alignment,
      'moveable': instance.moveable,
      'resizable': instance.resizable,
      'toolTipText': instance.toolTipText,
      'width': instance.width,
    };
