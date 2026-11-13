package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A ViewForm laid out like an Eclipse Compare pane (BORDER | FLAT, a CLabel and a ToolBar on top,
 * the separator under them) and a plain one, above a CBanner with a tool bar on each side of its curve.
 */
public class ViewFormCBannerSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ViewForm / CBanner");
        shell.setLayout(new GridLayout(2, true));
        shell.setSize(560, 300);

        viewForm(shell, SWT.BORDER | SWT.FLAT, "Compare pane (BORDER | FLAT)");
        viewForm(shell, SWT.NONE, "Plain (NONE)");

        CBanner banner = new CBanner(shell, SWT.NONE);
        banner.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
        banner.setLeft(toolBar(banner, "Left", "Tool"));
        banner.setRight(toolBar(banner, "Right"));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void viewForm(Shell shell, int style, String title) {
        ViewForm form = new ViewForm(shell, style);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        CLabel label = new CLabel(form, SWT.NONE);
        label.setText(title);
        form.setTopLeft(label);
        form.setTopRight(toolBar(form, "Go"));
        Text content = new Text(form, SWT.MULTI);
        content.setText("content");
        form.setContent(content);
    }

    private static ToolBar toolBar(Composite parent, String... items) {
        ToolBar bar = new ToolBar(parent, SWT.FLAT);
        for (String text : items) {
            new ToolItem(bar, SWT.PUSH).setText(text);
        }
        return bar;
    }
}
