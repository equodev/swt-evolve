// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glyphmetrics.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VGlyphMetrics _$VGlyphMetricsFromJson(Map<String, dynamic> json) =>
    VGlyphMetrics()
      ..ascent = (json['ascent'] as num).toInt()
      ..descent = (json['descent'] as num).toInt()
      ..width = (json['width'] as num).toInt();

Map<String, dynamic> _$VGlyphMetricsToJson(VGlyphMetrics instance) =>
    <String, dynamic>{
      'ascent': instance.ascent,
      'descent': instance.descent,
      'width': instance.width,
    };
