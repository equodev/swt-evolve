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
const int wireFingerprint = -1911255749;

/// Property numbers, for code that reads a frame by hand.
abstract final class WireKey {
  static const int k_Accessible_control = 1;
  static const int k_Accessible_focus = 2;
  static const int k_BorderData_hHint = 3;
  static const int k_BorderData_region = 4;
  static const int k_BorderData_wHint = 5;
  static const int k_Browser_functionNames = 6;
  static const int k_Browser_javascriptEnabled = 7;
  static const int k_Browser_text = 8;
  static const int k_Browser_url = 9;
  static const int k_BrowserFunction_browser = 10;
  static const int k_BrowserFunction_name = 11;
  static const int k_Button_alignment = 12;
  static const int k_Button_grayed = 13;
  static const int k_Button_image = 14;
  static const int k_Button_primary = 15;
  static const int k_Button_selection = 16;
  static const int k_Button_text = 17;
  static const int k_CBanner_bottomLine = 18;
  static const int k_CBanner_curveLine = 19;
  static const int k_CCombo_alignment = 20;
  static const int k_CCombo_editable = 21;
  static const int k_CCombo_itemTooltips = 22;
  static const int k_CCombo_items = 23;
  static const int k_CCombo_listVisible = 24;
  static const int k_CCombo_selection = 25;
  static const int k_CCombo_text = 26;
  static const int k_CCombo_textLimit = 27;
  static const int k_CLabel_alignment = 28;
  static const int k_CLabel_backgroundImage = 29;
  static const int k_CLabel_bottomMargin = 30;
  static const int k_CLabel_gradientColors = 31;
  static const int k_CLabel_gradientPercents = 32;
  static const int k_CLabel_gradientVertical = 33;
  static const int k_CLabel_image = 34;
  static const int k_CLabel_leftMargin = 35;
  static const int k_CLabel_rightMargin = 36;
  static const int k_CLabel_text = 37;
  static const int k_CLabel_topMargin = 38;
  static const int k_CTabFolder_borderVisible = 39;
  static const int k_CTabFolder_chevronVisible = 40;
  static const int k_CTabFolder_dirtyIndicatorStyle = 41;
  static const int k_CTabFolder_highlight = 42;
  static const int k_CTabFolder_highlightEnabled = 43;
  static const int k_CTabFolder_items = 44;
  static const int k_CTabFolder_maximizeVisible = 45;
  static const int k_CTabFolder_maximized = 46;
  static const int k_CTabFolder_minimizeVisible = 47;
  static const int k_CTabFolder_minimized = 48;
  static const int k_CTabFolder_minimumCharacters = 49;
  static const int k_CTabFolder_selectedImageVisible = 50;
  static const int k_CTabFolder_selection = 51;
  static const int k_CTabFolder_selectionBackground = 52;
  static const int k_CTabFolder_selectionBarThickness = 53;
  static const int k_CTabFolder_selectionBgImage = 54;
  static const int k_CTabFolder_selectionForeground = 55;
  static const int k_CTabFolder_showChevron = 56;
  static const int k_CTabFolder_showListPopupSeq = 57;
  static const int k_CTabFolder_single = 58;
  static const int k_CTabFolder_stripLayoutSeq = 59;
  static const int k_CTabFolder_tabHeight = 60;
  static const int k_CTabFolder_tabPosition = 61;
  static const int k_CTabFolder_topRight = 62;
  static const int k_CTabFolder_topRightAlignment = 63;
  static const int k_CTabFolder_unselectedCloseVisible = 64;
  static const int k_CTabFolder_unselectedImageVisible = 65;
  static const int k_CTabItem_control = 66;
  static const int k_CTabItem_font = 67;
  static const int k_CTabItem_foreground = 68;
  static const int k_CTabItem_selectionForeground = 69;
  static const int k_CTabItem_showClose = 70;
  static const int k_CTabItem_showDirty = 71;
  static const int k_CTabItem_showing = 72;
  static const int k_CTabItem_toolTipText = 73;
  static const int k_Canvas_caret = 74;
  static const int k_Caret_blinkRate = 75;
  static const int k_Caret_bounds = 76;
  static const int k_Caret_focused = 77;
  static const int k_Caret_image = 78;
  static const int k_Caret_visible = 79;
  static const int k_Clipboard_availableTypeNames = 80;
  static const int k_Color_a = 81;
  static const int k_Color_b = 82;
  static const int k_Color_g = 83;
  static const int k_Color_r = 84;
  static const int k_ColorDialog_RGB = 85;
  static const int k_ColorDialog_RGBs = 86;
  static const int k_Combo_caretPosition = 87;
  static const int k_Combo_items = 88;
  static const int k_Combo_listVisible = 89;
  static const int k_Combo_text = 90;
  static const int k_Combo_textLimit = 91;
  static const int k_Composite_backgroundMode = 92;
  static const int k_Composite_children = 93;
  static const int k_ConfigFlags_assets_path = 94;
  static const int k_ConfigFlags_bigger_icon_size = 95;
  static const int k_ConfigFlags_csd_maximize = 96;
  static const int k_ConfigFlags_csd_os = 97;
  static const int k_ConfigFlags_csd_placement = 98;
  static const int k_ConfigFlags_csd_titlebar_color = 99;
  static const int k_ConfigFlags_ctabfolder_chevron_after_tabs = 100;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 101;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 102;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 103;
  static const int k_ConfigFlags_custom_tooltip = 104;
  static const int k_ConfigFlags_decimal_separator = 105;
  static const int k_ConfigFlags_decorations_align = 106;
  static const int k_ConfigFlags_disable_control_gc_overlay = 107;
  static const int k_ConfigFlags_disable_evolve_icons = 108;
  static const int k_ConfigFlags_disable_hover_zoom = 109;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 110;
  static const int k_ConfigFlags_double_click_timeout_ms = 111;
  static const int k_ConfigFlags_focus_indicators = 112;
  static const int k_ConfigFlags_font_point_scale = 113;
  static const int k_ConfigFlags_force_theme = 114;
  static const int k_ConfigFlags_gc_icons_replacement = 115;
  static const int k_ConfigFlags_hover_delay_ms = 116;
  static const int k_ConfigFlags_image_disable_icons_replacement = 117;
  static const int k_ConfigFlags_locale = 118;
  static const int k_ConfigFlags_notification_popup = 119;
  static const int k_ConfigFlags_preserve_icon_colors = 120;
  static const int k_ConfigFlags_print_move = 121;
  static const int k_ConfigFlags_push_button_margin = 122;
  static const int k_ConfigFlags_scale_vertical_minimum_at_top = 123;
  static const int k_ConfigFlags_show_scaling_control = 124;
  static const int k_ConfigFlags_show_theme_color_palette = 125;
  static const int k_ConfigFlags_system_menu_bar = 126;
  static const int k_ConfigFlags_theme_color = 127;
  static const int k_ConfigFlags_theme_colors_by_widget = 128;
  static const int k_ConfigFlags_theme_name = 129;
  static const int k_ConfigFlags_transparent_menu_check_box = 130;
  static const int k_ConfigFlags_ui_zoom = 131;
  static const int k_ConfigFlags_use_special_dropdown_button = 132;
  static const int k_ConfigFlags_use_swt_border = 133;
  static const int k_ConfigFlags_use_swt_colors = 134;
  static const int k_ConfigFlags_use_swt_font_colors = 135;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 136;
  static const int k_ConfigFlags_use_swt_fonts = 137;
  static const int k_Control_background = 138;
  static const int k_Control_backgroundImage = 139;
  static const int k_Control_bounds = 140;
  static const int k_Control_cursor = 141;
  static const int k_Control_dragDetect = 142;
  static const int k_Control_dragSource = 143;
  static const int k_Control_dragSourceId = 144;
  static const int k_Control_dropTargetId = 145;
  static const int k_Control_enabled = 146;
  static const int k_Control_font = 147;
  static const int k_Control_foreground = 148;
  static const int k_Control_hasOwnBackground = 149;
  static const int k_Control_inheritsBackground = 150;
  static const int k_Control_menu = 151;
  static const int k_Control_region = 152;
  static const int k_Control_tabStop = 153;
  static const int k_Control_toolTipText = 154;
  static const int k_Control_visible = 155;
  static const int k_ControlEditor_editor = 156;
  static const int k_CoolBar_itemOrder = 157;
  static const int k_CoolBar_itemSizes = 158;
  static const int k_CoolBar_items = 159;
  static const int k_CoolBar_wrapIndices = 160;
  static const int k_CoolItem_control = 161;
  static const int k_CoolItem_preferredSize = 162;
  static const int k_Cursor_cursorStyle = 163;
  static const int k_Cursor_hotspotX = 164;
  static const int k_Cursor_hotspotY = 165;
  static const int k_Cursor_image = 166;
  static const int k_DateTime_day = 167;
  static const int k_DateTime_hours = 168;
  static const int k_DateTime_minutes = 169;
  static const int k_DateTime_month = 170;
  static const int k_DateTime_seconds = 171;
  static const int k_DateTime_year = 172;
  static const int k_Decorations_menuBar = 173;
  static const int k_Decorations_menuStripHeight = 174;
  static const int k_Decorations_ownMenuStrip = 175;
  static const int k_Decorations_text = 176;
  static const int k_Device_fontList = 177;
  static const int k_Device_tracking = 178;
  static const int k_Device_warnings = 179;
  static const int k_Dialog_id = 180;
  static const int k_Dialog_style = 181;
  static const int k_Dialog_swt = 182;
  static const int k_Dialog_text = 183;
  static const int k_DirectoryDialog_filterPath = 184;
  static const int k_DirectoryDialog_message = 185;
  static const int k_Display_activeShellId = 186;
  static const int k_Display_config = 187;
  static const int k_Display_focusHolderId = 188;
  static const int k_Display_mainShellId = 189;
  static const int k_Display_popups = 190;
  static const int k_Display_shells = 191;
  static const int k_Display_systemMenu = 192;
  static const int k_Display_tooltips = 193;
  static const int k_Display_windowedShellIds = 194;
  static const int k_Event_button = 195;
  static const int k_Event_character = 196;
  static const int k_Event_count = 197;
  static const int k_Event_currentDataTypeId = 198;
  static const int k_Event_detail = 199;
  static const int k_Event_doit = 200;
  static const int k_Event_dragImage = 201;
  static const int k_Event_end = 202;
  static const int k_Event_feedback = 203;
  static const int k_Event_gc = 204;
  static const int k_Event_height = 205;
  static const int k_Event_index = 206;
  static const int k_Event_item = 207;
  static const int k_Event_itemId = 208;
  static const int k_Event_keyCode = 209;
  static const int k_Event_keyLocation = 210;
  static const int k_Event_magnification = 211;
  static const int k_Event_operations = 212;
  static const int k_Event_rotation = 213;
  static const int k_Event_segments = 214;
  static const int k_Event_segmentsChars = 215;
  static const int k_Event_start = 216;
  static const int k_Event_stateMask = 217;
  static const int k_Event_text = 218;
  static const int k_Event_time = 219;
  static const int k_Event_touches = 220;
  static const int k_Event_type = 221;
  static const int k_Event_widget = 222;
  static const int k_Event_width = 223;
  static const int k_Event_x = 224;
  static const int k_Event_xDirection = 225;
  static const int k_Event_y = 226;
  static const int k_Event_yDirection = 227;
  static const int k_ExpandBar_items = 228;
  static const int k_ExpandBar_spacing = 229;
  static const int k_ExpandItem_control = 230;
  static const int k_ExpandItem_expanded = 231;
  static const int k_ExpandItem_headerHeight = 232;
  static const int k_ExpandItem_height = 233;
  static const int k_FileDialog_fileName = 234;
  static const int k_FileDialog_fileNames = 235;
  static const int k_FileDialog_filterExtensions = 236;
  static const int k_FileDialog_filterIndex = 237;
  static const int k_FileDialog_filterNames = 238;
  static const int k_FileDialog_filterPath = 239;
  static const int k_FileDialog_overwrite = 240;
  static const int k_Font_fontData = 241;
  static const int k_FontData_height = 242;
  static const int k_FontData_name = 243;
  static const int k_FontData_style = 244;
  static const int k_FontDialog_RGB = 245;
  static const int k_FontDialog_effectsVisible = 246;
  static const int k_FontDialog_fontData = 247;
  static const int k_FontDialog_fontList = 248;
  static const int k_FormAttachment_alignment = 249;
  static const int k_FormAttachment_control = 250;
  static const int k_FormAttachment_denominator = 251;
  static const int k_FormAttachment_numerator = 252;
  static const int k_FormAttachment_offset = 253;
  static const int k_FormData_bottom = 254;
  static const int k_FormData_cacheBottom = 255;
  static const int k_FormData_cacheHeight = 256;
  static const int k_FormData_cacheLeft = 257;
  static const int k_FormData_cacheRight = 258;
  static const int k_FormData_cacheTop = 259;
  static const int k_FormData_cacheWidth = 260;
  static const int k_FormData_currentHeight = 261;
  static const int k_FormData_currentHhint = 262;
  static const int k_FormData_currentWhint = 263;
  static const int k_FormData_currentWidth = 264;
  static const int k_FormData_defaultHeight = 265;
  static const int k_FormData_defaultHhint = 266;
  static const int k_FormData_defaultWhint = 267;
  static const int k_FormData_defaultWidth = 268;
  static const int k_FormData_height = 269;
  static const int k_FormData_isVisited = 270;
  static const int k_FormData_left = 271;
  static const int k_FormData_needed = 272;
  static const int k_FormData_right = 273;
  static const int k_FormData_top = 274;
  static const int k_FormData_width = 275;
  static const int k_GC_XORMode = 276;
  static const int k_GC_alpha = 277;
  static const int k_GC_antialias = 278;
  static const int k_GC_background = 279;
  static const int k_GC_backgroundPattern = 280;
  static const int k_GC_bufferScale = 281;
  static const int k_GC_bufferScaleY = 282;
  static const int k_GC_clipping = 283;
  static const int k_GC_clippingPath = 284;
  static const int k_GC_clippingRects = 285;
  static const int k_GC_clippingText = 286;
  static const int k_GC_fillRule = 287;
  static const int k_GC_font = 288;
  static const int k_GC_foreground = 289;
  static const int k_GC_foregroundPattern = 290;
  static const int k_GC_hoverFills = 291;
  static const int k_GC_interpolation = 292;
  static const int k_GC_lineCap = 293;
  static const int k_GC_lineDash = 294;
  static const int k_GC_lineDashOffset = 295;
  static const int k_GC_lineJoin = 296;
  static const int k_GC_lineStyle = 297;
  static const int k_GC_lineWidth = 298;
  static const int k_GC_selectionFills = 299;
  static const int k_GC_style = 300;
  static const int k_GC_themedCanvas = 301;
  static const int k_GC_transform = 302;
  static const int k_GLData_accumAlphaSize = 303;
  static const int k_GLData_accumBlueSize = 304;
  static const int k_GLData_accumGreenSize = 305;
  static const int k_GLData_accumRedSize = 306;
  static const int k_GLData_alphaSize = 307;
  static const int k_GLData_blueSize = 308;
  static const int k_GLData_depthSize = 309;
  static const int k_GLData_doubleBuffer = 310;
  static const int k_GLData_greenSize = 311;
  static const int k_GLData_redSize = 312;
  static const int k_GLData_sampleBuffers = 313;
  static const int k_GLData_samples = 314;
  static const int k_GLData_shareContext = 315;
  static const int k_GLData_stencilSize = 316;
  static const int k_GLData_stereo = 317;
  static const int k_GlyphMetrics_ascent = 318;
  static const int k_GlyphMetrics_descent = 319;
  static const int k_GlyphMetrics_width = 320;
  static const int k_GridData_cacheHeight = 321;
  static const int k_GridData_cacheWidth = 322;
  static const int k_GridData_currentHeight = 323;
  static const int k_GridData_currentHhint = 324;
  static const int k_GridData_currentWhint = 325;
  static const int k_GridData_currentWidth = 326;
  static const int k_GridData_defaultHeight = 327;
  static const int k_GridData_defaultHhint = 328;
  static const int k_GridData_defaultWhint = 329;
  static const int k_GridData_defaultWidth = 330;
  static const int k_GridData_exclude = 331;
  static const int k_GridData_grabExcessHorizontalSpace = 332;
  static const int k_GridData_grabExcessVerticalSpace = 333;
  static const int k_GridData_heightHint = 334;
  static const int k_GridData_horizontalAlignment = 335;
  static const int k_GridData_horizontalIndent = 336;
  static const int k_GridData_horizontalSpan = 337;
  static const int k_GridData_minimumHeight = 338;
  static const int k_GridData_minimumWidth = 339;
  static const int k_GridData_verticalAlignment = 340;
  static const int k_GridData_verticalIndent = 341;
  static const int k_GridData_verticalSpan = 342;
  static const int k_GridData_widthHint = 343;
  static const int k_Group_text = 344;
  static const int k_Image_filename = 345;
  static const int k_Image_height = 346;
  static const int k_Image_imageData = 347;
  static const int k_Image_remoteRef = 348;
  static const int k_Image_svgContent = 349;
  static const int k_Image_width = 350;
  static const int k_ImageData_data = 351;
  static const int k_ImageData_depth = 352;
  static const int k_ImageData_height = 353;
  static const int k_ImageData_width = 354;
  static const int k_Item_image = 355;
  static const int k_Item_text = 356;
  static const int k_Label_image = 357;
  static const int k_Label_text = 358;
  static const int k_LineInfo_alignment = 359;
  static const int k_LineInfo_background = 360;
  static const int k_LineInfo_flags = 361;
  static const int k_LineInfo_indent = 362;
  static const int k_LineInfo_justify = 363;
  static const int k_LineInfo_segments = 364;
  static const int k_LineInfo_segmentsChars = 365;
  static const int k_LineInfo_tabStops = 366;
  static const int k_LineInfo_verticalIndent = 367;
  static const int k_LineInfo_wrapIndent = 368;
  static const int k_LineSizeInfo_height = 369;
  static const int k_LineSizeInfo_width = 370;
  static const int k_Link_linkForeground = 371;
  static const int k_Link_text = 372;
  static const int k_List_items = 373;
  static const int k_List_selection = 374;
  static const int k_List_topIndex = 375;
  static const int k_Menu_enabled = 376;
  static const int k_Menu_items = 377;
  static const int k_Menu_location = 378;
  static const int k_Menu_orientation = 379;
  static const int k_Menu_visible = 380;
  static const int k_MenuItem_accelerator = 381;
  static const int k_MenuItem_enabled = 382;
  static const int k_MenuItem_menu = 383;
  static const int k_MenuItem_selection = 384;
  static const int k_MenuItem_toolTipText = 385;
  static const int k_MessageBox_buttonLabels = 386;
  static const int k_MessageBox_message = 387;
  static const int k_Monitor_clientHeight = 388;
  static const int k_Monitor_clientWidth = 389;
  static const int k_Monitor_clientX = 390;
  static const int k_Monitor_clientY = 391;
  static const int k_Monitor_handle = 392;
  static const int k_Monitor_height = 393;
  static const int k_Monitor_width = 394;
  static const int k_Monitor_x = 395;
  static const int k_Monitor_y = 396;
  static const int k_Monitor_zoom = 397;
  static const int k_PDFDocument_height = 398;
  static const int k_PDFDocument_width = 399;
  static const int k_Path_pathData = 400;
  static const int k_Path_textFonts = 401;
  static const int k_Path_textOrigins = 402;
  static const int k_Path_textStrings = 403;
  static const int k_PathData_points = 404;
  static const int k_PathData_types = 405;
  static const int k_Pattern_color1 = 406;
  static const int k_Pattern_color2 = 407;
  static const int k_Pattern_endX = 408;
  static const int k_Pattern_endY = 409;
  static const int k_Pattern_image = 410;
  static const int k_Pattern_startX = 411;
  static const int k_Pattern_startY = 412;
  static const int k_Point_x = 413;
  static const int k_Point_y = 414;
  static const int k_PopupList_font = 415;
  static const int k_PopupList_items = 416;
  static const int k_PopupList_minimumWidth = 417;
  static const int k_PopupList_selection = 418;
  static const int k_PrintDialog_endPage = 419;
  static const int k_PrintDialog_printToFile = 420;
  static const int k_PrintDialog_scope = 421;
  static const int k_PrintDialog_startPage = 422;
  static const int k_ProgressBar_maximum = 423;
  static const int k_ProgressBar_minimum = 424;
  static const int k_ProgressBar_selection = 425;
  static const int k_RGB_blue = 426;
  static const int k_RGB_green = 427;
  static const int k_RGB_red = 428;
  static const int k_Rectangle_height = 429;
  static const int k_Rectangle_width = 430;
  static const int k_Rectangle_x = 431;
  static const int k_Rectangle_y = 432;
  static const int k_Region_rects = 433;
  static const int k_RowData_exclude = 434;
  static const int k_RowData_height = 435;
  static const int k_RowData_width = 436;
  static const int k_SashForm_maximizedControl = 437;
  static const int k_SashForm_sashWidth = 438;
  static const int k_SashForm_weights = 439;
  static const int k_Scale_maximum = 440;
  static const int k_Scale_minimum = 441;
  static const int k_Scale_selection = 442;
  static const int k_ScrollBar_enabled = 443;
  static const int k_ScrollBar_increment = 444;
  static const int k_ScrollBar_maximum = 445;
  static const int k_ScrollBar_minimum = 446;
  static const int k_ScrollBar_pageIncrement = 447;
  static const int k_ScrollBar_selection = 448;
  static const int k_ScrollBar_thumb = 449;
  static const int k_ScrollBar_visible = 450;
  static const int k_Scrollable_horizontalBar = 451;
  static const int k_Scrollable_verticalBar = 452;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 453;
  static const int k_ScrolledComposite_content = 454;
  static const int k_ScrolledComposite_expandHorizontal = 455;
  static const int k_ScrolledComposite_expandVertical = 456;
  static const int k_ScrolledComposite_minHeight = 457;
  static const int k_ScrolledComposite_minWidth = 458;
  static const int k_ScrolledComposite_origin = 459;
  static const int k_ScrolledComposite_originRequest = 460;
  static const int k_Shell_alpha = 461;
  static const int k_Shell_dialogs = 462;
  static const int k_Shell_fullScreen = 463;
  static const int k_Shell_maximumSize = 464;
  static const int k_Shell_minimumSize = 465;
  static const int k_Shell_modified = 466;
  static const int k_Slider_increment = 467;
  static const int k_Slider_maximum = 468;
  static const int k_Slider_minimum = 469;
  static const int k_Slider_pageIncrement = 470;
  static const int k_Slider_selection = 471;
  static const int k_Slider_thumb = 472;
  static const int k_Spinner_digits = 473;
  static const int k_Spinner_increment = 474;
  static const int k_Spinner_keptText = 475;
  static const int k_Spinner_maximum = 476;
  static const int k_Spinner_minimum = 477;
  static const int k_Spinner_pageIncrement = 478;
  static const int k_Spinner_selection = 479;
  static const int k_Spinner_textLimit = 480;
  static const int k_StyleRange_fontStyle = 481;
  static const int k_StyleRange_length = 482;
  static const int k_StyleRange_start = 483;
  static const int k_StyledText_alignment = 484;
  static const int k_StyledText_alwaysShowScrollBars = 485;
  static const int k_StyledText_blockSelection = 486;
  static const int k_StyledText_blockSelectionBounds = 487;
  static const int k_StyledText_bottomMargin = 488;
  static const int k_StyledText_caretOffset = 489;
  static const int k_StyledText_caretOffsets = 490;
  static const int k_StyledText_doubleClickEnabled = 491;
  static const int k_StyledText_editable = 492;
  static const int k_StyledText_fixedLineHeight = 493;
  static const int k_StyledText_horizontalPixel = 494;
  static const int k_StyledText_indent = 495;
  static const int k_StyledText_justify = 496;
  static const int k_StyledText_leftMargin = 497;
  static const int k_StyledText_lineSpacing = 498;
  static const int k_StyledText_marginColor = 499;
  static const int k_StyledText_renderer = 500;
  static const int k_StyledText_rightMargin = 501;
  static const int k_StyledText_selection = 502;
  static const int k_StyledText_selectionBackground = 503;
  static const int k_StyledText_selectionForeground = 504;
  static const int k_StyledText_selectionRanges = 505;
  static const int k_StyledText_styleIndex = 506;
  static const int k_StyledText_styles = 507;
  static const int k_StyledText_tabStops = 508;
  static const int k_StyledText_tabs = 509;
  static const int k_StyledText_text = 510;
  static const int k_StyledText_topMargin = 511;
  static const int k_StyledText_topPixel = 512;
  static const int k_StyledText_wordWrap = 513;
  static const int k_StyledText_wrapIndent = 514;
  static const int k_StyledTextPrintOptions_footer = 515;
  static const int k_StyledTextPrintOptions_header = 516;
  static const int k_StyledTextPrintOptions_jobName = 517;
  static const int k_StyledTextPrintOptions_lineLabels = 518;
  static const int k_StyledTextPrintOptions_printLineBackground = 519;
  static const int k_StyledTextPrintOptions_printLineNumbers = 520;
  static const int k_StyledTextPrintOptions_printTextBackground = 521;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 522;
  static const int k_StyledTextPrintOptions_printTextForeground = 523;
  static const int k_StyledTextRenderer_ascent = 524;
  static const int k_StyledTextRenderer_averageCharWidth = 525;
  static const int k_StyledTextRenderer_averageLineHeight = 526;
  static const int k_StyledTextRenderer_boldFont = 527;
  static const int k_StyledTextRenderer_boldItalicFont = 528;
  static const int k_StyledTextRenderer_bulletTexts = 529;
  static const int k_StyledTextRenderer_bulletsIndices = 530;
  static const int k_StyledTextRenderer_descent = 531;
  static const int k_StyledTextRenderer_fixedPitch = 532;
  static const int k_StyledTextRenderer_hasLinks = 533;
  static const int k_StyledTextRenderer_idleRunning = 534;
  static const int k_StyledTextRenderer_italicFont = 535;
  static const int k_StyledTextRenderer_lineCount = 536;
  static const int k_StyledTextRenderer_lineSizes = 537;
  static const int k_StyledTextRenderer_lineSpacingComputing = 538;
  static const int k_StyledTextRenderer_lineSpacings = 539;
  static const int k_StyledTextRenderer_lines = 540;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 541;
  static const int k_StyledTextRenderer_maxWidth = 542;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 543;
  static const int k_StyledTextRenderer_ranges = 544;
  static const int k_StyledTextRenderer_redrawLines = 545;
  static const int k_StyledTextRenderer_regularFont = 546;
  static const int k_StyledTextRenderer_styleCount = 547;
  static const int k_StyledTextRenderer_styledText = 548;
  static const int k_StyledTextRenderer_styles = 549;
  static const int k_StyledTextRenderer_stylesSet = 550;
  static const int k_StyledTextRenderer_stylesSetCount = 551;
  static const int k_StyledTextRenderer_tabLength = 552;
  static const int k_StyledTextRenderer_tabWidth = 553;
  static const int k_StyledTextRenderer_topIndex = 554;
  static const int k_TabFolder_items = 555;
  static const int k_TabFolder_selection = 556;
  static const int k_TabItem_control = 557;
  static const int k_TabItem_toolTipText = 558;
  static const int k_Table_columnOrder = 559;
  static const int k_Table_columns = 560;
  static const int k_Table_editors = 561;
  static const int k_Table_headerBackground = 562;
  static const int k_Table_headerForeground = 563;
  static const int k_Table_headerVisible = 564;
  static const int k_Table_itemCount = 565;
  static const int k_Table_items = 566;
  static const int k_Table_linesVisible = 567;
  static const int k_Table_ownerDrawItemHeight = 568;
  static const int k_Table_selection = 569;
  static const int k_Table_sortColumn = 570;
  static const int k_Table_sortDirection = 571;
  static const int k_Table_topIndex = 572;
  static const int k_TableColumn_alignment = 573;
  static const int k_TableColumn_moveable = 574;
  static const int k_TableColumn_resizable = 575;
  static const int k_TableColumn_toolTipText = 576;
  static const int k_TableColumn_width = 577;
  static const int k_TableEditor_column = 578;
  static const int k_TableEditor_item = 579;
  static const int k_TableItem_background = 580;
  static const int k_TableItem_backgrounds = 581;
  static const int k_TableItem_checked = 582;
  static const int k_TableItem_font = 583;
  static const int k_TableItem_fonts = 584;
  static const int k_TableItem_foreground = 585;
  static const int k_TableItem_foregrounds = 586;
  static const int k_TableItem_grayed = 587;
  static const int k_TableItem_images = 588;
  static const int k_TableItem_paintedImages = 589;
  static const int k_TableItem_paintedTexts = 590;
  static const int k_TableItem_texts = 591;
  static const int k_Text_caretPosition = 592;
  static const int k_Text_echoCharacter = 593;
  static const int k_Text_editTexts = 594;
  static const int k_Text_editable = 595;
  static const int k_Text_edits = 596;
  static const int k_Text_message = 597;
  static const int k_Text_selection = 598;
  static const int k_Text_text = 599;
  static const int k_Text_textLimit = 600;
  static const int k_TextStyle_background = 601;
  static const int k_TextStyle_borderColor = 602;
  static const int k_TextStyle_borderStyle = 603;
  static const int k_TextStyle_font = 604;
  static const int k_TextStyle_foreground = 605;
  static const int k_TextStyle_metrics = 606;
  static const int k_TextStyle_rise = 607;
  static const int k_TextStyle_strikeout = 608;
  static const int k_TextStyle_strikeoutColor = 609;
  static const int k_TextStyle_underline = 610;
  static const int k_TextStyle_underlineColor = 611;
  static const int k_TextStyle_underlineStyle = 612;
  static const int k_ToolBar_items = 613;
  static const int k_ToolItem_background = 614;
  static const int k_ToolItem_control = 615;
  static const int k_ToolItem_disabledImage = 616;
  static const int k_ToolItem_enabled = 617;
  static const int k_ToolItem_foreground = 618;
  static const int k_ToolItem_hotImage = 619;
  static const int k_ToolItem_selection = 620;
  static const int k_ToolItem_toolTipText = 621;
  static const int k_ToolItem_width = 622;
  static const int k_ToolTip_location = 623;
  static const int k_ToolTip_message = 624;
  static const int k_ToolTip_text = 625;
  static const int k_ToolTip_visible = 626;
  static const int k_Touch_id = 627;
  static const int k_Touch_primary = 628;
  static const int k_Touch_source = 629;
  static const int k_Touch_state = 630;
  static const int k_Touch_x = 631;
  static const int k_Touch_y = 632;
  static const int k_TouchSource_bounds = 633;
  static const int k_TouchSource_direct = 634;
  static const int k_TouchSource_handle = 635;
  static const int k_Transform_elements = 636;
  static const int k_Tree_columnOrder = 637;
  static const int k_Tree_columns = 638;
  static const int k_Tree_editors = 639;
  static const int k_Tree_headerBackground = 640;
  static const int k_Tree_headerForeground = 641;
  static const int k_Tree_headerVisible = 642;
  static const int k_Tree_items = 643;
  static const int k_Tree_linesVisible = 644;
  static const int k_Tree_ownerDrawItemHeight = 645;
  static const int k_Tree_selection = 646;
  static const int k_Tree_sortColumn = 647;
  static const int k_Tree_sortDirection = 648;
  static const int k_Tree_topItem = 649;
  static const int k_TreeColumn_alignment = 650;
  static const int k_TreeColumn_moveable = 651;
  static const int k_TreeColumn_resizable = 652;
  static const int k_TreeColumn_toolTipText = 653;
  static const int k_TreeColumn_width = 654;
  static const int k_TreeEditor_column = 655;
  static const int k_TreeEditor_editor = 656;
  static const int k_TreeEditor_item = 657;
  static const int k_TreeItem_background = 658;
  static const int k_TreeItem_backgrounds = 659;
  static const int k_TreeItem_checked = 660;
  static const int k_TreeItem_expanded = 661;
  static const int k_TreeItem_font = 662;
  static const int k_TreeItem_fonts = 663;
  static const int k_TreeItem_foreground = 664;
  static const int k_TreeItem_foregrounds = 665;
  static const int k_TreeItem_grayed = 666;
  static const int k_TreeItem_images = 667;
  static const int k_TreeItem_itemCount = 668;
  static const int k_TreeItem_items = 669;
  static const int k_TreeItem_paintedImages = 670;
  static const int k_TreeItem_paintedTexts = 671;
  static const int k_TreeItem_texts = 672;
  static const int k_ViewForm_borderVisible = 673;
  static const int k_ViewForm_highlight = 674;
  static const int k_ViewForm_separator = 675;
  static const int k_Widget_style = 676;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  1,
  44,
  62,
  66,
  74,
  93,
  151,
  156,
  159,
  161,
  204,
  207,
  222,
  228,
  230,
  250,
  377,
  383,
  437,
  451,
  452,
  454,
  555,
  556,
  557,
  560,
  561,
  566,
  570,
  579,
  613,
  615,
  638,
  639,
  643,
  646,
  647,
  649,
  656,
  657,
  669,
};

/// Every type's number, for what names a widget by its type's number.
abstract final class WireType {
  static const int accessible = 1; // Accessible
  static const int animatedProgress = 2; // AnimatedProgress
  static const int borderData = 3; // BorderData
  static const int browser = 4; // Browser
  static const int browserFunction = 5; // BrowserFunction
  static const int busyIndicator = 6; // BusyIndicator
  static const int button = 7; // Button
  static const int byteArrayTransfer = 8; // ByteArrayTransfer
  static const int cBanner = 9; // CBanner
  static const int cCombo = 10; // CCombo
  static const int cLabel = 11; // CLabel
  static const int cTabFolder = 12; // CTabFolder
  static const int cTabItem = 13; // CTabItem
  static const int canvas = 14; // Canvas
  static const int caret = 15; // Caret
  static const int clipboard = 16; // Clipboard
  static const int color = 17; // Color
  static const int colorDialog = 18; // ColorDialog
  static const int combo = 19; // Combo
  static const int composite = 20; // Composite
  static const int controlEditor = 21; // ControlEditor
  static const int coolBar = 22; // CoolBar
  static const int coolItem = 23; // CoolItem
  static const int cursor = 24; // Cursor
  static const int dateTime = 25; // DateTime
  static const int decorations = 26; // Decorations
  static const int device = 27; // Device
  static const int dialog = 28; // Dialog
  static const int directoryDialog = 29; // DirectoryDialog
  static const int display = 30; // Display
  static const int displayPopups = 31; // DisplayPopups
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
}

/// Type names by the number a frame names a value's class with.
const Map<int, String> wireTypeNames = {
  1: 'Accessible',
  2: 'AnimatedProgress',
  3: 'BorderData',
  4: 'Browser',
  5: 'BrowserFunction',
  6: 'BusyIndicator',
  7: 'Button',
  8: 'ByteArrayTransfer',
  9: 'CBanner',
  10: 'CCombo',
  11: 'CLabel',
  12: 'CTabFolder',
  13: 'CTabItem',
  14: 'Canvas',
  15: 'Caret',
  16: 'Clipboard',
  17: 'Color',
  18: 'ColorDialog',
  19: 'Combo',
  20: 'Composite',
  21: 'ControlEditor',
  22: 'CoolBar',
  23: 'CoolItem',
  24: 'Cursor',
  25: 'DateTime',
  26: 'Decorations',
  27: 'Device',
  28: 'Dialog',
  29: 'DirectoryDialog',
  30: 'Display',
  31: 'DisplayPopups',
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
  23: 'DnD/remoteDrag',
  24: 'DnD/remoteDropNone',
  25: 'Drag/dragFinished',
  26: 'Drag/dragSetData',
  27: 'Drag/dragStart',
  28: 'DragDetect/DragDetect',
  29: 'DragDetect/dragCancel',
  30: 'DragDetect/dragImage',
  31: 'DragDetect/dragMove',
  32: 'DragDetect/dragRelease',
  33: 'DragDetect/dragStartResult',
  34: 'DragDetect/remoteAnswer',
  35: 'Drop/dragEnter',
  36: 'Drop/dragLeave',
  37: 'Drop/dragOperationChanged',
  38: 'Drop/dragOver',
  39: 'Drop/drop',
  40: 'Drop/dropAccept',
  41: 'Drop/negotiationResult',
  42: 'Expand/Collapse',
  43: 'Expand/Expand',
  44: 'ExtendedModify/modifyText',
  45: 'Focus/FocusIn',
  46: 'Focus/FocusOut',
  47: 'GC/create',
  48: 'GC/inFrame',
  49: 'Gesture/Gesture',
  50: 'GetIdFromPoint',
  51: 'GetIdFromPointResponse',
  52: 'GetItemBounds',
  53: 'GetItemBoundsResponse',
  54: 'GetItemHeight',
  55: 'GetItemHeightResponse',
  56: 'Help/Help',
  57: 'Image/forget',
  58: 'Image/releaseRemoteRef',
  59: 'Image/requestPixels',
  60: 'ImeComposition',
  61: 'Key/KeyDown',
  62: 'Key/KeyUp',
  63: 'LineBackground/lineGetBackground',
  64: 'LineStyle/lineGetStyle',
  65: 'List/Visible',
  66: 'Location/changed',
  67: 'Location/changing',
  68: 'Menu/Hide',
  69: 'Menu/Show',
  70: 'MenuDetect/MenuDetect',
  71: 'MenuKey',
  72: 'Modify/Modify',
  73: 'Mouse/MouseDoubleClick',
  74: 'Mouse/MouseDown',
  75: 'Mouse/MouseUp',
  76: 'MouseMove/MouseMove',
  77: 'MouseTrack/MouseEnter',
  78: 'MouseTrack/MouseExit',
  79: 'MouseTrack/MouseHover',
  80: 'MouseWheel/MouseWheel',
  81: 'OpenWindow',
  82: 'OpenWindow/open',
  83: 'Paint/Paint',
  84: 'PaintItem/PaintItem',
  85: 'PaintObject/paintObject',
  86: 'Popups',
  87: 'Progress/changed',
  88: 'Progress/completed',
  89: 'ResendGeometry',
  90: 'ResendText',
  91: 'Scroll/Scroll',
  92: 'Segment/Segments',
  93: 'Selection/DefaultSelection',
  94: 'Selection/OpenMenu',
  95: 'Selection/Selection',
  96: 'SetData/SetData',
  97: 'Shell/Activate',
  98: 'Shell/Close',
  99: 'Shell/Deactivate',
  100: 'Shell/Deiconify',
  101: 'Shell/Iconify',
  102: 'Shell/SetBounds',
  103: 'StatusText/changed',
  104: 'TextChange',
  105: 'TextGeometry',
  106: 'Title/changed',
  107: 'Touch/Touch',
  108: 'Tracker/cancel',
  109: 'Tracker/close',
  110: 'Tracker/open',
  111: 'Traverse/Traverse',
  112: 'Tree/Collapse',
  113: 'Tree/Expand',
  114: 'Verify/Verify',
  115: 'VerifyKey/verifyKey',
  116: 'VisibilityWindow/hide',
  117: 'VisibilityWindow/show',
  118: 'WinClose',
  119: 'WinDidEnterFullScreen',
  120: 'WinDidExitFullScreen',
  121: 'WinExitFullScreen',
  122: 'WinFullScreen',
  123: 'WinFullScreenState',
  124: 'WinMaximize',
  125: 'WinMinimize',
  126: 'WinRestore',
  127: 'WinUnload',
  128: 'WindowBounds',
  129: 'WindowOpenFailed',
  130: 'WindowState',
  131: 'WindowTitle',
  132: 'WordMovement/getNextOffset',
  133: 'WordMovement/getPreviousOffset',
  134: 'activate',
  135: 'activation',
  136: 'anim',
  137: 'b',
  138: 'back',
  139: 'browserFunctionCall',
  140: 'c',
  141: 'callback',
  142: 'close',
  143: 'cookie',
  144: 'copyAreaImageintint',
  145: 'copyAreaImageintintResponse',
  146: 'copyAreaintintintintintintboolean',
  147: 'drawArcintintintintintint',
  148: 'drawFocusintintintint',
  149: 'drawImageImageintint',
  150: 'drawImageImageintintintint',
  151: 'drawImageImageintintintintintintintint',
  152: 'drawLineintintintint',
  153: 'drawOvalintintintint',
  154: 'drawPathPath',
  155: 'drawPointintint',
  156: 'drawPolygonint',
  157: 'drawPolylineint',
  158: 'drawRectangleintintintint',
  159: 'drawRoundRectangleintintintintintint',
  160: 'drawTextStringintintint',
  161: 'evaluate',
  162: 'evaluate/result',
  163: 'evolve.test.bootTrace',
  164: 'evolve.test.bootTraceResponse',
  165: 'evolve.test.drag',
  166: 'evolve.test.dragDone',
  167: 'evolve.test.frameCost',
  168: 'evolve.test.frameCostResponse',
  169: 'evolve.test.frameStats',
  170: 'evolve.test.frameStatsResponse',
  171: 'evolve.test.frameSync',
  172: 'evolve.test.frameSynced',
  173: 'evolve.test.iframeLoaded',
  174: 'evolve.test.query',
  175: 'evolve.test.queryResponse',
  176: 'evolve.test.styledTextPerf',
  177: 'evolve.test.styledTextPerfResponse',
  178: 'evolve.test.wireStats',
  179: 'evolve.test.wireStatsResponse',
  180: 'execute',
  181: 'fillArcintintintintintint',
  182: 'fillGradientRectangleintintintintboolean',
  183: 'fillOvalintintintint',
  184: 'fillPathPath',
  185: 'fillPolygonint',
  186: 'fillRectangleintintintint',
  187: 'fillRoundRectangleintintintintintint',
  188: 'fontSizeRequest',
  189: 'fontSizeResponse',
  190: 'forward',
  191: 'gcDispose',
  192: 'getCookie',
  193: 'imageInit',
  194: 'imageResult',
  195: 'imageSizeRequest',
  196: 'imageSizeResponse',
  197: 'imageSnapshotResult',
  198: 'key/verdict',
  199: 'key/vetoable',
  200: 'locationchanging/result',
  201: 'menu/verdict',
  202: 'modify/verdict',
  203: 'modify/vetoable',
  204: 'navigate',
  205: 'navigateRequest',
  206: 'navigationState',
  207: 'nextFrame',
  208: 'openMenu',
  209: 'pixelsResult',
  210: 'rebuild',
  211: 'reload',
  212: 'renderSnapshot',
  213: 'setCookie',
  214: 'shown',
  215: 'stop',
  216: 'subtree',
  217: 'subtree/request',
  218: 'swing',
  219: 'swing.open',
  220: 'swingIsland',
  221: 'swingIsland.close',
  222: 'swingIsland.dispose',
  223: 'swingIsland.island',
  224: 'swingIsland.opened',
  225: 'swingIsland.resize',
  226: 'swingIsland.surface',
  227: 'swingIsland.title',
  228: 'swt.evolve.batch',
  229: 'swt.evolve.focus',
  230: 'swt.evolve.focus.seen',
  231: 'swt.evolve.focusHolder',
  232: 'swt.evolve.properties',
  233: 'swt.evolve.test.runUi',
  234: 'swt.evolve.test.runUiResponse',
  235: 'swt.evolve.timing.bare',
  236: 'swt.evolve.timing.ping',
  237: 'swt.evolve.timing.pong',
  238: 'swt.evolve.url.open',
  239: 'swt.evolve.widget.refresh',
  240: 'swt.evolve.wire',
  241: 'transport',
  242: 'transport/request',
  243: 'traverse/verdict',
  244: 'traverse/vetoable',
  245: 'v',
  246: 'widgetSizeRequest',
  247: 'widgetSizeResponse',
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
  static const int dnDRemoteDrag = 23; // DnD/remoteDrag
  static const int dnDRemoteDropNone = 24; // DnD/remoteDropNone
  static const int dragDragFinished = 25; // Drag/dragFinished
  static const int dragDragSetData = 26; // Drag/dragSetData
  static const int dragDragStart = 27; // Drag/dragStart
  static const int dragDetectDragDetect = 28; // DragDetect/DragDetect
  static const int dragDetectDragCancel = 29; // DragDetect/dragCancel
  static const int dragDetectDragImage = 30; // DragDetect/dragImage
  static const int dragDetectDragMove = 31; // DragDetect/dragMove
  static const int dragDetectDragRelease = 32; // DragDetect/dragRelease
  static const int dragDetectDragStartResult = 33; // DragDetect/dragStartResult
  static const int dragDetectRemoteAnswer = 34; // DragDetect/remoteAnswer
  static const int dropDragEnter = 35; // Drop/dragEnter
  static const int dropDragLeave = 36; // Drop/dragLeave
  static const int dropDragOperationChanged = 37; // Drop/dragOperationChanged
  static const int dropDragOver = 38; // Drop/dragOver
  static const int dropDrop = 39; // Drop/drop
  static const int dropDropAccept = 40; // Drop/dropAccept
  static const int dropNegotiationResult = 41; // Drop/negotiationResult
  static const int expandCollapse = 42; // Expand/Collapse
  static const int expandExpand = 43; // Expand/Expand
  static const int extendedModifyModifyText = 44; // ExtendedModify/modifyText
  static const int focusFocusIn = 45; // Focus/FocusIn
  static const int focusFocusOut = 46; // Focus/FocusOut
  static const int gcCreate = 47; // GC/create
  static const int gcInFrame = 48; // GC/inFrame
  static const int gestureGesture = 49; // Gesture/Gesture
  static const int getIdFromPoint = 50; // GetIdFromPoint
  static const int getIdFromPointResponse = 51; // GetIdFromPointResponse
  static const int getItemBounds = 52; // GetItemBounds
  static const int getItemBoundsResponse = 53; // GetItemBoundsResponse
  static const int getItemHeight = 54; // GetItemHeight
  static const int getItemHeightResponse = 55; // GetItemHeightResponse
  static const int helpHelp = 56; // Help/Help
  static const int imageForget = 57; // Image/forget
  static const int imageReleaseRemoteRef = 58; // Image/releaseRemoteRef
  static const int imageRequestPixels = 59; // Image/requestPixels
  static const int imeComposition = 60; // ImeComposition
  static const int keyKeyDown = 61; // Key/KeyDown
  static const int keyKeyUp = 62; // Key/KeyUp
  static const int lineBackgroundLineGetBackground =
      63; // LineBackground/lineGetBackground
  static const int lineStyleLineGetStyle = 64; // LineStyle/lineGetStyle
  static const int listVisible = 65; // List/Visible
  static const int locationChanged = 66; // Location/changed
  static const int locationChanging = 67; // Location/changing
  static const int menuHide = 68; // Menu/Hide
  static const int menuShow = 69; // Menu/Show
  static const int menuDetectMenuDetect = 70; // MenuDetect/MenuDetect
  static const int menuKey = 71; // MenuKey
  static const int modifyModify = 72; // Modify/Modify
  static const int mouseMouseDoubleClick = 73; // Mouse/MouseDoubleClick
  static const int mouseMouseDown = 74; // Mouse/MouseDown
  static const int mouseMouseUp = 75; // Mouse/MouseUp
  static const int mouseMoveMouseMove = 76; // MouseMove/MouseMove
  static const int mouseTrackMouseEnter = 77; // MouseTrack/MouseEnter
  static const int mouseTrackMouseExit = 78; // MouseTrack/MouseExit
  static const int mouseTrackMouseHover = 79; // MouseTrack/MouseHover
  static const int mouseWheelMouseWheel = 80; // MouseWheel/MouseWheel
  static const int openWindow = 81; // OpenWindow
  static const int openWindowOpen = 82; // OpenWindow/open
  static const int paintPaint = 83; // Paint/Paint
  static const int paintItemPaintItem = 84; // PaintItem/PaintItem
  static const int paintObjectPaintObject = 85; // PaintObject/paintObject
  static const int popups = 86; // Popups
  static const int progressChanged = 87; // Progress/changed
  static const int progressCompleted = 88; // Progress/completed
  static const int resendGeometry = 89; // ResendGeometry
  static const int resendText = 90; // ResendText
  static const int scrollScroll = 91; // Scroll/Scroll
  static const int segmentSegments = 92; // Segment/Segments
  static const int selectionDefaultSelection = 93; // Selection/DefaultSelection
  static const int selectionOpenMenu = 94; // Selection/OpenMenu
  static const int selectionSelection = 95; // Selection/Selection
  static const int setDataSetData = 96; // SetData/SetData
  static const int shellActivate = 97; // Shell/Activate
  static const int shellClose = 98; // Shell/Close
  static const int shellDeactivate = 99; // Shell/Deactivate
  static const int shellDeiconify = 100; // Shell/Deiconify
  static const int shellIconify = 101; // Shell/Iconify
  static const int shellSetBounds = 102; // Shell/SetBounds
  static const int statusTextChanged = 103; // StatusText/changed
  static const int textChange = 104; // TextChange
  static const int textGeometry = 105; // TextGeometry
  static const int titleChanged = 106; // Title/changed
  static const int touchTouch = 107; // Touch/Touch
  static const int trackerCancel = 108; // Tracker/cancel
  static const int trackerClose = 109; // Tracker/close
  static const int trackerOpen = 110; // Tracker/open
  static const int traverseTraverse = 111; // Traverse/Traverse
  static const int treeCollapse = 112; // Tree/Collapse
  static const int treeExpand = 113; // Tree/Expand
  static const int verifyVerify = 114; // Verify/Verify
  static const int verifyKeyVerifyKey = 115; // VerifyKey/verifyKey
  static const int visibilityWindowHide = 116; // VisibilityWindow/hide
  static const int visibilityWindowShow = 117; // VisibilityWindow/show
  static const int winClose = 118; // WinClose
  static const int winDidEnterFullScreen = 119; // WinDidEnterFullScreen
  static const int winDidExitFullScreen = 120; // WinDidExitFullScreen
  static const int winExitFullScreen = 121; // WinExitFullScreen
  static const int winFullScreen = 122; // WinFullScreen
  static const int winFullScreenState = 123; // WinFullScreenState
  static const int winMaximize = 124; // WinMaximize
  static const int winMinimize = 125; // WinMinimize
  static const int winRestore = 126; // WinRestore
  static const int winUnload = 127; // WinUnload
  static const int windowBounds = 128; // WindowBounds
  static const int windowOpenFailed = 129; // WindowOpenFailed
  static const int windowState = 130; // WindowState
  static const int windowTitle = 131; // WindowTitle
  static const int wordMovementGetNextOffset =
      132; // WordMovement/getNextOffset
  static const int wordMovementGetPreviousOffset =
      133; // WordMovement/getPreviousOffset
  static const int activate = 134; // activate
  static const int activation = 135; // activation
  static const int anim = 136; // anim
  static const int b = 137; // b
  static const int back = 138; // back
  static const int browserFunctionCall = 139; // browserFunctionCall
  static const int c = 140; // c
  static const int callback = 141; // callback
  static const int close = 142; // close
  static const int cookie = 143; // cookie
  static const int copyAreaImageintint = 144; // copyAreaImageintint
  static const int copyAreaImageintintResponse =
      145; // copyAreaImageintintResponse
  static const int copyAreaintintintintintintboolean =
      146; // copyAreaintintintintintintboolean
  static const int drawArcintintintintintint = 147; // drawArcintintintintintint
  static const int drawFocusintintintint = 148; // drawFocusintintintint
  static const int drawImageImageintint = 149; // drawImageImageintint
  static const int drawImageImageintintintint =
      150; // drawImageImageintintintint
  static const int drawImageImageintintintintintintintint =
      151; // drawImageImageintintintintintintintint
  static const int drawLineintintintint = 152; // drawLineintintintint
  static const int drawOvalintintintint = 153; // drawOvalintintintint
  static const int drawPathPath = 154; // drawPathPath
  static const int drawPointintint = 155; // drawPointintint
  static const int drawPolygonint = 156; // drawPolygonint
  static const int drawPolylineint = 157; // drawPolylineint
  static const int drawRectangleintintintint = 158; // drawRectangleintintintint
  static const int drawRoundRectangleintintintintintint =
      159; // drawRoundRectangleintintintintintint
  static const int drawTextStringintintint = 160; // drawTextStringintintint
  static const int evaluate = 161; // evaluate
  static const int evaluateResult = 162; // evaluate/result
  static const int evolveTestBootTrace = 163; // evolve.test.bootTrace
  static const int evolveTestBootTraceResponse =
      164; // evolve.test.bootTraceResponse
  static const int evolveTestDrag = 165; // evolve.test.drag
  static const int evolveTestDragDone = 166; // evolve.test.dragDone
  static const int evolveTestFrameCost = 167; // evolve.test.frameCost
  static const int evolveTestFrameCostResponse =
      168; // evolve.test.frameCostResponse
  static const int evolveTestFrameStats = 169; // evolve.test.frameStats
  static const int evolveTestFrameStatsResponse =
      170; // evolve.test.frameStatsResponse
  static const int evolveTestFrameSync = 171; // evolve.test.frameSync
  static const int evolveTestFrameSynced = 172; // evolve.test.frameSynced
  static const int evolveTestIframeLoaded = 173; // evolve.test.iframeLoaded
  static const int evolveTestQuery = 174; // evolve.test.query
  static const int evolveTestQueryResponse = 175; // evolve.test.queryResponse
  static const int evolveTestStyledTextPerf = 176; // evolve.test.styledTextPerf
  static const int evolveTestStyledTextPerfResponse =
      177; // evolve.test.styledTextPerfResponse
  static const int evolveTestWireStats = 178; // evolve.test.wireStats
  static const int evolveTestWireStatsResponse =
      179; // evolve.test.wireStatsResponse
  static const int execute = 180; // execute
  static const int fillArcintintintintintint = 181; // fillArcintintintintintint
  static const int fillGradientRectangleintintintintboolean =
      182; // fillGradientRectangleintintintintboolean
  static const int fillOvalintintintint = 183; // fillOvalintintintint
  static const int fillPathPath = 184; // fillPathPath
  static const int fillPolygonint = 185; // fillPolygonint
  static const int fillRectangleintintintint = 186; // fillRectangleintintintint
  static const int fillRoundRectangleintintintintintint =
      187; // fillRoundRectangleintintintintintint
  static const int fontSizeRequest = 188; // fontSizeRequest
  static const int fontSizeResponse = 189; // fontSizeResponse
  static const int forward = 190; // forward
  static const int gcDispose = 191; // gcDispose
  static const int getCookie = 192; // getCookie
  static const int imageInit = 193; // imageInit
  static const int imageResult = 194; // imageResult
  static const int imageSizeRequest = 195; // imageSizeRequest
  static const int imageSizeResponse = 196; // imageSizeResponse
  static const int imageSnapshotResult = 197; // imageSnapshotResult
  static const int keyVerdict = 198; // key/verdict
  static const int keyVetoable = 199; // key/vetoable
  static const int locationchangingResult = 200; // locationchanging/result
  static const int menuVerdict = 201; // menu/verdict
  static const int modifyVerdict = 202; // modify/verdict
  static const int modifyVetoable = 203; // modify/vetoable
  static const int navigate = 204; // navigate
  static const int navigateRequest = 205; // navigateRequest
  static const int navigationState = 206; // navigationState
  static const int nextFrame = 207; // nextFrame
  static const int openMenu = 208; // openMenu
  static const int pixelsResult = 209; // pixelsResult
  static const int rebuild = 210; // rebuild
  static const int reload = 211; // reload
  static const int renderSnapshot = 212; // renderSnapshot
  static const int setCookie = 213; // setCookie
  static const int shown = 214; // shown
  static const int stop = 215; // stop
  static const int subtree = 216; // subtree
  static const int subtreeRequest = 217; // subtree/request
  static const int swing = 218; // swing
  static const int swingOpen = 219; // swing.open
  static const int swingIsland = 220; // swingIsland
  static const int swingIslandClose = 221; // swingIsland.close
  static const int swingIslandDispose = 222; // swingIsland.dispose
  static const int swingIslandIsland = 223; // swingIsland.island
  static const int swingIslandOpened = 224; // swingIsland.opened
  static const int swingIslandResize = 225; // swingIsland.resize
  static const int swingIslandSurface = 226; // swingIsland.surface
  static const int swingIslandTitle = 227; // swingIsland.title
  static const int swtEvolveBatch = 228; // swt.evolve.batch
  static const int swtEvolveFocus = 229; // swt.evolve.focus
  static const int swtEvolveFocusSeen = 230; // swt.evolve.focus.seen
  static const int swtEvolveFocusHolder = 231; // swt.evolve.focusHolder
  static const int swtEvolveProperties = 232; // swt.evolve.properties
  static const int swtEvolveTestRunUi = 233; // swt.evolve.test.runUi
  static const int swtEvolveTestRunUiResponse =
      234; // swt.evolve.test.runUiResponse
  static const int swtEvolveTimingBare = 235; // swt.evolve.timing.bare
  static const int swtEvolveTimingPing = 236; // swt.evolve.timing.ping
  static const int swtEvolveTimingPong = 237; // swt.evolve.timing.pong
  static const int swtEvolveUrlOpen = 238; // swt.evolve.url.open
  static const int swtEvolveWidgetRefresh = 239; // swt.evolve.widget.refresh
  static const int swtEvolveWire = 240; // swt.evolve.wire
  static const int transport = 241; // transport
  static const int transportRequest = 242; // transport/request
  static const int traverseVerdict = 243; // traverse/verdict
  static const int traverseVetoable = 244; // traverse/vetoable
  static const int v = 245; // v
  static const int widgetSizeRequest = 246; // widgetSizeRequest
  static const int widgetSizeResponse = 247; // widgetSizeResponse
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
  34: 'bigger_icon_size',
  35: 'blinkRate',
  36: 'blockSelection',
  37: 'blockSelectionBounds',
  38: 'blue',
  39: 'blueSize',
  40: 'boldFont',
  41: 'boldItalicFont',
  42: 'borderColor',
  43: 'borderStyle',
  44: 'borderVisible',
  45: 'bottom',
  46: 'bottomLine',
  47: 'bottomMargin',
  48: 'bounds',
  49: 'browser',
  50: 'bufferScale',
  51: 'bufferScaleY',
  52: 'bulletTexts',
  53: 'bulletsIndices',
  54: 'button',
  55: 'buttonLabels',
  56: 'cacheBottom',
  57: 'cacheHeight',
  58: 'cacheLeft',
  59: 'cacheRight',
  60: 'cacheTop',
  61: 'cacheWidth',
  62: 'caret',
  63: 'caretOffset',
  64: 'caretOffsets',
  65: 'caretPosition',
  66: 'charCount',
  67: 'character',
  68: 'checked',
  69: 'chevronVisible',
  70: 'children',
  71: 'clientHeight',
  72: 'clientWidth',
  73: 'clientX',
  74: 'clientY',
  75: 'clipping',
  76: 'clippingPath',
  77: 'clippingRects',
  78: 'clippingText',
  79: 'color1',
  80: 'color2',
  81: 'column',
  82: 'columnOrder',
  83: 'columns',
  84: 'config',
  85: 'content',
  86: 'control',
  87: 'cookie',
  88: 'count',
  89: 'csd_maximize',
  90: 'csd_os',
  91: 'csd_placement',
  92: 'csd_titlebar_color',
  93: 'ctabfolder_chevron_after_tabs',
  94: 'ctabfolder_tab_scroll',
  95: 'ctabfolder_topright_auto_hide',
  96: 'ctabfolder_visible_controls',
  97: 'currentDataTypeId',
  98: 'currentHeight',
  99: 'currentHhint',
  100: 'currentWhint',
  101: 'currentWidth',
  102: 'cursor',
  103: 'cursorStyle',
  104: 'curveLine',
  105: 'custom_tooltip',
  106: 'damage',
  107: 'data',
  108: 'day',
  109: 'decimal_separator',
  110: 'decorations_align',
  111: 'defaultHeight',
  112: 'defaultHhint',
  113: 'defaultWhint',
  114: 'defaultWidth',
  115: 'denominator',
  116: 'depth',
  117: 'depthSize',
  118: 'descent',
  119: 'detail',
  120: 'dialogs',
  121: 'digits',
  122: 'direct',
  123: 'dirtyIndicatorStyle',
  124: 'disable_control_gc_overlay',
  125: 'disable_evolve_icons',
  126: 'disable_hover_zoom',
  127: 'disable_swt_canvas_colors',
  128: 'disabledImage',
  129: 'doit',
  130: 'doubleBuffer',
  131: 'doubleClickEnabled',
  132: 'double_click_timeout_ms',
  133: 'dragDetect',
  134: 'dragImage',
  135: 'dragSource',
  136: 'dragSourceId',
  137: 'dropTargetId',
  138: 'echoCharacter',
  139: 'editTexts',
  140: 'editable',
  141: 'editor',
  142: 'editors',
  143: 'edits',
  144: 'effectsVisible',
  145: 'elements',
  146: 'enabled',
  147: 'end',
  148: 'endPage',
  149: 'endX',
  150: 'endY',
  151: 'error',
  152: 'exclude',
  153: 'expandHorizontal',
  154: 'expandVertical',
  155: 'expanded',
  156: 'expires',
  157: 'feedback',
  158: 'fileName',
  159: 'fileNames',
  160: 'filename',
  161: 'fillRule',
  162: 'filterExtensions',
  163: 'filterIndex',
  164: 'filterNames',
  165: 'filterPath',
  166: 'fixedLineHeight',
  167: 'fixedPitch',
  168: 'flags',
  169: 'focus',
  170: 'focusHolderId',
  171: 'focus_indicators',
  172: 'focused',
  173: 'font',
  174: 'fontData',
  175: 'fontList',
  176: 'fontStyle',
  177: 'font_point_scale',
  178: 'fonts',
  179: 'footer',
  180: 'force_theme',
  181: 'foreground',
  182: 'foregroundPattern',
  183: 'foregrounds',
  184: 'fullRepaint',
  185: 'fullScreen',
  186: 'functionNames',
  187: 'g',
  188: 'gc',
  189: 'gc_icons_replacement',
  190: 'grabExcessHorizontalSpace',
  191: 'grabExcessVerticalSpace',
  192: 'gradientColors',
  193: 'gradientPercents',
  194: 'gradientVertical',
  195: 'grayed',
  196: 'green',
  197: 'greenSize',
  198: 'hHint',
  199: 'handle',
  200: 'hasLinks',
  201: 'hasOwnBackground',
  202: 'header',
  203: 'headerBackground',
  204: 'headerForeground',
  205: 'headerHeight',
  206: 'headerVisible',
  207: 'headers',
  208: 'height',
  209: 'heightHint',
  210: 'highlight',
  211: 'highlightEnabled',
  212: 'horizontalAlignment',
  213: 'horizontalBar',
  214: 'horizontalIndent',
  215: 'horizontalPixel',
  216: 'horizontalSpan',
  217: 'hotImage',
  218: 'hotspotX',
  219: 'hotspotY',
  220: 'hours',
  221: 'hoverFills',
  222: 'hover_delay_ms',
  223: 'id',
  224: 'idleRunning',
  225: 'image',
  226: 'imageData',
  227: 'image_disable_icons_replacement',
  228: 'images',
  229: 'increment',
  230: 'indent',
  231: 'index',
  232: 'inheritsBackground',
  233: 'interpolation',
  234: 'isVisited',
  235: 'italicFont',
  236: 'item',
  237: 'itemCount',
  238: 'itemId',
  239: 'itemOrder',
  240: 'itemSizes',
  241: 'itemTooltips',
  242: 'items',
  243: 'javascriptEnabled',
  244: 'jobName',
  245: 'justify',
  246: 'keptText',
  247: 'keyCode',
  248: 'keyLocation',
  249: 'left',
  250: 'leftMargin',
  251: 'length',
  252: 'lineCap',
  253: 'lineCount',
  254: 'lineDash',
  255: 'lineDashOffset',
  256: 'lineJoin',
  257: 'lineLabels',
  258: 'lineSizes',
  259: 'lineSpacing',
  260: 'lineSpacingComputing',
  261: 'lineSpacings',
  262: 'lineStyle',
  263: 'lineWidth',
  264: 'lines',
  265: 'linesInAverageLineHeight',
  266: 'linesVisible',
  267: 'linkForeground',
  268: 'listVisible',
  269: 'localFileBase',
  270: 'localFilePath',
  271: 'localFileText',
  272: 'locale',
  273: 'location',
  274: 'magnification',
  275: 'mainShellId',
  276: 'map',
  277: 'marginColor',
  278: 'maxWidth',
  279: 'maxWidthLineIndex',
  280: 'maximizeVisible',
  281: 'maximized',
  282: 'maximizedControl',
  283: 'maximum',
  284: 'maximumSize',
  285: 'menu',
  286: 'menuBar',
  287: 'menuStripHeight',
  288: 'message',
  289: 'metrics',
  290: 'minHeight',
  291: 'minWidth',
  292: 'minimizeVisible',
  293: 'minimized',
  294: 'minimum',
  295: 'minimumCharacters',
  296: 'minimumHeight',
  297: 'minimumSize',
  298: 'minimumWidth',
  299: 'minutes',
  300: 'modified',
  301: 'month',
  302: 'moveable',
  303: 'name',
  304: 'needed',
  305: 'notification_popup',
  306: 'numerator',
  307: 'offset',
  308: 'operations',
  309: 'orientation',
  310: 'origin',
  311: 'originRequest',
  312: 'overwrite',
  313: 'ownMenuStrip',
  314: 'ownerDrawItemHeight',
  315: 'pageIncrement',
  316: 'paintedImages',
  317: 'paintedTexts',
  318: 'pathData',
  319: 'pixels',
  320: 'points',
  321: 'popups',
  322: 'port',
  323: 'postData',
  324: 'preferredSize',
  325: 'preserve_icon_colors',
  326: 'primary',
  327: 'printLineBackground',
  328: 'printLineNumbers',
  329: 'printTextBackground',
  330: 'printTextFontStyle',
  331: 'printTextForeground',
  332: 'printToFile',
  333: 'print_move',
  334: 'push_button_margin',
  335: 'r',
  336: 'ranges',
  337: 'rects',
  338: 'red',
  339: 'redSize',
  340: 'redrawLines',
  341: 'ref',
  342: 'region',
  343: 'regularFont',
  344: 'remoteRef',
  345: 'renderer',
  346: 'replaced',
  347: 'reqId',
  348: 'resizable',
  349: 'result',
  350: 'retain',
  351: 'right',
  352: 'rightMargin',
  353: 'rise',
  354: 'rotation',
  355: 'sampleBuffers',
  356: 'samples',
  357: 'sashWidth',
  358: 'scale_vertical_minimum_at_top',
  359: 'scope',
  360: 'script',
  361: 'seconds',
  362: 'segments',
  363: 'segmentsChars',
  364: 'selectedImageVisible',
  365: 'selection',
  366: 'selectionBackground',
  367: 'selectionBarThickness',
  368: 'selectionBgImage',
  369: 'selectionFills',
  370: 'selectionForeground',
  371: 'selectionRanges',
  372: 'separator',
  373: 'seq',
  374: 'shareContext',
  375: 'shells',
  376: 'showChevron',
  377: 'showClose',
  378: 'showDirty',
  379: 'showListPopupSeq',
  380: 'show_scaling_control',
  381: 'show_theme_color_palette',
  382: 'showing',
  383: 'single',
  384: 'sortColumn',
  385: 'sortDirection',
  386: 'source',
  387: 'spacing',
  388: 'start',
  389: 'startPage',
  390: 'startX',
  391: 'startY',
  392: 'state',
  393: 'stateMask',
  394: 'stencilSize',
  395: 'stereo',
  396: 'strikeout',
  397: 'strikeoutColor',
  398: 'stripLayoutSeq',
  399: 'style',
  400: 'styleCount',
  401: 'styleIndex',
  402: 'styledText',
  403: 'styles',
  404: 'stylesSet',
  405: 'stylesSetCount',
  406: 'svgContent',
  407: 'swt',
  408: 'systemMenu',
  409: 'system_menu_bar',
  410: 'tabHeight',
  411: 'tabLength',
  412: 'tabPosition',
  413: 'tabStop',
  414: 'tabStops',
  415: 'tabWidth',
  416: 'tabs',
  417: 'text',
  418: 'textFonts',
  419: 'textLimit',
  420: 'textOrigins',
  421: 'textStrings',
  422: 'texts',
  423: 'theme_color',
  424: 'theme_colors_by_widget',
  425: 'theme_name',
  426: 'themedCanvas',
  427: 'thumb',
  428: 'time',
  429: 'toolTipText',
  430: 'tooltips',
  431: 'top',
  432: 'topIndex',
  433: 'topItem',
  434: 'topMargin',
  435: 'topPixel',
  436: 'topRight',
  437: 'topRightAlignment',
  438: 'touches',
  439: 'tracking',
  440: 'transform',
  441: 'transparent_menu_check_box',
  442: 'type',
  443: 'types',
  444: 'ui_zoom',
  445: 'underline',
  446: 'underlineColor',
  447: 'underlineStyle',
  448: 'unselectedCloseVisible',
  449: 'unselectedImageVisible',
  450: 'url',
  451: 'use_special_dropdown_button',
  452: 'use_swt_border',
  453: 'use_swt_colors',
  454: 'use_swt_font_colors',
  455: 'use_swt_font_colors_by_widget',
  456: 'use_swt_fonts',
  457: 'value',
  458: 'verdict',
  459: 'verticalAlignment',
  460: 'verticalBar',
  461: 'verticalIndent',
  462: 'verticalSpan',
  463: 'visible',
  464: 'wHint',
  465: 'warnings',
  466: 'weights',
  467: 'widget',
  468: 'width',
  469: 'widthHint',
  470: 'windowId',
  471: 'windowedShellIds',
  472: 'wordWrap',
  473: 'wrapIndent',
  474: 'wrapIndices',
  475: 'x',
  476: 'xDirection',
  477: 'y',
  478: 'yDirection',
  479: 'year',
  480: 'zoom',
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
