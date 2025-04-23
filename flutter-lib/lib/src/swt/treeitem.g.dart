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
      ..checked = json['checked'] as bool?
      ..expanded = json['expanded'] as bool?
      ..grayed = json['grayed'] as bool?
      ..itemCount = (json['itemCount'] as num?)?.toInt()
      ..texts =
          (json['texts'] as List<dynamic>?)?.map((e) => e as String).toList()
      ..selected = json['selected'] as bool?;

Map<String, dynamic> _$TreeItemValueToJson(TreeItemValue instance) {
  final val = <String, dynamic>{
    'swt': instance.swt,
    'id': instance.id,
  };

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('children', instance.children?.map((e) => e.toJson()).toList());
  val['style'] = instance.style;
  writeNotNull('text', instance.text);
  writeNotNull('checked', instance.checked);
  writeNotNull('expanded', instance.expanded);
  writeNotNull('grayed', instance.grayed);
  writeNotNull('itemCount', instance.itemCount);
  writeNotNull('texts', instance.texts);
  writeNotNull('selected', instance.selected);
  return val;
}
