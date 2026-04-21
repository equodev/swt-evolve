// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'formattachment.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VFormAttachment _$VFormAttachmentFromJson(Map<String, dynamic> json) =>
    VFormAttachment()
      ..alignment = (json['alignment'] as num?)?.toInt()
      ..control = json['control'] == null
          ? null
          : VControl.fromJson(json['control'] as Map<String, dynamic>)
      ..denominator = (json['denominator'] as num?)?.toInt()
      ..numerator = (json['numerator'] as num?)?.toInt()
      ..offset = (json['offset'] as num?)?.toInt();

Map<String, dynamic> _$VFormAttachmentToJson(VFormAttachment instance) =>
    <String, dynamic>{
      'alignment': ?instance.alignment,
      'control': ?instance.control,
      'denominator': ?instance.denominator,
      'numerator': ?instance.numerator,
      'offset': ?instance.offset,
    };
