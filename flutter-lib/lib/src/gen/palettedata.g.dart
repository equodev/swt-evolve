// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'palettedata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VPaletteData _$VPaletteDataFromJson(Map<String, dynamic> json) => VPaletteData()
  ..RGBs = (json['RGBs'] as List<dynamic>?)
      ?.map((e) => VRGB.fromJson(e as Map<String, dynamic>))
      .toList()
  ..pixel = (json['pixel'] as num).toInt();

Map<String, dynamic> _$VPaletteDataToJson(VPaletteData instance) =>
    <String, dynamic>{'RGBs': ?instance.RGBs, 'pixel': instance.pixel};
