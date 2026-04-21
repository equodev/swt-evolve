import 'package:json_annotation/json_annotation.dart';
import '../gen/formattachment.dart';

part 'formdata.g.dart';

@JsonSerializable()
class VFormData {
  VFormData() : this.empty();
  VFormData.empty();

  VFormAttachment? bottom;
  int? height;
  VFormAttachment? left;
  VFormAttachment? right;
  VFormAttachment? top;
  int? width;

  factory VFormData.fromJson(Map<String, dynamic> json) =>
      _$VFormDataFromJson(json);
  Map<String, dynamic> toJson() => _$VFormDataToJson(this);
}
