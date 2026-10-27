package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A pane docked on the right of a shell, resized from its left edge the way e4's
 * {@code TrimPaneLayout} resizes a fast view: the press captures the pointer and stores
 * {@link Display#getCursorLocation()}; every move adds the cursor's delta to the pane's left edge,
 * calls {@code setBounds} and {@code display.update()}, and stores the cursor again.
 *
 * <p>Natively the edge follows the pointer one to one. Every move is printed with an {@code [pane]}
 * prefix: the event position, the cursor SWT reports and the pane's bounds.
 */
public class FastViewEdgeDragSnippet {

    static final int EDGE = 4;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Fast view edge drag");
        shell.setSize(900, 600);

        Composite pane = new Composite(shell, SWT.BORDER);
        pane.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        Label label = new Label(pane, SWT.NONE);
        label.setText("Drag my left edge");
        label.setBounds(20, 20, 200, 20);
        Rectangle area = shell.getClientArea();
        pane.setBounds(area.width - 300, 0, 300, area.height);
        new EdgeResize(pane);

        shell.open();
        log("shell=" + shell.getBounds() + " pane=" + pane.getBounds()
                + " pane origin(display)=" + display.map(pane, null, 0, 0));
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    static class EdgeResize {
        final Composite pane;
        Point cursor;

        EdgeResize(Composite pane) {
            this.pane = pane;
            Display display = pane.getDisplay();
            pane.addListener(SWT.MouseDown, e -> {
                if (e.button != 1 || e.x > EDGE)
                    return;
                cursor = display.getCursorLocation();
                pane.setCapture(true);
                log("down e=" + e.x + "," + e.y + " cursor=" + cursor + " bounds=" + pane.getBounds());
            });
            pane.addListener(SWT.MouseMove, e -> {
                if (cursor == null) {
                    pane.setCursor(e.x <= EDGE ? display.getSystemCursor(SWT.CURSOR_SIZEWE) : null);
                    return;
                }
                Point now = display.getCursorLocation();
                int delta = now.x - cursor.x;
                Rectangle bounds = pane.getBounds();
                bounds.x += delta;
                bounds.width -= delta;
                pane.setBounds(bounds);
                display.update();
                cursor = now;
                log("move e=" + e.x + "," + e.y + " cursor=" + now + " delta=" + delta + " bounds="
                        + pane.getBounds());
            });
            pane.addListener(SWT.MouseUp, e -> {
                if (cursor == null)
                    return;
                cursor = null;
                pane.setCapture(false);
                log("up e=" + e.x + "," + e.y + " cursor=" + display.getCursorLocation() + " bounds="
                        + pane.getBounds());
            });
        }
    }

    static void log(String line) {
        System.out.println("[pane] " + line);
    }
}
