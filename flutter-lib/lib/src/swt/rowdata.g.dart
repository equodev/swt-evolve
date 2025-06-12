// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rowdata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RowDataValue _$RowDataValueFromJson(Map<String, dynamic> json) => RowDataValue()
  ..width = (json['width'] as num).toInt()
  ..height = (json['height'] as num).toInt()
  ..exclude = json['exclude'] as bool;

Map<String, dynamic> _$RowDataValueToJson(RowDataValue instance) =>
    <String, dynamic>{
      'width': instance.width,
      'height': instance.height,
      'exclude': instance.exclude,
    };
