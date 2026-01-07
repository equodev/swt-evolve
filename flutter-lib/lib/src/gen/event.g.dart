// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VEvent _$VEventFromJson(Map<String, dynamic> json) => VEvent()
  ..button = (json['button'] as num?)?.toInt()
  ..character = (json['character'] as num?)?.toInt()
  ..count = (json['count'] as num?)?.toInt()
  ..detail = (json['detail'] as num?)?.toInt()
  ..doit = json['doit'] as bool?
  ..end = (json['end'] as num?)?.toInt()
  ..gc = json['gc'] == null
      ? null
      : VGC.fromJson(json['gc'] as Map<String, dynamic>)
  ..height = (json['height'] as num?)?.toInt()
  ..index = (json['index'] as num?)?.toInt()
  ..item = json['item'] == null
      ? null
      : VWidget.fromJson(json['item'] as Map<String, dynamic>)
  ..keyCode = (json['keyCode'] as num?)?.toInt()
  ..keyLocation = (json['keyLocation'] as num?)?.toInt()
  ..magnification = (json['magnification'] as num?)?.toDouble()
  ..rotation = (json['rotation'] as num?)?.toDouble()
  ..segments = (json['segments'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..segmentsChars = (json['segmentsChars'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..start = (json['start'] as num?)?.toInt()
  ..stateMask = (json['stateMask'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..time = (json['time'] as num?)?.toInt()
  ..type = (json['type'] as num?)?.toInt()
  ..widget = json['widget'] == null
      ? null
      : VWidget.fromJson(json['widget'] as Map<String, dynamic>)
  ..width = (json['width'] as num?)?.toInt()
  ..x = (json['x'] as num?)?.toInt()
  ..xDirection = (json['xDirection'] as num?)?.toInt()
  ..y = (json['y'] as num?)?.toInt()
  ..yDirection = (json['yDirection'] as num?)?.toInt();

Map<String, dynamic> _$VEventToJson(VEvent instance) => <String, dynamic>{
  'button': instance.button,
  'character': instance.character,
  'count': instance.count,
  'detail': instance.detail,
  'doit': instance.doit,
  'end': instance.end,
  'gc': instance.gc,
  'height': instance.height,
  'index': instance.index,
  'item': instance.item,
  'keyCode': instance.keyCode,
  'keyLocation': instance.keyLocation,
  'magnification': instance.magnification,
  'rotation': instance.rotation,
  'segments': instance.segments,
  'segmentsChars': instance.segmentsChars,
  'start': instance.start,
  'stateMask': instance.stateMask,
  'text': instance.text,
  'time': instance.time,
  'type': instance.type,
  'widget': instance.widget,
  'width': instance.width,
  'x': instance.x,
  'xDirection': instance.xDirection,
  'y': instance.y,
  'yDirection': instance.yDirection,
};
