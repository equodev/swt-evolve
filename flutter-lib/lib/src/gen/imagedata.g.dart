// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'imagedata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VImageData _$VImageDataFromJson(Map<String, dynamic> json) => VImageData()
  ..alpha = (json['alpha'] as num?)?.toInt()
  ..alphaData = (json['alphaData'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..bytesPerLine = (json['bytesPerLine'] as num?)?.toInt()
  ..data =
      (json['data'] as List<dynamic>?)?.map((e) => (e as num).toInt()).toList()
  ..delayTime = (json['delayTime'] as num?)?.toInt()
  ..depth = (json['depth'] as num?)?.toInt()
  ..disposalMethod = (json['disposalMethod'] as num?)?.toInt()
  ..height = (json['height'] as num?)?.toInt()
  ..maskData = (json['maskData'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..maskPad = (json['maskPad'] as num?)?.toInt()
  ..scanlinePad = (json['scanlinePad'] as num?)?.toInt()
  ..transparentPixel = (json['transparentPixel'] as num?)?.toInt()
  ..type = (json['type'] as num?)?.toInt()
  ..width = (json['width'] as num?)?.toInt()
  ..x = (json['x'] as num?)?.toInt()
  ..y = (json['y'] as num?)?.toInt();

Map<String, dynamic> _$VImageDataToJson(VImageData instance) =>
    <String, dynamic>{
      'alpha': instance.alpha,
      'alphaData': instance.alphaData,
      'bytesPerLine': instance.bytesPerLine,
      'data': instance.data,
      'delayTime': instance.delayTime,
      'depth': instance.depth,
      'disposalMethod': instance.disposalMethod,
      'height': instance.height,
      'maskData': instance.maskData,
      'maskPad': instance.maskPad,
      'scanlinePad': instance.scanlinePad,
      'transparentPixel': instance.transparentPixel,
      'type': instance.type,
      'width': instance.width,
      'x': instance.x,
      'y': instance.y,
    };
