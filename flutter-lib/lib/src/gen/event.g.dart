// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VEvent _$VEventFromJson(Map<String, dynamic> json) => VEvent()
  ..button = (json['button'] as num?)?.toInt()
  ..character = (json['character'] as num?)?.toInt()
  ..count = (json['count'] as num?)?.toInt()
  ..currentDataTypeId = (json['currentDataTypeId'] as num?)?.toInt()
  ..detail = (json['detail'] as num?)?.toInt()
  ..doit = json['doit'] as bool?
  ..end = (json['end'] as num?)?.toInt()
  ..feedback = (json['feedback'] as num?)?.toInt()
  ..height = (json['height'] as num?)?.toInt()
  ..index = (json['index'] as num?)?.toInt()
  ..itemId = (json['itemId'] as num?)?.toInt()
  ..keyCode = (json['keyCode'] as num?)?.toInt()
  ..keyLocation = (json['keyLocation'] as num?)?.toInt()
  ..segments = (json['segments'] as List<dynamic>?)
      ?.map((e) => (e as num).toInt())
      .toList()
  ..start = (json['start'] as num?)?.toInt()
  ..stateMask = (json['stateMask'] as num?)?.toInt()
  ..text = json['text'] as String?
  ..time = (json['time'] as num?)?.toInt()
  ..type = (json['type'] as num?)?.toInt()
  ..width = (json['width'] as num?)?.toInt()
  ..x = (json['x'] as num?)?.toInt()
  ..y = (json['y'] as num?)?.toInt();

Map<String, dynamic> _$VEventToJson(VEvent instance) => <String, dynamic>{
  'button': ?instance.button,
  'character': ?instance.character,
  'count': ?instance.count,
  'currentDataTypeId': ?instance.currentDataTypeId,
  'detail': ?instance.detail,
  'doit': ?instance.doit,
  'end': ?instance.end,
  'feedback': ?instance.feedback,
  'height': ?instance.height,
  'index': ?instance.index,
  'itemId': ?instance.itemId,
  'keyCode': ?instance.keyCode,
  'keyLocation': ?instance.keyLocation,
  'segments': ?instance.segments,
  'start': ?instance.start,
  'stateMask': ?instance.stateMask,
  'text': ?instance.text,
  'time': ?instance.time,
  'type': ?instance.type,
  'width': ?instance.width,
  'x': ?instance.x,
  'y': ?instance.y,
};
