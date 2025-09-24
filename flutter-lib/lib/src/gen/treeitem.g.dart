// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'treeitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTreeItem _$VTreeItemFromJson(Map<String, dynamic> json) => VTreeItem()
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
  ..expanded = json['expanded'] as bool?
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..grayed = json['grayed'] as bool?
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VTreeItem.fromJson(e as Map<String, dynamic>))
      .toList()
  ..texts = (json['texts'] as List<dynamic>?)?.map((e) => e as String).toList();

Map<String, dynamic> _$VTreeItemToJson(VTreeItem instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': instance.image,
      'text': instance.text,
      'background': instance.background,
      'checked': instance.checked,
      'expanded': instance.expanded,
      'foreground': instance.foreground,
      'grayed': instance.grayed,
      'items': instance.items,
      'texts': instance.texts,
    };
