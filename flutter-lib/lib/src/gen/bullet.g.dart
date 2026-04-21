// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'bullet.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VBullet _$VBulletFromJson(Map<String, dynamic> json) => VBullet()
  ..style = json['style'] == null
      ? null
      : VStyleRange.fromJson(json['style'] as Map<String, dynamic>)
  ..text = json['text'] as String
  ..type = (json['type'] as num).toInt();

Map<String, dynamic> _$VBulletToJson(VBullet instance) => <String, dynamic>{
  'style': ?instance.style,
  'text': instance.text,
  'type': instance.type,
};
