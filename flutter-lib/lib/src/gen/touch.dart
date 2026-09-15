import 'package:json_annotation/json_annotation.dart';

part 'touch.g.dart';

@JsonSerializable()
class VTouch {
  VTouch() : this.empty();
  VTouch.empty();

  factory VTouch.fromJson(Map<String, dynamic> json) => _$VTouchFromJson(json);
  Map<String, dynamic> toJson() => _$VTouchToJson(this);
}
