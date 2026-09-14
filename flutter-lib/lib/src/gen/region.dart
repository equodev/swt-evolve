import 'package:json_annotation/json_annotation.dart';

part 'region.g.dart';

@JsonSerializable()
class VRegion {
  VRegion() : this.empty();
  VRegion.empty();

  /// The region's rectangles, flattened to `x, y, width, height` runs. A region is a set
  /// of disjoint rectangles, so its bounds are not its shape -- the outlines it is used
  /// for are mostly hole.
  List<int>? rects;

  factory VRegion.fromJson(Map<String, dynamic> json) =>
      _$VRegionFromJson(json);
  Map<String, dynamic> toJson() => _$VRegionToJson(this);
}
