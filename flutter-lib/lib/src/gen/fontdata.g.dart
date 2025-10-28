// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'fontdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VFontData _$VFontDataFromJson(Map<String, dynamic> json) => VFontData()
  ..height = (json['height'] as num).toInt()
  ..locale = json['locale'] as String
  ..name = json['name'] as String
  ..style = (json['style'] as num).toInt();

Map<String, dynamic> _$VFontDataToJson(VFontData instance) => <String, dynamic>{
      'height': instance.height,
      'locale': instance.locale,
      'name': instance.name,
      'style': instance.style,
    };
