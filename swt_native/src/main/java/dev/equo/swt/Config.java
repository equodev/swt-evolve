package dev.equo.swt;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import java.util.Map;

public class Config {

    public enum Impl { eclipse, equo }

    static Impl defaultImpl = Impl.valueOf(System.getProperty("dev.equo.swt.default", Impl.equo.name()));

    static final Map<Class<?>, Impl> equoEnabled = Map.of(
            Button.class, Impl.equo,
            CTabFolder.class, Impl.equo,
            CTabItem.class, Impl.equo,
            CTabFolderRenderer.class, Impl.equo
    );

    static final String PROPERTY_PREFIX = "dev.equo.swt.";


    public static void defaultToEquo() {
        defaultImpl = Impl.equo;
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
        if ((defaultImpl == Impl.equo && equoEnabled.containsKey(clazz)) && notNegatedDefault(clazz, Impl.equo))
            return true;
        if (defaultImpl == Impl.eclipse && !notNegatedDefault(clazz, Impl.eclipse))
            return true;
        return false;
    }

    public static boolean isEquo(Class<?> clazz, Composite parent) {
        Object data = parent != null ? parent.getData(getKey(clazz)) : null;
        if (data != null) {
            if (Impl.equo.equals(data)) return true;
            else if (Impl.eclipse.equals(data)) return false;
            else if (data instanceof String) return Impl.equo.name().equals(data);
        }
        return isEquo(clazz);
    }

    private static boolean notNegatedDefault(Class<?> clazz, Impl def) {
        String property = System.getProperty(getKey(clazz));
        return property == null || def.name().equals(property);
    }

    private static String getKey(Class<?> clazz) {
        return PROPERTY_PREFIX + clazz.getSimpleName();
    }
}
