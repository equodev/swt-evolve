import 'package:flutter/widgets.dart';
import '../comm/v_registry.dart';
import '../gen/animatedprogress.dart';
import '../gen/browser.dart';
import '../gen/button.dart';
import '../gen/canvas.dart';
import '../gen/caret.dart';
import '../gen/cbanner.dart';
import '../gen/ccombo.dart';
import '../gen/clabel.dart';
import '../gen/combo.dart';
import '../gen/composite.dart';
import '../gen/controleditor.dart';
import '../gen/coolbar.dart';
import '../gen/coolitem.dart';
import '../gen/ctabfolder.dart';
import '../gen/ctabitem.dart';
import '../gen/datetime.dart';
import '../gen/decorations.dart';
import '../gen/dragsource.dart';
import '../gen/droptarget.dart';
import '../gen/expandbar.dart';
import '../gen/expanditem.dart';
import '../gen/gc.dart';
import '../gen/glcanvas.dart';
import '../gen/group.dart';
import '../gen/ime.dart';
import '../gen/label.dart';
import '../gen/link.dart';
import '../gen/list.dart';
import '../gen/menu.dart';
import '../gen/menuitem.dart';
import '../gen/progressbar.dart';
import '../gen/sash.dart';
import '../gen/sashform.dart';
import '../gen/scale.dart';
import '../gen/scrollbar.dart';
import '../gen/scrolledcomposite.dart';
import '../gen/shell.dart';
import '../gen/slider.dart';
import '../gen/spinner.dart';
import '../gen/styledtext.dart';
import '../gen/tabfolder.dart';
import '../gen/tabitem.dart';
import '../gen/table.dart';
import '../gen/tablecolumn.dart';
import '../gen/tablecursor.dart';
import '../gen/tableeditor.dart';
import '../gen/tableitem.dart';
import '../gen/taskbar.dart';
import '../gen/taskitem.dart';
import '../gen/text.dart';
import '../gen/toolbar.dart';
import '../gen/toolitem.dart';
import '../gen/tooltip.dart';
import '../gen/tracker.dart';
import '../gen/tray.dart';
import '../gen/trayitem.dart';
import '../gen/tree.dart';
import '../gen/treecolumn.dart';
import '../gen/treecursor.dart';
import '../gen/treeeditor.dart';
import '../gen/treeitem.dart';
import '../gen/viewform.dart';
import '../gen/widget.dart';

Widget mapWidgetFromValue(VWidget child) {
  // Nothing to render for a widget that has only been named. Until the request the registry
  // already sent is answered, its value is an identity stub - no bounds, no content - and
  // building from it paints an empty widget that is replaced a frame later. Beyond the
  // flicker: rendering the same widget in two shapes reshapes the semantics tree, and
  // Flutter Web rebuilds the DOM element of a node whose engine role changes while
  // re-applying only the properties that changed with it. The identifier did not change,
  // so it is never written again and the widget loses its test tag for good.
  if (VRegistry.instance.isAwaitingAt(VRegistry.keyOf(child))) {
    return const SizedBox.shrink();
  }
  var type = child.swt;
  var id = child.id;
  // The same value gets the widget made for it before, so a parent's rebuild leaves the
  // child alone: a child takes its state from the registry, never from its parent.
  final made = _made[child];
  if (made != null && made.$1 == id) return made.$2;
  final widget = switch (child) {
    VShell() => ShellSwt(key: ValueKey(id), value: child),
    VAnimatedProgress() => AnimatedProgressSwt(key: ValueKey(id), value: child),
    VCLabel() => CLabelSwt(key: ValueKey(id), value: child),
    VDecorations() => DecorationsSwt(key: ValueKey(id), value: child),
    VGLCanvas() => GLCanvasSwt(key: ValueKey(id), value: child),
    VStyledText() => StyledTextSwt(key: ValueKey(id), value: child),
    VTableCursor() => TableCursorSwt(key: ValueKey(id), value: child),
    VTreeCursor() => TreeCursorSwt(key: ValueKey(id), value: child),
    VBrowser() => BrowserSwt(key: GlobalObjectKey(id), value: child),
    VCBanner() => CBannerSwt(key: ValueKey(id), value: child),
    VCCombo() => CComboSwt(key: ValueKey(id), value: child),
    VCTabFolder() => CTabFolderSwt(key: ValueKey(id), value: child),
    VCanvas() => CanvasSwt(key: ValueKey(id), value: child),
    VCombo() => ComboSwt(key: ValueKey(id), value: child),
    VCoolBar() => CoolBarSwt(key: ValueKey(id), value: child),
    VDateTime() => DateTimeSwt(key: ValueKey(id), value: child),
    VExpandBar() => ExpandBarSwt(key: ValueKey(id), value: child),
    VGroup() => GroupSwt(key: ValueKey(id), value: child),
    VSashForm() => SashFormSwt(key: ValueKey(id), value: child),
    VScrolledComposite() => ScrolledCompositeSwt(
      key: ValueKey(id),
      value: child,
    ),
    VSpinner() => SpinnerSwt(key: ValueKey(id), value: child),
    VTabFolder() => TabFolderSwt(key: ValueKey(id), value: child),
    VTable() => TableSwt(key: ValueKey(id), value: child),
    VToolBar() => ToolBarSwt(key: ValueKey(id), value: child),
    VTree() => TreeSwt(key: ValueKey(id), value: child),
    VViewForm() => ViewFormSwt(key: ValueKey(id), value: child),
    VComposite() => CompositeSwt(key: ValueKey(id), value: child),
    VList() => ListSwt(key: ValueKey(id), value: child),
    VText() => TextSwt(key: ValueKey(id), value: child),
    VButton() => ButtonSwt(key: ValueKey(id), value: child),
    VLabel() => LabelSwt(key: ValueKey(id), value: child),
    VLink() => LinkSwt(key: ValueKey(id), value: child),
    VProgressBar() => ProgressBarSwt(key: ValueKey(id), value: child),
    VSash() => SashSwt(key: ValueKey(id), value: child),
    VScale() => ScaleSwt(key: ValueKey(id), value: child),
    VSlider() => SliderSwt(key: ValueKey(id), value: child),
    VCTabItem() => CTabItemSwt(key: ValueKey(id), value: child),
    VCoolItem() => CoolItemSwt(key: ValueKey(id), value: child),
    VExpandItem() => ExpandItemSwt(key: ValueKey(id), value: child),
    VMenuItem() => MenuItemSwt(key: ValueKey(id), value: child),
    VTabItem() => TabItemSwt(key: ValueKey(id), value: child),
    VTableColumn() => TableColumnSwt(key: ValueKey(id), value: child),
    VTableItem() => TableItemSwt(key: ValueKey(id), value: child),
    VTaskItem() => TaskItemSwt(key: ValueKey(id), value: child),
    VToolItem() => ToolItemSwt(key: ValueKey(id), value: child),
    VTrayItem() => TrayItemSwt(key: ValueKey(id), value: child),
    VTreeColumn() => TreeColumnSwt(key: ValueKey(id), value: child),
    VTreeItem() => TreeItemSwt(key: ValueKey(id), value: child),
    VCaret() => CaretSwt(key: ValueKey(id), value: child),
    VDragSource() => DragSourceSwt(key: ValueKey(id), value: child),
    VDropTarget() => DropTargetSwt(key: ValueKey(id), value: child),
    VGC() => GCSwt(key: ValueKey(id), value: child),
    VIME() => IMESwt(key: ValueKey(id), value: child),
    VMenu() => MenuSwt(key: ValueKey(id), value: child),
    VScrollBar() => ScrollBarSwt(key: ValueKey(id), value: child),
    VTableEditor() => TableEditorSwt(key: ValueKey(id), value: child),
    VTaskBar() => TaskBarSwt(key: ValueKey(id), value: child),
    VToolTip() => ToolTipSwt(key: ValueKey(id), value: child),
    VTracker() => TrackerSwt(key: ValueKey(id), value: child),
    VTray() => TraySwt(key: ValueKey(id), value: child),
    VTreeEditor() => TreeEditorSwt(key: ValueKey(id), value: child),
    VControlEditor() => ControlEditorSwt(key: ValueKey(id), value: child),
    _ => throw "No widget for Value $type",
  };
  _made[child] = (id, widget);
  return widget;
}

final Expando<(int, Widget)> _made = Expando();

/// The widget of type [swt] numbered [id], drawn from what the registry holds for it.
Widget mapWidget(String swt, int id) {
  final value = newWidgetValue(swt);
  if (value == null) throw "Unknown Widget Value $swt";
  return mapWidgetFromValue(value..id = id);
}

/// The table's fingerprint, which Java compares with its own when this client connects.
const int wireFingerprint = 474132839;

/// Property numbers, for code that reads a frame by hand.
abstract final class WireKey {
  static const int k_Accessible_control = 64;
  static const int k_Accessible_focus = 65;
  static const int k_BorderData_hHint = 128;
  static const int k_BorderData_region = 129;
  static const int k_BorderData_wHint = 130;
  static const int k_Browser_functionNames = 192;
  static const int k_Browser_javascriptEnabled = 193;
  static const int k_Browser_text = 194;
  static const int k_Browser_url = 195;
  static const int k_BrowserFunction_browser = 256;
  static const int k_BrowserFunction_name = 257;
  static const int k_Button_alignment = 320;
  static const int k_Button_grayed = 321;
  static const int k_Button_image = 322;
  static const int k_Button_primary = 323;
  static const int k_Button_selection = 324;
  static const int k_Button_text = 325;
  static const int k_CBanner_bottomLine = 6272;
  static const int k_CBanner_curveLine = 6273;
  static const int k_CCombo_alignment = 384;
  static const int k_CCombo_editable = 391;
  static const int k_CCombo_itemTooltips = 385;
  static const int k_CCombo_items = 386;
  static const int k_CCombo_listVisible = 387;
  static const int k_CCombo_selection = 388;
  static const int k_CCombo_text = 389;
  static const int k_CCombo_textLimit = 390;
  static const int k_CLabel_alignment = 448;
  static const int k_CLabel_backgroundImage = 449;
  static const int k_CLabel_bottomMargin = 450;
  static const int k_CLabel_gradientColors = 451;
  static const int k_CLabel_gradientPercents = 452;
  static const int k_CLabel_gradientVertical = 453;
  static const int k_CLabel_image = 454;
  static const int k_CLabel_leftMargin = 455;
  static const int k_CLabel_rightMargin = 456;
  static const int k_CLabel_text = 457;
  static const int k_CLabel_topMargin = 458;
  static const int k_CTabFolder_borderVisible = 512;
  static const int k_CTabFolder_chevronVisible = 513;
  static const int k_CTabFolder_dirtyIndicatorStyle = 538;
  static const int k_CTabFolder_highlight = 514;
  static const int k_CTabFolder_highlightEnabled = 515;
  static const int k_CTabFolder_items = 516;
  static const int k_CTabFolder_maximizeVisible = 517;
  static const int k_CTabFolder_maximized = 518;
  static const int k_CTabFolder_minimizeVisible = 519;
  static const int k_CTabFolder_minimized = 520;
  static const int k_CTabFolder_minimumCharacters = 521;
  static const int k_CTabFolder_selectedImageVisible = 522;
  static const int k_CTabFolder_selection = 523;
  static const int k_CTabFolder_selectionBackground = 524;
  static const int k_CTabFolder_selectionBarThickness = 525;
  static const int k_CTabFolder_selectionBgImage = 526;
  static const int k_CTabFolder_selectionForeground = 527;
  static const int k_CTabFolder_showChevron = 528;
  static const int k_CTabFolder_showListPopupSeq = 529;
  static const int k_CTabFolder_single = 530;
  static const int k_CTabFolder_stripLayoutSeq = 537;
  static const int k_CTabFolder_tabHeight = 531;
  static const int k_CTabFolder_tabPosition = 532;
  static const int k_CTabFolder_topRight = 533;
  static const int k_CTabFolder_topRightAlignment = 534;
  static const int k_CTabFolder_unselectedCloseVisible = 535;
  static const int k_CTabFolder_unselectedImageVisible = 536;
  static const int k_CTabItem_control = 576;
  static const int k_CTabItem_font = 577;
  static const int k_CTabItem_foreground = 578;
  static const int k_CTabItem_selectionForeground = 579;
  static const int k_CTabItem_showClose = 580;
  static const int k_CTabItem_showDirty = 583;
  static const int k_CTabItem_showing = 581;
  static const int k_CTabItem_toolTipText = 582;
  static const int k_Canvas_caret = 640;
  static const int k_Caret_blinkRate = 704;
  static const int k_Caret_bounds = 705;
  static const int k_Caret_focused = 706;
  static const int k_Caret_image = 707;
  static const int k_Caret_visible = 708;
  static const int k_Clipboard_availableTypeNames = 768;
  static const int k_Color_a = 832;
  static const int k_Color_b = 833;
  static const int k_Color_g = 834;
  static const int k_Color_r = 835;
  static const int k_ColorDialog_RGB = 896;
  static const int k_ColorDialog_RGBs = 897;
  static const int k_Combo_caretPosition = 960;
  static const int k_Combo_items = 961;
  static const int k_Combo_listVisible = 962;
  static const int k_Combo_text = 963;
  static const int k_Combo_textLimit = 964;
  static const int k_Composite_backgroundMode = 1024;
  static const int k_Composite_children = 1025;
  static const int k_ConfigFlags_assets_path = 1088;
  static const int k_ConfigFlags_bigger_icon_size = 1129;
  static const int k_ConfigFlags_csd_maximize = 1089;
  static const int k_ConfigFlags_csd_os = 1090;
  static const int k_ConfigFlags_csd_placement = 1091;
  static const int k_ConfigFlags_csd_titlebar_color = 1092;
  static const int k_ConfigFlags_ctabfolder_chevron_after_tabs = 1131;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 1093;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 1094;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 1095;
  static const int k_ConfigFlags_custom_tooltip = 1096;
  static const int k_ConfigFlags_decimal_separator = 1097;
  static const int k_ConfigFlags_decorations_align = 1098;
  static const int k_ConfigFlags_disable_control_gc_overlay = 1099;
  static const int k_ConfigFlags_disable_evolve_icons = 1100;
  static const int k_ConfigFlags_disable_hover_zoom = 1101;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 1102;
  static const int k_ConfigFlags_double_click_timeout_ms = 1103;
  static const int k_ConfigFlags_focus_indicators = 1104;
  static const int k_ConfigFlags_font_point_scale = 1105;
  static const int k_ConfigFlags_force_theme = 1106;
  static const int k_ConfigFlags_gc_icons_replacement = 1107;
  static const int k_ConfigFlags_hover_delay_ms = 1108;
  static const int k_ConfigFlags_image_disable_icons_replacement = 1109;
  static const int k_ConfigFlags_locale = 1110;
  static const int k_ConfigFlags_notification_popup = 1111;
  static const int k_ConfigFlags_preserve_icon_colors = 1112;
  static const int k_ConfigFlags_print_move = 1113;
  static const int k_ConfigFlags_push_button_margin = 1114;
  static const int k_ConfigFlags_regional_locale = 1132;
  static const int k_ConfigFlags_scale_vertical_minimum_at_top = 1130;
  static const int k_ConfigFlags_show_scaling_control = 1115;
  static const int k_ConfigFlags_show_theme_color_palette = 1116;
  static const int k_ConfigFlags_system_menu_bar = 1117;
  static const int k_ConfigFlags_theme_color = 1118;
  static const int k_ConfigFlags_theme_colors_by_widget = 1119;
  static const int k_ConfigFlags_theme_name = 1120;
  static const int k_ConfigFlags_transparent_menu_check_box = 1121;
  static const int k_ConfigFlags_ui_zoom = 1122;
  static const int k_ConfigFlags_use_special_dropdown_button = 1123;
  static const int k_ConfigFlags_use_swt_border = 1124;
  static const int k_ConfigFlags_use_swt_colors = 1125;
  static const int k_ConfigFlags_use_swt_font_colors = 1126;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 1127;
  static const int k_ConfigFlags_use_swt_fonts = 1128;
  static const int k_Control_background = 1152;
  static const int k_Control_backgroundImage = 1153;
  static const int k_Control_bounds = 1154;
  static const int k_Control_cursor = 1155;
  static const int k_Control_dragDetect = 1156;
  static const int k_Control_dragSource = 1157;
  static const int k_Control_dragSourceId = 1158;
  static const int k_Control_dropTargetId = 1159;
  static const int k_Control_enabled = 1160;
  static const int k_Control_font = 1161;
  static const int k_Control_foreground = 1162;
  static const int k_Control_hasOwnBackground = 1163;
  static const int k_Control_inheritsBackground = 1164;
  static const int k_Control_menu = 1165;
  static const int k_Control_region = 1166;
  static const int k_Control_tabStop = 1167;
  static const int k_Control_toolTipText = 1168;
  static const int k_Control_visible = 1169;
  static const int k_ControlEditor_editor = 1216;
  static const int k_CoolBar_itemOrder = 1280;
  static const int k_CoolBar_itemSizes = 1281;
  static const int k_CoolBar_items = 1282;
  static const int k_CoolBar_wrapIndices = 1283;
  static const int k_CoolItem_control = 1344;
  static const int k_CoolItem_preferredSize = 1345;
  static const int k_Cursor_cursorStyle = 1408;
  static const int k_Cursor_hotspotX = 1409;
  static const int k_Cursor_hotspotY = 1410;
  static const int k_Cursor_image = 1411;
  static const int k_DateTime_day = 1472;
  static const int k_DateTime_hours = 1473;
  static const int k_DateTime_minutes = 1474;
  static const int k_DateTime_month = 1475;
  static const int k_DateTime_seconds = 1476;
  static const int k_DateTime_year = 1477;
  static const int k_Decorations_menuBar = 1536;
  static const int k_Decorations_menuStripHeight = 1537;
  static const int k_Decorations_ownMenuStrip = 1538;
  static const int k_Decorations_text = 1539;
  static const int k_Device_fontList = 1600;
  static const int k_Device_tracking = 1601;
  static const int k_Device_warnings = 1602;
  static const int k_Dialog_id = 1664;
  static const int k_Dialog_style = 1665;
  static const int k_Dialog_swt = 1666;
  static const int k_Dialog_text = 1667;
  static const int k_DirectoryDialog_filterPath = 1728;
  static const int k_DirectoryDialog_message = 1729;
  static const int k_Display_activeShellId = 1792;
  static const int k_Display_config = 1793;
  static const int k_Display_focusHolderId = 1794;
  static const int k_Display_mainShellId = 1795;
  static const int k_Display_popups = 1796;
  static const int k_Display_shells = 1797;
  static const int k_Display_systemMenu = 1798;
  static const int k_Display_tooltips = 1799;
  static const int k_Display_windowedShellIds = 1800;
  static const int k_Event_button = 1856;
  static const int k_Event_character = 1857;
  static const int k_Event_count = 1858;
  static const int k_Event_currentDataTypeId = 1859;
  static const int k_Event_detail = 1860;
  static const int k_Event_doit = 1861;
  static const int k_Event_dragImage = 1862;
  static const int k_Event_end = 1863;
  static const int k_Event_feedback = 1864;
  static const int k_Event_gc = 1865;
  static const int k_Event_height = 1866;
  static const int k_Event_index = 1867;
  static const int k_Event_item = 1868;
  static const int k_Event_itemId = 1869;
  static const int k_Event_keyCode = 1870;
  static const int k_Event_keyLocation = 1871;
  static const int k_Event_magnification = 1872;
  static const int k_Event_operations = 1873;
  static const int k_Event_rotation = 1874;
  static const int k_Event_segments = 1875;
  static const int k_Event_segmentsChars = 1876;
  static const int k_Event_start = 1877;
  static const int k_Event_stateMask = 1878;
  static const int k_Event_text = 1879;
  static const int k_Event_time = 1880;
  static const int k_Event_touches = 1881;
  static const int k_Event_type = 1882;
  static const int k_Event_widget = 1883;
  static const int k_Event_width = 1884;
  static const int k_Event_x = 1885;
  static const int k_Event_xDirection = 1886;
  static const int k_Event_y = 1887;
  static const int k_Event_yDirection = 1888;
  static const int k_ExpandBar_items = 1920;
  static const int k_ExpandBar_spacing = 1921;
  static const int k_ExpandItem_control = 1984;
  static const int k_ExpandItem_expanded = 1985;
  static const int k_ExpandItem_headerHeight = 1986;
  static const int k_ExpandItem_height = 1987;
  static const int k_FileDialog_fileName = 2048;
  static const int k_FileDialog_fileNames = 2049;
  static const int k_FileDialog_filterExtensions = 2050;
  static const int k_FileDialog_filterIndex = 2051;
  static const int k_FileDialog_filterNames = 2052;
  static const int k_FileDialog_filterPath = 2053;
  static const int k_FileDialog_overwrite = 2054;
  static const int k_Font_fontData = 2112;
  static const int k_FontData_height = 2176;
  static const int k_FontData_name = 2177;
  static const int k_FontData_style = 2178;
  static const int k_FontDialog_RGB = 2240;
  static const int k_FontDialog_effectsVisible = 2241;
  static const int k_FontDialog_fontData = 2242;
  static const int k_FontDialog_fontList = 2243;
  static const int k_FormAttachment_alignment = 2304;
  static const int k_FormAttachment_control = 2305;
  static const int k_FormAttachment_denominator = 2306;
  static const int k_FormAttachment_numerator = 2307;
  static const int k_FormAttachment_offset = 2308;
  static const int k_FormData_bottom = 2368;
  static const int k_FormData_cacheBottom = 2369;
  static const int k_FormData_cacheHeight = 2370;
  static const int k_FormData_cacheLeft = 2371;
  static const int k_FormData_cacheRight = 2372;
  static const int k_FormData_cacheTop = 2373;
  static const int k_FormData_cacheWidth = 2374;
  static const int k_FormData_currentHeight = 2375;
  static const int k_FormData_currentHhint = 2376;
  static const int k_FormData_currentWhint = 2377;
  static const int k_FormData_currentWidth = 2378;
  static const int k_FormData_defaultHeight = 2379;
  static const int k_FormData_defaultHhint = 2380;
  static const int k_FormData_defaultWhint = 2381;
  static const int k_FormData_defaultWidth = 2382;
  static const int k_FormData_height = 2383;
  static const int k_FormData_isVisited = 2384;
  static const int k_FormData_left = 2385;
  static const int k_FormData_needed = 2386;
  static const int k_FormData_right = 2387;
  static const int k_FormData_top = 2388;
  static const int k_FormData_width = 2389;
  static const int k_GC_XORMode = 2432;
  static const int k_GC_alpha = 2433;
  static const int k_GC_antialias = 2434;
  static const int k_GC_background = 2435;
  static const int k_GC_backgroundPattern = 2436;
  static const int k_GC_bufferScale = 2437;
  static const int k_GC_bufferScaleY = 2458;
  static const int k_GC_clipping = 2438;
  static const int k_GC_clippingPath = 2439;
  static const int k_GC_clippingRects = 2440;
  static const int k_GC_clippingText = 2441;
  static const int k_GC_fillRule = 2442;
  static const int k_GC_font = 2443;
  static const int k_GC_foreground = 2444;
  static const int k_GC_foregroundPattern = 2445;
  static const int k_GC_hoverFills = 2446;
  static const int k_GC_interpolation = 2447;
  static const int k_GC_lineCap = 2448;
  static const int k_GC_lineDash = 2449;
  static const int k_GC_lineDashOffset = 2450;
  static const int k_GC_lineJoin = 2451;
  static const int k_GC_lineStyle = 2452;
  static const int k_GC_lineWidth = 2453;
  static const int k_GC_selectionFills = 2454;
  static const int k_GC_style = 2455;
  static const int k_GC_themedCanvas = 2456;
  static const int k_GC_transform = 2457;
  static const int k_GLData_accumAlphaSize = 2496;
  static const int k_GLData_accumBlueSize = 2497;
  static const int k_GLData_accumGreenSize = 2498;
  static const int k_GLData_accumRedSize = 2499;
  static const int k_GLData_alphaSize = 2500;
  static const int k_GLData_blueSize = 2501;
  static const int k_GLData_depthSize = 2502;
  static const int k_GLData_doubleBuffer = 2503;
  static const int k_GLData_greenSize = 2504;
  static const int k_GLData_redSize = 2505;
  static const int k_GLData_sampleBuffers = 2506;
  static const int k_GLData_samples = 2507;
  static const int k_GLData_shareContext = 2508;
  static const int k_GLData_stencilSize = 2509;
  static const int k_GLData_stereo = 2510;
  static const int k_GlyphMetrics_ascent = 2560;
  static const int k_GlyphMetrics_descent = 2561;
  static const int k_GlyphMetrics_width = 2562;
  static const int k_GridData_cacheHeight = 2624;
  static const int k_GridData_cacheWidth = 2625;
  static const int k_GridData_currentHeight = 2626;
  static const int k_GridData_currentHhint = 2627;
  static const int k_GridData_currentWhint = 2628;
  static const int k_GridData_currentWidth = 2629;
  static const int k_GridData_defaultHeight = 2630;
  static const int k_GridData_defaultHhint = 2631;
  static const int k_GridData_defaultWhint = 2632;
  static const int k_GridData_defaultWidth = 2633;
  static const int k_GridData_exclude = 2634;
  static const int k_GridData_grabExcessHorizontalSpace = 2635;
  static const int k_GridData_grabExcessVerticalSpace = 2636;
  static const int k_GridData_heightHint = 2637;
  static const int k_GridData_horizontalAlignment = 2638;
  static const int k_GridData_horizontalIndent = 2639;
  static const int k_GridData_horizontalSpan = 2640;
  static const int k_GridData_minimumHeight = 2641;
  static const int k_GridData_minimumWidth = 2642;
  static const int k_GridData_verticalAlignment = 2643;
  static const int k_GridData_verticalIndent = 2644;
  static const int k_GridData_verticalSpan = 2645;
  static const int k_GridData_widthHint = 2646;
  static const int k_Group_text = 2688;
  static const int k_Image_filename = 2752;
  static const int k_Image_height = 2753;
  static const int k_Image_imageData = 2754;
  static const int k_Image_remoteRef = 2755;
  static const int k_Image_svgContent = 2756;
  static const int k_Image_width = 2757;
  static const int k_ImageData_data = 2816;
  static const int k_ImageData_depth = 2817;
  static const int k_ImageData_height = 2818;
  static const int k_ImageData_width = 2819;
  static const int k_Item_image = 2880;
  static const int k_Item_text = 2881;
  static const int k_Label_image = 2944;
  static const int k_Label_text = 2945;
  static const int k_LineInfo_alignment = 3008;
  static const int k_LineInfo_background = 3009;
  static const int k_LineInfo_flags = 3010;
  static const int k_LineInfo_indent = 3011;
  static const int k_LineInfo_justify = 3012;
  static const int k_LineInfo_segments = 3013;
  static const int k_LineInfo_segmentsChars = 3014;
  static const int k_LineInfo_tabStops = 3015;
  static const int k_LineInfo_verticalIndent = 3016;
  static const int k_LineInfo_wrapIndent = 3017;
  static const int k_LineSizeInfo_height = 3072;
  static const int k_LineSizeInfo_width = 3073;
  static const int k_Link_linkForeground = 3136;
  static const int k_Link_text = 3137;
  static const int k_List_items = 3200;
  static const int k_List_selection = 3201;
  static const int k_List_topIndex = 3202;
  static const int k_Menu_enabled = 3264;
  static const int k_Menu_items = 3265;
  static const int k_Menu_location = 3266;
  static const int k_Menu_orientation = 3267;
  static const int k_Menu_visible = 3268;
  static const int k_MenuItem_accelerator = 3328;
  static const int k_MenuItem_enabled = 3329;
  static const int k_MenuItem_menu = 3330;
  static const int k_MenuItem_selection = 3331;
  static const int k_MenuItem_toolTipText = 3332;
  static const int k_MessageBox_buttonLabels = 3392;
  static const int k_MessageBox_message = 3393;
  static const int k_Monitor_clientHeight = 3456;
  static const int k_Monitor_clientWidth = 3457;
  static const int k_Monitor_clientX = 3458;
  static const int k_Monitor_clientY = 3459;
  static const int k_Monitor_handle = 3460;
  static const int k_Monitor_height = 3461;
  static const int k_Monitor_width = 3462;
  static const int k_Monitor_x = 3463;
  static const int k_Monitor_y = 3464;
  static const int k_Monitor_zoom = 3465;
  static const int k_PDFDocument_height = 3520;
  static const int k_PDFDocument_width = 3521;
  static const int k_Path_pathData = 3584;
  static const int k_Path_textFonts = 3585;
  static const int k_Path_textOrigins = 3586;
  static const int k_Path_textStrings = 3587;
  static const int k_PathData_points = 3648;
  static const int k_PathData_types = 3649;
  static const int k_Pattern_color1 = 3712;
  static const int k_Pattern_color2 = 3713;
  static const int k_Pattern_endX = 3714;
  static const int k_Pattern_endY = 3715;
  static const int k_Pattern_image = 3716;
  static const int k_Pattern_startX = 3717;
  static const int k_Pattern_startY = 3718;
  static const int k_Point_x = 3776;
  static const int k_Point_y = 3777;
  static const int k_PopupList_font = 3840;
  static const int k_PopupList_items = 3841;
  static const int k_PopupList_minimumWidth = 3842;
  static const int k_PopupList_selection = 3843;
  static const int k_PrintDialog_endPage = 3904;
  static const int k_PrintDialog_printToFile = 3905;
  static const int k_PrintDialog_scope = 3906;
  static const int k_PrintDialog_startPage = 3907;
  static const int k_ProgressBar_maximum = 3968;
  static const int k_ProgressBar_minimum = 3969;
  static const int k_ProgressBar_selection = 3970;
  static const int k_RGB_blue = 4032;
  static const int k_RGB_green = 4033;
  static const int k_RGB_red = 4034;
  static const int k_Rectangle_height = 4096;
  static const int k_Rectangle_width = 4097;
  static const int k_Rectangle_x = 4098;
  static const int k_Rectangle_y = 4099;
  static const int k_Region_rects = 4160;
  static const int k_RowData_exclude = 4224;
  static const int k_RowData_height = 4225;
  static const int k_RowData_width = 4226;
  static const int k_SashForm_maximizedControl = 4288;
  static const int k_SashForm_sashWidth = 4289;
  static const int k_SashForm_weights = 4290;
  static const int k_Scale_maximum = 4352;
  static const int k_Scale_minimum = 4353;
  static const int k_Scale_selection = 4354;
  static const int k_ScrollBar_enabled = 4416;
  static const int k_ScrollBar_increment = 4417;
  static const int k_ScrollBar_maximum = 4418;
  static const int k_ScrollBar_minimum = 4419;
  static const int k_ScrollBar_pageIncrement = 4423;
  static const int k_ScrollBar_selection = 4420;
  static const int k_ScrollBar_thumb = 4421;
  static const int k_ScrollBar_visible = 4422;
  static const int k_Scrollable_horizontalBar = 4480;
  static const int k_Scrollable_verticalBar = 4481;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 4544;
  static const int k_ScrolledComposite_content = 4545;
  static const int k_ScrolledComposite_expandHorizontal = 4546;
  static const int k_ScrolledComposite_expandVertical = 4547;
  static const int k_ScrolledComposite_minHeight = 4548;
  static const int k_ScrolledComposite_minWidth = 4549;
  static const int k_ScrolledComposite_origin = 4550;
  static const int k_ScrolledComposite_originRequest = 4551;
  static const int k_Shell_alpha = 4608;
  static const int k_Shell_dialogs = 4609;
  static const int k_Shell_fullScreen = 4610;
  static const int k_Shell_maximumSize = 4611;
  static const int k_Shell_minimumSize = 4612;
  static const int k_Shell_modified = 4613;
  static const int k_Slider_increment = 4672;
  static const int k_Slider_maximum = 4673;
  static const int k_Slider_minimum = 4674;
  static const int k_Slider_pageIncrement = 4675;
  static const int k_Slider_selection = 4676;
  static const int k_Slider_thumb = 4677;
  static const int k_Spinner_digits = 4736;
  static const int k_Spinner_increment = 4737;
  static const int k_Spinner_keptText = 4738;
  static const int k_Spinner_maximum = 4739;
  static const int k_Spinner_minimum = 4740;
  static const int k_Spinner_pageIncrement = 4741;
  static const int k_Spinner_selection = 4742;
  static const int k_Spinner_textLimit = 4743;
  static const int k_StyleRange_fontStyle = 4800;
  static const int k_StyleRange_length = 4801;
  static const int k_StyleRange_start = 4802;
  static const int k_StyledText_alignment = 4864;
  static const int k_StyledText_alwaysShowScrollBars = 4865;
  static const int k_StyledText_blockSelection = 4866;
  static const int k_StyledText_blockSelectionBounds = 4867;
  static const int k_StyledText_bottomMargin = 4868;
  static const int k_StyledText_caretOffset = 4869;
  static const int k_StyledText_caretOffsets = 4870;
  static const int k_StyledText_doubleClickEnabled = 4871;
  static const int k_StyledText_editable = 4872;
  static const int k_StyledText_fixedLineHeight = 4873;
  static const int k_StyledText_horizontalPixel = 4874;
  static const int k_StyledText_indent = 4875;
  static const int k_StyledText_justify = 4876;
  static const int k_StyledText_leftMargin = 4877;
  static const int k_StyledText_lineSpacing = 4878;
  static const int k_StyledText_marginColor = 4879;
  static const int k_StyledText_renderer = 4880;
  static const int k_StyledText_rightMargin = 4881;
  static const int k_StyledText_selection = 4882;
  static const int k_StyledText_selectionBackground = 4883;
  static const int k_StyledText_selectionForeground = 4884;
  static const int k_StyledText_selectionRanges = 4885;
  static const int k_StyledText_styleIndex = 4886;
  static const int k_StyledText_styles = 4887;
  static const int k_StyledText_tabStops = 4888;
  static const int k_StyledText_tabs = 4889;
  static const int k_StyledText_text = 4890;
  static const int k_StyledText_topMargin = 4891;
  static const int k_StyledText_topPixel = 4892;
  static const int k_StyledText_wordWrap = 4893;
  static const int k_StyledText_wrapIndent = 4894;
  static const int k_StyledTextPrintOptions_footer = 4928;
  static const int k_StyledTextPrintOptions_header = 4929;
  static const int k_StyledTextPrintOptions_jobName = 4930;
  static const int k_StyledTextPrintOptions_lineLabels = 4931;
  static const int k_StyledTextPrintOptions_printLineBackground = 4932;
  static const int k_StyledTextPrintOptions_printLineNumbers = 4933;
  static const int k_StyledTextPrintOptions_printTextBackground = 4934;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 4935;
  static const int k_StyledTextPrintOptions_printTextForeground = 4936;
  static const int k_StyledTextRenderer_ascent = 4992;
  static const int k_StyledTextRenderer_averageCharWidth = 4993;
  static const int k_StyledTextRenderer_averageLineHeight = 4994;
  static const int k_StyledTextRenderer_boldFont = 4995;
  static const int k_StyledTextRenderer_boldItalicFont = 4996;
  static const int k_StyledTextRenderer_bulletTexts = 4997;
  static const int k_StyledTextRenderer_bulletsIndices = 4998;
  static const int k_StyledTextRenderer_descent = 4999;
  static const int k_StyledTextRenderer_fixedPitch = 5000;
  static const int k_StyledTextRenderer_hasLinks = 5001;
  static const int k_StyledTextRenderer_idleRunning = 5002;
  static const int k_StyledTextRenderer_italicFont = 5003;
  static const int k_StyledTextRenderer_lineCount = 5004;
  static const int k_StyledTextRenderer_lineSizes = 5005;
  static const int k_StyledTextRenderer_lineSpacingComputing = 5006;
  static const int k_StyledTextRenderer_lineSpacings = 5007;
  static const int k_StyledTextRenderer_lines = 5008;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 5009;
  static const int k_StyledTextRenderer_maxWidth = 5010;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 5011;
  static const int k_StyledTextRenderer_ranges = 5012;
  static const int k_StyledTextRenderer_redrawLines = 5013;
  static const int k_StyledTextRenderer_regularFont = 5014;
  static const int k_StyledTextRenderer_styleCount = 5015;
  static const int k_StyledTextRenderer_styledText = 5016;
  static const int k_StyledTextRenderer_styles = 5017;
  static const int k_StyledTextRenderer_stylesSet = 5018;
  static const int k_StyledTextRenderer_stylesSetCount = 5019;
  static const int k_StyledTextRenderer_tabLength = 5020;
  static const int k_StyledTextRenderer_tabWidth = 5021;
  static const int k_StyledTextRenderer_topIndex = 5022;
  static const int k_TabFolder_items = 5056;
  static const int k_TabFolder_selection = 5057;
  static const int k_TabItem_control = 5120;
  static const int k_TabItem_toolTipText = 5121;
  static const int k_Table_columnOrder = 5184;
  static const int k_Table_columns = 5185;
  static const int k_Table_editors = 5186;
  static const int k_Table_headerBackground = 5187;
  static const int k_Table_headerForeground = 5188;
  static const int k_Table_headerVisible = 5189;
  static const int k_Table_itemCount = 5190;
  static const int k_Table_items = 5191;
  static const int k_Table_linesVisible = 5192;
  static const int k_Table_ownerDrawItemHeight = 5193;
  static const int k_Table_selection = 5194;
  static const int k_Table_sortColumn = 5196;
  static const int k_Table_sortDirection = 5197;
  static const int k_Table_topIndex = 5195;
  static const int k_TableColumn_alignment = 5248;
  static const int k_TableColumn_moveable = 5249;
  static const int k_TableColumn_resizable = 5250;
  static const int k_TableColumn_toolTipText = 5252;
  static const int k_TableColumn_width = 5251;
  static const int k_TableEditor_column = 5312;
  static const int k_TableEditor_item = 5313;
  static const int k_TableItem_background = 5376;
  static const int k_TableItem_backgrounds = 5377;
  static const int k_TableItem_checked = 5378;
  static const int k_TableItem_font = 5379;
  static const int k_TableItem_fonts = 5380;
  static const int k_TableItem_foreground = 5381;
  static const int k_TableItem_foregrounds = 5382;
  static const int k_TableItem_grayed = 5383;
  static const int k_TableItem_images = 5384;
  static const int k_TableItem_paintedImages = 5387;
  static const int k_TableItem_paintedTexts = 5385;
  static const int k_TableItem_texts = 5386;
  static const int k_Text_caretPosition = 5440;
  static const int k_Text_echoCharacter = 5441;
  static const int k_Text_editTexts = 5442;
  static const int k_Text_editable = 5443;
  static const int k_Text_edits = 5444;
  static const int k_Text_message = 5445;
  static const int k_Text_selection = 5446;
  static const int k_Text_text = 5447;
  static const int k_Text_textLimit = 5448;
  static const int k_TextStyle_background = 5504;
  static const int k_TextStyle_borderColor = 5505;
  static const int k_TextStyle_borderStyle = 5506;
  static const int k_TextStyle_font = 5507;
  static const int k_TextStyle_foreground = 5508;
  static const int k_TextStyle_metrics = 5509;
  static const int k_TextStyle_rise = 5510;
  static const int k_TextStyle_strikeout = 5511;
  static const int k_TextStyle_strikeoutColor = 5512;
  static const int k_TextStyle_underline = 5513;
  static const int k_TextStyle_underlineColor = 5514;
  static const int k_TextStyle_underlineStyle = 5515;
  static const int k_ToolBar_items = 5568;
  static const int k_ToolItem_background = 5632;
  static const int k_ToolItem_control = 5633;
  static const int k_ToolItem_disabledImage = 5634;
  static const int k_ToolItem_enabled = 5635;
  static const int k_ToolItem_foreground = 5636;
  static const int k_ToolItem_hotImage = 5637;
  static const int k_ToolItem_selection = 5638;
  static const int k_ToolItem_toolTipText = 5639;
  static const int k_ToolItem_width = 5640;
  static const int k_ToolTip_location = 5696;
  static const int k_ToolTip_message = 5697;
  static const int k_ToolTip_text = 5698;
  static const int k_ToolTip_visible = 5699;
  static const int k_Touch_id = 5760;
  static const int k_Touch_primary = 5761;
  static const int k_Touch_source = 5762;
  static const int k_Touch_state = 5763;
  static const int k_Touch_x = 5764;
  static const int k_Touch_y = 5765;
  static const int k_TouchSource_bounds = 5824;
  static const int k_TouchSource_direct = 5825;
  static const int k_TouchSource_handle = 5826;
  static const int k_Transform_elements = 5888;
  static const int k_Tree_columnOrder = 5952;
  static const int k_Tree_columns = 5953;
  static const int k_Tree_editors = 5954;
  static const int k_Tree_headerBackground = 5955;
  static const int k_Tree_headerForeground = 5956;
  static const int k_Tree_headerVisible = 5957;
  static const int k_Tree_items = 5958;
  static const int k_Tree_linesVisible = 5959;
  static const int k_Tree_ownerDrawItemHeight = 5960;
  static const int k_Tree_selection = 5961;
  static const int k_Tree_sortColumn = 5962;
  static const int k_Tree_sortDirection = 5963;
  static const int k_Tree_topItem = 5964;
  static const int k_TreeColumn_alignment = 6016;
  static const int k_TreeColumn_moveable = 6017;
  static const int k_TreeColumn_resizable = 6018;
  static const int k_TreeColumn_toolTipText = 6020;
  static const int k_TreeColumn_width = 6019;
  static const int k_TreeEditor_column = 6080;
  static const int k_TreeEditor_editor = 6081;
  static const int k_TreeEditor_item = 6082;
  static const int k_TreeItem_background = 6144;
  static const int k_TreeItem_backgrounds = 6145;
  static const int k_TreeItem_checked = 6146;
  static const int k_TreeItem_expanded = 6147;
  static const int k_TreeItem_font = 6148;
  static const int k_TreeItem_fonts = 6149;
  static const int k_TreeItem_foreground = 6150;
  static const int k_TreeItem_foregrounds = 6151;
  static const int k_TreeItem_grayed = 6152;
  static const int k_TreeItem_images = 6153;
  static const int k_TreeItem_itemCount = 6154;
  static const int k_TreeItem_items = 6155;
  static const int k_TreeItem_paintedImages = 6158;
  static const int k_TreeItem_paintedTexts = 6156;
  static const int k_TreeItem_texts = 6157;
  static const int k_ViewForm_borderVisible = 6336;
  static const int k_ViewForm_highlight = 6337;
  static const int k_ViewForm_separator = 6338;
  static const int k_Widget_style = 6208;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  64,
  256,
  516,
  533,
  576,
  640,
  1025,
  1165,
  1216,
  1282,
  1344,
  1536,
  1865,
  1868,
  1883,
  1920,
  1984,
  2305,
  2508,
  3265,
  3330,
  4288,
  4480,
  4481,
  4545,
  5056,
  5057,
  5120,
  5185,
  5186,
  5191,
  5196,
  5313,
  5568,
  5633,
  5953,
  5954,
  5958,
  5961,
  5962,
  5964,
  6081,
  6082,
  6155,
};

/// Every type's number, for what names a widget by its type's number.
abstract final class WireType {
  static const int accessible = 1; // Accessible
  static const int animatedProgress = 2; // AnimatedProgress
  static const int borderData = 4; // BorderData
  static const int browser = 5; // Browser
  static const int browserFunction = 6; // BrowserFunction
  static const int busyIndicator = 7; // BusyIndicator
  static const int button = 8; // Button
  static const int byteArrayTransfer = 9; // ByteArrayTransfer
  static const int cBanner = 10; // CBanner
  static const int cCombo = 11; // CCombo
  static const int cLabel = 12; // CLabel
  static const int cTabFolder = 13; // CTabFolder
  static const int cTabItem = 14; // CTabItem
  static const int canvas = 15; // Canvas
  static const int caret = 16; // Caret
  static const int clipboard = 17; // Clipboard
  static const int color = 18; // Color
  static const int colorDialog = 19; // ColorDialog
  static const int combo = 20; // Combo
  static const int composite = 21; // Composite
  static const int controlEditor = 22; // ControlEditor
  static const int coolBar = 23; // CoolBar
  static const int coolItem = 24; // CoolItem
  static const int cursor = 25; // Cursor
  static const int dateTime = 26; // DateTime
  static const int decorations = 27; // Decorations
  static const int device = 28; // Device
  static const int dialog = 29; // Dialog
  static const int directoryDialog = 30; // DirectoryDialog
  static const int display = 31; // Display
  static const int dragSource = 32; // DragSource
  static const int dropTarget = 33; // DropTarget
  static const int event = 34; // Event
  static const int ewtWidget = 35; // EwtWidget
  static const int expandBar = 36; // ExpandBar
  static const int expandItem = 37; // ExpandItem
  static const int fileDialog = 38; // FileDialog
  static const int fileTransfer = 39; // FileTransfer
  static const int font = 40; // Font
  static const int fontData = 41; // FontData
  static const int fontDialog = 42; // FontDialog
  static const int fontMeasureBridge = 43; // FontMeasureBridge
  static const int fontMetrics = 44; // FontMetrics
  static const int formAttachment = 45; // FormAttachment
  static const int formData = 46; // FormData
  static const int gc = 47; // GC
  static const int gCData = 48; // GCData
  static const int gLCanvas = 49; // GLCanvas
  static const int gLData = 50; // GLData
  static const int glyphMetrics = 51; // GlyphMetrics
  static const int gridData = 52; // GridData
  static const int group = 53; // Group
  static const int hTMLTransfer = 54; // HTMLTransfer
  static const int ime = 55; // IME
  static const int image = 56; // Image
  static const int imageData = 57; // ImageData
  static const int imageSizeBridge = 58; // ImageSizeBridge
  static const int imageTransfer = 59; // ImageTransfer
  static const int label = 60; // Label
  static const int lineInfo = 61; // LineInfo
  static const int lineSizeInfo = 62; // LineSizeInfo
  static const int link = 63; // Link
  static const int list = 64; // List
  static const int mainComposite = 65; // MainComposite
  static const int mainToolbar = 66; // MainToolbar
  static const int menu = 67; // Menu
  static const int menuItem = 68; // MenuItem
  static const int messageBox = 69; // MessageBox
  static const int monitor = 70; // Monitor
  static const int pDFDocument = 71; // PDFDocument
  static const int path = 72; // Path
  static const int pathData = 73; // PathData
  static const int pattern = 74; // Pattern
  static const int point = 75; // Point
  static const int popupList = 76; // PopupList
  static const int printDialog = 77; // PrintDialog
  static const int printer = 78; // Printer
  static const int program = 79; // Program
  static const int progressBar = 80; // ProgressBar
  static const int rgb = 81; // RGB
  static const int rTFTransfer = 82; // RTFTransfer
  static const int rectangle = 83; // Rectangle
  static const int region = 84; // Region
  static const int resource = 85; // Resource
  static const int rowData = 86; // RowData
  static const int sash = 87; // Sash
  static const int sashForm = 88; // SashForm
  static const int scale = 89; // Scale
  static const int scrollBar = 90; // ScrollBar
  static const int scrolledComposite = 91; // ScrolledComposite
  static const int shell = 92; // Shell
  static const int shellPopups = 93; // ShellPopups
  static const int sideBar = 94; // SideBar
  static const int slider = 95; // Slider
  static const int spinner = 96; // Spinner
  static const int statusBar = 97; // StatusBar
  static const int styleRange = 98; // StyleRange
  static const int styledText = 99; // StyledText
  static const int styledTextPrintOptions = 100; // StyledTextPrintOptions
  static const int styledTextRenderer = 101; // StyledTextRenderer
  static const int swingIsland = 102; // SwingIsland
  static const int tabFolder = 103; // TabFolder
  static const int tabItem = 104; // TabItem
  static const int table = 105; // Table
  static const int tableColumn = 106; // TableColumn
  static const int tableCursor = 107; // TableCursor
  static const int tableEditor = 108; // TableEditor
  static const int tableItem = 109; // TableItem
  static const int taskBar = 110; // TaskBar
  static const int taskItem = 111; // TaskItem
  static const int text = 112; // Text
  static const int textStyle = 113; // TextStyle
  static const int textTransfer = 114; // TextTransfer
  static const int toolBar = 115; // ToolBar
  static const int toolItem = 116; // ToolItem
  static const int toolTip = 117; // ToolTip
  static const int touch = 118; // Touch
  static const int touchSource = 119; // TouchSource
  static const int tracker = 120; // Tracker
  static const int transfer = 121; // Transfer
  static const int transferData = 122; // TransferData
  static const int transform = 123; // Transform
  static const int tray = 124; // Tray
  static const int trayItem = 125; // TrayItem
  static const int tree = 126; // Tree
  static const int treeColumn = 127; // TreeColumn
  static const int treeCursor = 128; // TreeCursor
  static const int treeEditor = 129; // TreeEditor
  static const int treeItem = 130; // TreeItem
  static const int uRLTransfer = 131; // URLTransfer
  static const int viewForm = 132; // ViewForm
  static const int widgetSizeBridge = 133; // WidgetSizeBridge
  static const int displayPopups = 134; // DisplayPopups
}

/// Type names by the number a frame names a value's class with.
const Map<int, String> wireTypeNames = {
  1: 'Accessible',
  2: 'AnimatedProgress',
  4: 'BorderData',
  5: 'Browser',
  6: 'BrowserFunction',
  7: 'BusyIndicator',
  8: 'Button',
  9: 'ByteArrayTransfer',
  10: 'CBanner',
  11: 'CCombo',
  12: 'CLabel',
  13: 'CTabFolder',
  14: 'CTabItem',
  15: 'Canvas',
  16: 'Caret',
  17: 'Clipboard',
  18: 'Color',
  19: 'ColorDialog',
  20: 'Combo',
  21: 'Composite',
  22: 'ControlEditor',
  23: 'CoolBar',
  24: 'CoolItem',
  25: 'Cursor',
  26: 'DateTime',
  27: 'Decorations',
  28: 'Device',
  29: 'Dialog',
  30: 'DirectoryDialog',
  31: 'Display',
  134: 'DisplayPopups',
  32: 'DragSource',
  33: 'DropTarget',
  34: 'Event',
  35: 'EwtWidget',
  36: 'ExpandBar',
  37: 'ExpandItem',
  38: 'FileDialog',
  39: 'FileTransfer',
  40: 'Font',
  41: 'FontData',
  42: 'FontDialog',
  43: 'FontMeasureBridge',
  44: 'FontMetrics',
  45: 'FormAttachment',
  46: 'FormData',
  47: 'GC',
  48: 'GCData',
  49: 'GLCanvas',
  50: 'GLData',
  51: 'GlyphMetrics',
  52: 'GridData',
  53: 'Group',
  54: 'HTMLTransfer',
  55: 'IME',
  56: 'Image',
  57: 'ImageData',
  58: 'ImageSizeBridge',
  59: 'ImageTransfer',
  60: 'Label',
  61: 'LineInfo',
  62: 'LineSizeInfo',
  63: 'Link',
  64: 'List',
  65: 'MainComposite',
  66: 'MainToolbar',
  67: 'Menu',
  68: 'MenuItem',
  69: 'MessageBox',
  70: 'Monitor',
  71: 'PDFDocument',
  72: 'Path',
  73: 'PathData',
  74: 'Pattern',
  75: 'Point',
  76: 'PopupList',
  77: 'PrintDialog',
  78: 'Printer',
  79: 'Program',
  80: 'ProgressBar',
  81: 'RGB',
  82: 'RTFTransfer',
  83: 'Rectangle',
  84: 'Region',
  85: 'Resource',
  86: 'RowData',
  87: 'Sash',
  88: 'SashForm',
  89: 'Scale',
  90: 'ScrollBar',
  91: 'ScrolledComposite',
  92: 'Shell',
  93: 'ShellPopups',
  94: 'SideBar',
  95: 'Slider',
  96: 'Spinner',
  97: 'StatusBar',
  98: 'StyleRange',
  99: 'StyledText',
  100: 'StyledTextPrintOptions',
  101: 'StyledTextRenderer',
  102: 'SwingIsland',
  103: 'TabFolder',
  104: 'TabItem',
  105: 'Table',
  106: 'TableColumn',
  107: 'TableCursor',
  108: 'TableEditor',
  109: 'TableItem',
  110: 'TaskBar',
  111: 'TaskItem',
  112: 'Text',
  113: 'TextStyle',
  114: 'TextTransfer',
  115: 'ToolBar',
  116: 'ToolItem',
  117: 'ToolTip',
  118: 'Touch',
  119: 'TouchSource',
  120: 'Tracker',
  121: 'Transfer',
  122: 'TransferData',
  123: 'Transform',
  124: 'Tray',
  125: 'TrayItem',
  126: 'Tree',
  127: 'TreeColumn',
  128: 'TreeCursor',
  129: 'TreeEditor',
  130: 'TreeItem',
  131: 'URLTransfer',
  132: 'ViewForm',
  133: 'WidgetSizeBridge',
};

/// Channel names by the number a message names its channel with: a global name, or what follows
/// a widget's type and id.
const Map<int, String> wireChannelNames = {
  1: '',
  2: 'Arm/Arm',
  3: 'Authentication/authenticate',
  4: 'BidiSegment/lineGetSegments',
  5: 'CTabFolder/itemClosed',
  6: 'CTabFolder/reorderItems',
  7: 'CTabFolder/stripLaidOut',
  8: 'CTabFolder2/close',
  9: 'CTabFolder2/maximize',
  10: 'CTabFolder2/minimize',
  11: 'CTabFolder2/restore',
  12: 'CTabFolder2/showList',
  13: 'Caret/caretMoved',
  14: 'ClientReady',
  15: 'Clipboard/contents',
  16: 'Clipboard/read',
  17: 'Clipboard/write',
  18: 'CloseWindow',
  19: 'CloseWindow/close',
  20: 'Control/Move',
  21: 'Control/Resize',
  22: 'Dispose/Dispose',
  234: 'DnD/remoteDrag',
  235: 'DnD/remoteDropNone',
  23: 'Drag/dragFinished',
  24: 'Drag/dragSetData',
  25: 'Drag/dragStart',
  26: 'DragDetect/DragDetect',
  236: 'DragDetect/dragCancel',
  237: 'DragDetect/dragImage',
  238: 'DragDetect/dragMove',
  239: 'DragDetect/dragRelease',
  27: 'DragDetect/dragStartResult',
  240: 'DragDetect/remoteAnswer',
  28: 'Drop/dragEnter',
  29: 'Drop/dragLeave',
  30: 'Drop/dragOperationChanged',
  31: 'Drop/dragOver',
  32: 'Drop/drop',
  33: 'Drop/dropAccept',
  34: 'Drop/negotiationResult',
  35: 'Expand/Collapse',
  36: 'Expand/Expand',
  37: 'ExtendedModify/modifyText',
  38: 'Focus/FocusIn',
  39: 'Focus/FocusOut',
  40: 'GC/create',
  41: 'GC/inFrame',
  42: 'Gesture/Gesture',
  43: 'GetIdFromPoint',
  44: 'GetIdFromPointResponse',
  45: 'GetItemBounds',
  46: 'GetItemBoundsResponse',
  47: 'GetItemHeight',
  48: 'GetItemHeightResponse',
  49: 'Help/Help',
  50: 'Image/forget',
  51: 'Image/releaseRemoteRef',
  52: 'Image/requestPixels',
  53: 'ImeComposition',
  54: 'Key/KeyDown',
  55: 'Key/KeyUp',
  56: 'LineBackground/lineGetBackground',
  57: 'LineStyle/lineGetStyle',
  58: 'List/Visible',
  59: 'Location/changed',
  60: 'Location/changing',
  61: 'Menu/Hide',
  62: 'Menu/Show',
  63: 'MenuDetect/MenuDetect',
  64: 'MenuKey',
  65: 'Modify/Modify',
  66: 'Mouse/MouseDoubleClick',
  67: 'Mouse/MouseDown',
  68: 'Mouse/MouseUp',
  69: 'MouseMove/MouseMove',
  70: 'MouseTrack/MouseEnter',
  71: 'MouseTrack/MouseExit',
  72: 'MouseTrack/MouseHover',
  73: 'MouseWheel/MouseWheel',
  74: 'OpenWindow',
  75: 'OpenWindow/open',
  76: 'Paint/Paint',
  77: 'PaintItem/PaintItem',
  78: 'PaintObject/paintObject',
  79: 'Popups',
  80: 'Progress/changed',
  81: 'Progress/completed',
  82: 'ResendGeometry',
  83: 'ResendText',
  84: 'Scroll/Scroll',
  85: 'Segment/Segments',
  86: 'Selection/DefaultSelection',
  87: 'Selection/OpenMenu',
  88: 'Selection/Selection',
  89: 'SetData/SetData',
  90: 'Shell/Activate',
  91: 'Shell/Close',
  92: 'Shell/Deactivate',
  93: 'Shell/Deiconify',
  94: 'Shell/Iconify',
  95: 'Shell/SetBounds',
  96: 'StatusText/changed',
  97: 'TextChange',
  98: 'TextGeometry',
  99: 'Title/changed',
  100: 'Touch/Touch',
  101: 'Tracker/cancel',
  102: 'Tracker/close',
  103: 'Tracker/open',
  104: 'Traverse/Traverse',
  105: 'Tree/Collapse',
  106: 'Tree/Expand',
  107: 'Verify/Verify',
  108: 'VerifyKey/verifyKey',
  109: 'VisibilityWindow/hide',
  110: 'VisibilityWindow/show',
  111: 'WinClose',
  241: 'WinDidEnterFullScreen',
  242: 'WinDidExitFullScreen',
  243: 'WinExitFullScreen',
  244: 'WinFullScreen',
  245: 'WinFullScreenState',
  112: 'WinMaximize',
  113: 'WinMinimize',
  114: 'WinRestore',
  115: 'WinUnload',
  116: 'WindowBounds',
  117: 'WindowOpenFailed',
  118: 'WindowState',
  119: 'WindowTitle',
  120: 'WordMovement/getNextOffset',
  121: 'WordMovement/getPreviousOffset',
  122: 'activate',
  123: 'activation',
  246: 'anim',
  124: 'b',
  125: 'back',
  132: 'browserFunctionCall',
  133: 'c',
  247: 'callback',
  134: 'close',
  135: 'cookie',
  136: 'copyAreaImageintint',
  137: 'copyAreaImageintintResponse',
  138: 'copyAreaintintintintintintboolean',
  139: 'drawArcintintintintintint',
  140: 'drawFocusintintintint',
  141: 'drawImageImageintint',
  142: 'drawImageImageintintintint',
  143: 'drawImageImageintintintintintintintint',
  144: 'drawLineintintintint',
  145: 'drawOvalintintintint',
  146: 'drawPathPath',
  147: 'drawPointintint',
  148: 'drawPolygonint',
  149: 'drawPolylineint',
  150: 'drawRectangleintintintint',
  151: 'drawRoundRectangleintintintintintint',
  152: 'drawTextStringintintint',
  153: 'evaluate',
  154: 'evaluate/result',
  253: 'evolve.test.bootTrace',
  254: 'evolve.test.bootTraceResponse',
  155: 'evolve.test.drag',
  156: 'evolve.test.dragDone',
  157: 'evolve.test.frameCost',
  158: 'evolve.test.frameCostResponse',
  159: 'evolve.test.frameStats',
  160: 'evolve.test.frameStatsResponse',
  161: 'evolve.test.frameSync',
  162: 'evolve.test.frameSynced',
  163: 'evolve.test.iframeLoaded',
  164: 'evolve.test.query',
  165: 'evolve.test.queryResponse',
  166: 'evolve.test.styledTextPerf',
  167: 'evolve.test.styledTextPerfResponse',
  168: 'evolve.test.wireStats',
  169: 'evolve.test.wireStatsResponse',
  170: 'execute',
  171: 'fillArcintintintintintint',
  172: 'fillGradientRectangleintintintintboolean',
  173: 'fillOvalintintintint',
  174: 'fillPathPath',
  175: 'fillPolygonint',
  176: 'fillRectangleintintintint',
  177: 'fillRoundRectangleintintintintintint',
  178: 'fontSizeRequest',
  179: 'fontSizeResponse',
  180: 'forward',
  181: 'gcDispose',
  182: 'getCookie',
  183: 'imageInit',
  184: 'imageResult',
  185: 'imageSizeRequest',
  186: 'imageSizeResponse',
  187: 'imageSnapshotResult',
  188: 'key/verdict',
  189: 'key/vetoable',
  190: 'locationchanging/result',
  191: 'menu/verdict',
  192: 'modify/verdict',
  193: 'modify/vetoable',
  194: 'navigate',
  195: 'navigateRequest',
  196: 'navigationState',
  197: 'nextFrame',
  198: 'openMenu',
  199: 'pixelsResult',
  248: 'rebuild',
  200: 'reload',
  201: 'renderSnapshot',
  202: 'setCookie',
  203: 'shown',
  204: 'stop',
  249: 'subtree',
  250: 'subtree/request',
  205: 'swing',
  206: 'swing.open',
  207: 'swingIsland',
  208: 'swingIsland.close',
  209: 'swingIsland.dispose',
  210: 'swingIsland.island',
  211: 'swingIsland.opened',
  212: 'swingIsland.resize',
  213: 'swingIsland.surface',
  214: 'swingIsland.title',
  215: 'swt.evolve.batch',
  216: 'swt.evolve.focus',
  217: 'swt.evolve.focus.seen',
  218: 'swt.evolve.focusHolder',
  219: 'swt.evolve.properties',
  221: 'swt.evolve.test.runUi',
  222: 'swt.evolve.test.runUiResponse',
  223: 'swt.evolve.timing.bare',
  224: 'swt.evolve.timing.ping',
  225: 'swt.evolve.timing.pong',
  226: 'swt.evolve.url.open',
  227: 'swt.evolve.widget.refresh',
  228: 'swt.evolve.wire',
  251: 'transport',
  252: 'transport/request',
  229: 'traverse/verdict',
  230: 'traverse/vetoable',
  231: 'v',
  232: 'widgetSizeRequest',
  233: 'widgetSizeResponse',
};

/// Every channel's number, for what sends or answers a channel by the number a frame names.
abstract final class WireChannel {
  static const int armArm = 2; // Arm/Arm
  static const int authenticationAuthenticate =
      3; // Authentication/authenticate
  static const int bidiSegmentLineGetSegments =
      4; // BidiSegment/lineGetSegments
  static const int cTabFolderItemClosed = 5; // CTabFolder/itemClosed
  static const int cTabFolderReorderItems = 6; // CTabFolder/reorderItems
  static const int cTabFolderStripLaidOut = 7; // CTabFolder/stripLaidOut
  static const int cTabFolder2Close = 8; // CTabFolder2/close
  static const int cTabFolder2Maximize = 9; // CTabFolder2/maximize
  static const int cTabFolder2Minimize = 10; // CTabFolder2/minimize
  static const int cTabFolder2Restore = 11; // CTabFolder2/restore
  static const int cTabFolder2ShowList = 12; // CTabFolder2/showList
  static const int caretCaretMoved = 13; // Caret/caretMoved
  static const int clientReady = 14; // ClientReady
  static const int clipboardContents = 15; // Clipboard/contents
  static const int clipboardRead = 16; // Clipboard/read
  static const int clipboardWrite = 17; // Clipboard/write
  static const int closeWindow = 18; // CloseWindow
  static const int closeWindowClose = 19; // CloseWindow/close
  static const int controlMove = 20; // Control/Move
  static const int controlResize = 21; // Control/Resize
  static const int disposeDispose = 22; // Dispose/Dispose
  static const int dragDragFinished = 23; // Drag/dragFinished
  static const int dragDragSetData = 24; // Drag/dragSetData
  static const int dragDragStart = 25; // Drag/dragStart
  static const int dragDetectDragDetect = 26; // DragDetect/DragDetect
  static const int dragDetectDragStartResult = 27; // DragDetect/dragStartResult
  static const int dropDragEnter = 28; // Drop/dragEnter
  static const int dropDragLeave = 29; // Drop/dragLeave
  static const int dropDragOperationChanged = 30; // Drop/dragOperationChanged
  static const int dropDragOver = 31; // Drop/dragOver
  static const int dropDrop = 32; // Drop/drop
  static const int dropDropAccept = 33; // Drop/dropAccept
  static const int dropNegotiationResult = 34; // Drop/negotiationResult
  static const int expandCollapse = 35; // Expand/Collapse
  static const int expandExpand = 36; // Expand/Expand
  static const int extendedModifyModifyText = 37; // ExtendedModify/modifyText
  static const int focusFocusIn = 38; // Focus/FocusIn
  static const int focusFocusOut = 39; // Focus/FocusOut
  static const int gcCreate = 40; // GC/create
  static const int gcInFrame = 41; // GC/inFrame
  static const int gestureGesture = 42; // Gesture/Gesture
  static const int getIdFromPoint = 43; // GetIdFromPoint
  static const int getIdFromPointResponse = 44; // GetIdFromPointResponse
  static const int getItemBounds = 45; // GetItemBounds
  static const int getItemBoundsResponse = 46; // GetItemBoundsResponse
  static const int getItemHeight = 47; // GetItemHeight
  static const int getItemHeightResponse = 48; // GetItemHeightResponse
  static const int helpHelp = 49; // Help/Help
  static const int imageForget = 50; // Image/forget
  static const int imageReleaseRemoteRef = 51; // Image/releaseRemoteRef
  static const int imageRequestPixels = 52; // Image/requestPixels
  static const int imeComposition = 53; // ImeComposition
  static const int keyKeyDown = 54; // Key/KeyDown
  static const int keyKeyUp = 55; // Key/KeyUp
  static const int lineBackgroundLineGetBackground =
      56; // LineBackground/lineGetBackground
  static const int lineStyleLineGetStyle = 57; // LineStyle/lineGetStyle
  static const int listVisible = 58; // List/Visible
  static const int locationChanged = 59; // Location/changed
  static const int locationChanging = 60; // Location/changing
  static const int menuHide = 61; // Menu/Hide
  static const int menuShow = 62; // Menu/Show
  static const int menuDetectMenuDetect = 63; // MenuDetect/MenuDetect
  static const int menuKey = 64; // MenuKey
  static const int modifyModify = 65; // Modify/Modify
  static const int mouseMouseDoubleClick = 66; // Mouse/MouseDoubleClick
  static const int mouseMouseDown = 67; // Mouse/MouseDown
  static const int mouseMouseUp = 68; // Mouse/MouseUp
  static const int mouseMoveMouseMove = 69; // MouseMove/MouseMove
  static const int mouseTrackMouseEnter = 70; // MouseTrack/MouseEnter
  static const int mouseTrackMouseExit = 71; // MouseTrack/MouseExit
  static const int mouseTrackMouseHover = 72; // MouseTrack/MouseHover
  static const int mouseWheelMouseWheel = 73; // MouseWheel/MouseWheel
  static const int openWindow = 74; // OpenWindow
  static const int openWindowOpen = 75; // OpenWindow/open
  static const int paintPaint = 76; // Paint/Paint
  static const int paintItemPaintItem = 77; // PaintItem/PaintItem
  static const int paintObjectPaintObject = 78; // PaintObject/paintObject
  static const int popups = 79; // Popups
  static const int progressChanged = 80; // Progress/changed
  static const int progressCompleted = 81; // Progress/completed
  static const int resendGeometry = 82; // ResendGeometry
  static const int resendText = 83; // ResendText
  static const int scrollScroll = 84; // Scroll/Scroll
  static const int segmentSegments = 85; // Segment/Segments
  static const int selectionDefaultSelection = 86; // Selection/DefaultSelection
  static const int selectionOpenMenu = 87; // Selection/OpenMenu
  static const int selectionSelection = 88; // Selection/Selection
  static const int setDataSetData = 89; // SetData/SetData
  static const int shellActivate = 90; // Shell/Activate
  static const int shellClose = 91; // Shell/Close
  static const int shellDeactivate = 92; // Shell/Deactivate
  static const int shellDeiconify = 93; // Shell/Deiconify
  static const int shellIconify = 94; // Shell/Iconify
  static const int shellSetBounds = 95; // Shell/SetBounds
  static const int statusTextChanged = 96; // StatusText/changed
  static const int textChange = 97; // TextChange
  static const int textGeometry = 98; // TextGeometry
  static const int titleChanged = 99; // Title/changed
  static const int touchTouch = 100; // Touch/Touch
  static const int trackerCancel = 101; // Tracker/cancel
  static const int trackerClose = 102; // Tracker/close
  static const int trackerOpen = 103; // Tracker/open
  static const int traverseTraverse = 104; // Traverse/Traverse
  static const int treeCollapse = 105; // Tree/Collapse
  static const int treeExpand = 106; // Tree/Expand
  static const int verifyVerify = 107; // Verify/Verify
  static const int verifyKeyVerifyKey = 108; // VerifyKey/verifyKey
  static const int visibilityWindowHide = 109; // VisibilityWindow/hide
  static const int visibilityWindowShow = 110; // VisibilityWindow/show
  static const int winClose = 111; // WinClose
  static const int winMaximize = 112; // WinMaximize
  static const int winMinimize = 113; // WinMinimize
  static const int winRestore = 114; // WinRestore
  static const int winUnload = 115; // WinUnload
  static const int windowBounds = 116; // WindowBounds
  static const int windowOpenFailed = 117; // WindowOpenFailed
  static const int windowState = 118; // WindowState
  static const int windowTitle = 119; // WindowTitle
  static const int wordMovementGetNextOffset =
      120; // WordMovement/getNextOffset
  static const int wordMovementGetPreviousOffset =
      121; // WordMovement/getPreviousOffset
  static const int activate = 122; // activate
  static const int activation = 123; // activation
  static const int b = 124; // b
  static const int back = 125; // back
  static const int browserFunctionCall = 132; // browserFunctionCall
  static const int c = 133; // c
  static const int close = 134; // close
  static const int cookie = 135; // cookie
  static const int copyAreaImageintint = 136; // copyAreaImageintint
  static const int copyAreaImageintintResponse =
      137; // copyAreaImageintintResponse
  static const int copyAreaintintintintintintboolean =
      138; // copyAreaintintintintintintboolean
  static const int drawArcintintintintintint = 139; // drawArcintintintintintint
  static const int drawFocusintintintint = 140; // drawFocusintintintint
  static const int drawImageImageintint = 141; // drawImageImageintint
  static const int drawImageImageintintintint =
      142; // drawImageImageintintintint
  static const int drawImageImageintintintintintintintint =
      143; // drawImageImageintintintintintintintint
  static const int drawLineintintintint = 144; // drawLineintintintint
  static const int drawOvalintintintint = 145; // drawOvalintintintint
  static const int drawPathPath = 146; // drawPathPath
  static const int drawPointintint = 147; // drawPointintint
  static const int drawPolygonint = 148; // drawPolygonint
  static const int drawPolylineint = 149; // drawPolylineint
  static const int drawRectangleintintintint = 150; // drawRectangleintintintint
  static const int drawRoundRectangleintintintintintint =
      151; // drawRoundRectangleintintintintintint
  static const int drawTextStringintintint = 152; // drawTextStringintintint
  static const int evaluate = 153; // evaluate
  static const int evaluateResult = 154; // evaluate/result
  static const int evolveTestDrag = 155; // evolve.test.drag
  static const int evolveTestDragDone = 156; // evolve.test.dragDone
  static const int evolveTestFrameCost = 157; // evolve.test.frameCost
  static const int evolveTestFrameCostResponse =
      158; // evolve.test.frameCostResponse
  static const int evolveTestFrameStats = 159; // evolve.test.frameStats
  static const int evolveTestFrameStatsResponse =
      160; // evolve.test.frameStatsResponse
  static const int evolveTestFrameSync = 161; // evolve.test.frameSync
  static const int evolveTestFrameSynced = 162; // evolve.test.frameSynced
  static const int evolveTestIframeLoaded = 163; // evolve.test.iframeLoaded
  static const int evolveTestQuery = 164; // evolve.test.query
  static const int evolveTestQueryResponse = 165; // evolve.test.queryResponse
  static const int evolveTestStyledTextPerf = 166; // evolve.test.styledTextPerf
  static const int evolveTestStyledTextPerfResponse =
      167; // evolve.test.styledTextPerfResponse
  static const int evolveTestWireStats = 168; // evolve.test.wireStats
  static const int evolveTestWireStatsResponse =
      169; // evolve.test.wireStatsResponse
  static const int execute = 170; // execute
  static const int fillArcintintintintintint = 171; // fillArcintintintintintint
  static const int fillGradientRectangleintintintintboolean =
      172; // fillGradientRectangleintintintintboolean
  static const int fillOvalintintintint = 173; // fillOvalintintintint
  static const int fillPathPath = 174; // fillPathPath
  static const int fillPolygonint = 175; // fillPolygonint
  static const int fillRectangleintintintint = 176; // fillRectangleintintintint
  static const int fillRoundRectangleintintintintintint =
      177; // fillRoundRectangleintintintintintint
  static const int fontSizeRequest = 178; // fontSizeRequest
  static const int fontSizeResponse = 179; // fontSizeResponse
  static const int forward = 180; // forward
  static const int gcDispose = 181; // gcDispose
  static const int getCookie = 182; // getCookie
  static const int imageInit = 183; // imageInit
  static const int imageResult = 184; // imageResult
  static const int imageSizeRequest = 185; // imageSizeRequest
  static const int imageSizeResponse = 186; // imageSizeResponse
  static const int imageSnapshotResult = 187; // imageSnapshotResult
  static const int keyVerdict = 188; // key/verdict
  static const int keyVetoable = 189; // key/vetoable
  static const int locationchangingResult = 190; // locationchanging/result
  static const int menuVerdict = 191; // menu/verdict
  static const int modifyVerdict = 192; // modify/verdict
  static const int modifyVetoable = 193; // modify/vetoable
  static const int navigate = 194; // navigate
  static const int navigateRequest = 195; // navigateRequest
  static const int navigationState = 196; // navigationState
  static const int nextFrame = 197; // nextFrame
  static const int openMenu = 198; // openMenu
  static const int pixelsResult = 199; // pixelsResult
  static const int reload = 200; // reload
  static const int renderSnapshot = 201; // renderSnapshot
  static const int setCookie = 202; // setCookie
  static const int shown = 203; // shown
  static const int stop = 204; // stop
  static const int swing = 205; // swing
  static const int swingOpen = 206; // swing.open
  static const int swingIsland = 207; // swingIsland
  static const int swingIslandClose = 208; // swingIsland.close
  static const int swingIslandDispose = 209; // swingIsland.dispose
  static const int swingIslandIsland = 210; // swingIsland.island
  static const int swingIslandOpened = 211; // swingIsland.opened
  static const int swingIslandResize = 212; // swingIsland.resize
  static const int swingIslandSurface = 213; // swingIsland.surface
  static const int swingIslandTitle = 214; // swingIsland.title
  static const int swtEvolveBatch = 215; // swt.evolve.batch
  static const int swtEvolveFocus = 216; // swt.evolve.focus
  static const int swtEvolveFocusSeen = 217; // swt.evolve.focus.seen
  static const int swtEvolveFocusHolder = 218; // swt.evolve.focusHolder
  static const int swtEvolveProperties = 219; // swt.evolve.properties
  static const int swtEvolveTestRunUi = 221; // swt.evolve.test.runUi
  static const int swtEvolveTestRunUiResponse =
      222; // swt.evolve.test.runUiResponse
  static const int swtEvolveTimingBare = 223; // swt.evolve.timing.bare
  static const int swtEvolveTimingPing = 224; // swt.evolve.timing.ping
  static const int swtEvolveTimingPong = 225; // swt.evolve.timing.pong
  static const int swtEvolveUrlOpen = 226; // swt.evolve.url.open
  static const int swtEvolveWidgetRefresh = 227; // swt.evolve.widget.refresh
  static const int swtEvolveWire = 228; // swt.evolve.wire
  static const int traverseVerdict = 229; // traverse/verdict
  static const int traverseVetoable = 230; // traverse/vetoable
  static const int v = 231; // v
  static const int widgetSizeRequest = 232; // widgetSizeRequest
  static const int widgetSizeResponse = 233; // widgetSizeResponse
  static const int dnDRemoteDrag = 234; // DnD/remoteDrag
  static const int dnDRemoteDropNone = 235; // DnD/remoteDropNone
  static const int dragDetectDragCancel = 236; // DragDetect/dragCancel
  static const int dragDetectDragImage = 237; // DragDetect/dragImage
  static const int dragDetectDragMove = 238; // DragDetect/dragMove
  static const int dragDetectDragRelease = 239; // DragDetect/dragRelease
  static const int dragDetectRemoteAnswer = 240; // DragDetect/remoteAnswer
  static const int winDidEnterFullScreen = 241; // WinDidEnterFullScreen
  static const int winDidExitFullScreen = 242; // WinDidExitFullScreen
  static const int winExitFullScreen = 243; // WinExitFullScreen
  static const int winFullScreen = 244; // WinFullScreen
  static const int winFullScreenState = 245; // WinFullScreenState
  static const int anim = 246; // anim
  static const int callback = 247; // callback
  static const int rebuild = 248; // rebuild
  static const int subtree = 249; // subtree
  static const int subtreeRequest = 250; // subtree/request
  static const int transport = 251; // transport
  static const int transportRequest = 252; // transport/request
  static const int evolveTestBootTrace = 253; // evolve.test.bootTrace
  static const int evolveTestBootTraceResponse =
      254; // evolve.test.bootTraceResponse
}

/// Map keys by the number a map sent as tagged values names them with.
const Map<int, String> wireMapKeyNames = {
  1: 'RGB',
  2: 'RGBs',
  3: 'XORMode',
  4: '_d',
  5: '_gd',
  6: '_gr',
  7: '_r',
  8: '_s',
  9: 'a',
  10: 'accelerator',
  11: 'accumAlphaSize',
  12: 'accumBlueSize',
  13: 'accumGreenSize',
  14: 'accumRedSize',
  15: 'active',
  16: 'activeShellId',
  17: 'alignment',
  18: 'alpha',
  19: 'alphaSize',
  20: 'alwaysShowScrollBars',
  21: 'antialias',
  22: 'args',
  23: 'ascent',
  24: 'assets_path',
  25: 'availableTypeNames',
  26: 'averageCharWidth',
  27: 'averageLineHeight',
  28: 'b',
  29: 'background',
  30: 'backgroundImage',
  31: 'backgroundMode',
  32: 'backgroundPattern',
  33: 'backgrounds',
  470: 'bigger_icon_size',
  34: 'blinkRate',
  35: 'blockSelection',
  36: 'blockSelectionBounds',
  37: 'blue',
  38: 'blueSize',
  39: 'boldFont',
  40: 'boldItalicFont',
  41: 'borderColor',
  42: 'borderStyle',
  43: 'borderVisible',
  44: 'bottom',
  477: 'bottomLine',
  45: 'bottomMargin',
  46: 'bounds',
  47: 'browser',
  48: 'bufferScale',
  472: 'bufferScaleY',
  49: 'bulletTexts',
  50: 'bulletsIndices',
  51: 'button',
  52: 'buttonLabels',
  53: 'cacheBottom',
  54: 'cacheHeight',
  55: 'cacheLeft',
  56: 'cacheRight',
  57: 'cacheTop',
  58: 'cacheWidth',
  59: 'caret',
  60: 'caretOffset',
  61: 'caretOffsets',
  62: 'caretPosition',
  63: 'charCount',
  64: 'character',
  65: 'checked',
  66: 'chevronVisible',
  67: 'children',
  68: 'clientHeight',
  69: 'clientWidth',
  70: 'clientX',
  71: 'clientY',
  72: 'clipping',
  73: 'clippingPath',
  74: 'clippingRects',
  75: 'clippingText',
  76: 'color1',
  77: 'color2',
  78: 'column',
  79: 'columnOrder',
  80: 'columns',
  81: 'config',
  82: 'content',
  83: 'control',
  84: 'cookie',
  85: 'count',
  86: 'csd_maximize',
  87: 'csd_os',
  88: 'csd_placement',
  89: 'csd_titlebar_color',
  478: 'ctabfolder_chevron_after_tabs',
  90: 'ctabfolder_tab_scroll',
  91: 'ctabfolder_topright_auto_hide',
  92: 'ctabfolder_visible_controls',
  93: 'currentDataTypeId',
  94: 'currentHeight',
  95: 'currentHhint',
  96: 'currentWhint',
  97: 'currentWidth',
  98: 'cursor',
  99: 'cursorStyle',
  479: 'curveLine',
  100: 'custom_tooltip',
  101: 'damage',
  102: 'data',
  103: 'day',
  104: 'decimal_separator',
  105: 'decorations_align',
  106: 'defaultHeight',
  107: 'defaultHhint',
  108: 'defaultWhint',
  109: 'defaultWidth',
  110: 'denominator',
  111: 'depth',
  112: 'depthSize',
  113: 'descent',
  114: 'detail',
  115: 'dialogs',
  116: 'digits',
  117: 'direct',
  474: 'dirtyIndicatorStyle',
  118: 'disable_control_gc_overlay',
  119: 'disable_evolve_icons',
  120: 'disable_hover_zoom',
  121: 'disable_swt_canvas_colors',
  122: 'disabledImage',
  123: 'doit',
  124: 'doubleBuffer',
  125: 'doubleClickEnabled',
  126: 'double_click_timeout_ms',
  127: 'dragDetect',
  128: 'dragImage',
  129: 'dragSource',
  130: 'dragSourceId',
  131: 'dropTargetId',
  132: 'echoCharacter',
  133: 'editTexts',
  134: 'editable',
  135: 'editor',
  136: 'editors',
  137: 'edits',
  138: 'effectsVisible',
  139: 'elements',
  140: 'enabled',
  141: 'end',
  142: 'endPage',
  143: 'endX',
  144: 'endY',
  145: 'error',
  146: 'exclude',
  147: 'expandHorizontal',
  148: 'expandVertical',
  149: 'expanded',
  150: 'expires',
  151: 'feedback',
  152: 'fileName',
  153: 'fileNames',
  154: 'filename',
  155: 'fillRule',
  156: 'filterExtensions',
  157: 'filterIndex',
  158: 'filterNames',
  159: 'filterPath',
  160: 'fixedLineHeight',
  161: 'fixedPitch',
  162: 'flags',
  163: 'focus',
  164: 'focusHolderId',
  165: 'focus_indicators',
  166: 'focused',
  167: 'font',
  168: 'fontData',
  169: 'fontList',
  170: 'fontStyle',
  171: 'font_point_scale',
  172: 'fonts',
  173: 'footer',
  174: 'force_theme',
  175: 'foreground',
  176: 'foregroundPattern',
  177: 'foregrounds',
  178: 'fullRepaint',
  179: 'fullScreen',
  180: 'functionNames',
  181: 'g',
  182: 'gc',
  183: 'gc_icons_replacement',
  184: 'grabExcessHorizontalSpace',
  185: 'grabExcessVerticalSpace',
  186: 'gradientColors',
  187: 'gradientPercents',
  188: 'gradientVertical',
  189: 'grayed',
  190: 'green',
  191: 'greenSize',
  192: 'hHint',
  193: 'handle',
  194: 'hasLinks',
  195: 'hasOwnBackground',
  196: 'header',
  197: 'headerBackground',
  198: 'headerForeground',
  199: 'headerHeight',
  200: 'headerVisible',
  201: 'headers',
  202: 'height',
  203: 'heightHint',
  204: 'highlight',
  205: 'highlightEnabled',
  206: 'horizontalAlignment',
  207: 'horizontalBar',
  208: 'horizontalIndent',
  209: 'horizontalPixel',
  210: 'horizontalSpan',
  211: 'hotImage',
  212: 'hotspotX',
  213: 'hotspotY',
  214: 'hours',
  215: 'hoverFills',
  216: 'hover_delay_ms',
  217: 'id',
  218: 'idleRunning',
  219: 'image',
  220: 'imageData',
  221: 'image_disable_icons_replacement',
  222: 'images',
  223: 'increment',
  224: 'indent',
  225: 'index',
  226: 'inheritsBackground',
  227: 'interpolation',
  228: 'isVisited',
  229: 'italicFont',
  230: 'item',
  231: 'itemCount',
  232: 'itemId',
  233: 'itemOrder',
  234: 'itemSizes',
  235: 'itemTooltips',
  236: 'items',
  237: 'javascriptEnabled',
  238: 'jobName',
  239: 'justify',
  240: 'keptText',
  241: 'keyCode',
  242: 'keyLocation',
  243: 'left',
  244: 'leftMargin',
  245: 'length',
  246: 'lineCap',
  247: 'lineCount',
  248: 'lineDash',
  249: 'lineDashOffset',
  250: 'lineJoin',
  251: 'lineLabels',
  252: 'lineSizes',
  253: 'lineSpacing',
  254: 'lineSpacingComputing',
  255: 'lineSpacings',
  256: 'lineStyle',
  257: 'lineWidth',
  258: 'lines',
  259: 'linesInAverageLineHeight',
  260: 'linesVisible',
  261: 'linkForeground',
  262: 'listVisible',
  263: 'localFileBase',
  264: 'localFilePath',
  265: 'localFileText',
  266: 'locale',
  267: 'location',
  268: 'magnification',
  269: 'mainShellId',
  270: 'map',
  271: 'marginColor',
  272: 'maxWidth',
  273: 'maxWidthLineIndex',
  274: 'maximizeVisible',
  275: 'maximized',
  276: 'maximizedControl',
  277: 'maximum',
  278: 'maximumSize',
  279: 'menu',
  280: 'menuBar',
  281: 'menuStripHeight',
  282: 'message',
  283: 'metrics',
  284: 'minHeight',
  285: 'minWidth',
  286: 'minimizeVisible',
  287: 'minimized',
  288: 'minimum',
  289: 'minimumCharacters',
  290: 'minimumHeight',
  291: 'minimumSize',
  292: 'minimumWidth',
  293: 'minutes',
  294: 'modified',
  295: 'month',
  296: 'moveable',
  297: 'name',
  298: 'needed',
  299: 'notification_popup',
  300: 'numerator',
  301: 'offset',
  302: 'operations',
  303: 'orientation',
  304: 'origin',
  305: 'originRequest',
  306: 'overwrite',
  307: 'ownMenuStrip',
  308: 'ownerDrawItemHeight',
  309: 'pageIncrement',
  475: 'paintedImages',
  310: 'paintedTexts',
  311: 'pathData',
  312: 'pixels',
  313: 'points',
  314: 'popups',
  315: 'port',
  316: 'postData',
  317: 'preferredSize',
  318: 'preserve_icon_colors',
  319: 'primary',
  320: 'printLineBackground',
  321: 'printLineNumbers',
  322: 'printTextBackground',
  323: 'printTextFontStyle',
  324: 'printTextForeground',
  325: 'printToFile',
  326: 'print_move',
  327: 'push_button_margin',
  328: 'r',
  329: 'ranges',
  330: 'rects',
  331: 'red',
  332: 'redSize',
  333: 'redrawLines',
  334: 'ref',
  335: 'region',
  480: 'regional_locale',
  336: 'regularFont',
  337: 'remoteRef',
  338: 'renderer',
  339: 'replaced',
  340: 'reqId',
  341: 'resizable',
  342: 'result',
  343: 'retain',
  344: 'right',
  345: 'rightMargin',
  346: 'rise',
  347: 'rotation',
  348: 'sampleBuffers',
  349: 'samples',
  350: 'sashWidth',
  471: 'scale_vertical_minimum_at_top',
  351: 'scope',
  352: 'script',
  353: 'seconds',
  354: 'segments',
  355: 'segmentsChars',
  356: 'selectedImageVisible',
  357: 'selection',
  358: 'selectionBackground',
  359: 'selectionBarThickness',
  360: 'selectionBgImage',
  361: 'selectionFills',
  362: 'selectionForeground',
  363: 'selectionRanges',
  481: 'separator',
  364: 'seq',
  365: 'shareContext',
  366: 'shells',
  367: 'showChevron',
  368: 'showClose',
  476: 'showDirty',
  369: 'showListPopupSeq',
  370: 'show_scaling_control',
  371: 'show_theme_color_palette',
  372: 'showing',
  373: 'single',
  374: 'sortColumn',
  375: 'sortDirection',
  376: 'source',
  377: 'spacing',
  378: 'start',
  379: 'startPage',
  380: 'startX',
  381: 'startY',
  382: 'state',
  383: 'stateMask',
  384: 'stencilSize',
  385: 'stereo',
  386: 'strikeout',
  387: 'strikeoutColor',
  473: 'stripLayoutSeq',
  388: 'style',
  389: 'styleCount',
  390: 'styleIndex',
  391: 'styledText',
  392: 'styles',
  393: 'stylesSet',
  394: 'stylesSetCount',
  395: 'svgContent',
  396: 'swt',
  397: 'systemMenu',
  398: 'system_menu_bar',
  399: 'tabHeight',
  400: 'tabLength',
  401: 'tabPosition',
  402: 'tabStop',
  403: 'tabStops',
  404: 'tabWidth',
  405: 'tabs',
  406: 'text',
  407: 'textFonts',
  408: 'textLimit',
  409: 'textOrigins',
  410: 'textStrings',
  411: 'texts',
  412: 'theme_color',
  413: 'theme_colors_by_widget',
  414: 'theme_name',
  415: 'themedCanvas',
  416: 'thumb',
  417: 'time',
  418: 'toolTipText',
  419: 'tooltips',
  420: 'top',
  421: 'topIndex',
  422: 'topItem',
  423: 'topMargin',
  424: 'topPixel',
  425: 'topRight',
  426: 'topRightAlignment',
  427: 'touches',
  428: 'tracking',
  429: 'transform',
  430: 'transparent_menu_check_box',
  431: 'type',
  432: 'types',
  433: 'ui_zoom',
  434: 'underline',
  435: 'underlineColor',
  436: 'underlineStyle',
  437: 'unselectedCloseVisible',
  438: 'unselectedImageVisible',
  439: 'url',
  440: 'use_special_dropdown_button',
  441: 'use_swt_border',
  442: 'use_swt_colors',
  443: 'use_swt_font_colors',
  444: 'use_swt_font_colors_by_widget',
  445: 'use_swt_fonts',
  446: 'value',
  447: 'verdict',
  448: 'verticalAlignment',
  449: 'verticalBar',
  450: 'verticalIndent',
  451: 'verticalSpan',
  452: 'visible',
  453: 'wHint',
  454: 'warnings',
  455: 'weights',
  456: 'widget',
  457: 'width',
  458: 'widthHint',
  459: 'windowId',
  460: 'windowedShellIds',
  461: 'wordWrap',
  462: 'wrapIndent',
  463: 'wrapIndices',
  464: 'x',
  465: 'xDirection',
  466: 'y',
  467: 'yDirection',
  468: 'year',
  469: 'zoom',
};

/// The empty value of each widget type a frame can name.
const Map<String, VWidget Function()> _widgetValues = {
  "Shell": VShell.new,
  "AnimatedProgress": VAnimatedProgress.new,
  "CLabel": VCLabel.new,
  "Decorations": VDecorations.new,
  "GLCanvas": VGLCanvas.new,
  "StyledText": VStyledText.new,
  "TableCursor": VTableCursor.new,
  "TreeCursor": VTreeCursor.new,
  "Browser": VBrowser.new,
  "CBanner": VCBanner.new,
  "CCombo": VCCombo.new,
  "CTabFolder": VCTabFolder.new,
  "Canvas": VCanvas.new,
  "Combo": VCombo.new,
  "CoolBar": VCoolBar.new,
  "DateTime": VDateTime.new,
  "ExpandBar": VExpandBar.new,
  "Group": VGroup.new,
  "SashForm": VSashForm.new,
  "ScrolledComposite": VScrolledComposite.new,
  "Spinner": VSpinner.new,
  "TabFolder": VTabFolder.new,
  "Table": VTable.new,
  "ToolBar": VToolBar.new,
  "Tree": VTree.new,
  "ViewForm": VViewForm.new,
  "Composite": VComposite.new,
  "List": VList.new,
  "Text": VText.new,
  "Button": VButton.new,
  "Label": VLabel.new,
  "Link": VLink.new,
  "ProgressBar": VProgressBar.new,
  "Sash": VSash.new,
  "Scale": VScale.new,
  "Slider": VSlider.new,
  "CTabItem": VCTabItem.new,
  "CoolItem": VCoolItem.new,
  "ExpandItem": VExpandItem.new,
  "MenuItem": VMenuItem.new,
  "TabItem": VTabItem.new,
  "TableColumn": VTableColumn.new,
  "TableItem": VTableItem.new,
  "TaskItem": VTaskItem.new,
  "ToolItem": VToolItem.new,
  "TrayItem": VTrayItem.new,
  "TreeColumn": VTreeColumn.new,
  "TreeItem": VTreeItem.new,
  "Caret": VCaret.new,
  "DragSource": VDragSource.new,
  "DropTarget": VDropTarget.new,
  "GC": VGC.new,
  "IME": VIME.new,
  "Menu": VMenu.new,
  "ScrollBar": VScrollBar.new,
  "TableEditor": VTableEditor.new,
  "TaskBar": VTaskBar.new,
  "ToolTip": VToolTip.new,
  "Tracker": VTracker.new,
  "Tray": VTray.new,
  "TreeEditor": VTreeEditor.new,
  "ControlEditor": VControlEditor.new,
  "MainComposite": VComposite.new,
  "EwtWidget": VComposite.new,
  "SwingIsland": VComposite.new,
  "SideBar": VComposite.new,
  "MainToolbar": VComposite.new,
  "StatusBar": VComposite.new,
};

/// An empty value for a widget of type [swt], keeping that name: several names render as one class.
VWidget? newWidgetValue(String swt) {
  final create = _widgetValues[swt];
  return create == null ? null : (create()..swt = swt);
}
