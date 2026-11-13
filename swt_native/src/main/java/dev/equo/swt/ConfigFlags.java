package dev.equo.swt;


import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import dev.equo.swt.DecorationsAlign;
import org.eclipse.swt.graphics.RGB;

public class ConfigFlags implements WireObject {

    /**
     * System property selecting the rendering surface: {@link #MODE_DESKTOP} (a native top-level
     * window), {@link #MODE_CHROMIUM} (the Equo Chromium standalone window), or unset for the
     * default web surface (a browser). Read live via {@link #mode()} / {@link #isDesktopMode()} /
     * {@link #isChromiumMode()} — it can be set at runtime (e.g. by the desktop bridge), so it is
     * intentionally not cached with the rest of the flags below.
     */
    public static final String MODE_PROPERTY = "dev.equo.swt.mode";
    public static final String MODE_DESKTOP = "desktop";
    public static final String MODE_CHROMIUM = "chromium";

    /** The current rendering mode, or {@code null} when unset (the default web surface). */
    public static String mode() {
        return System.getProperty(MODE_PROPERTY);
    }

    /** True under the desktop-native surface ({@code -Ddev.equo.swt.mode=desktop}). */
    public static boolean isDesktopMode() {
        return MODE_DESKTOP.equalsIgnoreCase(mode());
    }

    /** True under the Equo Chromium standalone surface ({@code -Ddev.equo.swt.mode=chromium}). */
    public static boolean isChromiumMode() {
        return MODE_CHROMIUM.equalsIgnoreCase(mode());
    }

    /** Sets the rendering mode. Centralizes the write the way {@link #mode()} centralizes the read. */
    public static void setMode(String mode) {
        System.setProperty(MODE_PROPERTY, mode);
    }

    public boolean ctabfolder_visible_controls;

    /** Default true (matches the historical behaviour): the CTabFolder topRight/minimize/
     *  maximize/chevron block only appears on hover and floats on top of the tab row without
     *  reserving space. Set to false to keep it always shown and reserving its own width in the
     *  tab row (a Row sibling) instead -- lets a client that relies on an always-visible
     *  topRight toolbar (e.g. via setTopRight) keep every tab reachable instead of some being
     *  hidden underneath that floating overlay. */
    public boolean ctabfolder_topright_auto_hide;

    /** Default true: a CTabFolder whose tabs do not fit scrolls its tab strip. Set to false to lay
     *  the tabs out as SWT does: they shrink towards minimumCharacters and the ones that still do
     *  not fit go behind the chevron. */
    public boolean ctabfolder_tab_scroll;

    /** With the tab scroll off, a CTabFolder draws its chevron right after the last showing tab and
     *  lays its tabs out where they are drawn, so the chevron's list opens at the chevron, as in SWT.
     *  Off by default: the chevron stays at the strip's end. Set with
     *  {@code -Dswt.evolve.ctabfolder_chevron_after_tabs=true}. */
    public boolean ctabfolder_chevron_after_tabs;

    public boolean image_disable_icons_replacement;

    public String assets_path;

    /**
     * True keeps the application's own icons. Off by default, which lets Evolve substitute an icon
     * with one of ours — the bundled icon set and the name-to-Material-icon map. An
     * {@code assets_path} override applies either way.
     */
    public boolean disable_evolve_icons;

    /**
     * Lets the bundled icon set stand in for an image the application blits itself with
     * {@code GC#drawImage}. Off by default: the set is keyed by the bare filename stem, so any
     * application image sharing a name with one of ours would be replaced by it, drawn at the
     * replacement's own size. Turn it on for an application that wants its GC-painted icons themed.
     * An {@code assets_path} override reaches a blit either way, since Java resolves it up front.
     */
    public boolean gc_icons_replacement;

    public boolean use_swt_colors;

    public boolean disable_swt_canvas_colors;

    /**
     * {@link #disable_swt_canvas_colors} per control class
     * ({@code -Dswt.evolve.disable_swt_canvas_colors_<fully.qualified.Class>=true|false}), keyed by
     * the class name as written. It covers the controls of that class or a subclass of it, and every
     * control they contain; the nearest listed control wins, and a control under none follows the
     * global flag. Java-only: the client gets the outcome per GC as {@code VGC.themedCanvas}.
     */
    public Map<String, Boolean> disable_swt_canvas_colors_by_class;

    /**
     * The fill colors a control class paints a hover with
     * ({@code -Dswt.evolve.canvas_hover_fills_<fully.qualified.Class>=RRGGBB,...}), keyed by the
     * class name as written; resolved like {@link #disable_swt_canvas_colors_by_class}. Java-only:
     * the client gets the outcome per GC as {@code VGC.hoverFills}.
     */
    public Map<String, RGB[]> canvas_hover_fills_by_class;

    /** Like {@link #canvas_hover_fills_by_class}, for a selection ({@code canvas_selection_fills_<Class>}). */
    public Map<String, RGB[]> canvas_selection_fills_by_class;

    /** Drops what an application paints through a Control's own GC, leaving the control's own
     *  rendering alone. A control that draws itself and is then painted over -- a check box whose
     *  label the application redraws -- shows that drawing twice. Canvas and StyledText keep
     *  painting their GC content whatever this says. */
    public boolean disable_control_gc_overlay;

    /** Text in the application's font ({@code -Dswt.use_swt_fonts=true}). Off, the theme keeps its family and
     *  size and the font adds only weight and slant; {@code getTextStyle} and the {@code *Sizes} follow this. */
    public boolean use_swt_fonts;

    /**
     * Draws text in the foreground the application set rather than the theme's
     * ({@code -Dswt.use_swt_font_colors}). Null follows {@link #use_swt_fonts}.
     */
    public Boolean use_swt_font_colors;

    /**
     * {@link #use_swt_font_colors} for one widget class, keyed by its lower-case SWT name
     * ({@code -Dswt.evolve.use_swt_font_colors_tree=true}); it wins over the global value. An item follows
     * the control that draws it: a TreeItem answers to {@code tree}.
     */
    public Map<String, Boolean> use_swt_font_colors_by_widget;

    public boolean use_special_dropdown_button;

    public boolean preserve_icon_colors;

    /**
     * Turns off the hover zoom Evolve does by default -- a {@code ToolItem} grows its icon, a Table
     * cell grows its content. Native SWT has no such feedback, so an application that wants to
     * render exactly like it sets this. How far and how fast things grow are theme values, not
     * flags.
     */
    public boolean disable_hover_zoom;

    /**
     * Draws a main-toolbar {@code ToolItem}'s tooltip as a card -- icon, title, body and
     * keyboard-shortcut hint -- instead of the one-line strip {@code setToolTipText} gets
     * elsewhere. Off by default, and scoped to the main toolbar: everything outside it keeps the
     * plain tooltip either way. Colours, type and geometry are theme values
     * ({@code TooltipThemeExtension.rich*}), not flags.
     */
    public boolean custom_tooltip;

    /**
     * Raises a transient card in the corner of the window when a main-toolbar item is clicked. SWT
     * has no such widget, so there is nothing for an application to ask for -- this is the whole
     * feature, and it is off by default. The item's own action is untouched: the card is raised
     * alongside it, never instead of it.
     */
    public boolean notification_popup;

    public String force_theme;
    public String theme_name;
    public String theme_color;
    public Map<String, String> theme_colors_by_widget;
    public boolean show_theme_color_palette;

    /** Focus rings and focus-coloured borders; null leaves it to the named theme's default. */
    public Boolean focus_indicators;

    public boolean show_scaling_control;

    /** The zoom SWT draws its UI at, in percent — {@code swt.autoScale} already applied to the
     *  monitor's own zoom. The render layer scales itself by this over the monitor zoom it reports,
     *  so both halves of a mixed tree end up at the same size. 0 while it is still unknown. */
    public int ui_zoom;

    public DecorationsAlign decorations_align;

    public boolean print_move;

    /** Client-Side Decorations placement and on/off in one value: "toolbar" (default),
     *  "overlay", "floating", or "false" (disabled). */
    public String csd_placement;
    /** Host OS for picking native control styling: "mac", "windows", or "linux". */
    public String csd_os;
    /** Maximize behavior: "direct" (default, Dart calls window.equo.maximize()), "bounds",
     *  "native", or "fullscreen" (the last three delegate to the SWT bridge). */
    public String csd_maximize;

    /** True when the Shell's menu bar is published to an OS-owned menu bar instead of drawn inside
     *  the window, so the Flutter side must not draw one of its own. macOS only, and only where this
     *  process owns the window: elsewhere the menu bar belongs in the window, and in a browser tab
     *  the system bar belongs to the browser. */
    public boolean system_menu_bar;

    /** The decimal separator of the JVM's locale, which a desktop client writes numbers with. A browser
     *  client uses its own locale's instead, the one it reports to the Display. */
    public String decimal_separator;

    /** The JVM's locale as a language tag, which a desktop client writes dates in. A browser client uses its own. */
    public String locale;

    /** Double-click pairing window in milliseconds for the shared DoubleTapDetector on the Flutter
     *  side. 0 (the default) keeps Flutter's kDoubleTapTimeout; a positive value overrides it, e.g.
     *  for a test harness whose synthetic click pairs land further apart than a real user's. */
    public int double_click_timeout_ms;
    /** How long the pointer rests before a control reports MouseHover and shows its tooltip, in
     *  milliseconds; Display.getToolTipTime() answers it. */
    public int hover_delay_ms;
    /** Explicit CSD title-bar colour (RRGGBB/AARRGGBB hex); overrides the theme's own. */
    public String csd_titlebar_color;

    /** Logical pixels per SWT font point on this host, so the render side paints point-sized text
     *  at the size {@link FontMetricsUtil#dpiScale()} measured it at. The client cannot derive it:
     *  its own platform is the browser's, not the one the application is laid out for. Carries the
     *  host's DPI only — {@link FontMetricsUtil#rawDpiScale()} — with the zoom left for each side to
     *  divide by out of its own live copy. */
    public double font_point_scale;

    /** Transparent margin, in pixels, on each side of a push or toggle button: the preferred width
     *  reserves it and the render side paints the button inside it. For an application that lays
     *  its buttons out expecting a native frame wider than what the button draws, and overlaps
     *  adjacent frames accordingly. 0 (the default) paints the button over its whole bounds.
     *  Set with {@code -Dswt.evolve.push_button_margin=<px>}. */
    public int push_button_margin;

    /** Measures every {@code ToolItem} image as an icon of the theme's size, whatever the size of the
     *  image, so every ToolBar draws its icons at that one size and none is shrunk to fit its bar.
     *  A ToolBar in a {@code CTabFolder}'s tab row (the folder's own buttons, and its top-right
     *  control and the bars inside it) keeps its image size, so it fits that row.
     *  Off by default: an item is measured from its image, as the platform does. Turn it on with
     *  {@code -Dswt.evolve.bigger_icon_size=true}; it only takes effect when icons are replaced
     *  (Evolve's set, or an {@code assets_path} pack). */
    public boolean bigger_icon_size;

    /** Draws a CHECK or RADIO menu item without its box or ring: only the mark, in the item text
     *  colour, while the item is selected, as native menus do. Off by default: the theme's box is
     *  drawn. Set with {@code -Dswt.evolve.transparent_menu_check_box=true}. */
    public boolean transparent_menu_check_box;

    /** Draws a bordered ({@code SWT.BORDER}) CTabFolder's outline as the native renderer does: one line
     *  round the folder, joined over the selected tab. Off by default: the theme's flat frame is drawn.
     *  Set with {@code -Dswt.use_swt_border=true}. */
    public boolean use_swt_border;

    /** Puts a vertical Scale's minimum at the top, as GTK and Win32 do, so Up lowers it. Off by
     *  default: the minimum is at the bottom, as on Cocoa, and Up raises it. Set with
     *  {@code -Dswt.evolve.scale_vertical_minimum_at_top=true}. */
    public boolean scale_vertical_minimum_at_top;

    /** Not a type a value names at runtime: the flags only ever travel as the display's. */
    @Override
    public int wireTypeId() {
        return 0;
    }

    /** The fields {@link #writeWireFields} writes: the generator numbers them from this list (WireTable.CONFIG_FLAGS_*). */
    private static final String[] WIRE_FIELDS = {
            "ctabfolder_visible_controls", "ctabfolder_topright_auto_hide", "image_disable_icons_replacement",
            "assets_path", "disable_evolve_icons", "gc_icons_replacement", "use_swt_colors",
            "disable_swt_canvas_colors", "disable_control_gc_overlay", "use_swt_fonts", "use_swt_font_colors",
            "use_swt_font_colors_by_widget", "use_special_dropdown_button", "preserve_icon_colors",
            "disable_hover_zoom", "custom_tooltip", "notification_popup", "force_theme", "theme_name",
            "theme_color", "theme_colors_by_widget", "show_theme_color_palette", "focus_indicators",
            "show_scaling_control", "ui_zoom", "decorations_align", "print_move", "csd_placement", "csd_os",
            "csd_maximize", "system_menu_bar", "double_click_timeout_ms", "csd_titlebar_color",
            "font_point_scale", "push_button_margin", "hover_delay_ms", "ctabfolder_tab_scroll",
            "decimal_separator", "locale", "transparent_menu_check_box", "use_swt_border",
            "scale_vertical_minimum_at_top", "ctabfolder_chevron_after_tabs",
            "bigger_icon_size",
    };

    /** The flags the client sets (a theme picked on its side). */
    @Override
    public boolean readWire(int key, WireReader r) {
        switch (key) {
            case WireTable.CONFIG_FLAGS_FORCE_THEME: force_theme = r.string(); return true;
            case WireTable.CONFIG_FLAGS_THEME_NAME: theme_name = r.string(); return true;
            case WireTable.CONFIG_FLAGS_THEME_COLOR: theme_color = r.string(); return true;
        }
        return false;
    }

    /** The flags as an object of the binary widget wire. */
    @Override
    public void writeWireFields(WidgetWire w) {
        w.key(WireTable.CONFIG_FLAGS_CTABFOLDER_VISIBLE_CONTROLS);
        w.write(ctabfolder_visible_controls);
        w.key(WireTable.CONFIG_FLAGS_CTABFOLDER_TOPRIGHT_AUTO_HIDE);
        w.write(ctabfolder_topright_auto_hide);
        w.key(WireTable.CONFIG_FLAGS_IMAGE_DISABLE_ICONS_REPLACEMENT);
        w.write(image_disable_icons_replacement);
        w.key(WireTable.CONFIG_FLAGS_ASSETS_PATH);
        w.write(assets_path);
        w.key(WireTable.CONFIG_FLAGS_DISABLE_EVOLVE_ICONS);
        w.write(disable_evolve_icons);
        w.key(WireTable.CONFIG_FLAGS_GC_ICONS_REPLACEMENT);
        w.write(gc_icons_replacement);
        w.key(WireTable.CONFIG_FLAGS_USE_SWT_COLORS);
        w.write(use_swt_colors);
        w.key(WireTable.CONFIG_FLAGS_DISABLE_SWT_CANVAS_COLORS);
        w.write(disable_swt_canvas_colors);
        w.key(WireTable.CONFIG_FLAGS_DISABLE_CONTROL_GC_OVERLAY);
        w.write(disable_control_gc_overlay);
        w.key(WireTable.CONFIG_FLAGS_USE_SWT_FONTS);
        w.write(use_swt_fonts);
        w.key(WireTable.CONFIG_FLAGS_USE_SWT_FONT_COLORS);
        w.write(use_swt_font_colors);
        w.key(WireTable.CONFIG_FLAGS_USE_SWT_FONT_COLORS_BY_WIDGET);
        w.writeBooleans(use_swt_font_colors_by_widget);
        w.key(WireTable.CONFIG_FLAGS_USE_SPECIAL_DROPDOWN_BUTTON);
        w.write(use_special_dropdown_button);
        w.key(WireTable.CONFIG_FLAGS_PRESERVE_ICON_COLORS);
        w.write(preserve_icon_colors);
        w.key(WireTable.CONFIG_FLAGS_DISABLE_HOVER_ZOOM);
        w.write(disable_hover_zoom);
        w.key(WireTable.CONFIG_FLAGS_CUSTOM_TOOLTIP);
        w.write(custom_tooltip);
        w.key(WireTable.CONFIG_FLAGS_NOTIFICATION_POPUP);
        w.write(notification_popup);
        w.key(WireTable.CONFIG_FLAGS_FORCE_THEME);
        w.write(force_theme);
        w.key(WireTable.CONFIG_FLAGS_THEME_NAME);
        w.write(theme_name);
        w.key(WireTable.CONFIG_FLAGS_THEME_COLOR);
        w.write(theme_color);
        w.key(WireTable.CONFIG_FLAGS_THEME_COLORS_BY_WIDGET);
        w.writeStrings(theme_colors_by_widget);
        w.key(WireTable.CONFIG_FLAGS_SHOW_THEME_COLOR_PALETTE);
        w.write(show_theme_color_palette);
        w.key(WireTable.CONFIG_FLAGS_FOCUS_INDICATORS);
        w.write(focus_indicators);
        w.key(WireTable.CONFIG_FLAGS_SHOW_SCALING_CONTROL);
        w.write(show_scaling_control);
        w.key(WireTable.CONFIG_FLAGS_UI_ZOOM);
        w.write(ui_zoom);
        w.key(WireTable.CONFIG_FLAGS_DECORATIONS_ALIGN);
        w.write(decorations_align == null ? null : decorations_align.name());
        w.key(WireTable.CONFIG_FLAGS_PRINT_MOVE);
        w.write(print_move);
        w.key(WireTable.CONFIG_FLAGS_CSD_PLACEMENT);
        w.write(csd_placement);
        w.key(WireTable.CONFIG_FLAGS_CSD_OS);
        w.write(csd_os);
        w.key(WireTable.CONFIG_FLAGS_CSD_MAXIMIZE);
        w.write(csd_maximize);
        w.key(WireTable.CONFIG_FLAGS_SYSTEM_MENU_BAR);
        w.write(system_menu_bar);
        w.key(WireTable.CONFIG_FLAGS_DOUBLE_CLICK_TIMEOUT_MS);
        w.write(double_click_timeout_ms);
        w.key(WireTable.CONFIG_FLAGS_CSD_TITLEBAR_COLOR);
        w.write(csd_titlebar_color);
        w.key(WireTable.CONFIG_FLAGS_FONT_POINT_SCALE);
        w.write(font_point_scale);
        w.key(WireTable.CONFIG_FLAGS_PUSH_BUTTON_MARGIN);
        w.write(push_button_margin);
        w.key(WireTable.CONFIG_FLAGS_HOVER_DELAY_MS);
        w.write(hover_delay_ms);
        w.key(WireTable.CONFIG_FLAGS_CTABFOLDER_TAB_SCROLL);
        w.write(ctabfolder_tab_scroll);
        w.key(WireTable.CONFIG_FLAGS_DECIMAL_SEPARATOR);
        w.write(decimal_separator);
        w.key(WireTable.CONFIG_FLAGS_LOCALE);
        w.write(locale);
        w.key(WireTable.CONFIG_FLAGS_TRANSPARENT_MENU_CHECK_BOX);
        w.write(transparent_menu_check_box);
        w.key(WireTable.CONFIG_FLAGS_USE_SWT_BORDER);
        w.write(use_swt_border);
        w.key(WireTable.CONFIG_FLAGS_SCALE_VERTICAL_MINIMUM_AT_TOP);
        w.write(scale_vertical_minimum_at_top);
        w.key(WireTable.CONFIG_FLAGS_CTABFOLDER_CHEVRON_AFTER_TABS);
        w.write(ctabfolder_chevron_after_tabs);
        w.key(WireTable.CONFIG_FLAGS_BIGGER_ICON_SIZE);
        w.write(bigger_icon_size);
    }

    public static ConfigFlags use_swt_fonts(boolean v) {
        ConfigFlags configFlags = new ConfigFlags();
        configFlags.use_swt_fonts = v;
        return Config.setConfigFlags(configFlags);
    }

    @Override
    public String toString() {
        return "ConfigFlags{" +
                "mode=" + mode() +
                ", ctabfolder_visible_controls=" + ctabfolder_visible_controls +
                ", ctabfolder_topright_auto_hide=" + ctabfolder_topright_auto_hide +
                ", ctabfolder_tab_scroll=" + ctabfolder_tab_scroll +
                ", image_disable_icons_replacement=" + image_disable_icons_replacement +
                ", assets_path=" + assets_path +
                ", disable_evolve_icons=" + disable_evolve_icons +
                ", use_swt_colors=" + use_swt_colors +
                ", disable_swt_canvas_colors=" + disable_swt_canvas_colors +
                ", disable_swt_canvas_colors_by_class=" + disable_swt_canvas_colors_by_class +
                ", canvas_hover_fills_by_class=" + fills(canvas_hover_fills_by_class) +
                ", canvas_selection_fills_by_class=" + fills(canvas_selection_fills_by_class) +
                ", disable_control_gc_overlay=" + disable_control_gc_overlay +
                ", use_swt_fonts=" + use_swt_fonts +
                ", use_swt_font_colors=" + use_swt_font_colors +
                ", use_swt_font_colors_by_widget=" + use_swt_font_colors_by_widget +
                ", force_theme='" + force_theme + '\'' +
                ", theme_name='" + theme_name + '\'' +
                ", theme_color='" + theme_color + '\'' +
                ", theme_colors_by_widget=" + theme_colors_by_widget +
                ", show_theme_color_palette=" + show_theme_color_palette +
                ", focus_indicators=" + focus_indicators +
                ", preserve_icon_colors=" + preserve_icon_colors +
                ", disable_hover_zoom=" + disable_hover_zoom +
                ", custom_tooltip=" + custom_tooltip +
                ", notification_popup=" + notification_popup +
                ", show_scaling_control=" + show_scaling_control +
                ", decorations_align='" + decorations_align + '\'' +
                ", csd_placement='" + csd_placement + '\'' +
                ", csd_os='" + csd_os + '\'' +
                ", csd_maximize='" + csd_maximize + '\'' +
                ", double_click_timeout_ms=" + double_click_timeout_ms +
                ", hover_delay_ms=" + hover_delay_ms +
                ", csd_titlebar_color='" + csd_titlebar_color + '\'' +
                ", font_point_scale=" + font_point_scale +
                ", push_button_margin=" + push_button_margin +
                ", bigger_icon_size=" + bigger_icon_size +
                ", transparent_menu_check_box=" + transparent_menu_check_box +
                ", use_swt_border=" + use_swt_border +
                ", scale_vertical_minimum_at_top=" + scale_vertical_minimum_at_top +
                ", ctabfolder_chevron_after_tabs=" + ctabfolder_chevron_after_tabs +
                ", system_menu_bar=" + system_menu_bar +
                ", decimal_separator='" + decimal_separator + '\'' +
                ", locale='" + locale + '\'' +
                '}';
    }

    private static String fills(Map<String, RGB[]> byClass) {
        if (byClass == null) return "null";
        Map<String, String> printable = new TreeMap<>();
        for (Map.Entry<String, RGB[]> entry : byClass.entrySet()) {
            printable.put(entry.getKey(), Arrays.toString(entry.getValue()));
        }
        return printable.toString();
    }
}
