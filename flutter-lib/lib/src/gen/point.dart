import 'package:json_annotation/json_annotation.dart';

part 'point.g.dart';


@JsonSerializable() class VPoint {
  VPoint() : this.empty();
  VPoint.empty() ;
  
  int x = 0;
  int y = 0;
  
  factory VPoint.fromJson(Map<String, dynamic> json) => _$VPointFromJson(json);
  Map<String, dynamic> toJson() => _$VPointToJson(this);
  
}