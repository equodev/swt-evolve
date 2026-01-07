// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'image.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VImage _$VImageFromJson(Map<String, dynamic> json) => VImage()
  ..background = json['background'] == null
      ? null
      : VColor.fromJson(json['background'] as Map<String, dynamic>)
  ..filename = json['filename'] as String?
  ..imageData = json['imageData'] == null
      ? null
      : VImageData.fromJson(json['imageData'] as Map<String, dynamic>);

Map<String, dynamic> _$VImageToJson(VImage instance) => <String, dynamic>{
  'background': instance.background,
  'filename': instance.filename,
  'imageData': instance.imageData,
};
