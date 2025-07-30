import 'package:json_annotation/json_annotation.dart';
import 'swt.dart';

part 'filldata.g.dart';

@JsonSerializable()
class FillDataValue {
  FillDataValue() : this.empty();
  FillDataValue.empty();

  factory FillDataValue.fromJson(Map<String, dynamic> json) =>
      _$FillDataValueFromJson(json);
  Map<String, dynamic> toJson() => _$FillDataValueToJson(this);
}
