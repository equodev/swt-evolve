// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taskitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTaskItem _$VTaskItemFromJson(Map<String, dynamic> json) => VTaskItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..menu = json['menu'] == null
      ? null
      : VMenu.fromJson(json['menu'] as Map<String, dynamic>)
  ..overlayImage = json['overlayImage'] == null
      ? null
      : VImage.fromJson(json['overlayImage'] as Map<String, dynamic>)
  ..overlayText = json['overlayText'] as String?
  ..progress = (json['progress'] as num?)?.toInt()
  ..progressState = (json['progressState'] as num?)?.toInt();

Map<String, dynamic> _$VTaskItemToJson(VTaskItem instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'image': ?instance.image,
  'text': ?instance.text,
  'menu': ?instance.menu,
  'overlayImage': ?instance.overlayImage,
  'overlayText': ?instance.overlayText,
  'progress': ?instance.progress,
  'progressState': ?instance.progressState,
};
