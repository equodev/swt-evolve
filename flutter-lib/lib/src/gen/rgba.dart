import 'package:json_annotation/json_annotation.dart';

part 'rgba.g.dart';

@JsonSerializable()
class VRGBA {
  VRGBA() : this.empty();
  VRGBA.empty();

  int alpha = 0;

  factory VRGBA.fromJson(Map<String, dynamic> json) => _$VRGBAFromJson(json);
  Map<String, dynamic> toJson() => _$VRGBAToJson(this);
}
