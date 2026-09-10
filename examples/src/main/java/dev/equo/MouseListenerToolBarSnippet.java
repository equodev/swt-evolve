package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * A toolbar button that drives its own selection from raw SWT.MouseDown / SWT.MouseUp listeners
 * instead of SWT.Selection, gating on {@code e.count == 1} and {@code e.button == 1}.
 *
 * <p>Two shapes of the same control, because different interaction chromes serve them: a Composite
 * holding a child Label, and a childless Canvas that paints its own caption. Each keeps an
 * activation counter — the counter, not the drawing, is what the two shapes disagree about. The
 * log on the right shows the payload every listener actually received; run with
 * {@code -Ddev.equo.swt.forceEclipse=true} to read the same log off native SWT.
 */
public class MouseListenerToolBarSnippet {

    private static List log;

    public static void main(String[] args) {
        if (!Boolean.getBoolean("dev.equo.swt.forceEclipse")) Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setText("MouseListenerToolBarSnippet");
        shell.setSize(940, 470);
        shell.setLayout(new FillLayout());

        Composite root = new Composite(shell, SWT.NONE);
        Font big = new Font(display, "Sans", 16, SWT.BOLD);

        new ActivationButton(root, "Composite", 20, 20, true, big);
        new ActivationButton(root, "Canvas", 20, 150, false, big);

        Label hint = new Label(root, SWT.WRAP);
        hint.setBounds(20, 285, 440, 165);
        hint.setText("Click the UPPER part of each button, above the caption.\n\n"
                + "WATCH THE COUNTERS -- both must count every left click.\n\n"
                + "Before the fix, the Composite counter stays at 0 no matter how often you "
                + "click it; the Canvas counter counts normally.\n\n"
                + "The log shows why: button= and count= are what the handler tests.\n\n"
                + "The caption of the first button is a child Label, and a child takes the "
                + "pointer itself -- clicking it reaches no listener, exactly as a native SWT "
                + "child window would.");

        log = new List(root, SWT.BORDER | SWT.V_SCROLL);
        log.setBounds(480, 20, 430, 410);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        big.dispose();
        display.dispose();
    }

    private static void log(String line) {
        log.add(line);
        log.setTopIndex(log.getItemCount() - 1);
    }

    /**
     * One owner-drawn button plus its activation counter. {@code withChild} picks the shape: a
     * Composite carrying a child Label, or a childless Canvas that paints its own caption.
     */
    private static class ActivationButton {
        private final String name;
        private final Label counter;
        private int activations;
        private boolean pressed;

        ActivationButton(Composite parent, String name, int x, int y, boolean withChild, Font big) {
            this.name = name;

            Control button;
            if (withChild) {
                Composite c = new Composite(parent, SWT.BORDER);
                c.setBounds(x, y, 200, 70);
                Label caption = new Label(c, SWT.CENTER);
                caption.setBounds(0, 44, 198, 22);
                caption.setText(name + " (has a child)");
                button = c;
            } else {
                Canvas c = new Canvas(parent, SWT.BORDER);
                c.setBounds(x, y, 200, 70);
                c.addPaintListener(e -> {
                    GC gc = e.gc;
                    Rectangle area = c.getClientArea();
                    gc.setBackground(pressed ? new Color(150, 190, 240) : new Color(238, 238, 238));
                    gc.fillRectangle(area);
                    gc.drawText(name + " (childless)", 10, 24, true);
                });
                button = c;
            }

            counter = new Label(parent, SWT.NONE);
            counter.setBounds(x + 220, y + 20, 230, 34);
            counter.setFont(big);
            refreshCounter();

            hook(button);
        }

        private void refreshCounter() {
            counter.setText(name + ": " + activations);
        }

        /** The client's handler, verbatim in its guards: both edges, left button only, count == 1. */
        private void hook(Control control) {
            control.addMouseListener(new MouseAdapter() {
                boolean isMouseDownPressed = false;

                @Override
                public void mouseDown(MouseEvent e) {
                    log(String.format("%-9s down  button=%d count=%d x=%d y=%d",
                            name, e.button, e.count, e.x, e.y));
                    if (isEventOverElement(control, e) && e.button == 1 && e.count == 1) {
                        isMouseDownPressed = true;
                        pressed = true;
                        control.redraw();
                    }
                }

                @Override
                public void mouseUp(MouseEvent e) {
                    log(String.format("%-9s up    button=%d count=%d x=%d y=%d",
                            name, e.button, e.count, e.x, e.y));
                    if (isEventOverElement(control, e) && e.button == 1 && isMouseDownPressed) {
                        isMouseDownPressed = false;
                        pressed = false;
                        control.redraw();
                        activations++;
                        refreshCounter();
                        log("  -> " + name + " ACTIVATED (" + activations + ")");
                    }
                }
            });
        }
    }

    private static boolean isEventOverElement(Control control, MouseEvent e) {
        Point size = control.getSize();
        return e.x >= 0 && e.y >= 0 && e.x < size.x && e.y < size.y;
    }
}
