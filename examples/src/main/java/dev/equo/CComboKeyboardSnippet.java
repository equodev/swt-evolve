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
 * Drives a READ_ONLY and an editable CCombo from the keyboard, reached with Tab. Alt+Down opens the
 * list; moving with Down and pressing Enter puts the item into the text, fires Selection and closes
 * the list; Enter on the closed CCombo fires DefaultSelection. After every step the selection index,
 * text and list visibility are printed. Every event is printed with a [repro] prefix. Run with
 * {@code --drive} to send the keys with java.awt.Robot. For the native baseline, call
 * Config.defaultToEclipse() in place of the useEquo lines and run it with runEmbedExample.
 */
public class CComboKeyboardSnippet {
    static final String[] ITEMS = {"Alpha", "Bravo", "Charlie", "Delta", "Echo"};

    public static void main(String[] args) throws Exception {
        Config.useEquo(CCombo.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CComboKeyboardSnippet");
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
        combo.addListener(SWT.Selection, e -> log(name + " Selection index=" + combo.getSelectionIndex() + " text=" + combo.getText()));
        combo.addListener(SWT.DefaultSelection, e -> log(name + " DefaultSelection"));
        combo.addListener(SWT.FocusIn, e -> log(name + " FocusIn"));
        return combo;
    }

    static void drive(Display display, Shell shell, Control first, CCombo readOnly, CCombo editable) {
        Thread t = new Thread(() -> {
            try {
                Robot r = new Robot();
                r.delay(6000);
                activate(r, display, shell, first);
                display.syncExec(() -> log("focus control: " + display.getFocusControl()));
                for (CCombo combo : new CCombo[] {readOnly, editable}) {
                    step(r, display, combo, "Tab", KeyEvent.VK_TAB);
                    runKeys(r, display, combo);
                }
                log("=== focus by click");
                for (CCombo combo : new CCombo[] {readOnly, editable}) {
                    display.syncExec(() -> combo.select(0));
                    click(r, display, combo);
                    display.syncExec(() -> log("  after click: listVisible=" + combo.getListVisible() + " focus=" + display.getFocusControl()));
                    if (combo == readOnly) {
                        step(r, display, combo, "Escape (close the list the click opened)", KeyEvent.VK_ESCAPE);
                    }
                    runKeys(r, display, combo);
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

    static void runKeys(Robot r, Display display, CCombo combo) {
        step(r, display, combo, "Alt+Down", KeyEvent.VK_ALT, KeyEvent.VK_DOWN);
        step(r, display, combo, "Down", KeyEvent.VK_DOWN);
        step(r, display, combo, "Down", KeyEvent.VK_DOWN);
        step(r, display, combo, "Enter (in the list)", KeyEvent.VK_ENTER);
        step(r, display, combo, "Enter (list closed)", KeyEvent.VK_ENTER);
    }

    static void step(Robot r, Display display, CCombo combo, String label, int... keys) {
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
