import 'package:json_annotation/json_annotation.dart';

part 'rowdata.g.dart';

@JsonSerializable()
class VRowData {
  VRowData() : this.empty();
  VRowData.empty();

  factory VRowData.fromJson(Map<String, dynamic> json) =>
      _$VRowDataFromJson(json);
  Map<String, dynamic> toJson() => _$VRowDataToJson(this);
}
