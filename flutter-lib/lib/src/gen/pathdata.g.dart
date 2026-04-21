// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pathdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VPathData _$VPathDataFromJson(Map<String, dynamic> json) => VPathData()
  ..points = (json['points'] as List<dynamic>?)
      ?.map((e) => (e as num).toDouble())
      .toList()
  ..types = (json['types'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList();

Map<String, dynamic> _$VPathDataToJson(VPathData instance) => <String, dynamic>{
  'points': ?instance.points,
  'types': ?instance.types,
};
