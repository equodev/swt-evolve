package dev.equo.swt;

/**
 * The ports a web Display's page is served on when nothing names one. The browser keys its HTTP cache
 * and V8's compiled code by origin, and the port is part of it, so a page served on a new port every
 * launch downloads and compiles {@code main.dart.js} and CanvasKit again each time. Each application,
 * for each user, gets the same few ports: a second instance takes the next one, and a port another
 * process holds is skipped.
 *
 * <p>The range is below every OS's default ephemeral range (Linux starts at 32768, macOS and Windows at
 * 49152), so an outgoing connection rarely holds one of these ports.
 */
final class StableWebPort {

    /** Turns the stable ports off ({@code false}): every launch is served on a new ephemeral port. */
    static final String PROPERTY = "dev.equo.swt.web.stableOrigin";

    static final int FIRST = 20_000;
    static final int COUNT = 10_000;
    /** How many instances of one application can each have a stable origin. */
    static final int CANDIDATES = 4;

    private StableWebPort() {
    }

    /**
     * This application's ports, in the order they are tried. None when sessions share the JVM: each
     * session's page is reached through its host, and its port is not any one session's to keep.
     */
    static int[] candidates() {
        if (org.eclipse.swt.internal.cloudready.DefaultDisplayResolver.isSet()) return new int[0];
        if ("false".equalsIgnoreCase(System.getProperty(PROPERTY))) return new int[0];
        return candidates(System.getProperty("user.name", ""), applicationIdentity());
    }

    static int[] candidates(String user, String application) {
        int[] ports = new int[CANDIDATES];
        for (int i = 0; i < CANDIDATES; i++) {
            ports[i] = FIRST + Math.floorMod(mix((user + '\n' + application + '\n' + i).hashCode()), COUNT);
        }
        return ports;
    }

    /**
     * What tells this application from another started by the same user: an Eclipse product, else the
     * application it runs, together with where it is installed; otherwise the main class or jar.
     */
    static String applicationIdentity() {
        String product = firstNonBlank(System.getProperty("eclipse.product"), System.getProperty("eclipse.application"));
        if (product != null) {
            String installArea = System.getProperty("osgi.install.area");
            return product + (installArea != null ? '@' + installArea : "");
        }
        String command = System.getProperty("sun.java.command", "").trim();
        int space = command.indexOf(' ');
        return space < 0 ? command : command.substring(0, space);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v;
        }
        return null;
    }

    /** Spreads {@link String#hashCode}, whose values for strings differing in their last character are adjacent. */
    private static int mix(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }
}
