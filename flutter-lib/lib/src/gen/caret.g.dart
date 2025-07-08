// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'caret.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCaret _$VCaretFromJson(Map<String, dynamic> json) => VCaret()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..isVisible = json['isVisible'] as bool?
  ..height = (json['height'] as num?)?.toInt();

Map<String, dynamic> _$VCaretToJson(VCaret instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'isVisible': instance.isVisible,
      'height': instance.height,
    };
