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
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * The smallest useful GEF example: a {@link ScrollingGraphicalViewer} driven from a plain Shell,
 * with no workbench.
 * <p>
 * Where the draw2d snippets exercise a {@code FigureCanvas} directly, this one goes through GEF's
 * own stack -- {@link EditPartFactory}, {@link AbstractGraphicalEditPart}, {@link EditDomain} and a
 * {@link ScalableRootEditPart} -- which is how an application actually builds a diagram. The
 * viewer's figures are laid out well beyond the shell's client area, so the viewport scrolls and
 * the scrolled repaint path is covered too.
 */
public class GefBasicSnippet {

    /** Model: a named box at a fixed position, optionally linked to the previous one. */
    private record Node(String name, Rectangle bounds) {
    }

    private static class NodeEditPart extends AbstractGraphicalEditPart {
        @Override
        protected IFigure createFigure() {
            Label label = new Label(((Node) getModel()).name());
            label.setOpaque(true);
            label.setBackgroundColor(ColorConstants.lightGray);
            label.setBorder(new LineBorder(ColorConstants.blue, 2));
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

    private static List<Node> createModel() {
        List<Node> nodes = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                nodes.add(new Node("Node " + (row * 4 + col + 1),
                        new Rectangle(40 + col * 220, 40 + row * 130, 140, 60)));
            }
        }
        return nodes;
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("GEF Basic");

        ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();
        viewer.createControl(shell);
        viewer.setRootEditPart(new ScalableRootEditPart());
        viewer.setEditDomain(new EditDomain());
        viewer.setEditPartFactory(new DiagramEditPartFactory());
        viewer.setContents(createModel());

        shell.setSize(600, 450);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
