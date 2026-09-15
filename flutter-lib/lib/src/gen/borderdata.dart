import 'package:json_annotation/json_annotation.dart';

part 'borderdata.g.dart';

@JsonSerializable()
class VBorderData {
  VBorderData() : this.empty();
  VBorderData.empty();

  factory VBorderData.fromJson(Map<String, dynamic> json) =>
      _$VBorderDataFromJson(json);
  Map<String, dynamic> toJson() => _$VBorderDataToJson(this);
}
