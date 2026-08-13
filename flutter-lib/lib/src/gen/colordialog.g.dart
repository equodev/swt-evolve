// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'colordialog.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VColorDialog _$VColorDialogFromJson(Map<String, dynamic> json) => VColorDialog()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..rgb = json['RGB'] == null
      ? null
      : VRGB.fromJson(json['RGB'] as Map<String, dynamic>)
  ..rgbs = (json['RGBs'] as List<dynamic>?)
      ?.map((e) => VRGB.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VColorDialogToJson(VColorDialog instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': ?instance.style,
      'text': ?instance.text,
      'RGB': ?instance.rgb,
      'RGBs': ?instance.rgbs,
    };
