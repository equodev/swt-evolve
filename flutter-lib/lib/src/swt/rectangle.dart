import 'package:json_annotation/json_annotation.dart';
import 'swt.dart';

part 'rectangle.g.dart';

@JsonSerializable()
class RectangleValue {
  RectangleValue() : this.empty();
  RectangleValue.empty();

  int x = 0;
  int y = 0;
  int width = 0;
  int height = 0;

  factory RectangleValue.fromJson(Map<String, dynamic> json) =>
      _$RectangleValueFromJson(json);
  Map<String, dynamic> toJson() => _$RectangleValueToJson(this);
}
