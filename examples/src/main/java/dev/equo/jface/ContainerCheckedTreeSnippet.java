package dev.equo.jface;

import dev.equo.swt.Config;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A check tree whose parents follow their children: all checked = checkmark, some checked = partial
 * mark, none checked = empty box. Checking a parent checks its whole subtree.
 *
 * <p>The pane on the right prints the checked/grayed state the Java side holds for every item, so a
 * checkbox whose appearance lags behind its state is readable without a reference screenshot.
 *
 * <p>Run: {@code ./gradlew :examples:runDeskExample -PmainClass=dev.equo.jface.ContainerCheckedTreeSnippet}
 * <br>Swap Config.forceEquo() for Config.forceEclipse() (runEmbedExample) for the native baseline.
 */
public class ContainerCheckedTreeSnippet {

    private static final class Node {
        final String name;
        final Node parent;
        final List<Node> children = new ArrayList<>();

        Node(Node parent, String name) {
            this.parent = parent;
            this.name = name;
            if (parent != null) parent.children.add(this);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ContainerCheckedTreeSnippet — parents follow their children");
        shell.setLayout(new GridLayout(2, true));

        CheckboxTreeViewer viewer = new CheckboxTreeViewer(shell, SWT.BORDER);
        viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setContentProvider(new ITreeContentProvider() {
            @Override
            public Object[] getElements(Object input) {
                return getChildren(input);
            }

            @Override
            public Object[] getChildren(Object element) {
                return ((Node) element).children.toArray();
            }

            @Override
            public Object getParent(Object element) {
                return ((Node) element).parent;
            }

            @Override
            public boolean hasChildren(Object element) {
                return !((Node) element).children.isEmpty();
            }
        });
        viewer.setLabelProvider(new LabelProvider());
        viewer.setInput(buildModel());
        viewer.expandAll();

        Text state = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        state.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Runnable refreshState = () -> {
            StringBuilder sb = new StringBuilder("Java state (expected appearance)\n\n");
            for (TreeItem item : viewer.getTree().getItems()) describe(item, 0, sb);
            state.setText(sb.toString());
        };

        viewer.addCheckStateListener(event -> {
            TreeItem item = (TreeItem) viewer.testFindItem(event.getElement());
            if (item == null) return;
            item.setGrayed(false);
            setSubtree(item, event.getChecked());
            updateParents(item.getParentItem());
            refreshState.run();
        });

        Composite buttons = new Composite(shell, SWT.NONE);
        buttons.setLayout(new GridLayout(3, false));
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Button checkAll = new Button(buttons, SWT.PUSH);
        checkAll.setText("Check all");
        checkAll.addListener(SWT.Selection, e -> {
            for (TreeItem item : viewer.getTree().getItems()) {
                item.setGrayed(false);
                item.setChecked(true);
                setSubtree(item, true);
            }
            refreshState.run();
        });

        Button uncheckAll = new Button(buttons, SWT.PUSH);
        uncheckAll.setText("Uncheck all");
        uncheckAll.addListener(SWT.Selection, e -> {
            for (TreeItem item : viewer.getTree().getItems()) {
                item.setGrayed(false);
                item.setChecked(false);
                setSubtree(item, false);
            }
            refreshState.run();
        });

        // Flips one leaf from code, so the parents change without a click on the tree.
        Button toggleLeaf = new Button(buttons, SWT.PUSH);
        toggleLeaf.setText("Toggle \"Report A\" from code");
        toggleLeaf.addListener(SWT.Selection, e -> {
            TreeItem leaf = viewer.getTree().getItem(0).getItem(0).getItem(0);
            leaf.setChecked(!leaf.getChecked());
            updateParents(leaf.getParentItem());
            refreshState.run();
        });

        refreshState.run();
        shell.setSize(820, 520);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static Node buildModel() {
        Node root = new Node(null, "root");
        Node datasets = new Node(root, "Datasets");
        Node reports = new Node(datasets, "Reports");
        new Node(reports, "Report A");
        new Node(reports, "Report B");
        new Node(reports, "Report C");
        Node exports = new Node(datasets, "Exports");
        new Node(exports, "Export A");
        new Node(exports, "Export B");
        Node archives = new Node(root, "Archives");
        new Node(archives, "2023");
        new Node(archives, "2024");
        new Node(root, "Templates (leaf)");
        return root;
    }

    private static void setSubtree(TreeItem item, boolean checked) {
        for (TreeItem child : item.getItems()) {
            child.setChecked(checked);
            child.setGrayed(false);
            setSubtree(child, checked);
        }
    }

    /** A parent is checked when any child is, and grayed when its children disagree. */
    private static void updateParents(TreeItem item) {
        for (; item != null; item = item.getParentItem()) {
            boolean anyChecked = false;
            boolean anyUnchecked = false;
            for (TreeItem child : item.getItems()) {
                anyChecked |= child.getChecked();
                anyUnchecked |= !child.getChecked() || child.getGrayed();
            }
            item.setChecked(anyChecked);
            item.setGrayed(anyChecked && anyUnchecked);
        }
    }

    private static void describe(TreeItem item, int depth, StringBuilder sb) {
        String expected = !item.getChecked() ? "empty box" : item.getGrayed() ? "partial mark" : "checkmark";
        sb.append("  ".repeat(depth)).append(item.getText()).append(": ").append(expected).append('\n');
        for (TreeItem child : item.getItems()) describe(child, depth + 1, sb);
    }
}
