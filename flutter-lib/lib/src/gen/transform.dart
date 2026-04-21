import 'package:json_annotation/json_annotation.dart';

part 'transform.g.dart';

@JsonSerializable()
class VTransform {
  VTransform() : this.empty();
  VTransform.empty();

  factory VTransform.fromJson(Map<String, dynamic> json) =>
      _$VTransformFromJson(json);
  Map<String, dynamic> toJson() => _$VTransformToJson(this);
}
