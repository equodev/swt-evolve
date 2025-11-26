import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/imagedata.dart';

part 'image.g.dart';


@JsonSerializable() class VImage {
  VImage() : this.empty();
  VImage.empty() ;
  
  VColor? background;
  String? filename;
  VImageData? imageData;
  
  factory VImage.fromJson(Map<String, dynamic> json) => _$VImageFromJson(json);
  Map<String, dynamic> toJson() => _$VImageToJson(this);
  
}