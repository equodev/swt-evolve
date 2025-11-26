import 'package:json_annotation/json_annotation.dart';

part 'fontdata.g.dart';


@JsonSerializable() class VFontData {
  VFontData() : this.empty();
  VFontData.empty() ;
  
  int height = 0;
  String locale = "";
  String name = "";
  int style = 0;
  
  factory VFontData.fromJson(Map<String, dynamic> json) => _$VFontDataFromJson(json);
  Map<String, dynamic> toJson() => _$VFontDataToJson(this);
  
}