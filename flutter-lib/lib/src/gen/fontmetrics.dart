import 'package:json_annotation/json_annotation.dart';

part 'fontmetrics.g.dart';

@JsonSerializable()
class VFontMetrics {
  VFontMetrics() : this.empty();
  VFontMetrics.empty();

  factory VFontMetrics.fromJson(Map<String, dynamic> json) =>
      _$VFontMetricsFromJson(json);
  Map<String, dynamic> toJson() => _$VFontMetricsToJson(this);
}
