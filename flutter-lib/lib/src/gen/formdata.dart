import 'package:json_annotation/json_annotation.dart';

part 'formdata.g.dart';

@JsonSerializable()
class VFormData {
  VFormData() : this.empty();
  VFormData.empty();

  factory VFormData.fromJson(Map<String, dynamic> json) =>
      _$VFormDataFromJson(json);
  Map<String, dynamic> toJson() => _$VFormDataToJson(this);
}
