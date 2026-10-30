import 'dart:typed_data';

import 'decorations_align.dart';
import '../comm/widget_wire.dart';
import '../gen/widgets.dart' show WireKey;

class ConfigFlags implements WireReadable {
  ConfigFlags();

  /// The bytes these flags were read from, or null for flags made here: the flags repeat in every
  /// display record, and this is what tells one that changed nothing apart.
  Uint8List? wire;

  bool? ctabfolder_visible_controls;
  bool? ctabfolder_topright_auto_hide;
  bool? ctabfolder_tab_scroll;
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

  /// Text in the foreground the application set rather than the theme's. Null follows
  /// [use_swt_fonts]; see `useSwtFontColors`.
  bool? use_swt_font_colors;

  /// [use_swt_font_colors] per lower-case widget name, over the global value.
  Map<String, bool>? use_swt_font_colors_by_widget;
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
  DecorationsAlign? decorations_align;
  bool? print_move;
  String? csd_placement;
  String? csd_os;
  String? csd_maximize;
  int? double_click_timeout_ms;
  int? hover_delay_ms;

  /// Explicit CSD title-bar colour as RRGGBB/AARRGGBB hex. Overrides the theme's own
  /// title-bar colour; unset falls back to the theme (see CsdOverlayStrip).
  String? csd_titlebar_color;
  /// True when the OS owns the menu bar (macOS desktop) and this side must not draw one.
  bool? system_menu_bar;

  /// The decimal separator of the JVM's locale, which a desktop client writes numbers with.
  String? decimal_separator;

  /// The JVM's locale as a language tag, the user's locale on desktop.
  String? locale;

  /// Logical pixels per SWT font point on the host the application runs on, as the Java side
  /// measured its text extents at. Not derivable here: this client's platform is the browser's.
  double? font_point_scale;

  /// Transparent margin on each side of a bezelled push or toggle button, configured on the Java
  /// side (`swt.evolve.push_button_margin`). 0 paints the button over its whole bounds.
  int? push_button_margin;

  /// CHECK and RADIO menu items draw no box or ring, only the mark while selected
  /// (`swt.evolve.transparent_menu_check_box`). Off by default: the theme's box is drawn.
  bool? transparent_menu_check_box;

  /// A bordered CTabFolder draws the native renderer's outline, joined over the selected tab
  /// (`swt.use_swt_border`). Off by default: the theme's flat frame is drawn.
  bool? use_swt_border;

  /// The flags as an object of a binary frame, read field by field.
  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_ConfigFlags_ctabfolder_visible_controls:
        ctabfolder_visible_controls = r.boolean();
      case WireKey.k_ConfigFlags_ctabfolder_topright_auto_hide:
        ctabfolder_topright_auto_hide = r.boolean();
      case WireKey.k_ConfigFlags_image_disable_icons_replacement:
        image_disable_icons_replacement = r.boolean();
      case WireKey.k_ConfigFlags_assets_path:
        assets_path = r.string();
      case WireKey.k_ConfigFlags_disable_evolve_icons:
        disable_evolve_icons = r.boolean();
      case WireKey.k_ConfigFlags_gc_icons_replacement:
        gc_icons_replacement = r.boolean();
      case WireKey.k_ConfigFlags_use_swt_colors:
        use_swt_colors = r.boolean();
      case WireKey.k_ConfigFlags_disable_swt_canvas_colors:
        disable_swt_canvas_colors = r.boolean();
      case WireKey.k_ConfigFlags_disable_control_gc_overlay:
        disable_control_gc_overlay = r.boolean();
      case WireKey.k_ConfigFlags_use_swt_fonts:
        use_swt_fonts = r.boolean();
      case WireKey.k_ConfigFlags_use_swt_font_colors:
        use_swt_font_colors = r.boolBoxed();
      case WireKey.k_ConfigFlags_use_swt_font_colors_by_widget:
        use_swt_font_colors_by_widget = r.boolMap();
      case WireKey.k_ConfigFlags_theme_name:
        theme_name = r.string();
      case WireKey.k_ConfigFlags_force_theme:
        force_theme = r.string();
      case WireKey.k_ConfigFlags_theme_color:
        theme_color = r.string();
      case WireKey.k_ConfigFlags_theme_colors_by_widget:
        theme_colors_by_widget = r.stringMap();
      case WireKey.k_ConfigFlags_show_theme_color_palette:
        show_theme_color_palette = r.boolean();
      case WireKey.k_ConfigFlags_focus_indicators:
        focus_indicators = r.boolBoxed();
      case WireKey.k_ConfigFlags_use_special_dropdown_button:
        use_special_dropdown_button = r.boolean();
      case WireKey.k_ConfigFlags_preserve_icon_colors:
        preserve_icon_colors = r.boolean();
      case WireKey.k_ConfigFlags_disable_hover_zoom:
        disable_hover_zoom = r.boolean();
      case WireKey.k_ConfigFlags_custom_tooltip:
        custom_tooltip = r.boolean();
      case WireKey.k_ConfigFlags_notification_popup:
        notification_popup = r.boolean();
      case WireKey.k_ConfigFlags_show_scaling_control:
        show_scaling_control = r.boolean();
      case WireKey.k_ConfigFlags_ui_zoom:
        ui_zoom = r.int32();
      case WireKey.k_ConfigFlags_decorations_align:
        final align = r.string();
        decorations_align = align == null ? null : DecorationsAlign.fromString(align);
      case WireKey.k_ConfigFlags_print_move:
        print_move = r.boolean();
      case WireKey.k_ConfigFlags_csd_placement:
        csd_placement = r.string();
      case WireKey.k_ConfigFlags_csd_os:
        csd_os = r.string();
      case WireKey.k_ConfigFlags_csd_maximize:
        csd_maximize = r.string();
      case WireKey.k_ConfigFlags_double_click_timeout_ms:
        double_click_timeout_ms = r.int32();
      case WireKey.k_ConfigFlags_csd_titlebar_color:
        csd_titlebar_color = r.string();
      case WireKey.k_ConfigFlags_system_menu_bar:
        system_menu_bar = r.boolean();
      case WireKey.k_ConfigFlags_font_point_scale:
        font_point_scale = r.float64();
      case WireKey.k_ConfigFlags_push_button_margin:
        push_button_margin = r.int32();
      case WireKey.k_ConfigFlags_hover_delay_ms:
        hover_delay_ms = r.int32();
      case WireKey.k_ConfigFlags_ctabfolder_tab_scroll:
        ctabfolder_tab_scroll = r.boolean();
      case WireKey.k_ConfigFlags_decimal_separator:
        decimal_separator = r.string();
      case WireKey.k_ConfigFlags_locale:
        locale = r.string();
      case WireKey.k_ConfigFlags_transparent_menu_check_box:
        transparent_menu_check_box = r.boolean();
      case WireKey.k_ConfigFlags_use_swt_border:
        use_swt_border = r.boolean();
      default:
        return false;
    }
    return true;
  }

  /// Flags read from [bytes], a flags object's fields, keeping them as [wire].
  static ConfigFlags read(Uint8List bytes) =>
      WidgetWireReader(bytes).fields(ConfigFlags())..wire = Uint8List.fromList(bytes);
}
