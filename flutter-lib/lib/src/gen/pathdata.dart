import 'package:json_annotation/json_annotation.dart';

part 'pathdata.g.dart';

@JsonSerializable()
class VPathData {
  VPathData() : this.empty();
  VPathData.empty();

  List<double>? points;
  List<int>? types;

  factory VPathData.fromJson(Map<String, dynamic> json) =>
      _$VPathDataFromJson(json);
  Map<String, dynamic> toJson() => _$VPathDataToJson(this);
}
