import 'package:json_annotation/json_annotation.dart';

part 'formattachment.g.dart';

@JsonSerializable()
class VFormAttachment {
  VFormAttachment() : this.empty();
  VFormAttachment.empty();

  factory VFormAttachment.fromJson(Map<String, dynamic> json) =>
      _$VFormAttachmentFromJson(json);
  Map<String, dynamic> toJson() => _$VFormAttachmentToJson(this);
}
