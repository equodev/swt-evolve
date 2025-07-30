// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'caret.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCaret _$VCaretFromJson(Map<String, dynamic> json) => VCaret()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..bounds = json['bounds'] == null
      ? null
      : VRectangle.fromJson(json['bounds'] as Map<String, dynamic>)
  ..height = (json['height'] as num?)?.toInt()
  ..visible = json['visible'] as bool?;

Map<String, dynamic> _$VCaretToJson(VCaret instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'bounds': instance.bounds,
      'height': instance.height,
      'visible': instance.visible,
    };
