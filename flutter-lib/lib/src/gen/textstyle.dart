import 'package:json_annotation/json_annotation.dart';
import '../gen/color.dart';
import '../gen/font.dart';

part 'textstyle.g.dart';

@JsonSerializable()
class VTextStyle {
  VTextStyle() : this.empty();
  VTextStyle.empty();

  VColor? background;
  VColor? borderColor;
  int? borderStyle;
  VFont? font;
  VColor? foreground;
  int? rise;
  bool? strikeout;
  VColor? strikeoutColor;
  bool? underline;
  VColor? underlineColor;
  int? underlineStyle;

  factory VTextStyle.fromJson(Map<String, dynamic> json) =>
      _$VTextStyleFromJson(json);
  Map<String, dynamic> toJson() => _$VTextStyleToJson(this);
}
