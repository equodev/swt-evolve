import 'package:json_annotation/json_annotation.dart';

part 'config_flags.g.dart';

@JsonSerializable()
class ConfigFlags {
  ConfigFlags();

  bool? ctabfolder_visible_controls;
  bool? image_disable_icons_replacement;

  factory ConfigFlags.fromJson(Map<String, dynamic> json) =>
      _$ConfigFlagsFromJson(json);
  Map<String, dynamic> toJson() => _$ConfigFlagsToJson(this);
}
