package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Port of upstream Eclipse SWT Snippet284 (drag a URL between two labels), validating
 * URLTransfer (a built-in ByteArrayTransfer subclass, see DartURLTransfer's real
 * javaToNative/nativeToJava round trip added alongside issue #755's DND generalization)
 * and a non-DROP_COPY default operation (DND.DROP_LINK) resolved in
 * dragEnter/dragOperationChanged.
 */
public class Snippet284DragDropSnippet {

    public static void main(String[] args) {
        Config.useEquo(Label.class);

        Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("URLTransfer");
        shell.setLayout(new FillLayout());
        final Label label1 = new Label(shell, SWT.BORDER);
        label1.setText("http://www.eclipse.org");
        final Label label2 = new Label(shell, SWT.BORDER);
        setDragSource(label1);
        setDropTarget(label2);
        shell.setSize(600, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    public static void setDragSource(final Label label) {
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
        final DragSource source = new DragSource(label, operations);
        source.setTransfer(URLTransfer.getInstance());
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent e) {
                e.data = label.getText();
            }
        });
    }

    public static void setDropTarget(final Label label) {
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
        DropTarget target = new DropTarget(label, operations);
        target.setTransfer(URLTransfer.getInstance());
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent e) {
                if (e.detail == DND.DROP_NONE)
                    e.detail = DND.DROP_LINK;
            }

            @Override
            public void dragOperationChanged(DropTargetEvent e) {
                if (e.detail == DND.DROP_NONE)
                    e.detail = DND.DROP_LINK;
            }

            @Override
            public void drop(DropTargetEvent event) {
                if (event.data == null) {
                    event.detail = DND.DROP_NONE;
                    return;
                }
                label.setText((String) event.data);
            }
        });
    }
}
