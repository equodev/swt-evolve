import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/imagedata.dart';

part 'image.g.dart';

@JsonSerializable()
class VImage {
  VImage() : this.empty();
  VImage.empty();

  VColor? background;
  String? filename;
  int? height;
  VImageData? imageData;
  int? remoteRef;
  String? svgContent;
  int? width;

  factory VImage.fromJson(Map<String, dynamic> json) => _$VImageFromJson(json);
  Map<String, dynamic> toJson() => _$VImageToJson(this);
}
