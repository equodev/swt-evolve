// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'menuitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VMenuItem _$VMenuItemFromJson(Map<String, dynamic> json) => VMenuItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..ID = (json['ID'] as num?)?.toInt()
  ..accelerator = (json['accelerator'] as num?)?.toInt()
  ..enabled = json['enabled'] as bool?
  ..menu = json['menu'] == null
      ? null
      : VMenu.fromJson(json['menu'] as Map<String, dynamic>)
  ..selection = json['selection'] as bool?
  ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$VMenuItemToJson(VMenuItem instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'image': instance.image,
  'text': instance.text,
  'ID': instance.ID,
  'accelerator': instance.accelerator,
  'enabled': instance.enabled,
  'menu': instance.menu,
  'selection': instance.selection,
  'toolTipText': instance.toolTipText,
};
