import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/textstyle.dart';

part 'stylerange.g.dart';

@JsonSerializable()
class VStyleRange extends VTextStyle {
  VStyleRange() : this.empty();
  VStyleRange.empty() : super.empty();

  int fontStyle = 0;
  int length = 0;
  int start = 0;

  factory VStyleRange.fromJson(Map<String, dynamic> json) =>
      _$VStyleRangeFromJson(json);
  Map<String, dynamic> toJson() => _$VStyleRangeToJson(this);
}
