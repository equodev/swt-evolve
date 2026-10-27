package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Tab traversal right after the Shell opens. The first Text gets focus with setFocus(), and Tab then
 * moves focus to the next control in the tab order (the Button). The same is repeated after clicking
 * the Text. The focus control is printed after each step with a [repro] prefix. Run with
 * {@code --drive} to send the keys with java.awt.Robot. For the native baseline, call
 * Config.forceEclipse() in place of the useEquo lines and run it with runEmbedExample.
 */
public class TabTraversalAfterSetFocusSnippet {
    public static void main(String[] args) throws Exception {
        Config.useEquo(Text.class);
        Config.useEquo(Button.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TabTraversalAfterSetFocusSnippet");
        shell.setLayout(new RowLayout(SWT.VERTICAL));

        Text first = new Text(shell, SWT.BORDER);
        first.setText("first");
        Button button = new Button(shell, SWT.PUSH);
        button.setText("Button");
        Text second = new Text(shell, SWT.BORDER);
        second.setText("second");
        for (Control c : new Control[] {first, button, second}) {
            c.addListener(SWT.FocusIn, e -> log("FocusIn " + c));
        }
        shell.addListener(SWT.FocusIn, e -> log("FocusIn " + shell));

        shell.setSize(300, 180);
        shell.open();
        first.setFocus();
        if (Arrays.asList(args).contains("--drive")) drive(display, shell, first);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
        System.exit(0);
    }

    static void drive(Display display, Shell shell, Control first) {
        Thread t = new Thread(() -> {
            try {
                Robot r = new Robot();
                r.delay(6000);
                activate(r, display, shell, first);
                display.syncExec(() -> log("focus control: " + display.getFocusControl()));
                log("=== after setFocus()");
                step(r, display, "Tab", KeyEvent.VK_TAB);
                step(r, display, "Tab", KeyEvent.VK_TAB);
                log("=== after clicking the first Text");
                click(r, display, first);
                display.syncExec(() -> log("  focus=" + display.getFocusControl()));
                step(r, display, "Tab", KeyEvent.VK_TAB);
                step(r, display, "Tab", KeyEvent.VK_TAB);
                log("done");
                display.asyncExec(shell::dispose);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static void step(Robot r, Display display, String label, int... keys) {
        log("--- " + label);
        for (int k : keys) r.keyPress(k);
        for (int i = keys.length - 1; i >= 0; i--) r.keyRelease(keys[i]);
        r.delay(1200);
        display.syncExec(() -> log("  focus=" + display.getFocusControl()));
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
