// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pathdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VPathData _$VPathDataFromJson(Map<String, dynamic> json) => VPathData()
  ..points = (json['points'] as List<dynamic>?)
      ?.map((e) => (e as num).toDouble())
      .toList()
  ..types = ImageUtils.parseByteArray(json['types']);

Map<String, dynamic> _$VPathDataToJson(VPathData instance) => <String, dynamic>{
  'points': ?instance.points,
  'types': ?ImageUtils.serializeByteArray(instance.types),
};
