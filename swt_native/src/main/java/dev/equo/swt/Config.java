package dev.equo.swt;

import org.eclipse.swt.widgets.Composite;

public class Config {

    public enum Impl { eclipse, equo }

    static Impl defaultImpl = Impl.valueOf(System.getProperty("dev.equo.swt.default", Impl.eclipse.name()));

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
        if (defaultImpl == Impl.equo)
           System.setProperty(getKey(clazz), "");
        else if (defaultImpl == Impl.eclipse)
           System.clearProperty(getKey(clazz));
    }

    public static boolean isEquo(Class<?> clazz) {
        if (defaultImpl == Impl.equo && notNegatedDefault(clazz))
            return true;
        if (defaultImpl == Impl.eclipse && !notNegatedDefault(clazz))
            return true;
        return false;
    }

    public static boolean isEquo(Class<?> clazz, Composite parent) {
        Object data = parent.getData(getKey(clazz));
        if (data != null) {
            if (Impl.equo.equals(data)) return true;
            else if (Impl.eclipse.equals(data)) return false;
            else if (data instanceof String) return Impl.equo.name().equals(data);
        }
        return isEquo(clazz);
    }

    private static boolean notNegatedDefault(Class<?> clazz) {
        return System.getProperty(getKey(clazz)) == null;
    }

    private static String getKey(Class<?> clazz) {
        return PROPERTY_PREFIX + clazz.getSimpleName();
    }
}
