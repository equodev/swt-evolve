import 'package:json_annotation/json_annotation.dart';

part 'event.g.dart';

@JsonSerializable()
class VEvent {
  VEvent() : this.empty();
  VEvent.empty();

  int? button;
  int? character;
  int? count;
  int? currentDataTypeId;
  int? detail;
  bool? doit;
  int? end;
  int? feedback;
  int? height;
  int? index;
  int? itemId;
  int? keyCode;
  int? keyLocation;
  List<int>? segments;
  int? start;
  int? stateMask;
  String? text;
  int? time;
  int? type;
  int? width;
  int? x;
  int? y;

  factory VEvent.fromJson(Map<String, dynamic> json) => _$VEventFromJson(json);
  Map<String, dynamic> toJson() => _$VEventToJson(this);
}
