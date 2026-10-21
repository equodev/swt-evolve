package dev.equo.internal;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Menu accelerators: with every menu closed, pressing an item's accelerator (MOD1+N for New,
 * MOD1+Shift+C for Copy; MOD1 is Command on Cocoa, Ctrl elsewhere) fires Selection on that item.
 * Every event is printed with a [repro] prefix.
 * Run with {@code --drive} to send the keys with java.awt.Robot. For the native baseline, call
 * Config.forceEclipse() in place of the useEquo lines and run it with runEmbedExample.
 */
public class MenuAcceleratorSnippet {
    /** The key SWT.MOD1 stands for: Command on Cocoa, Ctrl elsewhere. */
    static final int MOD1_KEY = "cocoa".equals(SWT.getPlatform()) ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;

    public static void main(String[] args) throws Exception {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("MenuAcceleratorSnippet");
        shell.setLayout(new FillLayout());

        Menu bar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(bar);
        addItem(shell, bar, "&File", "&New\tCtrl+N", SWT.MOD1 | 'N');
        addItem(shell, bar, "&Edit", "&Copy Path\tCtrl+Shift+C", SWT.MOD1 | SWT.MOD2 | 'C');

        Text text = new Text(shell, SWT.BORDER | SWT.MULTI);
        text.addListener(SWT.FocusIn, e -> log("Text FocusIn"));
        display.addFilter(SWT.KeyDown, e -> log("KeyDown keyCode=" + e.keyCode + " stateMask=" + e.stateMask + " on " + e.widget));

        shell.setSize(320, 200);
        shell.open();
        text.setFocus();
        if (Arrays.asList(args).contains("--drive")) drive(display, shell, text);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
        System.exit(0);
    }

    static void addItem(Shell shell, Menu bar, String title, String text, int accelerator) {
        MenuItem cascade = new MenuItem(bar, SWT.CASCADE);
        cascade.setText(title);
        Menu menu = new Menu(shell, SWT.DROP_DOWN);
        cascade.setMenu(menu);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(text);
        item.setAccelerator(accelerator);
        item.addListener(SWT.Selection, e -> log("MenuItem Selection " + text.substring(0, text.indexOf('\t'))));
    }

    static void drive(Display display, Shell shell, Control first) {
        Thread t = new Thread(() -> {
            try {
                Robot r = new Robot();
                r.delay(6000);
                activate(r, display, shell, first);
                display.syncExec(() -> log("focus control: " + display.getFocusControl()));
                step(r, "MOD1+N", MOD1_KEY, KeyEvent.VK_N);
                step(r, "MOD1+Shift+C", MOD1_KEY, KeyEvent.VK_SHIFT, KeyEvent.VK_C);
                log("done");
                display.asyncExec(shell::dispose);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static void step(Robot r, String label, int... keys) {
        log("--- " + label);
        for (int k : keys) {
            r.keyPress(k);
            r.delay(150);
        }
        for (int i = keys.length - 1; i >= 0; i--) {
            r.keyRelease(keys[i]);
            r.delay(50);
        }
        r.delay(1200);
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

    static void log(String s) {
        System.out.println("[repro] " + s);
    }
}
