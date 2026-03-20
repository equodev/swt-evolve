package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class FakeSashLayoutExample {

    static class FakeSashLayout extends Layout {

        int sashWidth = 6;
        int weight = 30; // percentage

        Rectangle sashRect;

        @Override
        protected void layout(Composite parent, boolean flushCache) {
            Control[] children = parent.getChildren();
            if (children.length < 2) return;

            Control left = children[0];
            Control right = children[1];

            Rectangle area = parent.getClientArea();

            int leftWidth = area.width * weight / 100;
            int rightWidth = area.width - leftWidth - sashWidth;

            left.setBounds(area.x, area.y, leftWidth, area.height);
            right.setBounds(area.x + leftWidth + sashWidth, area.y, rightWidth, area.height);

            sashRect = new Rectangle(area.x + leftWidth, area.y, sashWidth, area.height);
        }

        @Override
        protected org.eclipse.swt.graphics.Point computeSize(Composite parent, int wHint, int hHint, boolean flushCache) {
            return new org.eclipse.swt.graphics.Point(800, 600);
        }

        public boolean isOnSash(int x, int y) {
            return sashRect != null && sashRect.contains(x, y);
        }

        public void updateWeight(Composite parent, int mouseX) {
            Rectangle area = parent.getClientArea();
            int newWeight = (int) ((mouseX / (double) area.width) * 100);

            if (newWeight < 10) newWeight = 10;
            if (newWeight > 90) newWeight = 90;

            weight = newWeight;
        }

        public void paintSash(GC gc) {
            if (sashRect != null) {
                gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_GRAY));
                gc.fillRectangle(sashRect);
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("dev.equo.swt.debug", "true");
//        Config.forceEclipse();
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Fake SashLayout (Eclipse-like)");
        shell.setSize(900, 600);
        shell.setLayout(new FillLayout());

        Composite root = new Composite(shell, SWT.DOUBLE_BUFFERED);
        FakeSashLayout layout = new FakeSashLayout();
        root.setLayout(layout);

        Text left = new Text(root, SWT.MULTI | SWT.BORDER);
        left.setText("Left panel (Explorer)");

        Text right = new Text(root, SWT.MULTI | SWT.BORDER);
        right.setText("Right panel (Editor)");

        final boolean[] dragging = {false};

        // Mouse down
        root.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (layout.isOnSash(e.x, e.y)) {
                    dragging[0] = true;
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {
                dragging[0] = false;
            }
        });

        // Mouse move
        root.addMouseMoveListener((MouseMoveListener) e -> {
            if (layout.isOnSash(e.x, e.y)) {
                root.setCursor(display.getSystemCursor(SWT.CURSOR_SIZEWE));
            } else {
                root.setCursor(null);
            }

            if (dragging[0]) {
                layout.updateWeight(root, e.x);
                root.layout();
                root.redraw();
            }
        });

        // Paint sash
        root.addListener(SWT.Paint, event -> {
            layout.paintSash(event.gc);
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}