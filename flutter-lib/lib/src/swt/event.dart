import 'package:json_annotation/json_annotation.dart';

part 'event.g.dart';

// ToDo: generate me with all fields
@JsonSerializable()
class Event {
  Event();

  int? type;
  int? detail;

  int? index;
  int? x;
  int? y;
  int? width;
  int? height;

  factory Event.fromJson(Map<String, dynamic> json) => _$EventFromJson(json);
  Map<String, dynamic> toJson() => _$EventToJson(this);
}
