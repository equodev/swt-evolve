import 'package:json_annotation/json_annotation.dart';

part 'styledtextprintoptions.g.dart';

@JsonSerializable()
class VStyledTextPrintOptions {
  VStyledTextPrintOptions() : this.empty();
  VStyledTextPrintOptions.empty();

  factory VStyledTextPrintOptions.fromJson(Map<String, dynamic> json) =>
      _$VStyledTextPrintOptionsFromJson(json);
  Map<String, dynamic> toJson() => _$VStyledTextPrintOptionsToJson(this);
}
