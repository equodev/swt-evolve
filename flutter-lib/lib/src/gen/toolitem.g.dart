// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'toolitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VToolItem _$VToolItemFromJson(Map<String, dynamic> json) => VToolItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..control = json['control'] == null
      ? null
      : VControl.fromJson(json['control'] as Map<String, dynamic>)
  ..disabledImage = json['disabledImage'] == null
      ? null
      : VImage.fromJson(json['disabledImage'] as Map<String, dynamic>)
  ..enabled = json['enabled'] as bool?
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..hotImage = json['hotImage'] == null
      ? null
      : VImage.fromJson(json['hotImage'] as Map<String, dynamic>)
  ..selection = json['selection'] as bool?
  ..toolTipText = json['toolTipText'] as String?
  ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$VToolItemToJson(VToolItem instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'image': instance.image,
  'text': instance.text,
  'background': instance.background,
  'control': instance.control,
  'disabledImage': instance.disabledImage,
  'enabled': instance.enabled,
  'foreground': instance.foreground,
  'hotImage': instance.hotImage,
  'selection': instance.selection,
  'toolTipText': instance.toolTipText,
  'width': instance.width,
};
