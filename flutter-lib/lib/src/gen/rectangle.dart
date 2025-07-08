import 'package:json_annotation/json_annotation.dart';

part 'rectangle.g.dart';

@JsonSerializable()
class VRectangle {
  VRectangle() : this.empty();
  VRectangle.empty();

  int x = 0;
  int y = 0;
  int width = 0;
  int height = 0;

  factory VRectangle.fromJson(Map<String, dynamic> json) =>
      _$VRectangleFromJson(json);
  Map<String, dynamic> toJson() => _$VRectangleToJson(this);
}
