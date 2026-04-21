// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cursor.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCursor _$VCursorFromJson(Map<String, dynamic> json) => VCursor()
  ..cursorStyle = (json['cursorStyle'] as num?)?.toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>);

Map<String, dynamic> _$VCursorToJson(VCursor instance) => <String, dynamic>{
  'cursorStyle': ?instance.cursorStyle,
  'image': ?instance.image,
};
