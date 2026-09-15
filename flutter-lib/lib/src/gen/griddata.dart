import 'package:json_annotation/json_annotation.dart';

part 'griddata.g.dart';

@JsonSerializable()
class VGridData {
  VGridData() : this.empty();
  VGridData.empty();

  factory VGridData.fromJson(Map<String, dynamic> json) =>
      _$VGridDataFromJson(json);
  Map<String, dynamic> toJson() => _$VGridDataToJson(this);
}
