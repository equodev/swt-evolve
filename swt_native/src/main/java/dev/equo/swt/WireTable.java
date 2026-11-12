package dev.equo.swt;

/**
 * The numbers the binary wire names things by, generated from the client's wire model: channels,
 * record types, map keys and properties, each by number, and the numbers code written by hand names.
 */
public final class WireTable {

    private WireTable() {
    }

    /** The table's fingerprint, which the client compares with its own when it connects. */
    public static final int FINGERPRINT = -243880975;

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
            "DnD/remoteDrag",
            "DnD/remoteDropNone",
            "Drag/dragFinished",
            "Drag/dragSetData",
            "Drag/dragStart",
            "DragDetect/DragDetect",
            "DragDetect/dragCancel",
            "DragDetect/dragImage",
            "DragDetect/dragMove",
            "DragDetect/dragRelease",
            "DragDetect/dragStartResult",
            "DragDetect/remoteAnswer",
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
            "WinDidEnterFullScreen",
            "WinDidExitFullScreen",
            "WinExitFullScreen",
            "WinFullScreen",
            "WinFullScreenState",
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
            "anim",
            "b",
            "back",
            "bench/d2j/probe",
            "bench/d2j/probeResponse",
            "bench/d2j/result",
            "bench/d2j/run",
            "bench/j2d/echo",
            "bench/j2d/echoResponse",
            "browserFunctionCall",
            "c",
            "callback",
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
            "evolve.test.bootTrace",
            "evolve.test.bootTraceResponse",
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
            "rebuild",
            "reload",
            "renderSnapshot",
            "setCookie",
            "shown",
            "stop",
            "subtree",
            "subtree/request",
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
            "swt.evolve.property.set",
            "swt.evolve.test.runUi",
            "swt.evolve.test.runUiResponse",
            "swt.evolve.timing.bare",
            "swt.evolve.timing.ping",
            "swt.evolve.timing.pong",
            "swt.evolve.url.open",
            "swt.evolve.widget.refresh",
            "swt.evolve.wire",
            "transport",
            "transport/request",
            "traverse/verdict",
            "traverse/vetoable",
            "v",
            "widgetSizeRequest",
            "widgetSizeResponse",
    };

    /** Record type names by number. */
    public static final String[] TYPES = {
            null,
            "Accessible",
            "AnimatedProgress",
            "BenchBridge",
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
            "DisplayPopups",
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
            "bigger_icon_size",
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
            "bufferScaleY",
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
            "dirtyIndicatorStyle",
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
            "paintedImages",
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
            "scale_vertical_minimum_at_top",
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
            "showDirty",
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
            "stripLayoutSeq",
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
    };

    /** Properties as `Class.name`, by number. */
    public static final String[] PROPERTIES = {
            null,
            "Accessible.control",
            "Accessible.focus",
            "BorderData.hHint",
            "BorderData.region",
            "BorderData.wHint",
            "Browser.functionNames",
            "Browser.javascriptEnabled",
            "Browser.text",
            "Browser.url",
            "BrowserFunction.browser",
            "BrowserFunction.name",
            "Button.alignment",
            "Button.grayed",
            "Button.image",
            "Button.primary",
            "Button.selection",
            "Button.text",
            "CCombo.alignment",
            "CCombo.editable",
            "CCombo.itemTooltips",
            "CCombo.items",
            "CCombo.listVisible",
            "CCombo.selection",
            "CCombo.text",
            "CCombo.textLimit",
            "CLabel.alignment",
            "CLabel.backgroundImage",
            "CLabel.bottomMargin",
            "CLabel.gradientColors",
            "CLabel.gradientPercents",
            "CLabel.gradientVertical",
            "CLabel.image",
            "CLabel.leftMargin",
            "CLabel.rightMargin",
            "CLabel.text",
            "CLabel.topMargin",
            "CTabFolder.borderVisible",
            "CTabFolder.chevronVisible",
            "CTabFolder.dirtyIndicatorStyle",
            "CTabFolder.highlight",
            "CTabFolder.highlightEnabled",
            "CTabFolder.items",
            "CTabFolder.maximizeVisible",
            "CTabFolder.maximized",
            "CTabFolder.minimizeVisible",
            "CTabFolder.minimized",
            "CTabFolder.minimumCharacters",
            "CTabFolder.selectedImageVisible",
            "CTabFolder.selection",
            "CTabFolder.selectionBackground",
            "CTabFolder.selectionBarThickness",
            "CTabFolder.selectionBgImage",
            "CTabFolder.selectionForeground",
            "CTabFolder.showChevron",
            "CTabFolder.showListPopupSeq",
            "CTabFolder.single",
            "CTabFolder.stripLayoutSeq",
            "CTabFolder.tabHeight",
            "CTabFolder.tabPosition",
            "CTabFolder.topRight",
            "CTabFolder.topRightAlignment",
            "CTabFolder.unselectedCloseVisible",
            "CTabFolder.unselectedImageVisible",
            "CTabItem.control",
            "CTabItem.font",
            "CTabItem.foreground",
            "CTabItem.selectionForeground",
            "CTabItem.showClose",
            "CTabItem.showDirty",
            "CTabItem.showing",
            "CTabItem.toolTipText",
            "Canvas.caret",
            "Caret.blinkRate",
            "Caret.bounds",
            "Caret.focused",
            "Caret.image",
            "Caret.visible",
            "Clipboard.availableTypeNames",
            "Color.a",
            "Color.b",
            "Color.g",
            "Color.r",
            "ColorDialog.RGB",
            "ColorDialog.RGBs",
            "Combo.caretPosition",
            "Combo.items",
            "Combo.listVisible",
            "Combo.text",
            "Combo.textLimit",
            "Composite.backgroundMode",
            "Composite.children",
            "ConfigFlags.assets_path",
            "ConfigFlags.bigger_icon_size",
            "ConfigFlags.csd_maximize",
            "ConfigFlags.csd_os",
            "ConfigFlags.csd_placement",
            "ConfigFlags.csd_titlebar_color",
            "ConfigFlags.ctabfolder_tab_scroll",
            "ConfigFlags.ctabfolder_topright_auto_hide",
            "ConfigFlags.ctabfolder_visible_controls",
            "ConfigFlags.custom_tooltip",
            "ConfigFlags.decimal_separator",
            "ConfigFlags.decorations_align",
            "ConfigFlags.disable_control_gc_overlay",
            "ConfigFlags.disable_evolve_icons",
            "ConfigFlags.disable_hover_zoom",
            "ConfigFlags.disable_swt_canvas_colors",
            "ConfigFlags.double_click_timeout_ms",
            "ConfigFlags.focus_indicators",
            "ConfigFlags.font_point_scale",
            "ConfigFlags.force_theme",
            "ConfigFlags.gc_icons_replacement",
            "ConfigFlags.hover_delay_ms",
            "ConfigFlags.image_disable_icons_replacement",
            "ConfigFlags.locale",
            "ConfigFlags.notification_popup",
            "ConfigFlags.preserve_icon_colors",
            "ConfigFlags.print_move",
            "ConfigFlags.push_button_margin",
            "ConfigFlags.scale_vertical_minimum_at_top",
            "ConfigFlags.show_scaling_control",
            "ConfigFlags.show_theme_color_palette",
            "ConfigFlags.system_menu_bar",
            "ConfigFlags.theme_color",
            "ConfigFlags.theme_colors_by_widget",
            "ConfigFlags.theme_name",
            "ConfigFlags.transparent_menu_check_box",
            "ConfigFlags.ui_zoom",
            "ConfigFlags.use_special_dropdown_button",
            "ConfigFlags.use_swt_border",
            "ConfigFlags.use_swt_colors",
            "ConfigFlags.use_swt_font_colors",
            "ConfigFlags.use_swt_font_colors_by_widget",
            "ConfigFlags.use_swt_fonts",
            "Control.background",
            "Control.backgroundImage",
            "Control.bounds",
            "Control.cursor",
            "Control.dragDetect",
            "Control.dragSource",
            "Control.dragSourceId",
            "Control.dropTargetId",
            "Control.enabled",
            "Control.font",
            "Control.foreground",
            "Control.hasOwnBackground",
            "Control.inheritsBackground",
            "Control.menu",
            "Control.region",
            "Control.tabStop",
            "Control.toolTipText",
            "Control.visible",
            "ControlEditor.editor",
            "CoolBar.itemOrder",
            "CoolBar.itemSizes",
            "CoolBar.items",
            "CoolBar.wrapIndices",
            "CoolItem.control",
            "CoolItem.preferredSize",
            "Cursor.cursorStyle",
            "Cursor.hotspotX",
            "Cursor.hotspotY",
            "Cursor.image",
            "DateTime.day",
            "DateTime.hours",
            "DateTime.minutes",
            "DateTime.month",
            "DateTime.seconds",
            "DateTime.year",
            "Decorations.menuBar",
            "Decorations.menuStripHeight",
            "Decorations.ownMenuStrip",
            "Decorations.text",
            "Device.fontList",
            "Device.tracking",
            "Device.warnings",
            "Dialog.id",
            "Dialog.style",
            "Dialog.swt",
            "Dialog.text",
            "DirectoryDialog.filterPath",
            "DirectoryDialog.message",
            "Display.activeShellId",
            "Display.config",
            "Display.focusHolderId",
            "Display.mainShellId",
            "Display.popups",
            "Display.shells",
            "Display.systemMenu",
            "Display.tooltips",
            "Display.windowedShellIds",
            "Event.button",
            "Event.character",
            "Event.count",
            "Event.currentDataTypeId",
            "Event.detail",
            "Event.doit",
            "Event.dragImage",
            "Event.end",
            "Event.feedback",
            "Event.gc",
            "Event.height",
            "Event.index",
            "Event.item",
            "Event.itemId",
            "Event.keyCode",
            "Event.keyLocation",
            "Event.magnification",
            "Event.operations",
            "Event.rotation",
            "Event.segments",
            "Event.segmentsChars",
            "Event.start",
            "Event.stateMask",
            "Event.text",
            "Event.time",
            "Event.touches",
            "Event.type",
            "Event.widget",
            "Event.width",
            "Event.x",
            "Event.xDirection",
            "Event.y",
            "Event.yDirection",
            "ExpandBar.items",
            "ExpandBar.spacing",
            "ExpandItem.control",
            "ExpandItem.expanded",
            "ExpandItem.headerHeight",
            "ExpandItem.height",
            "FileDialog.fileName",
            "FileDialog.fileNames",
            "FileDialog.filterExtensions",
            "FileDialog.filterIndex",
            "FileDialog.filterNames",
            "FileDialog.filterPath",
            "FileDialog.overwrite",
            "Font.fontData",
            "FontData.height",
            "FontData.name",
            "FontData.style",
            "FontDialog.RGB",
            "FontDialog.effectsVisible",
            "FontDialog.fontData",
            "FontDialog.fontList",
            "FormAttachment.alignment",
            "FormAttachment.control",
            "FormAttachment.denominator",
            "FormAttachment.numerator",
            "FormAttachment.offset",
            "FormData.bottom",
            "FormData.cacheBottom",
            "FormData.cacheHeight",
            "FormData.cacheLeft",
            "FormData.cacheRight",
            "FormData.cacheTop",
            "FormData.cacheWidth",
            "FormData.currentHeight",
            "FormData.currentHhint",
            "FormData.currentWhint",
            "FormData.currentWidth",
            "FormData.defaultHeight",
            "FormData.defaultHhint",
            "FormData.defaultWhint",
            "FormData.defaultWidth",
            "FormData.height",
            "FormData.isVisited",
            "FormData.left",
            "FormData.needed",
            "FormData.right",
            "FormData.top",
            "FormData.width",
            "GC.XORMode",
            "GC.alpha",
            "GC.antialias",
            "GC.background",
            "GC.backgroundPattern",
            "GC.bufferScale",
            "GC.bufferScaleY",
            "GC.clipping",
            "GC.clippingPath",
            "GC.clippingRects",
            "GC.clippingText",
            "GC.fillRule",
            "GC.font",
            "GC.foreground",
            "GC.foregroundPattern",
            "GC.hoverFills",
            "GC.interpolation",
            "GC.lineCap",
            "GC.lineDash",
            "GC.lineDashOffset",
            "GC.lineJoin",
            "GC.lineStyle",
            "GC.lineWidth",
            "GC.selectionFills",
            "GC.style",
            "GC.themedCanvas",
            "GC.transform",
            "GLData.accumAlphaSize",
            "GLData.accumBlueSize",
            "GLData.accumGreenSize",
            "GLData.accumRedSize",
            "GLData.alphaSize",
            "GLData.blueSize",
            "GLData.depthSize",
            "GLData.doubleBuffer",
            "GLData.greenSize",
            "GLData.redSize",
            "GLData.sampleBuffers",
            "GLData.samples",
            "GLData.shareContext",
            "GLData.stencilSize",
            "GLData.stereo",
            "GlyphMetrics.ascent",
            "GlyphMetrics.descent",
            "GlyphMetrics.width",
            "GridData.cacheHeight",
            "GridData.cacheWidth",
            "GridData.currentHeight",
            "GridData.currentHhint",
            "GridData.currentWhint",
            "GridData.currentWidth",
            "GridData.defaultHeight",
            "GridData.defaultHhint",
            "GridData.defaultWhint",
            "GridData.defaultWidth",
            "GridData.exclude",
            "GridData.grabExcessHorizontalSpace",
            "GridData.grabExcessVerticalSpace",
            "GridData.heightHint",
            "GridData.horizontalAlignment",
            "GridData.horizontalIndent",
            "GridData.horizontalSpan",
            "GridData.minimumHeight",
            "GridData.minimumWidth",
            "GridData.verticalAlignment",
            "GridData.verticalIndent",
            "GridData.verticalSpan",
            "GridData.widthHint",
            "Group.text",
            "Image.filename",
            "Image.height",
            "Image.imageData",
            "Image.remoteRef",
            "Image.svgContent",
            "Image.width",
            "ImageData.data",
            "ImageData.depth",
            "ImageData.height",
            "ImageData.width",
            "Item.image",
            "Item.text",
            "Label.image",
            "Label.text",
            "LineInfo.alignment",
            "LineInfo.background",
            "LineInfo.flags",
            "LineInfo.indent",
            "LineInfo.justify",
            "LineInfo.segments",
            "LineInfo.segmentsChars",
            "LineInfo.tabStops",
            "LineInfo.verticalIndent",
            "LineInfo.wrapIndent",
            "LineSizeInfo.height",
            "LineSizeInfo.width",
            "Link.linkForeground",
            "Link.text",
            "List.items",
            "List.selection",
            "List.topIndex",
            "Menu.enabled",
            "Menu.items",
            "Menu.location",
            "Menu.orientation",
            "Menu.visible",
            "MenuItem.accelerator",
            "MenuItem.enabled",
            "MenuItem.menu",
            "MenuItem.selection",
            "MenuItem.toolTipText",
            "MessageBox.buttonLabels",
            "MessageBox.message",
            "Monitor.clientHeight",
            "Monitor.clientWidth",
            "Monitor.clientX",
            "Monitor.clientY",
            "Monitor.handle",
            "Monitor.height",
            "Monitor.width",
            "Monitor.x",
            "Monitor.y",
            "Monitor.zoom",
            "PDFDocument.height",
            "PDFDocument.width",
            "Path.pathData",
            "Path.textFonts",
            "Path.textOrigins",
            "Path.textStrings",
            "PathData.points",
            "PathData.types",
            "Pattern.color1",
            "Pattern.color2",
            "Pattern.endX",
            "Pattern.endY",
            "Pattern.image",
            "Pattern.startX",
            "Pattern.startY",
            "Point.x",
            "Point.y",
            "PopupList.font",
            "PopupList.items",
            "PopupList.minimumWidth",
            "PopupList.selection",
            "PrintDialog.endPage",
            "PrintDialog.printToFile",
            "PrintDialog.scope",
            "PrintDialog.startPage",
            "ProgressBar.maximum",
            "ProgressBar.minimum",
            "ProgressBar.selection",
            "RGB.blue",
            "RGB.green",
            "RGB.red",
            "Rectangle.height",
            "Rectangle.width",
            "Rectangle.x",
            "Rectangle.y",
            "Region.rects",
            "RowData.exclude",
            "RowData.height",
            "RowData.width",
            "SashForm.maximizedControl",
            "SashForm.sashWidth",
            "SashForm.weights",
            "Scale.maximum",
            "Scale.minimum",
            "Scale.selection",
            "ScrollBar.enabled",
            "ScrollBar.increment",
            "ScrollBar.maximum",
            "ScrollBar.minimum",
            "ScrollBar.pageIncrement",
            "ScrollBar.selection",
            "ScrollBar.thumb",
            "ScrollBar.visible",
            "Scrollable.horizontalBar",
            "Scrollable.verticalBar",
            "ScrolledComposite.alwaysShowScrollBars",
            "ScrolledComposite.content",
            "ScrolledComposite.expandHorizontal",
            "ScrolledComposite.expandVertical",
            "ScrolledComposite.minHeight",
            "ScrolledComposite.minWidth",
            "ScrolledComposite.origin",
            "ScrolledComposite.originRequest",
            "Shell.alpha",
            "Shell.dialogs",
            "Shell.fullScreen",
            "Shell.maximumSize",
            "Shell.minimumSize",
            "Shell.modified",
            "Slider.increment",
            "Slider.maximum",
            "Slider.minimum",
            "Slider.pageIncrement",
            "Slider.selection",
            "Slider.thumb",
            "Spinner.digits",
            "Spinner.increment",
            "Spinner.keptText",
            "Spinner.maximum",
            "Spinner.minimum",
            "Spinner.pageIncrement",
            "Spinner.selection",
            "Spinner.textLimit",
            "StyleRange.fontStyle",
            "StyleRange.length",
            "StyleRange.start",
            "StyledText.alignment",
            "StyledText.alwaysShowScrollBars",
            "StyledText.blockSelection",
            "StyledText.blockSelectionBounds",
            "StyledText.bottomMargin",
            "StyledText.caretOffset",
            "StyledText.caretOffsets",
            "StyledText.doubleClickEnabled",
            "StyledText.editable",
            "StyledText.fixedLineHeight",
            "StyledText.horizontalPixel",
            "StyledText.indent",
            "StyledText.justify",
            "StyledText.leftMargin",
            "StyledText.lineSpacing",
            "StyledText.marginColor",
            "StyledText.renderer",
            "StyledText.rightMargin",
            "StyledText.selection",
            "StyledText.selectionBackground",
            "StyledText.selectionForeground",
            "StyledText.selectionRanges",
            "StyledText.styleIndex",
            "StyledText.styles",
            "StyledText.tabStops",
            "StyledText.tabs",
            "StyledText.text",
            "StyledText.topMargin",
            "StyledText.topPixel",
            "StyledText.wordWrap",
            "StyledText.wrapIndent",
            "StyledTextPrintOptions.footer",
            "StyledTextPrintOptions.header",
            "StyledTextPrintOptions.jobName",
            "StyledTextPrintOptions.lineLabels",
            "StyledTextPrintOptions.printLineBackground",
            "StyledTextPrintOptions.printLineNumbers",
            "StyledTextPrintOptions.printTextBackground",
            "StyledTextPrintOptions.printTextFontStyle",
            "StyledTextPrintOptions.printTextForeground",
            "StyledTextRenderer.ascent",
            "StyledTextRenderer.averageCharWidth",
            "StyledTextRenderer.averageLineHeight",
            "StyledTextRenderer.boldFont",
            "StyledTextRenderer.boldItalicFont",
            "StyledTextRenderer.bulletTexts",
            "StyledTextRenderer.bulletsIndices",
            "StyledTextRenderer.descent",
            "StyledTextRenderer.fixedPitch",
            "StyledTextRenderer.hasLinks",
            "StyledTextRenderer.idleRunning",
            "StyledTextRenderer.italicFont",
            "StyledTextRenderer.lineCount",
            "StyledTextRenderer.lineSizes",
            "StyledTextRenderer.lineSpacingComputing",
            "StyledTextRenderer.lineSpacings",
            "StyledTextRenderer.lines",
            "StyledTextRenderer.linesInAverageLineHeight",
            "StyledTextRenderer.maxWidth",
            "StyledTextRenderer.maxWidthLineIndex",
            "StyledTextRenderer.ranges",
            "StyledTextRenderer.redrawLines",
            "StyledTextRenderer.regularFont",
            "StyledTextRenderer.styleCount",
            "StyledTextRenderer.styledText",
            "StyledTextRenderer.styles",
            "StyledTextRenderer.stylesSet",
            "StyledTextRenderer.stylesSetCount",
            "StyledTextRenderer.tabLength",
            "StyledTextRenderer.tabWidth",
            "StyledTextRenderer.topIndex",
            "TabFolder.items",
            "TabFolder.selection",
            "TabItem.control",
            "TabItem.toolTipText",
            "Table.columnOrder",
            "Table.columns",
            "Table.editors",
            "Table.headerBackground",
            "Table.headerForeground",
            "Table.headerVisible",
            "Table.itemCount",
            "Table.items",
            "Table.linesVisible",
            "Table.ownerDrawItemHeight",
            "Table.selection",
            "Table.sortColumn",
            "Table.sortDirection",
            "Table.topIndex",
            "TableColumn.alignment",
            "TableColumn.moveable",
            "TableColumn.resizable",
            "TableColumn.toolTipText",
            "TableColumn.width",
            "TableEditor.column",
            "TableEditor.item",
            "TableItem.background",
            "TableItem.backgrounds",
            "TableItem.checked",
            "TableItem.font",
            "TableItem.fonts",
            "TableItem.foreground",
            "TableItem.foregrounds",
            "TableItem.grayed",
            "TableItem.images",
            "TableItem.paintedImages",
            "TableItem.paintedTexts",
            "TableItem.texts",
            "Text.caretPosition",
            "Text.echoCharacter",
            "Text.editTexts",
            "Text.editable",
            "Text.edits",
            "Text.message",
            "Text.selection",
            "Text.text",
            "Text.textLimit",
            "TextStyle.background",
            "TextStyle.borderColor",
            "TextStyle.borderStyle",
            "TextStyle.font",
            "TextStyle.foreground",
            "TextStyle.metrics",
            "TextStyle.rise",
            "TextStyle.strikeout",
            "TextStyle.strikeoutColor",
            "TextStyle.underline",
            "TextStyle.underlineColor",
            "TextStyle.underlineStyle",
            "ToolBar.items",
            "ToolItem.background",
            "ToolItem.control",
            "ToolItem.disabledImage",
            "ToolItem.enabled",
            "ToolItem.foreground",
            "ToolItem.hotImage",
            "ToolItem.selection",
            "ToolItem.toolTipText",
            "ToolItem.width",
            "ToolTip.location",
            "ToolTip.message",
            "ToolTip.text",
            "ToolTip.visible",
            "Touch.id",
            "Touch.primary",
            "Touch.source",
            "Touch.state",
            "Touch.x",
            "Touch.y",
            "TouchSource.bounds",
            "TouchSource.direct",
            "TouchSource.handle",
            "Transform.elements",
            "Tree.columnOrder",
            "Tree.columns",
            "Tree.editors",
            "Tree.headerBackground",
            "Tree.headerForeground",
            "Tree.headerVisible",
            "Tree.items",
            "Tree.linesVisible",
            "Tree.ownerDrawItemHeight",
            "Tree.selection",
            "Tree.sortColumn",
            "Tree.sortDirection",
            "Tree.topItem",
            "TreeColumn.alignment",
            "TreeColumn.moveable",
            "TreeColumn.resizable",
            "TreeColumn.toolTipText",
            "TreeColumn.width",
            "TreeEditor.column",
            "TreeEditor.editor",
            "TreeEditor.item",
            "TreeItem.background",
            "TreeItem.backgrounds",
            "TreeItem.checked",
            "TreeItem.expanded",
            "TreeItem.font",
            "TreeItem.fonts",
            "TreeItem.foreground",
            "TreeItem.foregrounds",
            "TreeItem.grayed",
            "TreeItem.images",
            "TreeItem.itemCount",
            "TreeItem.items",
            "TreeItem.paintedImages",
            "TreeItem.paintedTexts",
            "TreeItem.texts",
            "Widget.style",
    };

    /** Each property's encoding, by number; null where nothing writes it. */
    public static final String[] KINDS = {
            null,
            "ref",
            "int",
            "int",
            "int",
            "int",
            "string[]",
            "bool",
            "string",
            "string",
            "ref",
            "string",
            "int",
            "bool",
            "image",
            "bool",
            "bool",
            "string",
            "int",
            "bool",
            "string[]",
            "string[]",
            "bool",
            "point",
            "string",
            "int",
            "int",
            "image",
            "int",
            "color[]",
            "int[]",
            "bool",
            "image",
            "int",
            "int",
            "string",
            "int",
            "bool",
            "bool",
            "bool",
            "bool",
            "bool",
            "ref[]",
            "bool",
            "bool",
            "bool",
            "bool",
            "int",
            "bool",
            "int",
            "color",
            "int",
            "image",
            "color",
            "bool",
            "int",
            "bool",
            "int",
            "int?",
            "int",
            "ref",
            "int",
            "bool",
            "bool",
            "ref",
            "object:VFont",
            "color",
            "color",
            "bool",
            "bool",
            "bool",
            "string",
            "ref",
            "int",
            "rect",
            "bool",
            "image",
            "bool",
            "string[]",
            "int",
            "int",
            "int",
            "int",
            "rgb",
            "rgb[]",
            "int",
            "string[]",
            "bool",
            "string",
            "int",
            "int",
            "ref[]",
            "string",
            "bool",
            "string",
            "string",
            "string",
            "string",
            "bool",
            "bool",
            "bool",
            "bool",
            "string",
            "string",
            "bool",
            "bool",
            "bool",
            "bool",
            "int",
            "bool?",
            "double",
            "string",
            "bool",
            "int",
            "bool",
            "string",
            "bool",
            "bool",
            "bool",
            "int",
            "bool",
            "bool",
            "bool",
            "bool",
            "string",
            "stringMap",
            "string",
            "bool",
            "int",
            "bool",
            "bool",
            "bool",
            "bool?",
            "boolMap",
            "bool",
            "color",
            "image",
            "rect",
            "object:VCursor",
            "bool",
            "bool",
            "long?",
            "long?",
            "bool?",
            "object:VFont",
            "color",
            "bool",
            "bool",
            "ref",
            "object:VRegion",
            "bool",
            "string",
            "bool?",
            "ref",
            "int[]",
            "point[]",
            "ref[]",
            "int[]",
            "ref",
            "point",
            "int",
            "int",
            "int",
            "image",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "ref",
            "int",
            "bool",
            "string",
            "object:VFontData[]",
            "bool",
            "bool",
            "long",
            "int",
            "string",
            "string",
            "string",
            "string",
            "long",
            "object:ConfigFlags",
            "long",
            "long",
            "ref[]",
            "ref[]",
            "ref",
            "ref[]",
            "long[]",
            "int",
            "char",
            "int",
            "int",
            "int",
            "bool",
            "image",
            "int",
            "int",
            "ref",
            "int",
            "int",
            "ref",
            "long",
            "int",
            "int",
            "double",
            "int",
            "double",
            "int[]",
            "char[]",
            "int",
            "int",
            "string",
            "int",
            "object:VTouch[]",
            "int",
            "ref",
            "int",
            "int",
            "int",
            "int",
            "int",
            "ref[]",
            "int",
            "ref",
            "bool",
            "int",
            "int",
            "string",
            "string[]",
            "string[]",
            "int",
            "string[]",
            "string",
            "bool",
            "object:VFontData[]",
            "int",
            "string",
            "int",
            "rgb",
            "bool",
            "object:VFontData",
            "object:VFontData[]",
            "int",
            "ref",
            "int",
            "int",
            "int",
            "object:VFormAttachment",
            "object:VFormAttachment",
            "int",
            "object:VFormAttachment",
            "object:VFormAttachment",
            "object:VFormAttachment",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "bool",
            "object:VFormAttachment",
            "bool",
            "object:VFormAttachment",
            "object:VFormAttachment",
            "int",
            "bool",
            "int?",
            "int",
            "color",
            "object:VPattern",
            "float",
            "float",
            "rect",
            "object:VPathData",
            "int[]",
            "object:VPath",
            "int?",
            "object:VFont",
            "color",
            "object:VPattern",
            "rgb[]",
            "int",
            "int?",
            "int[]",
            "float",
            "int?",
            "int?",
            "int",
            "rgb[]",
            "int",
            "bool?",
            "object:VTransform",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "bool",
            "int",
            "int",
            "int",
            "int",
            "ref",
            "int",
            "bool",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "bool",
            "bool",
            "bool",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "string",
            "string",
            "int",
            "object:VImageData",
            "long?",
            "string",
            "int",
            "byte[]",
            "int",
            "int",
            "int",
            "image",
            "string",
            "image",
            "string",
            "int",
            "color",
            "int",
            "int",
            "bool",
            "int[]",
            "char[]",
            "int[]",
            "int",
            "int",
            "int",
            "int",
            "color",
            "string",
            "string[]",
            "int[]",
            "int",
            "bool",
            "ref[]",
            "point",
            "int",
            "bool",
            "int",
            "bool",
            "ref",
            "bool",
            "string",
            "string[]",
            "string",
            "int",
            "int",
            "int",
            "int",
            "long",
            "int",
            "int",
            "int",
            "int",
            "int",
            "double",
            "double",
            "object:VPathData",
            "object:VFont[]",
            "float[]",
            "string[]",
            "float[]",
            "byte[]",
            "color",
            "color",
            "float",
            "float",
            "image",
            "float",
            "float",
            "int",
            "int",
            "object:VFont",
            "string[]",
            "int",
            "string",
            "int",
            "bool",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int[]",
            "bool",
            "int",
            "int",
            "ref",
            "int",
            "int[]",
            "int",
            "int",
            "int",
            "bool",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "bool",
            "ref",
            "ref",
            "bool",
            "ref",
            "bool",
            "bool",
            "int",
            "int",
            "point",
            "int",
            "int",
            "poly[]",
            "bool",
            "point",
            "point",
            "bool",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "string",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "int",
            "bool",
            "bool",
            "rect",
            "int",
            "int",
            "int[]",
            "bool",
            "bool",
            "bool",
            "int",
            "int",
            "bool",
            "int",
            "int",
            "color",
            "object:VStyledTextRenderer",
            "int",
            "point",
            "color",
            "color",
            "int[]",
            "runs",
            "object:VStyleRange[]",
            "int[]",
            "int",
            "string",
            "int",
            "int",
            "bool",
            "int",
            "string",
            "string",
            "string",
            "string[]",
            "bool",
            "bool",
            "bool",
            "bool",
            "bool",
            "int",
            null,
            null,
            null,
            null,
            "string[]",
            null,
            "int",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "int[]",
            "object:VLineInfo[]",
            null,
            null,
            null,
            "int[]",
            null,
            "object:VFont",
            "int",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "ref[]",
            "ref[]",
            "ref",
            "string",
            "int[]",
            "ref[]",
            "ref[]",
            "color",
            "color",
            "bool",
            "int",
            "refs:TableItem",
            "bool",
            "int",
            "int[]",
            "ref",
            "int",
            "int",
            "int",
            "bool",
            "bool",
            "string",
            "int",
            "int",
            "ref",
            "color",
            "color[]",
            "bool",
            "object:VFont",
            "object:VFont[]",
            "color",
            "color[]",
            "bool",
            "image[]",
            "int[]",
            "int[]",
            "string[]",
            "int",
            "int",
            "string[]",
            "bool",
            "int[]",
            "string",
            "point",
            "string",
            "int",
            "color",
            "color",
            "int",
            "object:VFont",
            "color",
            "object:VGlyphMetrics",
            "int",
            "bool",
            "color",
            "bool",
            "color",
            "int",
            "ref[]",
            "color",
            "ref",
            "image",
            "bool",
            "color",
            "image",
            "bool",
            "string",
            "int",
            "point",
            "string",
            "string",
            "bool",
            "long",
            "bool",
            "object:VTouchSource",
            "int",
            "int",
            "int",
            "rect",
            "bool",
            "long",
            "float[]",
            "int[]",
            "ref[]",
            "ref[]",
            "color",
            "color",
            "bool",
            "refs:TreeItem",
            "bool",
            "int",
            "ref[]",
            "ref",
            "int",
            "ref",
            "int",
            "bool",
            "bool",
            "string",
            "int",
            "int",
            "ref",
            "ref",
            "color",
            "color[]",
            "bool",
            "bool",
            "object:VFont",
            "object:VFont[]",
            "color",
            "color[]",
            "bool",
            "image[]",
            "int",
            "refs:TreeItem",
            "int[]",
            "int[]",
            "string[]",
            "int",
    };

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

    /** The channel `DnD/remoteDrag`. */
    public static final int CHANNEL_DN_D_REMOTE_DRAG = 23;

    /** The channel `DnD/remoteDropNone`. */
    public static final int CHANNEL_DN_D_REMOTE_DROP_NONE = 24;

    /** The channel `Drag/dragFinished`. */
    public static final int CHANNEL_DRAG_DRAG_FINISHED = 25;

    /** The channel `Drag/dragSetData`. */
    public static final int CHANNEL_DRAG_DRAG_SET_DATA = 26;

    /** The channel `Drag/dragStart`. */
    public static final int CHANNEL_DRAG_DRAG_START = 27;

    /** The channel `DragDetect/DragDetect`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_DETECT = 28;

    /** The channel `DragDetect/dragCancel`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_CANCEL = 29;

    /** The channel `DragDetect/dragImage`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_IMAGE = 30;

    /** The channel `DragDetect/dragMove`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_MOVE = 31;

    /** The channel `DragDetect/dragRelease`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_RELEASE = 32;

    /** The channel `DragDetect/dragStartResult`. */
    public static final int CHANNEL_DRAG_DETECT_DRAG_START_RESULT = 33;

    /** The channel `DragDetect/remoteAnswer`. */
    public static final int CHANNEL_DRAG_DETECT_REMOTE_ANSWER = 34;

    /** The channel `Drop/dragEnter`. */
    public static final int CHANNEL_DROP_DRAG_ENTER = 35;

    /** The channel `Drop/dragLeave`. */
    public static final int CHANNEL_DROP_DRAG_LEAVE = 36;

    /** The channel `Drop/dragOperationChanged`. */
    public static final int CHANNEL_DROP_DRAG_OPERATION_CHANGED = 37;

    /** The channel `Drop/dragOver`. */
    public static final int CHANNEL_DROP_DRAG_OVER = 38;

    /** The channel `Drop/drop`. */
    public static final int CHANNEL_DROP_DROP = 39;

    /** The channel `Drop/dropAccept`. */
    public static final int CHANNEL_DROP_DROP_ACCEPT = 40;

    /** The channel `Drop/negotiationResult`. */
    public static final int CHANNEL_DROP_NEGOTIATION_RESULT = 41;

    /** The channel `Expand/Collapse`. */
    public static final int CHANNEL_EXPAND_COLLAPSE = 42;

    /** The channel `Expand/Expand`. */
    public static final int CHANNEL_EXPAND_EXPAND = 43;

    /** The channel `ExtendedModify/modifyText`. */
    public static final int CHANNEL_EXTENDED_MODIFY_MODIFY_TEXT = 44;

    /** The channel `Focus/FocusIn`. */
    public static final int CHANNEL_FOCUS_FOCUS_IN = 45;

    /** The channel `Focus/FocusOut`. */
    public static final int CHANNEL_FOCUS_FOCUS_OUT = 46;

    /** The channel `GC/create`. */
    public static final int CHANNEL_GC_CREATE = 47;

    /** The channel `GC/inFrame`. */
    public static final int CHANNEL_GC_IN_FRAME = 48;

    /** The channel `Gesture/Gesture`. */
    public static final int CHANNEL_GESTURE_GESTURE = 49;

    /** The channel `GetIdFromPoint`. */
    public static final int CHANNEL_GET_ID_FROM_POINT = 50;

    /** The channel `GetIdFromPointResponse`. */
    public static final int CHANNEL_GET_ID_FROM_POINT_RESPONSE = 51;

    /** The channel `GetItemBounds`. */
    public static final int CHANNEL_GET_ITEM_BOUNDS = 52;

    /** The channel `GetItemBoundsResponse`. */
    public static final int CHANNEL_GET_ITEM_BOUNDS_RESPONSE = 53;

    /** The channel `GetItemHeight`. */
    public static final int CHANNEL_GET_ITEM_HEIGHT = 54;

    /** The channel `GetItemHeightResponse`. */
    public static final int CHANNEL_GET_ITEM_HEIGHT_RESPONSE = 55;

    /** The channel `Help/Help`. */
    public static final int CHANNEL_HELP_HELP = 56;

    /** The channel `Image/forget`. */
    public static final int CHANNEL_IMAGE_FORGET = 57;

    /** The channel `Image/releaseRemoteRef`. */
    public static final int CHANNEL_IMAGE_RELEASE_REMOTE_REF = 58;

    /** The channel `Image/requestPixels`. */
    public static final int CHANNEL_IMAGE_REQUEST_PIXELS = 59;

    /** The channel `ImeComposition`. */
    public static final int CHANNEL_IME_COMPOSITION = 60;

    /** The channel `Key/KeyDown`. */
    public static final int CHANNEL_KEY_KEY_DOWN = 61;

    /** The channel `Key/KeyUp`. */
    public static final int CHANNEL_KEY_KEY_UP = 62;

    /** The channel `LineBackground/lineGetBackground`. */
    public static final int CHANNEL_LINE_BACKGROUND_LINE_GET_BACKGROUND = 63;

    /** The channel `LineStyle/lineGetStyle`. */
    public static final int CHANNEL_LINE_STYLE_LINE_GET_STYLE = 64;

    /** The channel `List/Visible`. */
    public static final int CHANNEL_LIST_VISIBLE = 65;

    /** The channel `Location/changed`. */
    public static final int CHANNEL_LOCATION_CHANGED = 66;

    /** The channel `Location/changing`. */
    public static final int CHANNEL_LOCATION_CHANGING = 67;

    /** The channel `Menu/Hide`. */
    public static final int CHANNEL_MENU_HIDE = 68;

    /** The channel `Menu/Show`. */
    public static final int CHANNEL_MENU_SHOW = 69;

    /** The channel `MenuDetect/MenuDetect`. */
    public static final int CHANNEL_MENU_DETECT_MENU_DETECT = 70;

    /** The channel `MenuKey`. */
    public static final int CHANNEL_MENU_KEY = 71;

    /** The channel `Modify/Modify`. */
    public static final int CHANNEL_MODIFY_MODIFY = 72;

    /** The channel `Mouse/MouseDoubleClick`. */
    public static final int CHANNEL_MOUSE_MOUSE_DOUBLE_CLICK = 73;

    /** The channel `Mouse/MouseDown`. */
    public static final int CHANNEL_MOUSE_MOUSE_DOWN = 74;

    /** The channel `Mouse/MouseUp`. */
    public static final int CHANNEL_MOUSE_MOUSE_UP = 75;

    /** The channel `MouseMove/MouseMove`. */
    public static final int CHANNEL_MOUSE_MOVE_MOUSE_MOVE = 76;

    /** The channel `MouseTrack/MouseEnter`. */
    public static final int CHANNEL_MOUSE_TRACK_MOUSE_ENTER = 77;

    /** The channel `MouseTrack/MouseExit`. */
    public static final int CHANNEL_MOUSE_TRACK_MOUSE_EXIT = 78;

    /** The channel `MouseTrack/MouseHover`. */
    public static final int CHANNEL_MOUSE_TRACK_MOUSE_HOVER = 79;

    /** The channel `MouseWheel/MouseWheel`. */
    public static final int CHANNEL_MOUSE_WHEEL_MOUSE_WHEEL = 80;

    /** The channel `OpenWindow`. */
    public static final int CHANNEL_OPEN_WINDOW = 81;

    /** The channel `OpenWindow/open`. */
    public static final int CHANNEL_OPEN_WINDOW_OPEN = 82;

    /** The channel `Paint/Paint`. */
    public static final int CHANNEL_PAINT_PAINT = 83;

    /** The channel `PaintItem/PaintItem`. */
    public static final int CHANNEL_PAINT_ITEM_PAINT_ITEM = 84;

    /** The channel `PaintObject/paintObject`. */
    public static final int CHANNEL_PAINT_OBJECT_PAINT_OBJECT = 85;

    /** The channel `Popups`. */
    public static final int CHANNEL_POPUPS = 86;

    /** The channel `Progress/changed`. */
    public static final int CHANNEL_PROGRESS_CHANGED = 87;

    /** The channel `Progress/completed`. */
    public static final int CHANNEL_PROGRESS_COMPLETED = 88;

    /** The channel `ResendGeometry`. */
    public static final int CHANNEL_RESEND_GEOMETRY = 89;

    /** The channel `ResendText`. */
    public static final int CHANNEL_RESEND_TEXT = 90;

    /** The channel `Scroll/Scroll`. */
    public static final int CHANNEL_SCROLL_SCROLL = 91;

    /** The channel `Segment/Segments`. */
    public static final int CHANNEL_SEGMENT_SEGMENTS = 92;

    /** The channel `Selection/DefaultSelection`. */
    public static final int CHANNEL_SELECTION_DEFAULT_SELECTION = 93;

    /** The channel `Selection/OpenMenu`. */
    public static final int CHANNEL_SELECTION_OPEN_MENU = 94;

    /** The channel `Selection/Selection`. */
    public static final int CHANNEL_SELECTION_SELECTION = 95;

    /** The channel `SetData/SetData`. */
    public static final int CHANNEL_SET_DATA_SET_DATA = 96;

    /** The channel `Shell/Activate`. */
    public static final int CHANNEL_SHELL_ACTIVATE = 97;

    /** The channel `Shell/Close`. */
    public static final int CHANNEL_SHELL_CLOSE = 98;

    /** The channel `Shell/Deactivate`. */
    public static final int CHANNEL_SHELL_DEACTIVATE = 99;

    /** The channel `Shell/Deiconify`. */
    public static final int CHANNEL_SHELL_DEICONIFY = 100;

    /** The channel `Shell/Iconify`. */
    public static final int CHANNEL_SHELL_ICONIFY = 101;

    /** The channel `Shell/SetBounds`. */
    public static final int CHANNEL_SHELL_SET_BOUNDS = 102;

    /** The channel `StatusText/changed`. */
    public static final int CHANNEL_STATUS_TEXT_CHANGED = 103;

    /** The channel `TextChange`. */
    public static final int CHANNEL_TEXT_CHANGE = 104;

    /** The channel `TextGeometry`. */
    public static final int CHANNEL_TEXT_GEOMETRY = 105;

    /** The channel `Title/changed`. */
    public static final int CHANNEL_TITLE_CHANGED = 106;

    /** The channel `Touch/Touch`. */
    public static final int CHANNEL_TOUCH_TOUCH = 107;

    /** The channel `Tracker/cancel`. */
    public static final int CHANNEL_TRACKER_CANCEL = 108;

    /** The channel `Tracker/close`. */
    public static final int CHANNEL_TRACKER_CLOSE = 109;

    /** The channel `Tracker/open`. */
    public static final int CHANNEL_TRACKER_OPEN = 110;

    /** The channel `Traverse/Traverse`. */
    public static final int CHANNEL_TRAVERSE_TRAVERSE = 111;

    /** The channel `Tree/Collapse`. */
    public static final int CHANNEL_TREE_COLLAPSE = 112;

    /** The channel `Tree/Expand`. */
    public static final int CHANNEL_TREE_EXPAND = 113;

    /** The channel `Verify/Verify`. */
    public static final int CHANNEL_VERIFY_VERIFY = 114;

    /** The channel `VerifyKey/verifyKey`. */
    public static final int CHANNEL_VERIFY_KEY_VERIFY_KEY = 115;

    /** The channel `VisibilityWindow/hide`. */
    public static final int CHANNEL_VISIBILITY_WINDOW_HIDE = 116;

    /** The channel `VisibilityWindow/show`. */
    public static final int CHANNEL_VISIBILITY_WINDOW_SHOW = 117;

    /** The channel `WinClose`. */
    public static final int CHANNEL_WIN_CLOSE = 118;

    /** The channel `WinDidEnterFullScreen`. */
    public static final int CHANNEL_WIN_DID_ENTER_FULL_SCREEN = 119;

    /** The channel `WinDidExitFullScreen`. */
    public static final int CHANNEL_WIN_DID_EXIT_FULL_SCREEN = 120;

    /** The channel `WinExitFullScreen`. */
    public static final int CHANNEL_WIN_EXIT_FULL_SCREEN = 121;

    /** The channel `WinFullScreen`. */
    public static final int CHANNEL_WIN_FULL_SCREEN = 122;

    /** The channel `WinFullScreenState`. */
    public static final int CHANNEL_WIN_FULL_SCREEN_STATE = 123;

    /** The channel `WinMaximize`. */
    public static final int CHANNEL_WIN_MAXIMIZE = 124;

    /** The channel `WinMinimize`. */
    public static final int CHANNEL_WIN_MINIMIZE = 125;

    /** The channel `WinRestore`. */
    public static final int CHANNEL_WIN_RESTORE = 126;

    /** The channel `WinUnload`. */
    public static final int CHANNEL_WIN_UNLOAD = 127;

    /** The channel `WindowBounds`. */
    public static final int CHANNEL_WINDOW_BOUNDS = 128;

    /** The channel `WindowOpenFailed`. */
    public static final int CHANNEL_WINDOW_OPEN_FAILED = 129;

    /** The channel `WindowState`. */
    public static final int CHANNEL_WINDOW_STATE = 130;

    /** The channel `WindowTitle`. */
    public static final int CHANNEL_WINDOW_TITLE = 131;

    /** The channel `WordMovement/getNextOffset`. */
    public static final int CHANNEL_WORD_MOVEMENT_GET_NEXT_OFFSET = 132;

    /** The channel `WordMovement/getPreviousOffset`. */
    public static final int CHANNEL_WORD_MOVEMENT_GET_PREVIOUS_OFFSET = 133;

    /** The channel `activate`. */
    public static final int CHANNEL_ACTIVATE = 134;

    /** The channel `activation`. */
    public static final int CHANNEL_ACTIVATION = 135;

    /** The channel `anim`. */
    public static final int CHANNEL_ANIM = 136;

    /** The channel `b`. */
    public static final int CHANNEL_B = 137;

    /** The channel `back`. */
    public static final int CHANNEL_BACK = 138;

    /** The channel `bench/d2j/probe`. */
    public static final int CHANNEL_BENCH_D2J_PROBE = 139;

    /** The channel `bench/d2j/probeResponse`. */
    public static final int CHANNEL_BENCH_D2J_PROBE_RESPONSE = 140;

    /** The channel `bench/d2j/result`. */
    public static final int CHANNEL_BENCH_D2J_RESULT = 141;

    /** The channel `bench/d2j/run`. */
    public static final int CHANNEL_BENCH_D2J_RUN = 142;

    /** The channel `bench/j2d/echo`. */
    public static final int CHANNEL_BENCH_J2D_ECHO = 143;

    /** The channel `bench/j2d/echoResponse`. */
    public static final int CHANNEL_BENCH_J2D_ECHO_RESPONSE = 144;

    /** The channel `browserFunctionCall`. */
    public static final int CHANNEL_BROWSER_FUNCTION_CALL = 145;

    /** The channel `c`. */
    public static final int CHANNEL_C = 146;

    /** The channel `callback`. */
    public static final int CHANNEL_CALLBACK = 147;

    /** The channel `close`. */
    public static final int CHANNEL_CLOSE = 148;

    /** The channel `cookie`. */
    public static final int CHANNEL_COOKIE = 149;

    /** The channel `copyAreaImageintint`. */
    public static final int CHANNEL_COPY_AREA_IMAGEINTINT = 150;

    /** The channel `copyAreaImageintintResponse`. */
    public static final int CHANNEL_COPY_AREA_IMAGEINTINT_RESPONSE = 151;

    /** The channel `copyAreaintintintintintintboolean`. */
    public static final int CHANNEL_COPY_AREAINTINTINTINTINTINTBOOLEAN = 152;

    /** The channel `drawArcintintintintintint`. */
    public static final int CHANNEL_DRAW_ARCINTINTINTINTINTINT = 153;

    /** The channel `drawFocusintintintint`. */
    public static final int CHANNEL_DRAW_FOCUSINTINTINTINT = 154;

    /** The channel `drawImageImageintint`. */
    public static final int CHANNEL_DRAW_IMAGE_IMAGEINTINT = 155;

    /** The channel `drawImageImageintintintint`. */
    public static final int CHANNEL_DRAW_IMAGE_IMAGEINTINTINTINT = 156;

    /** The channel `drawImageImageintintintintintintintint`. */
    public static final int CHANNEL_DRAW_IMAGE_IMAGEINTINTINTINTINTINTINTINT = 157;

    /** The channel `drawLineintintintint`. */
    public static final int CHANNEL_DRAW_LINEINTINTINTINT = 158;

    /** The channel `drawOvalintintintint`. */
    public static final int CHANNEL_DRAW_OVALINTINTINTINT = 159;

    /** The channel `drawPathPath`. */
    public static final int CHANNEL_DRAW_PATH_PATH = 160;

    /** The channel `drawPointintint`. */
    public static final int CHANNEL_DRAW_POINTINTINT = 161;

    /** The channel `drawPolygonint`. */
    public static final int CHANNEL_DRAW_POLYGONINT = 162;

    /** The channel `drawPolylineint`. */
    public static final int CHANNEL_DRAW_POLYLINEINT = 163;

    /** The channel `drawRectangleintintintint`. */
    public static final int CHANNEL_DRAW_RECTANGLEINTINTINTINT = 164;

    /** The channel `drawRoundRectangleintintintintintint`. */
    public static final int CHANNEL_DRAW_ROUND_RECTANGLEINTINTINTINTINTINT = 165;

    /** The channel `drawTextStringintintint`. */
    public static final int CHANNEL_DRAW_TEXT_STRINGINTINTINT = 166;

    /** The channel `evaluate`. */
    public static final int CHANNEL_EVALUATE = 167;

    /** The channel `evaluate/result`. */
    public static final int CHANNEL_EVALUATE_RESULT = 168;

    /** The channel `evolve.test.bootTrace`. */
    public static final int CHANNEL_EVOLVE_TEST_BOOT_TRACE = 169;

    /** The channel `evolve.test.bootTraceResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_BOOT_TRACE_RESPONSE = 170;

    /** The channel `evolve.test.drag`. */
    public static final int CHANNEL_EVOLVE_TEST_DRAG = 171;

    /** The channel `evolve.test.dragDone`. */
    public static final int CHANNEL_EVOLVE_TEST_DRAG_DONE = 172;

    /** The channel `evolve.test.frameCost`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_COST = 173;

    /** The channel `evolve.test.frameCostResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_COST_RESPONSE = 174;

    /** The channel `evolve.test.frameStats`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_STATS = 175;

    /** The channel `evolve.test.frameStatsResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_STATS_RESPONSE = 176;

    /** The channel `evolve.test.frameSync`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_SYNC = 177;

    /** The channel `evolve.test.frameSynced`. */
    public static final int CHANNEL_EVOLVE_TEST_FRAME_SYNCED = 178;

    /** The channel `evolve.test.iframeLoaded`. */
    public static final int CHANNEL_EVOLVE_TEST_IFRAME_LOADED = 179;

    /** The channel `evolve.test.query`. */
    public static final int CHANNEL_EVOLVE_TEST_QUERY = 180;

    /** The channel `evolve.test.queryResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_QUERY_RESPONSE = 181;

    /** The channel `evolve.test.styledTextPerf`. */
    public static final int CHANNEL_EVOLVE_TEST_STYLED_TEXT_PERF = 182;

    /** The channel `evolve.test.styledTextPerfResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_STYLED_TEXT_PERF_RESPONSE = 183;

    /** The channel `evolve.test.wireStats`. */
    public static final int CHANNEL_EVOLVE_TEST_WIRE_STATS = 184;

    /** The channel `evolve.test.wireStatsResponse`. */
    public static final int CHANNEL_EVOLVE_TEST_WIRE_STATS_RESPONSE = 185;

    /** The channel `execute`. */
    public static final int CHANNEL_EXECUTE = 186;

    /** The channel `fillArcintintintintintint`. */
    public static final int CHANNEL_FILL_ARCINTINTINTINTINTINT = 187;

    /** The channel `fillGradientRectangleintintintintboolean`. */
    public static final int CHANNEL_FILL_GRADIENT_RECTANGLEINTINTINTINTBOOLEAN = 188;

    /** The channel `fillOvalintintintint`. */
    public static final int CHANNEL_FILL_OVALINTINTINTINT = 189;

    /** The channel `fillPathPath`. */
    public static final int CHANNEL_FILL_PATH_PATH = 190;

    /** The channel `fillPolygonint`. */
    public static final int CHANNEL_FILL_POLYGONINT = 191;

    /** The channel `fillRectangleintintintint`. */
    public static final int CHANNEL_FILL_RECTANGLEINTINTINTINT = 192;

    /** The channel `fillRoundRectangleintintintintintint`. */
    public static final int CHANNEL_FILL_ROUND_RECTANGLEINTINTINTINTINTINT = 193;

    /** The channel `fontSizeRequest`. */
    public static final int CHANNEL_FONT_SIZE_REQUEST = 194;

    /** The channel `fontSizeResponse`. */
    public static final int CHANNEL_FONT_SIZE_RESPONSE = 195;

    /** The channel `forward`. */
    public static final int CHANNEL_FORWARD = 196;

    /** The channel `gcDispose`. */
    public static final int CHANNEL_GC_DISPOSE = 197;

    /** The channel `getCookie`. */
    public static final int CHANNEL_GET_COOKIE = 198;

    /** The channel `imageInit`. */
    public static final int CHANNEL_IMAGE_INIT = 199;

    /** The channel `imageResult`. */
    public static final int CHANNEL_IMAGE_RESULT = 200;

    /** The channel `imageSizeRequest`. */
    public static final int CHANNEL_IMAGE_SIZE_REQUEST = 201;

    /** The channel `imageSizeResponse`. */
    public static final int CHANNEL_IMAGE_SIZE_RESPONSE = 202;

    /** The channel `imageSnapshotResult`. */
    public static final int CHANNEL_IMAGE_SNAPSHOT_RESULT = 203;

    /** The channel `key/verdict`. */
    public static final int CHANNEL_KEY_VERDICT = 204;

    /** The channel `key/vetoable`. */
    public static final int CHANNEL_KEY_VETOABLE = 205;

    /** The channel `locationchanging/result`. */
    public static final int CHANNEL_LOCATIONCHANGING_RESULT = 206;

    /** The channel `menu/verdict`. */
    public static final int CHANNEL_MENU_VERDICT = 207;

    /** The channel `modify/verdict`. */
    public static final int CHANNEL_MODIFY_VERDICT = 208;

    /** The channel `modify/vetoable`. */
    public static final int CHANNEL_MODIFY_VETOABLE = 209;

    /** The channel `navigate`. */
    public static final int CHANNEL_NAVIGATE = 210;

    /** The channel `navigateRequest`. */
    public static final int CHANNEL_NAVIGATE_REQUEST = 211;

    /** The channel `navigationState`. */
    public static final int CHANNEL_NAVIGATION_STATE = 212;

    /** The channel `nextFrame`. */
    public static final int CHANNEL_NEXT_FRAME = 213;

    /** The channel `openMenu`. */
    public static final int CHANNEL_OPEN_MENU = 214;

    /** The channel `pixelsResult`. */
    public static final int CHANNEL_PIXELS_RESULT = 215;

    /** The channel `rebuild`. */
    public static final int CHANNEL_REBUILD = 216;

    /** The channel `reload`. */
    public static final int CHANNEL_RELOAD = 217;

    /** The channel `renderSnapshot`. */
    public static final int CHANNEL_RENDER_SNAPSHOT = 218;

    /** The channel `setCookie`. */
    public static final int CHANNEL_SET_COOKIE = 219;

    /** The channel `shown`. */
    public static final int CHANNEL_SHOWN = 220;

    /** The channel `stop`. */
    public static final int CHANNEL_STOP = 221;

    /** The channel `subtree`. */
    public static final int CHANNEL_SUBTREE = 222;

    /** The channel `subtree/request`. */
    public static final int CHANNEL_SUBTREE_REQUEST = 223;

    /** The channel `swing`. */
    public static final int CHANNEL_SWING = 224;

    /** The channel `swing.open`. */
    public static final int CHANNEL_SWING_OPEN = 225;

    /** The channel `swingIsland`. */
    public static final int CHANNEL_SWING_ISLAND = 226;

    /** The channel `swingIsland.close`. */
    public static final int CHANNEL_SWING_ISLAND_CLOSE = 227;

    /** The channel `swingIsland.dispose`. */
    public static final int CHANNEL_SWING_ISLAND_DISPOSE = 228;

    /** The channel `swingIsland.island`. */
    public static final int CHANNEL_SWING_ISLAND_ISLAND = 229;

    /** The channel `swingIsland.opened`. */
    public static final int CHANNEL_SWING_ISLAND_OPENED = 230;

    /** The channel `swingIsland.resize`. */
    public static final int CHANNEL_SWING_ISLAND_RESIZE = 231;

    /** The channel `swingIsland.surface`. */
    public static final int CHANNEL_SWING_ISLAND_SURFACE = 232;

    /** The channel `swingIsland.title`. */
    public static final int CHANNEL_SWING_ISLAND_TITLE = 233;

    /** The channel `swt.evolve.batch`. */
    public static final int CHANNEL_SWT_EVOLVE_BATCH = 234;

    /** The channel `swt.evolve.focus`. */
    public static final int CHANNEL_SWT_EVOLVE_FOCUS = 235;

    /** The channel `swt.evolve.focus.seen`. */
    public static final int CHANNEL_SWT_EVOLVE_FOCUS_SEEN = 236;

    /** The channel `swt.evolve.focusHolder`. */
    public static final int CHANNEL_SWT_EVOLVE_FOCUS_HOLDER = 237;

    /** The channel `swt.evolve.properties`. */
    public static final int CHANNEL_SWT_EVOLVE_PROPERTIES = 238;

    /** The channel `swt.evolve.property.set`. */
    public static final int CHANNEL_SWT_EVOLVE_PROPERTY_SET = 239;

    /** The channel `swt.evolve.test.runUi`. */
    public static final int CHANNEL_SWT_EVOLVE_TEST_RUN_UI = 240;

    /** The channel `swt.evolve.test.runUiResponse`. */
    public static final int CHANNEL_SWT_EVOLVE_TEST_RUN_UI_RESPONSE = 241;

    /** The channel `swt.evolve.timing.bare`. */
    public static final int CHANNEL_SWT_EVOLVE_TIMING_BARE = 242;

    /** The channel `swt.evolve.timing.ping`. */
    public static final int CHANNEL_SWT_EVOLVE_TIMING_PING = 243;

    /** The channel `swt.evolve.timing.pong`. */
    public static final int CHANNEL_SWT_EVOLVE_TIMING_PONG = 244;

    /** The channel `swt.evolve.url.open`. */
    public static final int CHANNEL_SWT_EVOLVE_URL_OPEN = 245;

    /** The channel `swt.evolve.widget.refresh`. */
    public static final int CHANNEL_SWT_EVOLVE_WIDGET_REFRESH = 246;

    /** The channel `swt.evolve.wire`. */
    public static final int CHANNEL_SWT_EVOLVE_WIRE = 247;

    /** The channel `transport`. */
    public static final int CHANNEL_TRANSPORT = 248;

    /** The channel `transport/request`. */
    public static final int CHANNEL_TRANSPORT_REQUEST = 249;

    /** The channel `traverse/verdict`. */
    public static final int CHANNEL_TRAVERSE_VERDICT = 250;

    /** The channel `traverse/vetoable`. */
    public static final int CHANNEL_TRAVERSE_VETOABLE = 251;

    /** The channel `v`. */
    public static final int CHANNEL_V = 252;

    /** The channel `widgetSizeRequest`. */
    public static final int CHANNEL_WIDGET_SIZE_REQUEST = 253;

    /** The channel `widgetSizeResponse`. */
    public static final int CHANNEL_WIDGET_SIZE_RESPONSE = 254;

    /** The type `Accessible`. */
    public static final int TYPE_ACCESSIBLE = 1;

    /** The type `AnimatedProgress`. */
    public static final int TYPE_ANIMATED_PROGRESS = 2;

    /** The type `BenchBridge`. */
    public static final int TYPE_BENCH_BRIDGE = 3;

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

    /** The type `DisplayPopups`. */
    public static final int TYPE_DISPLAY_POPUPS = 32;

    /** The type `DragSource`. */
    public static final int TYPE_DRAG_SOURCE = 33;

    /** The type `DropTarget`. */
    public static final int TYPE_DROP_TARGET = 34;

    /** The type `Event`. */
    public static final int TYPE_EVENT = 35;

    /** The type `EwtWidget`. */
    public static final int TYPE_EWT_WIDGET = 36;

    /** The type `ExpandBar`. */
    public static final int TYPE_EXPAND_BAR = 37;

    /** The type `ExpandItem`. */
    public static final int TYPE_EXPAND_ITEM = 38;

    /** The type `FileDialog`. */
    public static final int TYPE_FILE_DIALOG = 39;

    /** The type `FileTransfer`. */
    public static final int TYPE_FILE_TRANSFER = 40;

    /** The type `Font`. */
    public static final int TYPE_FONT = 41;

    /** The type `FontData`. */
    public static final int TYPE_FONT_DATA = 42;

    /** The type `FontDialog`. */
    public static final int TYPE_FONT_DIALOG = 43;

    /** The type `FontMeasureBridge`. */
    public static final int TYPE_FONT_MEASURE_BRIDGE = 44;

    /** The type `FontMetrics`. */
    public static final int TYPE_FONT_METRICS = 45;

    /** The type `FormAttachment`. */
    public static final int TYPE_FORM_ATTACHMENT = 46;

    /** The type `FormData`. */
    public static final int TYPE_FORM_DATA = 47;

    /** The type `GC`. */
    public static final int TYPE_GC = 48;

    /** The type `GCData`. */
    public static final int TYPE_GCDATA = 49;

    /** The type `GLCanvas`. */
    public static final int TYPE_GLCANVAS = 50;

    /** The type `GLData`. */
    public static final int TYPE_GLDATA = 51;

    /** The type `GlyphMetrics`. */
    public static final int TYPE_GLYPH_METRICS = 52;

    /** The type `GridData`. */
    public static final int TYPE_GRID_DATA = 53;

    /** The type `Group`. */
    public static final int TYPE_GROUP = 54;

    /** The type `HTMLTransfer`. */
    public static final int TYPE_HTMLTRANSFER = 55;

    /** The type `IME`. */
    public static final int TYPE_IME = 56;

    /** The type `Image`. */
    public static final int TYPE_IMAGE = 57;

    /** The type `ImageData`. */
    public static final int TYPE_IMAGE_DATA = 58;

    /** The type `ImageSizeBridge`. */
    public static final int TYPE_IMAGE_SIZE_BRIDGE = 59;

    /** The type `ImageTransfer`. */
    public static final int TYPE_IMAGE_TRANSFER = 60;

    /** The type `Label`. */
    public static final int TYPE_LABEL = 61;

    /** The type `LineInfo`. */
    public static final int TYPE_LINE_INFO = 62;

    /** The type `LineSizeInfo`. */
    public static final int TYPE_LINE_SIZE_INFO = 63;

    /** The type `Link`. */
    public static final int TYPE_LINK = 64;

    /** The type `List`. */
    public static final int TYPE_LIST = 65;

    /** The type `MainComposite`. */
    public static final int TYPE_MAIN_COMPOSITE = 66;

    /** The type `MainToolbar`. */
    public static final int TYPE_MAIN_TOOLBAR = 67;

    /** The type `Menu`. */
    public static final int TYPE_MENU = 68;

    /** The type `MenuItem`. */
    public static final int TYPE_MENU_ITEM = 69;

    /** The type `MessageBox`. */
    public static final int TYPE_MESSAGE_BOX = 70;

    /** The type `Monitor`. */
    public static final int TYPE_MONITOR = 71;

    /** The type `PDFDocument`. */
    public static final int TYPE_PDFDOCUMENT = 72;

    /** The type `Path`. */
    public static final int TYPE_PATH = 73;

    /** The type `PathData`. */
    public static final int TYPE_PATH_DATA = 74;

    /** The type `Pattern`. */
    public static final int TYPE_PATTERN = 75;

    /** The type `Point`. */
    public static final int TYPE_POINT = 76;

    /** The type `PopupList`. */
    public static final int TYPE_POPUP_LIST = 77;

    /** The type `PrintDialog`. */
    public static final int TYPE_PRINT_DIALOG = 78;

    /** The type `Printer`. */
    public static final int TYPE_PRINTER = 79;

    /** The type `Program`. */
    public static final int TYPE_PROGRAM = 80;

    /** The type `ProgressBar`. */
    public static final int TYPE_PROGRESS_BAR = 81;

    /** The type `RGB`. */
    public static final int TYPE_RGB = 82;

    /** The type `RTFTransfer`. */
    public static final int TYPE_RTFTRANSFER = 83;

    /** The type `Rectangle`. */
    public static final int TYPE_RECTANGLE = 84;

    /** The type `Region`. */
    public static final int TYPE_REGION = 85;

    /** The type `Resource`. */
    public static final int TYPE_RESOURCE = 86;

    /** The type `RowData`. */
    public static final int TYPE_ROW_DATA = 87;

    /** The type `Sash`. */
    public static final int TYPE_SASH = 88;

    /** The type `SashForm`. */
    public static final int TYPE_SASH_FORM = 89;

    /** The type `Scale`. */
    public static final int TYPE_SCALE = 90;

    /** The type `ScrollBar`. */
    public static final int TYPE_SCROLL_BAR = 91;

    /** The type `ScrolledComposite`. */
    public static final int TYPE_SCROLLED_COMPOSITE = 92;

    /** The type `Shell`. */
    public static final int TYPE_SHELL = 93;

    /** The type `ShellPopups`. */
    public static final int TYPE_SHELL_POPUPS = 94;

    /** The type `SideBar`. */
    public static final int TYPE_SIDE_BAR = 95;

    /** The type `Slider`. */
    public static final int TYPE_SLIDER = 96;

    /** The type `Spinner`. */
    public static final int TYPE_SPINNER = 97;

    /** The type `StatusBar`. */
    public static final int TYPE_STATUS_BAR = 98;

    /** The type `StyleRange`. */
    public static final int TYPE_STYLE_RANGE = 99;

    /** The type `StyledText`. */
    public static final int TYPE_STYLED_TEXT = 100;

    /** The type `StyledTextPrintOptions`. */
    public static final int TYPE_STYLED_TEXT_PRINT_OPTIONS = 101;

    /** The type `StyledTextRenderer`. */
    public static final int TYPE_STYLED_TEXT_RENDERER = 102;

    /** The type `SwingIsland`. */
    public static final int TYPE_SWING_ISLAND = 103;

    /** The type `TabFolder`. */
    public static final int TYPE_TAB_FOLDER = 104;

    /** The type `TabItem`. */
    public static final int TYPE_TAB_ITEM = 105;

    /** The type `Table`. */
    public static final int TYPE_TABLE = 106;

    /** The type `TableColumn`. */
    public static final int TYPE_TABLE_COLUMN = 107;

    /** The type `TableCursor`. */
    public static final int TYPE_TABLE_CURSOR = 108;

    /** The type `TableEditor`. */
    public static final int TYPE_TABLE_EDITOR = 109;

    /** The type `TableItem`. */
    public static final int TYPE_TABLE_ITEM = 110;

    /** The type `TaskBar`. */
    public static final int TYPE_TASK_BAR = 111;

    /** The type `TaskItem`. */
    public static final int TYPE_TASK_ITEM = 112;

    /** The type `Text`. */
    public static final int TYPE_TEXT = 113;

    /** The type `TextStyle`. */
    public static final int TYPE_TEXT_STYLE = 114;

    /** The type `TextTransfer`. */
    public static final int TYPE_TEXT_TRANSFER = 115;

    /** The type `ToolBar`. */
    public static final int TYPE_TOOL_BAR = 116;

    /** The type `ToolItem`. */
    public static final int TYPE_TOOL_ITEM = 117;

    /** The type `ToolTip`. */
    public static final int TYPE_TOOL_TIP = 118;

    /** The type `Touch`. */
    public static final int TYPE_TOUCH = 119;

    /** The type `TouchSource`. */
    public static final int TYPE_TOUCH_SOURCE = 120;

    /** The type `Tracker`. */
    public static final int TYPE_TRACKER = 121;

    /** The type `Transfer`. */
    public static final int TYPE_TRANSFER = 122;

    /** The type `TransferData`. */
    public static final int TYPE_TRANSFER_DATA = 123;

    /** The type `Transform`. */
    public static final int TYPE_TRANSFORM = 124;

    /** The type `Tray`. */
    public static final int TYPE_TRAY = 125;

    /** The type `TrayItem`. */
    public static final int TYPE_TRAY_ITEM = 126;

    /** The type `Tree`. */
    public static final int TYPE_TREE = 127;

    /** The type `TreeColumn`. */
    public static final int TYPE_TREE_COLUMN = 128;

    /** The type `TreeCursor`. */
    public static final int TYPE_TREE_CURSOR = 129;

    /** The type `TreeEditor`. */
    public static final int TYPE_TREE_EDITOR = 130;

    /** The type `TreeItem`. */
    public static final int TYPE_TREE_ITEM = 131;

    /** The type `URLTransfer`. */
    public static final int TYPE_URLTRANSFER = 132;

    /** The type `ViewForm`. */
    public static final int TYPE_VIEW_FORM = 133;

    /** The type `WidgetSizeBridge`. */
    public static final int TYPE_WIDGET_SIZE_BRIDGE = 134;

    /** The display's `activeShellId`. */
    public static final int DISPLAY_ACTIVE_SHELL_ID = 183;

    /** The display's `config`. */
    public static final int DISPLAY_CONFIG = 184;

    /** The display's `focusHolderId`. */
    public static final int DISPLAY_FOCUS_HOLDER_ID = 185;

    /** The display's `mainShellId`. */
    public static final int DISPLAY_MAIN_SHELL_ID = 186;

    /** The display's `popups`. */
    public static final int DISPLAY_POPUPS = 187;

    /** The display's `shells`. */
    public static final int DISPLAY_SHELLS = 188;

    /** The display's `systemMenu`. */
    public static final int DISPLAY_SYSTEM_MENU = 189;

    /** The display's `tooltips`. */
    public static final int DISPLAY_TOOLTIPS = 190;

    /** The display's `windowedShellIds`. */
    public static final int DISPLAY_WINDOWED_SHELL_IDS = 191;

    /** The flag `assets_path`. */
    public static final int CONFIG_FLAGS_ASSETS_PATH = 92;

    /** The flag `bigger_icon_size`. */
    public static final int CONFIG_FLAGS_BIGGER_ICON_SIZE = 93;

    /** The flag `csd_maximize`. */
    public static final int CONFIG_FLAGS_CSD_MAXIMIZE = 94;

    /** The flag `csd_os`. */
    public static final int CONFIG_FLAGS_CSD_OS = 95;

    /** The flag `csd_placement`. */
    public static final int CONFIG_FLAGS_CSD_PLACEMENT = 96;

    /** The flag `csd_titlebar_color`. */
    public static final int CONFIG_FLAGS_CSD_TITLEBAR_COLOR = 97;

    /** The flag `ctabfolder_tab_scroll`. */
    public static final int CONFIG_FLAGS_CTABFOLDER_TAB_SCROLL = 98;

    /** The flag `ctabfolder_topright_auto_hide`. */
    public static final int CONFIG_FLAGS_CTABFOLDER_TOPRIGHT_AUTO_HIDE = 99;

    /** The flag `ctabfolder_visible_controls`. */
    public static final int CONFIG_FLAGS_CTABFOLDER_VISIBLE_CONTROLS = 100;

    /** The flag `custom_tooltip`. */
    public static final int CONFIG_FLAGS_CUSTOM_TOOLTIP = 101;

    /** The flag `decimal_separator`. */
    public static final int CONFIG_FLAGS_DECIMAL_SEPARATOR = 102;

    /** The flag `decorations_align`. */
    public static final int CONFIG_FLAGS_DECORATIONS_ALIGN = 103;

    /** The flag `disable_control_gc_overlay`. */
    public static final int CONFIG_FLAGS_DISABLE_CONTROL_GC_OVERLAY = 104;

    /** The flag `disable_evolve_icons`. */
    public static final int CONFIG_FLAGS_DISABLE_EVOLVE_ICONS = 105;

    /** The flag `disable_hover_zoom`. */
    public static final int CONFIG_FLAGS_DISABLE_HOVER_ZOOM = 106;

    /** The flag `disable_swt_canvas_colors`. */
    public static final int CONFIG_FLAGS_DISABLE_SWT_CANVAS_COLORS = 107;

    /** The flag `double_click_timeout_ms`. */
    public static final int CONFIG_FLAGS_DOUBLE_CLICK_TIMEOUT_MS = 108;

    /** The flag `focus_indicators`. */
    public static final int CONFIG_FLAGS_FOCUS_INDICATORS = 109;

    /** The flag `font_point_scale`. */
    public static final int CONFIG_FLAGS_FONT_POINT_SCALE = 110;

    /** The flag `force_theme`. */
    public static final int CONFIG_FLAGS_FORCE_THEME = 111;

    /** The flag `gc_icons_replacement`. */
    public static final int CONFIG_FLAGS_GC_ICONS_REPLACEMENT = 112;

    /** The flag `hover_delay_ms`. */
    public static final int CONFIG_FLAGS_HOVER_DELAY_MS = 113;

    /** The flag `image_disable_icons_replacement`. */
    public static final int CONFIG_FLAGS_IMAGE_DISABLE_ICONS_REPLACEMENT = 114;

    /** The flag `locale`. */
    public static final int CONFIG_FLAGS_LOCALE = 115;

    /** The flag `notification_popup`. */
    public static final int CONFIG_FLAGS_NOTIFICATION_POPUP = 116;

    /** The flag `preserve_icon_colors`. */
    public static final int CONFIG_FLAGS_PRESERVE_ICON_COLORS = 117;

    /** The flag `print_move`. */
    public static final int CONFIG_FLAGS_PRINT_MOVE = 118;

    /** The flag `push_button_margin`. */
    public static final int CONFIG_FLAGS_PUSH_BUTTON_MARGIN = 119;

    /** The flag `scale_vertical_minimum_at_top`. */
    public static final int CONFIG_FLAGS_SCALE_VERTICAL_MINIMUM_AT_TOP = 120;

    /** The flag `show_scaling_control`. */
    public static final int CONFIG_FLAGS_SHOW_SCALING_CONTROL = 121;

    /** The flag `show_theme_color_palette`. */
    public static final int CONFIG_FLAGS_SHOW_THEME_COLOR_PALETTE = 122;

    /** The flag `system_menu_bar`. */
    public static final int CONFIG_FLAGS_SYSTEM_MENU_BAR = 123;

    /** The flag `theme_color`. */
    public static final int CONFIG_FLAGS_THEME_COLOR = 124;

    /** The flag `theme_colors_by_widget`. */
    public static final int CONFIG_FLAGS_THEME_COLORS_BY_WIDGET = 125;

    /** The flag `theme_name`. */
    public static final int CONFIG_FLAGS_THEME_NAME = 126;

    /** The flag `transparent_menu_check_box`. */
    public static final int CONFIG_FLAGS_TRANSPARENT_MENU_CHECK_BOX = 127;

    /** The flag `ui_zoom`. */
    public static final int CONFIG_FLAGS_UI_ZOOM = 128;

    /** The flag `use_special_dropdown_button`. */
    public static final int CONFIG_FLAGS_USE_SPECIAL_DROPDOWN_BUTTON = 129;

    /** The flag `use_swt_border`. */
    public static final int CONFIG_FLAGS_USE_SWT_BORDER = 130;

    /** The flag `use_swt_colors`. */
    public static final int CONFIG_FLAGS_USE_SWT_COLORS = 131;

    /** The flag `use_swt_font_colors`. */
    public static final int CONFIG_FLAGS_USE_SWT_FONT_COLORS = 132;

    /** The flag `use_swt_font_colors_by_widget`. */
    public static final int CONFIG_FLAGS_USE_SWT_FONT_COLORS_BY_WIDGET = 133;

    /** The flag `use_swt_fonts`. */
    public static final int CONFIG_FLAGS_USE_SWT_FONTS = 134;

    /** The event's `button`. */
    public static final int EVENT_BUTTON = 192;

    /** The event's `character`. */
    public static final int EVENT_CHARACTER = 193;

    /** The event's `count`. */
    public static final int EVENT_COUNT = 194;

    /** The event's `currentDataTypeId`. */
    public static final int EVENT_CURRENT_DATA_TYPE_ID = 195;

    /** The event's `detail`. */
    public static final int EVENT_DETAIL = 196;

    /** The event's `doit`. */
    public static final int EVENT_DOIT = 197;

    /** The event's `dragImage`. */
    public static final int EVENT_DRAG_IMAGE = 198;

    /** The event's `end`. */
    public static final int EVENT_END = 199;

    /** The event's `feedback`. */
    public static final int EVENT_FEEDBACK = 200;

    /** The event's `gc`. */
    public static final int EVENT_GC = 201;

    /** The event's `height`. */
    public static final int EVENT_HEIGHT = 202;

    /** The event's `index`. */
    public static final int EVENT_INDEX = 203;

    /** The event's `item`. */
    public static final int EVENT_ITEM = 204;

    /** The event's `itemId`. */
    public static final int EVENT_ITEM_ID = 205;

    /** The event's `keyCode`. */
    public static final int EVENT_KEY_CODE = 206;

    /** The event's `keyLocation`. */
    public static final int EVENT_KEY_LOCATION = 207;

    /** The event's `magnification`. */
    public static final int EVENT_MAGNIFICATION = 208;

    /** The event's `operations`. */
    public static final int EVENT_OPERATIONS = 209;

    /** The event's `rotation`. */
    public static final int EVENT_ROTATION = 210;

    /** The event's `segments`. */
    public static final int EVENT_SEGMENTS = 211;

    /** The event's `segmentsChars`. */
    public static final int EVENT_SEGMENTS_CHARS = 212;

    /** The event's `start`. */
    public static final int EVENT_START = 213;

    /** The event's `stateMask`. */
    public static final int EVENT_STATE_MASK = 214;

    /** The event's `text`. */
    public static final int EVENT_TEXT = 215;

    /** The event's `time`. */
    public static final int EVENT_TIME = 216;

    /** The event's `touches`. */
    public static final int EVENT_TOUCHES = 217;

    /** The event's `type`. */
    public static final int EVENT_TYPE = 218;

    /** The event's `widget`. */
    public static final int EVENT_WIDGET = 219;

    /** The event's `width`. */
    public static final int EVENT_WIDTH = 220;

    /** The event's `x`. */
    public static final int EVENT_X = 221;

    /** The event's `xDirection`. */
    public static final int EVENT_X_DIRECTION = 222;

    /** The event's `y`. */
    public static final int EVENT_Y = 223;

    /** The event's `yDirection`. */
    public static final int EVENT_Y_DIRECTION = 224;
}
