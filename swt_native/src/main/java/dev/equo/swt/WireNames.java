package dev.equo.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/** The wire's channel and type names by number: {@link WireTable}'s, then each {@link WireExtension}'s. */
public final class WireNames {

    private WireNames() {
    }

    private static final String[] CHANNELS;

    private static final String[] TYPES;

    static {
        List<WireExtension> extensions = new ArrayList<>();
        for (WireExtension e : ServiceLoader.load(WireExtension.class, WireExtension.class.getClassLoader())) {
            extensions.add(e);
        }
        Collections.sort(extensions, Comparator.comparingInt(WireExtension::firstChannel));
        List<String> channels = new ArrayList<>(Arrays.asList(WireTable.CHANNELS));
        List<String> types = new ArrayList<>(Arrays.asList(WireTable.TYPES));
        for (WireExtension e : extensions) {
            append(channels, e.firstChannel(), e.channels(), "channels", e);
            append(types, e.firstType(), e.types(), "types", e);
        }
        CHANNELS = channels.toArray(new String[0]);
        TYPES = types.toArray(new String[0]);
    }

    /** Channel names by number, a copy. */
    public static String[] channels() {
        return CHANNELS.clone();
    }

    /** Record type names by number, a copy. */
    public static String[] types() {
        return TYPES.clone();
    }

    private static void append(List<String> names, int first, String[] added, String what, WireExtension e) {
        if (first != names.size()) {
            throw new IllegalStateException("the " + what + " of " + e.getClass().getName() + " start at " + first
                    + ", not " + names.size() + ": it was not generated with this WireTable");
        }
        names.addAll(Arrays.asList(added));
    }
}
