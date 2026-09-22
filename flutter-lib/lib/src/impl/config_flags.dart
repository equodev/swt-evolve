import 'package:json_annotation/json_annotation.dart';
import 'decorations_align.dart';

part 'config_flags.g.dart';

@JsonSerializable()
class ConfigFlags {
  ConfigFlags();

  bool? ctabfolder_visible_controls;
  bool? ctabfolder_topright_auto_hide;
  bool? image_disable_icons_replacement;
  String? assets_path;
  bool? disable_evolve_icons;
  bool? gc_icons_replacement;
  bool? use_swt_colors;
  bool? disable_swt_canvas_colors;

  /// Drops what an application paints over a control through its GC, so only what the control draws
  /// for itself shows. Controls whose GC content is the control (Canvas, StyledText) ignore it.
  bool? disable_control_gc_overlay;
  bool? use_swt_fonts;
  String? theme_name;
  String? force_theme;
  String? theme_color;
  Map<String, String>? theme_colors_by_widget;
  bool? show_theme_color_palette;
  bool? focus_indicators;
  bool? use_special_dropdown_button;
  bool? preserve_icon_colors;
  bool? disable_hover_zoom;
  bool? custom_tooltip;
  bool? notification_popup;
  bool? show_scaling_control;

  /// The zoom SWT draws its UI at, in percent — `swt.autoScale` already applied to the monitor's
  /// own zoom. This side draws at the monitor's zoom, so the ratio between them is what the whole
  /// app has to be scaled by for both halves of a mixed tree to come out the same size.
  int? ui_zoom;
  @JsonKey(fromJson: DecorationsAlign.fromJson, toJson: DecorationsAlign.toJson)
  DecorationsAlign? decorations_align;
  bool? print_move;
  String? csd_placement;
  String? csd_os;
  String? csd_maximize;
  int? double_click_timeout_ms;

  /// Explicit CSD title-bar colour as RRGGBB/AARRGGBB hex. Overrides the theme's own
  /// title-bar colour; unset falls back to the theme (see CsdOverlayStrip).
  String? csd_titlebar_color;
  /// True when the OS owns the menu bar (macOS desktop) and this side must not draw one.
  bool? system_menu_bar;

  /// Logical pixels per SWT font point on the host the application runs on, as the Java side
  /// measured its text extents at. Not derivable here: this client's platform is the browser's.
  double? font_point_scale;

  factory ConfigFlags.fromJson(Map<String, dynamic> json) =>
      _$ConfigFlagsFromJson(json);
  Map<String, dynamic> toJson() => _$ConfigFlagsToJson(this);
}
