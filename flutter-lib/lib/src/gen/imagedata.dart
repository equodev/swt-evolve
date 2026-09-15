import 'package:json_annotation/json_annotation.dart';
import '../impl/utils/image_utils.dart';

part 'imagedata.g.dart';

@JsonSerializable()
class VImageData {
  VImageData() : this.empty();
  VImageData.empty();

  @JsonKey(
    fromJson: ImageUtils.parseByteArray,
    toJson: ImageUtils.serializeByteArray,
  )
  List<int>? data;
  int? depth;
  int? height;
  int? width;

  factory VImageData.fromJson(Map<String, dynamic> json) =>
      _$VImageDataFromJson(json);
  Map<String, dynamic> toJson() => _$VImageDataToJson(this);
}
