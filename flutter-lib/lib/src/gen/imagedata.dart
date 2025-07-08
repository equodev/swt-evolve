import 'package:json_annotation/json_annotation.dart';

part 'imagedata.g.dart';

@JsonSerializable()
class VImageData {
  VImageData() : this.empty();
  VImageData.empty();

  int alpha = 0;
  List<int>? alphaData = null;

  factory VImageData.fromJson(Map<String, dynamic> json) =>
      _$VImageDataFromJson(json);
  Map<String, dynamic> toJson() => _$VImageDataToJson(this);
}
