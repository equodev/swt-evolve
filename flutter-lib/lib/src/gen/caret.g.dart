// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'caret.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCaret _$VCaretFromJson(Map<String, dynamic> json) => VCaret()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0
  ..visible = json['visible'] as bool?
  ..bounds = json['bounds'] == null
      ? null
      : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>)
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..focused = json['focused'] as bool?
  ..blinkRate = (json['blinkRate'] as num?)?.toInt();

Map<String, dynamic> _$VCaretToJson(VCaret instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'visible': ?instance.visible,
  'bounds': ?instance.bounds,
  'image': ?instance.image,
  'focused': ?instance.focused,
  'blinkRate': ?instance.blinkRate,
};
