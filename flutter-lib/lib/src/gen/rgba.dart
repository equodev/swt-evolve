import 'package:json_annotation/json_annotation.dart';
import '../gen/rgb.dart';

part 'rgba.g.dart';

@JsonSerializable()
class VRGBA {
  VRGBA() : this.empty();
  VRGBA.empty();

  int alpha = 0;
  VRGB? rgb;

  factory VRGBA.fromJson(Map<String, dynamic> json) => _$VRGBAFromJson(json);
  Map<String, dynamic> toJson() => _$VRGBAToJson(this);
}
