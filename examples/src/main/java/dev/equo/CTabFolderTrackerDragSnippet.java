package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tracker;

/**
 * Two view stacks as a workbench builds them: dragging a tab of the left one opens a Tracker on
 * SWT.DragDetect, the way the Eclipse workbench moves a view, over the right one, whose tab row
 * carries the view's tool bar as its topRight control.
 */
public class CTabFolderTrackerDragSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolderTrackerDragSnippet");
        shell.setLayout(new FillLayout(SWT.HORIZONTAL));

        CTabFolder source = createFolder(shell, "Navigator", "Outline");
        CTabFolder target = createFolder(shell, "Connections", "Projects");
        Composite topRight = new Composite(target, SWT.NONE);
        topRight.setLayout(new FillLayout());
        ToolBar toolBar = new ToolBar(topRight, SWT.FLAT | SWT.RIGHT);
        for (String label : new String[] {"Collapse", "Link", "Menu"}) {
            new ToolItem(toolBar, SWT.PUSH).setText(label);
        }
        target.setTopRight(topRight, SWT.RIGHT);

        source.addListener(SWT.DragDetect, event -> {
            CTabItem item = source.getItem(new Point(event.x, event.y));
            if (item == null) return;
            Tracker tracker = new Tracker(display, SWT.NONE);
            tracker.setStippled(true);
            Rectangle bounds = display.map(source, null, item.getBounds());
            tracker.setRectangles(new Rectangle[] {bounds});
            tracker.addListener(SWT.Move, move -> {
                Point cursor = display.getCursorLocation();
                tracker.setRectangles(new Rectangle[] {
                        new Rectangle(cursor.x, cursor.y, bounds.width, bounds.height)});
            });
            tracker.open();
            tracker.dispose();
        });

        shell.setSize(700, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static CTabFolder createFolder(Shell shell, String... labels) {
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        for (String label : labels) {
            CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(label);
            Label body = new Label(folder, SWT.NONE);
            body.setText("Content of " + label);
            item.setControl(body);
        }
        folder.setSelection(0);
        return folder;
    }
}
