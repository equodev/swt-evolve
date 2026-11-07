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
const int wireFingerprint = 1346628195;

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
  static const int k_CTabFolder_stripLayoutSeq = 55;
  static const int k_CTabFolder_tabHeight = 56;
  static const int k_CTabFolder_tabPosition = 57;
  static const int k_CTabFolder_topRight = 58;
  static const int k_CTabFolder_topRightAlignment = 59;
  static const int k_CTabFolder_unselectedCloseVisible = 60;
  static const int k_CTabFolder_unselectedImageVisible = 61;
  static const int k_CTabItem_control = 62;
  static const int k_CTabItem_font = 63;
  static const int k_CTabItem_foreground = 64;
  static const int k_CTabItem_selectionForeground = 65;
  static const int k_CTabItem_showClose = 66;
  static const int k_CTabItem_showing = 67;
  static const int k_CTabItem_toolTipText = 68;
  static const int k_Canvas_caret = 69;
  static const int k_Caret_blinkRate = 70;
  static const int k_Caret_bounds = 71;
  static const int k_Caret_focused = 72;
  static const int k_Caret_image = 73;
  static const int k_Caret_visible = 74;
  static const int k_Clipboard_availableTypeNames = 75;
  static const int k_Color_a = 76;
  static const int k_Color_b = 77;
  static const int k_Color_g = 78;
  static const int k_Color_r = 79;
  static const int k_ColorDialog_RGB = 80;
  static const int k_ColorDialog_RGBs = 81;
  static const int k_Combo_caretPosition = 82;
  static const int k_Combo_items = 83;
  static const int k_Combo_listVisible = 84;
  static const int k_Combo_text = 85;
  static const int k_Composite_backgroundMode = 86;
  static const int k_Composite_children = 87;
  static const int k_ConfigFlags_assets_path = 88;
  static const int k_ConfigFlags_bigger_icon_size = 89;
  static const int k_ConfigFlags_csd_maximize = 90;
  static const int k_ConfigFlags_csd_os = 91;
  static const int k_ConfigFlags_csd_placement = 92;
  static const int k_ConfigFlags_csd_titlebar_color = 93;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 94;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 95;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 96;
  static const int k_ConfigFlags_custom_tooltip = 97;
  static const int k_ConfigFlags_decimal_separator = 98;
  static const int k_ConfigFlags_decorations_align = 99;
  static const int k_ConfigFlags_disable_control_gc_overlay = 100;
  static const int k_ConfigFlags_disable_evolve_icons = 101;
  static const int k_ConfigFlags_disable_hover_zoom = 102;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 103;
  static const int k_ConfigFlags_double_click_timeout_ms = 104;
  static const int k_ConfigFlags_focus_indicators = 105;
  static const int k_ConfigFlags_font_point_scale = 106;
  static const int k_ConfigFlags_force_theme = 107;
  static const int k_ConfigFlags_gc_icons_replacement = 108;
  static const int k_ConfigFlags_hover_delay_ms = 109;
  static const int k_ConfigFlags_image_disable_icons_replacement = 110;
  static const int k_ConfigFlags_locale = 111;
  static const int k_ConfigFlags_notification_popup = 112;
  static const int k_ConfigFlags_preserve_icon_colors = 113;
  static const int k_ConfigFlags_print_move = 114;
  static const int k_ConfigFlags_push_button_margin = 115;
  static const int k_ConfigFlags_scale_vertical_minimum_at_top = 116;
  static const int k_ConfigFlags_show_scaling_control = 117;
  static const int k_ConfigFlags_show_theme_color_palette = 118;
  static const int k_ConfigFlags_system_menu_bar = 119;
  static const int k_ConfigFlags_theme_color = 120;
  static const int k_ConfigFlags_theme_colors_by_widget = 121;
  static const int k_ConfigFlags_theme_name = 122;
  static const int k_ConfigFlags_transparent_menu_check_box = 123;
  static const int k_ConfigFlags_ui_zoom = 124;
  static const int k_ConfigFlags_use_special_dropdown_button = 125;
  static const int k_ConfigFlags_use_swt_border = 126;
  static const int k_ConfigFlags_use_swt_colors = 127;
  static const int k_ConfigFlags_use_swt_font_colors = 128;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 129;
  static const int k_ConfigFlags_use_swt_fonts = 130;
  static const int k_Control_background = 131;
  static const int k_Control_backgroundImage = 132;
  static const int k_Control_bounds = 133;
  static const int k_Control_cursor = 134;
  static const int k_Control_dragDetect = 135;
  static const int k_Control_dragSource = 136;
  static const int k_Control_dragSourceId = 137;
  static const int k_Control_dropTargetId = 138;
  static const int k_Control_enabled = 139;
  static const int k_Control_font = 140;
  static const int k_Control_foreground = 141;
  static const int k_Control_hasOwnBackground = 142;
  static const int k_Control_inheritsBackground = 143;
  static const int k_Control_menu = 144;
  static const int k_Control_region = 145;
  static const int k_Control_tabStop = 146;
  static const int k_Control_toolTipText = 147;
  static const int k_Control_visible = 148;
  static const int k_ControlEditor_editor = 149;
  static const int k_CoolBar_itemOrder = 150;
  static const int k_CoolBar_itemSizes = 151;
  static const int k_CoolBar_items = 152;
  static const int k_CoolBar_wrapIndices = 153;
  static const int k_CoolItem_control = 154;
  static const int k_CoolItem_preferredSize = 155;
  static const int k_Cursor_cursorStyle = 156;
  static const int k_Cursor_hotspotX = 157;
  static const int k_Cursor_hotspotY = 158;
  static const int k_Cursor_image = 159;
  static const int k_DateTime_day = 160;
  static const int k_DateTime_hours = 161;
  static const int k_DateTime_minutes = 162;
  static const int k_DateTime_month = 163;
  static const int k_DateTime_seconds = 164;
  static const int k_DateTime_year = 165;
  static const int k_Decorations_menuBar = 166;
  static const int k_Decorations_menuStripHeight = 167;
  static const int k_Decorations_ownMenuStrip = 168;
  static const int k_Decorations_text = 169;
  static const int k_Device_fontList = 170;
  static const int k_Device_tracking = 171;
  static const int k_Device_warnings = 172;
  static const int k_Dialog_id = 173;
  static const int k_Dialog_style = 174;
  static const int k_Dialog_swt = 175;
  static const int k_Dialog_text = 176;
  static const int k_DirectoryDialog_filterPath = 177;
  static const int k_DirectoryDialog_message = 178;
  static const int k_Display_activeShellId = 179;
  static const int k_Display_config = 180;
  static const int k_Display_focusHolderId = 181;
  static const int k_Display_mainShellId = 182;
  static const int k_Display_popups = 183;
  static const int k_Display_shells = 184;
  static const int k_Display_systemMenu = 185;
  static const int k_Display_tooltips = 186;
  static const int k_Display_windowedShellIds = 187;
  static const int k_Event_button = 188;
  static const int k_Event_character = 189;
  static const int k_Event_count = 190;
  static const int k_Event_currentDataTypeId = 191;
  static const int k_Event_detail = 192;
  static const int k_Event_doit = 193;
  static const int k_Event_dragImage = 194;
  static const int k_Event_end = 195;
  static const int k_Event_feedback = 196;
  static const int k_Event_gc = 197;
  static const int k_Event_height = 198;
  static const int k_Event_index = 199;
  static const int k_Event_item = 200;
  static const int k_Event_itemId = 201;
  static const int k_Event_keyCode = 202;
  static const int k_Event_keyLocation = 203;
  static const int k_Event_magnification = 204;
  static const int k_Event_operations = 205;
  static const int k_Event_rotation = 206;
  static const int k_Event_segments = 207;
  static const int k_Event_segmentsChars = 208;
  static const int k_Event_start = 209;
  static const int k_Event_stateMask = 210;
  static const int k_Event_text = 211;
  static const int k_Event_time = 212;
  static const int k_Event_touches = 213;
  static const int k_Event_type = 214;
  static const int k_Event_widget = 215;
  static const int k_Event_width = 216;
  static const int k_Event_x = 217;
  static const int k_Event_xDirection = 218;
  static const int k_Event_y = 219;
  static const int k_Event_yDirection = 220;
  static const int k_ExpandBar_items = 221;
  static const int k_ExpandBar_spacing = 222;
  static const int k_ExpandItem_control = 223;
  static const int k_ExpandItem_expanded = 224;
  static const int k_ExpandItem_headerHeight = 225;
  static const int k_ExpandItem_height = 226;
  static const int k_FileDialog_fileName = 227;
  static const int k_FileDialog_fileNames = 228;
  static const int k_FileDialog_filterExtensions = 229;
  static const int k_FileDialog_filterIndex = 230;
  static const int k_FileDialog_filterNames = 231;
  static const int k_FileDialog_filterPath = 232;
  static const int k_FileDialog_overwrite = 233;
  static const int k_Font_fontData = 234;
  static const int k_FontData_height = 235;
  static const int k_FontData_name = 236;
  static const int k_FontData_style = 237;
  static const int k_FontDialog_RGB = 238;
  static const int k_FontDialog_effectsVisible = 239;
  static const int k_FontDialog_fontData = 240;
  static const int k_FontDialog_fontList = 241;
  static const int k_FormAttachment_alignment = 242;
  static const int k_FormAttachment_control = 243;
  static const int k_FormAttachment_denominator = 244;
  static const int k_FormAttachment_numerator = 245;
  static const int k_FormAttachment_offset = 246;
  static const int k_FormData_bottom = 247;
  static const int k_FormData_cacheBottom = 248;
  static const int k_FormData_cacheHeight = 249;
  static const int k_FormData_cacheLeft = 250;
  static const int k_FormData_cacheRight = 251;
  static const int k_FormData_cacheTop = 252;
  static const int k_FormData_cacheWidth = 253;
  static const int k_FormData_currentHeight = 254;
  static const int k_FormData_currentHhint = 255;
  static const int k_FormData_currentWhint = 256;
  static const int k_FormData_currentWidth = 257;
  static const int k_FormData_defaultHeight = 258;
  static const int k_FormData_defaultHhint = 259;
  static const int k_FormData_defaultWhint = 260;
  static const int k_FormData_defaultWidth = 261;
  static const int k_FormData_height = 262;
  static const int k_FormData_isVisited = 263;
  static const int k_FormData_left = 264;
  static const int k_FormData_needed = 265;
  static const int k_FormData_right = 266;
  static const int k_FormData_top = 267;
  static const int k_FormData_width = 268;
  static const int k_GC_XORMode = 269;
  static const int k_GC_alpha = 270;
  static const int k_GC_antialias = 271;
  static const int k_GC_background = 272;
  static const int k_GC_backgroundPattern = 273;
  static const int k_GC_bufferScale = 274;
  static const int k_GC_bufferScaleY = 275;
  static const int k_GC_clipping = 276;
  static const int k_GC_clippingPath = 277;
  static const int k_GC_clippingRects = 278;
  static const int k_GC_clippingText = 279;
  static const int k_GC_fillRule = 280;
  static const int k_GC_font = 281;
  static const int k_GC_foreground = 282;
  static const int k_GC_foregroundPattern = 283;
  static const int k_GC_hoverFills = 284;
  static const int k_GC_interpolation = 285;
  static const int k_GC_lineCap = 286;
  static const int k_GC_lineDash = 287;
  static const int k_GC_lineDashOffset = 288;
  static const int k_GC_lineJoin = 289;
  static const int k_GC_lineStyle = 290;
  static const int k_GC_lineWidth = 291;
  static const int k_GC_selectionFills = 292;
  static const int k_GC_style = 293;
  static const int k_GC_themedCanvas = 294;
  static const int k_GC_transform = 295;
  static const int k_GLData_accumAlphaSize = 296;
  static const int k_GLData_accumBlueSize = 297;
  static const int k_GLData_accumGreenSize = 298;
  static const int k_GLData_accumRedSize = 299;
  static const int k_GLData_alphaSize = 300;
  static const int k_GLData_blueSize = 301;
  static const int k_GLData_depthSize = 302;
  static const int k_GLData_doubleBuffer = 303;
  static const int k_GLData_greenSize = 304;
  static const int k_GLData_redSize = 305;
  static const int k_GLData_sampleBuffers = 306;
  static const int k_GLData_samples = 307;
  static const int k_GLData_shareContext = 308;
  static const int k_GLData_stencilSize = 309;
  static const int k_GLData_stereo = 310;
  static const int k_GlyphMetrics_ascent = 311;
  static const int k_GlyphMetrics_descent = 312;
  static const int k_GlyphMetrics_width = 313;
  static const int k_GridData_cacheHeight = 314;
  static const int k_GridData_cacheWidth = 315;
  static const int k_GridData_currentHeight = 316;
  static const int k_GridData_currentHhint = 317;
  static const int k_GridData_currentWhint = 318;
  static const int k_GridData_currentWidth = 319;
  static const int k_GridData_defaultHeight = 320;
  static const int k_GridData_defaultHhint = 321;
  static const int k_GridData_defaultWhint = 322;
  static const int k_GridData_defaultWidth = 323;
  static const int k_GridData_exclude = 324;
  static const int k_GridData_grabExcessHorizontalSpace = 325;
  static const int k_GridData_grabExcessVerticalSpace = 326;
  static const int k_GridData_heightHint = 327;
  static const int k_GridData_horizontalAlignment = 328;
  static const int k_GridData_horizontalIndent = 329;
  static const int k_GridData_horizontalSpan = 330;
  static const int k_GridData_minimumHeight = 331;
  static const int k_GridData_minimumWidth = 332;
  static const int k_GridData_verticalAlignment = 333;
  static const int k_GridData_verticalIndent = 334;
  static const int k_GridData_verticalSpan = 335;
  static const int k_GridData_widthHint = 336;
  static const int k_Group_text = 337;
  static const int k_Image_filename = 338;
  static const int k_Image_height = 339;
  static const int k_Image_imageData = 340;
  static const int k_Image_remoteRef = 341;
  static const int k_Image_svgContent = 342;
  static const int k_Image_width = 343;
  static const int k_ImageData_data = 344;
  static const int k_ImageData_depth = 345;
  static const int k_ImageData_height = 346;
  static const int k_ImageData_width = 347;
  static const int k_Item_image = 348;
  static const int k_Item_text = 349;
  static const int k_Label_image = 350;
  static const int k_Label_text = 351;
  static const int k_LineInfo_alignment = 352;
  static const int k_LineInfo_background = 353;
  static const int k_LineInfo_flags = 354;
  static const int k_LineInfo_indent = 355;
  static const int k_LineInfo_justify = 356;
  static const int k_LineInfo_segments = 357;
  static const int k_LineInfo_segmentsChars = 358;
  static const int k_LineInfo_tabStops = 359;
  static const int k_LineInfo_verticalIndent = 360;
  static const int k_LineInfo_wrapIndent = 361;
  static const int k_LineSizeInfo_height = 362;
  static const int k_LineSizeInfo_width = 363;
  static const int k_Link_linkForeground = 364;
  static const int k_Link_text = 365;
  static const int k_List_items = 366;
  static const int k_List_selection = 367;
  static const int k_List_topIndex = 368;
  static const int k_Menu_enabled = 369;
  static const int k_Menu_items = 370;
  static const int k_Menu_location = 371;
  static const int k_Menu_orientation = 372;
  static const int k_Menu_visible = 373;
  static const int k_MenuItem_accelerator = 374;
  static const int k_MenuItem_enabled = 375;
  static const int k_MenuItem_menu = 376;
  static const int k_MenuItem_selection = 377;
  static const int k_MenuItem_toolTipText = 378;
  static const int k_MessageBox_buttonLabels = 379;
  static const int k_MessageBox_message = 380;
  static const int k_Monitor_clientHeight = 381;
  static const int k_Monitor_clientWidth = 382;
  static const int k_Monitor_clientX = 383;
  static const int k_Monitor_clientY = 384;
  static const int k_Monitor_handle = 385;
  static const int k_Monitor_height = 386;
  static const int k_Monitor_width = 387;
  static const int k_Monitor_x = 388;
  static const int k_Monitor_y = 389;
  static const int k_Monitor_zoom = 390;
  static const int k_PDFDocument_height = 391;
  static const int k_PDFDocument_width = 392;
  static const int k_Path_pathData = 393;
  static const int k_Path_textFonts = 394;
  static const int k_Path_textOrigins = 395;
  static const int k_Path_textStrings = 396;
  static const int k_PathData_points = 397;
  static const int k_PathData_types = 398;
  static const int k_Pattern_color1 = 399;
  static const int k_Pattern_color2 = 400;
  static const int k_Pattern_endX = 401;
  static const int k_Pattern_endY = 402;
  static const int k_Pattern_image = 403;
  static const int k_Pattern_startX = 404;
  static const int k_Pattern_startY = 405;
  static const int k_Point_x = 406;
  static const int k_Point_y = 407;
  static const int k_PopupList_font = 408;
  static const int k_PopupList_items = 409;
  static const int k_PopupList_minimumWidth = 410;
  static const int k_PopupList_selection = 411;
  static const int k_PrintDialog_endPage = 412;
  static const int k_PrintDialog_printToFile = 413;
  static const int k_PrintDialog_scope = 414;
  static const int k_PrintDialog_startPage = 415;
  static const int k_ProgressBar_maximum = 416;
  static const int k_ProgressBar_minimum = 417;
  static const int k_ProgressBar_selection = 418;
  static const int k_RGB_blue = 419;
  static const int k_RGB_green = 420;
  static const int k_RGB_red = 421;
  static const int k_Rectangle_height = 422;
  static const int k_Rectangle_width = 423;
  static const int k_Rectangle_x = 424;
  static const int k_Rectangle_y = 425;
  static const int k_Region_rects = 426;
  static const int k_RowData_exclude = 427;
  static const int k_RowData_height = 428;
  static const int k_RowData_width = 429;
  static const int k_SashForm_maximizedControl = 430;
  static const int k_SashForm_sashWidth = 431;
  static const int k_SashForm_weights = 432;
  static const int k_Scale_maximum = 433;
  static const int k_Scale_minimum = 434;
  static const int k_Scale_selection = 435;
  static const int k_ScrollBar_enabled = 436;
  static const int k_ScrollBar_increment = 437;
  static const int k_ScrollBar_maximum = 438;
  static const int k_ScrollBar_minimum = 439;
  static const int k_ScrollBar_selection = 440;
  static const int k_ScrollBar_thumb = 441;
  static const int k_ScrollBar_visible = 442;
  static const int k_Scrollable_horizontalBar = 443;
  static const int k_Scrollable_verticalBar = 444;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 445;
  static const int k_ScrolledComposite_content = 446;
  static const int k_ScrolledComposite_expandHorizontal = 447;
  static const int k_ScrolledComposite_expandVertical = 448;
  static const int k_ScrolledComposite_minHeight = 449;
  static const int k_ScrolledComposite_minWidth = 450;
  static const int k_ScrolledComposite_origin = 451;
  static const int k_ScrolledComposite_originRequest = 452;
  static const int k_Shell_alpha = 453;
  static const int k_Shell_dialogs = 454;
  static const int k_Shell_fullScreen = 455;
  static const int k_Shell_maximumSize = 456;
  static const int k_Shell_minimumSize = 457;
  static const int k_Shell_modified = 458;
  static const int k_Slider_increment = 459;
  static const int k_Slider_maximum = 460;
  static const int k_Slider_minimum = 461;
  static const int k_Slider_pageIncrement = 462;
  static const int k_Slider_selection = 463;
  static const int k_Slider_thumb = 464;
  static const int k_Spinner_digits = 465;
  static const int k_Spinner_increment = 466;
  static const int k_Spinner_keptText = 467;
  static const int k_Spinner_maximum = 468;
  static const int k_Spinner_minimum = 469;
  static const int k_Spinner_pageIncrement = 470;
  static const int k_Spinner_selection = 471;
  static const int k_Spinner_textLimit = 472;
  static const int k_StyleRange_fontStyle = 473;
  static const int k_StyleRange_length = 474;
  static const int k_StyleRange_start = 475;
  static const int k_StyledText_alignment = 476;
  static const int k_StyledText_alwaysShowScrollBars = 477;
  static const int k_StyledText_blockSelection = 478;
  static const int k_StyledText_blockSelectionBounds = 479;
  static const int k_StyledText_bottomMargin = 480;
  static const int k_StyledText_caretOffset = 481;
  static const int k_StyledText_caretOffsets = 482;
  static const int k_StyledText_doubleClickEnabled = 483;
  static const int k_StyledText_editable = 484;
  static const int k_StyledText_fixedLineHeight = 485;
  static const int k_StyledText_horizontalPixel = 486;
  static const int k_StyledText_indent = 487;
  static const int k_StyledText_justify = 488;
  static const int k_StyledText_leftMargin = 489;
  static const int k_StyledText_lineSpacing = 490;
  static const int k_StyledText_marginColor = 491;
  static const int k_StyledText_renderer = 492;
  static const int k_StyledText_rightMargin = 493;
  static const int k_StyledText_selection = 494;
  static const int k_StyledText_selectionBackground = 495;
  static const int k_StyledText_selectionForeground = 496;
  static const int k_StyledText_selectionRanges = 497;
  static const int k_StyledText_styleIndex = 498;
  static const int k_StyledText_styles = 499;
  static const int k_StyledText_tabStops = 500;
  static const int k_StyledText_tabs = 501;
  static const int k_StyledText_text = 502;
  static const int k_StyledText_topMargin = 503;
  static const int k_StyledText_topPixel = 504;
  static const int k_StyledText_wordWrap = 505;
  static const int k_StyledText_wrapIndent = 506;
  static const int k_StyledTextPrintOptions_footer = 507;
  static const int k_StyledTextPrintOptions_header = 508;
  static const int k_StyledTextPrintOptions_jobName = 509;
  static const int k_StyledTextPrintOptions_lineLabels = 510;
  static const int k_StyledTextPrintOptions_printLineBackground = 511;
  static const int k_StyledTextPrintOptions_printLineNumbers = 512;
  static const int k_StyledTextPrintOptions_printTextBackground = 513;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 514;
  static const int k_StyledTextPrintOptions_printTextForeground = 515;
  static const int k_StyledTextRenderer_ascent = 516;
  static const int k_StyledTextRenderer_averageCharWidth = 517;
  static const int k_StyledTextRenderer_averageLineHeight = 518;
  static const int k_StyledTextRenderer_boldFont = 519;
  static const int k_StyledTextRenderer_boldItalicFont = 520;
  static const int k_StyledTextRenderer_bulletTexts = 521;
  static const int k_StyledTextRenderer_bulletsIndices = 522;
  static const int k_StyledTextRenderer_descent = 523;
  static const int k_StyledTextRenderer_fixedPitch = 524;
  static const int k_StyledTextRenderer_hasLinks = 525;
  static const int k_StyledTextRenderer_idleRunning = 526;
  static const int k_StyledTextRenderer_italicFont = 527;
  static const int k_StyledTextRenderer_lineCount = 528;
  static const int k_StyledTextRenderer_lineSizes = 529;
  static const int k_StyledTextRenderer_lineSpacingComputing = 530;
  static const int k_StyledTextRenderer_lineSpacings = 531;
  static const int k_StyledTextRenderer_lines = 532;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 533;
  static const int k_StyledTextRenderer_maxWidth = 534;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 535;
  static const int k_StyledTextRenderer_ranges = 536;
  static const int k_StyledTextRenderer_redrawLines = 537;
  static const int k_StyledTextRenderer_regularFont = 538;
  static const int k_StyledTextRenderer_styleCount = 539;
  static const int k_StyledTextRenderer_styledText = 540;
  static const int k_StyledTextRenderer_styles = 541;
  static const int k_StyledTextRenderer_stylesSet = 542;
  static const int k_StyledTextRenderer_stylesSetCount = 543;
  static const int k_StyledTextRenderer_tabLength = 544;
  static const int k_StyledTextRenderer_tabWidth = 545;
  static const int k_StyledTextRenderer_topIndex = 546;
  static const int k_TabFolder_items = 547;
  static const int k_TabFolder_selection = 548;
  static const int k_TabItem_control = 549;
  static const int k_TabItem_toolTipText = 550;
  static const int k_Table_columnOrder = 551;
  static const int k_Table_columns = 552;
  static const int k_Table_editors = 553;
  static const int k_Table_headerBackground = 554;
  static const int k_Table_headerForeground = 555;
  static const int k_Table_headerVisible = 556;
  static const int k_Table_itemCount = 557;
  static const int k_Table_items = 558;
  static const int k_Table_linesVisible = 559;
  static const int k_Table_ownerDrawItemHeight = 560;
  static const int k_Table_selection = 561;
  static const int k_Table_topIndex = 562;
  static const int k_TableColumn_alignment = 563;
  static const int k_TableColumn_moveable = 564;
  static const int k_TableColumn_resizable = 565;
  static const int k_TableColumn_width = 566;
  static const int k_TableEditor_column = 567;
  static const int k_TableEditor_item = 568;
  static const int k_TableItem_background = 569;
  static const int k_TableItem_backgrounds = 570;
  static const int k_TableItem_checked = 571;
  static const int k_TableItem_font = 572;
  static const int k_TableItem_fonts = 573;
  static const int k_TableItem_foreground = 574;
  static const int k_TableItem_foregrounds = 575;
  static const int k_TableItem_grayed = 576;
  static const int k_TableItem_images = 577;
  static const int k_TableItem_paintedTexts = 578;
  static const int k_TableItem_texts = 579;
  static const int k_Text_caretPosition = 580;
  static const int k_Text_echoCharacter = 581;
  static const int k_Text_editTexts = 582;
  static const int k_Text_editable = 583;
  static const int k_Text_edits = 584;
  static const int k_Text_message = 585;
  static const int k_Text_selection = 586;
  static const int k_Text_text = 587;
  static const int k_Text_textLimit = 588;
  static const int k_TextStyle_background = 589;
  static const int k_TextStyle_borderColor = 590;
  static const int k_TextStyle_borderStyle = 591;
  static const int k_TextStyle_font = 592;
  static const int k_TextStyle_foreground = 593;
  static const int k_TextStyle_metrics = 594;
  static const int k_TextStyle_rise = 595;
  static const int k_TextStyle_strikeout = 596;
  static const int k_TextStyle_strikeoutColor = 597;
  static const int k_TextStyle_underline = 598;
  static const int k_TextStyle_underlineColor = 599;
  static const int k_TextStyle_underlineStyle = 600;
  static const int k_ToolBar_items = 601;
  static const int k_ToolItem_background = 602;
  static const int k_ToolItem_control = 603;
  static const int k_ToolItem_disabledImage = 604;
  static const int k_ToolItem_enabled = 605;
  static const int k_ToolItem_foreground = 606;
  static const int k_ToolItem_hotImage = 607;
  static const int k_ToolItem_selection = 608;
  static const int k_ToolItem_toolTipText = 609;
  static const int k_ToolItem_width = 610;
  static const int k_ToolTip_location = 611;
  static const int k_ToolTip_message = 612;
  static const int k_ToolTip_text = 613;
  static const int k_ToolTip_visible = 614;
  static const int k_Touch_id = 615;
  static const int k_Touch_primary = 616;
  static const int k_Touch_source = 617;
  static const int k_Touch_state = 618;
  static const int k_Touch_x = 619;
  static const int k_Touch_y = 620;
  static const int k_TouchSource_bounds = 621;
  static const int k_TouchSource_direct = 622;
  static const int k_TouchSource_handle = 623;
  static const int k_Transform_elements = 624;
  static const int k_Tree_columnOrder = 625;
  static const int k_Tree_columns = 626;
  static const int k_Tree_editors = 627;
  static const int k_Tree_headerBackground = 628;
  static const int k_Tree_headerForeground = 629;
  static const int k_Tree_headerVisible = 630;
  static const int k_Tree_items = 631;
  static const int k_Tree_linesVisible = 632;
  static const int k_Tree_ownerDrawItemHeight = 633;
  static const int k_Tree_selection = 634;
  static const int k_Tree_sortColumn = 635;
  static const int k_Tree_sortDirection = 636;
  static const int k_Tree_topItem = 637;
  static const int k_TreeColumn_alignment = 638;
  static const int k_TreeColumn_moveable = 639;
  static const int k_TreeColumn_resizable = 640;
  static const int k_TreeColumn_width = 641;
  static const int k_TreeEditor_column = 642;
  static const int k_TreeEditor_editor = 643;
  static const int k_TreeEditor_item = 644;
  static const int k_TreeItem_background = 645;
  static const int k_TreeItem_backgrounds = 646;
  static const int k_TreeItem_checked = 647;
  static const int k_TreeItem_expanded = 648;
  static const int k_TreeItem_font = 649;
  static const int k_TreeItem_fonts = 650;
  static const int k_TreeItem_foreground = 651;
  static const int k_TreeItem_foregrounds = 652;
  static const int k_TreeItem_grayed = 653;
  static const int k_TreeItem_images = 654;
  static const int k_TreeItem_itemCount = 655;
  static const int k_TreeItem_items = 656;
  static const int k_TreeItem_paintedTexts = 657;
  static const int k_TreeItem_texts = 658;
  static const int k_Widget_style = 659;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  1,
  40,
  58,
  62,
  69,
  87,
  144,
  149,
  152,
  154,
  197,
  200,
  215,
  221,
  223,
  243,
  370,
  376,
  430,
  443,
  444,
  446,
  547,
  548,
  549,
  552,
  553,
  558,
  568,
  601,
  603,
  626,
  627,
  631,
  634,
  635,
  637,
  643,
  644,
  656,
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
  136: 'anim',
  137: 'b',
  138: 'back',
  139: 'bench/d2j/probe',
  140: 'bench/d2j/probeResponse',
  141: 'bench/d2j/result',
  142: 'bench/d2j/run',
  143: 'bench/j2d/echo',
  144: 'bench/j2d/echoResponse',
  145: 'browserFunctionCall',
  146: 'c',
  147: 'callback',
  148: 'close',
  149: 'cookie',
  150: 'copyAreaImageintint',
  151: 'copyAreaImageintintResponse',
  152: 'copyAreaintintintintintintboolean',
  153: 'drawArcintintintintintint',
  154: 'drawFocusintintintint',
  155: 'drawImageImageintint',
  156: 'drawImageImageintintintint',
  157: 'drawImageImageintintintintintintintint',
  158: 'drawLineintintintint',
  159: 'drawOvalintintintint',
  160: 'drawPathPath',
  161: 'drawPointintint',
  162: 'drawPolygonint',
  163: 'drawPolylineint',
  164: 'drawRectangleintintintint',
  165: 'drawRoundRectangleintintintintintint',
  166: 'drawTextStringintintint',
  167: 'evaluate',
  168: 'evaluate/result',
  169: 'evolve.test.bootTrace',
  170: 'evolve.test.bootTraceResponse',
  171: 'evolve.test.drag',
  172: 'evolve.test.dragDone',
  173: 'evolve.test.frameCost',
  174: 'evolve.test.frameCostResponse',
  175: 'evolve.test.frameStats',
  176: 'evolve.test.frameStatsResponse',
  177: 'evolve.test.frameSync',
  178: 'evolve.test.frameSynced',
  179: 'evolve.test.iframeLoaded',
  180: 'evolve.test.query',
  181: 'evolve.test.queryResponse',
  182: 'evolve.test.styledTextPerf',
  183: 'evolve.test.styledTextPerfResponse',
  184: 'evolve.test.wireStats',
  185: 'evolve.test.wireStatsResponse',
  186: 'execute',
  187: 'fillArcintintintintintint',
  188: 'fillGradientRectangleintintintintboolean',
  189: 'fillOvalintintintint',
  190: 'fillPathPath',
  191: 'fillPolygonint',
  192: 'fillRectangleintintintint',
  193: 'fillRoundRectangleintintintintintint',
  194: 'fontSizeRequest',
  195: 'fontSizeResponse',
  196: 'forward',
  197: 'gcDispose',
  198: 'getCookie',
  199: 'imageInit',
  200: 'imageResult',
  201: 'imageSizeRequest',
  202: 'imageSizeResponse',
  203: 'imageSnapshotResult',
  204: 'key/verdict',
  205: 'key/vetoable',
  206: 'locationchanging/result',
  207: 'menu/verdict',
  208: 'modify/verdict',
  209: 'modify/vetoable',
  210: 'navigate',
  211: 'navigateRequest',
  212: 'navigationState',
  213: 'nextFrame',
  214: 'openMenu',
  215: 'pixelsResult',
  216: 'rebuild',
  217: 'reload',
  218: 'renderSnapshot',
  219: 'setCookie',
  220: 'shown',
  221: 'stop',
  222: 'subtree',
  223: 'subtree/request',
  224: 'swing',
  225: 'swing.open',
  226: 'swingIsland',
  227: 'swingIsland.close',
  228: 'swingIsland.dispose',
  229: 'swingIsland.island',
  230: 'swingIsland.opened',
  231: 'swingIsland.resize',
  232: 'swingIsland.surface',
  233: 'swingIsland.title',
  234: 'swt.evolve.batch',
  235: 'swt.evolve.focus',
  236: 'swt.evolve.focus.seen',
  237: 'swt.evolve.focusHolder',
  238: 'swt.evolve.properties',
  239: 'swt.evolve.property.set',
  240: 'swt.evolve.test.runUi',
  241: 'swt.evolve.test.runUiResponse',
  242: 'swt.evolve.timing.bare',
  243: 'swt.evolve.timing.ping',
  244: 'swt.evolve.timing.pong',
  245: 'swt.evolve.url.open',
  246: 'swt.evolve.widget.refresh',
  247: 'swt.evolve.wire',
  248: 'transport',
  249: 'transport/request',
  250: 'traverse/verdict',
  251: 'traverse/vetoable',
  252: 'v',
  253: 'widgetSizeRequest',
  254: 'widgetSizeResponse',
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
  static const int benchD2jProbe = 139; // bench/d2j/probe
  static const int benchD2jProbeResponse = 140; // bench/d2j/probeResponse
  static const int benchD2jResult = 141; // bench/d2j/result
  static const int benchD2jRun = 142; // bench/d2j/run
  static const int benchJ2dEcho = 143; // bench/j2d/echo
  static const int benchJ2dEchoResponse = 144; // bench/j2d/echoResponse
  static const int browserFunctionCall = 145; // browserFunctionCall
  static const int c = 146; // c
  static const int callback = 147; // callback
  static const int close = 148; // close
  static const int cookie = 149; // cookie
  static const int copyAreaImageintint = 150; // copyAreaImageintint
  static const int copyAreaImageintintResponse =
      151; // copyAreaImageintintResponse
  static const int copyAreaintintintintintintboolean =
      152; // copyAreaintintintintintintboolean
  static const int drawArcintintintintintint = 153; // drawArcintintintintintint
  static const int drawFocusintintintint = 154; // drawFocusintintintint
  static const int drawImageImageintint = 155; // drawImageImageintint
  static const int drawImageImageintintintint =
      156; // drawImageImageintintintint
  static const int drawImageImageintintintintintintintint =
      157; // drawImageImageintintintintintintintint
  static const int drawLineintintintint = 158; // drawLineintintintint
  static const int drawOvalintintintint = 159; // drawOvalintintintint
  static const int drawPathPath = 160; // drawPathPath
  static const int drawPointintint = 161; // drawPointintint
  static const int drawPolygonint = 162; // drawPolygonint
  static const int drawPolylineint = 163; // drawPolylineint
  static const int drawRectangleintintintint = 164; // drawRectangleintintintint
  static const int drawRoundRectangleintintintintintint =
      165; // drawRoundRectangleintintintintintint
  static const int drawTextStringintintint = 166; // drawTextStringintintint
  static const int evaluate = 167; // evaluate
  static const int evaluateResult = 168; // evaluate/result
  static const int evolveTestBootTrace = 169; // evolve.test.bootTrace
  static const int evolveTestBootTraceResponse =
      170; // evolve.test.bootTraceResponse
  static const int evolveTestDrag = 171; // evolve.test.drag
  static const int evolveTestDragDone = 172; // evolve.test.dragDone
  static const int evolveTestFrameCost = 173; // evolve.test.frameCost
  static const int evolveTestFrameCostResponse =
      174; // evolve.test.frameCostResponse
  static const int evolveTestFrameStats = 175; // evolve.test.frameStats
  static const int evolveTestFrameStatsResponse =
      176; // evolve.test.frameStatsResponse
  static const int evolveTestFrameSync = 177; // evolve.test.frameSync
  static const int evolveTestFrameSynced = 178; // evolve.test.frameSynced
  static const int evolveTestIframeLoaded = 179; // evolve.test.iframeLoaded
  static const int evolveTestQuery = 180; // evolve.test.query
  static const int evolveTestQueryResponse = 181; // evolve.test.queryResponse
  static const int evolveTestStyledTextPerf = 182; // evolve.test.styledTextPerf
  static const int evolveTestStyledTextPerfResponse =
      183; // evolve.test.styledTextPerfResponse
  static const int evolveTestWireStats = 184; // evolve.test.wireStats
  static const int evolveTestWireStatsResponse =
      185; // evolve.test.wireStatsResponse
  static const int execute = 186; // execute
  static const int fillArcintintintintintint = 187; // fillArcintintintintintint
  static const int fillGradientRectangleintintintintboolean =
      188; // fillGradientRectangleintintintintboolean
  static const int fillOvalintintintint = 189; // fillOvalintintintint
  static const int fillPathPath = 190; // fillPathPath
  static const int fillPolygonint = 191; // fillPolygonint
  static const int fillRectangleintintintint = 192; // fillRectangleintintintint
  static const int fillRoundRectangleintintintintintint =
      193; // fillRoundRectangleintintintintintint
  static const int fontSizeRequest = 194; // fontSizeRequest
  static const int fontSizeResponse = 195; // fontSizeResponse
  static const int forward = 196; // forward
  static const int gcDispose = 197; // gcDispose
  static const int getCookie = 198; // getCookie
  static const int imageInit = 199; // imageInit
  static const int imageResult = 200; // imageResult
  static const int imageSizeRequest = 201; // imageSizeRequest
  static const int imageSizeResponse = 202; // imageSizeResponse
  static const int imageSnapshotResult = 203; // imageSnapshotResult
  static const int keyVerdict = 204; // key/verdict
  static const int keyVetoable = 205; // key/vetoable
  static const int locationchangingResult = 206; // locationchanging/result
  static const int menuVerdict = 207; // menu/verdict
  static const int modifyVerdict = 208; // modify/verdict
  static const int modifyVetoable = 209; // modify/vetoable
  static const int navigate = 210; // navigate
  static const int navigateRequest = 211; // navigateRequest
  static const int navigationState = 212; // navigationState
  static const int nextFrame = 213; // nextFrame
  static const int openMenu = 214; // openMenu
  static const int pixelsResult = 215; // pixelsResult
  static const int rebuild = 216; // rebuild
  static const int reload = 217; // reload
  static const int renderSnapshot = 218; // renderSnapshot
  static const int setCookie = 219; // setCookie
  static const int shown = 220; // shown
  static const int stop = 221; // stop
  static const int subtree = 222; // subtree
  static const int subtreeRequest = 223; // subtree/request
  static const int swing = 224; // swing
  static const int swingOpen = 225; // swing.open
  static const int swingIsland = 226; // swingIsland
  static const int swingIslandClose = 227; // swingIsland.close
  static const int swingIslandDispose = 228; // swingIsland.dispose
  static const int swingIslandIsland = 229; // swingIsland.island
  static const int swingIslandOpened = 230; // swingIsland.opened
  static const int swingIslandResize = 231; // swingIsland.resize
  static const int swingIslandSurface = 232; // swingIsland.surface
  static const int swingIslandTitle = 233; // swingIsland.title
  static const int swtEvolveBatch = 234; // swt.evolve.batch
  static const int swtEvolveFocus = 235; // swt.evolve.focus
  static const int swtEvolveFocusSeen = 236; // swt.evolve.focus.seen
  static const int swtEvolveFocusHolder = 237; // swt.evolve.focusHolder
  static const int swtEvolveProperties = 238; // swt.evolve.properties
  static const int swtEvolvePropertySet = 239; // swt.evolve.property.set
  static const int swtEvolveTestRunUi = 240; // swt.evolve.test.runUi
  static const int swtEvolveTestRunUiResponse =
      241; // swt.evolve.test.runUiResponse
  static const int swtEvolveTimingBare = 242; // swt.evolve.timing.bare
  static const int swtEvolveTimingPing = 243; // swt.evolve.timing.ping
  static const int swtEvolveTimingPong = 244; // swt.evolve.timing.pong
  static const int swtEvolveUrlOpen = 245; // swt.evolve.url.open
  static const int swtEvolveWidgetRefresh = 246; // swt.evolve.widget.refresh
  static const int swtEvolveWire = 247; // swt.evolve.wire
  static const int transport = 248; // transport
  static const int transportRequest = 249; // transport/request
  static const int traverseVerdict = 250; // traverse/verdict
  static const int traverseVetoable = 251; // traverse/vetoable
  static const int v = 252; // v
  static const int widgetSizeRequest = 253; // widgetSizeRequest
  static const int widgetSizeResponse = 254; // widgetSizeResponse
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
  50: 'bufferScaleY',
  51: 'bulletTexts',
  52: 'bulletsIndices',
  53: 'button',
  54: 'buttonLabels',
  55: 'cacheBottom',
  56: 'cacheHeight',
  57: 'cacheLeft',
  58: 'cacheRight',
  59: 'cacheTop',
  60: 'cacheWidth',
  61: 'caret',
  62: 'caretOffset',
  63: 'caretOffsets',
  64: 'caretPosition',
  65: 'charCount',
  66: 'character',
  67: 'checked',
  68: 'chevronVisible',
  69: 'children',
  70: 'clientHeight',
  71: 'clientWidth',
  72: 'clientX',
  73: 'clientY',
  74: 'clipping',
  75: 'clippingPath',
  76: 'clippingRects',
  77: 'clippingText',
  78: 'color1',
  79: 'color2',
  80: 'column',
  81: 'columnOrder',
  82: 'columns',
  83: 'config',
  84: 'content',
  85: 'control',
  86: 'cookie',
  87: 'count',
  88: 'csd_maximize',
  89: 'csd_os',
  90: 'csd_placement',
  91: 'csd_titlebar_color',
  92: 'ctabfolder_tab_scroll',
  93: 'ctabfolder_topright_auto_hide',
  94: 'ctabfolder_visible_controls',
  95: 'currentDataTypeId',
  96: 'currentHeight',
  97: 'currentHhint',
  98: 'currentWhint',
  99: 'currentWidth',
  100: 'cursor',
  101: 'cursorStyle',
  102: 'custom_tooltip',
  103: 'damage',
  104: 'data',
  105: 'day',
  106: 'decimal_separator',
  107: 'decorations_align',
  108: 'defaultHeight',
  109: 'defaultHhint',
  110: 'defaultWhint',
  111: 'defaultWidth',
  112: 'denominator',
  113: 'depth',
  114: 'depthSize',
  115: 'descent',
  116: 'detail',
  117: 'dialogs',
  118: 'digits',
  119: 'direct',
  120: 'disable_control_gc_overlay',
  121: 'disable_evolve_icons',
  122: 'disable_hover_zoom',
  123: 'disable_swt_canvas_colors',
  124: 'disabledImage',
  125: 'doit',
  126: 'doubleBuffer',
  127: 'doubleClickEnabled',
  128: 'double_click_timeout_ms',
  129: 'dragDetect',
  130: 'dragImage',
  131: 'dragSource',
  132: 'dragSourceId',
  133: 'dropTargetId',
  134: 'echoCharacter',
  135: 'editTexts',
  136: 'editable',
  137: 'editor',
  138: 'editors',
  139: 'edits',
  140: 'effectsVisible',
  141: 'elements',
  142: 'enabled',
  143: 'end',
  144: 'endPage',
  145: 'endX',
  146: 'endY',
  147: 'error',
  148: 'exclude',
  149: 'expandHorizontal',
  150: 'expandVertical',
  151: 'expanded',
  152: 'expires',
  153: 'feedback',
  154: 'fileName',
  155: 'fileNames',
  156: 'filename',
  157: 'fillRule',
  158: 'filterExtensions',
  159: 'filterIndex',
  160: 'filterNames',
  161: 'filterPath',
  162: 'fixedLineHeight',
  163: 'fixedPitch',
  164: 'flags',
  165: 'focus',
  166: 'focusHolderId',
  167: 'focus_indicators',
  168: 'focused',
  169: 'font',
  170: 'fontData',
  171: 'fontList',
  172: 'fontStyle',
  173: 'font_point_scale',
  174: 'fonts',
  175: 'footer',
  176: 'force_theme',
  177: 'foreground',
  178: 'foregroundPattern',
  179: 'foregrounds',
  180: 'fullRepaint',
  181: 'fullScreen',
  182: 'functionNames',
  183: 'g',
  184: 'gc',
  185: 'gc_icons_replacement',
  186: 'grabExcessHorizontalSpace',
  187: 'grabExcessVerticalSpace',
  188: 'gradientColors',
  189: 'gradientPercents',
  190: 'gradientVertical',
  191: 'grayed',
  192: 'green',
  193: 'greenSize',
  194: 'hHint',
  195: 'handle',
  196: 'hasLinks',
  197: 'hasOwnBackground',
  198: 'header',
  199: 'headerBackground',
  200: 'headerForeground',
  201: 'headerHeight',
  202: 'headerVisible',
  203: 'headers',
  204: 'height',
  205: 'heightHint',
  206: 'highlight',
  207: 'highlightEnabled',
  208: 'horizontalAlignment',
  209: 'horizontalBar',
  210: 'horizontalIndent',
  211: 'horizontalPixel',
  212: 'horizontalSpan',
  213: 'hotImage',
  214: 'hotspotX',
  215: 'hotspotY',
  216: 'hours',
  217: 'hoverFills',
  218: 'hover_delay_ms',
  219: 'id',
  220: 'idleRunning',
  221: 'image',
  222: 'imageData',
  223: 'image_disable_icons_replacement',
  224: 'images',
  225: 'increment',
  226: 'indent',
  227: 'index',
  228: 'inheritsBackground',
  229: 'interpolation',
  230: 'isVisited',
  231: 'italicFont',
  232: 'item',
  233: 'itemCount',
  234: 'itemId',
  235: 'itemOrder',
  236: 'itemSizes',
  237: 'itemTooltips',
  238: 'items',
  239: 'javascriptEnabled',
  240: 'jobName',
  241: 'justify',
  242: 'keptText',
  243: 'keyCode',
  244: 'keyLocation',
  245: 'left',
  246: 'leftMargin',
  247: 'length',
  248: 'lineCap',
  249: 'lineCount',
  250: 'lineDash',
  251: 'lineDashOffset',
  252: 'lineJoin',
  253: 'lineLabels',
  254: 'lineSizes',
  255: 'lineSpacing',
  256: 'lineSpacingComputing',
  257: 'lineSpacings',
  258: 'lineStyle',
  259: 'lineWidth',
  260: 'lines',
  261: 'linesInAverageLineHeight',
  262: 'linesVisible',
  263: 'linkForeground',
  264: 'listVisible',
  265: 'localFileBase',
  266: 'localFilePath',
  267: 'localFileText',
  268: 'locale',
  269: 'location',
  270: 'magnification',
  271: 'mainShellId',
  272: 'map',
  273: 'marginColor',
  274: 'maxWidth',
  275: 'maxWidthLineIndex',
  276: 'maximizeVisible',
  277: 'maximized',
  278: 'maximizedControl',
  279: 'maximum',
  280: 'maximumSize',
  281: 'menu',
  282: 'menuBar',
  283: 'menuStripHeight',
  284: 'message',
  285: 'metrics',
  286: 'minHeight',
  287: 'minWidth',
  288: 'minimizeVisible',
  289: 'minimized',
  290: 'minimum',
  291: 'minimumCharacters',
  292: 'minimumHeight',
  293: 'minimumSize',
  294: 'minimumWidth',
  295: 'minutes',
  296: 'modified',
  297: 'month',
  298: 'moveable',
  299: 'name',
  300: 'needed',
  301: 'notification_popup',
  302: 'numerator',
  303: 'offset',
  304: 'operations',
  305: 'orientation',
  306: 'origin',
  307: 'originRequest',
  308: 'overwrite',
  309: 'ownMenuStrip',
  310: 'ownerDrawItemHeight',
  311: 'pageIncrement',
  312: 'paintedTexts',
  313: 'pathData',
  314: 'pixels',
  315: 'points',
  316: 'popups',
  317: 'port',
  318: 'postData',
  319: 'preferredSize',
  320: 'preserve_icon_colors',
  321: 'primary',
  322: 'printLineBackground',
  323: 'printLineNumbers',
  324: 'printTextBackground',
  325: 'printTextFontStyle',
  326: 'printTextForeground',
  327: 'printToFile',
  328: 'print_move',
  329: 'push_button_margin',
  330: 'r',
  331: 'ranges',
  332: 'rects',
  333: 'red',
  334: 'redSize',
  335: 'redrawLines',
  336: 'ref',
  337: 'region',
  338: 'regularFont',
  339: 'remoteRef',
  340: 'renderer',
  341: 'replaced',
  342: 'reqId',
  343: 'resizable',
  344: 'result',
  345: 'retain',
  346: 'right',
  347: 'rightMargin',
  348: 'rise',
  349: 'rotation',
  350: 'sampleBuffers',
  351: 'samples',
  352: 'sashWidth',
  353: 'scale_vertical_minimum_at_top',
  354: 'scope',
  355: 'script',
  356: 'seconds',
  357: 'segments',
  358: 'segmentsChars',
  359: 'selectedImageVisible',
  360: 'selection',
  361: 'selectionBackground',
  362: 'selectionBarThickness',
  363: 'selectionBgImage',
  364: 'selectionFills',
  365: 'selectionForeground',
  366: 'selectionRanges',
  367: 'seq',
  368: 'shareContext',
  369: 'shells',
  370: 'showChevron',
  371: 'showClose',
  372: 'showListPopupSeq',
  373: 'show_scaling_control',
  374: 'show_theme_color_palette',
  375: 'showing',
  376: 'single',
  377: 'sortColumn',
  378: 'sortDirection',
  379: 'source',
  380: 'spacing',
  381: 'start',
  382: 'startPage',
  383: 'startX',
  384: 'startY',
  385: 'state',
  386: 'stateMask',
  387: 'stencilSize',
  388: 'stereo',
  389: 'strikeout',
  390: 'strikeoutColor',
  391: 'stripLayoutSeq',
  392: 'style',
  393: 'styleCount',
  394: 'styleIndex',
  395: 'styledText',
  396: 'styles',
  397: 'stylesSet',
  398: 'stylesSetCount',
  399: 'svgContent',
  400: 'swt',
  401: 'systemMenu',
  402: 'system_menu_bar',
  403: 'tabHeight',
  404: 'tabLength',
  405: 'tabPosition',
  406: 'tabStop',
  407: 'tabStops',
  408: 'tabWidth',
  409: 'tabs',
  410: 'text',
  411: 'textFonts',
  412: 'textLimit',
  413: 'textOrigins',
  414: 'textStrings',
  415: 'texts',
  416: 'theme_color',
  417: 'theme_colors_by_widget',
  418: 'theme_name',
  419: 'themedCanvas',
  420: 'thumb',
  421: 'time',
  422: 'toolTipText',
  423: 'tooltips',
  424: 'top',
  425: 'topIndex',
  426: 'topItem',
  427: 'topMargin',
  428: 'topPixel',
  429: 'topRight',
  430: 'topRightAlignment',
  431: 'touches',
  432: 'tracking',
  433: 'transform',
  434: 'transparent_menu_check_box',
  435: 'type',
  436: 'types',
  437: 'ui_zoom',
  438: 'underline',
  439: 'underlineColor',
  440: 'underlineStyle',
  441: 'unselectedCloseVisible',
  442: 'unselectedImageVisible',
  443: 'url',
  444: 'use_special_dropdown_button',
  445: 'use_swt_border',
  446: 'use_swt_colors',
  447: 'use_swt_font_colors',
  448: 'use_swt_font_colors_by_widget',
  449: 'use_swt_fonts',
  450: 'value',
  451: 'verdict',
  452: 'verticalAlignment',
  453: 'verticalBar',
  454: 'verticalIndent',
  455: 'verticalSpan',
  456: 'visible',
  457: 'wHint',
  458: 'warnings',
  459: 'weights',
  460: 'widget',
  461: 'width',
  462: 'widthHint',
  463: 'windowId',
  464: 'windowedShellIds',
  465: 'wordWrap',
  466: 'wrapIndent',
  467: 'wrapIndices',
  468: 'x',
  469: 'xDirection',
  470: 'y',
  471: 'yDirection',
  472: 'year',
  473: 'zoom',
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
