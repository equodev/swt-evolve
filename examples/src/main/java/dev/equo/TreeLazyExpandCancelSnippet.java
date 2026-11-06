package dev.equo;

import dev.equo.swt.Config;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A lazy tree whose loading is cancelled from the Expand listener, as a lazy viewer does when it
 * asks for a logon and the user cancels it: the placeholder child is removed while the item opens.
 *
 * <p>{@code -Dvariant=} picks what the listener does on "Project":
 * <ul>
 * <li>{@code dispose} (default): disposes the placeholder child;</li>
 * <li>{@code readd}: disposes it, then puts a new placeholder back later, so the item can be
 * expanded again;</li>
 * <li>{@code collapse}: keeps the placeholder and collapses the item later;</li>
 * <li>{@code disposeCollapse}: disposes the placeholder and collapses the item in the listener;</li>
 * <li>{@code dialog}: asks first in a message box, then disposes the placeholder on Cancel;</li>
 * <li>{@code jface}: a {@code TreeViewer} whose content provider loads no children;</li>
 * <li>{@code jfaceDeferred}: the same viewer showing a "Loading..." child it removes afterwards;</li>
 * <li>{@code logon}: a {@code TreeViewer} whose content provider opens a modal logon dialog from
 * {@code getChildren}; on Cancel it returns no children and later calls
 * {@code collapseToLevel(element, ALL_LEVELS)}, which collapses the item and puts the placeholder
 * back. {@code -DautoCancel=<ms>} presses Cancel by itself.</li>
 * </ul>
 * {@code -Dnative=true} runs it on the platform's SWT.
 */
public class TreeLazyExpandCancelSnippet {

    public static void main(String[] args) {
        if (Boolean.getBoolean("native")) Config.defaultToEclipse();
        else Config.forceEquo();
        String variant = System.getProperty("variant", "dispose");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TreeLazyExpandCancelSnippet (" + variant + ")");
        shell.setLayout(new GridLayout(1, false));

        Tree tree = new Tree(shell, SWT.BORDER | SWT.SINGLE);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        if (variant.equals("logon")) {
            buildLogonViewer(tree);
        } else if (variant.startsWith("jface")) {
            buildViewer(tree, variant);
        } else {
            TreeItem project = new TreeItem(tree, SWT.NONE);
            project.setText("Project");
            new TreeItem(project, SWT.NONE).setText("Loading...");
            TreeItem other = new TreeItem(tree, SWT.NONE);
            other.setText("Other project");
            new TreeItem(other, SWT.NONE).setText("child");
        }
        TreeItem project = tree.getItem(0);

        Label state = new Label(shell, SWT.NONE);
        state.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Runnable show = () -> {
            if (project.isDisposed()) return;
            String text = "Project: expanded=" + project.getExpanded() + " items=" + project.getItemCount();
            state.setText(text);
            System.out.println("STATE " + text);
        };

        tree.addListener(SWT.Expand, e -> {
            if (e.item != project) return;
            switch (variant) {
                case "collapse" -> display.asyncExec(() -> {
                    project.setExpanded(false);
                    show.run();
                });
                case "readd" -> {
                    for (TreeItem child : project.getItems()) child.dispose();
                    display.asyncExec(() -> {
                        new TreeItem(project, SWT.NONE).setText("Loading...");
                        show.run();
                    });
                }
                case "disposeCollapse" -> {
                    for (TreeItem child : project.getItems()) child.dispose();
                    project.setExpanded(false);
                }
                case "jface", "jfaceDeferred", "logon" -> {
                }
                case "dialog" -> {
                    MessageBox box = new MessageBox(shell, SWT.OK | SWT.CANCEL);
                    box.setMessage("Log on to Project?");
                    if (box.open() == SWT.CANCEL) {
                        for (TreeItem child : project.getItems()) child.dispose();
                    }
                }
                default -> {
                    for (TreeItem child : project.getItems()) child.dispose();
                }
            }
            display.asyncExec(show);
        });

        Button refresh = new Button(shell, SWT.PUSH);
        refresh.setText("Show state");
        refresh.addListener(SWT.Selection, e -> show.run());
        show.run();

        shell.setSize(320, 260);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /**
     * A viewer whose projects report children but load none, as when the logon is cancelled. With
     * {@code deferred} the load first shows a "Loading..." child and removes it afterwards.
     */
    private static void buildViewer(Tree tree, String variant) {
        boolean deferred = !variant.equals("jface");
        TreeViewer viewer = new TreeViewer(tree);
        viewer.setContentProvider(new ITreeContentProvider() {
            @Override
            public Object[] getElements(Object input) {
                return (Object[]) input;
            }

            @Override
            public Object[] getChildren(Object parent) {
                if (!deferred) return new Object[0];
                Object pending = "Loading...";
                tree.getDisplay().asyncExec(() -> {
                    if (!viewer.getTree().isDisposed()) viewer.remove(parent, new Object[] {pending});
                });
                return new Object[] {pending};
            }

            @Override
            public Object getParent(Object element) {
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                return !"Loading...".equals(element);
            }
        });
        viewer.setLabelProvider(new LabelProvider());
        viewer.setInput(new Object[] {"Project", "Other project"});
    }

    /**
     * A viewer that asks for a logon while loading a project's children. On Cancel it loads none
     * and collapses the project on the next event loop turn; until then it answers no children.
     */
    private static void buildLogonViewer(Tree tree) {
        TreeViewer viewer = new TreeViewer(tree);
        Set<Object> collapsing = new HashSet<>();
        viewer.setContentProvider(new ITreeContentProvider() {
            @Override
            public Object[] getElements(Object input) {
                return (Object[]) input;
            }

            @Override
            public Object[] getChildren(Object parent) {
                if (collapsing.contains(parent)) return new Object[0];
                if (logOn(tree.getShell())) return new Object[] {parent + " package"};
                collapsing.add(parent);
                tree.getDisplay().asyncExec(() -> {
                    if (tree.isDisposed()) return;
                    // Twice, as a lazy viewer may: the second call finds the placeholder the first put back.
                    viewer.collapseToLevel(parent, AbstractTreeViewer.ALL_LEVELS);
                    viewer.collapseToLevel(parent, AbstractTreeViewer.ALL_LEVELS);
                    collapsing.remove(parent);
                    TreeItem item = tree.getItem(0);
                    System.out.println("COLLAPSED " + parent + ": expanded=" + item.getExpanded()
                            + " items=" + item.getItemCount());
                });
                return new Object[0];
            }

            @Override
            public Object getParent(Object element) {
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                return !element.toString().endsWith(" package");
            }
        });
        viewer.setLabelProvider(new LabelProvider());
        viewer.setInput(new Object[] {"Project", "Other project"});
    }

    /** Opens a modal logon dialog and returns whether the user pressed OK. */
    private static boolean logOn(Shell parent) {
        Display display = parent.getDisplay();
        Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Logon");
        dialog.setLayout(new GridLayout(2, true));
        Label message = new Label(dialog, SWT.NONE);
        message.setText("Log on to the project's system?");
        message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        boolean[] ok = {false};
        Button okButton = new Button(dialog, SWT.PUSH);
        okButton.setText("OK");
        okButton.addListener(SWT.Selection, e -> {
            ok[0] = true;
            dialog.close();
        });
        Button cancel = new Button(dialog, SWT.PUSH);
        cancel.setText("Cancel");
        cancel.addListener(SWT.Selection, e -> dialog.close());
        dialog.setDefaultButton(cancel);
        dialog.pack();
        dialog.open();
        System.out.println("LOGON dialog open");
        int autoCancel = Integer.getInteger("autoCancel", -1);
        if (autoCancel >= 0) {
            display.timerExec(autoCancel, () -> {
                if (!dialog.isDisposed()) dialog.close();
            });
        }
        while (!dialog.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        return ok[0];
    }
}
