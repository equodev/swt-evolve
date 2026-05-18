package dev.equo;

import java.util.ArrayList;
import java.util.List;

import dev.equo.swt.Config;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TreeOperationsSnippet {

    public TreeOperationsSnippet(Shell shell) {
        Config.forceEquo();
        shell.setLayout(new GridLayout(1, false));

        Composite buttons = new Composite(shell, SWT.NONE);
        buttons.setLayout(new RowLayout());
        Button deleteBtn = new Button(buttons, SWT.PUSH);
        deleteBtn.setText("Delete");
        Button moveUpBtn = new Button(buttons, SWT.PUSH);
        moveUpBtn.setText("Move Up");
        Button moveDownBtn = new Button(buttons, SWT.PUSH);
        moveDownBtn.setText("Move Down");

        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabItem tab = new CTabItem(tabFolder, SWT.NONE);
        tab.setText("Items");

        Composite treeComposite = new Composite(tabFolder, SWT.NONE);
        TreeColumnLayout treeLayout = new TreeColumnLayout();
        treeComposite.setLayout(treeLayout);
        tab.setControl(treeComposite);

        TreeViewer viewer = new TreeViewer(treeComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        viewer.getTree().setHeaderVisible(true);
        viewer.getTree().setLinesVisible(true);

        TreeViewerColumn nameCol = new TreeViewerColumn(viewer, SWT.NONE);
        nameCol.getColumn().setText("Name");
        treeLayout.setColumnData(nameCol.getColumn(), new ColumnWeightData(70));
        nameCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });

        TreeViewerColumn valueCol = new TreeViewerColumn(viewer, SWT.NONE);
        valueCol.getColumn().setText("Value");
        treeLayout.setColumnData(valueCol.getColumn(), new ColumnWeightData(30));
        valueCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return "#" + ((MyModel) element).counter;
            }
        });

        viewer.setContentProvider(new MyContentProvider());
        MyModel root = createModel();
        viewer.setInput(root);
        tabFolder.setSelection(0);

        viewer.getTree().addListener(SWT.Expand, e -> System.out.println("[snippet] SWT.Expand: " + e.item));
        viewer.getTree().addListener(SWT.Collapse, e -> System.out.println("[snippet] SWT.Collapse: " + e.item));
        viewer.getTree().addListener(SWT.MouseMove, e -> {
            int count = viewer.getTree().getItemCount();
            System.out.println("[snippet] MouseMove: " + count);
        });

        deleteBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            MyModel item = (MyModel) sel.getFirstElement();
            if (item.parent != null) {
                item.parent.child.remove(item);
                viewer.refresh();
            }
        });

        moveUpBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            MyModel item = (MyModel) sel.getFirstElement();
            if (item.parent == null) return;
            List<MyModel> siblings = item.parent.child;
            int idx = siblings.indexOf(item);
            if (idx > 0) {
                siblings.remove(idx);
                siblings.add(idx - 1, item);
                viewer.refresh();
            }
        });

        moveDownBtn.addListener(SWT.Selection, e -> {
            IStructuredSelection sel = viewer.getStructuredSelection();
            if (sel.isEmpty()) return;
            MyModel item = (MyModel) sel.getFirstElement();
            if (item.parent == null) return;
            List<MyModel> siblings = item.parent.child;
            int idx = siblings.indexOf(item);
            if (idx < siblings.size() - 1) {
                siblings.remove(idx);
                siblings.add(idx + 1, item);
                viewer.refresh();
            }
        });
    }

    private MyModel createModel() {
        MyModel root = new MyModel(0, null);
        for (int i = 1; i <= 5; i++) {
            MyModel parent = new MyModel(i, root);
            root.child.add(parent);
            for (int j = 1; j <= 3; j++) {
                parent.child.add(new MyModel(j, parent));
            }
        }
        return root;
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Tree in CTabFolder");
        shell.setSize(500, 400);
        new TreeOperationsSnippet(shell);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    static class MyContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object input) {
            return ((MyModel) input).child.toArray();
        }
        @Override
        public Object[] getChildren(Object element) {
            return ((MyModel) element).child.toArray();
        }
        @Override
        public Object getParent(Object element) {
            return ((MyModel) element).parent;
        }
        @Override
        public boolean hasChildren(Object element) {
            return !((MyModel) element).child.isEmpty();
        }
    }

    static class MyModel {
        MyModel parent;
        List<MyModel> child = new ArrayList<>();
        int counter;

        MyModel(int counter, MyModel parent) {
            this.counter = counter;
            this.parent = parent;
        }

        @Override
        public String toString() {
            if (parent != null && parent.parent != null) {
                return parent + "." + counter;
            }
            return "Item " + counter;
        }
    }
}