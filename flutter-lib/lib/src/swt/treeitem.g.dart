// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'treeitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TreeItemValue _$TreeItemValueFromJson(Map<String, dynamic> json) =>
    TreeItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..image = json['image'] as String?
      ..checked = json['checked'] as bool?
      ..expanded = json['expanded'] as bool?
      ..grayed = json['grayed'] as bool?
      ..itemCount = (json['itemCount'] as num?)?.toInt()
      ..texts =
          (json['texts'] as List<dynamic>?)?.map((e) => e as String).toList()
      ..selected = json['selected'] as bool?;

Map<String, dynamic> _$TreeItemValueToJson(TreeItemValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'checked': instance.checked,
      'expanded': instance.expanded,
      'grayed': instance.grayed,
      'itemCount': instance.itemCount,
      'texts': instance.texts,
      'selected': instance.selected,
    };
