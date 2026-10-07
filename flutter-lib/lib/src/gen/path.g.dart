// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'path.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VPath _$VPathFromJson(Map<String, dynamic> json) => VPath()
  ..pathData = json['pathData'] == null
      ? null
      : VPathData.fromJson(json['pathData'] as Map<String, dynamic>)
  ..textStrings = (json['textStrings'] as List<dynamic>?)
      ?.map((e) => e as String?)
      .toList()
  ..textOrigins = (json['textOrigins'] as List<dynamic>?)
      ?.map((e) => (e as num).toDouble())
      .toList()
  ..textFonts = (json['textFonts'] as List<dynamic>?)
      ?.map((e) => e == null ? null : VFont.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VPathToJson(VPath instance) => <String, dynamic>{
  'pathData': ?instance.pathData,
  'textStrings': ?instance.textStrings,
  'textOrigins': ?instance.textOrigins,
  'textFonts': ?instance.textFonts,
};
