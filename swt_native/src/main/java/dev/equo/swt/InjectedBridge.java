package dev.equo.swt;

/**
 * Holds the bridge a harness injects ({@link FlutterBridge#set}) apart from {@link FlutterBridge}, so
 * asking whether there is one does not initialize that class, which registers every widget codec.
 */
public final class InjectedBridge {

    static volatile FlutterBridge bridge;

    private InjectedBridge() {}

    /** Whether a harness injected a bridge, which then owns the comm, the server and the browser. */
    public static boolean present() {
        return bridge != null;
    }
}
