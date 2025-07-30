import 'package:json_annotation/json_annotation.dart';
import '../gen/widget.dart';

part 'event.g.dart';

@JsonSerializable()
class VEvent {
  VEvent() : this.empty();
  VEvent.empty();

  int? button;
  int? character;
  int? count;
  int? detail;
  bool? doit;
  int? end;
  int? height;
  int? index;
  VWidget? item;
  int? keyCode;
  int? keyLocation;
  double? magnification;
  double? rotation;
  List<int>? segments;
  List<int>? segmentsChars;
  int? start;
  int? stateMask;
  String? text;
  int? time;
  int? type;
  VWidget? widget;
  int? width;
  int? x;
  int? xDirection;
  int? y;
  int? yDirection;

  factory VEvent.fromJson(Map<String, dynamic> json) => _$VEventFromJson(json);
  Map<String, dynamic> toJson() => _$VEventToJson(this);
}
