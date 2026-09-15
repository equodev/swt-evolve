// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tablecolumn.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTableColumn _$VTableColumnFromJson(Map<String, dynamic> json) => VTableColumn()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..alignment = (json['alignment'] as num?)?.toInt()
  ..resizable = json['resizable'] as bool?
  ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$VTableColumnToJson(VTableColumn instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': ?instance.image,
      'text': ?instance.text,
      'alignment': ?instance.alignment,
      'resizable': ?instance.resizable,
      'width': ?instance.width,
    };
