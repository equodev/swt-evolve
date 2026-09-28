package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A Canvas that paints through a double buffer, the way an owner-drawn control does: everything
 * goes into an off-screen {@link Image} and that image is blitted in one go.
 *
 * <p>The buffer is asked for in SWT coordinates, so under {@code swt.autoScale} it has to be
 * allocated at the zoom's pixel count or the blit magnifies too few pixels and the whole control
 * comes out soft. Run it with and without {@code -Dswt.autoScale=200}; the text must look the same.
 *
 * <pre>
 * ./gradlew :swt-evolve:examples:runDeskExample -PmainClass=dev.equo.DoubleBufferCanvasSnippet
 * </pre>
 */
public class DoubleBufferCanvasSnippet {

    private static final String LINE1 = "This guide provides you with a dynamic listing of";
    private static final String LINE2 = "workflows for the currently open perspective.";

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Double buffer canvas");
        shell.setLayout(new FillLayout());

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.addPaintListener(e -> {
            Rectangle area = canvas.getClientArea();
            if (area.width <= 0 || area.height <= 0) return;
            Image buffer = new Image(display, area.width, area.height);
            GC bufferGC = new GC(buffer);
            try {
                bufferGC.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
                bufferGC.fillRectangle(0, 0, area.width, area.height);
                bufferGC.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
                bufferGC.drawText(LINE1, 4, 4, true);
                bufferGC.drawText(LINE2, 4, 4 + bufferGC.textExtent(LINE1).y, true);
            } finally {
                bufferGC.dispose();
            }
            e.gc.drawImage(buffer, 0, 0);
            Rectangle reported = buffer.getBounds();
            buffer.dispose();
            System.out.println("[dbuf] clientArea=" + area.width + "x" + area.height
                    + " bufferBounds=" + reported
                    + " deviceZoom=" + org.eclipse.swt.internal.DPIUtil.getDeviceZoom());
        });

        shell.setSize(600, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
