package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ThemeSystemColors;
import dev.equo.swt.size.CanvasTheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GraphicsUtils;
import org.eclipse.swt.graphics.RGB;

import java.util.HashMap;
import java.util.Map;

/**
 * The system colours that follow the theme's light/dark variant ({@link Config#systemColorsAreDark}).
 * A Display hands out one instance per id, tagged with that id, so the copy a control stores of it
 * is still known to be that system colour. When the variant changes at runtime, {@link #followVariant}
 * re-tints the instances, which applications keep and hand out later (Eclipse Forms' FormColors), and
 * every control's tagged copy, the way a native settings change moves them. An application foreground
 * that does not read on a control's system background is shown as the paired system foreground until
 * a pick makes it read again.
 */
public final class ThemedSystemColors {

    private final Map<Integer, Color> colors = new HashMap<>();

    private ThemedSystemColors() {
    }

    /** Whether {@code id} is one of the theme-dependent system colours this class hands out. */
    static boolean follows(int id) {
        return id == SWT.COLOR_WIDGET_FOREGROUND || id == SWT.COLOR_WIDGET_BACKGROUND
                || id == SWT.COLOR_LIST_FOREGROUND || id == SWT.COLOR_LIST_BACKGROUND;
    }

    /** {@code display}'s instance of a theme-dependent system colour id. */
    static Color get(DartDisplay display, int id) {
        if (display.themedSystemColors == null) display.themedSystemColors = new ThemedSystemColors();
        Map<Integer, Color> colors = display.themedSystemColors.colors;
        Color color = colors.get(id);
        if (color == null || color.isDisposed()) {
            color = GraphicsUtils.tagSystemColor(new Color(display.getApi(), rgb(display, id)), id);
            colors.put(id, color);
        }
        return color;
    }

    /** What a theme-dependent system colour id is in the variant {@code display} paints now. */
    static RGB rgb(DartDisplay display, int id) {
        // A foreground is only legible against the background it is paired with, so both come from
        // the same scheme: an application that sets neither inherits both from here.
        boolean dark = Config.systemColorsAreDark(display.darkTheme());
        RGB named = ThemeSystemColors.rgb(id, dark);
        if (named != null) return named;
        boolean foreground = id == SWT.COLOR_WIDGET_FOREGROUND || id == SWT.COLOR_LIST_FOREGROUND;
        if (!dark) {
            if (foreground) return new RGB(0, 0, 0);
            return id == SWT.COLOR_WIDGET_BACKGROUND ? new RGB(240, 240, 240) : new RGB(255, 255, 255);
        }
        CanvasTheme t = foreground ? CanvasTheme.getDarkOnSurface() : CanvasTheme.getDarkSurface();
        return new RGB(t.red(), t.green(), t.blue());
    }

    /**
     * Moves {@code display}'s theme-dependent system colours to the variant it paints now, with the
     * controls coloured from them, then sends {@code SWT.Settings} and redraws every shell so content
     * painted from them follows. UI thread only.
     */
    public static void followVariant(Display display) {
        if (display == null || display.isDisposed() || !(display.getImpl() instanceof DartDisplay)) return;
        DartDisplay impl = (DartDisplay) display.getImpl();
        if (impl.themedSystemColors != null) {
            for (Map.Entry<Integer, Color> entry : impl.themedSystemColors.colors.entrySet()) {
                if (!entry.getValue().isDisposed()) GraphicsUtils.setRGB(entry.getValue(), rgb(impl, entry.getKey()));
            }
        }
        for (Shell shell : display.getShells()) retint(impl, shell);
        impl.runSettings = true;
        impl.runSettings();
    }

    private static void retint(DartDisplay display, Control control) {
        if (control.isDisposed()) return;
        if (control.getImpl() instanceof DartControl) {
            DartControl c = (DartControl) control.getImpl();
            boolean groundMoved = retinted(display, c._background);
            // A control with no colour of its own is sent its default, which is the variant's.
            if (groundMoved || c._background == null) {
                c.getValue().markDirty(VControl.BACKGROUND);
                ControlHelper.markDamaged(c);
            }
            Color foreground = c._foreground;
            if (GraphicsUtils.standsInFor(foreground) != null) {
                // Decides again for the moved background: the application's colour may read now.
                c.setForeground(foreground);
            } else if (foreground == null || retinted(display, foreground)) {
                c.getValue().markDirty(VControl.FOREGROUND);
                ControlHelper.markDamaged(c);
            } else if (groundMoved && !foreground.isDisposed()) {
                c.setForeground(foreground);
            }
        }
        if (control instanceof Composite) {
            for (Control child : ((Composite) control).getChildren()) retint(display, child);
        }
    }

    private static boolean retinted(DartDisplay display, Color color) {
        int id = GraphicsUtils.systemColorId(color);
        if (id == 0 || color.isDisposed()) return false;
        RGB rgb = rgb(display, id);
        if (rgb.equals(color.getRGB())) return false;
        GraphicsUtils.setRGB(color, rgb);
        return true;
    }

    /** WCAG's minimum contrast for large text and user-interface components. */
    private static final double MIN_CONTRAST = 3.0;

    /**
     * The foreground {@code control} takes when the application sets {@code color}. An application's
     * own colour can be picked for a variant other than the one its system background paints now
     * (Eclipse Forms blends its title colour once, and sets it again on every paint); one below
     * {@link #MIN_CONTRAST} on such a background is shown as a copy of the system foreground paired
     * with it. The copy remembers the application's colour, which is shown again once a pick makes it
     * read, and which a copy set back on a control stands for. {@code getForeground()} answers what is
     * shown, so content the application paints from it reads too. Called by the generated
     * {@code Control.setForeground}.
     */
    static Color legibleForeground(DartControl control, Color color) {
        Color own = GraphicsUtils.standsInFor(color);
        if (own != null) color = own;
        Color ground = control._background;
        if (color == null || color.isDisposed() || GraphicsUtils.systemColorId(color) != 0
                || ground == null || ground.isDisposed()) return color;
        int paired = pairedForeground(GraphicsUtils.systemColorId(ground));
        if (paired == 0 || contrast(color.getRGB(), ground.getRGB()) >= MIN_CONTRAST) return color;
        Color shown = GraphicsUtils.copyColor(get((DartDisplay) control.getDisplay().getImpl(), paired));
        return GraphicsUtils.standIn(shown, color);
    }

    private static int pairedForeground(int backgroundId) {
        switch (backgroundId) {
            case SWT.COLOR_LIST_BACKGROUND: return SWT.COLOR_LIST_FOREGROUND;
            case SWT.COLOR_WIDGET_BACKGROUND: return SWT.COLOR_WIDGET_FOREGROUND;
            default: return 0;
        }
    }

    /** WCAG 2 contrast ratio of two colours, from 1 to 21. */
    public static double contrast(RGB a, RGB b) {
        double la = relativeLuminance(a), lb = relativeLuminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double relativeLuminance(RGB rgb) {
        return 0.2126 * linear(rgb.red) + 0.7152 * linear(rgb.green) + 0.0722 * linear(rgb.blue);
    }

    private static double linear(int channel) {
        double c = channel / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
