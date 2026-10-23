package dev.equo.swt;

import org.eclipse.swt.graphics.RGB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The application fill colors a control class declared a hover or a selection
 * ({@code -Dswt.evolve.canvas_fill_roles_<Class>=RRGGBB:hover,RRGGBB:selection}).
 * A role with no color is null, so it stays off the wire.
 */
public final class CanvasFillRoles {

    public final RGB[] hover;
    public final RGB[] selection;

    CanvasFillRoles(RGB[] hover, RGB[] selection) {
        this.hover = hover;
        this.selection = selection;
    }

    /**
     * The entries of a fill-roles value that are a six-digit color (an optional {@code #} before
     * it) and a known role, in any case; null when none is.
     */
    public static CanvasFillRoles parse(String value) {
        if (value == null) return null;
        List<RGB> hover = new ArrayList<>();
        List<RGB> selection = new ArrayList<>();
        for (String entry : value.split(",")) {
            String[] parts = entry.split(":", 2);
            if (parts.length != 2) continue;
            String color = parts[0].trim();
            if (color.startsWith("#")) color = color.substring(1);
            if (!color.matches("[0-9a-fA-F]{6}")) continue;
            int rgb = Integer.parseInt(color, 16);
            RGB fill = new RGB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
            String role = parts[1].trim().toLowerCase(Locale.ROOT);
            if (role.equals("hover")) hover.add(fill);
            else if (role.equals("selection")) selection.add(fill);
        }
        if (hover.isEmpty() && selection.isEmpty()) return null;
        return new CanvasFillRoles(toArray(hover), toArray(selection));
    }

    @Override
    public String toString() {
        return "hover=" + Arrays.toString(hover) + ", selection=" + Arrays.toString(selection);
    }

    private static RGB[] toArray(List<RGB> colors) {
        return colors.isEmpty() ? null : colors.toArray(new RGB[0]);
    }
}
