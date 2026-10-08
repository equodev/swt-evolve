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
 * Drives a READ_ONLY and an editable Combo from the keyboard, reached with Tab. Up/Down/Home/End/PgDn
 * move the selection and fire Selection, a letter selects the next item starting with it (READ_ONLY),
 * Alt+Down and F4 open the list, and Enter fires DefaultSelection. After every step the Combo's
 * selection index, text and list visibility are printed. Every event is printed with a [repro] prefix.
 * Run with {@code --drive} to send the keys with java.awt.Robot. For the native baseline, call
 * Config.defaultToEclipse() in place of the useEquo lines and run it with runEmbedExample.
 */
public class ComboKeyboardSnippet {
    static final String[] ITEMS = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel"};

    public static void main(String[] args) throws Exception {
        Config.useEquo(Combo.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ComboKeyboardSnippet");
        shell.setLayout(new RowLayout(SWT.VERTICAL));

        Text start = new Text(shell, SWT.BORDER);
        start.setText("start");
        Combo readOnly = combo(shell, SWT.READ_ONLY, "READ_ONLY");
        Combo editable = combo(shell, SWT.DROP_DOWN, "editable");
        display.addFilter(SWT.KeyDown, e -> log("KeyDown keyCode=" + e.keyCode + " stateMask=" + e.stateMask + " on " + e.widget));

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

    static Combo combo(Shell shell, int style, String name) {
        Combo combo = new Combo(shell, style);
        combo.setItems(ITEMS);
        combo.select(0);
        combo.addListener(SWT.Selection, e -> log(name + " Selection index=" + combo.getSelectionIndex()));
        combo.addListener(SWT.DefaultSelection, e -> log(name + " DefaultSelection"));
        combo.addListener(SWT.Modify, e -> log(name + " Modify text=" + combo.getText()));
        combo.addListener(SWT.FocusIn, e -> log(name + " FocusIn"));
        return combo;
    }

    static void drive(Display display, Shell shell, Control first, Combo readOnly, Combo editable) {
        Thread t = new Thread(() -> {
            try {
                Robot r = new Robot();
                r.delay(6000);
                activate(r, display, shell, first);
                display.syncExec(() -> log("focus control: " + display.getFocusControl()));
                for (boolean byClick : new boolean[] {false, true}) {
                    log("=== focus " + (byClick ? "by click" : "by Tab"));
                    if (byClick) {
                        click(r, display, readOnly);
                        step(r, display, readOnly, "Escape (close the list the click opened)", KeyEvent.VK_ESCAPE);
                        display.syncExec(() -> readOnly.select(0));
                    } else {
                        step(r, display, readOnly, "Tab", KeyEvent.VK_TAB);
                    }
                    step(r, display, readOnly, "Down", KeyEvent.VK_DOWN);
                    step(r, display, readOnly, "Down", KeyEvent.VK_DOWN);
                    step(r, display, readOnly, "Up", KeyEvent.VK_UP);
                    step(r, display, readOnly, "End", KeyEvent.VK_END);
                    step(r, display, readOnly, "Home", KeyEvent.VK_HOME);
                    step(r, display, readOnly, "PgDn", KeyEvent.VK_PAGE_DOWN);
                    step(r, display, readOnly, "d (type-ahead)", KeyEvent.VK_D);
                    step(r, display, readOnly, "Alt+Down", KeyEvent.VK_ALT, KeyEvent.VK_DOWN);
                    step(r, display, readOnly, "Escape", KeyEvent.VK_ESCAPE);
                    step(r, display, readOnly, "F4", KeyEvent.VK_F4);
                    step(r, display, readOnly, "Escape", KeyEvent.VK_ESCAPE);
                    step(r, display, readOnly, "Enter", KeyEvent.VK_ENTER);
                    if (byClick) {
                        display.syncExec(() -> editable.select(0));
                        click(r, display, editable);
                    } else {
                        step(r, display, editable, "Tab", KeyEvent.VK_TAB);
                    }
                    step(r, display, editable, "Down", KeyEvent.VK_DOWN);
                    step(r, display, editable, "Down", KeyEvent.VK_DOWN);
                    step(r, display, editable, "Alt+Down", KeyEvent.VK_ALT, KeyEvent.VK_DOWN);
                    step(r, display, editable, "Escape", KeyEvent.VK_ESCAPE);
                    step(r, display, editable, "Enter", KeyEvent.VK_ENTER);
                    if (!byClick) click(r, display, first);
                }
                log("done");
                display.asyncExec(shell::dispose);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static void step(Robot r, Display display, Combo combo, String label, int... keys) {
        log("--- " + label);
        for (int k : keys) r.keyPress(k);
        for (int i = keys.length - 1; i >= 0; i--) r.keyRelease(keys[i]);
        r.delay(1200);
        display.syncExec(() -> log("  state index=" + combo.getSelectionIndex() + " text=" + combo.getText()
                + " listVisible=" + combo.getListVisible() + " focus=" + display.getFocusControl()));
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
