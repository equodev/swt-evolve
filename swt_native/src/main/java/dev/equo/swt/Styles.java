package dev.equo.swt;

public class Styles {

    public static boolean hasFlags(int style, int flags) {
        return (style & flags) == flags;
    }

}
