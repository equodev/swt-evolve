import 'package:json_annotation/json_annotation.dart';
import '../gen/fontdata.dart';

part 'font.g.dart';


@JsonSerializable() class VFont {
  VFont() : this.empty();
  VFont.empty() ;
  
  List<VFontData>? fontData;
  
  factory VFont.fromJson(Map<String, dynamic> json) => _$VFontFromJson(json);
  Map<String, dynamic> toJson() => _$VFontToJson(this);
  
}