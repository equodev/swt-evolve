// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'imagedata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VImageData _$VImageDataFromJson(Map<String, dynamic> json) => VImageData()
  ..data = ImageUtils.parseByteArray(json['data'])
  ..depth = (json['depth'] as num?)?.toInt()
  ..height = (json['height'] as num?)?.toInt()
  ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$VImageDataToJson(VImageData instance) =>
    <String, dynamic>{
      'data': ?ImageUtils.serializeByteArray(instance.data),
      'depth': ?instance.depth,
      'height': ?instance.height,
      'width': ?instance.width,
    };
