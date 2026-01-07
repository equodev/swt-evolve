// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'menu.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VMenu _$VMenuFromJson(Map<String, dynamic> json) => VMenu()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..defaultItem = json['defaultItem'] == null
      ? null
      : VMenuItem.fromJson(json['defaultItem'] as Map<String, dynamic>)
  ..enabled = json['enabled'] as bool?
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VMenuItem.fromJson(e as Map<String, dynamic>))
      .toList()
  ..location = json['location'] == null
      ? null
      : VPoint.fromJson(json['location'] as Map<String, dynamic>)
  ..orientation = (json['orientation'] as num?)?.toInt()
  ..parentMenu = json['parentMenu'] == null
      ? null
      : VMenu.fromJson(json['parentMenu'] as Map<String, dynamic>)
  ..visible = json['visible'] as bool?;

Map<String, dynamic> _$VMenuToJson(VMenu instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'defaultItem': instance.defaultItem,
  'enabled': instance.enabled,
  'items': instance.items,
  'location': instance.location,
  'orientation': instance.orientation,
  'parentMenu': instance.parentMenu,
  'visible': instance.visible,
};
