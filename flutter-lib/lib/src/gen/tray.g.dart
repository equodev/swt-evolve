// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tray.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VTray _$VTrayFromJson(Map<String, dynamic> json) => VTray()
  ..swt = json['swt'] as String
  ..id = (json['id'] as num).toInt()
  ..seq = (json['_s'] as num?)?.toInt() ?? 0
  ..style = (json['style'] as num?)?.toInt() ?? 0;

Map<String, dynamic> _$VTrayToJson(VTray instance) => <String, dynamic>{
  'swt': instance.swt,
  'id': instance.id,
  'style': instance.style,
};
