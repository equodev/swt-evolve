import 'package:json_annotation/json_annotation.dart';

part 'lineattributes.g.dart';

@JsonSerializable()
class VLineAttributes {
  VLineAttributes() : this.empty();
  VLineAttributes.empty();

  int? cap;
  List<double>? dash;
  double? dashOffset;
  int? join;
  double? miterLimit;
  int? style;
  double? width;

  factory VLineAttributes.fromJson(Map<String, dynamic> json) =>
      _$VLineAttributesFromJson(json);
  Map<String, dynamic> toJson() => _$VLineAttributesToJson(this);
}
