// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'treecolumn.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TreeColumnValue _$TreeColumnValueFromJson(Map<String, dynamic> json) =>
    TreeColumnValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..alignment = (json['alignment'] as num?)?.toInt()
      ..moveable = json['moveable'] as bool?
      ..resizable = json['resizable'] as bool?
      ..toolTipText = json['toolTipText'] as String?
      ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$TreeColumnValueToJson(TreeColumnValue instance) {
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
  writeNotNull('alignment', instance.alignment);
  writeNotNull('moveable', instance.moveable);
  writeNotNull('resizable', instance.resizable);
  writeNotNull('toolTipText', instance.toolTipText);
  writeNotNull('width', instance.width);
  return val;
}
