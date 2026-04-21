// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'display.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VDisplay _$VDisplayFromJson(Map<String, dynamic> json) => VDisplay()
  ..swt = json['swt'] as String?
  ..id = (json['id'] as num?)?.toInt()
  ..shells = (json['shells'] as List<dynamic>?)
      ?.map((e) => VShell.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VDisplayToJson(VDisplay instance) => <String, dynamic>{
  'swt': ?instance.swt,
  'id': ?instance.id,
  'shells': ?instance.shells,
};
