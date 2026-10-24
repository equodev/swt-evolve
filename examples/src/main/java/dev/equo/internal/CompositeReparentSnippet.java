package dev.equo.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Moves nested content the way an RCP workbench does: a part's composite into a hidden shell and
 * back, tree branches collapsed and expanded, tabs switched, a part hidden and shown. Each step
 * first names itself in the shell title, then acts.
 */
public class CompositeReparentSnippet {

    static final int DEPTH = 4;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("step-0-load");
        shell.setLayout(new GridLayout(2, true));

        Composite host = new Composite(shell, SWT.BORDER);
        host.setLayout(new FillLayout());
        Composite part = nested(host, "part");
        Shell limbo = new Shell(shell, SWT.NONE);

        Tree tree = new Tree(shell, SWT.BORDER);
        TreeItem root = new TreeItem(tree, SWT.NONE);
        root.setText("root");
        for (int i = 0; i < 5; i++) {
            TreeItem a = new TreeItem(root, SWT.NONE);
            a.setText("a" + i);
            for (int j = 0; j < 5; j++) {
                TreeItem b = new TreeItem(a, SWT.NONE);
                b.setText("a" + i + "b" + j);
                for (int k = 0; k < 3; k++) new TreeItem(b, SWT.NONE).setText("a" + i + "b" + j + "c" + k);
                b.setExpanded(true);
            }
            a.setExpanded(true);
        }
        root.setExpanded(true);

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        for (int t = 0; t < 3; t++) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText("tab" + t);
            Composite body = new Composite(folder, SWT.NONE);
            body.setLayout(new FillLayout());
            nested(body, "tab" + t);
            item.setControl(body);
        }
        folder.setSelection(0);

        Composite toggled = new Composite(shell, SWT.BORDER);
        toggled.setLayout(new FillLayout());
        nested(toggled, "toggled");

        shell.setSize(1200, 800);
        shell.open();

        Runnable[] steps = {
            () -> { part.setParent(limbo); host.layout(true, true); },
            () -> { part.setParent(host); host.layout(true, true); },
            () -> { part.setParent(limbo); host.layout(true, true); },
            () -> { part.setParent(host); host.layout(true, true); },
            () -> root.setExpanded(false),
            () -> root.setExpanded(true),
            () -> root.setExpanded(false),
            () -> root.setExpanded(true),
            () -> folder.setSelection(1),
            () -> folder.setSelection(0),
            () -> folder.setSelection(2),
            () -> folder.setSelection(0),
            () -> toggled.setVisible(false),
            () -> toggled.setVisible(true),
        };
        String[] names = {"reparent-out", "reparent-in", "reparent-out", "reparent-in",
            "collapse", "expand", "collapse", "expand",
            "tab1", "tab0", "tab2", "tab0", "hide", "show"};
        int start = Integer.getInteger("delivery.startDelay", 20000);
        for (int s = 0; s < steps.length; s++) {
            int n = s;
            display.timerExec(start + n * 4000, () -> shell.setText("step-" + (n + 1) + "-" + names[n]));
            display.timerExec(start + n * 4000 + 500, () -> {
                System.out.println("step " + (n + 1) + " " + names[n]);
                steps[n].run();
            });
        }

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /** A chain of {@link #DEPTH} composites, each with a label and a toolbar, ending in a button. */
    static Composite nested(Composite parent, String name) {
        Composite top = null;
        Composite at = parent;
        for (int d = 0; d < DEPTH; d++) {
            Composite c = new Composite(at, SWT.NONE);
            c.setLayout(new GridLayout(1, false));
            new Label(c, SWT.NONE).setText(name + " level " + d);
            ToolBar bar = new ToolBar(c, SWT.FLAT);
            for (int i = 0; i < 3; i++) new ToolItem(bar, SWT.PUSH).setText(name + d + "." + i);
            if (top == null) top = c;
            at = c;
        }
        new Button(at, SWT.PUSH).setText(name + " leaf");
        return top;
    }
}
