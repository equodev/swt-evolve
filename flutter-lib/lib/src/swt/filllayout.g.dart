// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'filllayout.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FillLayoutValue _$FillLayoutValueFromJson(Map<String, dynamic> json) =>
    FillLayoutValue()
      ..swt = json['swt'] as String
      ..type = (json['type'] as num).toInt()
      ..marginWidth = (json['marginWidth'] as num).toInt()
      ..marginHeight = (json['marginHeight'] as num).toInt()
      ..spacing = (json['spacing'] as num).toInt();

Map<String, dynamic> _$FillLayoutValueToJson(FillLayoutValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'type': instance.type,
      'marginWidth': instance.marginWidth,
      'marginHeight': instance.marginHeight,
      'spacing': instance.spacing,
    };
