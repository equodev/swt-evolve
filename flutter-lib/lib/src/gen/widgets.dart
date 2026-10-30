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
  if (VRegistry.instance.isAwaiting(VRegistry.channelOf(child))) {
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

/// Property numbers, as wire-ids.txt gives them, for code that reads a frame by hand.
abstract final class WireKey {
  static const int k_Display_shells = 1;
  static const int k_Display_popups = 2;
  static const int k_Display_systemMenu = 3;
  static const int k_Display_tooltips = 4;
  static const int k_Display_activeShellId = 5;
  static const int k_Display_mainShellId = 6;
  static const int k_Display_windowedShellIds = 7;
  static const int k_Display_config = 8;
  static const int k_ConfigFlags_ctabfolder_visible_controls = 9;
  static const int k_ConfigFlags_ctabfolder_topright_auto_hide = 10;
  static const int k_ConfigFlags_image_disable_icons_replacement = 11;
  static const int k_ConfigFlags_assets_path = 12;
  static const int k_ConfigFlags_disable_evolve_icons = 13;
  static const int k_ConfigFlags_gc_icons_replacement = 14;
  static const int k_ConfigFlags_use_swt_colors = 15;
  static const int k_ConfigFlags_disable_swt_canvas_colors = 16;
  static const int k_ConfigFlags_disable_control_gc_overlay = 17;
  static const int k_ConfigFlags_use_swt_fonts = 18;
  static const int k_ConfigFlags_use_swt_font_colors = 19;
  static const int k_ConfigFlags_use_swt_font_colors_by_widget = 20;
  static const int k_ConfigFlags_use_special_dropdown_button = 21;
  static const int k_ConfigFlags_preserve_icon_colors = 22;
  static const int k_ConfigFlags_disable_hover_zoom = 23;
  static const int k_ConfigFlags_custom_tooltip = 24;
  static const int k_ConfigFlags_notification_popup = 25;
  static const int k_ConfigFlags_force_theme = 26;
  static const int k_ConfigFlags_theme_name = 27;
  static const int k_ConfigFlags_theme_color = 28;
  static const int k_ConfigFlags_theme_colors_by_widget = 29;
  static const int k_ConfigFlags_show_theme_color_palette = 30;
  static const int k_ConfigFlags_focus_indicators = 31;
  static const int k_ConfigFlags_show_scaling_control = 32;
  static const int k_ConfigFlags_ui_zoom = 33;
  static const int k_ConfigFlags_decorations_align = 34;
  static const int k_ConfigFlags_print_move = 35;
  static const int k_ConfigFlags_csd_placement = 36;
  static const int k_ConfigFlags_csd_os = 37;
  static const int k_ConfigFlags_csd_maximize = 38;
  static const int k_ConfigFlags_system_menu_bar = 39;
  static const int k_ConfigFlags_double_click_timeout_ms = 40;
  static const int k_ConfigFlags_csd_titlebar_color = 41;
  static const int k_ConfigFlags_font_point_scale = 42;
  static const int k_ConfigFlags_push_button_margin = 43;
  static const int k_GC_clippingText = 44;
  static const int k_Color_a = 45;
  static const int k_Color_b = 46;
  static const int k_Color_g = 47;
  static const int k_Color_r = 48;
  static const int k_Slider_increment = 50;
  static const int k_Slider_maximum = 51;
  static const int k_Slider_minimum = 52;
  static const int k_Slider_pageIncrement = 53;
  static const int k_Slider_selection = 54;
  static const int k_Slider_thumb = 55;
  static const int k_MenuItem_accelerator = 56;
  static const int k_MenuItem_enabled = 57;
  static const int k_MenuItem_menu = 58;
  static const int k_MenuItem_selection = 59;
  static const int k_MenuItem_toolTipText = 60;
  static const int k_Spinner_digits = 61;
  static const int k_Spinner_increment = 62;
  static const int k_Spinner_maximum = 63;
  static const int k_Spinner_minimum = 64;
  static const int k_Spinner_pageIncrement = 65;
  static const int k_Spinner_selection = 66;
  static const int k_Spinner_textLimit = 67;
  static const int k_Widget_style = 68;
  static const int k_Image_filename = 69;
  static const int k_Image_height = 70;
  static const int k_Image_imageData = 71;
  static const int k_Image_remoteRef = 72;
  static const int k_Image_svgContent = 73;
  static const int k_Image_width = 74;
  static const int k_ExpandBar_items = 76;
  static const int k_ExpandBar_spacing = 77;
  static const int k_Monitor_clientHeight = 78;
  static const int k_Monitor_clientWidth = 79;
  static const int k_Monitor_clientX = 80;
  static const int k_Monitor_clientY = 81;
  static const int k_Monitor_handle = 82;
  static const int k_Monitor_height = 83;
  static const int k_Monitor_width = 84;
  static const int k_Monitor_x = 85;
  static const int k_Monitor_y = 86;
  static const int k_Monitor_zoom = 87;
  static const int k_FormAttachment_alignment = 89;
  static const int k_FormAttachment_control = 90;
  static const int k_FormAttachment_denominator = 91;
  static const int k_FormAttachment_numerator = 92;
  static const int k_FormAttachment_offset = 93;
  static const int k_TreeItem_background = 95;
  static const int k_TreeItem_backgrounds = 96;
  static const int k_TreeItem_checked = 97;
  static const int k_TreeItem_expanded = 98;
  static const int k_TreeItem_font = 99;
  static const int k_TreeItem_fonts = 100;
  static const int k_TreeItem_foreground = 101;
  static const int k_TreeItem_foregrounds = 102;
  static const int k_TreeItem_grayed = 103;
  static const int k_TreeItem_images = 104;
  static const int k_TreeItem_itemCount = 105;
  static const int k_TreeItem_items = 106;
  static const int k_TreeItem_paintedTexts = 107;
  static const int k_TreeItem_texts = 108;
  static const int k_RGB_blue = 109;
  static const int k_RGB_green = 110;
  static const int k_RGB_red = 111;
  static const int k_TableItem_background = 113;
  static const int k_TableItem_backgrounds = 114;
  static const int k_TableItem_checked = 115;
  static const int k_TableItem_font = 116;
  static const int k_TableItem_fonts = 117;
  static const int k_TableItem_foreground = 118;
  static const int k_TableItem_foregrounds = 119;
  static const int k_TableItem_grayed = 120;
  static const int k_TableItem_images = 121;
  static const int k_TableItem_paintedTexts = 122;
  static const int k_TableItem_texts = 123;
  static const int k_PathData_points = 124;
  static const int k_PathData_types = 125;
  static const int k_GLData_accumAlphaSize = 127;
  static const int k_GLData_accumBlueSize = 128;
  static const int k_GLData_accumGreenSize = 129;
  static const int k_GLData_accumRedSize = 130;
  static const int k_GLData_alphaSize = 131;
  static const int k_GLData_blueSize = 132;
  static const int k_GLData_depthSize = 133;
  static const int k_GLData_doubleBuffer = 134;
  static const int k_GLData_greenSize = 135;
  static const int k_GLData_redSize = 136;
  static const int k_GLData_sampleBuffers = 137;
  static const int k_GLData_samples = 138;
  static const int k_GLData_stencilSize = 139;
  static const int k_GLData_stereo = 140;
  static const int k_ExpandItem_control = 142;
  static const int k_ExpandItem_expanded = 143;
  static const int k_ExpandItem_height = 144;
  static const int k_GC_XORMode = 145;
  static const int k_GC_alpha = 146;
  static const int k_GC_antialias = 147;
  static const int k_GC_background = 148;
  static const int k_GC_bufferScale = 149;
  static const int k_GC_clipping = 150;
  static const int k_GC_clippingPath = 151;
  static const int k_GC_clippingRects = 152;
  static const int k_GC_fillRule = 153;
  static const int k_GC_font = 154;
  static const int k_GC_foreground = 155;
  static const int k_GC_interpolation = 156;
  static const int k_GC_lineCap = 157;
  static const int k_GC_lineDash = 158;
  static const int k_GC_lineDashOffset = 159;
  static const int k_GC_lineJoin = 160;
  static const int k_GC_lineStyle = 161;
  static const int k_GC_lineWidth = 162;
  static const int k_GC_style = 163;
  static const int k_ToolTip_location = 164;
  static const int k_ToolTip_message = 165;
  static const int k_ToolTip_text = 166;
  static const int k_ToolTip_visible = 167;
  static const int k_Accessible_control = 168;
  static const int k_Accessible_focus = 169;
  static const int k_CoolItem_control = 171;
  static const int k_CoolItem_preferredSize = 172;
  static const int k_TableEditor_column = 173;
  static const int k_TableEditor_item = 174;
  static const int k_Group_text = 175;
  static const int k_TreeColumn_alignment = 176;
  static const int k_TreeColumn_moveable = 177;
  static const int k_TreeColumn_width = 178;
  static const int k_StyledText_alignment = 179;
  static const int k_StyledText_alwaysShowScrollBars = 180;
  static const int k_StyledText_blockSelection = 181;
  static const int k_StyledText_blockSelectionBounds = 182;
  static const int k_StyledText_bottomMargin = 183;
  static const int k_StyledText_caretOffset = 184;
  static const int k_StyledText_caretOffsets = 185;
  static const int k_StyledText_doubleClickEnabled = 186;
  static const int k_StyledText_editable = 187;
  static const int k_StyledText_horizontalPixel = 188;
  static const int k_StyledText_indent = 189;
  static const int k_StyledText_justify = 190;
  static const int k_StyledText_leftMargin = 191;
  static const int k_StyledText_lineSpacing = 192;
  static const int k_StyledText_marginColor = 193;
  static const int k_StyledText_renderer = 194;
  static const int k_StyledText_rightMargin = 195;
  static const int k_StyledText_selection = 196;
  static const int k_StyledText_selectionBackground = 197;
  static const int k_StyledText_selectionForeground = 198;
  static const int k_StyledText_selectionRanges = 199;
  static const int k_StyledText_styleIndex = 200;
  static const int k_StyledText_styles = 201;
  static const int k_StyledText_tabStops = 202;
  static const int k_StyledText_tabs = 203;
  static const int k_StyledText_text = 204;
  static const int k_StyledText_topMargin = 205;
  static const int k_StyledText_topPixel = 206;
  static const int k_StyledText_wordWrap = 207;
  static const int k_StyledText_wrapIndent = 208;
  static const int k_Point_x = 209;
  static const int k_Point_y = 210;
  static const int k_TableColumn_alignment = 212;
  static const int k_TableColumn_resizable = 213;
  static const int k_TableColumn_width = 214;
  static const int k_TouchSource_bounds = 215;
  static const int k_TouchSource_direct = 216;
  static const int k_TouchSource_handle = 217;
  static const int k_Menu_enabled = 219;
  static const int k_Menu_items = 220;
  static const int k_Menu_location = 221;
  static const int k_Menu_orientation = 222;
  static const int k_Menu_visible = 223;
  static const int k_FormData_bottom = 224;
  static const int k_FormData_cacheBottom = 225;
  static const int k_FormData_cacheHeight = 226;
  static const int k_FormData_cacheLeft = 227;
  static const int k_FormData_cacheRight = 228;
  static const int k_FormData_cacheTop = 229;
  static const int k_FormData_cacheWidth = 230;
  static const int k_FormData_currentHeight = 231;
  static const int k_FormData_currentHhint = 232;
  static const int k_FormData_currentWhint = 233;
  static const int k_FormData_currentWidth = 234;
  static const int k_FormData_defaultHeight = 235;
  static const int k_FormData_defaultHhint = 236;
  static const int k_FormData_defaultWhint = 237;
  static const int k_FormData_defaultWidth = 238;
  static const int k_FormData_height = 239;
  static const int k_FormData_isVisited = 240;
  static const int k_FormData_left = 241;
  static const int k_FormData_needed = 242;
  static const int k_FormData_right = 243;
  static const int k_FormData_top = 244;
  static const int k_FormData_width = 245;
  static const int k_CTabFolder_borderVisible = 248;
  static const int k_CTabFolder_chevronVisible = 249;
  static const int k_CTabFolder_highlight = 250;
  static const int k_CTabFolder_highlightEnabled = 251;
  static const int k_CTabFolder_items = 252;
  static const int k_CTabFolder_maximizeVisible = 253;
  static const int k_CTabFolder_maximized = 254;
  static const int k_CTabFolder_minimizeVisible = 255;
  static const int k_CTabFolder_minimized = 256;
  static const int k_CTabFolder_minimumCharacters = 257;
  static const int k_CTabFolder_selectedImageVisible = 258;
  static const int k_CTabFolder_selection = 259;
  static const int k_CTabFolder_selectionBackground = 260;
  static const int k_CTabFolder_selectionBarThickness = 261;
  static const int k_CTabFolder_selectionBgImage = 262;
  static const int k_CTabFolder_selectionForeground = 263;
  static const int k_CTabFolder_showChevron = 264;
  static const int k_CTabFolder_showListPopupSeq = 265;
  static const int k_CTabFolder_single = 266;
  static const int k_CTabFolder_tabPosition = 267;
  static const int k_CTabFolder_topRight = 268;
  static const int k_CTabFolder_topRightAlignment = 269;
  static const int k_CTabFolder_unselectedCloseVisible = 270;
  static const int k_CTabFolder_unselectedImageVisible = 271;
  static const int k_ControlEditor_editor = 272;
  static const int k_ScrolledComposite_alwaysShowScrollBars = 273;
  static const int k_ScrolledComposite_content = 274;
  static const int k_ScrolledComposite_expandHorizontal = 275;
  static const int k_ScrolledComposite_expandVertical = 276;
  static const int k_ScrolledComposite_minHeight = 277;
  static const int k_ScrolledComposite_minWidth = 278;
  static const int k_TabItem_control = 279;
  static const int k_TabItem_toolTipText = 280;
  static const int k_DateTime_day = 281;
  static const int k_DateTime_hours = 282;
  static const int k_DateTime_minutes = 283;
  static const int k_DateTime_month = 284;
  static const int k_DateTime_seconds = 285;
  static const int k_DateTime_year = 286;
  static const int k_Combo_items = 287;
  static const int k_Combo_listVisible = 288;
  static const int k_Combo_text = 289;
  static const int k_Control_background = 290;
  static const int k_Control_backgroundImage = 291;
  static const int k_Control_bounds = 292;
  static const int k_Control_cursor = 293;
  static const int k_Control_dragDetect = 294;
  static const int k_Control_dragSource = 295;
  static const int k_Control_dropTargetId = 296;
  static const int k_Control_enabled = 297;
  static const int k_Control_font = 298;
  static const int k_Control_foreground = 299;
  static const int k_Control_hasOwnBackground = 300;
  static const int k_Control_inheritsBackground = 301;
  static const int k_Control_menu = 302;
  static const int k_Control_toolTipText = 303;
  static const int k_Control_visible = 304;
  static const int k_Item_image = 305;
  static const int k_Item_text = 306;
  static const int k_Touch_id = 307;
  static const int k_Touch_primary = 308;
  static const int k_Touch_source = 309;
  static const int k_Touch_state = 310;
  static const int k_Touch_x = 311;
  static const int k_Touch_y = 312;
  static const int k_StyledTextPrintOptions_footer = 314;
  static const int k_StyledTextPrintOptions_header = 315;
  static const int k_StyledTextPrintOptions_jobName = 316;
  static const int k_StyledTextPrintOptions_lineLabels = 317;
  static const int k_StyledTextPrintOptions_printLineBackground = 318;
  static const int k_StyledTextPrintOptions_printLineNumbers = 319;
  static const int k_StyledTextPrintOptions_printTextBackground = 320;
  static const int k_StyledTextPrintOptions_printTextFontStyle = 321;
  static const int k_StyledTextPrintOptions_printTextForeground = 322;
  static const int k_ScrollBar_enabled = 324;
  static const int k_ScrollBar_increment = 325;
  static const int k_ScrollBar_maximum = 326;
  static const int k_ScrollBar_minimum = 327;
  static const int k_ScrollBar_selection = 328;
  static const int k_ScrollBar_thumb = 329;
  static const int k_ScrollBar_visible = 330;
  static const int k_Event_button = 331;
  static const int k_Event_character = 332;
  static const int k_Event_count = 333;
  static const int k_Event_currentDataTypeId = 334;
  static const int k_Event_detail = 335;
  static const int k_Event_doit = 336;
  static const int k_Event_dragImage = 337;
  static const int k_Event_end = 338;
  static const int k_Event_feedback = 339;
  static const int k_Event_gc = 340;
  static const int k_Event_height = 341;
  static const int k_Event_index = 342;
  static const int k_Event_item = 343;
  static const int k_Event_itemId = 344;
  static const int k_Event_keyCode = 345;
  static const int k_Event_keyLocation = 346;
  static const int k_Event_magnification = 347;
  static const int k_Event_operations = 348;
  static const int k_Event_rotation = 349;
  static const int k_Event_segments = 350;
  static const int k_Event_segmentsChars = 351;
  static const int k_Event_start = 352;
  static const int k_Event_stateMask = 353;
  static const int k_Event_text = 354;
  static const int k_Event_time = 355;
  static const int k_Event_touches = 356;
  static const int k_Event_type = 357;
  static const int k_Event_widget = 358;
  static const int k_Event_width = 359;
  static const int k_Event_x = 360;
  static const int k_Event_xDirection = 361;
  static const int k_Event_y = 362;
  static const int k_Event_yDirection = 363;
  static const int k_Caret_bounds = 365;
  static const int k_CoolBar_itemOrder = 366;
  static const int k_CoolBar_itemSizes = 367;
  static const int k_CoolBar_items = 368;
  static const int k_CoolBar_wrapIndices = 369;
  static const int k_ProgressBar_maximum = 371;
  static const int k_ProgressBar_minimum = 372;
  static const int k_ProgressBar_selection = 373;
  static const int k_GridData_cacheHeight = 374;
  static const int k_GridData_cacheWidth = 375;
  static const int k_GridData_currentHeight = 376;
  static const int k_GridData_currentHhint = 377;
  static const int k_GridData_currentWhint = 378;
  static const int k_GridData_currentWidth = 379;
  static const int k_GridData_defaultHeight = 380;
  static const int k_GridData_defaultHhint = 381;
  static const int k_GridData_defaultWhint = 382;
  static const int k_GridData_defaultWidth = 383;
  static const int k_GridData_exclude = 384;
  static const int k_GridData_grabExcessHorizontalSpace = 385;
  static const int k_GridData_grabExcessVerticalSpace = 386;
  static const int k_GridData_heightHint = 387;
  static const int k_GridData_horizontalAlignment = 388;
  static const int k_GridData_horizontalIndent = 389;
  static const int k_GridData_horizontalSpan = 390;
  static const int k_GridData_minimumHeight = 391;
  static const int k_GridData_minimumWidth = 392;
  static const int k_GridData_verticalAlignment = 393;
  static const int k_GridData_verticalIndent = 394;
  static const int k_GridData_verticalSpan = 395;
  static const int k_GridData_widthHint = 396;
  static const int k_RowData_exclude = 398;
  static const int k_RowData_height = 399;
  static const int k_RowData_width = 400;
  static const int k_Link_linkForeground = 402;
  static const int k_Link_text = 403;
  static const int k_Rectangle_height = 404;
  static const int k_Rectangle_width = 405;
  static const int k_Rectangle_x = 406;
  static const int k_Rectangle_y = 407;
  static const int k_FontData_height = 409;
  static const int k_FontData_name = 410;
  static const int k_FontData_style = 411;
  static const int k_CTabItem_control = 413;
  static const int k_CTabItem_font = 414;
  static const int k_CTabItem_foreground = 415;
  static const int k_CTabItem_selectionForeground = 416;
  static const int k_CTabItem_showClose = 417;
  static const int k_CTabItem_showing = 418;
  static const int k_CTabItem_toolTipText = 419;
  static const int k_Browser_functionNames = 420;
  static const int k_Browser_javascriptEnabled = 421;
  static const int k_Browser_text = 422;
  static const int k_Browser_url = 423;
  static const int k_Font_fontData = 424;
  static const int k_Canvas_caret = 426;
  static const int k_Composite_backgroundMode = 427;
  static const int k_Composite_children = 428;
  static const int k_TextStyle_background = 429;
  static const int k_TextStyle_borderColor = 430;
  static const int k_TextStyle_borderStyle = 431;
  static const int k_TextStyle_font = 432;
  static const int k_TextStyle_foreground = 433;
  static const int k_TextStyle_metrics = 434;
  static const int k_TextStyle_rise = 435;
  static const int k_TextStyle_strikeout = 436;
  static const int k_TextStyle_strikeoutColor = 437;
  static const int k_TextStyle_underline = 438;
  static const int k_TextStyle_underlineColor = 439;
  static const int k_TextStyle_underlineStyle = 440;
  static const int k_Scale_maximum = 442;
  static const int k_Scale_minimum = 443;
  static const int k_Scale_selection = 444;
  static const int k_ToolItem_background = 445;
  static const int k_ToolItem_control = 446;
  static const int k_ToolItem_disabledImage = 447;
  static const int k_ToolItem_enabled = 448;
  static const int k_ToolItem_foreground = 449;
  static const int k_ToolItem_hotImage = 450;
  static const int k_ToolItem_selection = 451;
  static const int k_ToolItem_toolTipText = 452;
  static const int k_ToolItem_width = 453;
  static const int k_Tree_columns = 454;
  static const int k_Tree_editors = 455;
  static const int k_Tree_headerBackground = 456;
  static const int k_Tree_headerVisible = 457;
  static const int k_Tree_items = 458;
  static const int k_Tree_linesVisible = 459;
  static const int k_Tree_selection = 460;
  static const int k_List_items = 461;
  static const int k_List_selection = 462;
  static const int k_Label_image = 463;
  static const int k_Label_text = 464;
  static const int k_Table_columns = 465;
  static const int k_Table_editors = 466;
  static const int k_Table_headerBackground = 467;
  static const int k_Table_headerForeground = 468;
  static const int k_Table_headerVisible = 469;
  static const int k_Table_itemCount = 470;
  static const int k_Table_items = 471;
  static const int k_Table_linesVisible = 472;
  static const int k_Table_selection = 473;
  static const int k_LineSizeInfo_height = 474;
  static const int k_LineSizeInfo_width = 475;
  static const int k_LineInfo_alignment = 477;
  static const int k_LineInfo_background = 478;
  static const int k_LineInfo_flags = 479;
  static const int k_LineInfo_indent = 480;
  static const int k_LineInfo_justify = 481;
  static const int k_LineInfo_segments = 482;
  static const int k_LineInfo_segmentsChars = 483;
  static const int k_LineInfo_tabStops = 484;
  static const int k_LineInfo_verticalIndent = 485;
  static const int k_LineInfo_wrapIndent = 486;
  static const int k_StyledTextRenderer_ascent = 488;
  static const int k_StyledTextRenderer_bulletTexts = 489;
  static const int k_StyledTextRenderer_descent = 490;
  static const int k_StyledTextRenderer_lineSpacings = 491;
  static const int k_StyledTextRenderer_lines = 492;
  static const int k_StyledTextRenderer_ranges = 493;
  static const int k_StyledTextRenderer_regularFont = 494;
  static const int k_StyledTextRenderer_styleCount = 495;
  static const int k_TabFolder_items = 497;
  static const int k_TabFolder_selection = 498;
  static const int k_Scrollable_horizontalBar = 499;
  static const int k_Scrollable_verticalBar = 500;
  static const int k_CCombo_alignment = 501;
  static const int k_CCombo_itemTooltips = 502;
  static const int k_CCombo_items = 503;
  static const int k_CCombo_listVisible = 504;
  static const int k_CCombo_selection = 505;
  static const int k_CCombo_text = 506;
  static const int k_CCombo_textLimit = 507;
  static const int k_BorderData_hHint = 508;
  static const int k_BorderData_region = 509;
  static const int k_BorderData_wHint = 510;
  static const int k_SashForm_maximizedControl = 512;
  static const int k_SashForm_sashWidth = 513;
  static const int k_SashForm_weights = 514;
  static const int k_Text_caretPosition = 515;
  static const int k_Text_echoCharacter = 516;
  static const int k_Text_editable = 517;
  static const int k_Text_message = 518;
  static const int k_Text_selection = 519;
  static const int k_Text_text = 520;
  static const int k_Text_textLimit = 521;
  static const int k_ToolBar_items = 522;
  static const int k_GlyphMetrics_ascent = 523;
  static const int k_GlyphMetrics_descent = 524;
  static const int k_GlyphMetrics_width = 525;
  static const int k_Button_alignment = 527;
  static const int k_Button_grayed = 528;
  static const int k_Button_image = 529;
  static const int k_Button_primary = 530;
  static const int k_Button_selection = 531;
  static const int k_Button_text = 532;
  static const int k_TreeEditor_column = 533;
  static const int k_TreeEditor_editor = 534;
  static const int k_TreeEditor_item = 535;
  static const int k_Cursor_cursorStyle = 536;
  static const int k_Cursor_hotspotX = 537;
  static const int k_Cursor_hotspotY = 538;
  static const int k_Cursor_image = 539;
  static const int k_CLabel_alignment = 541;
  static const int k_CLabel_backgroundImage = 542;
  static const int k_CLabel_bottomMargin = 543;
  static const int k_CLabel_image = 544;
  static const int k_CLabel_leftMargin = 545;
  static const int k_CLabel_rightMargin = 546;
  static const int k_CLabel_text = 547;
  static const int k_CLabel_topMargin = 548;
  static const int k_ImageData_data = 549;
  static const int k_ImageData_depth = 550;
  static const int k_ImageData_height = 551;
  static const int k_ImageData_width = 552;
  static const int k_StyleRange_fontStyle = 554;
  static const int k_StyleRange_length = 555;
  static const int k_StyleRange_start = 556;
  static const int k_Decorations_menuBar = 558;
  static const int k_Decorations_text = 559;
  static const int k_GC_backgroundPattern = 560;
  static const int k_GC_foregroundPattern = 561;
  static const int k_GC_transform = 562;
  static const int k_Transform_elements = 563;
  static const int k_TableEditor_editor = 564;
  static const int k_Path_pathData = 565;
  static const int k_Path_textStrings = 566;
  static const int k_Path_textOrigins = 567;
  static const int k_Path_textFonts = 568;
  static const int k_Control_region = 569;
  static const int k_Pattern_color1 = 570;
  static const int k_Pattern_color2 = 571;
  static const int k_Pattern_endX = 572;
  static const int k_Pattern_endY = 573;
  static const int k_Pattern_image = 574;
  static const int k_Pattern_startX = 575;
  static const int k_Pattern_startY = 576;
  static const int k_Shell_alpha = 577;
  static const int k_Shell_fullScreen = 578;
  static const int k_Shell_maximumSize = 579;
  static const int k_Shell_minimumSize = 580;
  static const int k_Shell_modified = 581;
  static const int k_Shell_dialogs = 582;
  static const int k_StyledTextRenderer_averageCharWidth = 583;
  static const int k_StyledTextRenderer_averageLineHeight = 584;
  static const int k_StyledTextRenderer_boldFont = 585;
  static const int k_StyledTextRenderer_boldItalicFont = 586;
  static const int k_StyledTextRenderer_bulletsIndices = 587;
  static const int k_StyledTextRenderer_fixedPitch = 588;
  static const int k_StyledTextRenderer_hasLinks = 589;
  static const int k_StyledTextRenderer_idleRunning = 590;
  static const int k_StyledTextRenderer_italicFont = 591;
  static const int k_StyledTextRenderer_lineCount = 592;
  static const int k_StyledTextRenderer_lineSizes = 593;
  static const int k_StyledTextRenderer_lineSpacingComputing = 594;
  static const int k_StyledTextRenderer_linesInAverageLineHeight = 595;
  static const int k_StyledTextRenderer_maxWidth = 596;
  static const int k_StyledTextRenderer_maxWidthLineIndex = 597;
  static const int k_StyledTextRenderer_redrawLines = 598;
  static const int k_StyledTextRenderer_styledText = 599;
  static const int k_StyledTextRenderer_styles = 600;
  static const int k_StyledTextRenderer_stylesSet = 601;
  static const int k_StyledTextRenderer_stylesSetCount = 602;
  static const int k_StyledTextRenderer_tabLength = 603;
  static const int k_StyledTextRenderer_tabWidth = 604;
  static const int k_StyledTextRenderer_topIndex = 605;
  static const int k_FileDialog_fileName = 606;
  static const int k_FileDialog_fileNames = 607;
  static const int k_FileDialog_filterExtensions = 608;
  static const int k_FileDialog_filterIndex = 609;
  static const int k_FileDialog_filterNames = 610;
  static const int k_FileDialog_filterPath = 611;
  static const int k_FileDialog_overwrite = 612;
  static const int k_Region_rects = 616;
  static const int k_PDFDocument_height = 618;
  static const int k_PDFDocument_width = 619;
  static const int k_GLData_shareContext = 622;
  static const int k_ColorDialog_RGB = 623;
  static const int k_ColorDialog_RGBs = 624;
  static const int k_Device_fontList = 629;
  static const int k_Device_tracking = 630;
  static const int k_Device_warnings = 631;
  static const int k_BrowserFunction_browser = 635;
  static const int k_BrowserFunction_name = 636;
  static const int k_PrintDialog_endPage = 639;
  static const int k_PrintDialog_printToFile = 640;
  static const int k_PrintDialog_scope = 641;
  static const int k_PrintDialog_startPage = 642;
  static const int k_MessageBox_message = 645;
  static const int k_Dialog_id = 647;
  static const int k_Dialog_style = 648;
  static const int k_Dialog_swt = 649;
  static const int k_Dialog_text = 650;
  static const int k_PopupList_font = 653;
  static const int k_PopupList_items = 654;
  static const int k_PopupList_minimumWidth = 655;
  static const int k_PopupList_selection = 656;
  static const int k_DirectoryDialog_filterPath = 659;
  static const int k_DirectoryDialog_message = 660;
  static const int k_Clipboard_availableTypeNames = 666;
  static const int k_FontDialog_RGB = 668;
  static const int k_FontDialog_effectsVisible = 669;
  static const int k_FontDialog_fontData = 670;
  static const int k_FontDialog_fontList = 671;
  static const int k_Table_topIndex = 1353;
  static const int k_Text_edits = 1354;
  static const int k_GC_themedCanvas = 1356;
  static const int k_CLabel_gradientColors = 1357;
  static const int k_CLabel_gradientPercents = 1358;
  static const int k_CLabel_gradientVertical = 1359;
  static const int k_StyledText_fixedLineHeight = 1364;
  static const int k_Decorations_menuStripHeight = 1366;
  static const int k_Decorations_ownMenuStrip = 1367;
  static const int k_ConfigFlags_hover_delay_ms = 1370;
  static const int k_GC_hoverFills = 1372;
  static const int k_GC_selectionFills = 1373;
  static const int k_CTabFolder_tabHeight = 1374;
  static const int k_Spinner_keptText = 1378;
  static const int k_TreeColumn_resizable = 1379;
  static const int k_TableColumn_moveable = 1380;
  static const int k_ScrolledComposite_origin = 1381;
  static const int k_ScrolledComposite_originRequest = 1382;
  static const int k_Tree_columnOrder = 1383;
  static const int k_Tree_headerForeground = 1384;
  static const int k_Tree_sortColumn = 1385;
  static const int k_Tree_sortDirection = 1386;
  static const int k_Tree_topItem = 1387;
  static const int k_Table_columnOrder = 1388;
  static const int k_Display_focusHolderId = 1396;
  static const int k_ConfigFlags_ctabfolder_tab_scroll = 1398;
  static const int k_ConfigFlags_decimal_separator = 1399;
  static const int k_ConfigFlags_locale = 1400;
  static const int k_ConfigFlags_transparent_menu_check_box = 1401;
  static const int k_ConfigFlags_use_swt_border = 1408;
  static const int k_Combo_caretPosition = 1410;
  static const int k_ExpandItem_headerHeight = 1416;
  static const int k_Caret_blinkRate = 1417;
  static const int k_Caret_focused = 1418;
  static const int k_Caret_image = 1419;
  static const int k_Caret_visible = 1420;
  static const int k_Tree_ownerDrawItemHeight = 1421;
  static const int k_List_topIndex = 1422;
  static const int k_Table_ownerDrawItemHeight = 1423;
  static const int k_MessageBox_buttonLabels = 1428;
}

/// The properties whose values name widgets.
const Set<int> wireRefKeys = {
  58,
  76,
  90,
  106,
  142,
  168,
  171,
  174,
  220,
  252,
  268,
  272,
  274,
  279,
  302,
  340,
  343,
  358,
  368,
  413,
  426,
  428,
  446,
  454,
  455,
  458,
  460,
  465,
  466,
  471,
  497,
  498,
  499,
  500,
  512,
  522,
  534,
  535,
  1385,
  1387,
};

/// Type names by the number a frame names a value's class with.
const Map<int, String> wireTypeNames = {
  49: 'Color',
  75: 'Image',
  88: 'Monitor',
  94: 'FormAttachment',
  112: 'RGB',
  126: 'PathData',
  141: 'GLData',
  170: 'Accessible',
  211: 'Point',
  218: 'TouchSource',
  246: 'FormData',
  247: 'FontMetrics',
  313: 'Touch',
  323: 'StyledTextPrintOptions',
  364: 'Event',
  370: 'Resource',
  397: 'GridData',
  401: 'RowData',
  408: 'Rectangle',
  412: 'FontData',
  425: 'Font',
  441: 'TextStyle',
  476: 'LineSizeInfo',
  487: 'LineInfo',
  496: 'StyledTextRenderer',
  511: 'BorderData',
  526: 'GlyphMetrics',
  540: 'Cursor',
  553: 'ImageData',
  557: 'StyleRange',
  613: 'FileDialog',
  614: 'FileTransfer',
  615: 'ImageTransfer',
  617: 'Region',
  620: 'PDFDocument',
  621: 'RTFTransfer',
  625: 'ColorDialog',
  626: 'Transform',
  627: 'Transfer',
  628: 'Program',
  632: 'Device',
  633: 'Path',
  634: 'TransferData',
  637: 'BrowserFunction',
  638: 'URLTransfer',
  643: 'PrintDialog',
  644: 'HTMLTransfer',
  646: 'MessageBox',
  651: 'Dialog',
  652: 'GCData',
  657: 'PopupList',
  658: 'BusyIndicator',
  661: 'DirectoryDialog',
  662: 'TextTransfer',
  663: 'Pattern',
  664: 'ByteArrayTransfer',
  665: 'Printer',
  667: 'Clipboard',
  672: 'FontDialog',
  673: 'Shell',
  674: 'AnimatedProgress',
  675: 'CLabel',
  676: 'Decorations',
  677: 'GLCanvas',
  678: 'StyledText',
  679: 'TableCursor',
  680: 'TreeCursor',
  681: 'Browser',
  682: 'CBanner',
  683: 'CCombo',
  684: 'CTabFolder',
  685: 'Canvas',
  686: 'Combo',
  687: 'CoolBar',
  688: 'DateTime',
  689: 'ExpandBar',
  690: 'Group',
  691: 'SashForm',
  692: 'ScrolledComposite',
  693: 'Spinner',
  694: 'TabFolder',
  695: 'Table',
  696: 'ToolBar',
  697: 'Tree',
  698: 'ViewForm',
  699: 'Composite',
  700: 'List',
  701: 'Text',
  702: 'Button',
  703: 'Label',
  704: 'Link',
  705: 'ProgressBar',
  706: 'Sash',
  707: 'Scale',
  708: 'Slider',
  709: 'CTabItem',
  710: 'CoolItem',
  711: 'ExpandItem',
  712: 'MenuItem',
  713: 'TabItem',
  714: 'TableColumn',
  715: 'TableItem',
  716: 'TaskItem',
  717: 'ToolItem',
  718: 'TrayItem',
  719: 'TreeColumn',
  720: 'TreeItem',
  721: 'Caret',
  722: 'DragSource',
  723: 'DropTarget',
  724: 'GC',
  725: 'IME',
  726: 'Menu',
  727: 'ScrollBar',
  728: 'TableEditor',
  729: 'TaskBar',
  730: 'ToolTip',
  731: 'Tracker',
  732: 'Tray',
  733: 'TreeEditor',
  734: 'ControlEditor',
  735: 'MainComposite',
  736: 'EwtWidget',
  737: 'SwingIsland',
  738: 'SideBar',
  739: 'MainToolbar',
  740: 'StatusBar',
  741: 'Display',
  742: 'ShellPopups',
};

/// Channel names by the number a message names its channel with: a global name, or what follows
/// a widget's type and id.
const Map<int, String> wireChannelNames = {
  743: 'Selection/DefaultSelection',
  744: 'Selection/Selection',
  745: 'Arm/Arm',
  746: 'Help/Help',
  747: 'Modify/Modify',
  748: 'Verify/Verify',
  749: 'Dispose/Dispose',
  750: 'Expand/Collapse',
  751: 'Expand/Expand',
  752: 'Control/Move',
  753: 'Control/Resize',
  754: 'BidiSegment/lineGetSegments',
  755: 'Caret/caretMoved',
  756: 'ExtendedModify/modifyText',
  757: 'LineBackground/lineGetBackground',
  758: 'LineStyle/lineGetStyle',
  759: 'PaintObject/paintObject',
  760: 'VerifyKey/verifyKey',
  761: 'WordMovement/getNextOffset',
  762: 'WordMovement/getPreviousOffset',
  763: 'Menu/Hide',
  764: 'Menu/Show',
  765: 'CTabFolder/itemClosed',
  766: 'CTabFolder/reorderItems',
  767: 'CTabFolder/stripLaidOut',
  768: 'CTabFolder2/close',
  769: 'CTabFolder2/maximize',
  770: 'CTabFolder2/minimize',
  771: 'CTabFolder2/restore',
  772: 'CTabFolder2/showList',
  773: 'Segment/Segments',
  774: 'DragDetect/DragDetect',
  775: 'Focus/FocusIn',
  776: 'Focus/FocusOut',
  777: 'Gesture/Gesture',
  778: 'Key/KeyDown',
  779: 'Key/KeyUp',
  780: 'MenuDetect/MenuDetect',
  781: 'Mouse/MouseDoubleClick',
  782: 'Mouse/MouseDown',
  783: 'Mouse/MouseUp',
  784: 'MouseMove/MouseMove',
  785: 'MouseTrack/MouseEnter',
  786: 'MouseTrack/MouseExit',
  787: 'MouseTrack/MouseHover',
  788: 'MouseWheel/MouseWheel',
  789: 'Paint/Paint',
  790: 'Touch/Touch',
  791: 'Traverse/Traverse',
  792: 'Drag/dragFinished',
  793: 'Drag/dragSetData',
  794: 'Drag/dragStart',
  795: 'Authentication/authenticate',
  796: 'CloseWindow/close',
  797: 'Location/changed',
  798: 'Location/changing',
  799: 'OpenWindow/open',
  800: 'Progress/changed',
  801: 'Progress/completed',
  802: 'StatusText/changed',
  803: 'Title/changed',
  804: 'VisibilityWindow/hide',
  805: 'VisibilityWindow/show',
  806: 'Selection/OpenMenu',
  807: 'SetData/SetData',
  808: 'Tree/Collapse',
  809: 'Tree/Expand',
  810: 'Drop/dragEnter',
  811: 'Drop/dragLeave',
  812: 'Drop/dragOperationChanged',
  813: 'Drop/dragOver',
  814: 'Drop/drop',
  815: 'Drop/dropAccept',
  816: 'swt.evolve.wire',
  817: 'swt.evolve.batch',
  818: 'swt.evolve.properties',
  819: 'swt.evolve.property.set',
  820: 'swt.evolve.focus',
  821: 'swt.evolve.url.open',
  822: 'swt.evolve.widget.refresh',
  823: 'swt.evolve.timing.ping',
  824: 'swt.evolve.timing.pong',
  825: 'swt.evolve.timing.bare',
  826: 'GC/create',
  827: 'GC/inFrame',
  828: 'Image/releaseRemoteRef',
  829: 'Image/requestPixels',
  830: 'Clipboard/read',
  831: 'Clipboard/write',
  832: 'Clipboard/contents',
  833: '',
  834: 'b',
  835: 'c',
  836: 'v',
  837: 'nextFrame',
  838: 'gcDispose',
  839: 'imageInit',
  840: 'imageResult',
  841: 'imageSnapshotResult',
  842: 'pixelsResult',
  843: 'renderSnapshot',
  844: 'copyAreaImageintintResponse',
  845: 'ClientReady',
  846: 'TextChange',
  847: 'TextGeometry',
  848: 'ImeComposition',
  849: 'GetIdFromPoint',
  850: 'GetIdFromPointResponse',
  851: 'GetItemBounds',
  852: 'GetItemBoundsResponse',
  853: 'GetItemHeight',
  854: 'GetItemHeightResponse',
  855: 'activation',
  856: 'browserFunctionCall',
  857: 'navigateRequest',
  858: 'navigationState',
  859: 'Popups',
  860: 'WinUnload',
  861: 'close',
  862: 'key/vetoable',
  863: 'modify/vetoable',
  864: 'traverse/vetoable',
  865: 'copyAreaImageintint',
  866: 'copyAreaintintintintintintboolean',
  867: 'drawArcintintintintintint',
  868: 'drawFocusintintintint',
  869: 'drawImageImageintint',
  870: 'drawImageImageintintintint',
  871: 'drawImageImageintintintintintintintint',
  872: 'drawLineintintintint',
  873: 'drawOvalintintintint',
  874: 'drawPathPath',
  875: 'drawPointintint',
  876: 'drawPolygonint',
  877: 'drawPolylineint',
  878: 'drawRectangleintintintint',
  879: 'drawRoundRectangleintintintintintint',
  880: 'drawTextStringintintint',
  881: 'fillArcintintintintintint',
  882: 'fillGradientRectangleintintintintboolean',
  883: 'fillOvalintintintint',
  884: 'fillPathPath',
  885: 'fillPolygonint',
  886: 'fillRectangleintintintint',
  887: 'fillRoundRectangleintintintintintint',
  888: 'Shell/Activate',
  889: 'Shell/Close',
  890: 'Shell/Deactivate',
  891: 'Shell/Deiconify',
  892: 'Shell/Iconify',
  893: 'Shell/SetBounds',
  894: 'key/verdict',
  895: 'modify/verdict',
  896: 'traverse/verdict',
  897: 'menu/verdict',
  898: 'navigate',
  899: 'locationchanging/result',
  900: 'evaluate',
  901: 'setCookie',
  902: 'getCookie',
  903: 'execute',
  904: 'back',
  905: 'forward',
  906: 'reload',
  907: 'stop',
  908: 'Drop/negotiationResult',
  909: 'DragDetect/dragStartResult',
  1345: 'evaluate/result',
  1346: 'copyAreaintintintintintint',
  1347: 'drawRectangleRectangle',
  1348: 'drawStringStringintint',
  1349: 'drawStringStringintintboolean',
  1350: 'drawTextStringintint',
  1351: 'drawTextStringintintboolean',
  1352: 'fillRectangleRectangle',
  1411: 'swt.evolve.focus.seen',
  1412: 'swt.evolve.focusHolder',
  1413: 'swt.evolve.test.runUi',
  1414: 'swt.evolve.test.runUiResponse',
  1415: 'Image/forget',
};

/// Map keys by the number a map sent as tagged values names them with.
const Map<int, String> wireMapKeyNames = {
  910: 'RGB',
  911: 'RGBs',
  912: 'XORMode',
  913: '_d',
  914: '_gd',
  915: '_gr',
  916: '_r',
  917: '_s',
  918: 'a',
  919: 'accelerator',
  920: 'accumAlphaSize',
  921: 'accumBlueSize',
  922: 'accumGreenSize',
  923: 'accumRedSize',
  924: 'activeShellId',
  925: 'alignment',
  926: 'alpha',
  927: 'alphaSize',
  928: 'alwaysShowScrollBars',
  929: 'antialias',
  930: 'args',
  931: 'ascent',
  932: 'assets_path',
  933: 'availableTypeNames',
  934: 'averageCharWidth',
  935: 'averageLineHeight',
  936: 'b',
  937: 'background',
  938: 'backgroundImage',
  939: 'backgroundMode',
  940: 'backgroundPattern',
  941: 'backgrounds',
  942: 'blockSelection',
  943: 'blockSelectionBounds',
  944: 'blue',
  945: 'blueSize',
  946: 'boldFont',
  947: 'boldItalicFont',
  948: 'borderColor',
  949: 'borderStyle',
  950: 'borderVisible',
  951: 'bottom',
  952: 'bottomMargin',
  953: 'bounds',
  954: 'browser',
  955: 'bufferScale',
  956: 'bulletTexts',
  957: 'bulletsIndices',
  958: 'button',
  959: 'cacheBottom',
  960: 'cacheHeight',
  961: 'cacheLeft',
  962: 'cacheRight',
  963: 'cacheTop',
  964: 'cacheWidth',
  965: 'caret',
  966: 'caretOffset',
  967: 'caretOffsets',
  968: 'caretPosition',
  969: 'charCount',
  970: 'character',
  971: 'checked',
  972: 'chevronVisible',
  973: 'children',
  974: 'clientHeight',
  975: 'clientWidth',
  976: 'clientX',
  977: 'clientY',
  978: 'clipping',
  979: 'clippingPath',
  980: 'clippingRects',
  981: 'clippingText',
  982: 'color1',
  983: 'color2',
  984: 'column',
  985: 'columns',
  986: 'config',
  987: 'content',
  988: 'control',
  989: 'cookie',
  990: 'count',
  991: 'csd_maximize',
  992: 'csd_os',
  993: 'csd_placement',
  994: 'csd_titlebar_color',
  995: 'ctabfolder_topright_auto_hide',
  996: 'ctabfolder_visible_controls',
  997: 'currentDataTypeId',
  998: 'currentHeight',
  999: 'currentHhint',
  1000: 'currentWhint',
  1001: 'currentWidth',
  1002: 'cursor',
  1003: 'cursorStyle',
  1004: 'custom_tooltip',
  1005: 'damage',
  1006: 'data',
  1007: 'day',
  1008: 'decorations_align',
  1009: 'defaultHeight',
  1010: 'defaultHhint',
  1011: 'defaultWhint',
  1012: 'defaultWidth',
  1013: 'denominator',
  1014: 'depth',
  1015: 'depthSize',
  1016: 'descent',
  1017: 'detail',
  1018: 'dialogs',
  1019: 'digits',
  1020: 'direct',
  1021: 'disable_control_gc_overlay',
  1022: 'disable_evolve_icons',
  1023: 'disable_hover_zoom',
  1024: 'disable_swt_canvas_colors',
  1025: 'disabledImage',
  1026: 'doit',
  1027: 'doubleBuffer',
  1028: 'doubleClickEnabled',
  1029: 'double_click_timeout_ms',
  1030: 'dragDetect',
  1031: 'dragImage',
  1032: 'dragSource',
  1033: 'dropTargetId',
  1034: 'echoCharacter',
  1035: 'editable',
  1036: 'editor',
  1037: 'editors',
  1038: 'effectsVisible',
  1039: 'elements',
  1040: 'enabled',
  1041: 'end',
  1042: 'endPage',
  1043: 'endX',
  1044: 'endY',
  1045: 'error',
  1046: 'exclude',
  1047: 'expandHorizontal',
  1048: 'expandVertical',
  1049: 'expanded',
  1050: 'expires',
  1051: 'feedback',
  1052: 'fileName',
  1053: 'fileNames',
  1054: 'filename',
  1055: 'fillRule',
  1056: 'filterExtensions',
  1057: 'filterIndex',
  1058: 'filterNames',
  1059: 'filterPath',
  1060: 'fixedPitch',
  1061: 'flags',
  1062: 'focus',
  1063: 'focus_indicators',
  1064: 'font',
  1065: 'fontData',
  1066: 'fontList',
  1067: 'fontStyle',
  1068: 'font_point_scale',
  1069: 'fonts',
  1070: 'footer',
  1071: 'force_theme',
  1072: 'foreground',
  1073: 'foregroundPattern',
  1074: 'foregrounds',
  1075: 'fullRepaint',
  1076: 'fullScreen',
  1077: 'functionNames',
  1078: 'g',
  1079: 'gc',
  1080: 'gc_icons_replacement',
  1081: 'grabExcessHorizontalSpace',
  1082: 'grabExcessVerticalSpace',
  1083: 'grayed',
  1084: 'green',
  1085: 'greenSize',
  1086: 'hHint',
  1087: 'handle',
  1088: 'hasLinks',
  1089: 'hasOwnBackground',
  1090: 'header',
  1091: 'headerBackground',
  1092: 'headerForeground',
  1093: 'headerVisible',
  1094: 'headers',
  1095: 'height',
  1096: 'heightHint',
  1097: 'highlight',
  1098: 'highlightEnabled',
  1099: 'horizontalAlignment',
  1100: 'horizontalBar',
  1101: 'horizontalIndent',
  1102: 'horizontalPixel',
  1103: 'horizontalSpan',
  1104: 'hotImage',
  1105: 'hotspotX',
  1106: 'hotspotY',
  1107: 'hours',
  1108: 'id',
  1109: 'idleRunning',
  1110: 'image',
  1111: 'imageData',
  1112: 'image_disable_icons_replacement',
  1113: 'images',
  1114: 'increment',
  1115: 'indent',
  1116: 'index',
  1117: 'inheritsBackground',
  1118: 'interpolation',
  1119: 'isVisited',
  1120: 'italicFont',
  1121: 'item',
  1122: 'itemCount',
  1123: 'itemId',
  1124: 'itemOrder',
  1125: 'itemSizes',
  1126: 'itemTooltips',
  1127: 'items',
  1128: 'javascriptEnabled',
  1129: 'jobName',
  1130: 'justify',
  1131: 'keyCode',
  1132: 'keyLocation',
  1133: 'left',
  1134: 'leftMargin',
  1135: 'length',
  1136: 'lineCap',
  1137: 'lineCount',
  1138: 'lineDash',
  1139: 'lineDashOffset',
  1140: 'lineJoin',
  1141: 'lineLabels',
  1142: 'lineSizes',
  1143: 'lineSpacing',
  1144: 'lineSpacingComputing',
  1145: 'lineSpacings',
  1146: 'lineStyle',
  1147: 'lineWidth',
  1148: 'lines',
  1149: 'linesInAverageLineHeight',
  1150: 'linesVisible',
  1151: 'linkForeground',
  1152: 'listVisible',
  1153: 'localFileBase',
  1154: 'localFilePath',
  1155: 'localFileText',
  1156: 'location',
  1157: 'magnification',
  1158: 'mainShellId',
  1159: 'map',
  1160: 'marginColor',
  1161: 'maxWidth',
  1162: 'maxWidthLineIndex',
  1163: 'maximizeVisible',
  1164: 'maximized',
  1165: 'maximizedControl',
  1166: 'maximum',
  1167: 'maximumSize',
  1168: 'menu',
  1169: 'menuBar',
  1170: 'message',
  1171: 'metrics',
  1172: 'minHeight',
  1173: 'minWidth',
  1174: 'minimizeVisible',
  1175: 'minimized',
  1176: 'minimum',
  1177: 'minimumCharacters',
  1178: 'minimumHeight',
  1179: 'minimumSize',
  1180: 'minimumWidth',
  1181: 'minutes',
  1182: 'modified',
  1183: 'month',
  1184: 'moveable',
  1185: 'name',
  1186: 'needed',
  1187: 'notification_popup',
  1188: 'numerator',
  1189: 'offset',
  1190: 'operations',
  1191: 'orientation',
  1192: 'overwrite',
  1193: 'pageIncrement',
  1194: 'paintedTexts',
  1195: 'pathData',
  1196: 'pixels',
  1197: 'points',
  1198: 'popups',
  1199: 'port',
  1200: 'postData',
  1201: 'preferredSize',
  1202: 'preserve_icon_colors',
  1203: 'primary',
  1204: 'printLineBackground',
  1205: 'printLineNumbers',
  1206: 'printTextBackground',
  1207: 'printTextFontStyle',
  1208: 'printTextForeground',
  1209: 'printToFile',
  1210: 'print_move',
  1211: 'push_button_margin',
  1212: 'r',
  1213: 'ranges',
  1214: 'rects',
  1215: 'red',
  1216: 'redSize',
  1217: 'redrawLines',
  1218: 'ref',
  1219: 'region',
  1220: 'regularFont',
  1221: 'remoteRef',
  1222: 'renderer',
  1223: 'replaced',
  1224: 'reqId',
  1225: 'resizable',
  1226: 'result',
  1227: 'retain',
  1228: 'right',
  1229: 'rightMargin',
  1230: 'rise',
  1231: 'rotation',
  1232: 'sampleBuffers',
  1233: 'samples',
  1234: 'sashWidth',
  1235: 'scope',
  1236: 'script',
  1237: 'seconds',
  1238: 'segments',
  1239: 'segmentsChars',
  1240: 'selectedImageVisible',
  1241: 'selection',
  1242: 'selectionBackground',
  1243: 'selectionBarThickness',
  1244: 'selectionBgImage',
  1245: 'selectionForeground',
  1246: 'selectionRanges',
  1247: 'seq',
  1248: 'shareContext',
  1249: 'shells',
  1250: 'showChevron',
  1251: 'showClose',
  1252: 'showListPopupSeq',
  1253: 'show_scaling_control',
  1254: 'show_theme_color_palette',
  1255: 'showing',
  1256: 'single',
  1257: 'source',
  1258: 'spacing',
  1259: 'start',
  1260: 'startPage',
  1261: 'startX',
  1262: 'startY',
  1263: 'state',
  1264: 'stateMask',
  1265: 'stencilSize',
  1266: 'stereo',
  1267: 'strikeout',
  1268: 'strikeoutColor',
  1269: 'style',
  1270: 'styleCount',
  1271: 'styleIndex',
  1272: 'styledText',
  1273: 'styles',
  1274: 'stylesSet',
  1275: 'stylesSetCount',
  1276: 'svgContent',
  1277: 'swt',
  1278: 'systemMenu',
  1279: 'system_menu_bar',
  1280: 'tabLength',
  1281: 'tabPosition',
  1282: 'tabStops',
  1283: 'tabWidth',
  1284: 'tabs',
  1285: 'text',
  1286: 'textFonts',
  1287: 'textLimit',
  1288: 'textOrigins',
  1289: 'textStrings',
  1290: 'texts',
  1291: 'theme_color',
  1292: 'theme_colors_by_widget',
  1293: 'theme_name',
  1294: 'thumb',
  1295: 'time',
  1296: 'toolTipText',
  1297: 'tooltips',
  1298: 'top',
  1299: 'topIndex',
  1300: 'topMargin',
  1301: 'topPixel',
  1302: 'topRight',
  1303: 'topRightAlignment',
  1304: 'touches',
  1305: 'tracking',
  1306: 'transform',
  1307: 'type',
  1308: 'types',
  1309: 'ui_zoom',
  1310: 'underline',
  1311: 'underlineColor',
  1312: 'underlineStyle',
  1313: 'unselectedCloseVisible',
  1314: 'unselectedImageVisible',
  1315: 'url',
  1316: 'use_special_dropdown_button',
  1317: 'use_swt_colors',
  1318: 'use_swt_font_colors',
  1319: 'use_swt_font_colors_by_widget',
  1320: 'use_swt_fonts',
  1321: 'value',
  1322: 'verdict',
  1323: 'verticalAlignment',
  1324: 'verticalBar',
  1325: 'verticalIndent',
  1326: 'verticalSpan',
  1327: 'visible',
  1328: 'wHint',
  1329: 'warnings',
  1330: 'weights',
  1331: 'widget',
  1332: 'width',
  1333: 'widthHint',
  1334: 'windowId',
  1335: 'windowedShellIds',
  1336: 'wordWrap',
  1337: 'wrapIndent',
  1338: 'wrapIndices',
  1339: 'x',
  1340: 'xDirection',
  1341: 'y',
  1342: 'yDirection',
  1343: 'year',
  1344: 'zoom',
  1355: 'edits',
  1360: 'gradientColors',
  1361: 'gradientPercents',
  1362: 'gradientVertical',
  1363: 'themedCanvas',
  1365: 'fixedLineHeight',
  1368: 'menuStripHeight',
  1369: 'ownMenuStrip',
  1371: 'hover_delay_ms',
  1375: 'hoverFills',
  1376: 'selectionFills',
  1377: 'tabHeight',
  1389: 'columnOrder',
  1390: 'keptText',
  1391: 'origin',
  1392: 'originRequest',
  1393: 'sortColumn',
  1394: 'sortDirection',
  1395: 'topItem',
  1397: 'focusHolderId',
  1402: 'ctabfolder_tab_scroll',
  1403: 'decimal_separator',
  1404: 'locale',
  1405: 'transparent_menu_check_box',
  1409: 'use_swt_border',
  1424: 'blinkRate',
  1425: 'focused',
  1426: 'headerHeight',
  1427: 'ownerDrawItemHeight',
  1429: 'buttonLabels',
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
