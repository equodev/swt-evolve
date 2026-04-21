// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taskbar.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTaskBar _$VTaskBarFromJson(Map<String, dynamic> json) => VTaskBar()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VTaskItem.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VTaskBarToJson(VTaskBar instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'items': ?instance.items,
};
