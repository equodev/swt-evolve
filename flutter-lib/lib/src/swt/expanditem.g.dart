// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expanditem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExpandItemValue _$ExpandItemValueFromJson(Map<String, dynamic> json) =>
    ExpandItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..expanded = json['expanded'] as bool?
      ..height = (json['height'] as num?)?.toInt();

Map<String, dynamic> _$ExpandItemValueToJson(ExpandItemValue instance) {
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
  writeNotNull('expanded', instance.expanded);
  writeNotNull('height', instance.height);
  return val;
}
