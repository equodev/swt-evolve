// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tray.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTray _$VTrayFromJson(Map<String, dynamic> json) => VTray()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..style = (json['style'] as num).toInt()
  ..items = (json['items'] as List<dynamic>?)
      ?.map((e) => VTrayItem.fromJson(e as Map<String, dynamic>))
      .toList();

Map<String, dynamic> _$VTrayToJson(VTray instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
  'items': ?instance.items,
};
