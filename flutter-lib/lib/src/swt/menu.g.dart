// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'menu.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MenuValue _$MenuValueFromJson(Map<String, dynamic> json) => MenuValue()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..children = (json['children'] as List<dynamic>?)
      ?.map((e) => WidgetValue.fromJson(e as Map<String, dynamic>))
      .toList()
  ..style = (json['style'] as num).toInt()
  ..enabled = json['enabled'] as bool?
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..visible = json['visible'] as bool?;

Map<String, dynamic> _$MenuValueToJson(MenuValue instance) {
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
  writeNotNull('enabled', instance.enabled);
  writeNotNull('orientation', instance.orientation);
  writeNotNull('visible', instance.visible);
  return val;
}
