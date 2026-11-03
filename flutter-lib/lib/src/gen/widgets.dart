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
  return switch (child) {
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
}

/// The widget of type [swt] numbered [id], drawn from what the registry holds for it.
Widget mapWidget(String swt, int id) {
  final value = newWidgetValue(swt);
  if (value == null) throw "Unknown Widget Value $swt";
  return mapWidgetFromValue(value..id = id);
}

/// The table's fingerprint, which Java compares with its own when this client connects.
const int wireFingerprint = -1940320440;

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
  static const int k_CCombo_alignment = 18;
  static const int k_CCombo_itemTooltips = 19;
  static const int k_CCombo_items = 20;
  static const int k_CCombo_listVisible = 21;
  static const int k_CCombo_selection = 22;
  static const int k_CCombo_text = 23;
  static const int k_CCombo_textLimit = 24;
  static const int k_CLabel_alignment = 25;
  static const int k_CLabel_backgroundImage = 26;
  static const int k_CLabel_bottomMargin = 27;
  static const int k_CLabel_gradientColors = 28;
  static const int k_CLabel_gradientPercents = 29;
  static const int k_CLabel_gradientVertical = 30;
  static const int k_CLabel_image = 31;
  static const int k_CLabel_leftMargin = 32;
  static const int k_CLabel_rightMargin = 33;
  static const int k_CLabel_text = 34;
  static const int k_CLabel_topMargin = 35;
  static const int k_CTabFolder_borderVisible = 36;
  static const int k_CTabFolder_chevronVisible = 37;
  static const int k_CTabFolder_highlight = 38;
  static const int k_CTabFolder_highlightEnabled = 39;
  static const int k_CTabFolder_items = 40;
  static const int k_CTabFolder_maximizeVisible = 41;
  static const int k_CTabFolder_maximized = 42;
  static const int k_CTabFolder_minimizeVisible = 43;
  static const int k_CTabFolder_minimized = 44;
  static const int k_CTabFolder_minimumCharacters = 45;
  static const int k_CTabFolder_selectedImageVisible = 46;
  static const int k_CTabFolder_selection = 47;
  static const int k_CTabFolder_selectionBackground = 48;
  static const int k_CTabFolder_selectionBarThickness = 49;
  static const int k_CTabFolder_selectionBgImage = 50;
  static const int k_CTabFolder_selectionForeground = 51;
  static const int k_CTabFolder_showChevron = 52;
  static const int k_CTabFolder_showListPopupSeq = 53;
  static const int k_CTabFolder_single = 54;
  static const int k_CTabFolder_tabHeight = 55;
  static const int k_CTabFolder_tabPosition = 56;
  static const int k_CTabFolder_topRight = 57;
  static const int k_CTabFolder_topRightAlignment = 58;
  static const int k_CTabFolder_unselectedCloseVisible = 59;
  static const int k_CTabFolder_unselectedImageVisible = 60;
  static const int k_CTabItem_control = 61;
  static const int k_CTabItem_font = 62;
  static const int k_CTabItem_foreground = 63;
  static const int k_CTabItem_selectionForeground = 64;
  static const int k_CTabItem_showClose = 65;
  static const int k_CTabItem_showing = 66;
  static const int k_CTabItem_toolTipText = 67;
  static const int k_Canvas_caret = 68;
  static const int k_Caret_blinkRate = 69;
  static const int k_Caret_bounds = 70;
  static const int k_Caret_focused = 71;
  static const int k_Caret_image = 72;
  static const int k_Caret_visible = 73;
  static const int k_Clipboard_availableTypeNames = 74;
  static const int k_Color_a = 75;
  static const int k_Color_b = 76;
  static const int k_Color_g = 77;
  static const int k_Color_r = 78;
  static const int k_ColorDialog_RGB = 79;
  static const int k_ColorDialog_RGBs = 80;
  static const int k_Combo_caretPosition = 81;
  static const int k_Combo_items = 82;
  static const int k_Combo_listVisible = 83;
  static const int k_Combo_text = 84;
  static const int k_Composite_backgroundMode = 85;
  static const int k_Composite_children = 86;
  static const int k_ConfigFlags_assets_path = 87;
  static const int k_ConfigFlags_bigger_icon_size = 88;
  static const int k_ConfigFlags_csd_maximize = 89;
  static const int k_ConfigFlags_csd_os = 90;
  static const int k_ConfigFlags_csd_placement = 91;
  static const int k_ConfigFlags_csd_titlebar_color = 92;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 93;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 94;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 95;
  static const int k_ConfigFlags_custom_tooltip = 96;
  static const int k_ConfigFlags_decimal_separator = 97;
  static const int k_ConfigFlags_decorations_align = 98;
  static const int k_ConfigFlags_disable_control_gc_overlay = 99;
  static const int k_ConfigFlags_disable_evolve_icons = 100;
  static const int k_ConfigFlags_disable_hover_zoom = 101;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 102;
  static const int k_ConfigFlags_double_click_timeout_ms = 103;
  static const int k_ConfigFlags_focus_indicators = 104;
  static const int k_ConfigFlags_font_point_scale = 105;
  static const int k_ConfigFlags_force_theme = 106;
  static const int k_ConfigFlags_gc_icons_replacement = 107;
  static const int k_ConfigFlags_hover_delay_ms = 108;
  static const int k_ConfigFlags_image_disable_icons_replacement = 109;
  static const int k_ConfigFlags_locale = 110;
  static const int k_ConfigFlags_notification_popup = 111;
  static const int k_ConfigFlags_preserve_icon_colors = 112;
  static const int k_ConfigFlags_print_move = 113;
  static const int k_ConfigFlags_push_button_margin = 114;
  static const int k_ConfigFlags_scale_vertical_minimum_at_top = 115;
  static const int k_ConfigFlags_show_scaling_control = 116;
  static const int k_ConfigFlags_show_theme_color_palette = 117;
  static const int k_ConfigFlags_system_menu_bar = 118;
  static const int k_ConfigFlags_theme_color = 119;
  static const int k_ConfigFlags_theme_colors_by_widget = 120;
  static const int k_ConfigFlags_theme_name = 121;
  static const int k_ConfigFlags_transparent_menu_check_box = 122;
  static const int k_ConfigFlags_ui_zoom = 123;
  static const int k_ConfigFlags_use_special_dropdown_button = 124;
  static const int k_ConfigFlags_use_swt_border = 125;
  static const int k_ConfigFlags_use_swt_colors = 126;
  static const int k_ConfigFlags_use_swt_font_colors = 127;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 128;
  static const int k_ConfigFlags_use_swt_fonts = 129;
  static const int k_Control_background = 130;
  static const int k_Control_backgroundImage = 131;
  static const int k_Control_bounds = 132;
  static const int k_Control_cursor = 133;
  static const int k_Control_dragDetect = 134;
  static const int k_Control_dragSource = 135;
  static const int k_Control_dragSourceId = 136;
  static const int k_Control_dropTargetId = 137;
  static const int k_Control_enabled = 138;
  static const int k_Control_font = 139;
  static const int k_Control_foreground = 140;
  static const int k_Control_hasOwnBackground = 141;
  static const int k_Control_inheritsBackground = 142;
  static const int k_Control_menu = 143;
  static const int k_Control_region = 144;
  static const int k_Control_tabStop = 145;
  static const int k_Control_toolTipText = 146;
  static const int k_Control_visible = 147;
  static const int k_ControlEditor_editor = 148;
  static const int k_CoolBar_itemOrder = 149;
  static const int k_CoolBar_itemSizes = 150;
  static const int k_CoolBar_items = 151;
  static const int k_CoolBar_wrapIndices = 152;
  static const int k_CoolItem_control = 153;
  static const int k_CoolItem_preferredSize = 154;
  static const int k_Cursor_cursorStyle = 155;
  static const int k_Cursor_hotspotX = 156;
  static const int k_Cursor_hotspotY = 157;
  static const int k_Cursor_image = 158;
  static const int k_DateTime_day = 159;
  static const int k_DateTime_hours = 160;
  static const int k_DateTime_minutes = 161;
  static const int k_DateTime_month = 162;
  static const int k_DateTime_seconds = 163;
  static const int k_DateTime_year = 164;
  static const int k_Decorations_menuBar = 165;
  static const int k_Decorations_menuStripHeight = 166;
  static const int k_Decorations_ownMenuStrip = 167;
  static const int k_Decorations_text = 168;
  static const int k_Device_fontList = 169;
  static const int k_Device_tracking = 170;
  static const int k_Device_warnings = 171;
  static const int k_Dialog_id = 172;
  static const int k_Dialog_style = 173;
  static const int k_Dialog_swt = 174;
  static const int k_Dialog_text = 175;
  static const int k_DirectoryDialog_filterPath = 176;
  static const int k_DirectoryDialog_message = 177;
  static const int k_Display_activeShellId = 178;
  static const int k_Display_config = 179;
  static const int k_Display_focusHolderId = 180;
  static const int k_Display_mainShellId = 181;
  static const int k_Display_popups = 182;
  static const int k_Display_shells = 183;
  static const int k_Display_systemMenu = 184;
  static const int k_Display_tooltips = 185;
  static const int k_Display_windowedShellIds = 186;
  static const int k_Event_button = 187;
  static const int k_Event_character = 188;
  static const int k_Event_count = 189;
  static const int k_Event_currentDataTypeId = 190;
  static const int k_Event_detail = 191;
  static const int k_Event_doit = 192;
  static const int k_Event_dragImage = 193;
  static const int k_Event_end = 194;
  static const int k_Event_feedback = 195;
  static const int k_Event_gc = 196;
  static const int k_Event_height = 197;
  static const int k_Event_index = 198;
  static const int k_Event_item = 199;
  static const int k_Event_itemId = 200;
  static const int k_Event_keyCode = 201;
  static const int k_Event_keyLocation = 202;
  static const int k_Event_magnification = 203;
  static const int k_Event_operations = 204;
  static const int k_Event_rotation = 205;
  static const int k_Event_segments = 206;
  static const int k_Event_segmentsChars = 207;
  static const int k_Event_start = 208;
  static const int k_Event_stateMask = 209;
  static const int k_Event_text = 210;
  static const int k_Event_time = 211;
  static const int k_Event_touches = 212;
  static const int k_Event_type = 213;
  static const int k_Event_widget = 214;
  static const int k_Event_width = 215;
  static const int k_Event_x = 216;
  static const int k_Event_xDirection = 217;
  static const int k_Event_y = 218;
  static const int k_Event_yDirection = 219;
  static const int k_ExpandBar_items = 220;
  static const int k_ExpandBar_spacing = 221;
  static const int k_ExpandItem_control = 222;
  static const int k_ExpandItem_expanded = 223;
  static const int k_ExpandItem_headerHeight = 224;
  static const int k_ExpandItem_height = 225;
  static const int k_FileDialog_fileName = 226;
  static const int k_FileDialog_fileNames = 227;
  static const int k_FileDialog_filterExtensions = 228;
  static const int k_FileDialog_filterIndex = 229;
  static const int k_FileDialog_filterNames = 230;
  static const int k_FileDialog_filterPath = 231;
  static const int k_FileDialog_overwrite = 232;
  static const int k_Font_fontData = 233;
  static const int k_FontData_height = 234;
  static const int k_FontData_name = 235;
  static const int k_FontData_style = 236;
  static const int k_FontDialog_RGB = 237;
  static const int k_FontDialog_effectsVisible = 238;
  static const int k_FontDialog_fontData = 239;
  static const int k_FontDialog_fontList = 240;
  static const int k_FormAttachment_alignment = 241;
  static const int k_FormAttachment_control = 242;
  static const int k_FormAttachment_denominator = 243;
  static const int k_FormAttachment_numerator = 244;
  static const int k_FormAttachment_offset = 245;
  static const int k_FormData_bottom = 246;
  static const int k_FormData_cacheBottom = 247;
  static const int k_FormData_cacheHeight = 248;
  static const int k_FormData_cacheLeft = 249;
  static const int k_FormData_cacheRight = 250;
  static const int k_FormData_cacheTop = 251;
  static const int k_FormData_cacheWidth = 252;
  static const int k_FormData_currentHeight = 253;
  static const int k_FormData_currentHhint = 254;
  static const int k_FormData_currentWhint = 255;
  static const int k_FormData_currentWidth = 256;
  static const int k_FormData_defaultHeight = 257;
  static const int k_FormData_defaultHhint = 258;
  static const int k_FormData_defaultWhint = 259;
  static const int k_FormData_defaultWidth = 260;
  static const int k_FormData_height = 261;
  static const int k_FormData_isVisited = 262;
  static const int k_FormData_left = 263;
  static const int k_FormData_needed = 264;
  static const int k_FormData_right = 265;
  static const int k_FormData_top = 266;
  static const int k_FormData_width = 267;
  static const int k_GC_XORMode = 268;
  static const int k_GC_alpha = 269;
  static const int k_GC_antialias = 270;
  static const int k_GC_background = 271;
  static const int k_GC_backgroundPattern = 272;
  static const int k_GC_bufferScale = 273;
  static const int k_GC_clipping = 274;
  static const int k_GC_clippingPath = 275;
  static const int k_GC_clippingRects = 276;
  static const int k_GC_clippingText = 277;
  static const int k_GC_fillRule = 278;
  static const int k_GC_font = 279;
  static const int k_GC_foreground = 280;
  static const int k_GC_foregroundPattern = 281;
  static const int k_GC_hoverFills = 282;
  static const int k_GC_interpolation = 283;
  static const int k_GC_lineCap = 284;
  static const int k_GC_lineDash = 285;
  static const int k_GC_lineDashOffset = 286;
  static const int k_GC_lineJoin = 287;
  static const int k_GC_lineStyle = 288;
  static const int k_GC_lineWidth = 289;
  static const int k_GC_selectionFills = 290;
  static const int k_GC_style = 291;
  static const int k_GC_themedCanvas = 292;
  static const int k_GC_transform = 293;
  static const int k_GLData_accumAlphaSize = 294;
  static const int k_GLData_accumBlueSize = 295;
  static const int k_GLData_accumGreenSize = 296;
  static const int k_GLData_accumRedSize = 297;
  static const int k_GLData_alphaSize = 298;
  static const int k_GLData_blueSize = 299;
  static const int k_GLData_depthSize = 300;
  static const int k_GLData_doubleBuffer = 301;
  static const int k_GLData_greenSize = 302;
  static const int k_GLData_redSize = 303;
  static const int k_GLData_sampleBuffers = 304;
  static const int k_GLData_samples = 305;
  static const int k_GLData_shareContext = 306;
  static const int k_GLData_stencilSize = 307;
  static const int k_GLData_stereo = 308;
  static const int k_GlyphMetrics_ascent = 309;
  static const int k_GlyphMetrics_descent = 310;
  static const int k_GlyphMetrics_width = 311;
  static const int k_GridData_cacheHeight = 312;
  static const int k_GridData_cacheWidth = 313;
  static const int k_GridData_currentHeight = 314;
  static const int k_GridData_currentHhint = 315;
  static const int k_GridData_currentWhint = 316;
  static const int k_GridData_currentWidth = 317;
  static const int k_GridData_defaultHeight = 318;
  static const int k_GridData_defaultHhint = 319;
  static const int k_GridData_defaultWhint = 320;
  static const int k_GridData_defaultWidth = 321;
  static const int k_GridData_exclude = 322;
  static const int k_GridData_grabExcessHorizontalSpace = 323;
  static const int k_GridData_grabExcessVerticalSpace = 324;
  static const int k_GridData_heightHint = 325;
  static const int k_GridData_horizontalAlignment = 326;
  static const int k_GridData_horizontalIndent = 327;
  static const int k_GridData_horizontalSpan = 328;
  static const int k_GridData_minimumHeight = 329;
  static const int k_GridData_minimumWidth = 330;
  static const int k_GridData_verticalAlignment = 331;
  static const int k_GridData_verticalIndent = 332;
  static const int k_GridData_verticalSpan = 333;
  static const int k_GridData_widthHint = 334;
  static const int k_Group_text = 335;
  static const int k_Image_filename = 336;
  static const int k_Image_height = 337;
  static const int k_Image_imageData = 338;
  static const int k_Image_remoteRef = 339;
  static const int k_Image_svgContent = 340;
  static const int k_Image_width = 341;
  static const int k_ImageData_data = 342;
  static const int k_ImageData_depth = 343;
  static const int k_ImageData_height = 344;
  static const int k_ImageData_width = 345;
  static const int k_Item_image = 346;
  static const int k_Item_text = 347;
  static const int k_Label_image = 348;
  static const int k_Label_text = 349;
  static const int k_LineInfo_alignment = 350;
  static const int k_LineInfo_background = 351;
  static const int k_LineInfo_flags = 352;
  static const int k_LineInfo_indent = 353;
  static const int k_LineInfo_justify = 354;
  static const int k_LineInfo_segments = 355;
  static const int k_LineInfo_segmentsChars = 356;
  static const int k_LineInfo_tabStops = 357;
  static const int k_LineInfo_verticalIndent = 358;
  static const int k_LineInfo_wrapIndent = 359;
  static const int k_LineSizeInfo_height = 360;
  static const int k_LineSizeInfo_width = 361;
  static const int k_Link_linkForeground = 362;
  static const int k_Link_text = 363;
  static const int k_List_items = 364;
  static const int k_List_selection = 365;
  static const int k_List_topIndex = 366;
  static const int k_Menu_enabled = 367;
  static const int k_Menu_items = 368;
  static const int k_Menu_location = 369;
  static const int k_Menu_orientation = 370;
  static const int k_Menu_visible = 371;
  static const int k_MenuItem_accelerator = 372;
  static const int k_MenuItem_enabled = 373;
  static const int k_MenuItem_menu = 374;
  static const int k_MenuItem_selection = 375;
  static const int k_MenuItem_toolTipText = 376;
  static const int k_MessageBox_buttonLabels = 377;
  static const int k_MessageBox_message = 378;
  static const int k_Monitor_clientHeight = 379;
  static const int k_Monitor_clientWidth = 380;
  static const int k_Monitor_clientX = 381;
  static const int k_Monitor_clientY = 382;
  static const int k_Monitor_handle = 383;
  static const int k_Monitor_height = 384;
  static const int k_Monitor_width = 385;
  static const int k_Monitor_x = 386;
  static const int k_Monitor_y = 387;
  static const int k_Monitor_zoom = 388;
  static const int k_PDFDocument_height = 389;
  static const int k_PDFDocument_width = 390;
  static const int k_Path_pathData = 391;
  static const int k_Path_textFonts = 392;
  static const int k_Path_textOrigins = 393;
  static const int k_Path_textStrings = 394;
  static const int k_PathData_points = 395;
  static const int k_PathData_types = 396;
  static const int k_Pattern_color1 = 397;
  static const int k_Pattern_color2 = 398;
  static const int k_Pattern_endX = 399;
  static const int k_Pattern_endY = 400;
  static const int k_Pattern_image = 401;
  static const int k_Pattern_startX = 402;
  static const int k_Pattern_startY = 403;
  static const int k_Point_x = 404;
  static const int k_Point_y = 405;
  static const int k_PopupList_font = 406;
  static const int k_PopupList_items = 407;
  static const int k_PopupList_minimumWidth = 408;
  static const int k_PopupList_selection = 409;
  static const int k_PrintDialog_endPage = 410;
  static const int k_PrintDialog_printToFile = 411;
  static const int k_PrintDialog_scope = 412;
  static const int k_PrintDialog_startPage = 413;
  static const int k_ProgressBar_maximum = 414;
  static const int k_ProgressBar_minimum = 415;
  static const int k_ProgressBar_selection = 416;
  static const int k_RGB_blue = 417;
  static const int k_RGB_green = 418;
  static const int k_RGB_red = 419;
  static const int k_Rectangle_height = 420;
  static const int k_Rectangle_width = 421;
  static const int k_Rectangle_x = 422;
  static const int k_Rectangle_y = 423;
  static const int k_Region_rects = 424;
  static const int k_RowData_exclude = 425;
  static const int k_RowData_height = 426;
  static const int k_RowData_width = 427;
  static const int k_SashForm_maximizedControl = 428;
  static const int k_SashForm_sashWidth = 429;
  static const int k_SashForm_weights = 430;
  static const int k_Scale_maximum = 431;
  static const int k_Scale_minimum = 432;
  static const int k_Scale_selection = 433;
  static const int k_ScrollBar_enabled = 434;
  static const int k_ScrollBar_increment = 435;
  static const int k_ScrollBar_maximum = 436;
  static const int k_ScrollBar_minimum = 437;
  static const int k_ScrollBar_selection = 438;
  static const int k_ScrollBar_thumb = 439;
  static const int k_ScrollBar_visible = 440;
  static const int k_Scrollable_horizontalBar = 441;
  static const int k_Scrollable_verticalBar = 442;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 443;
  static const int k_ScrolledComposite_content = 444;
  static const int k_ScrolledComposite_expandHorizontal = 445;
  static const int k_ScrolledComposite_expandVertical = 446;
  static const int k_ScrolledComposite_minHeight = 447;
  static const int k_ScrolledComposite_minWidth = 448;
  static const int k_ScrolledComposite_origin = 449;
  static const int k_ScrolledComposite_originRequest = 450;
  static const int k_Shell_alpha = 451;
  static const int k_Shell_dialogs = 452;
  static const int k_Shell_fullScreen = 453;
  static const int k_Shell_maximumSize = 454;
  static const int k_Shell_minimumSize = 455;
  static const int k_Shell_modified = 456;
  static const int k_Slider_increment = 457;
  static const int k_Slider_maximum = 458;
  static const int k_Slider_minimum = 459;
  static const int k_Slider_pageIncrement = 460;
  static const int k_Slider_selection = 461;
  static const int k_Slider_thumb = 462;
  static const int k_Spinner_digits = 463;
  static const int k_Spinner_increment = 464;
  static const int k_Spinner_keptText = 465;
  static const int k_Spinner_maximum = 466;
  static const int k_Spinner_minimum = 467;
  static const int k_Spinner_pageIncrement = 468;
  static const int k_Spinner_selection = 469;
  static const int k_Spinner_textLimit = 470;
  static const int k_StyleRange_fontStyle = 471;
  static const int k_StyleRange_length = 472;
  static const int k_StyleRange_start = 473;
  static const int k_StyledText_alignment = 474;
  static const int k_StyledText_alwaysShowScrollBars = 475;
  static const int k_StyledText_blockSelection = 476;
  static const int k_StyledText_blockSelectionBounds = 477;
  static const int k_StyledText_bottomMargin = 478;
  static const int k_StyledText_caretOffset = 479;
  static const int k_StyledText_caretOffsets = 480;
  static const int k_StyledText_doubleClickEnabled = 481;
  static const int k_StyledText_editable = 482;
  static const int k_StyledText_fixedLineHeight = 483;
  static const int k_StyledText_horizontalPixel = 484;
  static const int k_StyledText_indent = 485;
  static const int k_StyledText_justify = 486;
  static const int k_StyledText_leftMargin = 487;
  static const int k_StyledText_lineSpacing = 488;
  static const int k_StyledText_marginColor = 489;
  static const int k_StyledText_renderer = 490;
  static const int k_StyledText_rightMargin = 491;
  static const int k_StyledText_selection = 492;
  static const int k_StyledText_selectionBackground = 493;
  static const int k_StyledText_selectionForeground = 494;
  static const int k_StyledText_selectionRanges = 495;
  static const int k_StyledText_styleIndex = 496;
  static const int k_StyledText_styles = 497;
  static const int k_StyledText_tabStops = 498;
  static const int k_StyledText_tabs = 499;
  static const int k_StyledText_text = 500;
  static const int k_StyledText_topMargin = 501;
  static const int k_StyledText_topPixel = 502;
  static const int k_StyledText_wordWrap = 503;
  static const int k_StyledText_wrapIndent = 504;
  static const int k_StyledTextPrintOptions_footer = 505;
  static const int k_StyledTextPrintOptions_header = 506;
  static const int k_StyledTextPrintOptions_jobName = 507;
  static const int k_StyledTextPrintOptions_lineLabels = 508;
  static const int k_StyledTextPrintOptions_printLineBackground = 509;
  static const int k_StyledTextPrintOptions_printLineNumbers = 510;
  static const int k_StyledTextPrintOptions_printTextBackground = 511;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 512;
  static const int k_StyledTextPrintOptions_printTextForeground = 513;
  static const int k_StyledTextRenderer_ascent = 514;
  static const int k_StyledTextRenderer_averageCharWidth = 515;
  static const int k_StyledTextRenderer_averageLineHeight = 516;
  static const int k_StyledTextRenderer_boldFont = 517;
  static const int k_StyledTextRenderer_boldItalicFont = 518;
  static const int k_StyledTextRenderer_bulletTexts = 519;
  static const int k_StyledTextRenderer_bulletsIndices = 520;
  static const int k_StyledTextRenderer_descent = 521;
  static const int k_StyledTextRenderer_fixedPitch = 522;
  static const int k_StyledTextRenderer_hasLinks = 523;
  static const int k_StyledTextRenderer_idleRunning = 524;
  static const int k_StyledTextRenderer_italicFont = 525;
  static const int k_StyledTextRenderer_lineCount = 526;
  static const int k_StyledTextRenderer_lineSizes = 527;
  static const int k_StyledTextRenderer_lineSpacingComputing = 528;
  static const int k_StyledTextRenderer_lineSpacings = 529;
  static const int k_StyledTextRenderer_lines = 530;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 531;
  static const int k_StyledTextRenderer_maxWidth = 532;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 533;
  static const int k_StyledTextRenderer_ranges = 534;
  static const int k_StyledTextRenderer_redrawLines = 535;
  static const int k_StyledTextRenderer_regularFont = 536;
  static const int k_StyledTextRenderer_styleCount = 537;
  static const int k_StyledTextRenderer_styledText = 538;
  static const int k_StyledTextRenderer_styles = 539;
  static const int k_StyledTextRenderer_stylesSet = 540;
  static const int k_StyledTextRenderer_stylesSetCount = 541;
  static const int k_StyledTextRenderer_tabLength = 542;
  static const int k_StyledTextRenderer_tabWidth = 543;
  static const int k_StyledTextRenderer_topIndex = 544;
  static const int k_TabFolder_items = 545;
  static const int k_TabFolder_selection = 546;
  static const int k_TabItem_control = 547;
  static const int k_TabItem_toolTipText = 548;
  static const int k_Table_columnOrder = 549;
  static const int k_Table_columns = 550;
  static const int k_Table_editors = 551;
  static const int k_Table_headerBackground = 552;
  static const int k_Table_headerForeground = 553;
  static const int k_Table_headerVisible = 554;
  static const int k_Table_itemCount = 555;
  static const int k_Table_items = 556;
  static const int k_Table_linesVisible = 557;
  static const int k_Table_ownerDrawItemHeight = 558;
  static const int k_Table_selection = 559;
  static const int k_Table_topIndex = 560;
  static const int k_TableColumn_alignment = 561;
  static const int k_TableColumn_moveable = 562;
  static const int k_TableColumn_resizable = 563;
  static const int k_TableColumn_width = 564;
  static const int k_TableEditor_column = 565;
  static const int k_TableEditor_item = 566;
  static const int k_TableItem_background = 567;
  static const int k_TableItem_backgrounds = 568;
  static const int k_TableItem_checked = 569;
  static const int k_TableItem_font = 570;
  static const int k_TableItem_fonts = 571;
  static const int k_TableItem_foreground = 572;
  static const int k_TableItem_foregrounds = 573;
  static const int k_TableItem_grayed = 574;
  static const int k_TableItem_images = 575;
  static const int k_TableItem_paintedTexts = 576;
  static const int k_TableItem_texts = 577;
  static const int k_Text_caretPosition = 578;
  static const int k_Text_echoCharacter = 579;
  static const int k_Text_editTexts = 580;
  static const int k_Text_editable = 581;
  static const int k_Text_edits = 582;
  static const int k_Text_message = 583;
  static const int k_Text_selection = 584;
  static const int k_Text_text = 585;
  static const int k_Text_textLimit = 586;
  static const int k_TextStyle_background = 587;
  static const int k_TextStyle_borderColor = 588;
  static const int k_TextStyle_borderStyle = 589;
  static const int k_TextStyle_font = 590;
  static const int k_TextStyle_foreground = 591;
  static const int k_TextStyle_metrics = 592;
  static const int k_TextStyle_rise = 593;
  static const int k_TextStyle_strikeout = 594;
  static const int k_TextStyle_strikeoutColor = 595;
  static const int k_TextStyle_underline = 596;
  static const int k_TextStyle_underlineColor = 597;
  static const int k_TextStyle_underlineStyle = 598;
  static const int k_ToolBar_items = 599;
  static const int k_ToolItem_background = 600;
  static const int k_ToolItem_control = 601;
  static const int k_ToolItem_disabledImage = 602;
  static const int k_ToolItem_enabled = 603;
  static const int k_ToolItem_foreground = 604;
  static const int k_ToolItem_hotImage = 605;
  static const int k_ToolItem_selection = 606;
  static const int k_ToolItem_toolTipText = 607;
  static const int k_ToolItem_width = 608;
  static const int k_ToolTip_location = 609;
  static const int k_ToolTip_message = 610;
  static const int k_ToolTip_text = 611;
  static const int k_ToolTip_visible = 612;
  static const int k_Touch_id = 613;
  static const int k_Touch_primary = 614;
  static const int k_Touch_source = 615;
  static const int k_Touch_state = 616;
  static const int k_Touch_x = 617;
  static const int k_Touch_y = 618;
  static const int k_TouchSource_bounds = 619;
  static const int k_TouchSource_direct = 620;
  static const int k_TouchSource_handle = 621;
  static const int k_Transform_elements = 622;
  static const int k_Tree_columnOrder = 623;
  static const int k_Tree_columns = 624;
  static const int k_Tree_editors = 625;
  static const int k_Tree_headerBackground = 626;
  static const int k_Tree_headerForeground = 627;
  static const int k_Tree_headerVisible = 628;
  static const int k_Tree_items = 629;
  static const int k_Tree_linesVisible = 630;
  static const int k_Tree_ownerDrawItemHeight = 631;
  static const int k_Tree_selection = 632;
  static const int k_Tree_sortColumn = 633;
  static const int k_Tree_sortDirection = 634;
  static const int k_Tree_topItem = 635;
  static const int k_TreeColumn_alignment = 636;
  static const int k_TreeColumn_moveable = 637;
  static const int k_TreeColumn_resizable = 638;
  static const int k_TreeColumn_width = 639;
  static const int k_TreeEditor_column = 640;
  static const int k_TreeEditor_editor = 641;
  static const int k_TreeEditor_item = 642;
  static const int k_TreeItem_background = 643;
  static const int k_TreeItem_backgrounds = 644;
  static const int k_TreeItem_checked = 645;
  static const int k_TreeItem_expanded = 646;
  static const int k_TreeItem_font = 647;
  static const int k_TreeItem_fonts = 648;
  static const int k_TreeItem_foreground = 649;
  static const int k_TreeItem_foregrounds = 650;
  static const int k_TreeItem_grayed = 651;
  static const int k_TreeItem_images = 652;
  static const int k_TreeItem_itemCount = 653;
  static const int k_TreeItem_items = 654;
  static const int k_TreeItem_paintedTexts = 655;
  static const int k_TreeItem_texts = 656;
  static const int k_Widget_style = 657;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  1,
  40,
  57,
  61,
  68,
  86,
  143,
  148,
  151,
  153,
  196,
  199,
  214,
  220,
  222,
  242,
  368,
  374,
  428,
  441,
  442,
  444,
  545,
  546,
  547,
  550,
  551,
  556,
  566,
  599,
  601,
  624,
  625,
  629,
  632,
  633,
  635,
  641,
  642,
  654,
};

/// Every type's number, for what names a widget by its type's number.
abstract final class WireType {
  static const int accessible = 1; // Accessible
  static const int animatedProgress = 2; // AnimatedProgress
  static const int benchBridge = 3; // BenchBridge
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
}

/// Type names by the number a frame names a value's class with.
const Map<int, String> wireTypeNames = {
  1: 'Accessible',
  2: 'AnimatedProgress',
  3: 'BenchBridge',
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
  136: 'b',
  137: 'back',
  138: 'bench/d2j/probe',
  139: 'bench/d2j/probeResponse',
  140: 'bench/d2j/result',
  141: 'bench/d2j/run',
  142: 'bench/j2d/echo',
  143: 'bench/j2d/echoResponse',
  144: 'browserFunctionCall',
  145: 'c',
  146: 'close',
  147: 'cookie',
  148: 'copyAreaImageintint',
  149: 'copyAreaImageintintResponse',
  150: 'copyAreaintintintintintintboolean',
  151: 'drawArcintintintintintint',
  152: 'drawFocusintintintint',
  153: 'drawImageImageintint',
  154: 'drawImageImageintintintint',
  155: 'drawImageImageintintintintintintintint',
  156: 'drawLineintintintint',
  157: 'drawOvalintintintint',
  158: 'drawPathPath',
  159: 'drawPointintint',
  160: 'drawPolygonint',
  161: 'drawPolylineint',
  162: 'drawRectangleintintintint',
  163: 'drawRoundRectangleintintintintintint',
  164: 'drawTextStringintintint',
  165: 'evaluate',
  166: 'evaluate/result',
  167: 'evolve.test.drag',
  168: 'evolve.test.dragDone',
  169: 'evolve.test.frameCost',
  170: 'evolve.test.frameCostResponse',
  171: 'evolve.test.frameStats',
  172: 'evolve.test.frameStatsResponse',
  173: 'evolve.test.frameSync',
  174: 'evolve.test.frameSynced',
  175: 'evolve.test.iframeLoaded',
  176: 'evolve.test.query',
  177: 'evolve.test.queryResponse',
  178: 'evolve.test.styledTextPerf',
  179: 'evolve.test.styledTextPerfResponse',
  180: 'evolve.test.wireStats',
  181: 'evolve.test.wireStatsResponse',
  182: 'execute',
  183: 'fillArcintintintintintint',
  184: 'fillGradientRectangleintintintintboolean',
  185: 'fillOvalintintintint',
  186: 'fillPathPath',
  187: 'fillPolygonint',
  188: 'fillRectangleintintintint',
  189: 'fillRoundRectangleintintintintintint',
  190: 'fontSizeRequest',
  191: 'fontSizeResponse',
  192: 'forward',
  193: 'gcDispose',
  194: 'getCookie',
  195: 'imageInit',
  196: 'imageResult',
  197: 'imageSizeRequest',
  198: 'imageSizeResponse',
  199: 'imageSnapshotResult',
  200: 'key/verdict',
  201: 'key/vetoable',
  202: 'locationchanging/result',
  203: 'menu/verdict',
  204: 'modify/verdict',
  205: 'modify/vetoable',
  206: 'navigate',
  207: 'navigateRequest',
  208: 'navigationState',
  209: 'nextFrame',
  210: 'openMenu',
  211: 'pixelsResult',
  212: 'reload',
  213: 'renderSnapshot',
  214: 'setCookie',
  215: 'shown',
  216: 'stop',
  217: 'swing',
  218: 'swing.open',
  219: 'swingIsland',
  220: 'swingIsland.close',
  221: 'swingIsland.dispose',
  222: 'swingIsland.island',
  223: 'swingIsland.opened',
  224: 'swingIsland.resize',
  225: 'swingIsland.surface',
  226: 'swingIsland.title',
  227: 'swt.evolve.batch',
  228: 'swt.evolve.focus',
  229: 'swt.evolve.focus.seen',
  230: 'swt.evolve.focusHolder',
  231: 'swt.evolve.properties',
  232: 'swt.evolve.property.set',
  233: 'swt.evolve.test.runUi',
  234: 'swt.evolve.test.runUiResponse',
  235: 'swt.evolve.timing.bare',
  236: 'swt.evolve.timing.ping',
  237: 'swt.evolve.timing.pong',
  238: 'swt.evolve.url.open',
  239: 'swt.evolve.widget.refresh',
  240: 'swt.evolve.wire',
  241: 'traverse/verdict',
  242: 'traverse/vetoable',
  243: 'v',
  244: 'widgetSizeRequest',
  245: 'widgetSizeResponse',
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
  static const int b = 136; // b
  static const int back = 137; // back
  static const int benchD2jProbe = 138; // bench/d2j/probe
  static const int benchD2jProbeResponse = 139; // bench/d2j/probeResponse
  static const int benchD2jResult = 140; // bench/d2j/result
  static const int benchD2jRun = 141; // bench/d2j/run
  static const int benchJ2dEcho = 142; // bench/j2d/echo
  static const int benchJ2dEchoResponse = 143; // bench/j2d/echoResponse
  static const int browserFunctionCall = 144; // browserFunctionCall
  static const int c = 145; // c
  static const int close = 146; // close
  static const int cookie = 147; // cookie
  static const int copyAreaImageintint = 148; // copyAreaImageintint
  static const int copyAreaImageintintResponse =
      149; // copyAreaImageintintResponse
  static const int copyAreaintintintintintintboolean =
      150; // copyAreaintintintintintintboolean
  static const int drawArcintintintintintint = 151; // drawArcintintintintintint
  static const int drawFocusintintintint = 152; // drawFocusintintintint
  static const int drawImageImageintint = 153; // drawImageImageintint
  static const int drawImageImageintintintint =
      154; // drawImageImageintintintint
  static const int drawImageImageintintintintintintintint =
      155; // drawImageImageintintintintintintintint
  static const int drawLineintintintint = 156; // drawLineintintintint
  static const int drawOvalintintintint = 157; // drawOvalintintintint
  static const int drawPathPath = 158; // drawPathPath
  static const int drawPointintint = 159; // drawPointintint
  static const int drawPolygonint = 160; // drawPolygonint
  static const int drawPolylineint = 161; // drawPolylineint
  static const int drawRectangleintintintint = 162; // drawRectangleintintintint
  static const int drawRoundRectangleintintintintintint =
      163; // drawRoundRectangleintintintintintint
  static const int drawTextStringintintint = 164; // drawTextStringintintint
  static const int evaluate = 165; // evaluate
  static const int evaluateResult = 166; // evaluate/result
  static const int evolveTestDrag = 167; // evolve.test.drag
  static const int evolveTestDragDone = 168; // evolve.test.dragDone
  static const int evolveTestFrameCost = 169; // evolve.test.frameCost
  static const int evolveTestFrameCostResponse =
      170; // evolve.test.frameCostResponse
  static const int evolveTestFrameStats = 171; // evolve.test.frameStats
  static const int evolveTestFrameStatsResponse =
      172; // evolve.test.frameStatsResponse
  static const int evolveTestFrameSync = 173; // evolve.test.frameSync
  static const int evolveTestFrameSynced = 174; // evolve.test.frameSynced
  static const int evolveTestIframeLoaded = 175; // evolve.test.iframeLoaded
  static const int evolveTestQuery = 176; // evolve.test.query
  static const int evolveTestQueryResponse = 177; // evolve.test.queryResponse
  static const int evolveTestStyledTextPerf = 178; // evolve.test.styledTextPerf
  static const int evolveTestStyledTextPerfResponse =
      179; // evolve.test.styledTextPerfResponse
  static const int evolveTestWireStats = 180; // evolve.test.wireStats
  static const int evolveTestWireStatsResponse =
      181; // evolve.test.wireStatsResponse
  static const int execute = 182; // execute
  static const int fillArcintintintintintint = 183; // fillArcintintintintintint
  static const int fillGradientRectangleintintintintboolean =
      184; // fillGradientRectangleintintintintboolean
  static const int fillOvalintintintint = 185; // fillOvalintintintint
  static const int fillPathPath = 186; // fillPathPath
  static const int fillPolygonint = 187; // fillPolygonint
  static const int fillRectangleintintintint = 188; // fillRectangleintintintint
  static const int fillRoundRectangleintintintintintint =
      189; // fillRoundRectangleintintintintintint
  static const int fontSizeRequest = 190; // fontSizeRequest
  static const int fontSizeResponse = 191; // fontSizeResponse
  static const int forward = 192; // forward
  static const int gcDispose = 193; // gcDispose
  static const int getCookie = 194; // getCookie
  static const int imageInit = 195; // imageInit
  static const int imageResult = 196; // imageResult
  static const int imageSizeRequest = 197; // imageSizeRequest
  static const int imageSizeResponse = 198; // imageSizeResponse
  static const int imageSnapshotResult = 199; // imageSnapshotResult
  static const int keyVerdict = 200; // key/verdict
  static const int keyVetoable = 201; // key/vetoable
  static const int locationchangingResult = 202; // locationchanging/result
  static const int menuVerdict = 203; // menu/verdict
  static const int modifyVerdict = 204; // modify/verdict
  static const int modifyVetoable = 205; // modify/vetoable
  static const int navigate = 206; // navigate
  static const int navigateRequest = 207; // navigateRequest
  static const int navigationState = 208; // navigationState
  static const int nextFrame = 209; // nextFrame
  static const int openMenu = 210; // openMenu
  static const int pixelsResult = 211; // pixelsResult
  static const int reload = 212; // reload
  static const int renderSnapshot = 213; // renderSnapshot
  static const int setCookie = 214; // setCookie
  static const int shown = 215; // shown
  static const int stop = 216; // stop
  static const int swing = 217; // swing
  static const int swingOpen = 218; // swing.open
  static const int swingIsland = 219; // swingIsland
  static const int swingIslandClose = 220; // swingIsland.close
  static const int swingIslandDispose = 221; // swingIsland.dispose
  static const int swingIslandIsland = 222; // swingIsland.island
  static const int swingIslandOpened = 223; // swingIsland.opened
  static const int swingIslandResize = 224; // swingIsland.resize
  static const int swingIslandSurface = 225; // swingIsland.surface
  static const int swingIslandTitle = 226; // swingIsland.title
  static const int swtEvolveBatch = 227; // swt.evolve.batch
  static const int swtEvolveFocus = 228; // swt.evolve.focus
  static const int swtEvolveFocusSeen = 229; // swt.evolve.focus.seen
  static const int swtEvolveFocusHolder = 230; // swt.evolve.focusHolder
  static const int swtEvolveProperties = 231; // swt.evolve.properties
  static const int swtEvolvePropertySet = 232; // swt.evolve.property.set
  static const int swtEvolveTestRunUi = 233; // swt.evolve.test.runUi
  static const int swtEvolveTestRunUiResponse =
      234; // swt.evolve.test.runUiResponse
  static const int swtEvolveTimingBare = 235; // swt.evolve.timing.bare
  static const int swtEvolveTimingPing = 236; // swt.evolve.timing.ping
  static const int swtEvolveTimingPong = 237; // swt.evolve.timing.pong
  static const int swtEvolveUrlOpen = 238; // swt.evolve.url.open
  static const int swtEvolveWidgetRefresh = 239; // swt.evolve.widget.refresh
  static const int swtEvolveWire = 240; // swt.evolve.wire
  static const int traverseVerdict = 241; // traverse/verdict
  static const int traverseVetoable = 242; // traverse/vetoable
  static const int v = 243; // v
  static const int widgetSizeRequest = 244; // widgetSizeRequest
  static const int widgetSizeResponse = 245; // widgetSizeResponse
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
  46: 'bottomMargin',
  47: 'bounds',
  48: 'browser',
  49: 'bufferScale',
  50: 'bulletTexts',
  51: 'bulletsIndices',
  52: 'button',
  53: 'buttonLabels',
  54: 'cacheBottom',
  55: 'cacheHeight',
  56: 'cacheLeft',
  57: 'cacheRight',
  58: 'cacheTop',
  59: 'cacheWidth',
  60: 'caret',
  61: 'caretOffset',
  62: 'caretOffsets',
  63: 'caretPosition',
  64: 'charCount',
  65: 'character',
  66: 'checked',
  67: 'chevronVisible',
  68: 'children',
  69: 'clientHeight',
  70: 'clientWidth',
  71: 'clientX',
  72: 'clientY',
  73: 'clipping',
  74: 'clippingPath',
  75: 'clippingRects',
  76: 'clippingText',
  77: 'color1',
  78: 'color2',
  79: 'column',
  80: 'columnOrder',
  81: 'columns',
  82: 'config',
  83: 'content',
  84: 'control',
  85: 'cookie',
  86: 'count',
  87: 'csd_maximize',
  88: 'csd_os',
  89: 'csd_placement',
  90: 'csd_titlebar_color',
  91: 'ctabfolder_tab_scroll',
  92: 'ctabfolder_topright_auto_hide',
  93: 'ctabfolder_visible_controls',
  94: 'currentDataTypeId',
  95: 'currentHeight',
  96: 'currentHhint',
  97: 'currentWhint',
  98: 'currentWidth',
  99: 'cursor',
  100: 'cursorStyle',
  101: 'custom_tooltip',
  102: 'damage',
  103: 'data',
  104: 'day',
  105: 'decimal_separator',
  106: 'decorations_align',
  107: 'defaultHeight',
  108: 'defaultHhint',
  109: 'defaultWhint',
  110: 'defaultWidth',
  111: 'denominator',
  112: 'depth',
  113: 'depthSize',
  114: 'descent',
  115: 'detail',
  116: 'dialogs',
  117: 'digits',
  118: 'direct',
  119: 'disable_control_gc_overlay',
  120: 'disable_evolve_icons',
  121: 'disable_hover_zoom',
  122: 'disable_swt_canvas_colors',
  123: 'disabledImage',
  124: 'doit',
  125: 'doubleBuffer',
  126: 'doubleClickEnabled',
  127: 'double_click_timeout_ms',
  128: 'dragDetect',
  129: 'dragImage',
  130: 'dragSource',
  131: 'dragSourceId',
  132: 'dropTargetId',
  133: 'echoCharacter',
  134: 'editTexts',
  135: 'editable',
  136: 'editor',
  137: 'editors',
  138: 'edits',
  139: 'effectsVisible',
  140: 'elements',
  141: 'enabled',
  142: 'end',
  143: 'endPage',
  144: 'endX',
  145: 'endY',
  146: 'error',
  147: 'exclude',
  148: 'expandHorizontal',
  149: 'expandVertical',
  150: 'expanded',
  151: 'expires',
  152: 'feedback',
  153: 'fileName',
  154: 'fileNames',
  155: 'filename',
  156: 'fillRule',
  157: 'filterExtensions',
  158: 'filterIndex',
  159: 'filterNames',
  160: 'filterPath',
  161: 'fixedLineHeight',
  162: 'fixedPitch',
  163: 'flags',
  164: 'focus',
  165: 'focusHolderId',
  166: 'focus_indicators',
  167: 'focused',
  168: 'font',
  169: 'fontData',
  170: 'fontList',
  171: 'fontStyle',
  172: 'font_point_scale',
  173: 'fonts',
  174: 'footer',
  175: 'force_theme',
  176: 'foreground',
  177: 'foregroundPattern',
  178: 'foregrounds',
  179: 'fullRepaint',
  180: 'fullScreen',
  181: 'functionNames',
  182: 'g',
  183: 'gc',
  184: 'gc_icons_replacement',
  185: 'grabExcessHorizontalSpace',
  186: 'grabExcessVerticalSpace',
  187: 'gradientColors',
  188: 'gradientPercents',
  189: 'gradientVertical',
  190: 'grayed',
  191: 'green',
  192: 'greenSize',
  193: 'hHint',
  194: 'handle',
  195: 'hasLinks',
  196: 'hasOwnBackground',
  197: 'header',
  198: 'headerBackground',
  199: 'headerForeground',
  200: 'headerHeight',
  201: 'headerVisible',
  202: 'headers',
  203: 'height',
  204: 'heightHint',
  205: 'highlight',
  206: 'highlightEnabled',
  207: 'horizontalAlignment',
  208: 'horizontalBar',
  209: 'horizontalIndent',
  210: 'horizontalPixel',
  211: 'horizontalSpan',
  212: 'hotImage',
  213: 'hotspotX',
  214: 'hotspotY',
  215: 'hours',
  216: 'hoverFills',
  217: 'hover_delay_ms',
  218: 'id',
  219: 'idleRunning',
  220: 'image',
  221: 'imageData',
  222: 'image_disable_icons_replacement',
  223: 'images',
  224: 'increment',
  225: 'indent',
  226: 'index',
  227: 'inheritsBackground',
  228: 'interpolation',
  229: 'isVisited',
  230: 'italicFont',
  231: 'item',
  232: 'itemCount',
  233: 'itemId',
  234: 'itemOrder',
  235: 'itemSizes',
  236: 'itemTooltips',
  237: 'items',
  238: 'javascriptEnabled',
  239: 'jobName',
  240: 'justify',
  241: 'keptText',
  242: 'keyCode',
  243: 'keyLocation',
  244: 'left',
  245: 'leftMargin',
  246: 'length',
  247: 'lineCap',
  248: 'lineCount',
  249: 'lineDash',
  250: 'lineDashOffset',
  251: 'lineJoin',
  252: 'lineLabels',
  253: 'lineSizes',
  254: 'lineSpacing',
  255: 'lineSpacingComputing',
  256: 'lineSpacings',
  257: 'lineStyle',
  258: 'lineWidth',
  259: 'lines',
  260: 'linesInAverageLineHeight',
  261: 'linesVisible',
  262: 'linkForeground',
  263: 'listVisible',
  264: 'localFileBase',
  265: 'localFilePath',
  266: 'localFileText',
  267: 'locale',
  268: 'location',
  269: 'magnification',
  270: 'mainShellId',
  271: 'map',
  272: 'marginColor',
  273: 'maxWidth',
  274: 'maxWidthLineIndex',
  275: 'maximizeVisible',
  276: 'maximized',
  277: 'maximizedControl',
  278: 'maximum',
  279: 'maximumSize',
  280: 'menu',
  281: 'menuBar',
  282: 'menuStripHeight',
  283: 'message',
  284: 'metrics',
  285: 'minHeight',
  286: 'minWidth',
  287: 'minimizeVisible',
  288: 'minimized',
  289: 'minimum',
  290: 'minimumCharacters',
  291: 'minimumHeight',
  292: 'minimumSize',
  293: 'minimumWidth',
  294: 'minutes',
  295: 'modified',
  296: 'month',
  297: 'moveable',
  298: 'name',
  299: 'needed',
  300: 'notification_popup',
  301: 'numerator',
  302: 'offset',
  303: 'operations',
  304: 'orientation',
  305: 'origin',
  306: 'originRequest',
  307: 'overwrite',
  308: 'ownMenuStrip',
  309: 'ownerDrawItemHeight',
  310: 'pageIncrement',
  311: 'paintedTexts',
  312: 'pathData',
  313: 'pixels',
  314: 'points',
  315: 'popups',
  316: 'port',
  317: 'postData',
  318: 'preferredSize',
  319: 'preserve_icon_colors',
  320: 'primary',
  321: 'printLineBackground',
  322: 'printLineNumbers',
  323: 'printTextBackground',
  324: 'printTextFontStyle',
  325: 'printTextForeground',
  326: 'printToFile',
  327: 'print_move',
  328: 'push_button_margin',
  329: 'r',
  330: 'ranges',
  331: 'rects',
  332: 'red',
  333: 'redSize',
  334: 'redrawLines',
  335: 'ref',
  336: 'region',
  337: 'regularFont',
  338: 'remoteRef',
  339: 'renderer',
  340: 'replaced',
  341: 'reqId',
  342: 'resizable',
  343: 'result',
  344: 'retain',
  345: 'right',
  346: 'rightMargin',
  347: 'rise',
  348: 'rotation',
  349: 'sampleBuffers',
  350: 'samples',
  351: 'sashWidth',
  352: 'scale_vertical_minimum_at_top',
  353: 'scope',
  354: 'script',
  355: 'seconds',
  356: 'segments',
  357: 'segmentsChars',
  358: 'selectedImageVisible',
  359: 'selection',
  360: 'selectionBackground',
  361: 'selectionBarThickness',
  362: 'selectionBgImage',
  363: 'selectionFills',
  364: 'selectionForeground',
  365: 'selectionRanges',
  366: 'seq',
  367: 'shareContext',
  368: 'shells',
  369: 'showChevron',
  370: 'showClose',
  371: 'showListPopupSeq',
  372: 'show_scaling_control',
  373: 'show_theme_color_palette',
  374: 'showing',
  375: 'single',
  376: 'sortColumn',
  377: 'sortDirection',
  378: 'source',
  379: 'spacing',
  380: 'start',
  381: 'startPage',
  382: 'startX',
  383: 'startY',
  384: 'state',
  385: 'stateMask',
  386: 'stencilSize',
  387: 'stereo',
  388: 'strikeout',
  389: 'strikeoutColor',
  390: 'style',
  391: 'styleCount',
  392: 'styleIndex',
  393: 'styledText',
  394: 'styles',
  395: 'stylesSet',
  396: 'stylesSetCount',
  397: 'svgContent',
  398: 'swt',
  399: 'systemMenu',
  400: 'system_menu_bar',
  401: 'tabHeight',
  402: 'tabLength',
  403: 'tabPosition',
  404: 'tabStop',
  405: 'tabStops',
  406: 'tabWidth',
  407: 'tabs',
  408: 'text',
  409: 'textFonts',
  410: 'textLimit',
  411: 'textOrigins',
  412: 'textStrings',
  413: 'texts',
  414: 'theme_color',
  415: 'theme_colors_by_widget',
  416: 'theme_name',
  417: 'themedCanvas',
  418: 'thumb',
  419: 'time',
  420: 'toolTipText',
  421: 'tooltips',
  422: 'top',
  423: 'topIndex',
  424: 'topItem',
  425: 'topMargin',
  426: 'topPixel',
  427: 'topRight',
  428: 'topRightAlignment',
  429: 'touches',
  430: 'tracking',
  431: 'transform',
  432: 'transparent_menu_check_box',
  433: 'type',
  434: 'types',
  435: 'ui_zoom',
  436: 'underline',
  437: 'underlineColor',
  438: 'underlineStyle',
  439: 'unselectedCloseVisible',
  440: 'unselectedImageVisible',
  441: 'url',
  442: 'use_special_dropdown_button',
  443: 'use_swt_border',
  444: 'use_swt_colors',
  445: 'use_swt_font_colors',
  446: 'use_swt_font_colors_by_widget',
  447: 'use_swt_fonts',
  448: 'value',
  449: 'verdict',
  450: 'verticalAlignment',
  451: 'verticalBar',
  452: 'verticalIndent',
  453: 'verticalSpan',
  454: 'visible',
  455: 'wHint',
  456: 'warnings',
  457: 'weights',
  458: 'widget',
  459: 'width',
  460: 'widthHint',
  461: 'windowId',
  462: 'windowedShellIds',
  463: 'wordWrap',
  464: 'wrapIndent',
  465: 'wrapIndices',
  466: 'x',
  467: 'xDirection',
  468: 'y',
  469: 'yDirection',
  470: 'year',
  471: 'zoom',
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
