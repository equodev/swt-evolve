// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'menuitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MenuItemValue _$MenuItemValueFromJson(Map<String, dynamic> json) =>
    MenuItemValue()
      ..swt = json['swt'] as String
      ..id = (json['id'] as num).toInt()
      ..children = (json['children'] as List<dynamic>?)
          ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
          .toList()
      ..style = (json['style'] as num).toInt()
      ..text = json['text'] as String?
      ..accelerator = (json['accelerator'] as num?)?.toInt()
      ..enabled = json['enabled'] as bool?
      ..iD = (json['iD'] as num?)?.toInt()
      ..menu = json['menu'] == null
          ? null
          : MenuValue.fromJson(json['menu'] as Map<String, dynamic>)
      ..selection = json['selection'] as bool?
      ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$MenuItemValueToJson(MenuItemValue instance) {
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
  writeNotNull('accelerator', instance.accelerator);
  writeNotNull('enabled', instance.enabled);
  writeNotNull('iD', instance.iD);
  writeNotNull('menu', instance.menu?.toJson());
  writeNotNull('selection', instance.selection);
  writeNotNull('toolTipText', instance.toolTipText);
  return val;
}
