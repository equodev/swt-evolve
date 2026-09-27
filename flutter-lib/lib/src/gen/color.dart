import 'package:json_annotation/json_annotation.dart';

part 'color.g.dart';

@JsonSerializable()
class VColor {
  VColor() : this.empty();
  VColor.empty();

  @JsonKey(name: 'a')
  int alpha = 0;
  @JsonKey(name: 'b')
  int blue = 0;
  @JsonKey(name: 'g')
  int green = 0;
  @JsonKey(name: 'r')
  int red = 0;

  factory VColor.fromJson(Map<String, dynamic> json) => _$VColorFromJson(json);
  Map<String, dynamic> toJson() => _$VColorToJson(this);
}
