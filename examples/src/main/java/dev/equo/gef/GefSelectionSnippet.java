package dev.equo.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A GEF viewer with selectable nodes, one of them selected on open.
 * <p>
 * Selection handles are not part of any node's own figure: GEF puts them on the root part's handle
 * layer, painted outside the ordinary figure pass. That makes this the snippet that covers the
 * layer a diagram editor uses for everything it draws *about* the diagram rather than *in* it.
 */
public class GefSelectionSnippet {

    private record Node(String name, Rectangle bounds) {
    }

    private static class NodeEditPart extends AbstractGraphicalEditPart {
        @Override
        protected IFigure createFigure() {
            Label label = new Label(((Node) getModel()).name());
            label.setOpaque(true);
            label.setBackgroundColor(ColorConstants.lightGray);
            label.setBorder(new LineBorder(ColorConstants.gray, 1));
            return label;
        }

        @Override
        protected void createEditPolicies() {
            // What gives the part its selection handles once the viewer selects it.
            installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new NonResizableEditPolicy());
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

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("GEF Selection");

        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            nodes.add(new Node("Node " + (i + 1),
                    new Rectangle(50 + (i % 3) * 160, 60 + (i / 3) * 140, 120, 60)));
        }

        ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();
        viewer.createControl(shell);
        viewer.setRootEditPart(new ScalableRootEditPart());
        viewer.setEditDomain(new EditDomain());
        viewer.setEditPartFactory(new DiagramEditPartFactory());
        viewer.setContents(nodes);

        // Selecting from code rather than by click keeps the snippet deterministic when captured.
        EditPart selected = (EditPart) viewer.getEditPartRegistry().get(nodes.get(1));
        viewer.select(selected);

        shell.setSize(560, 380);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
