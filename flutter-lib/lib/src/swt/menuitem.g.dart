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
      ..image = json['image'] as String?
      ..accelerator = (json['accelerator'] as num?)?.toInt()
      ..enabled = json['enabled'] as bool?
      ..iD = (json['iD'] as num?)?.toInt()
      ..menu = json['menu'] == null
          ? null
          : MenuValue.fromJson(json['menu'] as Map<String, dynamic>)
      ..selection = json['selection'] as bool?
      ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$MenuItemValueToJson(MenuItemValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'children': instance.children,
      'style': instance.style,
      'text': instance.text,
      'image': instance.image,
      'accelerator': instance.accelerator,
      'enabled': instance.enabled,
      'iD': instance.iD,
      'menu': instance.menu,
      'selection': instance.selection,
      'toolTipText': instance.toolTipText,
    };
