package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;

/**
 * Two part stacks tiled the way the e4 workbench tiles them, to compare native and Evolve on the
 * two gestures a press near a stack's top edge can start.
 *
 * <ul>
 *   <li>A press on the band between the stacks resizes them, as e4's {@code SashLayout} does: a
 *       MouseDown on the host, capture, and a weight computed from each MouseMove's absolute
 *       position.</li>
 *   <li>A drag on a stack's tab strip opens a {@link Tracker} on DragDetect and reads the cursor
 *       on every move, as e4's {@code DnDManager} does when it moves a stack.</li>
 * </ul>
 *
 * Every step is printed with an {@code [edge]} prefix, so a run can be compared line by line.
 */
public class WorkbenchEdgeDragSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Workbench edge drag");
        shell.setLayout(new FillLayout());

        Composite host = new Composite(shell, SWT.NONE);
        StackTiling tiling = new StackTiling(host);
        host.setLayout(tiling);
        tiling.top = stack(host, "Outline", "Search");
        tiling.bottom = stack(host, "Properties", "Notes");

        shell.setSize(900, 640);
        shell.open();
        log("shell bounds=" + shell.getBounds() + " host origin(display)=" + display.map(host, null, 0, 0));
        reportCursor(display, host, 6);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    static CTabFolder stack(Composite host, String... titles) {
        CTabFolder folder = new CTabFolder(host, SWT.BORDER);
        for (String title : titles) {
            CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(title);
            Label content = new Label(folder, SWT.NONE);
            content.setText(title);
            item.setControl(content);
        }
        folder.setSelection(0);
        folder.addListener(SWT.DragDetect, e -> moveStack(folder, e.x, e.y));
        return folder;
    }

    /** What e4 does when a stack is dragged: a Tracker that reports the cursor on every move. */
    static void moveStack(CTabFolder folder, int x, int y) {
        Display display = folder.getDisplay();
        Shell shell = folder.getShell();
        log("dragDetect on " + folder.getItem(0).getText() + " at " + x + "," + y
                + " cursor(shell)=" + display.map(null, shell, display.getCursorLocation()));
        Tracker tracker = new Tracker(shell, SWT.NULL);
        tracker.setStippled(true);
        tracker.addListener(SWT.Move, e -> {
            Point cursor = display.getCursorLocation();
            log("tracker move event=" + e.x + "," + e.y + " cursor(shell)=" + display.map(null, shell, cursor)
                    + " over=" + describe(display.getCursorControl()));
        });
        shell.setCapture(true);
        boolean accepted = tracker.open();
        shell.setCapture(false);
        log("tracker closed accepted=" + accepted + " cursor(shell)="
                + display.map(null, shell, display.getCursorLocation()));
        tracker.dispose();
    }

    /** Logs where SWT puts the pointer, once a second, to compare with the screen position. */
    static void reportCursor(Display display, Composite host, int times) {
        if (times == 0 || host.isDisposed())
            return;
        Point cursor = display.getCursorLocation();
        log("cursor display=" + cursor + " host=" + display.map(null, host, cursor) + " over="
                + describe(display.getCursorControl()) + " shell=" + host.getShell().getBounds());
        display.timerExec(1000, () -> reportCursor(display, host, times - 1));
    }

    static String describe(Control control) {
        return control == null ? "null" : control.getClass().getSimpleName();
    }

    static void log(String line) {
        System.out.println("[edge] " + line);
    }

    /**
     * Tiles two stacks top to bottom with a draggable band between them. Named after the e4 layout
     * it stands in for, because Evolve recognises the workbench's sash area by that name.
     */
    static class StackTiling extends Layout {
        static final int SASH = 4;
        static final int MIN_PERCENT = 10;

        final Composite host;
        Control top;
        Control bottom;
        int topPercent = 50;
        boolean dragging;

        StackTiling(Composite host) {
            this.host = host;
            host.addListener(SWT.MouseDown, e -> {
                boolean onBand = e.button == 1 && band().intersects(new Rectangle(e.x - 5, e.y - 5, 10, 10));
                log("host mouseDown at " + e.x + "," + e.y + " band=" + band() + " onBand=" + onBand);
                if (onBand) {
                    dragging = true;
                    host.setCapture(true);
                }
            });
            host.addListener(SWT.MouseMove, e -> {
                if (!dragging) {
                    if (band().intersects(new Rectangle(e.x - 5, e.y - 5, 10, 10)))
                        host.setCursor(host.getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
                    return;
                }
                Rectangle area = host.getClientArea();
                int percent = (e.y - area.y) * 100 / Math.max(1, area.height - SASH);
                topPercent = Math.max(MIN_PERCENT, Math.min(100 - MIN_PERCENT, percent));
                log("host mouseMove at " + e.x + "," + e.y + " topPercent=" + topPercent);
                host.requestLayout();
            });
            host.addListener(SWT.MouseUp, e -> {
                log("host mouseUp at " + e.x + "," + e.y + " dragging=" + dragging
                        + " top=" + (top == null ? null : top.getBounds()));
                if (dragging) {
                    dragging = false;
                    host.setCapture(false);
                }
            });
            host.addListener(SWT.MouseExit, e -> host.setCursor(null));
        }

        Rectangle band() {
            if (top == null)
                return new Rectangle(0, 0, 0, 0);
            Rectangle t = top.getBounds();
            return new Rectangle(t.x, t.y + t.height, t.width, SASH);
        }

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            return new Point(600, 400);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            if (top == null || bottom == null)
                return;
            Rectangle area = composite.getClientArea();
            int topHeight = (area.height - SASH) * topPercent / 100;
            top.setBounds(area.x, area.y, area.width, topHeight);
            bottom.setBounds(area.x, area.y + topHeight + SASH, area.width, area.height - topHeight - SASH);
        }

        @Override
        public String toString() {
            return "org.eclipse.e4.ui.workbench.renderers.swt.SashLayout (stand-in)";
        }
    }
}
