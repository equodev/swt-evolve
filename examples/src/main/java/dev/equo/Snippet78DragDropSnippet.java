package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;

/**
 * Port of upstream Eclipse SWT Snippet78 (drag text between two labels), validating
 * TextTransfer drag/drop between two whole-widget Labels via the generic
 * ControlImpl.wrapDnd mechanism (see swt-evolve's issue #755 follow-up).
 */
public class Snippet78DragDropSnippet {

    public static void main(String[] args) {
        Config.useEquo(Label.class);

        Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("Snippet 78");
        shell.setLayout(new FillLayout());
        final Label label1 = new Label(shell, SWT.BORDER);
        label1.setText("TEXT");
        final Label label2 = new Label(shell, SWT.BORDER);
        setDragDrop(label1);
        setDragDrop(label2);
        shell.setSize(200, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    static void setDragDrop(final Label label) {
        DragSource source = new DragSource(label, DND.DROP_MOVE | DND.DROP_COPY);
        Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
        source.setTransfer(types);
        source.addDragListener(new org.eclipse.swt.dnd.DragSourceAdapter() {
            @Override
            public void dragSetData(org.eclipse.swt.dnd.DragSourceEvent event) {
                event.data = label.getText();
            }
        });

        DropTarget target = new DropTarget(label, DND.DROP_MOVE | DND.DROP_COPY);
        target.setTransfer(types);
        target.addDropListener(new org.eclipse.swt.dnd.DropTargetAdapter() {
            @Override
            public void drop(org.eclipse.swt.dnd.DropTargetEvent event) {
                label.setText((String) event.data);
            }
        });
    }
}
