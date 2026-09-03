package dev.equo;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.IToolTipProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Hover a tree item and expect a tooltip, the way a Project Explorer shows one.
 *
 * The three ways an SWT application puts a tooltip on a tree, side by side:
 * the control-level {@code Tree.setToolTipText}; the per-item tooltip a JFace viewer opens as
 * a popup Shell through {@code ColumnViewerToolTipSupport}; and the per-item tooltip a mouse
 * listener produces by rewriting the control-level one, which is the only per-item tooltip a
 * native GTK Tree has and so the one an Eclipse-based product's Project Explorer relies on.
 */
public class TreeItemToolTipSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Tree item tooltips");
        shell.setLayout(new GridLayout(4, true));
        shell.setSize(1240, 500);

        createPlainTree(shell);
        createViewerTree(shell);
        createRichViewerTree(shell);
        createDynamicTree(shell);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void createPlainTree(Shell shell) {
        Group group = new Group(shell, SWT.NONE);
        group.setText("Tree.setToolTipText (control level)");
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        group.setLayout(new FillLayout());

        Tree tree = new Tree(group, SWT.BORDER);
        tree.setToolTipText("Control-level tooltip on the Tree");
        for (int i = 0; i < 3; i++) {
            TreeItem root = new TreeItem(tree, SWT.NONE);
            root.setText("Plain root " + i);
            for (int j = 0; j < 3; j++) {
                TreeItem child = new TreeItem(root, SWT.NONE);
                child.setText("Plain child " + i + "." + j);
            }
            root.setExpanded(true);
        }
    }

    /**
     * The only way a native GTK Tree shows a per-item tooltip: a mouse listener rewrites the
     * control-level tooltip as the pointer crosses items. A product's Project Explorer showing
     * "the full resource path" is doing this, or the JFace variant.
     */
    private static void createDynamicTree(Shell shell) {
        Group group = new Group(shell, SWT.NONE);
        group.setText("dynamic setToolTipText (per item)");
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        group.setLayout(new FillLayout());

        Tree tree = new Tree(group, SWT.BORDER);
        for (int i = 0; i < 12; i++) {
            TreeItem root = new TreeItem(tree, SWT.NONE);
            root.setText("Dyn root " + i);
            for (int j = 0; j < 2; j++) {
                TreeItem child = new TreeItem(root, SWT.NONE);
                child.setText("Dyn child " + i + "." + j);
            }
            root.setExpanded(true);
        }

        Listener follow = event -> {
            TreeItem item = tree.getItem(new Point(event.x, event.y));
            String tip = item == null ? null : "/workspace/" + item.getText().replace(' ', '-');
            if (!java.util.Objects.equals(tip, tree.getToolTipText())) {
                tree.setToolTipText(tip);
            }
        };
        tree.addListener(SWT.MouseMove, follow);
        tree.addListener(SWT.MouseHover, follow);
    }

    /**
     * The Project Explorer's own stack: a column-less TreeViewer whose label provider is an
     * owner-drawn DelegatingStyledCellLabelProvider that also supplies the tooltip text, with
     * ColumnViewerToolTipSupport over it. Eclipse wires all of that up and then leaves it switched
     * off -- CommonNavigator calls enableFor only when the viewer declares
     * org.eclipse.ui.navigator.enableTooltipSupport, which no stock plugin does. A product that
     * declares it gets exactly what this pane shows.
     */
    private static void createViewerTree(Shell shell) {
        Group group = new Group(shell, SWT.NONE);
        group.setText("Project Explorer stack (styled + IToolTipProvider)");
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        group.setLayout(new FillLayout());

        TreeViewer viewer = new TreeViewer(group, SWT.BORDER | SWT.V_SCROLL);
        viewer.setContentProvider(new NodeContentProvider());
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new PathStyledLabelProvider()));
        ColumnViewerToolTipSupport.enableFor(viewer);

        Node input = new Node(null, "invisible-root");
        for (int i = 0; i < 12; i++) {
            Node root = new Node(input, "Viewer root " + i);
            for (int j = 0; j < 3; j++) {
                new Node(root, "Viewer child " + i + "." + j);
            }
        }
        viewer.setInput(input);
        viewer.expandAll();
    }

    /**
     * What a product actually ships: a ColumnViewerToolTipSupport subclass that builds its own
     * content area out of real controls, so the tooltip Shell holds a Composite of Labels rather
     * than a line of text. Shipping IDEs write their tree tooltips this way.
     */
    private static void createRichViewerTree(Shell shell) {
        Group group = new Group(shell, SWT.NONE);
        group.setText("custom content area (Composite of controls)");
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        group.setLayout(new FillLayout());

        TreeViewer viewer = new TreeViewer(group, SWT.BORDER | SWT.V_SCROLL);
        viewer.setContentProvider(new NodeContentProvider());
        viewer.setLabelProvider(new PathStyledLabelProvider());
        RichTooltip.install(viewer);

        Node input = new Node(null, "invisible-root");
        for (int i = 0; i < 12; i++) {
            Node root = new Node(input, "Rich root " + i);
            for (int j = 0; j < 3; j++) {
                new Node(root, "Rich child " + i + "." + j);
            }
        }
        viewer.setInput(input);
        viewer.expandAll();
    }

    private static final class RichTooltip extends ColumnViewerToolTipSupport {
        private final ColumnViewer viewer;

        private RichTooltip(ColumnViewer viewer) {
            super(viewer, ToolTip.NO_RECREATE, false);
            this.viewer = viewer;
        }

        static void install(ColumnViewer viewer) {
            new RichTooltip(viewer);
        }

        @Override
        protected boolean shouldCreateToolTip(Event event) {
            return cellAt(event) != null;
        }

        @Override
        protected Composite createToolTipContentArea(Event event, Composite parent) {
            Composite area = new Composite(parent, SWT.NONE);
            area.setLayout(new GridLayout(2, false));
            ViewerCell cell = cellAt(event);
            Node node = cell == null ? null : (Node) cell.getElement();
            row(area, "Name", node == null ? "-" : node.name);
            row(area, "Path", node == null ? "-" : node.path());
            row(area, "Depth", node == null ? "-" : String.valueOf(node.path().split("/").length - 1));
            return area;
        }

        private ViewerCell cellAt(Event event) {
            return viewer.getCell(new Point(event.x, event.y));
        }

        private static void row(Composite parent, String key, String value) {
            Label k = new Label(parent, SWT.NONE);
            k.setText(key + ":");
            new Label(parent, SWT.NONE).setText(value);
        }
    }

    /** Styled label plus tooltip from one provider, as NavigatorContentServiceLabelProvider does. */
    private static final class PathStyledLabelProvider extends LabelProvider
            implements DelegatingStyledCellLabelProvider.IStyledLabelProvider, IToolTipProvider {

        @Override
        public String getText(Object element) {
            return ((Node) element).name;
        }

        @Override
        public StyledString getStyledText(Object element) {
            Node node = (Node) element;
            StyledString styled = new StyledString(node.name);
            styled.append("  " + node.path(), StyledString.QUALIFIER_STYLER);
            return styled;
        }

        @Override
        public String getToolTipText(Object element) {
            return "Full path: " + ((Node) element).path();
        }
    }

    private static final class Node {
        final Node parent;
        final String name;
        final java.util.List<Node> children = new java.util.ArrayList<>();

        Node(Node parent, String name) {
            this.parent = parent;
            this.name = name;
            if (parent != null) {
                parent.children.add(this);
            }
        }

        String path() {
            return parent == null || parent.parent == null ? "/" + name : parent.path() + "/" + name;
        }
    }

    private static final class NodeContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getElements(Object inputElement) {
            return ((Node) inputElement).children.toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            return ((Node) parentElement).children.toArray();
        }

        @Override
        public Object getParent(Object element) {
            return ((Node) element).parent;
        }

        @Override
        public boolean hasChildren(Object element) {
            return !((Node) element).children.isEmpty();
        }
    }
}
