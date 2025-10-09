package dev.equo.draw2d.uml;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.swt.graphics.Color;

/**
 * since 3.0
 */
public class StickyNote extends FlowPage {

	static class DogEar extends AbstractBorder {
		private static final Insets INSETS = new Insets(3, 3, 3, 3);

		/**
		 * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
		 */
		@Override
		public Insets getInsets(IFigure figure) {
			return INSETS;
		}

		/**
		 * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
		 *      org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
		 */
		@Override
		public void paint(IFigure figure, Graphics g, Insets insets) {
			Rectangle r = getPaintRectangle(figure, insets);
			r.resize(-1, -1);
			PointList pl = new PointList(new int[] { -5, 5, 0, 5, -5, 0 });
			pl.translate(r.getTopRight());
			g.drawPolygon(pl);
			g.drawLine(r.getTopLeft(), r.getTopRight().translate(-5, 0));
			g.drawLine(r.getTopLeft(), r.getTopLeft());
			g.drawLine(r.getBottomLeft(), r.getBottomRight());
			g.drawLine(r.getTopRight().translate(0, 5), r.getBottomRight());
			g.drawLine(r.getTopLeft(), r.getBottomLeft());
		}
	}

	private TextFlow text = new TextFlow();

	public StickyNote() {
		setBorder(new DogEar());
		add(text);
		text.setText("This is a sticky note.  It wraps text based on its " + "width."); //$NON-NLS-1$ //$NON-NLS-2$
		setBackgroundColor(new Color(255, 255, 255));
	}

	/**
	 * @see org.eclipse.draw2d.text.FlowFigure#paintFigure(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void paintFigure(Graphics g) {
		Rectangle r = getBounds();
		PointList pl = new PointList(5);
		pl.addPoint(r.getTopLeft());
		pl.addPoint(r.getTopRight().translate(-6, 0));
		pl.addPoint(r.getTopRight().translate(0, 6));
		pl.addPoint(r.getBottomRight());
		pl.addPoint(r.getBottomLeft());
		g.fillPolygon(pl);
	}

}
