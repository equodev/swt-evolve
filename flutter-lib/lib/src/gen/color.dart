import 'package:json_annotation/json_annotation.dart';

part 'color.g.dart';

@JsonSerializable()
class VColor {
  VColor() : this.empty();
  VColor.empty();

  /// A colour packed as `0xAARRGGBB`, as a resource's change sends it.
  factory VColor.fromArgb(int argb) => VColor()
    ..alpha = (argb >> 24) & 0xFF
    ..red = (argb >> 16) & 0xFF
    ..green = (argb >> 8) & 0xFF
    ..blue = argb & 0xFF;

  /// A colour as either wire form: packed, or spelled out.
  static VColor? read(Object? json) => json == null
      ? null
      : json is num
      ? VColor.fromArgb(json.toInt())
      : VColor.fromJson(json as Map<String, dynamic>);

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
