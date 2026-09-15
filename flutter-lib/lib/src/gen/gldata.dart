import 'package:json_annotation/json_annotation.dart';

part 'gldata.g.dart';

@JsonSerializable()
class VGLData {
  VGLData() : this.empty();
  VGLData.empty();

  factory VGLData.fromJson(Map<String, dynamic> json) =>
      _$VGLDataFromJson(json);
  Map<String, dynamic> toJson() => _$VGLDataToJson(this);
}
