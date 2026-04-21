import 'package:json_annotation/json_annotation.dart';

part 'monitor.g.dart';

@JsonSerializable()
class VMonitor {
  VMonitor() : this.empty();
  VMonitor.empty();

  factory VMonitor.fromJson(Map<String, dynamic> json) =>
      _$VMonitorFromJson(json);
  Map<String, dynamic> toJson() => _$VMonitorToJson(this);
}
