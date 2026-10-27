// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cursor.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCursor _$VCursorFromJson(Map<String, dynamic> json) => VCursor()
  ..cursorStyle = (json['cursorStyle'] as num?)?.toInt()
  ..hotspotX = (json['hotspotX'] as num?)?.toInt()
  ..hotspotY = (json['hotspotY'] as num?)?.toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>);

Map<String, dynamic> _$VCursorToJson(VCursor instance) => <String, dynamic>{
  'cursorStyle': ?instance.cursorStyle,
  'hotspotX': ?instance.hotspotX,
  'hotspotY': ?instance.hotspotY,
  'image': ?instance.image,
};
