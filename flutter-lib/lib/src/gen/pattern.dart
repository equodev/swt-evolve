import 'package:json_annotation/json_annotation.dart';

part 'pattern.g.dart';

@JsonSerializable()
class VPattern {
  VPattern() : this.empty();
  VPattern.empty();

  factory VPattern.fromJson(Map<String, dynamic> json) =>
      _$VPatternFromJson(json);
  Map<String, dynamic> toJson() => _$VPatternToJson(this);
}
