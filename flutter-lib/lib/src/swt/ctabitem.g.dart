// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ctabitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CTabItemValue _$CTabItemValueFromJson(Map<String, dynamic> json) =>
    CTabItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..showClose = json['showClose'] as bool?
      ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$CTabItemValueToJson(CTabItemValue instance) {
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
  writeNotNull('showClose', instance.showClose);
  writeNotNull('toolTipText', instance.toolTipText);
  return val;
}
