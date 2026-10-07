import 'package:json_annotation/json_annotation.dart';
import '../gen/font.dart';
import '../gen/pathdata.dart';

part 'path.g.dart';

@JsonSerializable()
class VPath {
  VPath() : this.empty();
  VPath.empty();

  VPathData? pathData;

  List<String?>? textStrings;
  List<double>? textOrigins;
  List<VFont?>? textFonts;

  factory VPath.fromJson(Map<String, dynamic> json) => _$VPathFromJson(json);
  Map<String, dynamic> toJson() => _$VPathToJson(this);
}
