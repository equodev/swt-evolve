import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';

part 'pattern.g.dart';

@JsonSerializable()
class VPattern {
  VPattern() : this.empty();
  VPattern.empty();

  VColor? color1;
  VColor? color2;
  double? endX;
  double? endY;
  double? startX;
  double? startY;

  factory VPattern.fromJson(Map<String, dynamic> json) =>
      _$VPatternFromJson(json);
  Map<String, dynamic> toJson() => _$VPatternToJson(this);
}
