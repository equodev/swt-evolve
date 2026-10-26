package dev.equo.swt.harness;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import java.util.ArrayList;
import java.util.List;

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
        for (String[] entry : BatchEntries.of(frames)) {
            String channel = entry[0];
            if (!channel.startsWith("GC/"))
                continue;
            JsonElement body = JsonParser.parseString(entry[1]);
            if (channel.endsWith("/fillRectangleintintintint")) {
                JsonArray r = body.getAsJsonArray();
                Rectangle area = new Rectangle(r.get(0).getAsInt() + Math.round(dx), r.get(1).getAsInt() + Math.round(dy),
                        r.get(2).getAsInt(), r.get(3).getAsInt());
                fills.add(new Fill(area, image ? null : background, image ? new Point(Math.round(dx), Math.round(dy)) : null));
            } else if (body.isJsonObject()) {
                JsonObject state = body.getAsJsonObject();
                if (state.has("background"))
                    background = rgb(state.get("background"));
                if (state.has("backgroundPattern"))
                    image = state.get("backgroundPattern").isJsonObject()
                            && state.getAsJsonObject("backgroundPattern").has("image");
                if (state.has("transform")) {
                    JsonElement transform = state.get("transform");
                    JsonArray elements = transform.isJsonObject() ? transform.getAsJsonObject().getAsJsonArray("elements") : null;
                    dx = elements == null ? 0 : elements.get(4).getAsFloat();
                    dy = elements == null ? 0 : elements.get(5).getAsFloat();
                }
            }
        }
        return fills;
    }

    /** A colour as the GC's full state spells it, or as a state change carries it, ARGB in one number. */
    private static RGB rgb(JsonElement colour) {
        if (colour.isJsonNull())
            return null;
        if (colour.isJsonObject()) {
            JsonObject c = colour.getAsJsonObject();
            return new RGB(c.get("r").getAsInt(), c.get("g").getAsInt(), c.get("b").getAsInt());
        }
        long argb = colour.getAsLong();
        return new RGB((int) (argb >> 16) & 0xFF, (int) (argb >> 8) & 0xFF, (int) argb & 0xFF);
    }
}
