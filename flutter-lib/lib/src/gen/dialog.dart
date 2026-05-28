import 'package:json_annotation/json_annotation.dart';

part 'dialog.g.dart';

@JsonSerializable()
class VDialog {
  VDialog() : this.empty();
  VDialog.empty();

  String swt = '';
  int id = 0;
  int? style;
  String? text;

  factory VDialog.fromJson(Map<String, dynamic> json) =>
      _$VDialogFromJson(json);
  Map<String, dynamic> toJson() => _$VDialogToJson(this);
}
