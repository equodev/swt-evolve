// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coolitem.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VCoolItem _$VCoolItemFromJson(Map<String, dynamic> json) => VCoolItem()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..image = json['image'] == null
      ? null
      : VImage.fromJson(json['image'] as Map<String, dynamic>)
  ..text = json['text'] as String?
  ..control = json['control'] == null
      ? null
      : VControl.fromJson(json['control'] as Map<String, dynamic>)
  ..minimumSize = json['minimumSize'] == null
      ? null
      : VPoint.fromJson(json['minimumSize'] as Map<String, dynamic>)
  ..preferredSize = json['preferredSize'] == null
      ? null
      : VPoint.fromJson(json['preferredSize'] as Map<String, dynamic>);

Map<String, dynamic> _$VCoolItemToJson(VCoolItem instance) => <String, dynamic>{
      'swt': instance.swt,
      'id': instance.id,
      'style': instance.style,
      'image': instance.image,
      'text': instance.text,
      'control': instance.control,
      'minimumSize': instance.minimumSize,
      'preferredSize': instance.preferredSize,
    };
