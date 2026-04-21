import 'package:json_annotation/json_annotation.dart';

part 'touchsource.g.dart';

@JsonSerializable()
class VTouchSource {
  VTouchSource() : this.empty();
  VTouchSource.empty();

  factory VTouchSource.fromJson(Map<String, dynamic> json) =>
      _$VTouchSourceFromJson(json);
  Map<String, dynamic> toJson() => _$VTouchSourceToJson(this);
}
