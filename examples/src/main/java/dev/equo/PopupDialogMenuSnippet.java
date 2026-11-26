package dev.equo;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A JFace PopupDialog with its dialog menu on, built the way Eclipse's Quick Access builds its
 * dialog: resizable, takes focus, a filter field. The menu button at the top right must show the
 * menu (Move, Resize, Remember Size, ...), and the field must take typing afterwards. Menu
 * Show/Hide and every edit of the field are printed, so a run shows both.
 */
public class PopupDialogMenuSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("PopupDialog menu");
        shell.setLayout(new GridLayout());
        Button open = new Button(shell, SWT.PUSH);
        open.setText("Open popup");
        open.addListener(SWT.Selection, e -> new FilterPopup(shell).open());
        display.addFilter(SWT.Show, e -> {
            if (e.widget instanceof Menu) System.out.println("menu=show");
        });
        display.addFilter(SWT.Hide, e -> {
            if (e.widget instanceof Menu) System.out.println("menu=hide");
        });
        shell.setSize(700, 500);
        shell.open();
        display.timerExec(2000, () -> new FilterPopup(shell).open());
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static final class FilterPopup extends PopupDialog {
        private Text filter;

        FilterPopup(Shell parent) {
            super(parent, SWT.RESIZE, true, true, false, true, true, "", null);
        }

        @Override
        protected Control createTitleControl(Composite parent) {
            filter = new Text(parent, SWT.SINGLE | SWT.BORDER);
            filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            filter.addListener(SWT.Modify, e -> System.out.println("filter=" + filter.getText()));
            return filter;
        }

        @Override
        protected Control getFocusControl() {
            return filter;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite area = (Composite) super.createDialogArea(parent);
            new Text(area, SWT.MULTI | SWT.READ_ONLY).setText("results");
            return area;
        }

        @Override
        protected org.eclipse.swt.graphics.Point getDefaultSize() {
            return new org.eclipse.swt.graphics.Point(420, 240);
        }
    }
}
