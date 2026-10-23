package dev.equo.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The application fill colors a control class declared a hover or a selection
 * ({@code -Dswt.evolve.canvas_fill_roles_<Class>=RRGGBB:hover,RRGGBB:selection}), as 0xRRGGBB.
 * A role with no color is null, so it stays off the wire.
 */
public final class CanvasFillRoles {

    public final int[] hover;
    public final int[] selection;

    CanvasFillRoles(int[] hover, int[] selection) {
        this.hover = hover;
        this.selection = selection;
    }

    /**
     * The entries of a fill-roles value that are a six-digit color (an optional {@code #} before
     * it) and a known role, in any case; null when none is.
     */
    public static CanvasFillRoles parse(String value) {
        if (value == null) return null;
        List<Integer> hover = new ArrayList<>();
        List<Integer> selection = new ArrayList<>();
        for (String entry : value.split(",")) {
            String[] parts = entry.split(":", 2);
            if (parts.length != 2) continue;
            String color = parts[0].trim();
            if (color.startsWith("#")) color = color.substring(1);
            if (!color.matches("[0-9a-fA-F]{6}")) continue;
            int rgb = Integer.parseInt(color, 16);
            String role = parts[1].trim().toLowerCase(Locale.ROOT);
            if (role.equals("hover")) hover.add(rgb);
            else if (role.equals("selection")) selection.add(rgb);
        }
        if (hover.isEmpty() && selection.isEmpty()) return null;
        return new CanvasFillRoles(toArray(hover), toArray(selection));
    }

    @Override
    public String toString() {
        return "hover=" + hex(hover) + ", selection=" + hex(selection);
    }

    private static String hex(int[] colors) {
        if (colors == null) return "[]";
        List<String> hex = new ArrayList<>();
        for (int rgb : colors) hex.add(String.format("%06X", rgb));
        return hex.toString();
    }

    private static int[] toArray(List<Integer> colors) {
        if (colors.isEmpty()) return null;
        int[] array = new int[colors.size()];
        for (int i = 0; i < array.length; i++) array[i] = colors.get(i);
        return array;
    }
}
