// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taskbar.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTaskBar _$VTaskBarFromJson(Map<String, dynamic> json) => VTaskBar()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0;

Map<String, dynamic> _$VTaskBarToJson(VTaskBar instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
};
