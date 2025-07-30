// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'gridlayout.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GridLayoutValue _$GridLayoutValueFromJson(Map<String, dynamic> json) =>
    GridLayoutValue()
      ..swt = json['swt'] as String
      ..numColumns = (json['numColumns'] as num).toInt()
      ..makeColumnsEqualWidth = json['makeColumnsEqualWidth'] as bool
      ..marginWidth = (json['marginWidth'] as num).toInt()
      ..marginHeight = (json['marginHeight'] as num).toInt()
      ..marginLeft = (json['marginLeft'] as num).toInt()
      ..marginTop = (json['marginTop'] as num).toInt()
      ..marginRight = (json['marginRight'] as num).toInt()
      ..marginBottom = (json['marginBottom'] as num).toInt()
      ..horizontalSpacing = (json['horizontalSpacing'] as num).toInt()
      ..verticalSpacing = (json['verticalSpacing'] as num).toInt();

Map<String, dynamic> _$GridLayoutValueToJson(GridLayoutValue instance) =>
    <String, dynamic>{
      'swt': instance.swt,
      'numColumns': instance.numColumns,
      'makeColumnsEqualWidth': instance.makeColumnsEqualWidth,
      'marginWidth': instance.marginWidth,
      'marginHeight': instance.marginHeight,
      'marginLeft': instance.marginLeft,
      'marginTop': instance.marginTop,
      'marginRight': instance.marginRight,
      'marginBottom': instance.marginBottom,
      'horizontalSpacing': instance.horizontalSpacing,
      'verticalSpacing': instance.verticalSpacing,
    };
