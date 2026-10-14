package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Every checked/grayed combination of a check box, set by the application.
 *
 * <p>Natively, grayed only shows on a checked box: {@code checked=false, grayed=true} is an empty
 * box. {@code setChecked(false)} does not clear {@code grayed}, so an application that keeps a
 * parent's tri-state itself can leave a parent unchecked but still grayed, which must look empty.
 * Every row is labelled with what it should look like.
 *
 * <p>Run: {@code ./gradlew :examples:runDeskExample -PmainClass=dev.equo.TreeCheckGrayedStatesSnippet}
 */
public class TreeCheckGrayedStatesSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TreeCheckGrayedStatesSnippet");
        shell.setLayout(new GridLayout(1, false));

        buildTree(shell);
        buildTable(shell);
        buildButtons(shell);

        shell.setSize(480, 640);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void buildTree(Shell shell) {
        Group group = group(shell, "Tree (SWT.CHECK)", true);
        Tree tree = new Tree(group, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        item(tree, "checked=false grayed=false (empty box)", false, false);
        item(tree, "checked=true  grayed=false (checkmark)", true, false);
        item(tree, "checked=true  grayed=true (partial mark)", true, true);
        item(tree, "checked=false grayed=true (empty box)", false, true);

        TreeItem parent = item(tree, "Parent, children all unchecked (empty box)", false, false);
        for (String child : new String[] {"Child A", "Child B", "Child C"}) {
            item(parent, child + " (empty box)", false, false);
        }
        parent.setGrayed(true);
        parent.setExpanded(true);

        TreeItem parent2 = item(tree, "Parent, one child checked (partial mark)", true, true);
        item(parent2, "Child A (checkmark)", true, false);
        item(parent2, "Child B (empty box)", false, false);
        parent2.setExpanded(true);
    }

    private static void buildTable(Shell shell) {
        Group group = group(shell, "Table (SWT.CHECK)", false);
        Table table = new Table(group, SWT.CHECK | SWT.BORDER);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        row(table, "checked=false grayed=false (empty box)", false, false);
        row(table, "checked=true  grayed=false (checkmark)", true, false);
        row(table, "checked=true  grayed=true (partial mark)", true, true);
        row(table, "checked=false grayed=true (empty box)", false, true);
    }

    private static void buildButtons(Shell shell) {
        Group group = group(shell, "Button (SWT.CHECK)", false);
        button(group, "selection=false grayed=false (empty box)", false, false);
        button(group, "selection=true  grayed=false (checkmark)", true, false);
        button(group, "selection=true  grayed=true (partial mark)", true, true);
        button(group, "selection=false grayed=true (empty box)", false, true);
    }

    private static Group group(Shell shell, String text, boolean grab) {
        Group group = new Group(shell, SWT.NONE);
        group.setText(text);
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, grab));
        return group;
    }

    private static TreeItem item(Tree tree, String text, boolean checked, boolean grayed) {
        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(text);
        item.setChecked(checked);
        item.setGrayed(grayed);
        return item;
    }

    private static TreeItem item(TreeItem parent, String text, boolean checked, boolean grayed) {
        TreeItem item = new TreeItem(parent, SWT.NONE);
        item.setText(text);
        item.setChecked(checked);
        item.setGrayed(grayed);
        return item;
    }

    private static void row(Table table, String text, boolean checked, boolean grayed) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(text);
        item.setChecked(checked);
        item.setGrayed(grayed);
    }

    private static void button(Group group, String text, boolean selection, boolean grayed) {
        Button button = new Button(group, SWT.CHECK);
        button.setText(text);
        button.setSelection(selection);
        button.setGrayed(grayed);
    }
}
