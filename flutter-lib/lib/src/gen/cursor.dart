import 'package:json_annotation/json_annotation.dart';
import '../gen/image.dart';

part 'cursor.g.dart';

@JsonSerializable()
class VCursor {
  VCursor() : this.empty();
  VCursor.empty();

  int? cursorStyle = null;
  VImage? image = null;

  factory VCursor.fromJson(Map<String, dynamic> json) =>
      _$VCursorFromJson(json);
  Map<String, dynamic> toJson() => _$VCursorToJson(this);
}
