package org.eclipse.swt.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The distinct styles a renderer's runs are named by, in the order first used. A style that stops
 * being used keeps its place, so one that comes and goes (a link, an occurrence mark) changes only
 * the runs' names, not the palette the client holds. It starts over past {@link #MAX} entries, and
 * when a style it kept holds a colour or font the application has disposed since.
 */
final class StylePalette {

    static final int MAX = 128;

    private final List<StyleRange> entries = new ArrayList<>();
    private final Map<StyleRange, Integer> places = new HashMap<>();
    /** Changes whenever an entry is added or the palette starts over. */
    private int version;

    /** The place of {@code entry}, a style at no place, added at the end when new. */
    int placeOf(StyleRange entry) {
        Integer place = places.get(entry);
        if (place != null) return place;
        place = entries.size();
        entries.add(entry);
        places.put(entry, place);
        version++;
        return place;
    }

    /** Whether it must start over: too long, or keeping a style that can no longer be sent. */
    boolean stale() {
        if (entries.size() > MAX) return true;
        for (StyleRange entry : entries) {
            if (disposed(entry.foreground) || disposed(entry.background) || disposed(entry.underlineColor)
                    || disposed(entry.strikeoutColor) || disposed(entry.borderColor)
                    || (entry.font != null && entry.font.isDisposed())) {
                return true;
            }
        }
        return false;
    }

    private static boolean disposed(org.eclipse.swt.graphics.Color colour) {
        return colour != null && colour.isDisposed();
    }

    void restart() {
        entries.clear();
        places.clear();
        version++;
    }

    int version() {
        return version;
    }

    StyleRange[] toArray() {
        return entries.toArray(new StyleRange[0]);
    }
}
