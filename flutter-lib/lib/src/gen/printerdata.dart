import 'package:json_annotation/json_annotation.dart';
import '../gen/devicedata.dart';

part 'printerdata.g.dart';

@JsonSerializable()
class VPrinterData extends VDeviceData {
  VPrinterData() : this.empty();
  VPrinterData.empty() : super.empty();

  factory VPrinterData.fromJson(Map<String, dynamic> json) =>
      _$VPrinterDataFromJson(json);
  Map<String, dynamic> toJson() => _$VPrinterDataToJson(this);
}
