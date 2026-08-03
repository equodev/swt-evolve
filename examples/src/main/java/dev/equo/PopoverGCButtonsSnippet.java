package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Minimal reproduction: a toolbar icon opens a borderless popup Shell on hover,
 * fading its alpha in over ~500ms and continuously repositioning itself on every Paint event of
 * its parent toolbar. Inside, three Canvas "buttons" are drawn entirely with GC PaintListeners
 * (hand-drawn menu items), the part that intermittently fails to render (or flashes and
 * disappears) under the Dart/web backend.
 */
public class PopoverGCButtonsSnippet {

    private static final String[] LABELS = {"Support Portal", "Forum", "Onboarding Hub"};

    private static Shell popup;
    private static Listener repositionListener;
    private static ToolItem target;

    public static void main(String[] args) {
        Config.useEquo(ToolBar.class);
        Config.useEquo(ToolItem.class);
        Config.useEquo(Canvas.class);
        Config.useEquo(Composite.class);
        Config.useEquo(Shell.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("Popover GC Buttons Snippet");
        shell.setSize(500, 300);

        ToolBar toolBar = new ToolBar(shell, SWT.FLAT);
        target = new ToolItem(toolBar, SWT.PUSH);
        target.setText("?");

        toolBar.addListener(SWT.MouseEnter, e -> onEnter());
        toolBar.addListener(SWT.MouseExit, e -> onExit());

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void onEnter() {
        if (popup == null || popup.isDisposed()) {
            openPopup();
        }
    }

    private static void onExit() {
        scheduleMaybeClose();
    }

    // Deciding whether to close by replaying MouseEnter/MouseExit into boolean flags races
    // the popup Shell appearing right under the cursor, so query the real cursor position
    // against both controls' bounds instead of depending on event delivery order.
    private static boolean isPointerOverTargetOrPopup() {
        Display display = target.getDisplay();
        Point cursor = display.getCursorLocation();
        Rectangle targetBounds = target.getBounds();
        if (!targetBounds.isEmpty()) {
            Point targetScreenPos = target.getParent().toDisplay(targetBounds.x, targetBounds.y);
            Rectangle targetScreen = new Rectangle(
                    targetScreenPos.x, targetScreenPos.y, targetBounds.width, targetBounds.height);
            if (targetScreen.contains(cursor)) return true;
        }
        return popup != null && !popup.isDisposed() && popup.getBounds().contains(cursor);
    }

    private static void scheduleMaybeClose() {
        Display display = target.getDisplay();
        display.timerExec(300, () -> {
            if (popup != null && !popup.isDisposed() && !isPointerOverTargetOrPopup()) {
                closePopup();
            }
        });
    }

    private static void openPopup() {
        Shell parent = target.getParent().getShell();
        popup = new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP);
        popup.setAlpha(0);
        popup.setLayout(new FillLayout());

        Composite menu = new Composite(popup, SWT.NONE);
        menu.setLayout(new RowLayout(SWT.VERTICAL));

        for (String label : LABELS) {
            addMenuItemButton(menu, label);
        }

        popup.addListener(SWT.MouseExit, e -> scheduleMaybeClose());

        repositionListener = e -> reposition();
        target.getParent().addListener(SWT.Paint, repositionListener);

        reposition();
        popup.setVisible(true);
        fadeIn(popup);
    }

    private static void addMenuItemButton(Composite parent, String label) {
        Canvas button = new Canvas(parent, SWT.NONE);
        button.setLayoutData(new RowData(180, 32));

        final boolean[] hovered = {false};
        button.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                Rectangle bounds = button.getBounds();
                Color bg = hovered[0]
                        ? e.display.getSystemColor(SWT.COLOR_LIST_SELECTION)
                        : e.display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
                e.gc.setBackground(bg);
                e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
                e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
                e.gc.drawText(label, 10, 8, true);
            }
        });
        button.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(org.eclipse.swt.events.MouseEvent e) {
                hovered[0] = true;
                button.redraw();
            }

            @Override
            public void mouseExit(org.eclipse.swt.events.MouseEvent e) {
                hovered[0] = false;
                button.redraw();
            }
        });
    }

    private static void reposition() {
        if (popup == null || popup.isDisposed()) return;
        Rectangle targetBounds = target.getBounds();
        Point screenPos = target.getParent().toDisplay(targetBounds.x, targetBounds.y + targetBounds.height);
        Point size = popup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        popup.setBounds(screenPos.x, screenPos.y, size.x, size.y);
    }

    private static void closePopup() {
        if (repositionListener != null && !target.getParent().isDisposed()) {
            target.getParent().removeListener(SWT.Paint, repositionListener);
        }
        fadeOutThenDispose(popup);
    }

    /** Mirrors AnimationUtil.fade(): a 15ms-tick alpha ramp over ~500ms via Display.timerExec. */
    private static void fadeIn(Shell shell) {
        fade(shell, 0, 255);
    }

    private static void fadeOutThenDispose(Shell shell) {
        fade(shell, 255, 0);
        Display display = shell.getDisplay();
        display.timerExec(260, () -> {
            if (!shell.isDisposed()) shell.dispose();
        });
    }

    private static void fade(Shell shell, int from, int to) {
        long duration = 250;
        long start = System.currentTimeMillis();
        Runnable[] tick = new Runnable[1];
        tick[0] = () -> {
            if (shell.isDisposed()) return;
            double pct = Math.min(1.0, (System.currentTimeMillis() - start) / (double) duration);
            int alpha = (int) Math.round(from + (to - from) * pct);
            shell.setAlpha(alpha);
            if (pct < 1.0) {
                shell.getDisplay().timerExec(15, tick[0]);
            }
        };
        tick[0].run();
    }
}
