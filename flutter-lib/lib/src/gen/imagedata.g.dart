// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'imagedata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VImageData _$VImageDataFromJson(Map<String, dynamic> json) => VImageData()
  ..alpha = (json['alpha'] as num?)?.toInt()
  ..alphaData = ImageUtils.parseByteArray(json['alphaData'])
  ..bytesPerLine = (json['bytesPerLine'] as num?)?.toInt()
  ..data = ImageUtils.parseByteArray(json['data'])
  ..delayTime = (json['delayTime'] as num?)?.toInt()
  ..depth = (json['depth'] as num?)?.toInt()
  ..disposalMethod = (json['disposalMethod'] as num?)?.toInt()
  ..height = (json['height'] as num?)?.toInt()
  ..maskData = ImageUtils.parseByteArray(json['maskData'])
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
      'alphaData': ImageUtils.serializeByteArray(instance.alphaData),
      'bytesPerLine': instance.bytesPerLine,
      'data': ImageUtils.serializeByteArray(instance.data),
      'delayTime': instance.delayTime,
      'depth': instance.depth,
      'disposalMethod': instance.disposalMethod,
      'height': instance.height,
      'maskData': ImageUtils.serializeByteArray(instance.maskData),
      'maskPad': instance.maskPad,
      'scanlinePad': instance.scanlinePad,
      'transparentPixel': instance.transparentPixel,
      'type': instance.type,
      'width': instance.width,
      'x': instance.x,
      'y': instance.y,
    };
