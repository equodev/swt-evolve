import 'package:json_annotation/json_annotation.dart';
import '../gen/touchsource.dart';

part 'touch.g.dart';

@JsonSerializable()
class VTouch {
  VTouch() : this.empty();
  VTouch.empty();

  int? id;
  bool? primary;
  VTouchSource? source;
  int? state;
  int? x;
  int? y;

  factory VTouch.fromJson(Map<String, dynamic> json) => _$VTouchFromJson(json);
  Map<String, dynamic> toJson() => _$VTouchToJson(this);
}
