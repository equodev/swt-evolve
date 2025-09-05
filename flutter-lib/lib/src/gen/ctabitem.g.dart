// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ctabitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCTabItem _$VCTabItemFromJson(Map<String, dynamic> json) => VCTabItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..control = json['control'] == null
      ? null
      : VControl.fromJson(json['control'] as Map<String, dynamic>)
  ..disabledImage = json['disabledImage'] == null
      ? null
      : VImage.fromJson(json['disabledImage'] as Map<String, dynamic>)
  ..foreground = json['foreground'] == null
      ? null
      : VColor.fromJson(json['foreground'] as Map<String, dynamic>)
  ..selectionForeground = json['selectionForeground'] == null
      ? null
      : VColor.fromJson(json['selectionForeground'] as Map<String, dynamic>)
  ..showClose = json['showClose'] as bool?
  ..toolTipText = json['toolTipText'] as String?;

Map<String, dynamic> _$VCTabItemToJson(VCTabItem instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': instance.image,
      'text': instance.text,
      'control': instance.control,
      'disabledImage': instance.disabledImage,
      'foreground': instance.foreground,
      'selectionForeground': instance.selectionForeground,
      'showClose': instance.showClose,
      'toolTipText': instance.toolTipText,
    };
