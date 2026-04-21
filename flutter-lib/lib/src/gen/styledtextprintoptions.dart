import 'package:json_annotation/json_annotation.dart';

part 'styledtextprintoptions.g.dart';

@JsonSerializable()
class VStyledTextPrintOptions {
  VStyledTextPrintOptions() : this.empty();
  VStyledTextPrintOptions.empty();

  String? PAGE_TAG;
  String? SEPARATOR;
  String? footer;
  String? header;
  String? jobName;
  List<String>? lineLabels;
  bool? printLineBackground;
  bool? printLineNumbers;
  bool? printTextBackground;
  bool? printTextFontStyle;
  bool? printTextForeground;

  factory VStyledTextPrintOptions.fromJson(Map<String, dynamic> json) =>
      _$VStyledTextPrintOptionsFromJson(json);
  Map<String, dynamic> toJson() => _$VStyledTextPrintOptionsToJson(this);
}
