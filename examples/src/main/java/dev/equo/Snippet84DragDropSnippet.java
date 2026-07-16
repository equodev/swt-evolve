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
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Port of upstream Eclipse SWT Snippet84 (define a default operation), validating Label ->
 * Text drag/drop with DND.DROP_COPY|MOVE|LINK, DND.DROP_DEFAULT resolution in
 * dragEnter/dragOperationChanged, and event.detail correctly reported back to
 * dragFinished (see DartDragSource#fireDragEnd, swt-evolve's DND generalization).
 */
public class Snippet84DragDropSnippet {

    public static void main(String[] args) {
        Config.useEquo(Label.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Snippet 84");
        shell.setLayout(new FillLayout());

        final Label label = new Label(shell, SWT.BORDER);
        label.setText("Drag Source");
        DragSource source = new DragSource(label, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
        source.setTransfer(TextTransfer.getInstance());
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = "Text Transferred";
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                if (event.doit) {
                    String operation;
                    switch (event.detail) {
                    case DND.DROP_MOVE:
                        operation = "moved";
                        break;
                    case DND.DROP_COPY:
                        operation = "copied";
                        break;
                    case DND.DROP_LINK:
                        operation = "linked";
                        break;
                    case DND.DROP_NONE:
                        operation = "disallowed";
                        break;
                    default:
                        operation = "unknown";
                        break;
                    }
                    label.setText("Drag Source (data " + operation + ")");
                } else {
                    label.setText("Drag Source (drag cancelled)");
                }
            }
        });

        final Text text = new Text(shell, SWT.BORDER | SWT.MULTI);
        text.setText("Drop Target");
        DropTarget target = new DropTarget(text, DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
        target.setTransfer(TextTransfer.getInstance());
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT)
                    event.detail = DND.DROP_COPY;
            }

            @Override
            public void dragOperationChanged(DropTargetEvent event) {
                if (event.detail == DND.DROP_DEFAULT)
                    event.detail = DND.DROP_COPY;
            }

            @Override
            public void drop(DropTargetEvent event) {
                String operation;
                switch (event.detail) {
                case DND.DROP_MOVE:
                    operation = "moved";
                    break;
                case DND.DROP_COPY:
                    operation = "copied";
                    break;
                case DND.DROP_LINK:
                    operation = "linked";
                    break;
                case DND.DROP_NONE:
                    operation = "disallowed";
                    break;
                default:
                    operation = "unknown";
                    break;
                }
                text.append("\n" + operation + (String) event.data);
            }
        });

        shell.setSize(400, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
