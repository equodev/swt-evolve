package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * The grayed (partial-check) state, side by side with the fully-checked one.
 *
 * <p>A grayed item must be visually distinguishable from a fully-checked item — that difference is
 * how a check tree says "some but not all of my children are checked". Every row below is labelled
 * with what it should look like, so a wrong render is readable without a reference screenshot.
 *
 * <p>Run: {@code ./gradlew :examples:runDeskExample -PmainClass=dev.equo.TreeGrayedSnippet}
 */
public class TreeGrayedSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TreeGrayedSnippet — grayed vs fully checked");
        shell.setLayout(new GridLayout(1, false));

        buildTree(shell);
        buildButtons(shell);

        shell.setSize(560, 460);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /** Mirrors the reported shape: a parent grayed because only some of its children are checked. */
    private static void buildTree(Shell shell) {
        Group group = new Group(shell, SWT.NONE);
        group.setText("Tree (SWT.CHECK)");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Tree tree = new Tree(group, SWT.BORDER | SWT.CHECK);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TreeItem partial = new TreeItem(tree, SWT.NONE);
        partial.setText("Datasets — checked + grayed (expected: partial mark)");
        partial.setChecked(true);
        partial.setGrayed(true);
        partial.setExpanded(true);

        TreeItem checkedChild = new TreeItem(partial, SWT.NONE);
        checkedChild.setText("Report A — checked (this is why the parent is grayed)");
        checkedChild.setChecked(true);

        TreeItem uncheckedChild = new TreeItem(partial, SWT.NONE);
        uncheckedChild.setText("Report B — unchecked (and this is the other reason)");

        TreeItem full = new TreeItem(tree, SWT.NONE);
        full.setText("Archives — checked, not grayed (expected: checkmark)");
        full.setChecked(true);

        TreeItem empty = new TreeItem(tree, SWT.NONE);
        empty.setText("Templates — unchecked (expected: empty box)");
    }

    /** The same three states on plain check buttons: the defect is in the shared checkbox, not in Tree. */
    private static void buildButtons(Shell shell) {
        Group group = new Group(shell, SWT.NONE);
        group.setText("Button (SWT.CHECK) — same shared checkbox");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Button partial = new Button(group, SWT.CHECK);
        partial.setText("checked + grayed (expected: partial mark)");
        partial.setSelection(true);
        partial.setGrayed(true);

        Button full = new Button(group, SWT.CHECK);
        full.setText("checked, not grayed (expected: checkmark)");
        full.setSelection(true);

        Button empty = new Button(group, SWT.CHECK);
        empty.setText("unchecked (expected: empty box)");
    }
}
