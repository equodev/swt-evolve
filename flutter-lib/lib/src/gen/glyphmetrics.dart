import 'package:json_annotation/json_annotation.dart';

part 'glyphmetrics.g.dart';

@JsonSerializable()
class VGlyphMetrics {
  VGlyphMetrics() : this.empty();
  VGlyphMetrics.empty();

  factory VGlyphMetrics.fromJson(Map<String, dynamic> json) =>
      _$VGlyphMetricsFromJson(json);
  Map<String, dynamic> toJson() => _$VGlyphMetricsToJson(this);
}
