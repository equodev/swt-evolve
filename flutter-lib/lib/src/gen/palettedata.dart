import 'package:json_annotation/json_annotation.dart';

part 'palettedata.g.dart';

@JsonSerializable()
class VPaletteData {
  VPaletteData() : this.empty();
  VPaletteData.empty();

  factory VPaletteData.fromJson(Map<String, dynamic> json) =>
      _$VPaletteDataFromJson(json);
  Map<String, dynamic> toJson() => _$VPaletteDataToJson(this);
}
