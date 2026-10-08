package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * When a CCombo fires Modify, getText() already returns the new text. The listener prints getText()
 * for each Modify while the selection is changed from the keyboard and while text is typed. Every
 * event is printed with a [repro] prefix. Run with {@code --drive} to send the keys with
 * java.awt.Robot. For the native baseline, call
 * Config.defaultToEclipse() in place of the useEquo lines and run it with runEmbedExample.
 */
public class CComboModifySnippet {
    static final String[] ITEMS = {"Alpha", "Bravo", "Charlie", "Delta", "Echo"};

    public static void main(String[] args) throws Exception {
        Config.useEquo(CCombo.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CComboModifySnippet");
        shell.setLayout(new RowLayout(SWT.VERTICAL));

        Text start = new Text(shell, SWT.BORDER);
        start.setText("start");
        CCombo readOnly = combo(shell, SWT.READ_ONLY | SWT.BORDER, "READ_ONLY");
        CCombo editable = combo(shell, SWT.BORDER, "editable");

        shell.setSize(300, 220);
        shell.open();
        start.setFocus();
        if (Arrays.asList(args).contains("--drive")) drive(display, shell, start, readOnly, editable);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
        System.exit(0);
    }

    static CCombo combo(Shell shell, int style, String name) {
        CCombo combo = new CCombo(shell, style);
        combo.setItems(ITEMS);
        combo.select(0);
        combo.addListener(SWT.Modify, e -> log(name + " Modify getText()=" + combo.getText()));
        combo.addListener(SWT.Selection, e -> log(name + " Selection index=" + combo.getSelectionIndex()));
        return combo;
    }

    static void drive(Display display, Shell shell, Control first, CCombo readOnly, CCombo editable) {
        Thread t = new Thread(() -> {
            try {
                Robot r = new Robot();
                r.delay(6000);
                activate(r, display, shell, first);
                display.syncExec(() -> log("focus control: " + display.getFocusControl()));
                step(r, display, readOnly, "Tab", KeyEvent.VK_TAB);
                step(r, display, readOnly, "Down", KeyEvent.VK_DOWN);
                step(r, display, readOnly, "Down", KeyEvent.VK_DOWN);
                step(r, display, readOnly, "End", KeyEvent.VK_END);
                step(r, display, editable, "Tab", KeyEvent.VK_TAB);
                step(r, display, editable, "Down", KeyEvent.VK_DOWN);
                step(r, display, editable, "End", KeyEvent.VK_END);
                step(r, display, editable, "z", KeyEvent.VK_Z);
                step(r, display, editable, "y", KeyEvent.VK_Y);
                log("=== focus by click");
                display.syncExec(() -> { readOnly.select(0); editable.select(0); });
                click(r, display, readOnly);
                step(r, display, readOnly, "Escape (close the list the click opened)", KeyEvent.VK_ESCAPE);
                step(r, display, readOnly, "Down", KeyEvent.VK_DOWN);
                step(r, display, readOnly, "Down", KeyEvent.VK_DOWN);
                click(r, display, editable);
                step(r, display, editable, "Down", KeyEvent.VK_DOWN);
                step(r, display, editable, "End", KeyEvent.VK_END);
                step(r, display, editable, "z", KeyEvent.VK_Z);
                step(r, display, editable, "y", KeyEvent.VK_Y);
                log("done");
                display.asyncExec(shell::dispose);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static void step(Robot r, Display display, CCombo combo, String label, int... keys) {
        log("--- " + label);
        for (int k : keys) r.keyPress(k);
        for (int i = keys.length - 1; i >= 0; i--) r.keyRelease(keys[i]);
        r.delay(1200);
        display.syncExec(() -> log("  state text=" + combo.getText() + " focus=" + display.getFocusControl()));
    }

    static void activate(Robot r, Display display, Shell shell, Control control) {
        display.syncExec(shell::forceActive);
        r.delay(500);
        Point[] center = new Point[1];
        display.syncExec(() -> {
            if (display.getFocusControl() == null) {
                Point size = control.getSize();
                center[0] = display.map(control, null, size.x / 2, size.y / 2);
            }
        });
        if (center[0] != null) {
            r.mouseMove(center[0].x, center[0].y);
            r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            r.delay(800);
        }
    }

    static void click(Robot r, Display display, Control control) {
        Point[] center = new Point[1];
        display.syncExec(() -> {
            Point size = control.getSize();
            center[0] = display.map(control, null, size.x / 2, size.y / 2);
        });
        r.mouseMove(center[0].x, center[0].y);
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        r.delay(1200);
    }

    static void log(String s) {
        System.out.println("[repro] " + s);
    }
}
