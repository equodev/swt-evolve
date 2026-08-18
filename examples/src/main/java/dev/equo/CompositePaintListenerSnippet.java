package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A Composite — not a Canvas — that draws its whole content from a PaintListener, the shape a
 * colour-scale editor has: a gradient with a ruler down one side, and a hidden inline Text editor
 * that pops over a value on demand.
 *
 * The two panels differ in exactly one thing: the right one owns a child (the editor), the left one
 * does not. Both must paint identically. If only the left one paints, the composite's children
 * branch is dropping the draw operations Java emits.
 *
 * Every other GC example here paints on a Canvas, which is why this path went unexercised.
 */
public class CompositePaintListenerSnippet {

    private static final int PANEL_W = 260;
    private static final int PANEL_H = 460;

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CompositePaintListenerSnippet");
        shell.setSize(620, 600);

        new Label(shell, SWT.NONE).setBounds(20, 12, 260, 20);
        Label left = new Label(shell, SWT.NONE);
        left.setText("no children");
        left.setBounds(20, 12, 260, 20);
        Label right = new Label(shell, SWT.NONE);
        right.setText("one child (the inline editor)");
        right.setBounds(310, 12, 260, 20);

        Composite childless = new Composite(shell, SWT.NONE);
        childless.setBounds(20, 36, PANEL_W, PANEL_H);
        childless.addPaintListener(e -> drawScale(e.gc, "no children"));

        Composite withChild = new Composite(shell, SWT.NONE);
        withChild.setBounds(310, 36, PANEL_W, PANEL_H);
        withChild.addPaintListener(e -> drawScale(e.gc, "with child"));

        // The inline editor: present, hidden, and small — the only structural difference between
        // the two panels, and enough to route this one down the other build branch.
        Text editor = new Text(withChild, SWT.BORDER);
        editor.setBounds(4, 4, 57, 22);
        editor.setText("255.0");
        editor.setVisible(false);

        Button redraw = new Button(shell, SWT.PUSH);
        redraw.setText("redraw() both");
        redraw.setBounds(20, 508, 180, 30);
        redraw.addListener(SWT.Selection, e -> {
            childless.redraw();
            withChild.redraw();
        });

        Button toggleEditor = new Button(shell, SWT.PUSH);
        toggleEditor.setText("toggle editor");
        toggleEditor.setBounds(210, 508, 180, 30);
        toggleEditor.addListener(SWT.Selection, e -> editor.setVisible(!editor.getVisible()));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    /** Greyscale ramp, a ruler with labelled ticks, and a rotated caption. */
    private static void drawScale(GC gc, String caption) {
        int rampX = 60, rampW = 120;
        for (int y = 0; y < PANEL_H; y++) {
            int v = (int) (255.0 * y / PANEL_H);
            Color c = new Color(gc.getDevice(), v, v, v);
            gc.setBackground(c);
            gc.fillRectangle(rampX, y, rampW, 1);
            c.dispose();
        }

        Color black = new Color(gc.getDevice(), 0, 0, 0);
        gc.setForeground(black);
        gc.drawRectangle(rampX, 0, rampW, PANEL_H - 1);
        for (int i = 0; i <= 10; i++) {
            int y = i * (PANEL_H - 1) / 10;
            gc.drawLine(rampX - 8, y, rampX, y);
            gc.drawText(String.valueOf(i * 25.5), 4, Math.min(y, PANEL_H - 16), true);
        }
        gc.drawText(caption, rampX + rampW + 8, 4, true);
        black.dispose();
    }
}
