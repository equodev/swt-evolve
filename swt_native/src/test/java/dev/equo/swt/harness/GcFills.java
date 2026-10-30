package dev.equo.swt.harness;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The rectangles a GC filled, replayed from its recorded traffic: where each one lands on the drawable and
 * what paints it, so a test reads what was painted rather than which calls painted it. Transforms are taken
 * to be translations.
 */
public final class GcFills {

    /**
     * One filled rectangle in the drawable's coordinates: with {@code colour} when the GC's background
     * filled it, or with the background image repeated from {@code imageOrigin}.
     */
    public record Fill(Rectangle area, RGB colour, Point imageOrigin) {

        public static Fill colour(int x, int y, int width, int height, RGB colour) {
            return new Fill(new Rectangle(x, y, width, height), colour, null);
        }

        public static Fill image(int x, int y, int width, int height, int originX, int originY) {
            return new Fill(new Rectangle(x, y, width, height), null, new Point(originX, originY));
        }
    }

    private GcFills() {
    }

    public static List<Fill> of(List<RecordingComm.Frame> frames) {
        List<Fill> fills = new ArrayList<>();
        RGB background = null;
        boolean image = false;
        float dx = 0, dy = 0;
        for (GcFrames.Entry entry : BatchEntries.of(frames)) {
            String channel = entry.channel();
            if (!channel.startsWith("GC/"))
                continue;
            Object body = entry.value();
            if (channel.endsWith("/fillRectangleintintintint")) {
                List<?> r = (List<?>) body;
                Rectangle area = new Rectangle(number(r.get(0)) + Math.round(dx), number(r.get(1)) + Math.round(dy),
                        number(r.get(2)), number(r.get(3)));
                fills.add(new Fill(area, image ? null : background, image ? new Point(Math.round(dx), Math.round(dy)) : null));
            } else if (body instanceof Map) {
                Map<?, ?> state = (Map<?, ?>) body;
                if (state.containsKey("background"))
                    background = rgb(state.get("background"));
                if (state.containsKey("backgroundPattern"))
                    image = state.get("backgroundPattern") instanceof Map
                            && ((Map<?, ?>) state.get("backgroundPattern")).containsKey("image");
                if (state.containsKey("transform")) {
                    Object transform = state.get("transform");
                    List<?> elements = transform instanceof Map ? (List<?>) ((Map<?, ?>) transform).get("elements") : null;
                    dx = elements == null ? 0 : ((Number) elements.get(4)).floatValue();
                    dy = elements == null ? 0 : ((Number) elements.get(5)).floatValue();
                }
            }
        }
        return fills;
    }

    private static int number(Object v) {
        return ((Number) v).intValue();
    }

    /** A colour as the wire reads back: {@code #aarrggbb}, its channels, or ARGB in one number. */
    private static RGB rgb(Object colour) {
        if (colour == null)
            return null;
        if (colour instanceof Map) {
            Map<?, ?> c = (Map<?, ?>) colour;
            return new RGB(number(c.get("r")), number(c.get("g")), number(c.get("b")));
        }
        long argb = colour instanceof String ? Long.parseLong(((String) colour).substring(1), 16) : ((Number) colour).longValue();
        return new RGB((int) (argb >> 16) & 0xFF, (int) (argb >> 8) & 0xFF, (int) argb & 0xFF);
    }
}
