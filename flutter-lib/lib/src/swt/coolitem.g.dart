// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coolitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoolItemValue _$CoolItemValueFromJson(Map<String, dynamic> json) =>
    CoolItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..image = json['image'] as String?
      ..enabled = json['enabled'] as bool?
      ..selection = json['selection'] as bool?
      ..toolTipText = json['toolTipText'] as String?
      ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$CoolItemValueToJson(CoolItemValue instance) {
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
  writeNotNull('image', instance.image);
  writeNotNull('enabled', instance.enabled);
  writeNotNull('selection', instance.selection);
  writeNotNull('toolTipText', instance.toolTipText);
  writeNotNull('width', instance.width);
  return val;
}
