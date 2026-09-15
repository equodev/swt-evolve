import 'package:json_annotation/json_annotation.dart';

part 'cursor.g.dart';

@JsonSerializable()
class VCursor {
  VCursor() : this.empty();
  VCursor.empty();

  int? cursorStyle;

  factory VCursor.fromJson(Map<String, dynamic> json) =>
      _$VCursorFromJson(json);
  Map<String, dynamic> toJson() => _$VCursorToJson(this);
}
