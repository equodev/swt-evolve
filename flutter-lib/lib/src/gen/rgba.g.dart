// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rgba.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VRGBA _$VRGBAFromJson(Map<String, dynamic> json) => VRGBA()
  ..alpha = (json['alpha'] as num).toInt()
  ..rgb = json['rgb'] == null
      ? null
      : VRGB.fromJson(json['rgb'] as Map<String, dynamic>);

Map<String, dynamic> _$VRGBAToJson(VRGBA instance) => <String, dynamic>{
  'alpha': instance.alpha,
  'rgb': ?instance.rgb,
};
