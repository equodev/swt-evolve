import 'package:json_annotation/json_annotation.dart';
import 'swt.dart';

part 'sashformdata.g.dart';

@JsonSerializable()
class SashFormDataValue {
  SashFormDataValue() : this.empty();
  SashFormDataValue.empty();

  factory SashFormDataValue.fromJson(Map<String, dynamic> json) =>
      _$SashFormDataValueFromJson(json);
  Map<String, dynamic> toJson() => _$SashFormDataValueToJson(this);
}
