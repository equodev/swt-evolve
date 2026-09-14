// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'region.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VRegion _$VRegionFromJson(Map<String, dynamic> json) => VRegion()
  ..rects = (json['rects'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList();

Map<String, dynamic> _$VRegionToJson(VRegion instance) => <String, dynamic>{
  'rects': ?instance.rects,
};
