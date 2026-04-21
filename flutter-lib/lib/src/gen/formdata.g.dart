// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'formdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VFormData _$VFormDataFromJson(Map<String, dynamic> json) => VFormData()
  ..bottom = json['bottom'] == null
      ? null
      : VFormAttachment.fromJson(json['bottom'] as Map<String, dynamic>)
  ..height = (json['height'] as num?)?.toInt()
  ..left = json['left'] == null
      ? null
      : VFormAttachment.fromJson(json['left'] as Map<String, dynamic>)
  ..right = json['right'] == null
      ? null
      : VFormAttachment.fromJson(json['right'] as Map<String, dynamic>)
  ..top = json['top'] == null
      ? null
      : VFormAttachment.fromJson(json['top'] as Map<String, dynamic>)
  ..width = (json['width'] as num?)?.toInt();

Map<String, dynamic> _$VFormDataToJson(VFormData instance) => <String, dynamic>{
  'bottom': ?instance.bottom,
  'height': ?instance.height,
  'left': ?instance.left,
  'right': ?instance.right,
  'top': ?instance.top,
  'width': ?instance.width,
};
