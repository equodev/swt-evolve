// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tooltip.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VToolTip _$VToolTipFromJson(Map<String, dynamic> json) => VToolTip()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..autoHide = json['autoHide'] as bool?
  ..location = json['location'] == null
      ? null
      : VPoint.fromJson(json['location'] as Map<String, dynamic>)
  ..message = json['message'] as String?
  ..text = json['text'] as String?
  ..visible = json['visible'] as bool?;

Map<String, dynamic> _$VToolTipToJson(VToolTip instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'autoHide': instance.autoHide,
  'location': instance.location,
  'message': instance.message,
  'text': instance.text,
  'visible': instance.visible,
};
