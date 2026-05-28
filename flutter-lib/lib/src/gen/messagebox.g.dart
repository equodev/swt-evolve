// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'messagebox.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VMessageBox _$VMessageBoxFromJson(Map<String, dynamic> json) => VMessageBox()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..message = json['message'] as String?;

Map<String, dynamic> _$VMessageBoxToJson(VMessageBox instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': ?instance.style,
      'text': ?instance.text,
      'message': ?instance.message,
    };
