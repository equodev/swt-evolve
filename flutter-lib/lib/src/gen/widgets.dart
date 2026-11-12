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
const int wireFingerprint = -634545929;

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
  static const int k_ConfigFlags_ctabfolder_chevron_after_tabs = 98;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 99;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 100;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 101;
  static const int k_ConfigFlags_custom_tooltip = 102;
  static const int k_ConfigFlags_decimal_separator = 103;
  static const int k_ConfigFlags_decorations_align = 104;
  static const int k_ConfigFlags_disable_control_gc_overlay = 105;
  static const int k_ConfigFlags_disable_evolve_icons = 106;
  static const int k_ConfigFlags_disable_hover_zoom = 107;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 108;
  static const int k_ConfigFlags_double_click_timeout_ms = 109;
  static const int k_ConfigFlags_focus_indicators = 110;
  static const int k_ConfigFlags_font_point_scale = 111;
  static const int k_ConfigFlags_force_theme = 112;
  static const int k_ConfigFlags_gc_icons_replacement = 113;
  static const int k_ConfigFlags_hover_delay_ms = 114;
  static const int k_ConfigFlags_image_disable_icons_replacement = 115;
  static const int k_ConfigFlags_locale = 116;
  static const int k_ConfigFlags_notification_popup = 117;
  static const int k_ConfigFlags_preserve_icon_colors = 118;
  static const int k_ConfigFlags_print_move = 119;
  static const int k_ConfigFlags_push_button_margin = 120;
  static const int k_ConfigFlags_scale_vertical_minimum_at_top = 121;
  static const int k_ConfigFlags_show_scaling_control = 122;
  static const int k_ConfigFlags_show_theme_color_palette = 123;
  static const int k_ConfigFlags_system_menu_bar = 124;
  static const int k_ConfigFlags_theme_color = 125;
  static const int k_ConfigFlags_theme_colors_by_widget = 126;
  static const int k_ConfigFlags_theme_name = 127;
  static const int k_ConfigFlags_transparent_menu_check_box = 128;
  static const int k_ConfigFlags_ui_zoom = 129;
  static const int k_ConfigFlags_use_special_dropdown_button = 130;
  static const int k_ConfigFlags_use_swt_border = 131;
  static const int k_ConfigFlags_use_swt_colors = 132;
  static const int k_ConfigFlags_use_swt_font_colors = 133;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 134;
  static const int k_ConfigFlags_use_swt_fonts = 135;
  static const int k_Control_background = 136;
  static const int k_Control_backgroundImage = 137;
  static const int k_Control_bounds = 138;
  static const int k_Control_cursor = 139;
  static const int k_Control_dragDetect = 140;
  static const int k_Control_dragSource = 141;
  static const int k_Control_dragSourceId = 142;
  static const int k_Control_dropTargetId = 143;
  static const int k_Control_enabled = 144;
  static const int k_Control_font = 145;
  static const int k_Control_foreground = 146;
  static const int k_Control_hasOwnBackground = 147;
  static const int k_Control_inheritsBackground = 148;
  static const int k_Control_menu = 149;
  static const int k_Control_region = 150;
  static const int k_Control_tabStop = 151;
  static const int k_Control_toolTipText = 152;
  static const int k_Control_visible = 153;
  static const int k_ControlEditor_editor = 154;
  static const int k_CoolBar_itemOrder = 155;
  static const int k_CoolBar_itemSizes = 156;
  static const int k_CoolBar_items = 157;
  static const int k_CoolBar_wrapIndices = 158;
  static const int k_CoolItem_control = 159;
  static const int k_CoolItem_preferredSize = 160;
  static const int k_Cursor_cursorStyle = 161;
  static const int k_Cursor_hotspotX = 162;
  static const int k_Cursor_hotspotY = 163;
  static const int k_Cursor_image = 164;
  static const int k_DateTime_day = 165;
  static const int k_DateTime_hours = 166;
  static const int k_DateTime_minutes = 167;
  static const int k_DateTime_month = 168;
  static const int k_DateTime_seconds = 169;
  static const int k_DateTime_year = 170;
  static const int k_Decorations_menuBar = 171;
  static const int k_Decorations_menuStripHeight = 172;
  static const int k_Decorations_ownMenuStrip = 173;
  static const int k_Decorations_text = 174;
  static const int k_Device_fontList = 175;
  static const int k_Device_tracking = 176;
  static const int k_Device_warnings = 177;
  static const int k_Dialog_id = 178;
  static const int k_Dialog_style = 179;
  static const int k_Dialog_swt = 180;
  static const int k_Dialog_text = 181;
  static const int k_DirectoryDialog_filterPath = 182;
  static const int k_DirectoryDialog_message = 183;
  static const int k_Display_activeShellId = 184;
  static const int k_Display_config = 185;
  static const int k_Display_focusHolderId = 186;
  static const int k_Display_mainShellId = 187;
  static const int k_Display_popups = 188;
  static const int k_Display_shells = 189;
  static const int k_Display_systemMenu = 190;
  static const int k_Display_tooltips = 191;
  static const int k_Display_windowedShellIds = 192;
  static const int k_Event_button = 193;
  static const int k_Event_character = 194;
  static const int k_Event_count = 195;
  static const int k_Event_currentDataTypeId = 196;
  static const int k_Event_detail = 197;
  static const int k_Event_doit = 198;
  static const int k_Event_dragImage = 199;
  static const int k_Event_end = 200;
  static const int k_Event_feedback = 201;
  static const int k_Event_gc = 202;
  static const int k_Event_height = 203;
  static const int k_Event_index = 204;
  static const int k_Event_item = 205;
  static const int k_Event_itemId = 206;
  static const int k_Event_keyCode = 207;
  static const int k_Event_keyLocation = 208;
  static const int k_Event_magnification = 209;
  static const int k_Event_operations = 210;
  static const int k_Event_rotation = 211;
  static const int k_Event_segments = 212;
  static const int k_Event_segmentsChars = 213;
  static const int k_Event_start = 214;
  static const int k_Event_stateMask = 215;
  static const int k_Event_text = 216;
  static const int k_Event_time = 217;
  static const int k_Event_touches = 218;
  static const int k_Event_type = 219;
  static const int k_Event_widget = 220;
  static const int k_Event_width = 221;
  static const int k_Event_x = 222;
  static const int k_Event_xDirection = 223;
  static const int k_Event_y = 224;
  static const int k_Event_yDirection = 225;
  static const int k_ExpandBar_items = 226;
  static const int k_ExpandBar_spacing = 227;
  static const int k_ExpandItem_control = 228;
  static const int k_ExpandItem_expanded = 229;
  static const int k_ExpandItem_headerHeight = 230;
  static const int k_ExpandItem_height = 231;
  static const int k_FileDialog_fileName = 232;
  static const int k_FileDialog_fileNames = 233;
  static const int k_FileDialog_filterExtensions = 234;
  static const int k_FileDialog_filterIndex = 235;
  static const int k_FileDialog_filterNames = 236;
  static const int k_FileDialog_filterPath = 237;
  static const int k_FileDialog_overwrite = 238;
  static const int k_Font_fontData = 239;
  static const int k_FontData_height = 240;
  static const int k_FontData_name = 241;
  static const int k_FontData_style = 242;
  static const int k_FontDialog_RGB = 243;
  static const int k_FontDialog_effectsVisible = 244;
  static const int k_FontDialog_fontData = 245;
  static const int k_FontDialog_fontList = 246;
  static const int k_FormAttachment_alignment = 247;
  static const int k_FormAttachment_control = 248;
  static const int k_FormAttachment_denominator = 249;
  static const int k_FormAttachment_numerator = 250;
  static const int k_FormAttachment_offset = 251;
  static const int k_FormData_bottom = 252;
  static const int k_FormData_cacheBottom = 253;
  static const int k_FormData_cacheHeight = 254;
  static const int k_FormData_cacheLeft = 255;
  static const int k_FormData_cacheRight = 256;
  static const int k_FormData_cacheTop = 257;
  static const int k_FormData_cacheWidth = 258;
  static const int k_FormData_currentHeight = 259;
  static const int k_FormData_currentHhint = 260;
  static const int k_FormData_currentWhint = 261;
  static const int k_FormData_currentWidth = 262;
  static const int k_FormData_defaultHeight = 263;
  static const int k_FormData_defaultHhint = 264;
  static const int k_FormData_defaultWhint = 265;
  static const int k_FormData_defaultWidth = 266;
  static const int k_FormData_height = 267;
  static const int k_FormData_isVisited = 268;
  static const int k_FormData_left = 269;
  static const int k_FormData_needed = 270;
  static const int k_FormData_right = 271;
  static const int k_FormData_top = 272;
  static const int k_FormData_width = 273;
  static const int k_GC_XORMode = 274;
  static const int k_GC_alpha = 275;
  static const int k_GC_antialias = 276;
  static const int k_GC_background = 277;
  static const int k_GC_backgroundPattern = 278;
  static const int k_GC_bufferScale = 279;
  static const int k_GC_bufferScaleY = 280;
  static const int k_GC_clipping = 281;
  static const int k_GC_clippingPath = 282;
  static const int k_GC_clippingRects = 283;
  static const int k_GC_clippingText = 284;
  static const int k_GC_fillRule = 285;
  static const int k_GC_font = 286;
  static const int k_GC_foreground = 287;
  static const int k_GC_foregroundPattern = 288;
  static const int k_GC_hoverFills = 289;
  static const int k_GC_interpolation = 290;
  static const int k_GC_lineCap = 291;
  static const int k_GC_lineDash = 292;
  static const int k_GC_lineDashOffset = 293;
  static const int k_GC_lineJoin = 294;
  static const int k_GC_lineStyle = 295;
  static const int k_GC_lineWidth = 296;
  static const int k_GC_selectionFills = 297;
  static const int k_GC_style = 298;
  static const int k_GC_themedCanvas = 299;
  static const int k_GC_transform = 300;
  static const int k_GLData_accumAlphaSize = 301;
  static const int k_GLData_accumBlueSize = 302;
  static const int k_GLData_accumGreenSize = 303;
  static const int k_GLData_accumRedSize = 304;
  static const int k_GLData_alphaSize = 305;
  static const int k_GLData_blueSize = 306;
  static const int k_GLData_depthSize = 307;
  static const int k_GLData_doubleBuffer = 308;
  static const int k_GLData_greenSize = 309;
  static const int k_GLData_redSize = 310;
  static const int k_GLData_sampleBuffers = 311;
  static const int k_GLData_samples = 312;
  static const int k_GLData_shareContext = 313;
  static const int k_GLData_stencilSize = 314;
  static const int k_GLData_stereo = 315;
  static const int k_GlyphMetrics_ascent = 316;
  static const int k_GlyphMetrics_descent = 317;
  static const int k_GlyphMetrics_width = 318;
  static const int k_GridData_cacheHeight = 319;
  static const int k_GridData_cacheWidth = 320;
  static const int k_GridData_currentHeight = 321;
  static const int k_GridData_currentHhint = 322;
  static const int k_GridData_currentWhint = 323;
  static const int k_GridData_currentWidth = 324;
  static const int k_GridData_defaultHeight = 325;
  static const int k_GridData_defaultHhint = 326;
  static const int k_GridData_defaultWhint = 327;
  static const int k_GridData_defaultWidth = 328;
  static const int k_GridData_exclude = 329;
  static const int k_GridData_grabExcessHorizontalSpace = 330;
  static const int k_GridData_grabExcessVerticalSpace = 331;
  static const int k_GridData_heightHint = 332;
  static const int k_GridData_horizontalAlignment = 333;
  static const int k_GridData_horizontalIndent = 334;
  static const int k_GridData_horizontalSpan = 335;
  static const int k_GridData_minimumHeight = 336;
  static const int k_GridData_minimumWidth = 337;
  static const int k_GridData_verticalAlignment = 338;
  static const int k_GridData_verticalIndent = 339;
  static const int k_GridData_verticalSpan = 340;
  static const int k_GridData_widthHint = 341;
  static const int k_Group_text = 342;
  static const int k_Image_filename = 343;
  static const int k_Image_height = 344;
  static const int k_Image_imageData = 345;
  static const int k_Image_remoteRef = 346;
  static const int k_Image_svgContent = 347;
  static const int k_Image_width = 348;
  static const int k_ImageData_data = 349;
  static const int k_ImageData_depth = 350;
  static const int k_ImageData_height = 351;
  static const int k_ImageData_width = 352;
  static const int k_Item_image = 353;
  static const int k_Item_text = 354;
  static const int k_Label_image = 355;
  static const int k_Label_text = 356;
  static const int k_LineInfo_alignment = 357;
  static const int k_LineInfo_background = 358;
  static const int k_LineInfo_flags = 359;
  static const int k_LineInfo_indent = 360;
  static const int k_LineInfo_justify = 361;
  static const int k_LineInfo_segments = 362;
  static const int k_LineInfo_segmentsChars = 363;
  static const int k_LineInfo_tabStops = 364;
  static const int k_LineInfo_verticalIndent = 365;
  static const int k_LineInfo_wrapIndent = 366;
  static const int k_LineSizeInfo_height = 367;
  static const int k_LineSizeInfo_width = 368;
  static const int k_Link_linkForeground = 369;
  static const int k_Link_text = 370;
  static const int k_List_items = 371;
  static const int k_List_selection = 372;
  static const int k_List_topIndex = 373;
  static const int k_Menu_enabled = 374;
  static const int k_Menu_items = 375;
  static const int k_Menu_location = 376;
  static const int k_Menu_orientation = 377;
  static const int k_Menu_visible = 378;
  static const int k_MenuItem_accelerator = 379;
  static const int k_MenuItem_enabled = 380;
  static const int k_MenuItem_menu = 381;
  static const int k_MenuItem_selection = 382;
  static const int k_MenuItem_toolTipText = 383;
  static const int k_MessageBox_buttonLabels = 384;
  static const int k_MessageBox_message = 385;
  static const int k_Monitor_clientHeight = 386;
  static const int k_Monitor_clientWidth = 387;
  static const int k_Monitor_clientX = 388;
  static const int k_Monitor_clientY = 389;
  static const int k_Monitor_handle = 390;
  static const int k_Monitor_height = 391;
  static const int k_Monitor_width = 392;
  static const int k_Monitor_x = 393;
  static const int k_Monitor_y = 394;
  static const int k_Monitor_zoom = 395;
  static const int k_PDFDocument_height = 396;
  static const int k_PDFDocument_width = 397;
  static const int k_Path_pathData = 398;
  static const int k_Path_textFonts = 399;
  static const int k_Path_textOrigins = 400;
  static const int k_Path_textStrings = 401;
  static const int k_PathData_points = 402;
  static const int k_PathData_types = 403;
  static const int k_Pattern_color1 = 404;
  static const int k_Pattern_color2 = 405;
  static const int k_Pattern_endX = 406;
  static const int k_Pattern_endY = 407;
  static const int k_Pattern_image = 408;
  static const int k_Pattern_startX = 409;
  static const int k_Pattern_startY = 410;
  static const int k_Point_x = 411;
  static const int k_Point_y = 412;
  static const int k_PopupList_font = 413;
  static const int k_PopupList_items = 414;
  static const int k_PopupList_minimumWidth = 415;
  static const int k_PopupList_selection = 416;
  static const int k_PrintDialog_endPage = 417;
  static const int k_PrintDialog_printToFile = 418;
  static const int k_PrintDialog_scope = 419;
  static const int k_PrintDialog_startPage = 420;
  static const int k_ProgressBar_maximum = 421;
  static const int k_ProgressBar_minimum = 422;
  static const int k_ProgressBar_selection = 423;
  static const int k_RGB_blue = 424;
  static const int k_RGB_green = 425;
  static const int k_RGB_red = 426;
  static const int k_Rectangle_height = 427;
  static const int k_Rectangle_width = 428;
  static const int k_Rectangle_x = 429;
  static const int k_Rectangle_y = 430;
  static const int k_Region_rects = 431;
  static const int k_RowData_exclude = 432;
  static const int k_RowData_height = 433;
  static const int k_RowData_width = 434;
  static const int k_SashForm_maximizedControl = 435;
  static const int k_SashForm_sashWidth = 436;
  static const int k_SashForm_weights = 437;
  static const int k_Scale_maximum = 438;
  static const int k_Scale_minimum = 439;
  static const int k_Scale_selection = 440;
  static const int k_ScrollBar_enabled = 441;
  static const int k_ScrollBar_increment = 442;
  static const int k_ScrollBar_maximum = 443;
  static const int k_ScrollBar_minimum = 444;
  static const int k_ScrollBar_pageIncrement = 445;
  static const int k_ScrollBar_selection = 446;
  static const int k_ScrollBar_thumb = 447;
  static const int k_ScrollBar_visible = 448;
  static const int k_Scrollable_horizontalBar = 449;
  static const int k_Scrollable_verticalBar = 450;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 451;
  static const int k_ScrolledComposite_content = 452;
  static const int k_ScrolledComposite_expandHorizontal = 453;
  static const int k_ScrolledComposite_expandVertical = 454;
  static const int k_ScrolledComposite_minHeight = 455;
  static const int k_ScrolledComposite_minWidth = 456;
  static const int k_ScrolledComposite_origin = 457;
  static const int k_ScrolledComposite_originRequest = 458;
  static const int k_Shell_alpha = 459;
  static const int k_Shell_dialogs = 460;
  static const int k_Shell_fullScreen = 461;
  static const int k_Shell_maximumSize = 462;
  static const int k_Shell_minimumSize = 463;
  static const int k_Shell_modified = 464;
  static const int k_Slider_increment = 465;
  static const int k_Slider_maximum = 466;
  static const int k_Slider_minimum = 467;
  static const int k_Slider_pageIncrement = 468;
  static const int k_Slider_selection = 469;
  static const int k_Slider_thumb = 470;
  static const int k_Spinner_digits = 471;
  static const int k_Spinner_increment = 472;
  static const int k_Spinner_keptText = 473;
  static const int k_Spinner_maximum = 474;
  static const int k_Spinner_minimum = 475;
  static const int k_Spinner_pageIncrement = 476;
  static const int k_Spinner_selection = 477;
  static const int k_Spinner_textLimit = 478;
  static const int k_StyleRange_fontStyle = 479;
  static const int k_StyleRange_length = 480;
  static const int k_StyleRange_start = 481;
  static const int k_StyledText_alignment = 482;
  static const int k_StyledText_alwaysShowScrollBars = 483;
  static const int k_StyledText_blockSelection = 484;
  static const int k_StyledText_blockSelectionBounds = 485;
  static const int k_StyledText_bottomMargin = 486;
  static const int k_StyledText_caretOffset = 487;
  static const int k_StyledText_caretOffsets = 488;
  static const int k_StyledText_doubleClickEnabled = 489;
  static const int k_StyledText_editable = 490;
  static const int k_StyledText_fixedLineHeight = 491;
  static const int k_StyledText_horizontalPixel = 492;
  static const int k_StyledText_indent = 493;
  static const int k_StyledText_justify = 494;
  static const int k_StyledText_leftMargin = 495;
  static const int k_StyledText_lineSpacing = 496;
  static const int k_StyledText_marginColor = 497;
  static const int k_StyledText_renderer = 498;
  static const int k_StyledText_rightMargin = 499;
  static const int k_StyledText_selection = 500;
  static const int k_StyledText_selectionBackground = 501;
  static const int k_StyledText_selectionForeground = 502;
  static const int k_StyledText_selectionRanges = 503;
  static const int k_StyledText_styleIndex = 504;
  static const int k_StyledText_styles = 505;
  static const int k_StyledText_tabStops = 506;
  static const int k_StyledText_tabs = 507;
  static const int k_StyledText_text = 508;
  static const int k_StyledText_topMargin = 509;
  static const int k_StyledText_topPixel = 510;
  static const int k_StyledText_wordWrap = 511;
  static const int k_StyledText_wrapIndent = 512;
  static const int k_StyledTextPrintOptions_footer = 513;
  static const int k_StyledTextPrintOptions_header = 514;
  static const int k_StyledTextPrintOptions_jobName = 515;
  static const int k_StyledTextPrintOptions_lineLabels = 516;
  static const int k_StyledTextPrintOptions_printLineBackground = 517;
  static const int k_StyledTextPrintOptions_printLineNumbers = 518;
  static const int k_StyledTextPrintOptions_printTextBackground = 519;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 520;
  static const int k_StyledTextPrintOptions_printTextForeground = 521;
  static const int k_StyledTextRenderer_ascent = 522;
  static const int k_StyledTextRenderer_averageCharWidth = 523;
  static const int k_StyledTextRenderer_averageLineHeight = 524;
  static const int k_StyledTextRenderer_boldFont = 525;
  static const int k_StyledTextRenderer_boldItalicFont = 526;
  static const int k_StyledTextRenderer_bulletTexts = 527;
  static const int k_StyledTextRenderer_bulletsIndices = 528;
  static const int k_StyledTextRenderer_descent = 529;
  static const int k_StyledTextRenderer_fixedPitch = 530;
  static const int k_StyledTextRenderer_hasLinks = 531;
  static const int k_StyledTextRenderer_idleRunning = 532;
  static const int k_StyledTextRenderer_italicFont = 533;
  static const int k_StyledTextRenderer_lineCount = 534;
  static const int k_StyledTextRenderer_lineSizes = 535;
  static const int k_StyledTextRenderer_lineSpacingComputing = 536;
  static const int k_StyledTextRenderer_lineSpacings = 537;
  static const int k_StyledTextRenderer_lines = 538;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 539;
  static const int k_StyledTextRenderer_maxWidth = 540;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 541;
  static const int k_StyledTextRenderer_ranges = 542;
  static const int k_StyledTextRenderer_redrawLines = 543;
  static const int k_StyledTextRenderer_regularFont = 544;
  static const int k_StyledTextRenderer_styleCount = 545;
  static const int k_StyledTextRenderer_styledText = 546;
  static const int k_StyledTextRenderer_styles = 547;
  static const int k_StyledTextRenderer_stylesSet = 548;
  static const int k_StyledTextRenderer_stylesSetCount = 549;
  static const int k_StyledTextRenderer_tabLength = 550;
  static const int k_StyledTextRenderer_tabWidth = 551;
  static const int k_StyledTextRenderer_topIndex = 552;
  static const int k_TabFolder_items = 553;
  static const int k_TabFolder_selection = 554;
  static const int k_TabItem_control = 555;
  static const int k_TabItem_toolTipText = 556;
  static const int k_Table_columnOrder = 557;
  static const int k_Table_columns = 558;
  static const int k_Table_editors = 559;
  static const int k_Table_headerBackground = 560;
  static const int k_Table_headerForeground = 561;
  static const int k_Table_headerVisible = 562;
  static const int k_Table_itemCount = 563;
  static const int k_Table_items = 564;
  static const int k_Table_linesVisible = 565;
  static const int k_Table_ownerDrawItemHeight = 566;
  static const int k_Table_selection = 567;
  static const int k_Table_sortColumn = 568;
  static const int k_Table_sortDirection = 569;
  static const int k_Table_topIndex = 570;
  static const int k_TableColumn_alignment = 571;
  static const int k_TableColumn_moveable = 572;
  static const int k_TableColumn_resizable = 573;
  static const int k_TableColumn_toolTipText = 574;
  static const int k_TableColumn_width = 575;
  static const int k_TableEditor_column = 576;
  static const int k_TableEditor_item = 577;
  static const int k_TableItem_background = 578;
  static const int k_TableItem_backgrounds = 579;
  static const int k_TableItem_checked = 580;
  static const int k_TableItem_font = 581;
  static const int k_TableItem_fonts = 582;
  static const int k_TableItem_foreground = 583;
  static const int k_TableItem_foregrounds = 584;
  static const int k_TableItem_grayed = 585;
  static const int k_TableItem_images = 586;
  static const int k_TableItem_paintedImages = 587;
  static const int k_TableItem_paintedTexts = 588;
  static const int k_TableItem_texts = 589;
  static const int k_Text_caretPosition = 590;
  static const int k_Text_echoCharacter = 591;
  static const int k_Text_editTexts = 592;
  static const int k_Text_editable = 593;
  static const int k_Text_edits = 594;
  static const int k_Text_message = 595;
  static const int k_Text_selection = 596;
  static const int k_Text_text = 597;
  static const int k_Text_textLimit = 598;
  static const int k_TextStyle_background = 599;
  static const int k_TextStyle_borderColor = 600;
  static const int k_TextStyle_borderStyle = 601;
  static const int k_TextStyle_font = 602;
  static const int k_TextStyle_foreground = 603;
  static const int k_TextStyle_metrics = 604;
  static const int k_TextStyle_rise = 605;
  static const int k_TextStyle_strikeout = 606;
  static const int k_TextStyle_strikeoutColor = 607;
  static const int k_TextStyle_underline = 608;
  static const int k_TextStyle_underlineColor = 609;
  static const int k_TextStyle_underlineStyle = 610;
  static const int k_ToolBar_items = 611;
  static const int k_ToolItem_background = 612;
  static const int k_ToolItem_control = 613;
  static const int k_ToolItem_disabledImage = 614;
  static const int k_ToolItem_enabled = 615;
  static const int k_ToolItem_foreground = 616;
  static const int k_ToolItem_hotImage = 617;
  static const int k_ToolItem_selection = 618;
  static const int k_ToolItem_toolTipText = 619;
  static const int k_ToolItem_width = 620;
  static const int k_ToolTip_location = 621;
  static const int k_ToolTip_message = 622;
  static const int k_ToolTip_text = 623;
  static const int k_ToolTip_visible = 624;
  static const int k_Touch_id = 625;
  static const int k_Touch_primary = 626;
  static const int k_Touch_source = 627;
  static const int k_Touch_state = 628;
  static const int k_Touch_x = 629;
  static const int k_Touch_y = 630;
  static const int k_TouchSource_bounds = 631;
  static const int k_TouchSource_direct = 632;
  static const int k_TouchSource_handle = 633;
  static const int k_Transform_elements = 634;
  static const int k_Tree_columnOrder = 635;
  static const int k_Tree_columns = 636;
  static const int k_Tree_editors = 637;
  static const int k_Tree_headerBackground = 638;
  static const int k_Tree_headerForeground = 639;
  static const int k_Tree_headerVisible = 640;
  static const int k_Tree_items = 641;
  static const int k_Tree_linesVisible = 642;
  static const int k_Tree_ownerDrawItemHeight = 643;
  static const int k_Tree_selection = 644;
  static const int k_Tree_sortColumn = 645;
  static const int k_Tree_sortDirection = 646;
  static const int k_Tree_topItem = 647;
  static const int k_TreeColumn_alignment = 648;
  static const int k_TreeColumn_moveable = 649;
  static const int k_TreeColumn_resizable = 650;
  static const int k_TreeColumn_toolTipText = 651;
  static const int k_TreeColumn_width = 652;
  static const int k_TreeEditor_column = 653;
  static const int k_TreeEditor_editor = 654;
  static const int k_TreeEditor_item = 655;
  static const int k_TreeItem_background = 656;
  static const int k_TreeItem_backgrounds = 657;
  static const int k_TreeItem_checked = 658;
  static const int k_TreeItem_expanded = 659;
  static const int k_TreeItem_font = 660;
  static const int k_TreeItem_fonts = 661;
  static const int k_TreeItem_foreground = 662;
  static const int k_TreeItem_foregrounds = 663;
  static const int k_TreeItem_grayed = 664;
  static const int k_TreeItem_images = 665;
  static const int k_TreeItem_itemCount = 666;
  static const int k_TreeItem_items = 667;
  static const int k_TreeItem_paintedImages = 668;
  static const int k_TreeItem_paintedTexts = 669;
  static const int k_TreeItem_texts = 670;
  static const int k_Widget_style = 671;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  1,
  42,
  60,
  64,
  72,
  91,
  149,
  154,
  157,
  159,
  202,
  205,
  220,
  226,
  228,
  248,
  375,
  381,
  435,
  449,
  450,
  452,
  553,
  554,
  555,
  558,
  559,
  564,
  568,
  577,
  611,
  613,
  636,
  637,
  641,
  644,
  645,
  647,
  654,
  655,
  667,
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
  92: 'ctabfolder_chevron_after_tabs',
  93: 'ctabfolder_tab_scroll',
  94: 'ctabfolder_topright_auto_hide',
  95: 'ctabfolder_visible_controls',
  96: 'currentDataTypeId',
  97: 'currentHeight',
  98: 'currentHhint',
  99: 'currentWhint',
  100: 'currentWidth',
  101: 'cursor',
  102: 'cursorStyle',
  103: 'custom_tooltip',
  104: 'damage',
  105: 'data',
  106: 'day',
  107: 'decimal_separator',
  108: 'decorations_align',
  109: 'defaultHeight',
  110: 'defaultHhint',
  111: 'defaultWhint',
  112: 'defaultWidth',
  113: 'denominator',
  114: 'depth',
  115: 'depthSize',
  116: 'descent',
  117: 'detail',
  118: 'dialogs',
  119: 'digits',
  120: 'direct',
  121: 'dirtyIndicatorStyle',
  122: 'disable_control_gc_overlay',
  123: 'disable_evolve_icons',
  124: 'disable_hover_zoom',
  125: 'disable_swt_canvas_colors',
  126: 'disabledImage',
  127: 'doit',
  128: 'doubleBuffer',
  129: 'doubleClickEnabled',
  130: 'double_click_timeout_ms',
  131: 'dragDetect',
  132: 'dragImage',
  133: 'dragSource',
  134: 'dragSourceId',
  135: 'dropTargetId',
  136: 'echoCharacter',
  137: 'editTexts',
  138: 'editable',
  139: 'editor',
  140: 'editors',
  141: 'edits',
  142: 'effectsVisible',
  143: 'elements',
  144: 'enabled',
  145: 'end',
  146: 'endPage',
  147: 'endX',
  148: 'endY',
  149: 'error',
  150: 'exclude',
  151: 'expandHorizontal',
  152: 'expandVertical',
  153: 'expanded',
  154: 'expires',
  155: 'feedback',
  156: 'fileName',
  157: 'fileNames',
  158: 'filename',
  159: 'fillRule',
  160: 'filterExtensions',
  161: 'filterIndex',
  162: 'filterNames',
  163: 'filterPath',
  164: 'fixedLineHeight',
  165: 'fixedPitch',
  166: 'flags',
  167: 'focus',
  168: 'focusHolderId',
  169: 'focus_indicators',
  170: 'focused',
  171: 'font',
  172: 'fontData',
  173: 'fontList',
  174: 'fontStyle',
  175: 'font_point_scale',
  176: 'fonts',
  177: 'footer',
  178: 'force_theme',
  179: 'foreground',
  180: 'foregroundPattern',
  181: 'foregrounds',
  182: 'fullRepaint',
  183: 'fullScreen',
  184: 'functionNames',
  185: 'g',
  186: 'gc',
  187: 'gc_icons_replacement',
  188: 'grabExcessHorizontalSpace',
  189: 'grabExcessVerticalSpace',
  190: 'gradientColors',
  191: 'gradientPercents',
  192: 'gradientVertical',
  193: 'grayed',
  194: 'green',
  195: 'greenSize',
  196: 'hHint',
  197: 'handle',
  198: 'hasLinks',
  199: 'hasOwnBackground',
  200: 'header',
  201: 'headerBackground',
  202: 'headerForeground',
  203: 'headerHeight',
  204: 'headerVisible',
  205: 'headers',
  206: 'height',
  207: 'heightHint',
  208: 'highlight',
  209: 'highlightEnabled',
  210: 'horizontalAlignment',
  211: 'horizontalBar',
  212: 'horizontalIndent',
  213: 'horizontalPixel',
  214: 'horizontalSpan',
  215: 'hotImage',
  216: 'hotspotX',
  217: 'hotspotY',
  218: 'hours',
  219: 'hoverFills',
  220: 'hover_delay_ms',
  221: 'id',
  222: 'idleRunning',
  223: 'image',
  224: 'imageData',
  225: 'image_disable_icons_replacement',
  226: 'images',
  227: 'increment',
  228: 'indent',
  229: 'index',
  230: 'inheritsBackground',
  231: 'interpolation',
  232: 'isVisited',
  233: 'italicFont',
  234: 'item',
  235: 'itemCount',
  236: 'itemId',
  237: 'itemOrder',
  238: 'itemSizes',
  239: 'itemTooltips',
  240: 'items',
  241: 'javascriptEnabled',
  242: 'jobName',
  243: 'justify',
  244: 'keptText',
  245: 'keyCode',
  246: 'keyLocation',
  247: 'left',
  248: 'leftMargin',
  249: 'length',
  250: 'lineCap',
  251: 'lineCount',
  252: 'lineDash',
  253: 'lineDashOffset',
  254: 'lineJoin',
  255: 'lineLabels',
  256: 'lineSizes',
  257: 'lineSpacing',
  258: 'lineSpacingComputing',
  259: 'lineSpacings',
  260: 'lineStyle',
  261: 'lineWidth',
  262: 'lines',
  263: 'linesInAverageLineHeight',
  264: 'linesVisible',
  265: 'linkForeground',
  266: 'listVisible',
  267: 'localFileBase',
  268: 'localFilePath',
  269: 'localFileText',
  270: 'locale',
  271: 'location',
  272: 'magnification',
  273: 'mainShellId',
  274: 'map',
  275: 'marginColor',
  276: 'maxWidth',
  277: 'maxWidthLineIndex',
  278: 'maximizeVisible',
  279: 'maximized',
  280: 'maximizedControl',
  281: 'maximum',
  282: 'maximumSize',
  283: 'menu',
  284: 'menuBar',
  285: 'menuStripHeight',
  286: 'message',
  287: 'metrics',
  288: 'minHeight',
  289: 'minWidth',
  290: 'minimizeVisible',
  291: 'minimized',
  292: 'minimum',
  293: 'minimumCharacters',
  294: 'minimumHeight',
  295: 'minimumSize',
  296: 'minimumWidth',
  297: 'minutes',
  298: 'modified',
  299: 'month',
  300: 'moveable',
  301: 'name',
  302: 'needed',
  303: 'notification_popup',
  304: 'numerator',
  305: 'offset',
  306: 'operations',
  307: 'orientation',
  308: 'origin',
  309: 'originRequest',
  310: 'overwrite',
  311: 'ownMenuStrip',
  312: 'ownerDrawItemHeight',
  313: 'pageIncrement',
  314: 'paintedImages',
  315: 'paintedTexts',
  316: 'pathData',
  317: 'pixels',
  318: 'points',
  319: 'popups',
  320: 'port',
  321: 'postData',
  322: 'preferredSize',
  323: 'preserve_icon_colors',
  324: 'primary',
  325: 'printLineBackground',
  326: 'printLineNumbers',
  327: 'printTextBackground',
  328: 'printTextFontStyle',
  329: 'printTextForeground',
  330: 'printToFile',
  331: 'print_move',
  332: 'push_button_margin',
  333: 'r',
  334: 'ranges',
  335: 'rects',
  336: 'red',
  337: 'redSize',
  338: 'redrawLines',
  339: 'ref',
  340: 'region',
  341: 'regularFont',
  342: 'remoteRef',
  343: 'renderer',
  344: 'replaced',
  345: 'reqId',
  346: 'resizable',
  347: 'result',
  348: 'retain',
  349: 'right',
  350: 'rightMargin',
  351: 'rise',
  352: 'rotation',
  353: 'sampleBuffers',
  354: 'samples',
  355: 'sashWidth',
  356: 'scale_vertical_minimum_at_top',
  357: 'scope',
  358: 'script',
  359: 'seconds',
  360: 'segments',
  361: 'segmentsChars',
  362: 'selectedImageVisible',
  363: 'selection',
  364: 'selectionBackground',
  365: 'selectionBarThickness',
  366: 'selectionBgImage',
  367: 'selectionFills',
  368: 'selectionForeground',
  369: 'selectionRanges',
  370: 'seq',
  371: 'shareContext',
  372: 'shells',
  373: 'showChevron',
  374: 'showClose',
  375: 'showDirty',
  376: 'showListPopupSeq',
  377: 'show_scaling_control',
  378: 'show_theme_color_palette',
  379: 'showing',
  380: 'single',
  381: 'sortColumn',
  382: 'sortDirection',
  383: 'source',
  384: 'spacing',
  385: 'start',
  386: 'startPage',
  387: 'startX',
  388: 'startY',
  389: 'state',
  390: 'stateMask',
  391: 'stencilSize',
  392: 'stereo',
  393: 'strikeout',
  394: 'strikeoutColor',
  395: 'stripLayoutSeq',
  396: 'style',
  397: 'styleCount',
  398: 'styleIndex',
  399: 'styledText',
  400: 'styles',
  401: 'stylesSet',
  402: 'stylesSetCount',
  403: 'svgContent',
  404: 'swt',
  405: 'systemMenu',
  406: 'system_menu_bar',
  407: 'tabHeight',
  408: 'tabLength',
  409: 'tabPosition',
  410: 'tabStop',
  411: 'tabStops',
  412: 'tabWidth',
  413: 'tabs',
  414: 'text',
  415: 'textFonts',
  416: 'textLimit',
  417: 'textOrigins',
  418: 'textStrings',
  419: 'texts',
  420: 'theme_color',
  421: 'theme_colors_by_widget',
  422: 'theme_name',
  423: 'themedCanvas',
  424: 'thumb',
  425: 'time',
  426: 'toolTipText',
  427: 'tooltips',
  428: 'top',
  429: 'topIndex',
  430: 'topItem',
  431: 'topMargin',
  432: 'topPixel',
  433: 'topRight',
  434: 'topRightAlignment',
  435: 'touches',
  436: 'tracking',
  437: 'transform',
  438: 'transparent_menu_check_box',
  439: 'type',
  440: 'types',
  441: 'ui_zoom',
  442: 'underline',
  443: 'underlineColor',
  444: 'underlineStyle',
  445: 'unselectedCloseVisible',
  446: 'unselectedImageVisible',
  447: 'url',
  448: 'use_special_dropdown_button',
  449: 'use_swt_border',
  450: 'use_swt_colors',
  451: 'use_swt_font_colors',
  452: 'use_swt_font_colors_by_widget',
  453: 'use_swt_fonts',
  454: 'value',
  455: 'verdict',
  456: 'verticalAlignment',
  457: 'verticalBar',
  458: 'verticalIndent',
  459: 'verticalSpan',
  460: 'visible',
  461: 'wHint',
  462: 'warnings',
  463: 'weights',
  464: 'widget',
  465: 'width',
  466: 'widthHint',
  467: 'windowId',
  468: 'windowedShellIds',
  469: 'wordWrap',
  470: 'wrapIndent',
  471: 'wrapIndices',
  472: 'x',
  473: 'xDirection',
  474: 'y',
  475: 'yDirection',
  476: 'year',
  477: 'zoom',
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
