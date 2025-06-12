// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'griddata.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GridDataValue _$GridDataValueFromJson(Map<String, dynamic> json) =>
    GridDataValue()
      ..verticalAlignment = (json['verticalAlignment'] as num).toInt()
      ..horizontalAlignment = (json['horizontalAlignment'] as num).toInt()
      ..widthHint = (json['widthHint'] as num).toInt()
      ..heightHint = (json['heightHint'] as num).toInt()
      ..horizontalIndent = (json['horizontalIndent'] as num).toInt()
      ..verticalIndent = (json['verticalIndent'] as num).toInt()
      ..horizontalSpan = (json['horizontalSpan'] as num).toInt()
      ..verticalSpan = (json['verticalSpan'] as num).toInt()
      ..grabExcessHorizontalSpace = json['grabExcessHorizontalSpace'] as bool
      ..grabExcessVerticalSpace = json['grabExcessVerticalSpace'] as bool
      ..minimumWidth = (json['minimumWidth'] as num).toInt()
      ..minimumHeight = (json['minimumHeight'] as num).toInt()
      ..exclude = json['exclude'] as bool;

Map<String, dynamic> _$GridDataValueToJson(GridDataValue instance) =>
    <String, dynamic>{
      'verticalAlignment': instance.verticalAlignment,
      'horizontalAlignment': instance.horizontalAlignment,
      'widthHint': instance.widthHint,
      'heightHint': instance.heightHint,
      'horizontalIndent': instance.horizontalIndent,
      'verticalIndent': instance.verticalIndent,
      'horizontalSpan': instance.horizontalSpan,
      'verticalSpan': instance.verticalSpan,
      'grabExcessHorizontalSpace': instance.grabExcessHorizontalSpace,
      'grabExcessVerticalSpace': instance.grabExcessVerticalSpace,
      'minimumWidth': instance.minimumWidth,
      'minimumHeight': instance.minimumHeight,
      'exclude': instance.exclude,
    };
