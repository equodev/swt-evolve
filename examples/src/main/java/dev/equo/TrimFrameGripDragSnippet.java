package dev.equo;

import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;

/**
 * Two tool bar contributions in the main trim, each in a frame Canvas that draws a grip to the
 * left of its ToolBar, as the Eclipse workbench builds them. A drag on a grip opens a Tracker on
 * SWT.DragDetect and drops the contribution where the Tracker was released, ahead of every frame
 * whose centre lies to the right of that point. The text in the window body keeps the keyboard
 * focus throughout: the status line at the bottom shows the drops, the display's focus control and
 * the text's FocusOut events since the last press on a frame.
 */
public class TrimFrameGripDragSnippet {

    private static final int GRIP = 12;

    private static Text text;
    private static Label status;
    private static int drops;
    private static int focusOutsSincePress;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TrimFrameGripDragSnippet");
        shell.setSize(700, 300);

        TrimmedPartLayout trim = new TrimmedPartLayout();
        shell.setLayout(trim);
        Composite top = trim.getTrimComposite(shell, SWT.TOP);
        top.setLayout(new RowLayout(SWT.HORIZONTAL));
        createFrame(top, "Edit", "Cut", "Copy", "Paste");
        createFrame(top, "Run", "Debug", "Profile");

        Composite body = trim.getClientArea(shell);
        body.setLayout(new FillLayout());
        text = new Text(body, SWT.MULTI | SWT.BORDER);
        text.setText("The keyboard focus stays here while a contribution is dragged.");
        text.addListener(SWT.FocusIn, e -> showStatus());
        text.addListener(SWT.FocusOut, e -> {
            focusOutsSincePress++;
            showStatus();
        });

        Composite bottom = trim.getTrimComposite(shell, SWT.BOTTOM);
        bottom.setLayout(new FillLayout());
        status = new Label(bottom, SWT.NONE);
        showStatus();

        shell.open();
        text.setFocus();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void createFrame(Composite top, String... labels) {
        Canvas frame = new Canvas(top, SWT.NONE);
        ToolBar toolBar = new ToolBar(frame, SWT.FLAT | SWT.RIGHT);
        for (String label : labels) {
            new ToolItem(toolBar, SWT.PUSH).setText(label);
        }
        toolBar.pack();
        Rectangle bar = toolBar.getBounds();
        toolBar.setLocation(GRIP, 0);
        frame.setLayoutData(new RowData(GRIP + bar.width, bar.height));
        frame.addListener(SWT.Paint, e -> {
            e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
            for (int y = 3; y < bar.height - 3; y += 4) {
                e.gc.drawLine(4, y, GRIP - 4, y);
            }
        });
        frame.addListener(SWT.MouseDown, e -> focusOutsSincePress = 0);
        frame.addListener(SWT.DragDetect, e -> {
            if (e.x >= GRIP) return;
            Display display = frame.getDisplay();
            Tracker tracker = new Tracker(display, SWT.NONE);
            tracker.setStippled(true);
            tracker.setRectangles(new Rectangle[] {display.map(top, null, frame.getBounds())});
            if (tracker.open()) {
                Rectangle dropped = display.map(null, top, tracker.getRectangles()[0]);
                dropAt(frame, dropped.x);
                drops++;
                showStatus();
            }
            tracker.dispose();
        });
    }

    private static void showStatus() {
        Control focus = status.getDisplay().getFocusControl();
        status.setText("drops " + drops + ", focus on " + (focus == text ? "text" : String.valueOf(focus))
                + ", text focus-outs since the press " + focusOutsSincePress);
    }

    /** Moves {@code frame} ahead of the first other frame whose centre is right of {@code x}, else last. */
    private static void dropAt(Canvas frame, int x) {
        Composite top = frame.getParent();
        Control after = null;
        for (Control other : top.getChildren()) {
            if (other == frame) continue;
            Rectangle b = other.getBounds();
            if (b.x + b.width / 2 > x) {
                after = other;
                break;
            }
        }
        if (after != null) {
            frame.moveAbove(after);
        } else {
            Control[] children = top.getChildren();
            frame.moveBelow(children[children.length - 1]);
        }
        top.layout(true);
    }
}
