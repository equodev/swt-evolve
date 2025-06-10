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

Map<String, dynamic> _$MenuValueToJson(MenuValue instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'enabled': instance.enabled,
      'orientation': instance.orientation,
      'visible': instance.visible,
    };
