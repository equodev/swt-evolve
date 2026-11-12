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
const int wireFingerprint = -243880975;

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
  static const int k_CCombo_editable = 19;
  static const int k_CCombo_itemTooltips = 20;
  static const int k_CCombo_items = 21;
  static const int k_CCombo_listVisible = 22;
  static const int k_CCombo_selection = 23;
  static const int k_CCombo_text = 24;
  static const int k_CCombo_textLimit = 25;
  static const int k_CLabel_alignment = 26;
  static const int k_CLabel_backgroundImage = 27;
  static const int k_CLabel_bottomMargin = 28;
  static const int k_CLabel_gradientColors = 29;
  static const int k_CLabel_gradientPercents = 30;
  static const int k_CLabel_gradientVertical = 31;
  static const int k_CLabel_image = 32;
  static const int k_CLabel_leftMargin = 33;
  static const int k_CLabel_rightMargin = 34;
  static const int k_CLabel_text = 35;
  static const int k_CLabel_topMargin = 36;
  static const int k_CTabFolder_borderVisible = 37;
  static const int k_CTabFolder_chevronVisible = 38;
  static const int k_CTabFolder_dirtyIndicatorStyle = 39;
  static const int k_CTabFolder_highlight = 40;
  static const int k_CTabFolder_highlightEnabled = 41;
  static const int k_CTabFolder_items = 42;
  static const int k_CTabFolder_maximizeVisible = 43;
  static const int k_CTabFolder_maximized = 44;
  static const int k_CTabFolder_minimizeVisible = 45;
  static const int k_CTabFolder_minimized = 46;
  static const int k_CTabFolder_minimumCharacters = 47;
  static const int k_CTabFolder_selectedImageVisible = 48;
  static const int k_CTabFolder_selection = 49;
  static const int k_CTabFolder_selectionBackground = 50;
  static const int k_CTabFolder_selectionBarThickness = 51;
  static const int k_CTabFolder_selectionBgImage = 52;
  static const int k_CTabFolder_selectionForeground = 53;
  static const int k_CTabFolder_showChevron = 54;
  static const int k_CTabFolder_showListPopupSeq = 55;
  static const int k_CTabFolder_single = 56;
  static const int k_CTabFolder_stripLayoutSeq = 57;
  static const int k_CTabFolder_tabHeight = 58;
  static const int k_CTabFolder_tabPosition = 59;
  static const int k_CTabFolder_topRight = 60;
  static const int k_CTabFolder_topRightAlignment = 61;
  static const int k_CTabFolder_unselectedCloseVisible = 62;
  static const int k_CTabFolder_unselectedImageVisible = 63;
  static const int k_CTabItem_control = 64;
  static const int k_CTabItem_font = 65;
  static const int k_CTabItem_foreground = 66;
  static const int k_CTabItem_selectionForeground = 67;
  static const int k_CTabItem_showClose = 68;
  static const int k_CTabItem_showDirty = 69;
  static const int k_CTabItem_showing = 70;
  static const int k_CTabItem_toolTipText = 71;
  static const int k_Canvas_caret = 72;
  static const int k_Caret_blinkRate = 73;
  static const int k_Caret_bounds = 74;
  static const int k_Caret_focused = 75;
  static const int k_Caret_image = 76;
  static const int k_Caret_visible = 77;
  static const int k_Clipboard_availableTypeNames = 78;
  static const int k_Color_a = 79;
  static const int k_Color_b = 80;
  static const int k_Color_g = 81;
  static const int k_Color_r = 82;
  static const int k_ColorDialog_RGB = 83;
  static const int k_ColorDialog_RGBs = 84;
  static const int k_Combo_caretPosition = 85;
  static const int k_Combo_items = 86;
  static const int k_Combo_listVisible = 87;
  static const int k_Combo_text = 88;
  static const int k_Combo_textLimit = 89;
  static const int k_Composite_backgroundMode = 90;
  static const int k_Composite_children = 91;
  static const int k_ConfigFlags_assets_path = 92;
  static const int k_ConfigFlags_bigger_icon_size = 93;
  static const int k_ConfigFlags_csd_maximize = 94;
  static const int k_ConfigFlags_csd_os = 95;
  static const int k_ConfigFlags_csd_placement = 96;
  static const int k_ConfigFlags_csd_titlebar_color = 97;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 98;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 99;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 100;
  static const int k_ConfigFlags_custom_tooltip = 101;
  static const int k_ConfigFlags_decimal_separator = 102;
  static const int k_ConfigFlags_decorations_align = 103;
  static const int k_ConfigFlags_disable_control_gc_overlay = 104;
  static const int k_ConfigFlags_disable_evolve_icons = 105;
  static const int k_ConfigFlags_disable_hover_zoom = 106;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 107;
  static const int k_ConfigFlags_double_click_timeout_ms = 108;
  static const int k_ConfigFlags_focus_indicators = 109;
  static const int k_ConfigFlags_font_point_scale = 110;
  static const int k_ConfigFlags_force_theme = 111;
  static const int k_ConfigFlags_gc_icons_replacement = 112;
  static const int k_ConfigFlags_hover_delay_ms = 113;
  static const int k_ConfigFlags_image_disable_icons_replacement = 114;
  static const int k_ConfigFlags_locale = 115;
  static const int k_ConfigFlags_notification_popup = 116;
  static const int k_ConfigFlags_preserve_icon_colors = 117;
  static const int k_ConfigFlags_print_move = 118;
  static const int k_ConfigFlags_push_button_margin = 119;
  static const int k_ConfigFlags_scale_vertical_minimum_at_top = 120;
  static const int k_ConfigFlags_show_scaling_control = 121;
  static const int k_ConfigFlags_show_theme_color_palette = 122;
  static const int k_ConfigFlags_system_menu_bar = 123;
  static const int k_ConfigFlags_theme_color = 124;
  static const int k_ConfigFlags_theme_colors_by_widget = 125;
  static const int k_ConfigFlags_theme_name = 126;
  static const int k_ConfigFlags_transparent_menu_check_box = 127;
  static const int k_ConfigFlags_ui_zoom = 128;
  static const int k_ConfigFlags_use_special_dropdown_button = 129;
  static const int k_ConfigFlags_use_swt_border = 130;
  static const int k_ConfigFlags_use_swt_colors = 131;
  static const int k_ConfigFlags_use_swt_font_colors = 132;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 133;
  static const int k_ConfigFlags_use_swt_fonts = 134;
  static const int k_Control_background = 135;
  static const int k_Control_backgroundImage = 136;
  static const int k_Control_bounds = 137;
  static const int k_Control_cursor = 138;
  static const int k_Control_dragDetect = 139;
  static const int k_Control_dragSource = 140;
  static const int k_Control_dragSourceId = 141;
  static const int k_Control_dropTargetId = 142;
  static const int k_Control_enabled = 143;
  static const int k_Control_font = 144;
  static const int k_Control_foreground = 145;
  static const int k_Control_hasOwnBackground = 146;
  static const int k_Control_inheritsBackground = 147;
  static const int k_Control_menu = 148;
  static const int k_Control_region = 149;
  static const int k_Control_tabStop = 150;
  static const int k_Control_toolTipText = 151;
  static const int k_Control_visible = 152;
  static const int k_ControlEditor_editor = 153;
  static const int k_CoolBar_itemOrder = 154;
  static const int k_CoolBar_itemSizes = 155;
  static const int k_CoolBar_items = 156;
  static const int k_CoolBar_wrapIndices = 157;
  static const int k_CoolItem_control = 158;
  static const int k_CoolItem_preferredSize = 159;
  static const int k_Cursor_cursorStyle = 160;
  static const int k_Cursor_hotspotX = 161;
  static const int k_Cursor_hotspotY = 162;
  static const int k_Cursor_image = 163;
  static const int k_DateTime_day = 164;
  static const int k_DateTime_hours = 165;
  static const int k_DateTime_minutes = 166;
  static const int k_DateTime_month = 167;
  static const int k_DateTime_seconds = 168;
  static const int k_DateTime_year = 169;
  static const int k_Decorations_menuBar = 170;
  static const int k_Decorations_menuStripHeight = 171;
  static const int k_Decorations_ownMenuStrip = 172;
  static const int k_Decorations_text = 173;
  static const int k_Device_fontList = 174;
  static const int k_Device_tracking = 175;
  static const int k_Device_warnings = 176;
  static const int k_Dialog_id = 177;
  static const int k_Dialog_style = 178;
  static const int k_Dialog_swt = 179;
  static const int k_Dialog_text = 180;
  static const int k_DirectoryDialog_filterPath = 181;
  static const int k_DirectoryDialog_message = 182;
  static const int k_Display_activeShellId = 183;
  static const int k_Display_config = 184;
  static const int k_Display_focusHolderId = 185;
  static const int k_Display_mainShellId = 186;
  static const int k_Display_popups = 187;
  static const int k_Display_shells = 188;
  static const int k_Display_systemMenu = 189;
  static const int k_Display_tooltips = 190;
  static const int k_Display_windowedShellIds = 191;
  static const int k_Event_button = 192;
  static const int k_Event_character = 193;
  static const int k_Event_count = 194;
  static const int k_Event_currentDataTypeId = 195;
  static const int k_Event_detail = 196;
  static const int k_Event_doit = 197;
  static const int k_Event_dragImage = 198;
  static const int k_Event_end = 199;
  static const int k_Event_feedback = 200;
  static const int k_Event_gc = 201;
  static const int k_Event_height = 202;
  static const int k_Event_index = 203;
  static const int k_Event_item = 204;
  static const int k_Event_itemId = 205;
  static const int k_Event_keyCode = 206;
  static const int k_Event_keyLocation = 207;
  static const int k_Event_magnification = 208;
  static const int k_Event_operations = 209;
  static const int k_Event_rotation = 210;
  static const int k_Event_segments = 211;
  static const int k_Event_segmentsChars = 212;
  static const int k_Event_start = 213;
  static const int k_Event_stateMask = 214;
  static const int k_Event_text = 215;
  static const int k_Event_time = 216;
  static const int k_Event_touches = 217;
  static const int k_Event_type = 218;
  static const int k_Event_widget = 219;
  static const int k_Event_width = 220;
  static const int k_Event_x = 221;
  static const int k_Event_xDirection = 222;
  static const int k_Event_y = 223;
  static const int k_Event_yDirection = 224;
  static const int k_ExpandBar_items = 225;
  static const int k_ExpandBar_spacing = 226;
  static const int k_ExpandItem_control = 227;
  static const int k_ExpandItem_expanded = 228;
  static const int k_ExpandItem_headerHeight = 229;
  static const int k_ExpandItem_height = 230;
  static const int k_FileDialog_fileName = 231;
  static const int k_FileDialog_fileNames = 232;
  static const int k_FileDialog_filterExtensions = 233;
  static const int k_FileDialog_filterIndex = 234;
  static const int k_FileDialog_filterNames = 235;
  static const int k_FileDialog_filterPath = 236;
  static const int k_FileDialog_overwrite = 237;
  static const int k_Font_fontData = 238;
  static const int k_FontData_height = 239;
  static const int k_FontData_name = 240;
  static const int k_FontData_style = 241;
  static const int k_FontDialog_RGB = 242;
  static const int k_FontDialog_effectsVisible = 243;
  static const int k_FontDialog_fontData = 244;
  static const int k_FontDialog_fontList = 245;
  static const int k_FormAttachment_alignment = 246;
  static const int k_FormAttachment_control = 247;
  static const int k_FormAttachment_denominator = 248;
  static const int k_FormAttachment_numerator = 249;
  static const int k_FormAttachment_offset = 250;
  static const int k_FormData_bottom = 251;
  static const int k_FormData_cacheBottom = 252;
  static const int k_FormData_cacheHeight = 253;
  static const int k_FormData_cacheLeft = 254;
  static const int k_FormData_cacheRight = 255;
  static const int k_FormData_cacheTop = 256;
  static const int k_FormData_cacheWidth = 257;
  static const int k_FormData_currentHeight = 258;
  static const int k_FormData_currentHhint = 259;
  static const int k_FormData_currentWhint = 260;
  static const int k_FormData_currentWidth = 261;
  static const int k_FormData_defaultHeight = 262;
  static const int k_FormData_defaultHhint = 263;
  static const int k_FormData_defaultWhint = 264;
  static const int k_FormData_defaultWidth = 265;
  static const int k_FormData_height = 266;
  static const int k_FormData_isVisited = 267;
  static const int k_FormData_left = 268;
  static const int k_FormData_needed = 269;
  static const int k_FormData_right = 270;
  static const int k_FormData_top = 271;
  static const int k_FormData_width = 272;
  static const int k_GC_XORMode = 273;
  static const int k_GC_alpha = 274;
  static const int k_GC_antialias = 275;
  static const int k_GC_background = 276;
  static const int k_GC_backgroundPattern = 277;
  static const int k_GC_bufferScale = 278;
  static const int k_GC_bufferScaleY = 279;
  static const int k_GC_clipping = 280;
  static const int k_GC_clippingPath = 281;
  static const int k_GC_clippingRects = 282;
  static const int k_GC_clippingText = 283;
  static const int k_GC_fillRule = 284;
  static const int k_GC_font = 285;
  static const int k_GC_foreground = 286;
  static const int k_GC_foregroundPattern = 287;
  static const int k_GC_hoverFills = 288;
  static const int k_GC_interpolation = 289;
  static const int k_GC_lineCap = 290;
  static const int k_GC_lineDash = 291;
  static const int k_GC_lineDashOffset = 292;
  static const int k_GC_lineJoin = 293;
  static const int k_GC_lineStyle = 294;
  static const int k_GC_lineWidth = 295;
  static const int k_GC_selectionFills = 296;
  static const int k_GC_style = 297;
  static const int k_GC_themedCanvas = 298;
  static const int k_GC_transform = 299;
  static const int k_GLData_accumAlphaSize = 300;
  static const int k_GLData_accumBlueSize = 301;
  static const int k_GLData_accumGreenSize = 302;
  static const int k_GLData_accumRedSize = 303;
  static const int k_GLData_alphaSize = 304;
  static const int k_GLData_blueSize = 305;
  static const int k_GLData_depthSize = 306;
  static const int k_GLData_doubleBuffer = 307;
  static const int k_GLData_greenSize = 308;
  static const int k_GLData_redSize = 309;
  static const int k_GLData_sampleBuffers = 310;
  static const int k_GLData_samples = 311;
  static const int k_GLData_shareContext = 312;
  static const int k_GLData_stencilSize = 313;
  static const int k_GLData_stereo = 314;
  static const int k_GlyphMetrics_ascent = 315;
  static const int k_GlyphMetrics_descent = 316;
  static const int k_GlyphMetrics_width = 317;
  static const int k_GridData_cacheHeight = 318;
  static const int k_GridData_cacheWidth = 319;
  static const int k_GridData_currentHeight = 320;
  static const int k_GridData_currentHhint = 321;
  static const int k_GridData_currentWhint = 322;
  static const int k_GridData_currentWidth = 323;
  static const int k_GridData_defaultHeight = 324;
  static const int k_GridData_defaultHhint = 325;
  static const int k_GridData_defaultWhint = 326;
  static const int k_GridData_defaultWidth = 327;
  static const int k_GridData_exclude = 328;
  static const int k_GridData_grabExcessHorizontalSpace = 329;
  static const int k_GridData_grabExcessVerticalSpace = 330;
  static const int k_GridData_heightHint = 331;
  static const int k_GridData_horizontalAlignment = 332;
  static const int k_GridData_horizontalIndent = 333;
  static const int k_GridData_horizontalSpan = 334;
  static const int k_GridData_minimumHeight = 335;
  static const int k_GridData_minimumWidth = 336;
  static const int k_GridData_verticalAlignment = 337;
  static const int k_GridData_verticalIndent = 338;
  static const int k_GridData_verticalSpan = 339;
  static const int k_GridData_widthHint = 340;
  static const int k_Group_text = 341;
  static const int k_Image_filename = 342;
  static const int k_Image_height = 343;
  static const int k_Image_imageData = 344;
  static const int k_Image_remoteRef = 345;
  static const int k_Image_svgContent = 346;
  static const int k_Image_width = 347;
  static const int k_ImageData_data = 348;
  static const int k_ImageData_depth = 349;
  static const int k_ImageData_height = 350;
  static const int k_ImageData_width = 351;
  static const int k_Item_image = 352;
  static const int k_Item_text = 353;
  static const int k_Label_image = 354;
  static const int k_Label_text = 355;
  static const int k_LineInfo_alignment = 356;
  static const int k_LineInfo_background = 357;
  static const int k_LineInfo_flags = 358;
  static const int k_LineInfo_indent = 359;
  static const int k_LineInfo_justify = 360;
  static const int k_LineInfo_segments = 361;
  static const int k_LineInfo_segmentsChars = 362;
  static const int k_LineInfo_tabStops = 363;
  static const int k_LineInfo_verticalIndent = 364;
  static const int k_LineInfo_wrapIndent = 365;
  static const int k_LineSizeInfo_height = 366;
  static const int k_LineSizeInfo_width = 367;
  static const int k_Link_linkForeground = 368;
  static const int k_Link_text = 369;
  static const int k_List_items = 370;
  static const int k_List_selection = 371;
  static const int k_List_topIndex = 372;
  static const int k_Menu_enabled = 373;
  static const int k_Menu_items = 374;
  static const int k_Menu_location = 375;
  static const int k_Menu_orientation = 376;
  static const int k_Menu_visible = 377;
  static const int k_MenuItem_accelerator = 378;
  static const int k_MenuItem_enabled = 379;
  static const int k_MenuItem_menu = 380;
  static const int k_MenuItem_selection = 381;
  static const int k_MenuItem_toolTipText = 382;
  static const int k_MessageBox_buttonLabels = 383;
  static const int k_MessageBox_message = 384;
  static const int k_Monitor_clientHeight = 385;
  static const int k_Monitor_clientWidth = 386;
  static const int k_Monitor_clientX = 387;
  static const int k_Monitor_clientY = 388;
  static const int k_Monitor_handle = 389;
  static const int k_Monitor_height = 390;
  static const int k_Monitor_width = 391;
  static const int k_Monitor_x = 392;
  static const int k_Monitor_y = 393;
  static const int k_Monitor_zoom = 394;
  static const int k_PDFDocument_height = 395;
  static const int k_PDFDocument_width = 396;
  static const int k_Path_pathData = 397;
  static const int k_Path_textFonts = 398;
  static const int k_Path_textOrigins = 399;
  static const int k_Path_textStrings = 400;
  static const int k_PathData_points = 401;
  static const int k_PathData_types = 402;
  static const int k_Pattern_color1 = 403;
  static const int k_Pattern_color2 = 404;
  static const int k_Pattern_endX = 405;
  static const int k_Pattern_endY = 406;
  static const int k_Pattern_image = 407;
  static const int k_Pattern_startX = 408;
  static const int k_Pattern_startY = 409;
  static const int k_Point_x = 410;
  static const int k_Point_y = 411;
  static const int k_PopupList_font = 412;
  static const int k_PopupList_items = 413;
  static const int k_PopupList_minimumWidth = 414;
  static const int k_PopupList_selection = 415;
  static const int k_PrintDialog_endPage = 416;
  static const int k_PrintDialog_printToFile = 417;
  static const int k_PrintDialog_scope = 418;
  static const int k_PrintDialog_startPage = 419;
  static const int k_ProgressBar_maximum = 420;
  static const int k_ProgressBar_minimum = 421;
  static const int k_ProgressBar_selection = 422;
  static const int k_RGB_blue = 423;
  static const int k_RGB_green = 424;
  static const int k_RGB_red = 425;
  static const int k_Rectangle_height = 426;
  static const int k_Rectangle_width = 427;
  static const int k_Rectangle_x = 428;
  static const int k_Rectangle_y = 429;
  static const int k_Region_rects = 430;
  static const int k_RowData_exclude = 431;
  static const int k_RowData_height = 432;
  static const int k_RowData_width = 433;
  static const int k_SashForm_maximizedControl = 434;
  static const int k_SashForm_sashWidth = 435;
  static const int k_SashForm_weights = 436;
  static const int k_Scale_maximum = 437;
  static const int k_Scale_minimum = 438;
  static const int k_Scale_selection = 439;
  static const int k_ScrollBar_enabled = 440;
  static const int k_ScrollBar_increment = 441;
  static const int k_ScrollBar_maximum = 442;
  static const int k_ScrollBar_minimum = 443;
  static const int k_ScrollBar_pageIncrement = 444;
  static const int k_ScrollBar_selection = 445;
  static const int k_ScrollBar_thumb = 446;
  static const int k_ScrollBar_visible = 447;
  static const int k_Scrollable_horizontalBar = 448;
  static const int k_Scrollable_verticalBar = 449;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 450;
  static const int k_ScrolledComposite_content = 451;
  static const int k_ScrolledComposite_expandHorizontal = 452;
  static const int k_ScrolledComposite_expandVertical = 453;
  static const int k_ScrolledComposite_minHeight = 454;
  static const int k_ScrolledComposite_minWidth = 455;
  static const int k_ScrolledComposite_origin = 456;
  static const int k_ScrolledComposite_originRequest = 457;
  static const int k_Shell_alpha = 458;
  static const int k_Shell_dialogs = 459;
  static const int k_Shell_fullScreen = 460;
  static const int k_Shell_maximumSize = 461;
  static const int k_Shell_minimumSize = 462;
  static const int k_Shell_modified = 463;
  static const int k_Slider_increment = 464;
  static const int k_Slider_maximum = 465;
  static const int k_Slider_minimum = 466;
  static const int k_Slider_pageIncrement = 467;
  static const int k_Slider_selection = 468;
  static const int k_Slider_thumb = 469;
  static const int k_Spinner_digits = 470;
  static const int k_Spinner_increment = 471;
  static const int k_Spinner_keptText = 472;
  static const int k_Spinner_maximum = 473;
  static const int k_Spinner_minimum = 474;
  static const int k_Spinner_pageIncrement = 475;
  static const int k_Spinner_selection = 476;
  static const int k_Spinner_textLimit = 477;
  static const int k_StyleRange_fontStyle = 478;
  static const int k_StyleRange_length = 479;
  static const int k_StyleRange_start = 480;
  static const int k_StyledText_alignment = 481;
  static const int k_StyledText_alwaysShowScrollBars = 482;
  static const int k_StyledText_blockSelection = 483;
  static const int k_StyledText_blockSelectionBounds = 484;
  static const int k_StyledText_bottomMargin = 485;
  static const int k_StyledText_caretOffset = 486;
  static const int k_StyledText_caretOffsets = 487;
  static const int k_StyledText_doubleClickEnabled = 488;
  static const int k_StyledText_editable = 489;
  static const int k_StyledText_fixedLineHeight = 490;
  static const int k_StyledText_horizontalPixel = 491;
  static const int k_StyledText_indent = 492;
  static const int k_StyledText_justify = 493;
  static const int k_StyledText_leftMargin = 494;
  static const int k_StyledText_lineSpacing = 495;
  static const int k_StyledText_marginColor = 496;
  static const int k_StyledText_renderer = 497;
  static const int k_StyledText_rightMargin = 498;
  static const int k_StyledText_selection = 499;
  static const int k_StyledText_selectionBackground = 500;
  static const int k_StyledText_selectionForeground = 501;
  static const int k_StyledText_selectionRanges = 502;
  static const int k_StyledText_styleIndex = 503;
  static const int k_StyledText_styles = 504;
  static const int k_StyledText_tabStops = 505;
  static const int k_StyledText_tabs = 506;
  static const int k_StyledText_text = 507;
  static const int k_StyledText_topMargin = 508;
  static const int k_StyledText_topPixel = 509;
  static const int k_StyledText_wordWrap = 510;
  static const int k_StyledText_wrapIndent = 511;
  static const int k_StyledTextPrintOptions_footer = 512;
  static const int k_StyledTextPrintOptions_header = 513;
  static const int k_StyledTextPrintOptions_jobName = 514;
  static const int k_StyledTextPrintOptions_lineLabels = 515;
  static const int k_StyledTextPrintOptions_printLineBackground = 516;
  static const int k_StyledTextPrintOptions_printLineNumbers = 517;
  static const int k_StyledTextPrintOptions_printTextBackground = 518;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 519;
  static const int k_StyledTextPrintOptions_printTextForeground = 520;
  static const int k_StyledTextRenderer_ascent = 521;
  static const int k_StyledTextRenderer_averageCharWidth = 522;
  static const int k_StyledTextRenderer_averageLineHeight = 523;
  static const int k_StyledTextRenderer_boldFont = 524;
  static const int k_StyledTextRenderer_boldItalicFont = 525;
  static const int k_StyledTextRenderer_bulletTexts = 526;
  static const int k_StyledTextRenderer_bulletsIndices = 527;
  static const int k_StyledTextRenderer_descent = 528;
  static const int k_StyledTextRenderer_fixedPitch = 529;
  static const int k_StyledTextRenderer_hasLinks = 530;
  static const int k_StyledTextRenderer_idleRunning = 531;
  static const int k_StyledTextRenderer_italicFont = 532;
  static const int k_StyledTextRenderer_lineCount = 533;
  static const int k_StyledTextRenderer_lineSizes = 534;
  static const int k_StyledTextRenderer_lineSpacingComputing = 535;
  static const int k_StyledTextRenderer_lineSpacings = 536;
  static const int k_StyledTextRenderer_lines = 537;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 538;
  static const int k_StyledTextRenderer_maxWidth = 539;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 540;
  static const int k_StyledTextRenderer_ranges = 541;
  static const int k_StyledTextRenderer_redrawLines = 542;
  static const int k_StyledTextRenderer_regularFont = 543;
  static const int k_StyledTextRenderer_styleCount = 544;
  static const int k_StyledTextRenderer_styledText = 545;
  static const int k_StyledTextRenderer_styles = 546;
  static const int k_StyledTextRenderer_stylesSet = 547;
  static const int k_StyledTextRenderer_stylesSetCount = 548;
  static const int k_StyledTextRenderer_tabLength = 549;
  static const int k_StyledTextRenderer_tabWidth = 550;
  static const int k_StyledTextRenderer_topIndex = 551;
  static const int k_TabFolder_items = 552;
  static const int k_TabFolder_selection = 553;
  static const int k_TabItem_control = 554;
  static const int k_TabItem_toolTipText = 555;
  static const int k_Table_columnOrder = 556;
  static const int k_Table_columns = 557;
  static const int k_Table_editors = 558;
  static const int k_Table_headerBackground = 559;
  static const int k_Table_headerForeground = 560;
  static const int k_Table_headerVisible = 561;
  static const int k_Table_itemCount = 562;
  static const int k_Table_items = 563;
  static const int k_Table_linesVisible = 564;
  static const int k_Table_ownerDrawItemHeight = 565;
  static const int k_Table_selection = 566;
  static const int k_Table_sortColumn = 567;
  static const int k_Table_sortDirection = 568;
  static const int k_Table_topIndex = 569;
  static const int k_TableColumn_alignment = 570;
  static const int k_TableColumn_moveable = 571;
  static const int k_TableColumn_resizable = 572;
  static const int k_TableColumn_toolTipText = 573;
  static const int k_TableColumn_width = 574;
  static const int k_TableEditor_column = 575;
  static const int k_TableEditor_item = 576;
  static const int k_TableItem_background = 577;
  static const int k_TableItem_backgrounds = 578;
  static const int k_TableItem_checked = 579;
  static const int k_TableItem_font = 580;
  static const int k_TableItem_fonts = 581;
  static const int k_TableItem_foreground = 582;
  static const int k_TableItem_foregrounds = 583;
  static const int k_TableItem_grayed = 584;
  static const int k_TableItem_images = 585;
  static const int k_TableItem_paintedImages = 586;
  static const int k_TableItem_paintedTexts = 587;
  static const int k_TableItem_texts = 588;
  static const int k_Text_caretPosition = 589;
  static const int k_Text_echoCharacter = 590;
  static const int k_Text_editTexts = 591;
  static const int k_Text_editable = 592;
  static const int k_Text_edits = 593;
  static const int k_Text_message = 594;
  static const int k_Text_selection = 595;
  static const int k_Text_text = 596;
  static const int k_Text_textLimit = 597;
  static const int k_TextStyle_background = 598;
  static const int k_TextStyle_borderColor = 599;
  static const int k_TextStyle_borderStyle = 600;
  static const int k_TextStyle_font = 601;
  static const int k_TextStyle_foreground = 602;
  static const int k_TextStyle_metrics = 603;
  static const int k_TextStyle_rise = 604;
  static const int k_TextStyle_strikeout = 605;
  static const int k_TextStyle_strikeoutColor = 606;
  static const int k_TextStyle_underline = 607;
  static const int k_TextStyle_underlineColor = 608;
  static const int k_TextStyle_underlineStyle = 609;
  static const int k_ToolBar_items = 610;
  static const int k_ToolItem_background = 611;
  static const int k_ToolItem_control = 612;
  static const int k_ToolItem_disabledImage = 613;
  static const int k_ToolItem_enabled = 614;
  static const int k_ToolItem_foreground = 615;
  static const int k_ToolItem_hotImage = 616;
  static const int k_ToolItem_selection = 617;
  static const int k_ToolItem_toolTipText = 618;
  static const int k_ToolItem_width = 619;
  static const int k_ToolTip_location = 620;
  static const int k_ToolTip_message = 621;
  static const int k_ToolTip_text = 622;
  static const int k_ToolTip_visible = 623;
  static const int k_Touch_id = 624;
  static const int k_Touch_primary = 625;
  static const int k_Touch_source = 626;
  static const int k_Touch_state = 627;
  static const int k_Touch_x = 628;
  static const int k_Touch_y = 629;
  static const int k_TouchSource_bounds = 630;
  static const int k_TouchSource_direct = 631;
  static const int k_TouchSource_handle = 632;
  static const int k_Transform_elements = 633;
  static const int k_Tree_columnOrder = 634;
  static const int k_Tree_columns = 635;
  static const int k_Tree_editors = 636;
  static const int k_Tree_headerBackground = 637;
  static const int k_Tree_headerForeground = 638;
  static const int k_Tree_headerVisible = 639;
  static const int k_Tree_items = 640;
  static const int k_Tree_linesVisible = 641;
  static const int k_Tree_ownerDrawItemHeight = 642;
  static const int k_Tree_selection = 643;
  static const int k_Tree_sortColumn = 644;
  static const int k_Tree_sortDirection = 645;
  static const int k_Tree_topItem = 646;
  static const int k_TreeColumn_alignment = 647;
  static const int k_TreeColumn_moveable = 648;
  static const int k_TreeColumn_resizable = 649;
  static const int k_TreeColumn_toolTipText = 650;
  static const int k_TreeColumn_width = 651;
  static const int k_TreeEditor_column = 652;
  static const int k_TreeEditor_editor = 653;
  static const int k_TreeEditor_item = 654;
  static const int k_TreeItem_background = 655;
  static const int k_TreeItem_backgrounds = 656;
  static const int k_TreeItem_checked = 657;
  static const int k_TreeItem_expanded = 658;
  static const int k_TreeItem_font = 659;
  static const int k_TreeItem_fonts = 660;
  static const int k_TreeItem_foreground = 661;
  static const int k_TreeItem_foregrounds = 662;
  static const int k_TreeItem_grayed = 663;
  static const int k_TreeItem_images = 664;
  static const int k_TreeItem_itemCount = 665;
  static const int k_TreeItem_items = 666;
  static const int k_TreeItem_paintedImages = 667;
  static const int k_TreeItem_paintedTexts = 668;
  static const int k_TreeItem_texts = 669;
  static const int k_Widget_style = 670;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  1,
  42,
  60,
  64,
  72,
  91,
  148,
  153,
  156,
  158,
  201,
  204,
  219,
  225,
  227,
  247,
  374,
  380,
  434,
  448,
  449,
  451,
  552,
  553,
  554,
  557,
  558,
  563,
  567,
  576,
  610,
  612,
  635,
  636,
  640,
  643,
  644,
  646,
  653,
  654,
  666,
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
  static const int displayPopups = 32; // DisplayPopups
  static const int dragSource = 33; // DragSource
  static const int dropTarget = 34; // DropTarget
  static const int event = 35; // Event
  static const int ewtWidget = 36; // EwtWidget
  static const int expandBar = 37; // ExpandBar
  static const int expandItem = 38; // ExpandItem
  static const int fileDialog = 39; // FileDialog
  static const int fileTransfer = 40; // FileTransfer
  static const int font = 41; // Font
  static const int fontData = 42; // FontData
  static const int fontDialog = 43; // FontDialog
  static const int fontMeasureBridge = 44; // FontMeasureBridge
  static const int fontMetrics = 45; // FontMetrics
  static const int formAttachment = 46; // FormAttachment
  static const int formData = 47; // FormData
  static const int gc = 48; // GC
  static const int gCData = 49; // GCData
  static const int gLCanvas = 50; // GLCanvas
  static const int gLData = 51; // GLData
  static const int glyphMetrics = 52; // GlyphMetrics
  static const int gridData = 53; // GridData
  static const int group = 54; // Group
  static const int hTMLTransfer = 55; // HTMLTransfer
  static const int ime = 56; // IME
  static const int image = 57; // Image
  static const int imageData = 58; // ImageData
  static const int imageSizeBridge = 59; // ImageSizeBridge
  static const int imageTransfer = 60; // ImageTransfer
  static const int label = 61; // Label
  static const int lineInfo = 62; // LineInfo
  static const int lineSizeInfo = 63; // LineSizeInfo
  static const int link = 64; // Link
  static const int list = 65; // List
  static const int mainComposite = 66; // MainComposite
  static const int mainToolbar = 67; // MainToolbar
  static const int menu = 68; // Menu
  static const int menuItem = 69; // MenuItem
  static const int messageBox = 70; // MessageBox
  static const int monitor = 71; // Monitor
  static const int pDFDocument = 72; // PDFDocument
  static const int path = 73; // Path
  static const int pathData = 74; // PathData
  static const int pattern = 75; // Pattern
  static const int point = 76; // Point
  static const int popupList = 77; // PopupList
  static const int printDialog = 78; // PrintDialog
  static const int printer = 79; // Printer
  static const int program = 80; // Program
  static const int progressBar = 81; // ProgressBar
  static const int rgb = 82; // RGB
  static const int rTFTransfer = 83; // RTFTransfer
  static const int rectangle = 84; // Rectangle
  static const int region = 85; // Region
  static const int resource = 86; // Resource
  static const int rowData = 87; // RowData
  static const int sash = 88; // Sash
  static const int sashForm = 89; // SashForm
  static const int scale = 90; // Scale
  static const int scrollBar = 91; // ScrollBar
  static const int scrolledComposite = 92; // ScrolledComposite
  static const int shell = 93; // Shell
  static const int shellPopups = 94; // ShellPopups
  static const int sideBar = 95; // SideBar
  static const int slider = 96; // Slider
  static const int spinner = 97; // Spinner
  static const int statusBar = 98; // StatusBar
  static const int styleRange = 99; // StyleRange
  static const int styledText = 100; // StyledText
  static const int styledTextPrintOptions = 101; // StyledTextPrintOptions
  static const int styledTextRenderer = 102; // StyledTextRenderer
  static const int swingIsland = 103; // SwingIsland
  static const int tabFolder = 104; // TabFolder
  static const int tabItem = 105; // TabItem
  static const int table = 106; // Table
  static const int tableColumn = 107; // TableColumn
  static const int tableCursor = 108; // TableCursor
  static const int tableEditor = 109; // TableEditor
  static const int tableItem = 110; // TableItem
  static const int taskBar = 111; // TaskBar
  static const int taskItem = 112; // TaskItem
  static const int text = 113; // Text
  static const int textStyle = 114; // TextStyle
  static const int textTransfer = 115; // TextTransfer
  static const int toolBar = 116; // ToolBar
  static const int toolItem = 117; // ToolItem
  static const int toolTip = 118; // ToolTip
  static const int touch = 119; // Touch
  static const int touchSource = 120; // TouchSource
  static const int tracker = 121; // Tracker
  static const int transfer = 122; // Transfer
  static const int transferData = 123; // TransferData
  static const int transform = 124; // Transform
  static const int tray = 125; // Tray
  static const int trayItem = 126; // TrayItem
  static const int tree = 127; // Tree
  static const int treeColumn = 128; // TreeColumn
  static const int treeCursor = 129; // TreeCursor
  static const int treeEditor = 130; // TreeEditor
  static const int treeItem = 131; // TreeItem
  static const int uRLTransfer = 132; // URLTransfer
  static const int viewForm = 133; // ViewForm
  static const int widgetSizeBridge = 134; // WidgetSizeBridge
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
  32: 'DisplayPopups',
  33: 'DragSource',
  34: 'DropTarget',
  35: 'Event',
  36: 'EwtWidget',
  37: 'ExpandBar',
  38: 'ExpandItem',
  39: 'FileDialog',
  40: 'FileTransfer',
  41: 'Font',
  42: 'FontData',
  43: 'FontDialog',
  44: 'FontMeasureBridge',
  45: 'FontMetrics',
  46: 'FormAttachment',
  47: 'FormData',
  48: 'GC',
  49: 'GCData',
  50: 'GLCanvas',
  51: 'GLData',
  52: 'GlyphMetrics',
  53: 'GridData',
  54: 'Group',
  55: 'HTMLTransfer',
  56: 'IME',
  57: 'Image',
  58: 'ImageData',
  59: 'ImageSizeBridge',
  60: 'ImageTransfer',
  61: 'Label',
  62: 'LineInfo',
  63: 'LineSizeInfo',
  64: 'Link',
  65: 'List',
  66: 'MainComposite',
  67: 'MainToolbar',
  68: 'Menu',
  69: 'MenuItem',
  70: 'MessageBox',
  71: 'Monitor',
  72: 'PDFDocument',
  73: 'Path',
  74: 'PathData',
  75: 'Pattern',
  76: 'Point',
  77: 'PopupList',
  78: 'PrintDialog',
  79: 'Printer',
  80: 'Program',
  81: 'ProgressBar',
  82: 'RGB',
  83: 'RTFTransfer',
  84: 'Rectangle',
  85: 'Region',
  86: 'Resource',
  87: 'RowData',
  88: 'Sash',
  89: 'SashForm',
  90: 'Scale',
  91: 'ScrollBar',
  92: 'ScrolledComposite',
  93: 'Shell',
  94: 'ShellPopups',
  95: 'SideBar',
  96: 'Slider',
  97: 'Spinner',
  98: 'StatusBar',
  99: 'StyleRange',
  100: 'StyledText',
  101: 'StyledTextPrintOptions',
  102: 'StyledTextRenderer',
  103: 'SwingIsland',
  104: 'TabFolder',
  105: 'TabItem',
  106: 'Table',
  107: 'TableColumn',
  108: 'TableCursor',
  109: 'TableEditor',
  110: 'TableItem',
  111: 'TaskBar',
  112: 'TaskItem',
  113: 'Text',
  114: 'TextStyle',
  115: 'TextTransfer',
  116: 'ToolBar',
  117: 'ToolItem',
  118: 'ToolTip',
  119: 'Touch',
  120: 'TouchSource',
  121: 'Tracker',
  122: 'Transfer',
  123: 'TransferData',
  124: 'Transform',
  125: 'Tray',
  126: 'TrayItem',
  127: 'Tree',
  128: 'TreeColumn',
  129: 'TreeCursor',
  130: 'TreeEditor',
  131: 'TreeItem',
  132: 'URLTransfer',
  133: 'ViewForm',
  134: 'WidgetSizeBridge',
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
  120: 'dirtyIndicatorStyle',
  121: 'disable_control_gc_overlay',
  122: 'disable_evolve_icons',
  123: 'disable_hover_zoom',
  124: 'disable_swt_canvas_colors',
  125: 'disabledImage',
  126: 'doit',
  127: 'doubleBuffer',
  128: 'doubleClickEnabled',
  129: 'double_click_timeout_ms',
  130: 'dragDetect',
  131: 'dragImage',
  132: 'dragSource',
  133: 'dragSourceId',
  134: 'dropTargetId',
  135: 'echoCharacter',
  136: 'editTexts',
  137: 'editable',
  138: 'editor',
  139: 'editors',
  140: 'edits',
  141: 'effectsVisible',
  142: 'elements',
  143: 'enabled',
  144: 'end',
  145: 'endPage',
  146: 'endX',
  147: 'endY',
  148: 'error',
  149: 'exclude',
  150: 'expandHorizontal',
  151: 'expandVertical',
  152: 'expanded',
  153: 'expires',
  154: 'feedback',
  155: 'fileName',
  156: 'fileNames',
  157: 'filename',
  158: 'fillRule',
  159: 'filterExtensions',
  160: 'filterIndex',
  161: 'filterNames',
  162: 'filterPath',
  163: 'fixedLineHeight',
  164: 'fixedPitch',
  165: 'flags',
  166: 'focus',
  167: 'focusHolderId',
  168: 'focus_indicators',
  169: 'focused',
  170: 'font',
  171: 'fontData',
  172: 'fontList',
  173: 'fontStyle',
  174: 'font_point_scale',
  175: 'fonts',
  176: 'footer',
  177: 'force_theme',
  178: 'foreground',
  179: 'foregroundPattern',
  180: 'foregrounds',
  181: 'fullRepaint',
  182: 'fullScreen',
  183: 'functionNames',
  184: 'g',
  185: 'gc',
  186: 'gc_icons_replacement',
  187: 'grabExcessHorizontalSpace',
  188: 'grabExcessVerticalSpace',
  189: 'gradientColors',
  190: 'gradientPercents',
  191: 'gradientVertical',
  192: 'grayed',
  193: 'green',
  194: 'greenSize',
  195: 'hHint',
  196: 'handle',
  197: 'hasLinks',
  198: 'hasOwnBackground',
  199: 'header',
  200: 'headerBackground',
  201: 'headerForeground',
  202: 'headerHeight',
  203: 'headerVisible',
  204: 'headers',
  205: 'height',
  206: 'heightHint',
  207: 'highlight',
  208: 'highlightEnabled',
  209: 'horizontalAlignment',
  210: 'horizontalBar',
  211: 'horizontalIndent',
  212: 'horizontalPixel',
  213: 'horizontalSpan',
  214: 'hotImage',
  215: 'hotspotX',
  216: 'hotspotY',
  217: 'hours',
  218: 'hoverFills',
  219: 'hover_delay_ms',
  220: 'id',
  221: 'idleRunning',
  222: 'image',
  223: 'imageData',
  224: 'image_disable_icons_replacement',
  225: 'images',
  226: 'increment',
  227: 'indent',
  228: 'index',
  229: 'inheritsBackground',
  230: 'interpolation',
  231: 'isVisited',
  232: 'italicFont',
  233: 'item',
  234: 'itemCount',
  235: 'itemId',
  236: 'itemOrder',
  237: 'itemSizes',
  238: 'itemTooltips',
  239: 'items',
  240: 'javascriptEnabled',
  241: 'jobName',
  242: 'justify',
  243: 'keptText',
  244: 'keyCode',
  245: 'keyLocation',
  246: 'left',
  247: 'leftMargin',
  248: 'length',
  249: 'lineCap',
  250: 'lineCount',
  251: 'lineDash',
  252: 'lineDashOffset',
  253: 'lineJoin',
  254: 'lineLabels',
  255: 'lineSizes',
  256: 'lineSpacing',
  257: 'lineSpacingComputing',
  258: 'lineSpacings',
  259: 'lineStyle',
  260: 'lineWidth',
  261: 'lines',
  262: 'linesInAverageLineHeight',
  263: 'linesVisible',
  264: 'linkForeground',
  265: 'listVisible',
  266: 'localFileBase',
  267: 'localFilePath',
  268: 'localFileText',
  269: 'locale',
  270: 'location',
  271: 'magnification',
  272: 'mainShellId',
  273: 'map',
  274: 'marginColor',
  275: 'maxWidth',
  276: 'maxWidthLineIndex',
  277: 'maximizeVisible',
  278: 'maximized',
  279: 'maximizedControl',
  280: 'maximum',
  281: 'maximumSize',
  282: 'menu',
  283: 'menuBar',
  284: 'menuStripHeight',
  285: 'message',
  286: 'metrics',
  287: 'minHeight',
  288: 'minWidth',
  289: 'minimizeVisible',
  290: 'minimized',
  291: 'minimum',
  292: 'minimumCharacters',
  293: 'minimumHeight',
  294: 'minimumSize',
  295: 'minimumWidth',
  296: 'minutes',
  297: 'modified',
  298: 'month',
  299: 'moveable',
  300: 'name',
  301: 'needed',
  302: 'notification_popup',
  303: 'numerator',
  304: 'offset',
  305: 'operations',
  306: 'orientation',
  307: 'origin',
  308: 'originRequest',
  309: 'overwrite',
  310: 'ownMenuStrip',
  311: 'ownerDrawItemHeight',
  312: 'pageIncrement',
  313: 'paintedImages',
  314: 'paintedTexts',
  315: 'pathData',
  316: 'pixels',
  317: 'points',
  318: 'popups',
  319: 'port',
  320: 'postData',
  321: 'preferredSize',
  322: 'preserve_icon_colors',
  323: 'primary',
  324: 'printLineBackground',
  325: 'printLineNumbers',
  326: 'printTextBackground',
  327: 'printTextFontStyle',
  328: 'printTextForeground',
  329: 'printToFile',
  330: 'print_move',
  331: 'push_button_margin',
  332: 'r',
  333: 'ranges',
  334: 'rects',
  335: 'red',
  336: 'redSize',
  337: 'redrawLines',
  338: 'ref',
  339: 'region',
  340: 'regularFont',
  341: 'remoteRef',
  342: 'renderer',
  343: 'replaced',
  344: 'reqId',
  345: 'resizable',
  346: 'result',
  347: 'retain',
  348: 'right',
  349: 'rightMargin',
  350: 'rise',
  351: 'rotation',
  352: 'sampleBuffers',
  353: 'samples',
  354: 'sashWidth',
  355: 'scale_vertical_minimum_at_top',
  356: 'scope',
  357: 'script',
  358: 'seconds',
  359: 'segments',
  360: 'segmentsChars',
  361: 'selectedImageVisible',
  362: 'selection',
  363: 'selectionBackground',
  364: 'selectionBarThickness',
  365: 'selectionBgImage',
  366: 'selectionFills',
  367: 'selectionForeground',
  368: 'selectionRanges',
  369: 'seq',
  370: 'shareContext',
  371: 'shells',
  372: 'showChevron',
  373: 'showClose',
  374: 'showDirty',
  375: 'showListPopupSeq',
  376: 'show_scaling_control',
  377: 'show_theme_color_palette',
  378: 'showing',
  379: 'single',
  380: 'sortColumn',
  381: 'sortDirection',
  382: 'source',
  383: 'spacing',
  384: 'start',
  385: 'startPage',
  386: 'startX',
  387: 'startY',
  388: 'state',
  389: 'stateMask',
  390: 'stencilSize',
  391: 'stereo',
  392: 'strikeout',
  393: 'strikeoutColor',
  394: 'stripLayoutSeq',
  395: 'style',
  396: 'styleCount',
  397: 'styleIndex',
  398: 'styledText',
  399: 'styles',
  400: 'stylesSet',
  401: 'stylesSetCount',
  402: 'svgContent',
  403: 'swt',
  404: 'systemMenu',
  405: 'system_menu_bar',
  406: 'tabHeight',
  407: 'tabLength',
  408: 'tabPosition',
  409: 'tabStop',
  410: 'tabStops',
  411: 'tabWidth',
  412: 'tabs',
  413: 'text',
  414: 'textFonts',
  415: 'textLimit',
  416: 'textOrigins',
  417: 'textStrings',
  418: 'texts',
  419: 'theme_color',
  420: 'theme_colors_by_widget',
  421: 'theme_name',
  422: 'themedCanvas',
  423: 'thumb',
  424: 'time',
  425: 'toolTipText',
  426: 'tooltips',
  427: 'top',
  428: 'topIndex',
  429: 'topItem',
  430: 'topMargin',
  431: 'topPixel',
  432: 'topRight',
  433: 'topRightAlignment',
  434: 'touches',
  435: 'tracking',
  436: 'transform',
  437: 'transparent_menu_check_box',
  438: 'type',
  439: 'types',
  440: 'ui_zoom',
  441: 'underline',
  442: 'underlineColor',
  443: 'underlineStyle',
  444: 'unselectedCloseVisible',
  445: 'unselectedImageVisible',
  446: 'url',
  447: 'use_special_dropdown_button',
  448: 'use_swt_border',
  449: 'use_swt_colors',
  450: 'use_swt_font_colors',
  451: 'use_swt_font_colors_by_widget',
  452: 'use_swt_fonts',
  453: 'value',
  454: 'verdict',
  455: 'verticalAlignment',
  456: 'verticalBar',
  457: 'verticalIndent',
  458: 'verticalSpan',
  459: 'visible',
  460: 'wHint',
  461: 'warnings',
  462: 'weights',
  463: 'widget',
  464: 'width',
  465: 'widthHint',
  466: 'windowId',
  467: 'windowedShellIds',
  468: 'wordWrap',
  469: 'wrapIndent',
  470: 'wrapIndices',
  471: 'x',
  472: 'xDirection',
  473: 'y',
  474: 'yDirection',
  475: 'year',
  476: 'zoom',
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
