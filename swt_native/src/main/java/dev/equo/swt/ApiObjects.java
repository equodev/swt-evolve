package dev.equo.swt;

/** Helpers the generated API classes share. */
public final class ApiObjects {

    private ApiObjects() {
    }

    /**
     * The API object's toString(): its impl's, or {@code Name {identityHash}} while the impl is unset, as
     * when a debugger or a log line reaches it during construction. The name drops only the package, so
     * an anonymous subclass reads {@code Outer$1}.
     */
    public static String describe(Object api, Object impl) {
        if (impl != null)
            return impl.toString();
        String name = api.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1) + " {" + System.identityHashCode(api) + "}";
    }
}
