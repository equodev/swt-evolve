package dev.equo.swt;

/**
 * The numbers the binary wire names things by, generated from the client's wire model: channels,
 * record types, map keys and properties, each by number, and the numbers code written by hand names.
 */
public final class WireTable {

    private WireTable() {
    }

    /** The table's fingerprint, which the client compares with its own when it connects. */
    public static final int FINGERPRINT = 1986568125;

    /** Channel names by number: a global name, or what follows a widget's type and id. */
    public static final String[] CHANNELS = {
            null,
            "",
            "Arm/Arm",
            "Authentication/authenticate",
            "BidiSegment/lineGetSegments",
            "CTabFolder/itemClosed",
            "CTabFolder/reorderItems",
            "CTabFolder/stripLaidOut",
            "CTabFolder2/close",
            "CTabFolder2/maximize",
            "CTabFolder2/minimize",
            "CTabFolder2/restore",
            "CTabFolder2/showList",
            "Caret/caretMoved",
            "ClientReady",
            "Clipboard/contents",
            "Clipboard/read",
            "Clipboard/write",
            "CloseWindow",
            "CloseWindow/close",
            "Control/Move",
            "Control/Resize",
            "Dispose/Dispose",
            "Drag/dragFinished",
            "Drag/dragSetData",
            "Drag/dragStart",
            "DragDetect/DragDetect",
            "DragDetect/dragStartResult",
            "Drop/dragEnter",
            "Drop/dragLeave",
            "Drop/dragOperationChanged",
            "Drop/dragOver",
            "Drop/drop",
            "Drop/dropAccept",
            "Drop/negotiationResult",
            "Expand/Collapse",
            "Expand/Expand",
            "ExtendedModify/modifyText",
            "Focus/FocusIn",
            "Focus/FocusOut",
            "GC/create",
            "GC/inFrame",
            "Gesture/Gesture",
            "GetIdFromPoint",
            "GetIdFromPointResponse",
            "GetItemBounds",
            "GetItemBoundsResponse",
            "GetItemHeight",
            "GetItemHeightResponse",
            "Help/Help",
            "Image/forget",
            "Image/releaseRemoteRef",
            "Image/requestPixels",
            "ImeComposition",
            "Key/KeyDown",
            "Key/KeyUp",
            "LineBackground/lineGetBackground",
            "LineStyle/lineGetStyle",
            "List/Visible",
            "Location/changed",
            "Location/changing",
            "Menu/Hide",
            "Menu/Show",
            "MenuDetect/MenuDetect",
            "MenuKey",
            "Modify/Modify",
            "Mouse/MouseDoubleClick",
            "Mouse/MouseDown",
            "Mouse/MouseUp",
            "MouseMove/MouseMove",
            "MouseTrack/MouseEnter",
            "MouseTrack/MouseExit",
            "MouseTrack/MouseHover",
            "MouseWheel/MouseWheel",
            "OpenWindow",
            "OpenWindow/open",
            "Paint/Paint",
            "PaintItem/PaintItem",
            "PaintObject/paintObject",
            "Popups",
            "Progress/changed",
            "Progress/completed",
            "ResendGeometry",
            "ResendText",
            "Scroll/Scroll",
            "Segment/Segments",
            "Selection/DefaultSelection",
            "Selection/OpenMenu",
            "Selection/Selection",
            "SetData/SetData",
            "Shell/Activate",
            "Shell/Close",
            "Shell/Deactivate",
            "Shell/Deiconify",
            "Shell/Iconify",
            "Shell/SetBounds",
            "StatusText/changed",
            "TextChange",
            "TextGeometry",
            "Title/changed",
            "Touch/Touch",
            "Tracker/cancel",
            "Tracker/close",
            "Tracker/open",
            "Traverse/Traverse",
            "Tree/Collapse",
            "Tree/Expand",
            "Verify/Verify",
            "VerifyKey/verifyKey",
            "VisibilityWindow/hide",
            "VisibilityWindow/show",
            "WinClose",
            "WinMaximize",
            "WinMinimize",
            "WinRestore",
            "WinUnload",
            "WindowBounds",
            "WindowOpenFailed",
            "WindowState",
            "WindowTitle",
            "WordMovement/getNextOffset",
            "WordMovement/getPreviousOffset",
            "activate",
            "activation",
            "b",
            "back",
            null,
            null,
            null,
            null,
            null,
            null,
            "browserFunctionCall",
            "c",
            "close",
            "cookie",
            "copyAreaImageintint",
            "copyAreaImageintintResponse",
            "copyAreaintintintintintintboolean",
            "drawArcintintintintintint",
            "drawFocusintintintint",
            "drawImageImageintint",
            "drawImageImageintintintint",
            "drawImageImageintintintintintintintint",
            "drawLineintintintint",
            "drawOvalintintintint",
            "drawPathPath",
            "drawPointintint",
            "drawPolygonint",
            "drawPolylineint",
            "drawRectangleintintintint",
            "drawRoundRectangleintintintintintint",
            "drawTextStringintintint",
            "evaluate",
            "evaluate/result",
            "evolve.test.drag",
            "evolve.test.dragDone",
            "evolve.test.frameCost",
            "evolve.test.frameCostResponse",
            "evolve.test.frameStats",
            "evolve.test.frameStatsResponse",
            "evolve.test.frameSync",
            "evolve.test.frameSynced",
            "evolve.test.iframeLoaded",
            "evolve.test.query",
            "evolve.test.queryResponse",
            "evolve.test.styledTextPerf",
            "evolve.test.styledTextPerfResponse",
            "evolve.test.wireStats",
            "evolve.test.wireStatsResponse",
            "execute",
            "fillArcintintintintintint",
            "fillGradientRectangleintintintintboolean",
            "fillOvalintintintint",
            "fillPathPath",
            "fillPolygonint",
            "fillRectangleintintintint",
            "fillRoundRectangleintintintintintint",
            "fontSizeRequest",
            "fontSizeResponse",
            "forward",
            "gcDispose",
            "getCookie",
            "imageInit",
            "imageResult",
            "imageSizeRequest",
            "imageSizeResponse",
            "imageSnapshotResult",
            "key/verdict",
            "key/vetoable",
            "locationchanging/result",
            "menu/verdict",
            "modify/verdict",
            "modify/vetoable",
            "navigate",
            "navigateRequest",
            "navigationState",
            "nextFrame",
            "openMenu",
            "pixelsResult",
            "reload",
            "renderSnapshot",
            "setCookie",
            "shown",
            "stop",
            "swing",
            "swing.open",
            "swingIsland",
            "swingIsland.close",
            "swingIsland.dispose",
            "swingIsland.island",
            "swingIsland.opened",
            "swingIsland.resize",
            "swingIsland.surface",
            "swingIsland.title",
            "swt.evolve.batch",
            "swt.evolve.focus",
            "swt.evolve.focus.seen",
            "swt.evolve.focusHolder",
            "swt.evolve.properties",
            null,
            "swt.evolve.test.runUi",
            "swt.evolve.test.runUiResponse",
            "swt.evolve.timing.bare",
            "swt.evolve.timing.ping",
            "swt.evolve.timing.pong",
            "swt.evolve.url.open",
            "swt.evolve.widget.refresh",
            "swt.evolve.wire",
            "traverse/verdict",
            "traverse/vetoable",
            "v",
            "widgetSizeRequest",
            "widgetSizeResponse",
            "DnD/remoteDrag",
            "DnD/remoteDropNone",
            "DragDetect/dragCancel",
            "DragDetect/dragImage",
            "DragDetect/dragMove",
            "DragDetect/dragRelease",
            "DragDetect/remoteAnswer",
            "WinDidEnterFullScreen",
            "WinDidExitFullScreen",
            "WinExitFullScreen",
            "WinFullScreen",
            "WinFullScreenState",
            "anim",
            "callback",
            "rebuild",
            "subtree",
            "subtree/request",
            "transport",
            "transport/request",
            "evolve.test.bootTrace",
            "evolve.test.bootTraceResponse",
            "TabFolder/stripLaidOut",
    };

    /** Record type names by number. */
    public static final String[] TYPES = {
            null,
            "Accessible",
            "AnimatedProgress",
            null,
            "BorderData",
            "Browser",
            "BrowserFunction",
            "BusyIndicator",
            "Button",
            "ByteArrayTransfer",
            "CBanner",
            "CCombo",
            "CLabel",
            "CTabFolder",
            "CTabItem",
            "Canvas",
            "Caret",
            "Clipboard",
            "Color",
            "ColorDialog",
            "Combo",
            "Composite",
            "ControlEditor",
            "CoolBar",
            "CoolItem",
            "Cursor",
            "DateTime",
            "Decorations",
            "Device",
            "Dialog",
            "DirectoryDialog",
            "Display",
            "DragSource",
            "DropTarget",
            "Event",
            "EwtWidget",
            "ExpandBar",
            "ExpandItem",
            "FileDialog",
            "FileTransfer",
            "Font",
            "FontData",
            "FontDialog",
            "FontMeasureBridge",
            "FontMetrics",
            "FormAttachment",
            "FormData",
            "GC",
            "GCData",
            "GLCanvas",
            "GLData",
            "GlyphMetrics",
            "GridData",
            "Group",
            "HTMLTransfer",
            "IME",
            "Image",
            "ImageData",
            "ImageSizeBridge",
            "ImageTransfer",
            "Label",
            "LineInfo",
            "LineSizeInfo",
            "Link",
            "List",
            "MainComposite",
            "MainToolbar",
            "Menu",
            "MenuItem",
            "MessageBox",
            "Monitor",
            "PDFDocument",
            "Path",
            "PathData",
            "Pattern",
            "Point",
            "PopupList",
            "PrintDialog",
            "Printer",
            "Program",
            "ProgressBar",
            "RGB",
            "RTFTransfer",
            "Rectangle",
            "Region",
            "Resource",
            "RowData",
            "Sash",
            "SashForm",
            "Scale",
            "ScrollBar",
            "ScrolledComposite",
            "Shell",
            "ShellPopups",
            "SideBar",
            "Slider",
            "Spinner",
            "StatusBar",
            "StyleRange",
            "StyledText",
            "StyledTextPrintOptions",
            "StyledTextRenderer",
            "SwingIsland",
            "TabFolder",
            "TabItem",
            "Table",
            "TableColumn",
            "TableCursor",
            "TableEditor",
            "TableItem",
            "TaskBar",
            "TaskItem",
            "Text",
            "TextStyle",
            "TextTransfer",
            "ToolBar",
            "ToolItem",
            "ToolTip",
            "Touch",
            "TouchSource",
            "Tracker",
            "Transfer",
            "TransferData",
            "Transform",
            "Tray",
            "TrayItem",
            "Tree",
            "TreeColumn",
            "TreeCursor",
            "TreeEditor",
            "TreeItem",
            "URLTransfer",
            "ViewForm",
            "WidgetSizeBridge",
            "DisplayPopups",
    };

    /** The keys of a map sent as tagged values, by number. */
    public static final String[] KEYS = {
            null,
            "RGB",
            "RGBs",
            "XORMode",
            "_d",
            "_gd",
            "_gr",
            "_r",
            "_s",
            "a",
            "accelerator",
            "accumAlphaSize",
            "accumBlueSize",
            "accumGreenSize",
            "accumRedSize",
            "active",
            "activeShellId",
            "alignment",
            "alpha",
            "alphaSize",
            "alwaysShowScrollBars",
            "antialias",
            "args",
            "ascent",
            "assets_path",
            "availableTypeNames",
            "averageCharWidth",
            "averageLineHeight",
            "b",
            "background",
            "backgroundImage",
            "backgroundMode",
            "backgroundPattern",
            "backgrounds",
            "blinkRate",
            "blockSelection",
            "blockSelectionBounds",
            "blue",
            "blueSize",
            "boldFont",
            "boldItalicFont",
            "borderColor",
            "borderStyle",
            "borderVisible",
            "bottom",
            "bottomMargin",
            "bounds",
            "browser",
            "bufferScale",
            "bulletTexts",
            "bulletsIndices",
            "button",
            "buttonLabels",
            "cacheBottom",
            "cacheHeight",
            "cacheLeft",
            "cacheRight",
            "cacheTop",
            "cacheWidth",
            "caret",
            "caretOffset",
            "caretOffsets",
            "caretPosition",
            "charCount",
            "character",
            "checked",
            "chevronVisible",
            "children",
            "clientHeight",
            "clientWidth",
            "clientX",
            "clientY",
            "clipping",
            "clippingPath",
            "clippingRects",
            "clippingText",
            "color1",
            "color2",
            "column",
            "columnOrder",
            "columns",
            "config",
            "content",
            "control",
            "cookie",
            "count",
            "csd_maximize",
            "csd_os",
            "csd_placement",
            "csd_titlebar_color",
            "ctabfolder_tab_scroll",
            "ctabfolder_topright_auto_hide",
            "ctabfolder_visible_controls",
            "currentDataTypeId",
            "currentHeight",
            "currentHhint",
            "currentWhint",
            "currentWidth",
            "cursor",
            "cursorStyle",
            "custom_tooltip",
            "damage",
            "data",
            "day",
            "decimal_separator",
            "decorations_align",
            "defaultHeight",
            "defaultHhint",
            "defaultWhint",
            "defaultWidth",
            "denominator",
            "depth",
            "depthSize",
            "descent",
            "detail",
            "dialogs",
            "digits",
            "direct",
            "disable_control_gc_overlay",
            "disable_evolve_icons",
            "disable_hover_zoom",
            "disable_swt_canvas_colors",
            "disabledImage",
            "doit",
            "doubleBuffer",
            "doubleClickEnabled",
            "double_click_timeout_ms",
            "dragDetect",
            "dragImage",
            "dragSource",
            "dragSourceId",
            "dropTargetId",
            "echoCharacter",
            "editTexts",
            "editable",
            "editor",
            "editors",
            "edits",
            "effectsVisible",
            "elements",
            "enabled",
            "end",
            "endPage",
            "endX",
            "endY",
            "error",
            "exclude",
            "expandHorizontal",
            "expandVertical",
            "expanded",
            "expires",
            "feedback",
            "fileName",
            "fileNames",
            "filename",
            "fillRule",
            "filterExtensions",
            "filterIndex",
            "filterNames",
            "filterPath",
            "fixedLineHeight",
            "fixedPitch",
            "flags",
            "focus",
            "focusHolderId",
            "focus_indicators",
            "focused",
            "font",
            "fontData",
            "fontList",
            "fontStyle",
            "font_point_scale",
            "fonts",
            "footer",
            "force_theme",
            "foreground",
            "foregroundPattern",
            "foregrounds",
            "fullRepaint",
            "fullScreen",
            "functionNames",
            "g",
            "gc",
            "gc_icons_replacement",
            "grabExcessHorizontalSpace",
            "grabExcessVerticalSpace",
            "gradientColors",
            "gradientPercents",
            "gradientVertical",
            "grayed",
            "green",
            "greenSize",
            "hHint",
            "handle",
            "hasLinks",
            "hasOwnBackground",
            "header",
            "headerBackground",
            "headerForeground",
            "headerHeight",
            "headerVisible",
            "headers",
            "height",
            "heightHint",
            "highlight",
            "highlightEnabled",
            "horizontalAlignment",
            "horizontalBar",
            "horizontalIndent",
            "horizontalPixel",
            "horizontalSpan",
            "hotImage",
            "hotspotX",
            "hotspotY",
            "hours",
            "hoverFills",
            "hover_delay_ms",
            "id",
            "idleRunning",
            "image",
            "imageData",
            "image_disable_icons_replacement",
            "images",
            "increment",
            "indent",
            "index",
            "inheritsBackground",
            "interpolation",
            "isVisited",
            "italicFont",
            "item",
            "itemCount",
            "itemId",
            "itemOrder",
            "itemSizes",
            "itemTooltips",
            "items",
            "javascriptEnabled",
            "jobName",
            "justify",
            "keptText",
            "keyCode",
            "keyLocation",
            "left",
            "leftMargin",
            "length",
            "lineCap",
            "lineCount",
            "lineDash",
            "lineDashOffset",
            "lineJoin",
            "lineLabels",
            "lineSizes",
            "lineSpacing",
            "lineSpacingComputing",
            "lineSpacings",
            "lineStyle",
            "lineWidth",
            "lines",
            "linesInAverageLineHeight",
            "linesVisible",
            "linkForeground",
            "listVisible",
            "localFileBase",
            "localFilePath",
            "localFileText",
            "locale",
            "location",
            "magnification",
            "mainShellId",
            "map",
            "marginColor",
            "maxWidth",
            "maxWidthLineIndex",
            "maximizeVisible",
            "maximized",
            "maximizedControl",
            "maximum",
            "maximumSize",
            "menu",
            "menuBar",
            "menuStripHeight",
            "message",
            "metrics",
            "minHeight",
            "minWidth",
            "minimizeVisible",
            "minimized",
            "minimum",
            "minimumCharacters",
            "minimumHeight",
            "minimumSize",
            "minimumWidth",
            "minutes",
            "modified",
            "month",
            "moveable",
            "name",
            "needed",
            "notification_popup",
            "numerator",
            "offset",
            "operations",
            "orientation",
            "origin",
            "originRequest",
            "overwrite",
            "ownMenuStrip",
            "ownerDrawItemHeight",
            "pageIncrement",
            "paintedTexts",
            "pathData",
            "pixels",
            "points",
            "popups",
            "port",
            "postData",
            "preferredSize",
            "preserve_icon_colors",
            "primary",
            "printLineBackground",
            "printLineNumbers",
            "printTextBackground",
            "printTextFontStyle",
            "printTextForeground",
            "printToFile",
            "print_move",
            "push_button_margin",
            "r",
            "ranges",
            "rects",
            "red",
            "redSize",
            "redrawLines",
            "ref",
            "region",
            "regularFont",
            "remoteRef",
            "renderer",
            "replaced",
            "reqId",
            "resizable",
            "result",
            "retain",
            "right",
            "rightMargin",
            "rise",
            "rotation",
            "sampleBuffers",
            "samples",
            "sashWidth",
            "scope",
            "script",
            "seconds",
            "segments",
            "segmentsChars",
            "selectedImageVisible",
            "selection",
            "selectionBackground",
            "selectionBarThickness",
            "selectionBgImage",
            "selectionFills",
            "selectionForeground",
            "selectionRanges",
            "seq",
            "shareContext",
            "shells",
            "showChevron",
            "showClose",
            "showListPopupSeq",
            "show_scaling_control",
            "show_theme_color_palette",
            "showing",
            "single",
            "sortColumn",
            "sortDirection",
            "source",
            "spacing",
            "start",
            "startPage",
            "startX",
            "startY",
            "state",
            "stateMask",
            "stencilSize",
            "stereo",
            "strikeout",
            "strikeoutColor",
            "style",
            "styleCount",
            "styleIndex",
            "styledText",
            "styles",
            "stylesSet",
            "stylesSetCount",
            "svgContent",
            "swt",
            "systemMenu",
            "system_menu_bar",
            "tabHeight",
            "tabLength",
            "tabPosition",
            "tabStop",
            "tabStops",
            "tabWidth",
            "tabs",
            "text",
            "textFonts",
            "textLimit",
            "textOrigins",
            "textStrings",
            "texts",
            "theme_color",
            "theme_colors_by_widget",
            "theme_name",
            "themedCanvas",
            "thumb",
            "time",
            "toolTipText",
            "tooltips",
            "top",
            "topIndex",
            "topItem",
            "topMargin",
            "topPixel",
            "topRight",
            "topRightAlignment",
            "touches",
            "tracking",
            "transform",
            "transparent_menu_check_box",
            "type",
            "types",
            "ui_zoom",
            "underline",
            "underlineColor",
            "underlineStyle",
            "unselectedCloseVisible",
            "unselectedImageVisible",
            "url",
            "use_special_dropdown_button",
            "use_swt_border",
            "use_swt_colors",
            "use_swt_font_colors",
            "use_swt_font_colors_by_widget",
            "use_swt_fonts",
            "value",
            "verdict",
            "verticalAlignment",
            "verticalBar",
            "verticalIndent",
            "verticalSpan",
            "visible",
            "wHint",
            "warnings",
            "weights",
            "widget",
            "width",
            "widthHint",
            "windowId",
            "windowedShellIds",
            "wordWrap",
            "wrapIndent",
            "wrapIndices",
            "x",
            "xDirection",
            "y",
            "yDirection",
            "year",
            "zoom",
            "bigger_icon_size",
            "scale_vertical_minimum_at_top",
            "bufferScaleY",
            "stripLayoutSeq",
            "dirtyIndicatorStyle",
            "paintedImages",
            "showDirty",
            "bottomLine",
            "ctabfolder_chevron_after_tabs",
            "curveLine",
            "regional_locale",
            "separator",
            "compactRows",
    };

    /** Properties as `Class.name`, by number; null where no property has the number. */
    public static final String[] PROPERTIES = new String[6339];

    /** Each property's encoding, by number; null where nothing writes it. */
    public static final String[] KINDS = new String[PROPERTIES.length];

    // A property number is its class's block times 64 plus its slot, so the arrays are mostly empty
    // and filled here: an array initializer that size would not fit a class initializer.
    static {
        property(64, "Accessible.control", "ref");
        property(65, "Accessible.focus", "int");
        property(128, "BorderData.hHint", "int");
        property(129, "BorderData.region", "int");
        property(130, "BorderData.wHint", "int");
        property(192, "Browser.functionNames", "string[]");
        property(193, "Browser.javascriptEnabled", "bool");
        property(194, "Browser.text", "string");
        property(195, "Browser.url", "string");
        property(256, "BrowserFunction.browser", "ref");
        property(257, "BrowserFunction.name", "string");
        property(320, "Button.alignment", "int");
        property(321, "Button.grayed", "bool");
        property(322, "Button.image", "image");
        property(323, "Button.primary", "bool");
        property(324, "Button.selection", "bool");
        property(325, "Button.text", "string");
        property(384, "CCombo.alignment", "int");
        property(385, "CCombo.itemTooltips", "string[]");
        property(386, "CCombo.items", "string[]");
        property(387, "CCombo.listVisible", "bool");
        property(388, "CCombo.selection", "point");
        property(389, "CCombo.text", "string");
        property(390, "CCombo.textLimit", "int");
        property(391, "CCombo.editable", "bool");
        property(448, "CLabel.alignment", "int");
        property(449, "CLabel.backgroundImage", "image");
        property(450, "CLabel.bottomMargin", "int");
        property(451, "CLabel.gradientColors", "color[]");
        property(452, "CLabel.gradientPercents", "int[]");
        property(453, "CLabel.gradientVertical", "bool");
        property(454, "CLabel.image", "image");
        property(455, "CLabel.leftMargin", "int");
        property(456, "CLabel.rightMargin", "int");
        property(457, "CLabel.text", "string");
        property(458, "CLabel.topMargin", "int");
        property(512, "CTabFolder.borderVisible", "bool");
        property(513, "CTabFolder.chevronVisible", "bool");
        property(514, "CTabFolder.highlight", "bool");
        property(515, "CTabFolder.highlightEnabled", "bool");
        property(516, "CTabFolder.items", "ref[]");
        property(517, "CTabFolder.maximizeVisible", "bool");
        property(518, "CTabFolder.maximized", "bool");
        property(519, "CTabFolder.minimizeVisible", "bool");
        property(520, "CTabFolder.minimized", "bool");
        property(521, "CTabFolder.minimumCharacters", "int");
        property(522, "CTabFolder.selectedImageVisible", "bool");
        property(523, "CTabFolder.selection", "int");
        property(524, "CTabFolder.selectionBackground", "color");
        property(525, "CTabFolder.selectionBarThickness", "int");
        property(526, "CTabFolder.selectionBgImage", "image");
        property(527, "CTabFolder.selectionForeground", "color");
        property(528, "CTabFolder.showChevron", "bool");
        property(529, "CTabFolder.showListPopupSeq", "int");
        property(530, "CTabFolder.single", "bool");
        property(531, "CTabFolder.tabHeight", "int?");
        property(532, "CTabFolder.tabPosition", "int");
        property(533, "CTabFolder.topRight", "ref");
        property(534, "CTabFolder.topRightAlignment", "int");
        property(535, "CTabFolder.unselectedCloseVisible", "bool");
        property(536, "CTabFolder.unselectedImageVisible", "bool");
        property(537, "CTabFolder.stripLayoutSeq", "int");
        property(538, "CTabFolder.dirtyIndicatorStyle", "bool");
        property(576, "CTabItem.control", "ref");
        property(577, "CTabItem.font", "object:VFont");
        property(578, "CTabItem.foreground", "color");
        property(579, "CTabItem.selectionForeground", "color");
        property(580, "CTabItem.showClose", "bool");
        property(581, "CTabItem.showing", "bool");
        property(582, "CTabItem.toolTipText", "string");
        property(583, "CTabItem.showDirty", "bool");
        property(640, "Canvas.caret", "ref");
        property(704, "Caret.blinkRate", "int");
        property(705, "Caret.bounds", "rect");
        property(706, "Caret.focused", "bool");
        property(707, "Caret.image", "image");
        property(708, "Caret.visible", "bool");
        property(768, "Clipboard.availableTypeNames", "string[]");
        property(832, "Color.a", "int");
        property(833, "Color.b", "int");
        property(834, "Color.g", "int");
        property(835, "Color.r", "int");
        property(896, "ColorDialog.RGB", "rgb");
        property(897, "ColorDialog.RGBs", "rgb[]");
        property(960, "Combo.caretPosition", "int");
        property(961, "Combo.items", "string[]");
        property(962, "Combo.listVisible", "bool");
        property(963, "Combo.text", "string");
        property(964, "Combo.textLimit", "int");
        property(1024, "Composite.backgroundMode", "int");
        property(1025, "Composite.children", "ref[]");
        property(1088, "ConfigFlags.assets_path", "string");
        property(1089, "ConfigFlags.csd_maximize", "string");
        property(1090, "ConfigFlags.csd_os", "string");
        property(1091, "ConfigFlags.csd_placement", "string");
        property(1092, "ConfigFlags.csd_titlebar_color", "string");
        property(1093, "ConfigFlags.ctabfolder_tab_scroll", "bool");
        property(1094, "ConfigFlags.ctabfolder_topright_auto_hide", "bool");
        property(1095, "ConfigFlags.ctabfolder_visible_controls", "bool");
        property(1096, "ConfigFlags.custom_tooltip", "bool");
        property(1097, "ConfigFlags.decimal_separator", "string");
        property(1098, "ConfigFlags.decorations_align", "string");
        property(1099, "ConfigFlags.disable_control_gc_overlay", "bool");
        property(1100, "ConfigFlags.disable_evolve_icons", "bool");
        property(1101, "ConfigFlags.disable_hover_zoom", "bool");
        property(1102, "ConfigFlags.disable_swt_canvas_colors", "bool");
        property(1103, "ConfigFlags.double_click_timeout_ms", "int");
        property(1104, "ConfigFlags.focus_indicators", "bool?");
        property(1105, "ConfigFlags.font_point_scale", "double");
        property(1106, "ConfigFlags.force_theme", "string");
        property(1107, "ConfigFlags.gc_icons_replacement", "bool");
        property(1108, "ConfigFlags.hover_delay_ms", "int");
        property(1109, "ConfigFlags.image_disable_icons_replacement", "bool");
        property(1110, "ConfigFlags.locale", "string");
        property(1111, "ConfigFlags.notification_popup", "bool");
        property(1112, "ConfigFlags.preserve_icon_colors", "bool");
        property(1113, "ConfigFlags.print_move", "bool");
        property(1114, "ConfigFlags.push_button_margin", "int");
        property(1115, "ConfigFlags.show_scaling_control", "bool");
        property(1116, "ConfigFlags.show_theme_color_palette", "bool");
        property(1117, "ConfigFlags.system_menu_bar", "bool");
        property(1118, "ConfigFlags.theme_color", "string");
        property(1119, "ConfigFlags.theme_colors_by_widget", "stringMap");
        property(1120, "ConfigFlags.theme_name", "string");
        property(1121, "ConfigFlags.transparent_menu_check_box", "bool");
        property(1122, "ConfigFlags.ui_zoom", "int");
        property(1123, "ConfigFlags.use_special_dropdown_button", "bool");
        property(1124, "ConfigFlags.use_swt_border", "bool");
        property(1125, "ConfigFlags.use_swt_colors", "bool");
        property(1126, "ConfigFlags.use_swt_font_colors", "bool?");
        property(1127, "ConfigFlags.use_swt_font_colors_by_widget", "boolMap");
        property(1128, "ConfigFlags.use_swt_fonts", "bool");
        property(1129, "ConfigFlags.bigger_icon_size", "bool");
        property(1130, "ConfigFlags.scale_vertical_minimum_at_top", "bool");
        property(1131, "ConfigFlags.ctabfolder_chevron_after_tabs", "bool");
        property(1132, "ConfigFlags.regional_locale", "string");
        property(1152, "Control.background", "color");
        property(1153, "Control.backgroundImage", "image");
        property(1154, "Control.bounds", "rect");
        property(1155, "Control.cursor", "object:VCursor");
        property(1156, "Control.dragDetect", "bool");
        property(1157, "Control.dragSource", "bool");
        property(1158, "Control.dragSourceId", "long?");
        property(1159, "Control.dropTargetId", "long?");
        property(1160, "Control.enabled", "bool?");
        property(1161, "Control.font", "object:VFont");
        property(1162, "Control.foreground", "color");
        property(1163, "Control.hasOwnBackground", "bool");
        property(1164, "Control.inheritsBackground", "bool");
        property(1165, "Control.menu", "ref");
        property(1166, "Control.region", "object:VRegion");
        property(1167, "Control.tabStop", "bool");
        property(1168, "Control.toolTipText", "string");
        property(1169, "Control.visible", "bool?");
        property(1216, "ControlEditor.editor", "ref");
        property(1280, "CoolBar.itemOrder", "int[]");
        property(1281, "CoolBar.itemSizes", "point[]");
        property(1282, "CoolBar.items", "ref[]");
        property(1283, "CoolBar.wrapIndices", "int[]");
        property(1344, "CoolItem.control", "ref");
        property(1345, "CoolItem.preferredSize", "point");
        property(1408, "Cursor.cursorStyle", "int");
        property(1409, "Cursor.hotspotX", "int");
        property(1410, "Cursor.hotspotY", "int");
        property(1411, "Cursor.image", "image");
        property(1472, "DateTime.day", "int");
        property(1473, "DateTime.hours", "int");
        property(1474, "DateTime.minutes", "int");
        property(1475, "DateTime.month", "int");
        property(1476, "DateTime.seconds", "int");
        property(1477, "DateTime.year", "int");
        property(1536, "Decorations.menuBar", "ref");
        property(1537, "Decorations.menuStripHeight", "int");
        property(1538, "Decorations.ownMenuStrip", "bool");
        property(1539, "Decorations.text", "string");
        property(1600, "Device.fontList", "object:VFontData[]");
        property(1601, "Device.tracking", "bool");
        property(1602, "Device.warnings", "bool");
        property(1664, "Dialog.id", "long");
        property(1665, "Dialog.style", "int");
        property(1666, "Dialog.swt", "string");
        property(1667, "Dialog.text", "string");
        property(1728, "DirectoryDialog.filterPath", "string");
        property(1729, "DirectoryDialog.message", "string");
        property(1792, "Display.activeShellId", "long");
        property(1793, "Display.config", "object:ConfigFlags");
        property(1794, "Display.focusHolderId", "long");
        property(1795, "Display.mainShellId", "long");
        property(1796, "Display.popups", "ref[]");
        property(1797, "Display.shells", "ref[]");
        property(1798, "Display.systemMenu", "ref");
        property(1799, "Display.tooltips", "ref[]");
        property(1800, "Display.windowedShellIds", "long[]");
        property(1856, "Event.button", "int");
        property(1857, "Event.character", "char");
        property(1858, "Event.count", "int");
        property(1859, "Event.currentDataTypeId", "int");
        property(1860, "Event.detail", "int");
        property(1861, "Event.doit", "bool");
        property(1862, "Event.dragImage", "image");
        property(1863, "Event.end", "int");
        property(1864, "Event.feedback", "int");
        property(1865, "Event.gc", "ref");
        property(1866, "Event.height", "int");
        property(1867, "Event.index", "int");
        property(1868, "Event.item", "ref");
        property(1869, "Event.itemId", "long");
        property(1870, "Event.keyCode", "int");
        property(1871, "Event.keyLocation", "int");
        property(1872, "Event.magnification", "double");
        property(1873, "Event.operations", "int");
        property(1874, "Event.rotation", "double");
        property(1875, "Event.segments", "int[]");
        property(1876, "Event.segmentsChars", "char[]");
        property(1877, "Event.start", "int");
        property(1878, "Event.stateMask", "int");
        property(1879, "Event.text", "string");
        property(1880, "Event.time", "int");
        property(1881, "Event.touches", "object:VTouch[]");
        property(1882, "Event.type", "int");
        property(1883, "Event.widget", "ref");
        property(1884, "Event.width", "int");
        property(1885, "Event.x", "int");
        property(1886, "Event.xDirection", "int");
        property(1887, "Event.y", "int");
        property(1888, "Event.yDirection", "int");
        property(1920, "ExpandBar.items", "ref[]");
        property(1921, "ExpandBar.spacing", "int");
        property(1984, "ExpandItem.control", "ref");
        property(1985, "ExpandItem.expanded", "bool");
        property(1986, "ExpandItem.headerHeight", "int");
        property(1987, "ExpandItem.height", "int");
        property(2048, "FileDialog.fileName", "string");
        property(2049, "FileDialog.fileNames", "string[]");
        property(2050, "FileDialog.filterExtensions", "string[]");
        property(2051, "FileDialog.filterIndex", "int");
        property(2052, "FileDialog.filterNames", "string[]");
        property(2053, "FileDialog.filterPath", "string");
        property(2054, "FileDialog.overwrite", "bool");
        property(2112, "Font.fontData", "object:VFontData[]");
        property(2176, "FontData.height", "int");
        property(2177, "FontData.name", "string");
        property(2178, "FontData.style", "int");
        property(2240, "FontDialog.RGB", "rgb");
        property(2241, "FontDialog.effectsVisible", "bool");
        property(2242, "FontDialog.fontData", "object:VFontData");
        property(2243, "FontDialog.fontList", "object:VFontData[]");
        property(2304, "FormAttachment.alignment", "int");
        property(2305, "FormAttachment.control", "ref");
        property(2306, "FormAttachment.denominator", "int");
        property(2307, "FormAttachment.numerator", "int");
        property(2308, "FormAttachment.offset", "int");
        property(2368, "FormData.bottom", "object:VFormAttachment");
        property(2369, "FormData.cacheBottom", "object:VFormAttachment");
        property(2370, "FormData.cacheHeight", "int");
        property(2371, "FormData.cacheLeft", "object:VFormAttachment");
        property(2372, "FormData.cacheRight", "object:VFormAttachment");
        property(2373, "FormData.cacheTop", "object:VFormAttachment");
        property(2374, "FormData.cacheWidth", "int");
        property(2375, "FormData.currentHeight", "int");
        property(2376, "FormData.currentHhint", "int");
        property(2377, "FormData.currentWhint", "int");
        property(2378, "FormData.currentWidth", "int");
        property(2379, "FormData.defaultHeight", "int");
        property(2380, "FormData.defaultHhint", "int");
        property(2381, "FormData.defaultWhint", "int");
        property(2382, "FormData.defaultWidth", "int");
        property(2383, "FormData.height", "int");
        property(2384, "FormData.isVisited", "bool");
        property(2385, "FormData.left", "object:VFormAttachment");
        property(2386, "FormData.needed", "bool");
        property(2387, "FormData.right", "object:VFormAttachment");
        property(2388, "FormData.top", "object:VFormAttachment");
        property(2389, "FormData.width", "int");
        property(2432, "GC.XORMode", "bool");
        property(2433, "GC.alpha", "int?");
        property(2434, "GC.antialias", "int");
        property(2435, "GC.background", "color");
        property(2436, "GC.backgroundPattern", "object:VPattern");
        property(2437, "GC.bufferScale", "float");
        property(2438, "GC.clipping", "rect");
        property(2439, "GC.clippingPath", "object:VPathData");
        property(2440, "GC.clippingRects", "int[]");
        property(2441, "GC.clippingText", "object:VPath");
        property(2442, "GC.fillRule", "int?");
        property(2443, "GC.font", "object:VFont");
        property(2444, "GC.foreground", "color");
        property(2445, "GC.foregroundPattern", "object:VPattern");
        property(2446, "GC.hoverFills", "rgb[]");
        property(2447, "GC.interpolation", "int");
        property(2448, "GC.lineCap", "int?");
        property(2449, "GC.lineDash", "int[]");
        property(2450, "GC.lineDashOffset", "float");
        property(2451, "GC.lineJoin", "int?");
        property(2452, "GC.lineStyle", "int?");
        property(2453, "GC.lineWidth", "int");
        property(2454, "GC.selectionFills", "rgb[]");
        property(2455, "GC.style", "int");
        property(2456, "GC.themedCanvas", "bool?");
        property(2457, "GC.transform", "object:VTransform");
        property(2458, "GC.bufferScaleY", "float");
        property(2496, "GLData.accumAlphaSize", "int");
        property(2497, "GLData.accumBlueSize", "int");
        property(2498, "GLData.accumGreenSize", "int");
        property(2499, "GLData.accumRedSize", "int");
        property(2500, "GLData.alphaSize", "int");
        property(2501, "GLData.blueSize", "int");
        property(2502, "GLData.depthSize", "int");
        property(2503, "GLData.doubleBuffer", "bool");
        property(2504, "GLData.greenSize", "int");
        property(2505, "GLData.redSize", "int");
        property(2506, "GLData.sampleBuffers", "int");
        property(2507, "GLData.samples", "int");
        property(2508, "GLData.shareContext", "ref");
        property(2509, "GLData.stencilSize", "int");
        property(2510, "GLData.stereo", "bool");
        property(2560, "GlyphMetrics.ascent", "int");
        property(2561, "GlyphMetrics.descent", "int");
        property(2562, "GlyphMetrics.width", "int");
        property(2624, "GridData.cacheHeight", "int");
        property(2625, "GridData.cacheWidth", "int");
        property(2626, "GridData.currentHeight", "int");
        property(2627, "GridData.currentHhint", "int");
        property(2628, "GridData.currentWhint", "int");
        property(2629, "GridData.currentWidth", "int");
        property(2630, "GridData.defaultHeight", "int");
        property(2631, "GridData.defaultHhint", "int");
        property(2632, "GridData.defaultWhint", "int");
        property(2633, "GridData.defaultWidth", "int");
        property(2634, "GridData.exclude", "bool");
        property(2635, "GridData.grabExcessHorizontalSpace", "bool");
        property(2636, "GridData.grabExcessVerticalSpace", "bool");
        property(2637, "GridData.heightHint", "int");
        property(2638, "GridData.horizontalAlignment", "int");
        property(2639, "GridData.horizontalIndent", "int");
        property(2640, "GridData.horizontalSpan", "int");
        property(2641, "GridData.minimumHeight", "int");
        property(2642, "GridData.minimumWidth", "int");
        property(2643, "GridData.verticalAlignment", "int");
        property(2644, "GridData.verticalIndent", "int");
        property(2645, "GridData.verticalSpan", "int");
        property(2646, "GridData.widthHint", "int");
        property(2688, "Group.text", "string");
        property(2752, "Image.filename", "string");
        property(2753, "Image.height", "int");
        property(2754, "Image.imageData", "object:VImageData");
        property(2755, "Image.remoteRef", "long?");
        property(2756, "Image.svgContent", "string");
        property(2757, "Image.width", "int");
        property(2816, "ImageData.data", "byte[]");
        property(2817, "ImageData.depth", "int");
        property(2818, "ImageData.height", "int");
        property(2819, "ImageData.width", "int");
        property(2880, "Item.image", "image");
        property(2881, "Item.text", "string");
        property(2944, "Label.image", "image");
        property(2945, "Label.text", "string");
        property(3008, "LineInfo.alignment", "int");
        property(3009, "LineInfo.background", "color");
        property(3010, "LineInfo.flags", "int");
        property(3011, "LineInfo.indent", "int");
        property(3012, "LineInfo.justify", "bool");
        property(3013, "LineInfo.segments", "int[]");
        property(3014, "LineInfo.segmentsChars", "char[]");
        property(3015, "LineInfo.tabStops", "int[]");
        property(3016, "LineInfo.verticalIndent", "int");
        property(3017, "LineInfo.wrapIndent", "int");
        property(3072, "LineSizeInfo.height", "int");
        property(3073, "LineSizeInfo.width", "int");
        property(3136, "Link.linkForeground", "color");
        property(3137, "Link.text", "string");
        property(3200, "List.items", "string[]");
        property(3201, "List.selection", "int[]");
        property(3202, "List.topIndex", "int");
        property(3264, "Menu.enabled", "bool");
        property(3265, "Menu.items", "ref[]");
        property(3266, "Menu.location", "point");
        property(3267, "Menu.orientation", "int");
        property(3268, "Menu.visible", "bool");
        property(3328, "MenuItem.accelerator", "int");
        property(3329, "MenuItem.enabled", "bool");
        property(3330, "MenuItem.menu", "ref");
        property(3331, "MenuItem.selection", "bool");
        property(3332, "MenuItem.toolTipText", "string");
        property(3392, "MessageBox.buttonLabels", "string[]");
        property(3393, "MessageBox.message", "string");
        property(3456, "Monitor.clientHeight", "int");
        property(3457, "Monitor.clientWidth", "int");
        property(3458, "Monitor.clientX", "int");
        property(3459, "Monitor.clientY", "int");
        property(3460, "Monitor.handle", "long");
        property(3461, "Monitor.height", "int");
        property(3462, "Monitor.width", "int");
        property(3463, "Monitor.x", "int");
        property(3464, "Monitor.y", "int");
        property(3465, "Monitor.zoom", "int");
        property(3520, "PDFDocument.height", "double");
        property(3521, "PDFDocument.width", "double");
        property(3584, "Path.pathData", "object:VPathData");
        property(3585, "Path.textFonts", "object:VFont[]");
        property(3586, "Path.textOrigins", "float[]");
        property(3587, "Path.textStrings", "string[]");
        property(3648, "PathData.points", "float[]");
        property(3649, "PathData.types", "byte[]");
        property(3712, "Pattern.color1", "color");
        property(3713, "Pattern.color2", "color");
        property(3714, "Pattern.endX", "float");
        property(3715, "Pattern.endY", "float");
        property(3716, "Pattern.image", "image");
        property(3717, "Pattern.startX", "float");
        property(3718, "Pattern.startY", "float");
        property(3776, "Point.x", "int");
        property(3777, "Point.y", "int");
        property(3840, "PopupList.font", "object:VFont");
        property(3841, "PopupList.items", "string[]");
        property(3842, "PopupList.minimumWidth", "int");
        property(3843, "PopupList.selection", "string");
        property(3904, "PrintDialog.endPage", "int");
        property(3905, "PrintDialog.printToFile", "bool");
        property(3906, "PrintDialog.scope", "int");
        property(3907, "PrintDialog.startPage", "int");
        property(3968, "ProgressBar.maximum", "int");
        property(3969, "ProgressBar.minimum", "int");
        property(3970, "ProgressBar.selection", "int");
        property(4032, "RGB.blue", "int");
        property(4033, "RGB.green", "int");
        property(4034, "RGB.red", "int");
        property(4096, "Rectangle.height", "int");
        property(4097, "Rectangle.width", "int");
        property(4098, "Rectangle.x", "int");
        property(4099, "Rectangle.y", "int");
        property(4160, "Region.rects", "int[]");
        property(4224, "RowData.exclude", "bool");
        property(4225, "RowData.height", "int");
        property(4226, "RowData.width", "int");
        property(4288, "SashForm.maximizedControl", "ref");
        property(4289, "SashForm.sashWidth", "int");
        property(4290, "SashForm.weights", "int[]");
        property(4352, "Scale.maximum", "int");
        property(4353, "Scale.minimum", "int");
        property(4354, "Scale.selection", "int");
        property(4416, "ScrollBar.enabled", "bool");
        property(4417, "ScrollBar.increment", "int");
        property(4418, "ScrollBar.maximum", "int");
        property(4419, "ScrollBar.minimum", "int");
        property(4420, "ScrollBar.selection", "int");
        property(4421, "ScrollBar.thumb", "int");
        property(4422, "ScrollBar.visible", "bool");
        property(4423, "ScrollBar.pageIncrement", "int");
        property(4480, "Scrollable.horizontalBar", "ref");
        property(4481, "Scrollable.verticalBar", "ref");
        property(4544, "ScrolledComposite.alwaysShowScrollBars", "bool");
        property(4545, "ScrolledComposite.content", "ref");
        property(4546, "ScrolledComposite.expandHorizontal", "bool");
        property(4547, "ScrolledComposite.expandVertical", "bool");
        property(4548, "ScrolledComposite.minHeight", "int");
        property(4549, "ScrolledComposite.minWidth", "int");
        property(4550, "ScrolledComposite.origin", "point");
        property(4551, "ScrolledComposite.originRequest", "int");
        property(4608, "Shell.alpha", "int");
        property(4609, "Shell.dialogs", "poly[]");
        property(4610, "Shell.fullScreen", "bool");
        property(4611, "Shell.maximumSize", "point");
        property(4612, "Shell.minimumSize", "point");
        property(4613, "Shell.modified", "bool");
        property(4672, "Slider.increment", "int");
        property(4673, "Slider.maximum", "int");
        property(4674, "Slider.minimum", "int");
        property(4675, "Slider.pageIncrement", "int");
        property(4676, "Slider.selection", "int");
        property(4677, "Slider.thumb", "int");
        property(4736, "Spinner.digits", "int");
        property(4737, "Spinner.increment", "int");
        property(4738, "Spinner.keptText", "string");
        property(4739, "Spinner.maximum", "int");
        property(4740, "Spinner.minimum", "int");
        property(4741, "Spinner.pageIncrement", "int");
        property(4742, "Spinner.selection", "int");
        property(4743, "Spinner.textLimit", "int");
        property(4800, "StyleRange.fontStyle", "int");
        property(4801, "StyleRange.length", "int");
        property(4802, "StyleRange.start", "int");
        property(4864, "StyledText.alignment", "int");
        property(4865, "StyledText.alwaysShowScrollBars", "bool");
        property(4866, "StyledText.blockSelection", "bool");
        property(4867, "StyledText.blockSelectionBounds", "rect");
        property(4868, "StyledText.bottomMargin", "int");
        property(4869, "StyledText.caretOffset", "int");
        property(4870, "StyledText.caretOffsets", "int[]");
        property(4871, "StyledText.doubleClickEnabled", "bool");
        property(4872, "StyledText.editable", "bool");
        property(4873, "StyledText.fixedLineHeight", "bool");
        property(4874, "StyledText.horizontalPixel", "int");
        property(4875, "StyledText.indent", "int");
        property(4876, "StyledText.justify", "bool");
        property(4877, "StyledText.leftMargin", "int");
        property(4878, "StyledText.lineSpacing", "int");
        property(4879, "StyledText.marginColor", "color");
        property(4880, "StyledText.renderer", "object:VStyledTextRenderer");
        property(4881, "StyledText.rightMargin", "int");
        property(4882, "StyledText.selection", "point");
        property(4883, "StyledText.selectionBackground", "color");
        property(4884, "StyledText.selectionForeground", "color");
        property(4885, "StyledText.selectionRanges", "int[]");
        property(4886, "StyledText.styleIndex", "runs");
        property(4887, "StyledText.styles", "object:VStyleRange[]");
        property(4888, "StyledText.tabStops", "int[]");
        property(4889, "StyledText.tabs", "int");
        property(4890, "StyledText.text", "string");
        property(4891, "StyledText.topMargin", "int");
        property(4892, "StyledText.topPixel", "int");
        property(4893, "StyledText.wordWrap", "bool");
        property(4894, "StyledText.wrapIndent", "int");
        property(4928, "StyledTextPrintOptions.footer", "string");
        property(4929, "StyledTextPrintOptions.header", "string");
        property(4930, "StyledTextPrintOptions.jobName", "string");
        property(4931, "StyledTextPrintOptions.lineLabels", "string[]");
        property(4932, "StyledTextPrintOptions.printLineBackground", "bool");
        property(4933, "StyledTextPrintOptions.printLineNumbers", "bool");
        property(4934, "StyledTextPrintOptions.printTextBackground", "bool");
        property(4935, "StyledTextPrintOptions.printTextFontStyle", "bool");
        property(4936, "StyledTextPrintOptions.printTextForeground", "bool");
        property(4992, "StyledTextRenderer.ascent", "int");
        property(4993, "StyledTextRenderer.averageCharWidth", null);
        property(4994, "StyledTextRenderer.averageLineHeight", null);
        property(4995, "StyledTextRenderer.boldFont", null);
        property(4996, "StyledTextRenderer.boldItalicFont", null);
        property(4997, "StyledTextRenderer.bulletTexts", "string[]");
        property(4998, "StyledTextRenderer.bulletsIndices", null);
        property(4999, "StyledTextRenderer.descent", "int");
        property(5000, "StyledTextRenderer.fixedPitch", null);
        property(5001, "StyledTextRenderer.hasLinks", null);
        property(5002, "StyledTextRenderer.idleRunning", null);
        property(5003, "StyledTextRenderer.italicFont", null);
        property(5004, "StyledTextRenderer.lineCount", null);
        property(5005, "StyledTextRenderer.lineSizes", null);
        property(5006, "StyledTextRenderer.lineSpacingComputing", null);
        property(5007, "StyledTextRenderer.lineSpacings", "int[]");
        property(5008, "StyledTextRenderer.lines", "object:VLineInfo[]");
        property(5009, "StyledTextRenderer.linesInAverageLineHeight", null);
        property(5010, "StyledTextRenderer.maxWidth", null);
        property(5011, "StyledTextRenderer.maxWidthLineIndex", null);
        property(5012, "StyledTextRenderer.ranges", "int[]");
        property(5013, "StyledTextRenderer.redrawLines", null);
        property(5014, "StyledTextRenderer.regularFont", "object:VFont");
        property(5015, "StyledTextRenderer.styleCount", "int");
        property(5016, "StyledTextRenderer.styledText", null);
        property(5017, "StyledTextRenderer.styles", null);
        property(5018, "StyledTextRenderer.stylesSet", null);
        property(5019, "StyledTextRenderer.stylesSetCount", null);
        property(5020, "StyledTextRenderer.tabLength", null);
        property(5021, "StyledTextRenderer.tabWidth", null);
        property(5022, "StyledTextRenderer.topIndex", null);
        property(5056, "TabFolder.items", "ref[]");
        property(5057, "TabFolder.selection", "ref[]");
        property(5120, "TabItem.control", "ref");
        property(5121, "TabItem.toolTipText", "string");
        property(5184, "Table.columnOrder", "int[]");
        property(5185, "Table.columns", "ref[]");
        property(5186, "Table.editors", "ref[]");
        property(5187, "Table.headerBackground", "color");
        property(5188, "Table.headerForeground", "color");
        property(5189, "Table.headerVisible", "bool");
        property(5190, "Table.itemCount", "int");
        property(5191, "Table.items", "refs:TableItem");
        property(5192, "Table.linesVisible", "bool");
        property(5193, "Table.ownerDrawItemHeight", "int");
        property(5194, "Table.selection", "int[]");
        property(5195, "Table.topIndex", "int");
        property(5196, "Table.sortColumn", "ref");
        property(5197, "Table.sortDirection", "int");
        property(5198, "Table.compactRows", "bool");
        property(5248, "TableColumn.alignment", "int");
        property(5249, "TableColumn.moveable", "bool");
        property(5250, "TableColumn.resizable", "bool");
        property(5251, "TableColumn.width", "int");
        property(5252, "TableColumn.toolTipText", "string");
        property(5312, "TableEditor.column", "int");
        property(5313, "TableEditor.item", "ref");
        property(5376, "TableItem.background", "color");
        property(5377, "TableItem.backgrounds", "color[]");
        property(5378, "TableItem.checked", "bool");
        property(5379, "TableItem.font", "object:VFont");
        property(5380, "TableItem.fonts", "object:VFont[]");
        property(5381, "TableItem.foreground", "color");
        property(5382, "TableItem.foregrounds", "color[]");
        property(5383, "TableItem.grayed", "bool");
        property(5384, "TableItem.images", "image[]");
        property(5385, "TableItem.paintedTexts", "int[]");
        property(5386, "TableItem.texts", "string[]");
        property(5387, "TableItem.paintedImages", "int[]");
        property(5440, "Text.caretPosition", "int");
        property(5441, "Text.echoCharacter", "int");
        property(5442, "Text.editTexts", "string[]");
        property(5443, "Text.editable", "bool");
        property(5444, "Text.edits", "int[]");
        property(5445, "Text.message", "string");
        property(5446, "Text.selection", "point");
        property(5447, "Text.text", "string");
        property(5448, "Text.textLimit", "int");
        property(5504, "TextStyle.background", "color");
        property(5505, "TextStyle.borderColor", "color");
        property(5506, "TextStyle.borderStyle", "int");
        property(5507, "TextStyle.font", "object:VFont");
        property(5508, "TextStyle.foreground", "color");
        property(5509, "TextStyle.metrics", "object:VGlyphMetrics");
        property(5510, "TextStyle.rise", "int");
        property(5511, "TextStyle.strikeout", "bool");
        property(5512, "TextStyle.strikeoutColor", "color");
        property(5513, "TextStyle.underline", "bool");
        property(5514, "TextStyle.underlineColor", "color");
        property(5515, "TextStyle.underlineStyle", "int");
        property(5568, "ToolBar.items", "ref[]");
        property(5632, "ToolItem.background", "color");
        property(5633, "ToolItem.control", "ref");
        property(5634, "ToolItem.disabledImage", "image");
        property(5635, "ToolItem.enabled", "bool");
        property(5636, "ToolItem.foreground", "color");
        property(5637, "ToolItem.hotImage", "image");
        property(5638, "ToolItem.selection", "bool");
        property(5639, "ToolItem.toolTipText", "string");
        property(5640, "ToolItem.width", "int");
        property(5696, "ToolTip.location", "point");
        property(5697, "ToolTip.message", "string");
        property(5698, "ToolTip.text", "string");
        property(5699, "ToolTip.visible", "bool");
        property(5760, "Touch.id", "long");
        property(5761, "Touch.primary", "bool");
        property(5762, "Touch.source", "object:VTouchSource");
        property(5763, "Touch.state", "int");
        property(5764, "Touch.x", "int");
        property(5765, "Touch.y", "int");
        property(5824, "TouchSource.bounds", "rect");
        property(5825, "TouchSource.direct", "bool");
        property(5826, "TouchSource.handle", "long");
        property(5888, "Transform.elements", "float[]");
        property(5952, "Tree.columnOrder", "int[]");
        property(5953, "Tree.columns", "ref[]");
        property(5954, "Tree.editors", "ref[]");
        property(5955, "Tree.headerBackground", "color");
        property(5956, "Tree.headerForeground", "color");
        property(5957, "Tree.headerVisible", "bool");
        property(5958, "Tree.items", "refs:TreeItem");
        property(5959, "Tree.linesVisible", "bool");
        property(5960, "Tree.ownerDrawItemHeight", "int");
        property(5961, "Tree.selection", "ref[]");
        property(5962, "Tree.sortColumn", "ref");
        property(5963, "Tree.sortDirection", "int");
        property(5964, "Tree.topItem", "ref");
        property(5965, "Tree.compactRows", "bool");
        property(6016, "TreeColumn.alignment", "int");
        property(6017, "TreeColumn.moveable", "bool");
        property(6018, "TreeColumn.resizable", "bool");
        property(6019, "TreeColumn.width", "int");
        property(6020, "TreeColumn.toolTipText", "string");
        property(6080, "TreeEditor.column", "int");
        property(6081, "TreeEditor.editor", "ref");
        property(6082, "TreeEditor.item", "ref");
        property(6144, "TreeItem.background", "color");
        property(6145, "TreeItem.backgrounds", "color[]");
        property(6146, "TreeItem.checked", "bool");
        property(6147, "TreeItem.expanded", "bool");
        property(6148, "TreeItem.font", "object:VFont");
        property(6149, "TreeItem.fonts", "object:VFont[]");
        property(6150, "TreeItem.foreground", "color");
        property(6151, "TreeItem.foregrounds", "color[]");
        property(6152, "TreeItem.grayed", "bool");
        property(6153, "TreeItem.images", "image[]");
        property(6154, "TreeItem.itemCount", "int");
        property(6155, "TreeItem.items", "refs:TreeItem");
        property(6156, "TreeItem.paintedTexts", "int[]");
        property(6157, "TreeItem.texts", "string[]");
        property(6158, "TreeItem.paintedImages", "int[]");
        property(6208, "Widget.style", "int");
        property(6272, "CBanner.bottomLine", "int");
        property(6273, "CBanner.curveLine", "int[]");
        property(6336, "ViewForm.borderVisible", "bool");
        property(6337, "ViewForm.highlight", "int");
        property(6338, "ViewForm.separator", "int");
    }

    private static void property(int number, String name, String kind) {
        PROPERTIES[number] = name;
        KINDS[number] = kind;
    }

    /** The channel ``. */
    public static final int CHANNEL_SELF = 1;

    /** The channel `Arm/Arm`. */
    public static final int CHANNEL_ARM_ARM = 2;

    /** The channel `Authentication/authenticate`. */
    public static final int CHANNEL_AUTHENTICATION_AUTHENTICATE = 3;

    /** The channel `BidiSegment/lineGetSegments`. */
    public static final int CHANNEL_BIDI_SEGMENT_LINE_GET_SEGMENTS = 4;

    /** The channel `CTabFolder/itemClosed`. */
    public static final int CHANNEL_CTAB_FOLDER_ITEM_CLOSED = 5;

    /** The channel `CTabFolder/reorderItems`. */
    public static final int CHANNEL_CTAB_FOLDER_REORDER_ITEMS = 6;

    /** The channel `CTabFolder/stripLaidOut`. */
    public static final int CHANNEL_CTAB_FOLDER_STRIP_LAID_OUT = 7;

    /** The channel `CTabFolder2/close`. */
    public static final int CHANNEL_CTAB_FOLDER2_CLOSE = 8;

    /** The channel `CTabFolder2/maximize`. */
    public static final int CHANNEL_CTAB_FOLDER2_MAXIMIZE = 9;

    /** The channel `CTabFolder2/minimize`. */
    public static final int CHANNEL_CTAB_FOLDER2_MINIMIZE = 10;

    /** The channel `CTabFolder2/restore`. */
    public static final int CHANNEL_CTAB_FOLDER2_RESTORE = 11;

    /** The channel `CTabFolder2/showList`. */
    public static final int CHANNEL_CTAB_FOLDER2_SHOW_LIST = 12;

    /** The channel `Caret/caretMoved`. */
    public static final int CHANNEL_CARET_CARET_MOVED = 13;

    /** The channel `ClientReady`. */
    public static final int CHANNEL_CLIENT_READY = 14;

    /** The channel `Clipboard/contents`. */
    public static final int CHANNEL_CLIPBOARD_CONTENTS = 15;

    /** The channel `Clipboard/read`. */
    public static final int CHANNEL_CLIPBOARD_READ = 16;

    /** The channel `Clipboard/write`. */
    public static final int CHANNEL_CLIPBOARD_WRITE = 17;

    /** The channel `CloseWindow`. */
    public static final int CHANNEL_CLOSE_WINDOW = 18;

    /** The channel `CloseWindow/close`. */
    public static final int CHANNEL_CLOSE_WINDOW_CLOSE = 19;

    /** The channel `Control/Move`. */
    public static final int CHANNEL_CONTROL_MOVE = 20;

    /** The channel `Control/Resize`. */
    public static final int CHANNEL_CONTROL_RESIZE = 21;

    /** The channel `Dispose/Dispose`. */
    public static final int CHANNEL_DISPOSE_DISPOSE = 22;

    /** The channel `Drag/dragFinished`. */
    public static final int CHANNEL_DRAG_DRAG_FINISHED = 23;

    /** The channel `Drag/dragSetData`. */
    public static final int CHANNEL_DRAG_DRAG_SET_DATA = 24;

    /** The channel `Drag/dragStart`. */
    public static final int CHANNEL_DRAG_DRAG_START = 25;

    /** The channel `DragDetect/DragDetect`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_DETECT = 26;

    /** The channel `DragDetect/dragStartResult`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_START_RESULT = 27;

    /** The channel `Drop/dragEnter`. */
    public static final int CHANNEL_DROP_DRAG_ENTER = 28;

    /** The channel `Drop/dragLeave`. */
    public static final int CHANNEL_DROP_DRAG_LEAVE = 29;

    /** The channel `Drop/dragOperationChanged`. */
    public static final int CHANNEL_DROP_DRAG_OPERATION_CHANGED = 30;

    /** The channel `Drop/dragOver`. */
    public static final int CHANNEL_DROP_DRAG_OVER = 31;

    /** The channel `Drop/drop`. */
    public static final int CHANNEL_DROP_DROP = 32;

    /** The channel `Drop/dropAccept`. */
    public static final int CHANNEL_DROP_DROP_ACCEPT = 33;

    /** The channel `Drop/negotiationResult`. */
    public static final int CHANNEL_DROP_NEGOTIATION_RESULT = 34;

    /** The channel `Expand/Collapse`. */
    public static final int CHANNEL_EXPAND_COLLAPSE = 35;

    /** The channel `Expand/Expand`. */
    public static final int CHANNEL_EXPAND_EXPAND = 36;

    /** The channel `ExtendedModify/modifyText`. */
    public static final int CHANNEL_EXTENDED_MODIFY_MODIFY_TEXT = 37;

    /** The channel `Focus/FocusIn`. */
    public static final int CHANNEL_FOCUS_FOCUS_IN = 38;

    /** The channel `Focus/FocusOut`. */
    public static final int CHANNEL_FOCUS_FOCUS_OUT = 39;

    /** The channel `GC/create`. */
    public static final int CHANNEL_GC_CREATE = 40;

    /** The channel `GC/inFrame`. */
    public static final int CHANNEL_GC_IN_FRAME = 41;

    /** The channel `Gesture/Gesture`. */
    public static final int CHANNEL_GESTURE_GESTURE = 42;

    /** The channel `GetIdFromPoint`. */
    public static final int CHANNEL_GET_ID_FROM_POINT = 43;

    /** The channel `GetIdFromPointResponse`. */
    public static final int CHANNEL_GET_ID_FROM_POINT_RESPONSE = 44;

    /** The channel `GetItemBounds`. */
    public static final int CHANNEL_GET_ITEM_BOUNDS = 45;

    /** The channel `GetItemBoundsResponse`. */
    public static final int CHANNEL_GET_ITEM_BOUNDS_RESPONSE = 46;

    /** The channel `GetItemHeight`. */
    public static final int CHANNEL_GET_ITEM_HEIGHT = 47;

    /** The channel `GetItemHeightResponse`. */
    public static final int CHANNEL_GET_ITEM_HEIGHT_RESPONSE = 48;

    /** The channel `Help/Help`. */
    public static final int CHANNEL_HELP_HELP = 49;

    /** The channel `Image/forget`. */
    public static final int CHANNEL_IMAGE_FORGET = 50;

    /** The channel `Image/releaseRemoteRef`. */
    public static final int CHANNEL_IMAGE_RELEASE_REMOTE_REF = 51;

    /** The channel `Image/requestPixels`. */
    public static final int CHANNEL_IMAGE_REQUEST_PIXELS = 52;

    /** The channel `ImeComposition`. */
    public static final int CHANNEL_IME_COMPOSITION = 53;

    /** The channel `Key/KeyDown`. */
    public static final int CHANNEL_KEY_KEY_DOWN = 54;

    /** The channel `Key/KeyUp`. */
    public static final int CHANNEL_KEY_KEY_UP = 55;

    /** The channel `LineBackground/lineGetBackground`. */
    public static final int CHANNEL_LINE_BACKGROUND_LINE_GET_BACKGROUND = 56;

    /** The channel `LineStyle/lineGetStyle`. */
    public static final int CHANNEL_LINE_STYLE_LINE_GET_STYLE = 57;

    /** The channel `List/Visible`. */
    public static final int CHANNEL_LIST_VISIBLE = 58;

    /** The channel `Location/changed`. */
    public static final int CHANNEL_LOCATION_CHANGED = 59;

    /** The channel `Location/changing`. */
    public static final int CHANNEL_LOCATION_CHANGING = 60;

    /** The channel `Menu/Hide`. */
    public static final int CHANNEL_MENU_HIDE = 61;

    /** The channel `Menu/Show`. */
    public static final int CHANNEL_MENU_SHOW = 62;

    /** The channel `MenuDetect/MenuDetect`. */
    public static final int CHANNEL_MENU_DETECT_MENU_DETECT = 63;

    /** The channel `MenuKey`. */
    public static final int CHANNEL_MENU_KEY = 64;

    /** The channel `Modify/Modify`. */
    public static final int CHANNEL_MODIFY_MODIFY = 65;

    /** The channel `Mouse/MouseDoubleClick`. */
    public static final int CHANNEL_MOUSE_MOUSE_DOUBLE_CLICK = 66;

    /** The channel `Mouse/MouseDown`. */
    public static final int CHANNEL_MOUSE_MOUSE_DOWN = 67;

    /** The channel `Mouse/MouseUp`. */
    public static final int CHANNEL_MOUSE_MOUSE_UP = 68;

    /** The channel `MouseMove/MouseMove`. */
    public static final int CHANNEL_MOUSE_MOVE_MOUSE_MOVE = 69;

    /** The channel `MouseTrack/MouseEnter`. */
    public static final int CHANNEL_MOUSE_TRACK_MOUSE_ENTER = 70;

    /** The channel `MouseTrack/MouseExit`. */
    public static final int CHANNEL_MOUSE_TRACK_MOUSE_EXIT = 71;

    /** The channel `MouseTrack/MouseHover`. */
    public static final int CHANNEL_MOUSE_TRACK_MOUSE_HOVER = 72;

    /** The channel `MouseWheel/MouseWheel`. */
    public static final int CHANNEL_MOUSE_WHEEL_MOUSE_WHEEL = 73;

    /** The channel `OpenWindow`. */
    public static final int CHANNEL_OPEN_WINDOW = 74;

    /** The channel `OpenWindow/open`. */
    public static final int CHANNEL_OPEN_WINDOW_OPEN = 75;

    /** The channel `Paint/Paint`. */
    public static final int CHANNEL_PAINT_PAINT = 76;

    /** The channel `PaintItem/PaintItem`. */
    public static final int CHANNEL_PAINT_ITEM_PAINT_ITEM = 77;

    /** The channel `PaintObject/paintObject`. */
    public static final int CHANNEL_PAINT_OBJECT_PAINT_OBJECT = 78;

    /** The channel `Popups`. */
    public static final int CHANNEL_POPUPS = 79;

    /** The channel `Progress/changed`. */
    public static final int CHANNEL_PROGRESS_CHANGED = 80;

    /** The channel `Progress/completed`. */
    public static final int CHANNEL_PROGRESS_COMPLETED = 81;

    /** The channel `ResendGeometry`. */
    public static final int CHANNEL_RESEND_GEOMETRY = 82;

    /** The channel `ResendText`. */
    public static final int CHANNEL_RESEND_TEXT = 83;

    /** The channel `Scroll/Scroll`. */
    public static final int CHANNEL_SCROLL_SCROLL = 84;

    /** The channel `Segment/Segments`. */
    public static final int CHANNEL_SEGMENT_SEGMENTS = 85;

    /** The channel `Selection/DefaultSelection`. */
    public static final int CHANNEL_SELECTION_DEFAULT_SELECTION = 86;

    /** The channel `Selection/OpenMenu`. */
    public static final int CHANNEL_SELECTION_OPEN_MENU = 87;

    /** The channel `Selection/Selection`. */
    public static final int CHANNEL_SELECTION_SELECTION = 88;

    /** The channel `SetData/SetData`. */
    public static final int CHANNEL_SET_DATA_SET_DATA = 89;

    /** The channel `Shell/Activate`. */
    public static final int CHANNEL_SHELL_ACTIVATE = 90;

    /** The channel `Shell/Close`. */
    public static final int CHANNEL_SHELL_CLOSE = 91;

    /** The channel `Shell/Deactivate`. */
    public static final int CHANNEL_SHELL_DEACTIVATE = 92;

    /** The channel `Shell/Deiconify`. */
    public static final int CHANNEL_SHELL_DEICONIFY = 93;

    /** The channel `Shell/Iconify`. */
    public static final int CHANNEL_SHELL_ICONIFY = 94;

    /** The channel `Shell/SetBounds`. */
    public static final int CHANNEL_SHELL_SET_BOUNDS = 95;

    /** The channel `StatusText/changed`. */
    public static final int CHANNEL_STATUS_TEXT_CHANGED = 96;

    /** The channel `TextChange`. */
    public static final int CHANNEL_TEXT_CHANGE = 97;

    /** The channel `TextGeometry`. */
    public static final int CHANNEL_TEXT_GEOMETRY = 98;

    /** The channel `Title/changed`. */
    public static final int CHANNEL_TITLE_CHANGED = 99;

    /** The channel `Touch/Touch`. */
    public static final int CHANNEL_TOUCH_TOUCH = 100;

    /** The channel `Tracker/cancel`. */
    public static final int CHANNEL_TRACKER_CANCEL = 101;

    /** The channel `Tracker/close`. */
    public static final int CHANNEL_TRACKER_CLOSE = 102;

    /** The channel `Tracker/open`. */
    public static final int CHANNEL_TRACKER_OPEN = 103;

    /** The channel `Traverse/Traverse`. */
    public static final int CHANNEL_TRAVERSE_TRAVERSE = 104;

    /** The channel `Tree/Collapse`. */
    public static final int CHANNEL_TREE_COLLAPSE = 105;

    /** The channel `Tree/Expand`. */
    public static final int CHANNEL_TREE_EXPAND = 106;

    /** The channel `Verify/Verify`. */
    public static final int CHANNEL_VERIFY_VERIFY = 107;

    /** The channel `VerifyKey/verifyKey`. */
    public static final int CHANNEL_VERIFY_KEY_VERIFY_KEY = 108;

    /** The channel `VisibilityWindow/hide`. */
    public static final int CHANNEL_VISIBILITY_WINDOW_HIDE = 109;

    /** The channel `VisibilityWindow/show`. */
    public static final int CHANNEL_VISIBILITY_WINDOW_SHOW = 110;

    /** The channel `WinClose`. */
    public static final int CHANNEL_WIN_CLOSE = 111;

    /** The channel `WinMaximize`. */
    public static final int CHANNEL_WIN_MAXIMIZE = 112;

    /** The channel `WinMinimize`. */
    public static final int CHANNEL_WIN_MINIMIZE = 113;

    /** The channel `WinRestore`. */
    public static final int CHANNEL_WIN_RESTORE = 114;

    /** The channel `WinUnload`. */
    public static final int CHANNEL_WIN_UNLOAD = 115;

    /** The channel `WindowBounds`. */
    public static final int CHANNEL_WINDOW_BOUNDS = 116;

    /** The channel `WindowOpenFailed`. */
    public static final int CHANNEL_WINDOW_OPEN_FAILED = 117;

    /** The channel `WindowState`. */
    public static final int CHANNEL_WINDOW_STATE = 118;

    /** The channel `WindowTitle`. */
    public static final int CHANNEL_WINDOW_TITLE = 119;

    /** The channel `WordMovement/getNextOffset`. */
    public static final int CHANNEL_WORD_MOVEMENT_GET_NEXT_OFFSET = 120;

    /** The channel `WordMovement/getPreviousOffset`. */
    public static final int CHANNEL_WORD_MOVEMENT_GET_PREVIOUS_OFFSET = 121;

    /** The channel `activate`. */
    public static final int CHANNEL_ACTIVATE = 122;

    /** The channel `activation`. */
    public static final int CHANNEL_ACTIVATION = 123;

    /** The channel `b`. */
    public static final int CHANNEL_B = 124;

    /** The channel `back`. */
    public static final int CHANNEL_BACK = 125;

    /** The channel `browserFunctionCall`. */
    public static final int CHANNEL_BROWSER_FUNCTION_CALL = 132;

    /** The channel `c`. */
    public static final int CHANNEL_C = 133;

    /** The channel `close`. */
    public static final int CHANNEL_CLOSE = 134;

    /** The channel `cookie`. */
    public static final int CHANNEL_COOKIE = 135;

    /** The channel `copyAreaImageintint`. */
    public static final int CHANNEL_COPY_AREA_IMAGEINTINT = 136;

    /** The channel `copyAreaImageintintResponse`. */
    public static final int CHANNEL_COPY_AREA_IMAGEINTINT_RESPONSE = 137;

    /** The channel `copyAreaintintintintintintboolean`. */
    public static final int CHANNEL_COPY_AREAINTINTINTINTINTINTBOOLEAN = 138;

    /** The channel `drawArcintintintintintint`. */
    public static final int CHANNEL_DRAW_ARCINTINTINTINTINTINT = 139;

    /** The channel `drawFocusintintintint`. */
    public static final int CHANNEL_DRAW_FOCUSINTINTINTINT = 140;

    /** The channel `drawImageImageintint`. */
    public static final int CHANNEL_DRAW_IMAGE_IMAGEINTINT = 141;

    /** The channel `drawImageImageintintintint`. */
    public static final int CHANNEL_DRAW_IMAGE_IMAGEINTINTINTINT = 142;

    /** The channel `drawImageImageintintintintintintintint`. */
    public static final int CHANNEL_DRAW_IMAGE_IMAGEINTINTINTINTINTINTINTINT = 143;

    /** The channel `drawLineintintintint`. */
    public static final int CHANNEL_DRAW_LINEINTINTINTINT = 144;

    /** The channel `drawOvalintintintint`. */
    public static final int CHANNEL_DRAW_OVALINTINTINTINT = 145;

    /** The channel `drawPathPath`. */
    public static final int CHANNEL_DRAW_PATH_PATH = 146;

    /** The channel `drawPointintint`. */
    public static final int CHANNEL_DRAW_POINTINTINT = 147;

    /** The channel `drawPolygonint`. */
    public static final int CHANNEL_DRAW_POLYGONINT = 148;

    /** The channel `drawPolylineint`. */
    public static final int CHANNEL_DRAW_POLYLINEINT = 149;

    /** The channel `drawRectangleintintintint`. */
    public static final int CHANNEL_DRAW_RECTANGLEINTINTINTINT = 150;

    /** The channel `drawRoundRectangleintintintintintint`. */
    public static final int CHANNEL_DRAW_ROUND_RECTANGLEINTINTINTINTINTINT = 151;

    /** The channel `drawTextStringintintint`. */
    public static final int CHANNEL_DRAW_TEXT_STRINGINTINTINT = 152;

    /** The channel `evaluate`. */
    public static final int CHANNEL_EVALUATE = 153;

    /** The channel `evaluate/result`. */
    public static final int CHANNEL_EVALUATE_RESULT = 154;

    /** The channel `evolve.test.drag`. */
    public static final int CHANNEL_EVOLVE_TEST_DRAG = 155;

    /** The channel `evolve.test.dragDone`. */
    public static final int CHANNEL_EVOLVE_TEST_DRAG_DONE = 156;

    /** The channel `evolve.test.frameCost`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_COST = 157;

    /** The channel `evolve.test.frameCostResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_COST_RESPONSE = 158;

    /** The channel `evolve.test.frameStats`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_STATS = 159;

    /** The channel `evolve.test.frameStatsResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_STATS_RESPONSE = 160;

    /** The channel `evolve.test.frameSync`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_SYNC = 161;

    /** The channel `evolve.test.frameSynced`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_SYNCED = 162;

    /** The channel `evolve.test.iframeLoaded`. */
    public static final int CHANNEL_EVOLVE_TEST_IFRAME_LOADED = 163;

    /** The channel `evolve.test.query`. */
    public static final int CHANNEL_EVOLVE_TEST_QUERY = 164;

    /** The channel `evolve.test.queryResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_QUERY_RESPONSE = 165;

    /** The channel `evolve.test.styledTextPerf`. */
    public static final int CHANNEL_EVOLVE_TEST_STYLED_TEXT_PERF = 166;

    /** The channel `evolve.test.styledTextPerfResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_STYLED_TEXT_PERF_RESPONSE = 167;

    /** The channel `evolve.test.wireStats`. */
    public static final int CHANNEL_EVOLVE_TEST_WIRE_STATS = 168;

    /** The channel `evolve.test.wireStatsResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_WIRE_STATS_RESPONSE = 169;

    /** The channel `execute`. */
    public static final int CHANNEL_EXECUTE = 170;

    /** The channel `fillArcintintintintintint`. */
    public static final int CHANNEL_FILL_ARCINTINTINTINTINTINT = 171;

    /** The channel `fillGradientRectangleintintintintboolean`. */
    public static final int CHANNEL_FILL_GRADIENT_RECTANGLEINTINTINTINTBOOLEAN = 172;

    /** The channel `fillOvalintintintint`. */
    public static final int CHANNEL_FILL_OVALINTINTINTINT = 173;

    /** The channel `fillPathPath`. */
    public static final int CHANNEL_FILL_PATH_PATH = 174;

    /** The channel `fillPolygonint`. */
    public static final int CHANNEL_FILL_POLYGONINT = 175;

    /** The channel `fillRectangleintintintint`. */
    public static final int CHANNEL_FILL_RECTANGLEINTINTINTINT = 176;

    /** The channel `fillRoundRectangleintintintintintint`. */
    public static final int CHANNEL_FILL_ROUND_RECTANGLEINTINTINTINTINTINT = 177;

    /** The channel `fontSizeRequest`. */
    public static final int CHANNEL_FONT_SIZE_REQUEST = 178;

    /** The channel `fontSizeResponse`. */
    public static final int CHANNEL_FONT_SIZE_RESPONSE = 179;

    /** The channel `forward`. */
    public static final int CHANNEL_FORWARD = 180;

    /** The channel `gcDispose`. */
    public static final int CHANNEL_GC_DISPOSE = 181;

    /** The channel `getCookie`. */
    public static final int CHANNEL_GET_COOKIE = 182;

    /** The channel `imageInit`. */
    public static final int CHANNEL_IMAGE_INIT = 183;

    /** The channel `imageResult`. */
    public static final int CHANNEL_IMAGE_RESULT = 184;

    /** The channel `imageSizeRequest`. */
    public static final int CHANNEL_IMAGE_SIZE_REQUEST = 185;

    /** The channel `imageSizeResponse`. */
    public static final int CHANNEL_IMAGE_SIZE_RESPONSE = 186;

    /** The channel `imageSnapshotResult`. */
    public static final int CHANNEL_IMAGE_SNAPSHOT_RESULT = 187;

    /** The channel `key/verdict`. */
    public static final int CHANNEL_KEY_VERDICT = 188;

    /** The channel `key/vetoable`. */
    public static final int CHANNEL_KEY_VETOABLE = 189;

    /** The channel `locationchanging/result`. */
    public static final int CHANNEL_LOCATIONCHANGING_RESULT = 190;

    /** The channel `menu/verdict`. */
    public static final int CHANNEL_MENU_VERDICT = 191;

    /** The channel `modify/verdict`. */
    public static final int CHANNEL_MODIFY_VERDICT = 192;

    /** The channel `modify/vetoable`. */
    public static final int CHANNEL_MODIFY_VETOABLE = 193;

    /** The channel `navigate`. */
    public static final int CHANNEL_NAVIGATE = 194;

    /** The channel `navigateRequest`. */
    public static final int CHANNEL_NAVIGATE_REQUEST = 195;

    /** The channel `navigationState`. */
    public static final int CHANNEL_NAVIGATION_STATE = 196;

    /** The channel `nextFrame`. */
    public static final int CHANNEL_NEXT_FRAME = 197;

    /** The channel `openMenu`. */
    public static final int CHANNEL_OPEN_MENU = 198;

    /** The channel `pixelsResult`. */
    public static final int CHANNEL_PIXELS_RESULT = 199;

    /** The channel `reload`. */
    public static final int CHANNEL_RELOAD = 200;

    /** The channel `renderSnapshot`. */
    public static final int CHANNEL_RENDER_SNAPSHOT = 201;

    /** The channel `setCookie`. */
    public static final int CHANNEL_SET_COOKIE = 202;

    /** The channel `shown`. */
    public static final int CHANNEL_SHOWN = 203;

    /** The channel `stop`. */
    public static final int CHANNEL_STOP = 204;

    /** The channel `swing`. */
    public static final int CHANNEL_SWING = 205;

    /** The channel `swing.open`. */
    public static final int CHANNEL_SWING_OPEN = 206;

    /** The channel `swingIsland`. */
    public static final int CHANNEL_SWING_ISLAND = 207;

    /** The channel `swingIsland.close`. */
    public static final int CHANNEL_SWING_ISLAND_CLOSE = 208;

    /** The channel `swingIsland.dispose`. */
    public static final int CHANNEL_SWING_ISLAND_DISPOSE = 209;

    /** The channel `swingIsland.island`. */
    public static final int CHANNEL_SWING_ISLAND_ISLAND = 210;

    /** The channel `swingIsland.opened`. */
    public static final int CHANNEL_SWING_ISLAND_OPENED = 211;

    /** The channel `swingIsland.resize`. */
    public static final int CHANNEL_SWING_ISLAND_RESIZE = 212;

    /** The channel `swingIsland.surface`. */
    public static final int CHANNEL_SWING_ISLAND_SURFACE = 213;

    /** The channel `swingIsland.title`. */
    public static final int CHANNEL_SWING_ISLAND_TITLE = 214;

    /** The channel `swt.evolve.batch`. */
    public static final int CHANNEL_SWT_EVOLVE_BATCH = 215;

    /** The channel `swt.evolve.focus`. */
    public static final int CHANNEL_SWT_EVOLVE_FOCUS = 216;

    /** The channel `swt.evolve.focus.seen`. */
    public static final int CHANNEL_SWT_EVOLVE_FOCUS_SEEN = 217;

    /** The channel `swt.evolve.focusHolder`. */
    public static final int CHANNEL_SWT_EVOLVE_FOCUS_HOLDER = 218;

    /** The channel `swt.evolve.properties`. */
    public static final int CHANNEL_SWT_EVOLVE_PROPERTIES = 219;

    /** The channel `swt.evolve.test.runUi`. */
    public static final int CHANNEL_SWT_EVOLVE_TEST_RUN_UI = 221;

    /** The channel `swt.evolve.test.runUiResponse`. */
    public static final int CHANNEL_SWT_EVOLVE_TEST_RUN_UI_RESPONSE = 222;

    /** The channel `swt.evolve.timing.bare`. */
    public static final int CHANNEL_SWT_EVOLVE_TIMING_BARE = 223;

    /** The channel `swt.evolve.timing.ping`. */
    public static final int CHANNEL_SWT_EVOLVE_TIMING_PING = 224;

    /** The channel `swt.evolve.timing.pong`. */
    public static final int CHANNEL_SWT_EVOLVE_TIMING_PONG = 225;

    /** The channel `swt.evolve.url.open`. */
    public static final int CHANNEL_SWT_EVOLVE_URL_OPEN = 226;

    /** The channel `swt.evolve.widget.refresh`. */
    public static final int CHANNEL_SWT_EVOLVE_WIDGET_REFRESH = 227;

    /** The channel `swt.evolve.wire`. */
    public static final int CHANNEL_SWT_EVOLVE_WIRE = 228;

    /** The channel `traverse/verdict`. */
    public static final int CHANNEL_TRAVERSE_VERDICT = 229;

    /** The channel `traverse/vetoable`. */
    public static final int CHANNEL_TRAVERSE_VETOABLE = 230;

    /** The channel `v`. */
    public static final int CHANNEL_V = 231;

    /** The channel `widgetSizeRequest`. */
    public static final int CHANNEL_WIDGET_SIZE_REQUEST = 232;

    /** The channel `widgetSizeResponse`. */
    public static final int CHANNEL_WIDGET_SIZE_RESPONSE = 233;

    /** The channel `DnD/remoteDrag`. */
    public static final int CHANNEL_DN_D_REMOTE_DRAG = 234;

    /** The channel `DnD/remoteDropNone`. */
    public static final int CHANNEL_DN_D_REMOTE_DROP_NONE = 235;

    /** The channel `DragDetect/dragCancel`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_CANCEL = 236;

    /** The channel `DragDetect/dragImage`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_IMAGE = 237;

    /** The channel `DragDetect/dragMove`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_MOVE = 238;

    /** The channel `DragDetect/dragRelease`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_RELEASE = 239;

    /** The channel `DragDetect/remoteAnswer`. */
    public static final int CHANNEL_DRAG_DETECT_REMOTE_ANSWER = 240;

    /** The channel `WinDidEnterFullScreen`. */
    public static final int CHANNEL_WIN_DID_ENTER_FULL_SCREEN = 241;

    /** The channel `WinDidExitFullScreen`. */
    public static final int CHANNEL_WIN_DID_EXIT_FULL_SCREEN = 242;

    /** The channel `WinExitFullScreen`. */
    public static final int CHANNEL_WIN_EXIT_FULL_SCREEN = 243;

    /** The channel `WinFullScreen`. */
    public static final int CHANNEL_WIN_FULL_SCREEN = 244;

    /** The channel `WinFullScreenState`. */
    public static final int CHANNEL_WIN_FULL_SCREEN_STATE = 245;

    /** The channel `anim`. */
    public static final int CHANNEL_ANIM = 246;

    /** The channel `callback`. */
    public static final int CHANNEL_CALLBACK = 247;

    /** The channel `rebuild`. */
    public static final int CHANNEL_REBUILD = 248;

    /** The channel `subtree`. */
    public static final int CHANNEL_SUBTREE = 249;

    /** The channel `subtree/request`. */
    public static final int CHANNEL_SUBTREE_REQUEST = 250;

    /** The channel `transport`. */
    public static final int CHANNEL_TRANSPORT = 251;

    /** The channel `transport/request`. */
    public static final int CHANNEL_TRANSPORT_REQUEST = 252;

    /** The channel `evolve.test.bootTrace`. */
    public static final int CHANNEL_EVOLVE_TEST_BOOT_TRACE = 253;

    /** The channel `evolve.test.bootTraceResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_BOOT_TRACE_RESPONSE = 254;

    /** The channel `TabFolder/stripLaidOut`. */
    public static final int CHANNEL_TAB_FOLDER_STRIP_LAID_OUT = 255;

    /** The type `Accessible`. */
    public static final int TYPE_ACCESSIBLE = 1;

    /** The type `AnimatedProgress`. */
    public static final int TYPE_ANIMATED_PROGRESS = 2;

    /** The type `BorderData`. */
    public static final int TYPE_BORDER_DATA = 4;

    /** The type `Browser`. */
    public static final int TYPE_BROWSER = 5;

    /** The type `BrowserFunction`. */
    public static final int TYPE_BROWSER_FUNCTION = 6;

    /** The type `BusyIndicator`. */
    public static final int TYPE_BUSY_INDICATOR = 7;

    /** The type `Button`. */
    public static final int TYPE_BUTTON = 8;

    /** The type `ByteArrayTransfer`. */
    public static final int TYPE_BYTE_ARRAY_TRANSFER = 9;

    /** The type `CBanner`. */
    public static final int TYPE_CBANNER = 10;

    /** The type `CCombo`. */
    public static final int TYPE_CCOMBO = 11;

    /** The type `CLabel`. */
    public static final int TYPE_CLABEL = 12;

    /** The type `CTabFolder`. */
    public static final int TYPE_CTAB_FOLDER = 13;

    /** The type `CTabItem`. */
    public static final int TYPE_CTAB_ITEM = 14;

    /** The type `Canvas`. */
    public static final int TYPE_CANVAS = 15;

    /** The type `Caret`. */
    public static final int TYPE_CARET = 16;

    /** The type `Clipboard`. */
    public static final int TYPE_CLIPBOARD = 17;

    /** The type `Color`. */
    public static final int TYPE_COLOR = 18;

    /** The type `ColorDialog`. */
    public static final int TYPE_COLOR_DIALOG = 19;

    /** The type `Combo`. */
    public static final int TYPE_COMBO = 20;

    /** The type `Composite`. */
    public static final int TYPE_COMPOSITE = 21;

    /** The type `ControlEditor`. */
    public static final int TYPE_CONTROL_EDITOR = 22;

    /** The type `CoolBar`. */
    public static final int TYPE_COOL_BAR = 23;

    /** The type `CoolItem`. */
    public static final int TYPE_COOL_ITEM = 24;

    /** The type `Cursor`. */
    public static final int TYPE_CURSOR = 25;

    /** The type `DateTime`. */
    public static final int TYPE_DATE_TIME = 26;

    /** The type `Decorations`. */
    public static final int TYPE_DECORATIONS = 27;

    /** The type `Device`. */
    public static final int TYPE_DEVICE = 28;

    /** The type `Dialog`. */
    public static final int TYPE_DIALOG = 29;

    /** The type `DirectoryDialog`. */
    public static final int TYPE_DIRECTORY_DIALOG = 30;

    /** The type `Display`. */
    public static final int TYPE_DISPLAY = 31;

    /** The type `DragSource`. */
    public static final int TYPE_DRAG_SOURCE = 32;

    /** The type `DropTarget`. */
    public static final int TYPE_DROP_TARGET = 33;

    /** The type `Event`. */
    public static final int TYPE_EVENT = 34;

    /** The type `EwtWidget`. */
    public static final int TYPE_EWT_WIDGET = 35;

    /** The type `ExpandBar`. */
    public static final int TYPE_EXPAND_BAR = 36;

    /** The type `ExpandItem`. */
    public static final int TYPE_EXPAND_ITEM = 37;

    /** The type `FileDialog`. */
    public static final int TYPE_FILE_DIALOG = 38;

    /** The type `FileTransfer`. */
    public static final int TYPE_FILE_TRANSFER = 39;

    /** The type `Font`. */
    public static final int TYPE_FONT = 40;

    /** The type `FontData`. */
    public static final int TYPE_FONT_DATA = 41;

    /** The type `FontDialog`. */
    public static final int TYPE_FONT_DIALOG = 42;

    /** The type `FontMeasureBridge`. */
    public static final int TYPE_FONT_MEASURE_BRIDGE = 43;

    /** The type `FontMetrics`. */
    public static final int TYPE_FONT_METRICS = 44;

    /** The type `FormAttachment`. */
    public static final int TYPE_FORM_ATTACHMENT = 45;

    /** The type `FormData`. */
    public static final int TYPE_FORM_DATA = 46;

    /** The type `GC`. */
    public static final int TYPE_GC = 47;

    /** The type `GCData`. */
    public static final int TYPE_GCDATA = 48;

    /** The type `GLCanvas`. */
    public static final int TYPE_GLCANVAS = 49;

    /** The type `GLData`. */
    public static final int TYPE_GLDATA = 50;

    /** The type `GlyphMetrics`. */
    public static final int TYPE_GLYPH_METRICS = 51;

    /** The type `GridData`. */
    public static final int TYPE_GRID_DATA = 52;

    /** The type `Group`. */
    public static final int TYPE_GROUP = 53;

    /** The type `HTMLTransfer`. */
    public static final int TYPE_HTMLTRANSFER = 54;

    /** The type `IME`. */
    public static final int TYPE_IME = 55;

    /** The type `Image`. */
    public static final int TYPE_IMAGE = 56;

    /** The type `ImageData`. */
    public static final int TYPE_IMAGE_DATA = 57;

    /** The type `ImageSizeBridge`. */
    public static final int TYPE_IMAGE_SIZE_BRIDGE = 58;

    /** The type `ImageTransfer`. */
    public static final int TYPE_IMAGE_TRANSFER = 59;

    /** The type `Label`. */
    public static final int TYPE_LABEL = 60;

    /** The type `LineInfo`. */
    public static final int TYPE_LINE_INFO = 61;

    /** The type `LineSizeInfo`. */
    public static final int TYPE_LINE_SIZE_INFO = 62;

    /** The type `Link`. */
    public static final int TYPE_LINK = 63;

    /** The type `List`. */
    public static final int TYPE_LIST = 64;

    /** The type `MainComposite`. */
    public static final int TYPE_MAIN_COMPOSITE = 65;

    /** The type `MainToolbar`. */
    public static final int TYPE_MAIN_TOOLBAR = 66;

    /** The type `Menu`. */
    public static final int TYPE_MENU = 67;

    /** The type `MenuItem`. */
    public static final int TYPE_MENU_ITEM = 68;

    /** The type `MessageBox`. */
    public static final int TYPE_MESSAGE_BOX = 69;

    /** The type `Monitor`. */
    public static final int TYPE_MONITOR = 70;

    /** The type `PDFDocument`. */
    public static final int TYPE_PDFDOCUMENT = 71;

    /** The type `Path`. */
    public static final int TYPE_PATH = 72;

    /** The type `PathData`. */
    public static final int TYPE_PATH_DATA = 73;

    /** The type `Pattern`. */
    public static final int TYPE_PATTERN = 74;

    /** The type `Point`. */
    public static final int TYPE_POINT = 75;

    /** The type `PopupList`. */
    public static final int TYPE_POPUP_LIST = 76;

    /** The type `PrintDialog`. */
    public static final int TYPE_PRINT_DIALOG = 77;

    /** The type `Printer`. */
    public static final int TYPE_PRINTER = 78;

    /** The type `Program`. */
    public static final int TYPE_PROGRAM = 79;

    /** The type `ProgressBar`. */
    public static final int TYPE_PROGRESS_BAR = 80;

    /** The type `RGB`. */
    public static final int TYPE_RGB = 81;

    /** The type `RTFTransfer`. */
    public static final int TYPE_RTFTRANSFER = 82;

    /** The type `Rectangle`. */
    public static final int TYPE_RECTANGLE = 83;

    /** The type `Region`. */
    public static final int TYPE_REGION = 84;

    /** The type `Resource`. */
    public static final int TYPE_RESOURCE = 85;

    /** The type `RowData`. */
    public static final int TYPE_ROW_DATA = 86;

    /** The type `Sash`. */
    public static final int TYPE_SASH = 87;

    /** The type `SashForm`. */
    public static final int TYPE_SASH_FORM = 88;

    /** The type `Scale`. */
    public static final int TYPE_SCALE = 89;

    /** The type `ScrollBar`. */
    public static final int TYPE_SCROLL_BAR = 90;

    /** The type `ScrolledComposite`. */
    public static final int TYPE_SCROLLED_COMPOSITE = 91;

    /** The type `Shell`. */
    public static final int TYPE_SHELL = 92;

    /** The type `ShellPopups`. */
    public static final int TYPE_SHELL_POPUPS = 93;

    /** The type `SideBar`. */
    public static final int TYPE_SIDE_BAR = 94;

    /** The type `Slider`. */
    public static final int TYPE_SLIDER = 95;

    /** The type `Spinner`. */
    public static final int TYPE_SPINNER = 96;

    /** The type `StatusBar`. */
    public static final int TYPE_STATUS_BAR = 97;

    /** The type `StyleRange`. */
    public static final int TYPE_STYLE_RANGE = 98;

    /** The type `StyledText`. */
    public static final int TYPE_STYLED_TEXT = 99;

    /** The type `StyledTextPrintOptions`. */
    public static final int TYPE_STYLED_TEXT_PRINT_OPTIONS = 100;

    /** The type `StyledTextRenderer`. */
    public static final int TYPE_STYLED_TEXT_RENDERER = 101;

    /** The type `SwingIsland`. */
    public static final int TYPE_SWING_ISLAND = 102;

    /** The type `TabFolder`. */
    public static final int TYPE_TAB_FOLDER = 103;

    /** The type `TabItem`. */
    public static final int TYPE_TAB_ITEM = 104;

    /** The type `Table`. */
    public static final int TYPE_TABLE = 105;

    /** The type `TableColumn`. */
    public static final int TYPE_TABLE_COLUMN = 106;

    /** The type `TableCursor`. */
    public static final int TYPE_TABLE_CURSOR = 107;

    /** The type `TableEditor`. */
    public static final int TYPE_TABLE_EDITOR = 108;

    /** The type `TableItem`. */
    public static final int TYPE_TABLE_ITEM = 109;

    /** The type `TaskBar`. */
    public static final int TYPE_TASK_BAR = 110;

    /** The type `TaskItem`. */
    public static final int TYPE_TASK_ITEM = 111;

    /** The type `Text`. */
    public static final int TYPE_TEXT = 112;

    /** The type `TextStyle`. */
    public static final int TYPE_TEXT_STYLE = 113;

    /** The type `TextTransfer`. */
    public static final int TYPE_TEXT_TRANSFER = 114;

    /** The type `ToolBar`. */
    public static final int TYPE_TOOL_BAR = 115;

    /** The type `ToolItem`. */
    public static final int TYPE_TOOL_ITEM = 116;

    /** The type `ToolTip`. */
    public static final int TYPE_TOOL_TIP = 117;

    /** The type `Touch`. */
    public static final int TYPE_TOUCH = 118;

    /** The type `TouchSource`. */
    public static final int TYPE_TOUCH_SOURCE = 119;

    /** The type `Tracker`. */
    public static final int TYPE_TRACKER = 120;

    /** The type `Transfer`. */
    public static final int TYPE_TRANSFER = 121;

    /** The type `TransferData`. */
    public static final int TYPE_TRANSFER_DATA = 122;

    /** The type `Transform`. */
    public static final int TYPE_TRANSFORM = 123;

    /** The type `Tray`. */
    public static final int TYPE_TRAY = 124;

    /** The type `TrayItem`. */
    public static final int TYPE_TRAY_ITEM = 125;

    /** The type `Tree`. */
    public static final int TYPE_TREE = 126;

    /** The type `TreeColumn`. */
    public static final int TYPE_TREE_COLUMN = 127;

    /** The type `TreeCursor`. */
    public static final int TYPE_TREE_CURSOR = 128;

    /** The type `TreeEditor`. */
    public static final int TYPE_TREE_EDITOR = 129;

    /** The type `TreeItem`. */
    public static final int TYPE_TREE_ITEM = 130;

    /** The type `URLTransfer`. */
    public static final int TYPE_URLTRANSFER = 131;

    /** The type `ViewForm`. */
    public static final int TYPE_VIEW_FORM = 132;

    /** The type `WidgetSizeBridge`. */
    public static final int TYPE_WIDGET_SIZE_BRIDGE = 133;

    /** The type `DisplayPopups`. */
    public static final int TYPE_DISPLAY_POPUPS = 134;

    /** The display's `activeShellId`. */
    public static final int DISPLAY_ACTIVE_SHELL_ID = 1792;

    /** The display's `config`. */
    public static final int DISPLAY_CONFIG = 1793;

    /** The display's `focusHolderId`. */
    public static final int DISPLAY_FOCUS_HOLDER_ID = 1794;

    /** The display's `mainShellId`. */
    public static final int DISPLAY_MAIN_SHELL_ID = 1795;

    /** The display's `popups`. */
    public static final int DISPLAY_POPUPS = 1796;

    /** The display's `shells`. */
    public static final int DISPLAY_SHELLS = 1797;

    /** The display's `systemMenu`. */
    public static final int DISPLAY_SYSTEM_MENU = 1798;

    /** The display's `tooltips`. */
    public static final int DISPLAY_TOOLTIPS = 1799;

    /** The display's `windowedShellIds`. */
    public static final int DISPLAY_WINDOWED_SHELL_IDS = 1800;

    /** The flag `assets_path`. */
    public static final int CONFIG_FLAGS_ASSETS_PATH = 1088;

    /** The flag `csd_maximize`. */
    public static final int CONFIG_FLAGS_CSD_MAXIMIZE = 1089;

    /** The flag `csd_os`. */
    public static final int CONFIG_FLAGS_CSD_OS = 1090;

    /** The flag `csd_placement`. */
    public static final int CONFIG_FLAGS_CSD_PLACEMENT = 1091;

    /** The flag `csd_titlebar_color`. */
    public static final int CONFIG_FLAGS_CSD_TITLEBAR_COLOR = 1092;

    /** The flag `ctabfolder_tab_scroll`. */
    public static final int CONFIG_FLAGS_CTABFOLDER_TAB_SCROLL = 1093;

    /** The flag `ctabfolder_topright_auto_hide`. */
    public static final int CONFIG_FLAGS_CTABFOLDER_TOPRIGHT_AUTO_HIDE = 1094;

    /** The flag `ctabfolder_visible_controls`. */
    public static final int CONFIG_FLAGS_CTABFOLDER_VISIBLE_CONTROLS = 1095;

    /** The flag `custom_tooltip`. */
    public static final int CONFIG_FLAGS_CUSTOM_TOOLTIP = 1096;

    /** The flag `decimal_separator`. */
    public static final int CONFIG_FLAGS_DECIMAL_SEPARATOR = 1097;

    /** The flag `decorations_align`. */
    public static final int CONFIG_FLAGS_DECORATIONS_ALIGN = 1098;

    /** The flag `disable_control_gc_overlay`. */
    public static final int CONFIG_FLAGS_DISABLE_CONTROL_GC_OVERLAY = 1099;

    /** The flag `disable_evolve_icons`. */
    public static final int CONFIG_FLAGS_DISABLE_EVOLVE_ICONS = 1100;

    /** The flag `disable_hover_zoom`. */
    public static final int CONFIG_FLAGS_DISABLE_HOVER_ZOOM = 1101;

    /** The flag `disable_swt_canvas_colors`. */
    public static final int CONFIG_FLAGS_DISABLE_SWT_CANVAS_COLORS = 1102;

    /** The flag `double_click_timeout_ms`. */
    public static final int CONFIG_FLAGS_DOUBLE_CLICK_TIMEOUT_MS = 1103;

    /** The flag `focus_indicators`. */
    public static final int CONFIG_FLAGS_FOCUS_INDICATORS = 1104;

    /** The flag `font_point_scale`. */
    public static final int CONFIG_FLAGS_FONT_POINT_SCALE = 1105;

    /** The flag `force_theme`. */
    public static final int CONFIG_FLAGS_FORCE_THEME = 1106;

    /** The flag `gc_icons_replacement`. */
    public static final int CONFIG_FLAGS_GC_ICONS_REPLACEMENT = 1107;

    /** The flag `hover_delay_ms`. */
    public static final int CONFIG_FLAGS_HOVER_DELAY_MS = 1108;

    /** The flag `image_disable_icons_replacement`. */
    public static final int CONFIG_FLAGS_IMAGE_DISABLE_ICONS_REPLACEMENT = 1109;

    /** The flag `locale`. */
    public static final int CONFIG_FLAGS_LOCALE = 1110;

    /** The flag `notification_popup`. */
    public static final int CONFIG_FLAGS_NOTIFICATION_POPUP = 1111;

    /** The flag `preserve_icon_colors`. */
    public static final int CONFIG_FLAGS_PRESERVE_ICON_COLORS = 1112;

    /** The flag `print_move`. */
    public static final int CONFIG_FLAGS_PRINT_MOVE = 1113;

    /** The flag `push_button_margin`. */
    public static final int CONFIG_FLAGS_PUSH_BUTTON_MARGIN = 1114;

    /** The flag `show_scaling_control`. */
    public static final int CONFIG_FLAGS_SHOW_SCALING_CONTROL = 1115;

    /** The flag `show_theme_color_palette`. */
    public static final int CONFIG_FLAGS_SHOW_THEME_COLOR_PALETTE = 1116;

    /** The flag `system_menu_bar`. */
    public static final int CONFIG_FLAGS_SYSTEM_MENU_BAR = 1117;

    /** The flag `theme_color`. */
    public static final int CONFIG_FLAGS_THEME_COLOR = 1118;

    /** The flag `theme_colors_by_widget`. */
    public static final int CONFIG_FLAGS_THEME_COLORS_BY_WIDGET = 1119;

    /** The flag `theme_name`. */
    public static final int CONFIG_FLAGS_THEME_NAME = 1120;

    /** The flag `transparent_menu_check_box`. */
    public static final int CONFIG_FLAGS_TRANSPARENT_MENU_CHECK_BOX = 1121;

    /** The flag `ui_zoom`. */
    public static final int CONFIG_FLAGS_UI_ZOOM = 1122;

    /** The flag `use_special_dropdown_button`. */
    public static final int CONFIG_FLAGS_USE_SPECIAL_DROPDOWN_BUTTON = 1123;

    /** The flag `use_swt_border`. */
    public static final int CONFIG_FLAGS_USE_SWT_BORDER = 1124;

    /** The flag `use_swt_colors`. */
    public static final int CONFIG_FLAGS_USE_SWT_COLORS = 1125;

    /** The flag `use_swt_font_colors`. */
    public static final int CONFIG_FLAGS_USE_SWT_FONT_COLORS = 1126;

    /** The flag `use_swt_font_colors_by_widget`. */
    public static final int CONFIG_FLAGS_USE_SWT_FONT_COLORS_BY_WIDGET = 1127;

    /** The flag `use_swt_fonts`. */
    public static final int CONFIG_FLAGS_USE_SWT_FONTS = 1128;

    /** The flag `bigger_icon_size`. */
    public static final int CONFIG_FLAGS_BIGGER_ICON_SIZE = 1129;

    /** The flag `scale_vertical_minimum_at_top`. */
    public static final int CONFIG_FLAGS_SCALE_VERTICAL_MINIMUM_AT_TOP = 1130;

    /** The flag `ctabfolder_chevron_after_tabs`. */
    public static final int CONFIG_FLAGS_CTABFOLDER_CHEVRON_AFTER_TABS = 1131;

    /** The flag `regional_locale`. */
    public static final int CONFIG_FLAGS_REGIONAL_LOCALE = 1132;

    /** The event's `button`. */
    public static final int EVENT_BUTTON = 1856;

    /** The event's `character`. */
    public static final int EVENT_CHARACTER = 1857;

    /** The event's `count`. */
    public static final int EVENT_COUNT = 1858;

    /** The event's `currentDataTypeId`. */
    public static final int EVENT_CURRENT_DATA_TYPE_ID = 1859;

    /** The event's `detail`. */
    public static final int EVENT_DETAIL = 1860;

    /** The event's `doit`. */
    public static final int EVENT_DOIT = 1861;

    /** The event's `dragImage`. */
    public static final int EVENT_DRAG_IMAGE = 1862;

    /** The event's `end`. */
    public static final int EVENT_END = 1863;

    /** The event's `feedback`. */
    public static final int EVENT_FEEDBACK = 1864;

    /** The event's `gc`. */
    public static final int EVENT_GC = 1865;

    /** The event's `height`. */
    public static final int EVENT_HEIGHT = 1866;

    /** The event's `index`. */
    public static final int EVENT_INDEX = 1867;

    /** The event's `item`. */
    public static final int EVENT_ITEM = 1868;

    /** The event's `itemId`. */
    public static final int EVENT_ITEM_ID = 1869;

    /** The event's `keyCode`. */
    public static final int EVENT_KEY_CODE = 1870;

    /** The event's `keyLocation`. */
    public static final int EVENT_KEY_LOCATION = 1871;

    /** The event's `magnification`. */
    public static final int EVENT_MAGNIFICATION = 1872;

    /** The event's `operations`. */
    public static final int EVENT_OPERATIONS = 1873;

    /** The event's `rotation`. */
    public static final int EVENT_ROTATION = 1874;

    /** The event's `segments`. */
    public static final int EVENT_SEGMENTS = 1875;

    /** The event's `segmentsChars`. */
    public static final int EVENT_SEGMENTS_CHARS = 1876;

    /** The event's `start`. */
    public static final int EVENT_START = 1877;

    /** The event's `stateMask`. */
    public static final int EVENT_STATE_MASK = 1878;

    /** The event's `text`. */
    public static final int EVENT_TEXT = 1879;

    /** The event's `time`. */
    public static final int EVENT_TIME = 1880;

    /** The event's `touches`. */
    public static final int EVENT_TOUCHES = 1881;

    /** The event's `type`. */
    public static final int EVENT_TYPE = 1882;

    /** The event's `widget`. */
    public static final int EVENT_WIDGET = 1883;

    /** The event's `width`. */
    public static final int EVENT_WIDTH = 1884;

    /** The event's `x`. */
    public static final int EVENT_X = 1885;

    /** The event's `xDirection`. */
    public static final int EVENT_X_DIRECTION = 1886;

    /** The event's `y`. */
    public static final int EVENT_Y = 1887;

    /** The event's `yDirection`. */
    public static final int EVENT_Y_DIRECTION = 1888;
}
