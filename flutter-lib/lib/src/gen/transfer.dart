import 'package:json_annotation/json_annotation.dart';

part 'transfer.g.dart';

@JsonSerializable()
class VTransfer {
  VTransfer() : this.empty();
  VTransfer.empty();

  factory VTransfer.fromJson(Map<String, dynamic> json) =>
      _$VTransferFromJson(json);
  Map<String, dynamic> toJson() => _$VTransferToJson(this);
}
