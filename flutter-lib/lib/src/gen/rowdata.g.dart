// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rowdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VRowData _$VRowDataFromJson(Map<String, dynamic> json) => VRowData()
  ..exclude = json['exclude'] as bool
  ..height = (json['height'] as num).toInt()
  ..width = (json['width'] as num).toInt();

Map<String, dynamic> _$VRowDataToJson(VRowData instance) => <String, dynamic>{
  'exclude': instance.exclude,
  'height': instance.height,
  'width': instance.width,
};
