// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'font.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VFont _$VFontFromJson(Map<String, dynamic> json) => VFont()
  ..fontData = (json['fontData'] as List<dynamic>?)
      ?.map((e) => VFontData.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VFontToJson(VFont instance) => <String, dynamic>{
      'fontData': instance.fontData,
    };
