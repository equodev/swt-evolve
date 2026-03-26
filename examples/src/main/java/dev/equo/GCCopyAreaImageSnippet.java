package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests two copyArea scenarios:
 *
 * 1. GC from paint listener on a Canvas  — should capture drawn shapes.
 * 2. GC(button).copyArea(image)          — does it capture the button's content?
 */
public class GCCopyAreaImageSnippet {

	public static void main(String[] args) {
		Config.forceEquo();
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("GCCopyAreaImageSnippet");
		shell.setSize(720, 520);

		// ── Section 1: Canvas paint-listener GC ──────────────────────────────
		Label sec1 = new Label(shell, SWT.NONE);
		sec1.setText("1) GC from paint listener:");
		sec1.setBounds(10, 5, 300, 20);

		final int drawW = 260, drawH = 200;

		Canvas canvas = new Canvas(shell, SWT.BORDER);
		canvas.setBounds(10, 28, 680, 220);
		canvas.addPaintListener(e -> {
			GC gc = e.gc;

			// Left: draw shapes
			gc.setBackground(new Color(220, 240, 255));
			gc.fillRectangle(0, 0, drawW, drawH);
			gc.setBackground(new Color(255, 180, 0));
			gc.fillOval(20, 20, 80, 80);
			gc.setForeground(new Color(180, 0, 0));
			gc.setLineWidth(3);
			gc.drawRectangle(120, 30, 100, 70);
			gc.setBackground(new Color(0, 160, 0));
			gc.fillPolygon(new int[]{30, 180, 100, 110, 170, 180});
			gc.setForeground(new Color(0, 0, 0));
			gc.drawText("Original (direct)", 50, 155, true);

			// Capture left area
			Image snapshot = new Image(display, drawW, drawH);
			gc.copyArea(snapshot, 0, 0);

			// Right: show captured image
			gc.drawText("Snapshot via copyArea:", 290, 0, true);
			gc.drawImage(snapshot, 290, 20);
			snapshot.dispose();
		});

		// ── Section 2: GC(button).copyArea ───────────────────────────────────
		Label sec2 = new Label(shell, SWT.NONE);
		sec2.setText("2) GC(button).copyArea — does it capture the button's content?");
		sec2.setBounds(10, 260, 500, 20);

		Button btn = new Button(shell, SWT.PUSH);
		btn.setText("Hello, Button!");
		btn.setBounds(10, 285, 200, 50);

		Label arrow = new Label(shell, SWT.NONE);
		arrow.setText("→ copyArea result:");
		arrow.setBounds(220, 295, 150, 20);

		final int btnW = 200, btnH = 50;
		Canvas btnResult = new Canvas(shell, SWT.BORDER);
		btnResult.setBounds(375, 285, btnW, btnH);
		btnResult.addPaintListener(e -> {
			GC gcBtn = new GC(btn);
			Image snap = new Image(display, btnW, btnH);
			gcBtn.copyArea(snap, 0, 0);
			gcBtn.dispose();
			e.gc.drawImage(snap, 0, 0);
			snap.dispose();
		});

		Label hint = new Label(shell, SWT.NONE);
		hint.setText("(if blank/black → GC(button) does not capture widget rendering)");
		hint.setBounds(10, 345, 680, 20);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}