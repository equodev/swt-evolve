// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trayitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTrayItem _$VTrayItemFromJson(Map<String, dynamic> json) => VTrayItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..highlightImage = json['highlightImage'] == null
      ? null
      : VImage.fromJson(json['highlightImage'] as Map<String, dynamic>)
  ..toolTip = json['toolTip'] == null
      ? null
      : VToolTip.fromJson(json['toolTip'] as Map<String, dynamic>)
  ..toolTipText = json['toolTipText'] as String?
  ..visible = json['visible'] as bool?;

Map<String, dynamic> _$VTrayItemToJson(VTrayItem instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'image': ?instance.image,
  'text': ?instance.text,
  'highlightImage': ?instance.highlightImage,
  'toolTip': ?instance.toolTip,
  'toolTipText': ?instance.toolTipText,
  'visible': ?instance.visible,
};
