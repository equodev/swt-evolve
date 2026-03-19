package dev.equo.internal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class CanvasExample {

    static Color textColor;
    static int y = 50;

    public static void main(String[] args) {
//        Config.forceEclipse();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Canvas GC Example");
        shell.setSize(300, 200);
        shell.setLayout(new FillLayout());

        Canvas canvas = new Canvas(shell, SWT.NONE);

        textColor = display.getSystemColor(SWT.COLOR_RED);

        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            gc.setForeground(textColor);
            gc.drawString("Hello", 50, y);
        });

        shell.open();

        display.timerExec(2000, () -> {
            textColor = display.getSystemColor(SWT.COLOR_GREEN);
            y = 90;
            canvas.redraw();
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        display.dispose();
    }
}