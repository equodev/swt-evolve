import 'package:json_annotation/json_annotation.dart';

part 'event.g.dart';

@JsonSerializable()
class VEvent {
  VEvent() : this.empty();
  VEvent.empty();

  int x = 0;

  factory VEvent.fromJson(Map<String, dynamic> json) => _$VEventFromJson(json);
  Map<String, dynamic> toJson() => _$VEventToJson(this);
}
