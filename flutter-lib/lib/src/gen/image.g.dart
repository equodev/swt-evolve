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
  ..height = (json['height'] as num?)?.toInt()
  ..imageData = json['imageData'] == null
      ? null
      : VImageData.fromJson(json['imageData'] as Map<String, dynamic>)
  ..remoteRef = (json['remoteRef'] as num?)?.toInt()
  ..svgContent = json['svgContent'] as String?
  ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$VImageToJson(VImage instance) => <String, dynamic>{
  'background': ?instance.background,
  'filename': ?instance.filename,
  'height': ?instance.height,
  'imageData': ?instance.imageData,
  'remoteRef': ?instance.remoteRef,
  'svgContent': ?instance.svgContent,
  'width': ?instance.width,
};
