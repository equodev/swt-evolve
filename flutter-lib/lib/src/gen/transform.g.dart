// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'transform.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTransform _$VTransformFromJson(Map<String, dynamic> json) => VTransform()
  ..elements = (json['elements'] as List<dynamic>?)
      ?.map((e) => (e as num).toDouble())
      .toList();

Map<String, dynamic> _$VTransformToJson(VTransform instance) =>
    <String, dynamic>{'elements': ?instance.elements};
