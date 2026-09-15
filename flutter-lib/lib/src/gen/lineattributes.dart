import 'package:json_annotation/json_annotation.dart';

part 'lineattributes.g.dart';

@JsonSerializable()
class VLineAttributes {
  VLineAttributes() : this.empty();
  VLineAttributes.empty();

  factory VLineAttributes.fromJson(Map<String, dynamic> json) =>
      _$VLineAttributesFromJson(json);
  Map<String, dynamic> toJson() => _$VLineAttributesToJson(this);
}
