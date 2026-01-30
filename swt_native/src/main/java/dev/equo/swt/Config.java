package dev.equo.swt;

import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public class Config {

    public enum Impl {eclipse, equo, force_equo}

    static Impl defaultImpl = Impl.valueOf(System.getProperty("dev.equo.swt.default", Impl.equo.name()));
    static Impl mainToolbarImpl = Impl.valueOf(System.getProperty("dev.equo.swt.maintoolbar", defaultImpl.name()));
    static Impl sideBarImpl = Impl.valueOf(System.getProperty("dev.equo.swt.sidebar", defaultImpl.name()));

    private static final String os = System.getProperty("os.name").toLowerCase();
    static final Map<Class<?>, Impl> equoEnabled;
    private static boolean forceEclipse = false;

    static boolean idTracker = Boolean.getBoolean("dev.equo.swt.tracker");
    static IdWidgetTracker widgetTracker;

    static {
        try {
            equoEnabled = Map.ofEntries(
                    entry(Button.class, Impl.equo),
                    entry(Label.class, Impl.equo),
                    entry(CTabFolder.class, Impl.equo),
                    entry(CTabItem.class, Impl.equo),
                    entry(CTabFolderRenderer.class, Impl.equo),
                    entry(Class.forName("org.eclipse.swt.custom.CTabFolderLayout"), Impl.equo),
                    entry(StyledText.class, Impl.equo),
                    entry(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"), Impl.equo),
                    entry(Table.class, Impl.equo),
                    entry(TableItem.class, Impl.equo),
                    entry(TableColumn.class, Impl.equo),
                    entry(Text.class, Impl.equo),
                    entry(Link.class, Impl.equo),
                    //entry(Group.class, Impl.equo),
                    //entry(ExpandBar.class, Impl.equo),
                    //entry(ExpandItem.class, Impl.equo),
                    //entry(Sash.class, Impl.equo),
                    entry(List.class, Impl.equo),
                    entry(Combo.class, Impl.equo),
                    entry(CCombo.class, Impl.equo),
                    entry(CLabel.class, Impl.equo),
                    entry(ToolBar.class, Impl.equo),
                    entry(Tree.class, Impl.equo),
                    entry(TreeItem.class, Impl.equo),
                    entry(TreeColumn.class, Impl.equo)
                    //entry(Canvas.class, Impl.equo),
                    //entry(ScrolledComposite.class, Impl.equo)
                    //entry(Menu.class, Impl.equo)
                    //entry(MenuItem.class, Impl.equo),
                    //entry(TabFolder.class, Impl.equo),
                    //entry(TabItem.class, Impl.equo),
                    //entry(CoolBar.class, Impl.equo),
                    //entry(ProgressBar.class, Impl.equo),
                    //entry(Scale.class, Impl.equo),
                    //entry(Slider.class, Impl.equo),
                    //entry(Spinner.class, Impl.equo),
                    //entry(ToolTip.class, Impl.equo),
                    //entry(Shell.class, Impl.equo),
                    //entry(Composite.class, Impl.equo),
                    //entry(DateTime.class, Impl.equo),
                    //entry(Tray.class, Impl.equo),
                    //entry(TrayItem.class, Impl.equo)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static final String PROPERTY_PREFIX = "dev.equo.swt.";
    static final String DART = "Dart";

    /**
     * Widget dependency groups. All widgets in the same group should be activated together.
     * When any widget in a group is activated via system property, all other widgets in the
     * same group are also activated (unless explicitly disabled).
     */
    static final java.util.List<Set<String>> WIDGET_DEPENDENCY_GROUPS = java.util.List.of(
            // TabFolder group
            Set.of("TabFolder", "TabItem"),
            // CTabFolder group
            Set.of("CTabFolder", "CTabItem", "CTabFolderLayout", "CTabFolderRenderer"),
            // Table group
            Set.of("Table", "TableColumn", "TableItem"),
            // Tree group
            Set.of("Tree", "TreeColumn", "TreeItem"),
            // ToolBar group
            Set.of("ToolBar", "ToolItem"),
            // CoolBar group
            Set.of("CoolBar", "CoolItem"),
            // Menu group
            Set.of("Menu", "MenuItem"),
            // ExpandBar group
            Set.of("ExpandBar", "ExpandItem"),
            // StyledText group
            Set.of("StyledText", "StyledTextRenderer"),
            // TaskBar group
            Set.of("TaskBar", "TaskItem")
    );

    /** Cache mapping widget simple names to their dependency group */
    private static final Map<String, Set<String>> widgetToGroupCache = new HashMap<>();

    static {
        // Build the cache for fast lookup
        for (Set<String> group : WIDGET_DEPENDENCY_GROUPS) {
            for (String widget : group) {
                widgetToGroupCache.put(widget, group);
            }
        }
    }

    /**
     * Gets the dependency group for a widget, or null if it has no dependencies.
     */
    static Set<String> getDependencyGroup(String widgetSimpleName) {
        return widgetToGroupCache.get(widgetSimpleName);
    }

    /**
     * Gets the dependency group for a widget class.
     */
    static Set<String> getDependencyGroup(Class<?> clazz) {
        return getDependencyGroup(clazz.getSimpleName());
    }

    public static void defaultToEquo() {
        defaultImpl = Impl.equo;
    }

    public static void forceEquo() {
        defaultImpl = Impl.force_equo;
    }

    public static void forceEclipse() {
        forceEclipse = true;
    }

    public static void defaultToEclipse() {
        defaultImpl = Impl.eclipse;
    }

    public static void useEquo(Class<?> clazz) {
        System.setProperty(getKey(clazz), Impl.equo.name());
    }

    public static void useEclipse(Class<?> clazz) {
        System.setProperty(getKey(clazz), Impl.eclipse.name());
    }

    public static void clear(Class<?> clazz) {
        System.clearProperty(getKey(clazz));
    }

    public static boolean isEquo(Class<?> clazz) {
        if (forceEclipse) return false;
        // Per-widget override
        if (isEquoForced(clazz)) {
            return true;
        }

        if (defaultImpl == Impl.force_equo)
            return true;

        if ((defaultImpl == Impl.equo && equoEnabled.containsKey(clazz)))
            return true;

        if (isCreatedInsideDart())
            return true;

        if (defaultImpl == Impl.eclipse) {
            return false;
        }

        return false;
    }

    private static boolean isCreatedInsideDart() {
        StackWalker walker = StackWalker.getInstance();

        for (int skip : new int[]{3, 4}) {
            StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(skip).findFirst().orElse(null));
            if (frame != null && frame.getFileName() != null && frame.getFileName().startsWith(DART)) {
                return true;
            }
            if (frame != null && frame.getFileName().startsWith("SwtFont"))
                break;
        }
        return false;
    }

    public static boolean isEquo(Class<?> clazz, Drawable parent) {
        if (forceEclipse) return false;
        // Per-widget override
        if (isEquoForced(clazz)) {
            return true;
        }

        if (parent instanceof Canvas && clazz == GC.class && ((Canvas) parent).getImpl() instanceof DartCanvas)
            return true;

        if (defaultImpl == Impl.eclipse)
            return false;

        if (defaultImpl == Impl.force_equo)
            return true;

        return false;
    }

    public static boolean isEquo(Class<?> clazz, org.eclipse.swt.widgets.Menu parent) {
        if (forceEclipse) return false;
        // MenuItem should use the same implementation as its parent Menu
        return parent != null && parent.getImpl() instanceof org.eclipse.swt.widgets.DartMenu;
    }

    static boolean isEquoForced(Class<?> clazz) {
        // First check if this specific widget is explicitly set
        String forcedImpl = System.getProperty(getKey(clazz));
        if (forcedImpl != null) {
            // If explicitly set to eclipse, respect that
            if (Impl.eclipse.name().equals(forcedImpl)) {
                return false;
            }
            // If explicitly set to equo, return true
            if (Impl.equo.name().equals(forcedImpl)) {
                return true;
            }
        }

        // Check if any widget in the same dependency group is forced to equo
        // (only if this widget is not explicitly disabled)
        Set<String> dependencyGroup = getDependencyGroup(clazz);
        if (dependencyGroup != null) {
            for (String dependentWidget : dependencyGroup) {
                String dependentImpl = System.getProperty(PROPERTY_PREFIX + dependentWidget);
                if (dependentImpl != null && Impl.equo.name().equals(dependentImpl)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isEquo(Class<?> clazz, Scrollable parent) {
        if (idTracker && widgetTracker == null) {
            widgetTracker = new IdWidgetTracker();
            widgetTracker.startTracking();
        }

        if (forceEclipse) return false;

        // Per-id override
        if (parent instanceof Composite c) {
            String id = getId(clazz, c);
            String forcedImpl = System.getProperty(PROPERTY_PREFIX+id);
            if (forcedImpl != null) {
                return Impl.equo.name().equals(forcedImpl);
            }
        }
        // Per-widget override
        if (isEquoForced(clazz)) {
            return true;
        }

        // Check parent data for per-widget override
        Object data = parent != null ? parent.getData(getKey(clazz)) : null;
        if (data != null) {
            if (Impl.equo.equals(data)) return true;
            else if (Impl.eclipse.equals(data)) return false;
            else if (data instanceof String) return Impl.equo.name().equals(data);
        }

        if (defaultImpl == Impl.force_equo)
            return true;

        /// This is used because Eclipse creates "hidden" toolbars as children of the shell
        if (clazz == ToolBar.class && parent instanceof Shell && (isEquoForced(CTabFolder.class) || defaultImpl == Impl.equo)) {
            return true;
        }

        if (isAncestorOf(parent, DartCTabFolder.class) && isToolBar()) {
            return true;
        }

        if (isCustomAncestor(parent))
            return true;
        // Special handling for Canvas: use Equo implementation when created from FigureCanvas
        if (clazz == Canvas.class) {
            StackWalker.StackFrame caller = StackWalker.getInstance()
                    .walk(stream -> stream.skip(2).findFirst().orElse(null));
            if (caller != null && caller.getClassName().contains("FigureCanvas"))
                return true;
        }
        if (parent != null && clazz == Caret.class && parent.getImpl().getClass().getSimpleName().startsWith(DART))
            return true;
        if (parent != null && parent.getImpl().getClass().getSimpleName().startsWith(DART) && !isCTabFolderBody(clazz, parent))
            return true;
        return isEquo(clazz);
    }

    private static final String E4_MAIN_TOOLBAR_CLASS = "org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout";
    private static final String E4_MAIN_TOOLBAR_METHOD = "getTrimComposite";

    private static boolean isMainToolbarComposite(Class<?> clazz, Composite parent) {
        String id = getId(clazz, parent);
        return id.equals("/Shell/-1/Composite/1") && isInStackTrace(E4_MAIN_TOOLBAR_CLASS, E4_MAIN_TOOLBAR_METHOD);
    }

    private static boolean isSideToolbarComposite(Class<?> clazz, Composite parent) {
        String id = getId(clazz, parent);
        return (id.equals("/Shell/-1/Composite/3") || id.equals("/Shell/-1/Composite/4")) && isInStackTrace(E4_MAIN_TOOLBAR_CLASS, E4_MAIN_TOOLBAR_METHOD);
    }

    private static boolean isCTabFolderBody(Class<?> clazz, Scrollable parent) {
        return clazz == Composite.class && parent.getClass() == CTabFolder.class;
    }

    static boolean isCustomAncestor(Scrollable parent) {
        Class<DartMainToolbar> dartMainToolbarClass = DartMainToolbar.class;
        return isAncestorOf(parent, dartMainToolbarClass);
    }

    private static boolean isAncestorOf(Scrollable parent, Class<?> classType) {
        while (parent != null) {
            if (classType.isInstance(parent.getImpl()))
                return true;
            parent = parent.getImpl()._parent();
        }
        return false;
    }

    public static IWidget getCompositeImpl(Composite parent, int style, Composite composite) {
        if (Config.isEquo(Composite.class, parent))
            return new DartComposite(parent, style, composite);
        // In eclipse mode, always use SWT implementation without any special handling
        if (defaultImpl == Impl.eclipse || forceEclipse)
            return new SwtComposite(parent, style, composite);
        if (mainToolbarImpl == Impl.equo && isMainToolbarComposite(Composite.class, parent))
            return new DartMainToolbar(parent, style, composite);
        if (sideBarImpl == Impl.equo && isSideToolbarComposite(Composite.class, parent))
            return new DartSideBar(parent, style, composite);
        return new SwtComposite(parent, style, composite);
    }

    private static final String EDITOR_CLASS = "org.eclipse.ui.texteditor.AbstractTextEditor";

    private static boolean isEditor(Class<?> clazz) {
        return (clazz == StyledText.class || clazz.getSimpleName().equals("StyledTextRenderer")) &&
                isInStackTrace(EDITOR_CLASS);
    }

    private static boolean isInStackTrace(String className) {
        return isInStackTrace(className, null);
    }

    private static boolean isInStackTrace(String className, String methodName) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        return walker.walk(frames ->
                frames.anyMatch(f -> className.equals(f.getClassName()) &&
                        (methodName == null || methodName.equals(f.getMethodName())))
        );
    }

    private static boolean isInStackTraceAtSkip(String className, String methodName, int skip) {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        StackWalker.StackFrame frame = walker.walk(stream ->
                stream.skip(skip).findFirst().orElse(null));

        return frame != null && className.equals(frame.getClassName()) &&
                methodName.equals(frame.getMethodName());
    }

    private static final String E4_CLASS = "org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer";
    private static final String E4_METHOD = "addTopRight";

    private static boolean isToolBar() {
        return isInStackTrace(E4_CLASS, E4_METHOD);
    }

    static String getId(Class<?> clazz, Composite parent) {
        String id = "";
        Class<?> idClass = clazz;
        Composite idParent = parent;
        while (idClass != null) {
            String simpleName = idClass.getSimpleName();
            if (simpleName.isEmpty()) {
                String[] split = idClass.getName().split("\\.");
                simpleName = split[split.length-1];
            }
            id = "/" + simpleName + "/" + (idParent != null && idParent.getChildren() != null ? idParent.getChildren().length : -1) + id;
            idClass = (idParent != null) ? idParent.getClass() : null;
            idParent = (idParent != null) ? idParent.getParent() : null;
        }
        return id;
    }

    private static String getKey(Class<?> clazz) {
        return PROPERTY_PREFIX + clazz.getSimpleName();
    }

    private static ConfigFlags configFlags;

    public static ConfigFlags setConfigFlags(ConfigFlags flags) {
        configFlags = flags;
        return configFlags;
    }

    public static ConfigFlags getConfigFlags() {
        if (configFlags == null) {
            configFlags = new ConfigFlags();
            configFlags.ctabfolder_visible_controls = Boolean.getBoolean("swt.evolve.ctabfolder_visible_controls");
            configFlags.image_disable_icons_replacement = Boolean.getBoolean("swt.evolve.image_disable_icons_replacement");
            configFlags.assets_path = System.getProperty("swt.evolve.assets_path");
            configFlags.use_swt_colors = Boolean.getBoolean("swt.use_swt_colors");
            configFlags.use_swt_fonts = Boolean.getBoolean("swt.use_swt_fonts");
            configFlags.preserve_icon_colors = Boolean.getBoolean("swt.evolve.preserve_icon_colors");
        }
        return configFlags;
    }

    static String getSwtBaseClassName(Class<?> clazz) {
        Class<?> target = clazz;

        if (clazz.isAnonymousClass()) {
            target = clazz.getSuperclass();
        } else {
            while (target.getSuperclass() != null) {
                String pkg = target.getPackageName();
                if (pkg.startsWith("org.eclipse.swt.widgets") ||
                        pkg.startsWith("org.eclipse.swt.custom")) {
                    break;
                }
                target = target.getSuperclass();
            }
        }

        return target.getSimpleName();
    }

    public static String asString() {
        StringBuilder buffer = new StringBuilder("=== CONFIG ===");
        buffer.append("defaultImpl=").append(defaultImpl).append("\n");
        buffer.append("mainToolbarImpl=").append(mainToolbarImpl).append("\n");
        buffer.append("sideBarImpl=").append(sideBarImpl).append("\n");
        buffer.append("forceEclipse=").append(forceEclipse).append("\n");
        System.getProperties().forEach((k, v) -> {
            if (k.toString().startsWith(PROPERTY_PREFIX))
                buffer.append(k).append("=").append(v).append("\n");
        });
        return buffer.toString();
    }

}
