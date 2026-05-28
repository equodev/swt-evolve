import 'package:json_annotation/json_annotation.dart';
import '../gen/dialog.dart';

part 'messagebox.g.dart';

@JsonSerializable()
class VMessageBox extends VDialog {
  VMessageBox() : this.empty();
  VMessageBox.empty() : super.empty();

  String? message;

  factory VMessageBox.fromJson(Map<String, dynamic> json) =>
      _$VMessageBoxFromJson(json);
  Map<String, dynamic> toJson() => _$VMessageBoxToJson(this);
}
