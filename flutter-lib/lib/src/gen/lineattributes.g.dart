// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'lineattributes.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VLineAttributes _$VLineAttributesFromJson(Map<String, dynamic> json) =>
    VLineAttributes()
      ..cap = (json['cap'] as num?)?.toInt()
      ..dash = (json['dash'] as List<dynamic>?)
          ?.map((e) => (e as num).toDouble())
          .toList()
      ..dashOffset = (json['dashOffset'] as num?)?.toDouble()
      ..join = (json['join'] as num?)?.toInt()
      ..miterLimit = (json['miterLimit'] as num?)?.toDouble()
      ..style = (json['style'] as num?)?.toInt()
      ..width = (json['width'] as num?)?.toDouble();

Map<String, dynamic> _$VLineAttributesToJson(VLineAttributes instance) =>
    <String, dynamic>{
      'cap': ?instance.cap,
      'dash': ?instance.dash,
      'dashOffset': ?instance.dashOffset,
      'join': ?instance.join,
      'miterLimit': ?instance.miterLimit,
      'style': ?instance.style,
      'width': ?instance.width,
    };
