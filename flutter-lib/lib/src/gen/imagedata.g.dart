// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'imagedata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VImageData _$VImageDataFromJson(Map<String, dynamic> json) => VImageData()
  ..alpha = (json['alpha'] as num).toInt()
  ..alphaData = (json['alphaData'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList();

Map<String, dynamic> _$VImageDataToJson(VImageData instance) =>
    <String, dynamic>{
      'alpha': instance.alpha,
      'alphaData': instance.alphaData,
    };
