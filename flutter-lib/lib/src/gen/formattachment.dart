import 'package:json_annotation/json_annotation.dart';
import '../gen/control.dart';

part 'formattachment.g.dart';

@JsonSerializable()
class VFormAttachment {
  VFormAttachment() : this.empty();
  VFormAttachment.empty();

  int? alignment;
  VControl? control;
  int? denominator;
  int? numerator;
  int? offset;

  factory VFormAttachment.fromJson(Map<String, dynamic> json) =>
      _$VFormAttachmentFromJson(json);
  Map<String, dynamic> toJson() => _$VFormAttachmentToJson(this);
}
