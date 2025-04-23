// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tableitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TableItemValue _$TableItemValueFromJson(Map<String, dynamic> json) =>
    TableItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..checked = json['checked'] as bool?
      ..grayed = json['grayed'] as bool?
      ..imageIndent = (json['imageIndent'] as num?)?.toInt()
      ..texts =
          (json['texts'] as List<dynamic>?)?.map((e) => e as String?).toList();

Map<String, dynamic> _$TableItemValueToJson(TableItemValue instance) {
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
  writeNotNull('grayed', instance.grayed);
  writeNotNull('imageIndent', instance.imageIndent);
  writeNotNull('texts', instance.texts);
  return val;
}
