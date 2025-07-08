import 'package:json_annotation/json_annotation.dart';

part 'imagedata.g.dart';

@JsonSerializable()
class VImageData {
  VImageData() : this.empty();
  VImageData.empty();

  int? alpha;
  List<int>? alphaData;
  int? bytesPerLine;
  List<int>? data;
  int? delayTime;
  int? depth;
  int? disposalMethod;
  int? height;
  List<int>? maskData;
  int? maskPad;
  int? scanlinePad;
  int? transparentPixel;
  int? type;
  int? width;
  int? x;
  int? y;

  factory VImageData.fromJson(Map<String, dynamic> json) =>
      _$VImageDataFromJson(json);
  Map<String, dynamic> toJson() => _$VImageDataToJson(this);
}
