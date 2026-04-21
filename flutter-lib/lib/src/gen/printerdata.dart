import 'package:json_annotation/json_annotation.dart';
import '../gen/devicedata.dart';

part 'printerdata.g.dart';

@JsonSerializable()
class VPrinterData extends VDeviceData {
  VPrinterData() : this.empty();
  VPrinterData.empty() : super.empty();

  int? ALL_PAGES;
  int? DUPLEX_LONG_EDGE;
  int? DUPLEX_NONE;
  int? DUPLEX_SHORT_EDGE;
  int? LANDSCAPE;
  int? PAGE_RANGE;
  int? PORTRAIT;
  int? SELECTION;
  bool? collate;
  int? copyCount;
  String? driver;
  int? duplex;
  int? endPage;
  String? fileName;
  String? name;
  int? orientation;
  bool? printToFile;
  int? scope;
  int? startPage;

  factory VPrinterData.fromJson(Map<String, dynamic> json) =>
      _$VPrinterDataFromJson(json);
  Map<String, dynamic> toJson() => _$VPrinterDataToJson(this);
}
