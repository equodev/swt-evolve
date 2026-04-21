import 'package:json_annotation/json_annotation.dart';

part 'devicedata.g.dart';

@JsonSerializable()
class VDeviceData {
  VDeviceData() : this.empty();
  VDeviceData.empty();

  bool debug = false;
  bool tracking = false;

  factory VDeviceData.fromJson(Map<String, dynamic> json) =>
      _$VDeviceDataFromJson(json);
  Map<String, dynamic> toJson() => _$VDeviceDataToJson(this);
}
