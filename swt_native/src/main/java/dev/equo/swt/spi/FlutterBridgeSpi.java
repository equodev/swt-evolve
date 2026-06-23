package dev.equo.swt.spi;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Classloader-shareable holder for the host-integration SPI: the
 * display-created notification plus the web-server-URL lookup.
 *
 * <p>Expressed purely in terms of {@code java.*} types (Object / String /
 * functional interfaces) so it carries <b>no</b> dependency on SWT widgets,
 * {@code WebFlutterServer}, or {@code Serializer}. 
 * <p>The actual {@code Display} instances always originate from the bundle and
 * are carried as {@code Object} across the boundary, so no SWT class identity
 * ever crosses classloaders.
 *
 */
public final class FlutterBridgeSpi {

    private FlutterBridgeSpi() {}

    private static volatile Consumer<Object> displayCreatedListener = d -> {};
    private static volatile Function<Object, String> webServerUrlLookup = d -> null;
    private static volatile ToIntFunction<Object> commPortLookup = d -> -1;

    public static void registerDisplayCreatedListener(Consumer<Object> listener) {
        if (listener != null) displayCreatedListener = listener;
    }

    public static void notifyDisplayCreated(Object display) {
        if (display != null) displayCreatedListener.accept(display);
    }

    public static void registerWebServerUrlLookup(Function<Object, String> fn) {
        if (fn != null) webServerUrlLookup = fn;
    }

    public static String getWebServerUrl(Object display) {
        return webServerUrlLookup.apply(display);
    }

    public static void registerCommPortLookup(ToIntFunction<Object> fn) {
        if (fn != null) commPortLookup = fn;
    }

    public static int getCommPort(Object display) {
        return display != null ? commPortLookup.applyAsInt(display) : -1;
    }
}
