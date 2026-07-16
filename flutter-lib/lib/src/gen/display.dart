import 'package:json_annotation/json_annotation.dart';
import '../impl/config_flags.dart';
import 'menu.dart';
import 'shell.dart';
import 'tooltip.dart';

part 'display.g.dart';

@JsonSerializable()
class VDisplay {
  String? swt;
  int? id;
  List<VShell>? shells;
  List<VMenu>? popups;
  List<VToolTip>? tooltips;
  ConfigFlags? config;

  VDisplay();

  factory VDisplay.fromJson(Map<String, dynamic> json) =>
      _$VDisplayFromJson(json);

  Map<String, dynamic> toJson() => _$VDisplayToJson(this);
}
