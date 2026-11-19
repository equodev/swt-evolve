package dev.equo.internal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.graphics.GC;

public class CanvasExample {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Canvas GC Example");
        shell.setSize(300, 200);
        shell.setLayout(new FillLayout());

        Canvas canvas = new Canvas(shell, SWT.NONE);

        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            gc.setForeground(display.getSystemColor(SWT.COLOR_GREEN));
            gc.drawString("Hello", 50, 90);
        });

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        display.dispose();
    }
}
