package dev.equo.swt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Java 8 stand-ins for the handful of newer JDK methods this shared code used to call.
 *
 * <p>{@code src/main} is compiled into every platform fragment, so it can only use API the oldest
 * supported SWT release's JDK has. Unlike the Swing bridge and StackWalker (see {@link Jdk9}) these
 * all have exact Java 8 equivalents, so there is nothing to fall back to at runtime — they are just
 * written the way Java 8 spells them.
 */
public final class Java8 {

    private Java8() {}

    /**
     * {@code Map.of(...)} for an even number of key/value pairs, as the payloads sent over the
     * bridge. Insertion-ordered, so a payload's field order stays stable in the wire format.
     */
    public static Map<String, Object> map(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0)
            throw new IllegalArgumentException("Expected key/value pairs, got " + keyValuePairs.length + " arguments");
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2)
            m.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        return m;
    }

    /** {@code Set.of(...)}, insertion-ordered and unmodifiable. */
    @SafeVarargs
    public static <T> Set<T> set(T... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }

    /** {@code List.of(...)}, unmodifiable. */
    @SafeVarargs
    public static <T> List<T> list(T... values) {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(values)));
    }

    /** {@code URLEncoder.encode(String, Charset)} (Java 10). */
    public static String urlEncode(String value, java.nio.charset.Charset charset) {
        try {
            return java.net.URLEncoder.encode(value, charset.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(charset.name() + " is not supported", e);
        }
    }

    /** {@code URLDecoder.decode(String, Charset)} (Java 10). */
    public static String urlDecode(String value, java.nio.charset.Charset charset) {
        try {
            return java.net.URLDecoder.decode(value, charset.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(charset.name() + " is not supported", e);
        }
    }

    /**
     * {@code CompletableFuture.orTimeout} (Java 9). Fails the future with a
     * {@link java.util.concurrent.TimeoutException} if it has not completed in time; the timer is
     * cancelled as soon as it does, so a future that completes normally costs nothing.
     */
    public static <T> java.util.concurrent.CompletableFuture<T> orTimeout(
            java.util.concurrent.CompletableFuture<T> future, long timeout, java.util.concurrent.TimeUnit unit) {
        java.util.concurrent.ScheduledFuture<?> timer = TIMEOUTS.schedule(
                () -> future.completeExceptionally(new java.util.concurrent.TimeoutException()), timeout, unit);
        future.whenComplete((ignored, error) -> timer.cancel(false));
        return future;
    }

    private static final java.util.concurrent.ScheduledExecutorService TIMEOUTS =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "equo-swt-timeout");
                t.setDaemon(true);
                return t;
            });

    /** {@code Class.getPackageName()} (Java 9), without relying on a Package being loaded. */
    public static String packageName(Class<?> clazz) {
        String name = clazz.getName();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(0, dot);
    }

    /** {@code String.repeat(int)} (Java 11). */
    public static String repeat(String s, int count) {
        if (count <= 0) return "";
        StringBuilder b = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) b.append(s);
        return b.toString();
    }

    /** {@code OutputStream.writeBytes(byte[])} (Java 11). */
    public static void writeBytes(ByteArrayOutputStream out, byte[] bytes) {
        out.write(bytes, 0, bytes.length);
    }

    /** {@code InputStream.transferTo(OutputStream)} (Java 9). Returns the bytes copied. */
    public static long copy(InputStream in, java.io.OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        long total = 0;
        for (int n; (n = in.read(buf)) != -1; ) {
            out.write(buf, 0, n);
            total += n;
        }
        return total;
    }

    /** {@code InputStream}'s own {@code readAllBytes}, which arrived in Java 9. */
    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        for (int n; (n = in.read(buf)) != -1; )
            out.write(buf, 0, n);
        return out.toByteArray();
    }
}
