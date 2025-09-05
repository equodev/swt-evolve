import 'package:json_annotation/json_annotation.dart';
import '../impl/utils/image_utils.dart';

part 'imagedata.g.dart';

@JsonSerializable()
class VImageData {
  VImageData() : this.empty();
  VImageData.empty();

  int? alpha;
  @JsonKey(
      fromJson: ImageUtils.parseByteArray,
      toJson: ImageUtils.serializeByteArray)
  List<int>? alphaData;
  int? bytesPerLine;
  @JsonKey(
      fromJson: ImageUtils.parseByteArray,
      toJson: ImageUtils.serializeByteArray)
  List<int>? data;
  int? delayTime;
  int? depth;
  int? disposalMethod;
  int? height;
  @JsonKey(
      fromJson: ImageUtils.parseByteArray,
      toJson: ImageUtils.serializeByteArray)
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
