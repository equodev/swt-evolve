import 'package:json_annotation/json_annotation.dart';

part 'region.g.dart';

@JsonSerializable()
class VRegion {
  VRegion() : this.empty();
  VRegion.empty();

  factory VRegion.fromJson(Map<String, dynamic> json) =>
      _$VRegionFromJson(json);
  Map<String, dynamic> toJson() => _$VRegionToJson(this);
}
