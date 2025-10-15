package dev.equo.draw2d;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ScaledGraphics;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Draw2DHelloWorldScaledGraphics {

    static class ScaledStringFigure extends Figure {
        private String text;
        private double scale;

        public ScaledStringFigure(String text, double scale) {
            this.text = text;
            this.scale = scale;
        }

        @Override
        protected void paintFigure(Graphics graphics) {
            super.paintFigure(graphics);

            // Create a ScaledGraphics to force the flow through ScaledGraphics#drawString
            ScaledGraphics scaledGraphics = new ScaledGraphics(graphics);
            scaledGraphics.scale(scale);

            // This will call ScaledGraphics#drawString
            scaledGraphics.drawString(text, 10, 10);

            scaledGraphics.dispose();
        }
    }

    public static void main(String[] args) {
        Display d = new Display();
        Shell shell = new Shell(d);
        shell.setLayout(new FillLayout());

        FigureCanvas canvas = new FigureCanvas(shell);

        // Create a scaled figure that will use ScaledGraphics#drawString
        ScaledStringFigure figure = new ScaledStringFigure("Hello World via ScaledGraphics!", 3); //$NON-NLS-1$
        canvas.setContents(figure);

        shell.setText("Draw2d with ScaledGraphics"); //$NON-NLS-1$
        shell.setSize(400, 200);
        shell.open();
        while (!shell.isDisposed()) {
            while (!d.readAndDispatch()) {
                d.sleep();
            }
        }

    }

}
