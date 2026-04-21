import 'package:json_annotation/json_annotation.dart';

part 'rgb.g.dart';

@JsonSerializable()
class VRGB {
  VRGB() : this.empty();
  VRGB.empty();

  int blue = 0;
  int green = 0;
  int red = 0;

  factory VRGB.fromJson(Map<String, dynamic> json) => _$VRGBFromJson(json);
  Map<String, dynamic> toJson() => _$VRGBToJson(this);
}
