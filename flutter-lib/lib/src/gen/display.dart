import 'package:json_annotation/json_annotation.dart';
import 'menu.dart';
import 'shell.dart';

part 'display.g.dart';

@JsonSerializable()
class VDisplay {
  String? swt;
  int? id;
  List<VShell>? shells;
  List<VMenu>? popups;

  VDisplay();

  factory VDisplay.fromJson(Map<String, dynamic> json) =>
      _$VDisplayFromJson(json);

  Map<String, dynamic> toJson() => _$VDisplayToJson(this);
}
