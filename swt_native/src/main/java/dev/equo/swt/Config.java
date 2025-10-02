package dev.equo.swt;

import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import java.util.Map;

import static java.util.Map.entry;

public class Config {

    public enum Impl {eclipse, equo, force_equo}

    static Impl defaultImpl = Impl.valueOf(System.getProperty("dev.equo.swt.default", Impl.equo.name()));
    static Impl toolbarImpl = Impl.valueOf(System.getProperty("dev.equo.swt.toolbar", Impl.eclipse.name()));

    private static final String os = System.getProperty("os.name").toLowerCase();
    static final Map<Class<?>, Impl> equoEnabled;
    private static boolean toolBarDrawn;

    static {
        try {
            equoEnabled = Map.ofEntries(
                    entry(Button.class, Impl.equo),
                    entry(CTabFolder.class, Impl.equo),
                    entry(CTabItem.class, Impl.equo),
                    entry(CTabFolderRenderer.class, Impl.equo),
                    entry(Class.forName("org.eclipse.swt.custom.CTabFolderLayout"), Impl.equo),
                    entry(StyledText.class, Impl.equo),
                    entry(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"), Impl.equo),
                    entry(Table.class, Impl.equo),
                    entry(TableItem.class, Impl.equo),
                    entry(TableColumn.class, Impl.equo)
                    //entry(Tree.class, Impl.equo),
                    //entry(TreeItem.class, Impl.equo),
                    //entry(TreeColumn.class, Impl.equo),
                    //entry(Canvas.class, Impl.equo)
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static final String PROPERTY_PREFIX = "dev.equo.swt.";
    static final String DART = "Dart";

    public static void defaultToEquo() {
        defaultImpl = Impl.equo;
    }

    public static void forceEquo() {
        defaultImpl = Impl.force_equo;
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

    public static boolean isEquo(Class<?> clazz) {
        String forcedImpl = System.getProperty(getKey(clazz));
        if (forcedImpl != null) {
            return Impl.equo.name().equals(forcedImpl);
        }

        if (isEditor(clazz)) {
            return false;
        }
        if (defaultImpl == Impl.force_equo)
            return true;
        if ((defaultImpl == Impl.equo && equoEnabled.containsKey(clazz)))
            return true;
        return isCreatedInsideDart();
    }

    private static boolean isCreatedInsideDart() {
        StackWalker walker = StackWalker.getInstance();

        for (int skip : new int[]{3, 4}) {
            StackWalker.StackFrame frame = walker.walk(stream -> stream.skip(skip).findFirst().orElse(null));
            if (frame != null && frame.getFileName() != null && frame.getFileName().startsWith(DART)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEquo(Class<?> clazz, Drawable parent) {
        return parent instanceof Canvas && clazz == GC.class && ((Canvas) parent).getImpl() instanceof DartCanvas;
    }

    public static boolean isEquo(Class<?> clazz, Scrollable parent) {
        if (clazz == Composite.class && isMainToolbarComposite(clazz, (Composite) parent)) {
            return true;
        }
        if (isEditor(clazz)) {
            return false;
        }
        /// This is used because Eclipse creates "hidden" toolbars as children of the shell
        if (clazz == ToolBar.class && parent instanceof Shell) {
            return true;
        }

        if (isAncestorOf(parent, DartCTabFolder.class) && isToolBar()) {
            return true;
        }

        Object data = parent != null ? parent.getData(getKey(clazz)) : null;
        if (data != null) {
            if (Impl.equo.equals(data)) return true;
            else if (Impl.eclipse.equals(data)) return false;
            else if (data instanceof String) return Impl.equo.name().equals(data);
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
        if (isEquo(clazz))
            return true;
        if (parent != null && clazz == Caret.class && parent.getImpl().getClass().getSimpleName().startsWith(DART))
            return true;

        return parent != null && parent.getImpl().getClass().getSimpleName().startsWith(DART) && !isCTabFolderBody(clazz, parent);
    }

    private static final String E4_MAIN_TOOLBAR_CLASS = "org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout";
    private static final String E4_MAIN_TOOLBAR_METHOD = "getTrimComposite";

    private static boolean isMainToolbarComposite(Class<?> clazz, Composite parent) {
        String id = getId(clazz, parent);
        return id.equals("//Shell//-1//Composite//1") && isInStackTrace(E4_MAIN_TOOLBAR_CLASS, E4_MAIN_TOOLBAR_METHOD);
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
        if (!toolBarDrawn && toolbarImpl == Impl.equo && isMainToolbar(Composite.class, parent)) {
            toolBarDrawn = true;
            return new DartMainToolbar(parent, style, composite);
        }
        return Config.isEquo(Composite.class, parent) ? new DartComposite(parent, style, composite) : new SwtComposite(parent, style, composite);
    }

    private static boolean isMainToolbar(Class<?> clazz, Composite parent) {
        String id = getId(clazz, parent);
        if (id.startsWith("//Shell//-1//Composite//") && (id.endsWith("0") || id.endsWith("1") || id.endsWith("2"))) { // it changes on first launch
            return isInStackTraceAtSkip("org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine",
                    "subscribeTopicToBeRendered", 11);
        }
        return false;
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

    private static String getId(Class<?> clazz, Composite parent) {
        String id = "";
        while (clazz != null) {
            id = "//" + clazz.getSimpleName() + "//" + (parent != null && parent.getChildren() != null ? parent.getChildren().length : -1) + id;
            clazz = (parent != null) ? parent.getClass() : null;
            parent = (parent != null) ? parent.getParent() : null;
        }
        return id;
    }

    private static String getKey(Class<?> clazz) {
        return PROPERTY_PREFIX + clazz.getSimpleName();
    }

    private static ConfigFlags configFlags;

    public static ConfigFlags getConfigFlags() {
        if (configFlags == null) {
            configFlags = new ConfigFlags();
            configFlags.ctabfolder_visible_controls = Boolean.getBoolean("swt.evolve.ctabfolder_visible_controls");
            configFlags.image_disable_icons_replacement = Boolean.getBoolean("swt.evolve.image_disable_icons_replacement");
            configFlags.assets_path = System.getProperty("swt.evolve.assets_path");
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
}
