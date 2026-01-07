package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.List;

public class TreeWithJFaceTreeViewerExample extends ApplicationWindow {

    public TreeWithJFaceTreeViewerExample() {
        super(null);
    }

    @Override
    protected Control createContents(Composite parent) {
        TreeViewer treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer.setContentProvider(new ViewContentProvider());
        treeViewer.setLabelProvider(new ViewLabelProvider());
        treeViewer.setInput(createModel());

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object selectedNode = selection.getFirstElement();

                if (selectedNode instanceof TreeNode) {
                    TreeNode node = (TreeNode) selectedNode;
                    if (!node.hasChildren()) { // It's a leaf
                        System.out.println("Leaf node clicked: " + node.getName());
                    }
                }
            }
        });

        return treeViewer.getControl();
    }

    private List<TreeNode> createModel() {
        List<TreeNode> roots = new ArrayList<>();

        // Three-level tree
        TreeNode root1 = new TreeNode("Root 1 (3 Levels)");
        TreeNode child1_1 = new TreeNode("Child 1.1");
        TreeNode leaf1_1_1 = new TreeNode("Leaf 1.1.1");
        TreeNode leaf1_1_2 = new TreeNode("Leaf 1.1.2");
        TreeNode leaf1_1_3 = new TreeNode("Leaf 1.1.3");
        TreeNode leaf1_1_4 = new TreeNode("Leaf 1.1.4");
        child1_1.addChild(leaf1_1_1);
        child1_1.addChild(leaf1_1_2);
        child1_1.addChild(leaf1_1_3);
        child1_1.addChild(leaf1_1_4);
        root1.addChild(child1_1);
        roots.add(root1);

        // Four-level tree
        TreeNode root2 = new TreeNode("Root 2 (4 Levels)");
        TreeNode child2_1 = new TreeNode("Child 2.1");
        TreeNode child2_1_1 = new TreeNode("Child 2.1.1");
        TreeNode leaf2_1_1_1 = new TreeNode("Leaf 2.1.1.1");
        child2_1_1.addChild(leaf2_1_1_1);
        child2_1.addChild(child2_1_1);
        root2.addChild(child2_1);
        roots.add(root2);
        
        // Another three-level tree for variety
        TreeNode root3 = new TreeNode("Root 3 (3 Levels)");
        TreeNode child3_1 = new TreeNode("Child 3.1");
        TreeNode leaf3_1_1 = new TreeNode("Leaf 3.1.1");
        child3_1.addChild(leaf3_1_1);
        root3.addChild(child3_1);
        roots.add(root3);


        return roots;
    }

    public static void main(String[] args) {
        Config.useEquo(Tree.class);
        Config.useEquo(TreeItem.class);
        Config.useEquo(TreeColumn.class);
        TreeWithJFaceTreeViewerExample window = new TreeWithJFaceTreeViewerExample();
        window.setBlockOnOpen(true);
        window.open();
        Display.getCurrent().dispose();
    }
    
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Tree with JFace TreeViewer Example");
        shell.setSize(400, 300);
    }

    public static class ViewLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof TreeNode) {
                return ((TreeNode) element).getName();
            }
            return super.getText(element);
        }
    }

    public static class ViewContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List<?>) inputElement).toArray();
            }
            return new Object[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TreeNode) {
                return ((TreeNode) parentElement).getChildren().toArray();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof TreeNode) {
                return ((TreeNode) element).getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof TreeNode) {
                return ((TreeNode) element).hasChildren();
            }
            return false;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Not used in this example
        }

        @Override
        public void dispose() {
            // Not used in this example
        }
    }

    public static class TreeNode {
        private String name;
        private List<TreeNode> children = new ArrayList<>();
        private TreeNode parent;

        public TreeNode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void addChild(TreeNode child) {
            children.add(child);
            child.setParent(this);
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public TreeNode getParent() {
            return parent;
        }

        public void setParent(TreeNode parent) {
            this.parent = parent;
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
