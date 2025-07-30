// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rowlayout.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RowLayoutValue _$RowLayoutValueFromJson(Map<String, dynamic> json) =>
    RowLayoutValue()
      ..swt = json['swt'] as String
      ..type = (json['type'] as num).toInt()
      ..marginWidth = (json['marginWidth'] as num).toInt()
      ..marginHeight = (json['marginHeight'] as num).toInt()
      ..spacing = (json['spacing'] as num).toInt()
      ..wrap = json['wrap'] as bool
      ..pack = json['pack'] as bool
      ..fill = json['fill'] as bool
      ..center = json['center'] as bool
      ..justify = json['justify'] as bool
      ..marginLeft = (json['marginLeft'] as num).toInt()
      ..marginTop = (json['marginTop'] as num).toInt()
      ..marginRight = (json['marginRight'] as num).toInt()
      ..marginBottom = (json['marginBottom'] as num).toInt();

Map<String, dynamic> _$RowLayoutValueToJson(RowLayoutValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'type': instance.type,
      'marginWidth': instance.marginWidth,
      'marginHeight': instance.marginHeight,
      'spacing': instance.spacing,
      'wrap': instance.wrap,
      'pack': instance.pack,
      'fill': instance.fill,
      'center': instance.center,
      'justify': instance.justify,
      'marginLeft': instance.marginLeft,
      'marginTop': instance.marginTop,
      'marginRight': instance.marginRight,
      'marginBottom': instance.marginBottom,
    };
