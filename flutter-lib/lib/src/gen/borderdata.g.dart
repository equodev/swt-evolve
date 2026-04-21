// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'borderdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VBorderData _$VBorderDataFromJson(Map<String, dynamic> json) => VBorderData()
  ..hHint = (json['hHint'] as num).toInt()
  ..region = (json['region'] as num).toInt()
  ..wHint = (json['wHint'] as num).toInt();

Map<String, dynamic> _$VBorderDataToJson(VBorderData instance) =>
    <String, dynamic>{
      'hHint': instance.hHint,
      'region': instance.region,
      'wHint': instance.wHint,
    };
