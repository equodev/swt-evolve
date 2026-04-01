import 'package:json_annotation/json_annotation.dart';

part 'config_flags.g.dart';

@JsonSerializable()
class ConfigFlags {
  ConfigFlags();

  bool? ctabfolder_visible_controls;
  bool? image_disable_icons_replacement;
  String? assets_path;
  bool? use_swt_colors;
  bool? use_swt_fonts;
  String? theme_name;
  String? force_theme;
  String? theme_color;
  Map<String, String>? theme_colors_by_widget;
  bool? show_theme_color_palette;
  bool? use_special_dropdown_button;
  bool? preserve_icon_colors;

  factory ConfigFlags.fromJson(Map<String, dynamic> json) =>
      _$ConfigFlagsFromJson(json);
  Map<String, dynamic> toJson() => _$ConfigFlagsToJson(this);
}
