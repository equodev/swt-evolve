package dev.equo.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A GEF viewer whose nodes are joined by connections on the root part's connection layer.
 * <p>
 * Where {@link GefBasicSnippet} only paints node figures on the primary layer, this exercises the
 * layered side of a GEF diagram: connections live on {@link LayerConstants#CONNECTION_LAYER}, and
 * each one re-routes itself off its endpoints' {@link ChopboxAnchor}s rather than being positioned
 * by the layout.
 */
public class GefConnectionsSnippet {

    private record Node(String name, Rectangle bounds) {
    }

    private static class NodeEditPart extends AbstractGraphicalEditPart {
        @Override
        protected IFigure createFigure() {
            Label label = new Label(((Node) getModel()).name());
            label.setOpaque(true);
            label.setBackgroundColor(ColorConstants.lightGray);
            label.setBorder(new LineBorder(ColorConstants.darkBlue, 2));
            return label;
        }

        @Override
        protected void createEditPolicies() {
        }

        @Override
        protected void refreshVisuals() {
            ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(),
                    ((Node) getModel()).bounds());
        }
    }

    private static class DiagramEditPart extends AbstractGraphicalEditPart {
        @Override
        protected IFigure createFigure() {
            Figure figure = new Figure();
            figure.setLayoutManager(new XYLayout());
            return figure;
        }

        @Override
        protected void createEditPolicies() {
        }

        @Override
        @SuppressWarnings("unchecked")
        protected List<Node> getModelChildren() {
            return (List<Node>) getModel();
        }
    }

    private static class DiagramEditPartFactory implements EditPartFactory {
        @Override
        public EditPart createEditPart(EditPart context, Object model) {
            AbstractGraphicalEditPart part = (model instanceof Node) ? new NodeEditPart()
                    : new DiagramEditPart();
            part.setModel(model);
            return part;
        }
    }

    /** Draws an arrow from the figure of {@code from} to the figure of {@code to}. */
    private static void connect(ScrollingGraphicalViewer viewer, Node from, Node to) {
        IFigure source = ((GraphicalEditPart) viewer.getEditPartRegistry().get(from)).getFigure();
        IFigure target = ((GraphicalEditPart) viewer.getEditPartRegistry().get(to)).getFigure();

        PolylineConnection connection = new PolylineConnection();
        connection.setSourceAnchor(new ChopboxAnchor(source));
        connection.setTargetAnchor(new ChopboxAnchor(target));
        connection.setTargetDecoration(new PolygonDecoration());
        connection.setForegroundColor(ColorConstants.darkBlue);

        IFigure connectionLayer = ((ScalableRootEditPart) viewer.getRootEditPart())
                .getLayer(LayerConstants.CONNECTION_LAYER);
        connectionLayer.add(connection);
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("GEF Connections");

        List<Node> nodes = new ArrayList<>();
        Node root = new Node("Root", new Rectangle(190, 30, 120, 50));
        Node left = new Node("Left", new Rectangle(40, 170, 120, 50));
        Node right = new Node("Right", new Rectangle(340, 170, 120, 50));
        Node leaf = new Node("Leaf", new Rectangle(190, 300, 120, 50));
        nodes.add(root);
        nodes.add(left);
        nodes.add(right);
        nodes.add(leaf);

        ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();
        viewer.createControl(shell);
        viewer.setRootEditPart(new ScalableRootEditPart());
        viewer.setEditDomain(new EditDomain());
        viewer.setEditPartFactory(new DiagramEditPartFactory());
        viewer.setContents(nodes);

        // After setContents, so every node already has an EditPart (and a figure) to anchor to.
        connect(viewer, root, left);
        connect(viewer, root, right);
        connect(viewer, left, leaf);
        connect(viewer, right, leaf);

        shell.setSize(540, 420);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
