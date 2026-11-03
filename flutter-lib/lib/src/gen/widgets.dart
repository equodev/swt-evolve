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
const int wireFingerprint = -822819000;

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
  static const int k_ConfigFlags_csd_maximize = 88;
  static const int k_ConfigFlags_csd_os = 89;
  static const int k_ConfigFlags_csd_placement = 90;
  static const int k_ConfigFlags_csd_titlebar_color = 91;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 92;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 93;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 94;
  static const int k_ConfigFlags_custom_tooltip = 95;
  static const int k_ConfigFlags_decimal_separator = 96;
  static const int k_ConfigFlags_decorations_align = 97;
  static const int k_ConfigFlags_disable_control_gc_overlay = 98;
  static const int k_ConfigFlags_disable_evolve_icons = 99;
  static const int k_ConfigFlags_disable_hover_zoom = 100;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 101;
  static const int k_ConfigFlags_double_click_timeout_ms = 102;
  static const int k_ConfigFlags_focus_indicators = 103;
  static const int k_ConfigFlags_font_point_scale = 104;
  static const int k_ConfigFlags_force_theme = 105;
  static const int k_ConfigFlags_gc_icons_replacement = 106;
  static const int k_ConfigFlags_hover_delay_ms = 107;
  static const int k_ConfigFlags_image_disable_icons_replacement = 108;
  static const int k_ConfigFlags_locale = 109;
  static const int k_ConfigFlags_notification_popup = 110;
  static const int k_ConfigFlags_preserve_icon_colors = 111;
  static const int k_ConfigFlags_print_move = 112;
  static const int k_ConfigFlags_push_button_margin = 113;
  static const int k_ConfigFlags_scale_vertical_minimum_at_top = 114;
  static const int k_ConfigFlags_show_scaling_control = 115;
  static const int k_ConfigFlags_show_theme_color_palette = 116;
  static const int k_ConfigFlags_system_menu_bar = 117;
  static const int k_ConfigFlags_theme_color = 118;
  static const int k_ConfigFlags_theme_colors_by_widget = 119;
  static const int k_ConfigFlags_theme_name = 120;
  static const int k_ConfigFlags_transparent_menu_check_box = 121;
  static const int k_ConfigFlags_ui_zoom = 122;
  static const int k_ConfigFlags_use_special_dropdown_button = 123;
  static const int k_ConfigFlags_use_swt_border = 124;
  static const int k_ConfigFlags_use_swt_colors = 125;
  static const int k_ConfigFlags_use_swt_font_colors = 126;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 127;
  static const int k_ConfigFlags_use_swt_fonts = 128;
  static const int k_Control_background = 129;
  static const int k_Control_backgroundImage = 130;
  static const int k_Control_bounds = 131;
  static const int k_Control_cursor = 132;
  static const int k_Control_dragDetect = 133;
  static const int k_Control_dragSource = 134;
  static const int k_Control_dragSourceId = 135;
  static const int k_Control_dropTargetId = 136;
  static const int k_Control_enabled = 137;
  static const int k_Control_font = 138;
  static const int k_Control_foreground = 139;
  static const int k_Control_hasOwnBackground = 140;
  static const int k_Control_inheritsBackground = 141;
  static const int k_Control_menu = 142;
  static const int k_Control_region = 143;
  static const int k_Control_tabStop = 144;
  static const int k_Control_toolTipText = 145;
  static const int k_Control_visible = 146;
  static const int k_ControlEditor_editor = 147;
  static const int k_CoolBar_itemOrder = 148;
  static const int k_CoolBar_itemSizes = 149;
  static const int k_CoolBar_items = 150;
  static const int k_CoolBar_wrapIndices = 151;
  static const int k_CoolItem_control = 152;
  static const int k_CoolItem_preferredSize = 153;
  static const int k_Cursor_cursorStyle = 154;
  static const int k_Cursor_hotspotX = 155;
  static const int k_Cursor_hotspotY = 156;
  static const int k_Cursor_image = 157;
  static const int k_DateTime_day = 158;
  static const int k_DateTime_hours = 159;
  static const int k_DateTime_minutes = 160;
  static const int k_DateTime_month = 161;
  static const int k_DateTime_seconds = 162;
  static const int k_DateTime_year = 163;
  static const int k_Decorations_menuBar = 164;
  static const int k_Decorations_menuStripHeight = 165;
  static const int k_Decorations_ownMenuStrip = 166;
  static const int k_Decorations_text = 167;
  static const int k_Device_fontList = 168;
  static const int k_Device_tracking = 169;
  static const int k_Device_warnings = 170;
  static const int k_Dialog_id = 171;
  static const int k_Dialog_style = 172;
  static const int k_Dialog_swt = 173;
  static const int k_Dialog_text = 174;
  static const int k_DirectoryDialog_filterPath = 175;
  static const int k_DirectoryDialog_message = 176;
  static const int k_Display_activeShellId = 177;
  static const int k_Display_config = 178;
  static const int k_Display_focusHolderId = 179;
  static const int k_Display_mainShellId = 180;
  static const int k_Display_popups = 181;
  static const int k_Display_shells = 182;
  static const int k_Display_systemMenu = 183;
  static const int k_Display_tooltips = 184;
  static const int k_Display_windowedShellIds = 185;
  static const int k_Event_button = 186;
  static const int k_Event_character = 187;
  static const int k_Event_count = 188;
  static const int k_Event_currentDataTypeId = 189;
  static const int k_Event_detail = 190;
  static const int k_Event_doit = 191;
  static const int k_Event_dragImage = 192;
  static const int k_Event_end = 193;
  static const int k_Event_feedback = 194;
  static const int k_Event_gc = 195;
  static const int k_Event_height = 196;
  static const int k_Event_index = 197;
  static const int k_Event_item = 198;
  static const int k_Event_itemId = 199;
  static const int k_Event_keyCode = 200;
  static const int k_Event_keyLocation = 201;
  static const int k_Event_magnification = 202;
  static const int k_Event_operations = 203;
  static const int k_Event_rotation = 204;
  static const int k_Event_segments = 205;
  static const int k_Event_segmentsChars = 206;
  static const int k_Event_start = 207;
  static const int k_Event_stateMask = 208;
  static const int k_Event_text = 209;
  static const int k_Event_time = 210;
  static const int k_Event_touches = 211;
  static const int k_Event_type = 212;
  static const int k_Event_widget = 213;
  static const int k_Event_width = 214;
  static const int k_Event_x = 215;
  static const int k_Event_xDirection = 216;
  static const int k_Event_y = 217;
  static const int k_Event_yDirection = 218;
  static const int k_ExpandBar_items = 219;
  static const int k_ExpandBar_spacing = 220;
  static const int k_ExpandItem_control = 221;
  static const int k_ExpandItem_expanded = 222;
  static const int k_ExpandItem_headerHeight = 223;
  static const int k_ExpandItem_height = 224;
  static const int k_FileDialog_fileName = 225;
  static const int k_FileDialog_fileNames = 226;
  static const int k_FileDialog_filterExtensions = 227;
  static const int k_FileDialog_filterIndex = 228;
  static const int k_FileDialog_filterNames = 229;
  static const int k_FileDialog_filterPath = 230;
  static const int k_FileDialog_overwrite = 231;
  static const int k_Font_fontData = 232;
  static const int k_FontData_height = 233;
  static const int k_FontData_name = 234;
  static const int k_FontData_style = 235;
  static const int k_FontDialog_RGB = 236;
  static const int k_FontDialog_effectsVisible = 237;
  static const int k_FontDialog_fontData = 238;
  static const int k_FontDialog_fontList = 239;
  static const int k_FormAttachment_alignment = 240;
  static const int k_FormAttachment_control = 241;
  static const int k_FormAttachment_denominator = 242;
  static const int k_FormAttachment_numerator = 243;
  static const int k_FormAttachment_offset = 244;
  static const int k_FormData_bottom = 245;
  static const int k_FormData_cacheBottom = 246;
  static const int k_FormData_cacheHeight = 247;
  static const int k_FormData_cacheLeft = 248;
  static const int k_FormData_cacheRight = 249;
  static const int k_FormData_cacheTop = 250;
  static const int k_FormData_cacheWidth = 251;
  static const int k_FormData_currentHeight = 252;
  static const int k_FormData_currentHhint = 253;
  static const int k_FormData_currentWhint = 254;
  static const int k_FormData_currentWidth = 255;
  static const int k_FormData_defaultHeight = 256;
  static const int k_FormData_defaultHhint = 257;
  static const int k_FormData_defaultWhint = 258;
  static const int k_FormData_defaultWidth = 259;
  static const int k_FormData_height = 260;
  static const int k_FormData_isVisited = 261;
  static const int k_FormData_left = 262;
  static const int k_FormData_needed = 263;
  static const int k_FormData_right = 264;
  static const int k_FormData_top = 265;
  static const int k_FormData_width = 266;
  static const int k_GC_XORMode = 267;
  static const int k_GC_alpha = 268;
  static const int k_GC_antialias = 269;
  static const int k_GC_background = 270;
  static const int k_GC_backgroundPattern = 271;
  static const int k_GC_bufferScale = 272;
  static const int k_GC_clipping = 273;
  static const int k_GC_clippingPath = 274;
  static const int k_GC_clippingRects = 275;
  static const int k_GC_clippingText = 276;
  static const int k_GC_fillRule = 277;
  static const int k_GC_font = 278;
  static const int k_GC_foreground = 279;
  static const int k_GC_foregroundPattern = 280;
  static const int k_GC_hoverFills = 281;
  static const int k_GC_interpolation = 282;
  static const int k_GC_lineCap = 283;
  static const int k_GC_lineDash = 284;
  static const int k_GC_lineDashOffset = 285;
  static const int k_GC_lineJoin = 286;
  static const int k_GC_lineStyle = 287;
  static const int k_GC_lineWidth = 288;
  static const int k_GC_selectionFills = 289;
  static const int k_GC_style = 290;
  static const int k_GC_themedCanvas = 291;
  static const int k_GC_transform = 292;
  static const int k_GLData_accumAlphaSize = 293;
  static const int k_GLData_accumBlueSize = 294;
  static const int k_GLData_accumGreenSize = 295;
  static const int k_GLData_accumRedSize = 296;
  static const int k_GLData_alphaSize = 297;
  static const int k_GLData_blueSize = 298;
  static const int k_GLData_depthSize = 299;
  static const int k_GLData_doubleBuffer = 300;
  static const int k_GLData_greenSize = 301;
  static const int k_GLData_redSize = 302;
  static const int k_GLData_sampleBuffers = 303;
  static const int k_GLData_samples = 304;
  static const int k_GLData_shareContext = 305;
  static const int k_GLData_stencilSize = 306;
  static const int k_GLData_stereo = 307;
  static const int k_GlyphMetrics_ascent = 308;
  static const int k_GlyphMetrics_descent = 309;
  static const int k_GlyphMetrics_width = 310;
  static const int k_GridData_cacheHeight = 311;
  static const int k_GridData_cacheWidth = 312;
  static const int k_GridData_currentHeight = 313;
  static const int k_GridData_currentHhint = 314;
  static const int k_GridData_currentWhint = 315;
  static const int k_GridData_currentWidth = 316;
  static const int k_GridData_defaultHeight = 317;
  static const int k_GridData_defaultHhint = 318;
  static const int k_GridData_defaultWhint = 319;
  static const int k_GridData_defaultWidth = 320;
  static const int k_GridData_exclude = 321;
  static const int k_GridData_grabExcessHorizontalSpace = 322;
  static const int k_GridData_grabExcessVerticalSpace = 323;
  static const int k_GridData_heightHint = 324;
  static const int k_GridData_horizontalAlignment = 325;
  static const int k_GridData_horizontalIndent = 326;
  static const int k_GridData_horizontalSpan = 327;
  static const int k_GridData_minimumHeight = 328;
  static const int k_GridData_minimumWidth = 329;
  static const int k_GridData_verticalAlignment = 330;
  static const int k_GridData_verticalIndent = 331;
  static const int k_GridData_verticalSpan = 332;
  static const int k_GridData_widthHint = 333;
  static const int k_Group_text = 334;
  static const int k_Image_filename = 335;
  static const int k_Image_height = 336;
  static const int k_Image_imageData = 337;
  static const int k_Image_remoteRef = 338;
  static const int k_Image_svgContent = 339;
  static const int k_Image_width = 340;
  static const int k_ImageData_data = 341;
  static const int k_ImageData_depth = 342;
  static const int k_ImageData_height = 343;
  static const int k_ImageData_width = 344;
  static const int k_Item_image = 345;
  static const int k_Item_text = 346;
  static const int k_Label_image = 347;
  static const int k_Label_text = 348;
  static const int k_LineInfo_alignment = 349;
  static const int k_LineInfo_background = 350;
  static const int k_LineInfo_flags = 351;
  static const int k_LineInfo_indent = 352;
  static const int k_LineInfo_justify = 353;
  static const int k_LineInfo_segments = 354;
  static const int k_LineInfo_segmentsChars = 355;
  static const int k_LineInfo_tabStops = 356;
  static const int k_LineInfo_verticalIndent = 357;
  static const int k_LineInfo_wrapIndent = 358;
  static const int k_LineSizeInfo_height = 359;
  static const int k_LineSizeInfo_width = 360;
  static const int k_Link_linkForeground = 361;
  static const int k_Link_text = 362;
  static const int k_List_items = 363;
  static const int k_List_selection = 364;
  static const int k_List_topIndex = 365;
  static const int k_Menu_enabled = 366;
  static const int k_Menu_items = 367;
  static const int k_Menu_location = 368;
  static const int k_Menu_orientation = 369;
  static const int k_Menu_visible = 370;
  static const int k_MenuItem_accelerator = 371;
  static const int k_MenuItem_enabled = 372;
  static const int k_MenuItem_menu = 373;
  static const int k_MenuItem_selection = 374;
  static const int k_MenuItem_toolTipText = 375;
  static const int k_MessageBox_buttonLabels = 376;
  static const int k_MessageBox_message = 377;
  static const int k_Monitor_clientHeight = 378;
  static const int k_Monitor_clientWidth = 379;
  static const int k_Monitor_clientX = 380;
  static const int k_Monitor_clientY = 381;
  static const int k_Monitor_handle = 382;
  static const int k_Monitor_height = 383;
  static const int k_Monitor_width = 384;
  static const int k_Monitor_x = 385;
  static const int k_Monitor_y = 386;
  static const int k_Monitor_zoom = 387;
  static const int k_PDFDocument_height = 388;
  static const int k_PDFDocument_width = 389;
  static const int k_Path_pathData = 390;
  static const int k_Path_textFonts = 391;
  static const int k_Path_textOrigins = 392;
  static const int k_Path_textStrings = 393;
  static const int k_PathData_points = 394;
  static const int k_PathData_types = 395;
  static const int k_Pattern_color1 = 396;
  static const int k_Pattern_color2 = 397;
  static const int k_Pattern_endX = 398;
  static const int k_Pattern_endY = 399;
  static const int k_Pattern_image = 400;
  static const int k_Pattern_startX = 401;
  static const int k_Pattern_startY = 402;
  static const int k_Point_x = 403;
  static const int k_Point_y = 404;
  static const int k_PopupList_font = 405;
  static const int k_PopupList_items = 406;
  static const int k_PopupList_minimumWidth = 407;
  static const int k_PopupList_selection = 408;
  static const int k_PrintDialog_endPage = 409;
  static const int k_PrintDialog_printToFile = 410;
  static const int k_PrintDialog_scope = 411;
  static const int k_PrintDialog_startPage = 412;
  static const int k_ProgressBar_maximum = 413;
  static const int k_ProgressBar_minimum = 414;
  static const int k_ProgressBar_selection = 415;
  static const int k_RGB_blue = 416;
  static const int k_RGB_green = 417;
  static const int k_RGB_red = 418;
  static const int k_Rectangle_height = 419;
  static const int k_Rectangle_width = 420;
  static const int k_Rectangle_x = 421;
  static const int k_Rectangle_y = 422;
  static const int k_Region_rects = 423;
  static const int k_RowData_exclude = 424;
  static const int k_RowData_height = 425;
  static const int k_RowData_width = 426;
  static const int k_SashForm_maximizedControl = 427;
  static const int k_SashForm_sashWidth = 428;
  static const int k_SashForm_weights = 429;
  static const int k_Scale_maximum = 430;
  static const int k_Scale_minimum = 431;
  static const int k_Scale_selection = 432;
  static const int k_ScrollBar_enabled = 433;
  static const int k_ScrollBar_increment = 434;
  static const int k_ScrollBar_maximum = 435;
  static const int k_ScrollBar_minimum = 436;
  static const int k_ScrollBar_selection = 437;
  static const int k_ScrollBar_thumb = 438;
  static const int k_ScrollBar_visible = 439;
  static const int k_Scrollable_horizontalBar = 440;
  static const int k_Scrollable_verticalBar = 441;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 442;
  static const int k_ScrolledComposite_content = 443;
  static const int k_ScrolledComposite_expandHorizontal = 444;
  static const int k_ScrolledComposite_expandVertical = 445;
  static const int k_ScrolledComposite_minHeight = 446;
  static const int k_ScrolledComposite_minWidth = 447;
  static const int k_ScrolledComposite_origin = 448;
  static const int k_ScrolledComposite_originRequest = 449;
  static const int k_Shell_alpha = 450;
  static const int k_Shell_dialogs = 451;
  static const int k_Shell_fullScreen = 452;
  static const int k_Shell_maximumSize = 453;
  static const int k_Shell_minimumSize = 454;
  static const int k_Shell_modified = 455;
  static const int k_Slider_increment = 456;
  static const int k_Slider_maximum = 457;
  static const int k_Slider_minimum = 458;
  static const int k_Slider_pageIncrement = 459;
  static const int k_Slider_selection = 460;
  static const int k_Slider_thumb = 461;
  static const int k_Spinner_digits = 462;
  static const int k_Spinner_increment = 463;
  static const int k_Spinner_keptText = 464;
  static const int k_Spinner_maximum = 465;
  static const int k_Spinner_minimum = 466;
  static const int k_Spinner_pageIncrement = 467;
  static const int k_Spinner_selection = 468;
  static const int k_Spinner_textLimit = 469;
  static const int k_StyleRange_fontStyle = 470;
  static const int k_StyleRange_length = 471;
  static const int k_StyleRange_start = 472;
  static const int k_StyledText_alignment = 473;
  static const int k_StyledText_alwaysShowScrollBars = 474;
  static const int k_StyledText_blockSelection = 475;
  static const int k_StyledText_blockSelectionBounds = 476;
  static const int k_StyledText_bottomMargin = 477;
  static const int k_StyledText_caretOffset = 478;
  static const int k_StyledText_caretOffsets = 479;
  static const int k_StyledText_doubleClickEnabled = 480;
  static const int k_StyledText_editable = 481;
  static const int k_StyledText_fixedLineHeight = 482;
  static const int k_StyledText_horizontalPixel = 483;
  static const int k_StyledText_indent = 484;
  static const int k_StyledText_justify = 485;
  static const int k_StyledText_leftMargin = 486;
  static const int k_StyledText_lineSpacing = 487;
  static const int k_StyledText_marginColor = 488;
  static const int k_StyledText_renderer = 489;
  static const int k_StyledText_rightMargin = 490;
  static const int k_StyledText_selection = 491;
  static const int k_StyledText_selectionBackground = 492;
  static const int k_StyledText_selectionForeground = 493;
  static const int k_StyledText_selectionRanges = 494;
  static const int k_StyledText_styleIndex = 495;
  static const int k_StyledText_styles = 496;
  static const int k_StyledText_tabStops = 497;
  static const int k_StyledText_tabs = 498;
  static const int k_StyledText_text = 499;
  static const int k_StyledText_topMargin = 500;
  static const int k_StyledText_topPixel = 501;
  static const int k_StyledText_wordWrap = 502;
  static const int k_StyledText_wrapIndent = 503;
  static const int k_StyledTextPrintOptions_footer = 504;
  static const int k_StyledTextPrintOptions_header = 505;
  static const int k_StyledTextPrintOptions_jobName = 506;
  static const int k_StyledTextPrintOptions_lineLabels = 507;
  static const int k_StyledTextPrintOptions_printLineBackground = 508;
  static const int k_StyledTextPrintOptions_printLineNumbers = 509;
  static const int k_StyledTextPrintOptions_printTextBackground = 510;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 511;
  static const int k_StyledTextPrintOptions_printTextForeground = 512;
  static const int k_StyledTextRenderer_ascent = 513;
  static const int k_StyledTextRenderer_averageCharWidth = 514;
  static const int k_StyledTextRenderer_averageLineHeight = 515;
  static const int k_StyledTextRenderer_boldFont = 516;
  static const int k_StyledTextRenderer_boldItalicFont = 517;
  static const int k_StyledTextRenderer_bulletTexts = 518;
  static const int k_StyledTextRenderer_bulletsIndices = 519;
  static const int k_StyledTextRenderer_descent = 520;
  static const int k_StyledTextRenderer_fixedPitch = 521;
  static const int k_StyledTextRenderer_hasLinks = 522;
  static const int k_StyledTextRenderer_idleRunning = 523;
  static const int k_StyledTextRenderer_italicFont = 524;
  static const int k_StyledTextRenderer_lineCount = 525;
  static const int k_StyledTextRenderer_lineSizes = 526;
  static const int k_StyledTextRenderer_lineSpacingComputing = 527;
  static const int k_StyledTextRenderer_lineSpacings = 528;
  static const int k_StyledTextRenderer_lines = 529;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 530;
  static const int k_StyledTextRenderer_maxWidth = 531;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 532;
  static const int k_StyledTextRenderer_ranges = 533;
  static const int k_StyledTextRenderer_redrawLines = 534;
  static const int k_StyledTextRenderer_regularFont = 535;
  static const int k_StyledTextRenderer_styleCount = 536;
  static const int k_StyledTextRenderer_styledText = 537;
  static const int k_StyledTextRenderer_styles = 538;
  static const int k_StyledTextRenderer_stylesSet = 539;
  static const int k_StyledTextRenderer_stylesSetCount = 540;
  static const int k_StyledTextRenderer_tabLength = 541;
  static const int k_StyledTextRenderer_tabWidth = 542;
  static const int k_StyledTextRenderer_topIndex = 543;
  static const int k_TabFolder_items = 544;
  static const int k_TabFolder_selection = 545;
  static const int k_TabItem_control = 546;
  static const int k_TabItem_toolTipText = 547;
  static const int k_Table_columnOrder = 548;
  static const int k_Table_columns = 549;
  static const int k_Table_editors = 550;
  static const int k_Table_headerBackground = 551;
  static const int k_Table_headerForeground = 552;
  static const int k_Table_headerVisible = 553;
  static const int k_Table_itemCount = 554;
  static const int k_Table_items = 555;
  static const int k_Table_linesVisible = 556;
  static const int k_Table_ownerDrawItemHeight = 557;
  static const int k_Table_selection = 558;
  static const int k_Table_topIndex = 559;
  static const int k_TableColumn_alignment = 560;
  static const int k_TableColumn_moveable = 561;
  static const int k_TableColumn_resizable = 562;
  static const int k_TableColumn_width = 563;
  static const int k_TableEditor_column = 564;
  static const int k_TableEditor_item = 565;
  static const int k_TableItem_background = 566;
  static const int k_TableItem_backgrounds = 567;
  static const int k_TableItem_checked = 568;
  static const int k_TableItem_font = 569;
  static const int k_TableItem_fonts = 570;
  static const int k_TableItem_foreground = 571;
  static const int k_TableItem_foregrounds = 572;
  static const int k_TableItem_grayed = 573;
  static const int k_TableItem_images = 574;
  static const int k_TableItem_paintedTexts = 575;
  static const int k_TableItem_texts = 576;
  static const int k_Text_caretPosition = 577;
  static const int k_Text_echoCharacter = 578;
  static const int k_Text_editTexts = 579;
  static const int k_Text_editable = 580;
  static const int k_Text_edits = 581;
  static const int k_Text_message = 582;
  static const int k_Text_selection = 583;
  static const int k_Text_text = 584;
  static const int k_Text_textLimit = 585;
  static const int k_TextStyle_background = 586;
  static const int k_TextStyle_borderColor = 587;
  static const int k_TextStyle_borderStyle = 588;
  static const int k_TextStyle_font = 589;
  static const int k_TextStyle_foreground = 590;
  static const int k_TextStyle_metrics = 591;
  static const int k_TextStyle_rise = 592;
  static const int k_TextStyle_strikeout = 593;
  static const int k_TextStyle_strikeoutColor = 594;
  static const int k_TextStyle_underline = 595;
  static const int k_TextStyle_underlineColor = 596;
  static const int k_TextStyle_underlineStyle = 597;
  static const int k_ToolBar_items = 598;
  static const int k_ToolItem_background = 599;
  static const int k_ToolItem_control = 600;
  static const int k_ToolItem_disabledImage = 601;
  static const int k_ToolItem_enabled = 602;
  static const int k_ToolItem_foreground = 603;
  static const int k_ToolItem_hotImage = 604;
  static const int k_ToolItem_selection = 605;
  static const int k_ToolItem_toolTipText = 606;
  static const int k_ToolItem_width = 607;
  static const int k_ToolTip_location = 608;
  static const int k_ToolTip_message = 609;
  static const int k_ToolTip_text = 610;
  static const int k_ToolTip_visible = 611;
  static const int k_Touch_id = 612;
  static const int k_Touch_primary = 613;
  static const int k_Touch_source = 614;
  static const int k_Touch_state = 615;
  static const int k_Touch_x = 616;
  static const int k_Touch_y = 617;
  static const int k_TouchSource_bounds = 618;
  static const int k_TouchSource_direct = 619;
  static const int k_TouchSource_handle = 620;
  static const int k_Transform_elements = 621;
  static const int k_Tree_columnOrder = 622;
  static const int k_Tree_columns = 623;
  static const int k_Tree_editors = 624;
  static const int k_Tree_headerBackground = 625;
  static const int k_Tree_headerForeground = 626;
  static const int k_Tree_headerVisible = 627;
  static const int k_Tree_items = 628;
  static const int k_Tree_linesVisible = 629;
  static const int k_Tree_ownerDrawItemHeight = 630;
  static const int k_Tree_selection = 631;
  static const int k_Tree_sortColumn = 632;
  static const int k_Tree_sortDirection = 633;
  static const int k_Tree_topItem = 634;
  static const int k_TreeColumn_alignment = 635;
  static const int k_TreeColumn_moveable = 636;
  static const int k_TreeColumn_resizable = 637;
  static const int k_TreeColumn_width = 638;
  static const int k_TreeEditor_column = 639;
  static const int k_TreeEditor_editor = 640;
  static const int k_TreeEditor_item = 641;
  static const int k_TreeItem_background = 642;
  static const int k_TreeItem_backgrounds = 643;
  static const int k_TreeItem_checked = 644;
  static const int k_TreeItem_expanded = 645;
  static const int k_TreeItem_font = 646;
  static const int k_TreeItem_fonts = 647;
  static const int k_TreeItem_foreground = 648;
  static const int k_TreeItem_foregrounds = 649;
  static const int k_TreeItem_grayed = 650;
  static const int k_TreeItem_images = 651;
  static const int k_TreeItem_itemCount = 652;
  static const int k_TreeItem_items = 653;
  static const int k_TreeItem_paintedTexts = 654;
  static const int k_TreeItem_texts = 655;
  static const int k_Widget_style = 656;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  1,
  40,
  57,
  61,
  68,
  86,
  142,
  147,
  150,
  152,
  195,
  198,
  213,
  219,
  221,
  241,
  367,
  373,
  427,
  440,
  441,
  443,
  544,
  545,
  546,
  549,
  550,
  555,
  565,
  598,
  600,
  623,
  624,
  628,
  631,
  632,
  634,
  640,
  641,
  653,
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
  23: 'Drag/dragFinished',
  24: 'Drag/dragSetData',
  25: 'Drag/dragStart',
  26: 'DragDetect/DragDetect',
  27: 'DragDetect/dragStartResult',
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
  124: 'b',
  125: 'back',
  126: 'bench/d2j/probe',
  127: 'bench/d2j/probeResponse',
  128: 'bench/d2j/result',
  129: 'bench/d2j/run',
  130: 'bench/j2d/echo',
  131: 'bench/j2d/echoResponse',
  132: 'browserFunctionCall',
  133: 'c',
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
  200: 'reload',
  201: 'renderSnapshot',
  202: 'setCookie',
  203: 'shown',
  204: 'stop',
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
  220: 'swt.evolve.property.set',
  221: 'swt.evolve.test.runUi',
  222: 'swt.evolve.test.runUiResponse',
  223: 'swt.evolve.timing.bare',
  224: 'swt.evolve.timing.ping',
  225: 'swt.evolve.timing.pong',
  226: 'swt.evolve.url.open',
  227: 'swt.evolve.widget.refresh',
  228: 'swt.evolve.wire',
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
  static const int benchD2jProbe = 126; // bench/d2j/probe
  static const int benchD2jProbeResponse = 127; // bench/d2j/probeResponse
  static const int benchD2jResult = 128; // bench/d2j/result
  static const int benchD2jRun = 129; // bench/d2j/run
  static const int benchJ2dEcho = 130; // bench/j2d/echo
  static const int benchJ2dEchoResponse = 131; // bench/j2d/echoResponse
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
  static const int swtEvolvePropertySet = 220; // swt.evolve.property.set
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
  45: 'bottomMargin',
  46: 'bounds',
  47: 'browser',
  48: 'bufferScale',
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
  351: 'scale_vertical_minimum_at_top',
  352: 'scope',
  353: 'script',
  354: 'seconds',
  355: 'segments',
  356: 'segmentsChars',
  357: 'selectedImageVisible',
  358: 'selection',
  359: 'selectionBackground',
  360: 'selectionBarThickness',
  361: 'selectionBgImage',
  362: 'selectionFills',
  363: 'selectionForeground',
  364: 'selectionRanges',
  365: 'seq',
  366: 'shareContext',
  367: 'shells',
  368: 'showChevron',
  369: 'showClose',
  370: 'showListPopupSeq',
  371: 'show_scaling_control',
  372: 'show_theme_color_palette',
  373: 'showing',
  374: 'single',
  375: 'sortColumn',
  376: 'sortDirection',
  377: 'source',
  378: 'spacing',
  379: 'start',
  380: 'startPage',
  381: 'startX',
  382: 'startY',
  383: 'state',
  384: 'stateMask',
  385: 'stencilSize',
  386: 'stereo',
  387: 'strikeout',
  388: 'strikeoutColor',
  389: 'style',
  390: 'styleCount',
  391: 'styleIndex',
  392: 'styledText',
  393: 'styles',
  394: 'stylesSet',
  395: 'stylesSetCount',
  396: 'svgContent',
  397: 'swt',
  398: 'systemMenu',
  399: 'system_menu_bar',
  400: 'tabHeight',
  401: 'tabLength',
  402: 'tabPosition',
  403: 'tabStop',
  404: 'tabStops',
  405: 'tabWidth',
  406: 'tabs',
  407: 'text',
  408: 'textFonts',
  409: 'textLimit',
  410: 'textOrigins',
  411: 'textStrings',
  412: 'texts',
  413: 'theme_color',
  414: 'theme_colors_by_widget',
  415: 'theme_name',
  416: 'themedCanvas',
  417: 'thumb',
  418: 'time',
  419: 'toolTipText',
  420: 'tooltips',
  421: 'top',
  422: 'topIndex',
  423: 'topItem',
  424: 'topMargin',
  425: 'topPixel',
  426: 'topRight',
  427: 'topRightAlignment',
  428: 'touches',
  429: 'tracking',
  430: 'transform',
  431: 'transparent_menu_check_box',
  432: 'type',
  433: 'types',
  434: 'ui_zoom',
  435: 'underline',
  436: 'underlineColor',
  437: 'underlineStyle',
  438: 'unselectedCloseVisible',
  439: 'unselectedImageVisible',
  440: 'url',
  441: 'use_special_dropdown_button',
  442: 'use_swt_border',
  443: 'use_swt_colors',
  444: 'use_swt_font_colors',
  445: 'use_swt_font_colors_by_widget',
  446: 'use_swt_fonts',
  447: 'value',
  448: 'verdict',
  449: 'verticalAlignment',
  450: 'verticalBar',
  451: 'verticalIndent',
  452: 'verticalSpan',
  453: 'visible',
  454: 'wHint',
  455: 'warnings',
  456: 'weights',
  457: 'widget',
  458: 'width',
  459: 'widthHint',
  460: 'windowId',
  461: 'windowedShellIds',
  462: 'wordWrap',
  463: 'wrapIndent',
  464: 'wrapIndices',
  465: 'x',
  466: 'xDirection',
  467: 'y',
  468: 'yDirection',
  469: 'year',
  470: 'zoom',
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
