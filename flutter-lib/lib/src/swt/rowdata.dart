import 'package:json_annotation/json_annotation.dart';
import 'swt.dart';

part 'rowdata.g.dart';

@JsonSerializable()
class RowDataValue {
  RowDataValue() : this.empty();
  RowDataValue.empty();

  int width = 0;
  int height = 0;
  bool exclude = false;

  factory RowDataValue.fromJson(Map<String, dynamic> json) =>
      _$RowDataValueFromJson(json);
  Map<String, dynamic> toJson() => _$RowDataValueToJson(this);
}
