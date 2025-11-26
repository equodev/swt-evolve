import 'package:json_annotation/json_annotation.dart';

part 'color.g.dart';


@JsonSerializable() class VColor {
  VColor() : this.empty();
  VColor.empty() ;
  
  int alpha = 0;
  int blue = 0;
  int green = 0;
  int red = 0;
  
  factory VColor.fromJson(Map<String, dynamic> json) => _$VColorFromJson(json);
  Map<String, dynamic> toJson() => _$VColorToJson(this);
  
}