package dev.equo.swt;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.*;

import java.util.Map;

public class Config {

    public enum Impl { eclipse, equo, force_equo }

    static Impl defaultImpl = Impl.valueOf(System.getProperty("dev.equo.swt.default", Impl.equo.name()));
    static Impl toolbarImpl = Impl.valueOf(System.getProperty("dev.equo.swt.toolbar", Impl.equo.name()));

    static final Map<Class<?>, Impl> equoEnabled;
    private static boolean toolBarDrawn;

    static {
        try {
            equoEnabled = Map.of(
                Button.class, Impl.equo,
                CTabFolder.class, Impl.equo,
                CTabItem.class, Impl.equo,
                CTabFolderRenderer.class, Impl.equo,
                Class.forName("org.eclipse.swt.custom.CTabFolderLayout"), Impl.equo
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

    static void forceEquo() {
        defaultImpl = Impl.force_equo;
    }

    public static void defaultToEclipse() {
        defaultImpl = Impl.eclipse;
    }

    public static void useEquo(Class<?> clazz) {
        if (defaultImpl == Impl.eclipse)
           System.setProperty(getKey(clazz), "");
        else if (defaultImpl == Impl.equo)
           System.clearProperty(getKey(clazz));
    }

    public static void useEclipse(Class<?> clazz) {
       System.setProperty(getKey(clazz), Impl.eclipse.name());
    }

    public static boolean isEquo(Class<?> clazz) {
        if (defaultImpl == Impl.force_equo && notNegatedDefault(clazz, Impl.equo))
            return true;
        if ((defaultImpl == Impl.equo && equoEnabled.containsKey(clazz)) && notNegatedDefault(clazz, Impl.equo))
            return true;
        if (defaultImpl == Impl.eclipse && !notNegatedDefault(clazz, Impl.eclipse))
            return true;
        StackWalker.StackFrame caller = StackWalker.getInstance()
                .walk(stream -> stream.skip(2).findFirst().orElse(null));
        if (caller != null && caller.getFileName().startsWith(DART)) // Scrollbar and other created inside Dart widget should be also Dart
            return true;

        return false;
    }

    public static boolean isEquo(Class<?> clazz, Scrollable parent) {
        Object data = parent != null ? parent.getData(getKey(clazz)) : null;
        if (data != null) {
            if (Impl.equo.equals(data)) return true;
            else if (Impl.eclipse.equals(data)) return false;
            else if (data instanceof String) return Impl.equo.name().equals(data);
        }
        if (isCustomAncestor(parent))
            return true;
        if (isEquo(clazz))
            return true;
        return false;
    }

    private static boolean isCustomAncestor(Scrollable parent) {
        while (parent != null) {
            if (parent.getImpl() instanceof DartMainToolbar)
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
        if (id.startsWith("//Shell//-1//Composite//") && (id.endsWith("1") || id.endsWith("2"))) { // it changes on first launch
            StackWalker.StackFrame caller = StackWalker.getInstance()
                    .walk(stream -> stream.skip(11).findFirst().orElse(null));
            if (caller != null && "org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine".equals(caller.getClassName())
                    && "subscribeTopicToBeRendered".equals(caller.getMethodName()))
                return true;
        }
        return false;
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

    private static boolean notNegatedDefault(Class<?> clazz, Impl def) {
        String property = System.getProperty(getKey(clazz));
        return property == null || def.name().equals(property);
    }

    private static String getKey(Class<?> clazz) {
        return PROPERTY_PREFIX + clazz.getSimpleName();
    }
}
