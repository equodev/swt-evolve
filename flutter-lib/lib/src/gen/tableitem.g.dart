// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tableitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTableItem _$VTableItemFromJson(Map<String, dynamic> json) => VTableItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..checked = json['checked'] as bool?
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..grayed = json['grayed'] as bool?
  ..imageIndent = (json['imageIndent'] as num?)?.toInt()
  ..texts = (json['texts'] as List<dynamic>?)?.map((e) => e as String).toList();

Map<String, dynamic> _$VTableItemToJson(VTableItem instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': instance.image,
      'text': instance.text,
      'background': instance.background,
      'checked': instance.checked,
      'font': instance.font,
      'foreground': instance.foreground,
      'grayed': instance.grayed,
      'imageIndent': instance.imageIndent,
      'texts': instance.texts,
    };
