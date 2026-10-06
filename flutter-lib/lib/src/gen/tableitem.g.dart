// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tableitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTableItem _$VTableItemFromJson(Map<String, dynamic> json) => VTableItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..backgrounds = (json['backgrounds'] as List<dynamic>?)
      ?.map(
        (e) => e == null ? null : VColor.fromJson(e as Map<String, dynamic>),
      )
      .toList()
  ..checked = json['checked'] as bool?
  ..font = json['font'] == null
      ? null
      : VFont.fromJson(json['font'] as Map<String, dynamic>)
  ..fonts = (json['fonts'] as List<dynamic>?)
      ?.map((e) => e == null ? null : VFont.fromJson(e as Map<String, dynamic>))
      .toList()
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..foregrounds = (json['foregrounds'] as List<dynamic>?)
      ?.map(
        (e) => e == null ? null : VColor.fromJson(e as Map<String, dynamic>),
      )
      .toList()
  ..grayed = json['grayed'] as bool?
  ..images = (json['images'] as List<dynamic>?)
      ?.map(
        (e) => e == null ? null : VImage.fromJson(e as Map<String, dynamic>),
      )
      .toList()
  ..paintedTexts = (json['paintedTexts'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..texts = (json['texts'] as List<dynamic>?)
      ?.map((e) => e as String?)
      .toList();

Map<String, dynamic> _$VTableItemToJson(VTableItem instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': ?instance.image,
      'text': ?instance.text,
      'background': ?instance.background,
      'backgrounds': ?instance.backgrounds,
      'checked': ?instance.checked,
      'font': ?instance.font,
      'fonts': ?instance.fonts,
      'foreground': ?instance.foreground,
      'foregrounds': ?instance.foregrounds,
      'grayed': ?instance.grayed,
      'images': ?instance.images,
      'paintedTexts': ?instance.paintedTexts,
      'texts': ?instance.texts,
    };
