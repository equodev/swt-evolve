// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dialog.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VDialog _$VDialogFromJson(Map<String, dynamic> json) => VDialog()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num?)?.toInt()
  ..text = json['text'] as String?;

Map<String, dynamic> _$VDialogToJson(VDialog instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': ?instance.style,
  'text': ?instance.text,
};
