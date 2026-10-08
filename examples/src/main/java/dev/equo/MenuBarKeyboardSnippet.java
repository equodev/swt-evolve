package dev.equo;

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
 * Opens a Shell's menu bar from the keyboard. Alt+F opens the &File menu, Alt+E the &Edit menu, and
 * F10 or a lone Alt activates the bar so that Down opens its first menu; each opened menu fires Show.
 * Every event is printed with a [repro] prefix. Run with {@code --drive} to send the keys with
 * java.awt.Robot. For the native baseline, call
 * Config.defaultToEclipse() in place of the useEquo lines and run it with runEmbedExample.
 */
public class MenuBarKeyboardSnippet {
    public static void main(String[] args) throws Exception {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("MenuBarKeyboardSnippet");
        shell.setLayout(new FillLayout());

        Menu bar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(bar);
        addMenu(shell, bar, "&File", "&New", "E&xit");
        addMenu(shell, bar, "&Edit", "&Copy", "&Paste");

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

    static void addMenu(Shell shell, Menu bar, String title, String... items) {
        MenuItem cascade = new MenuItem(bar, SWT.CASCADE);
        cascade.setText(title);
        Menu menu = new Menu(shell, SWT.DROP_DOWN);
        cascade.setMenu(menu);
        menu.addListener(SWT.Show, e -> log("Menu Show " + title));
        menu.addListener(SWT.Hide, e -> log("Menu Hide " + title));
        for (String s : items) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(s);
            item.addListener(SWT.Arm, e -> log("MenuItem Arm " + s));
            item.addListener(SWT.Selection, e -> log("MenuItem Selection " + s));
        }
    }

    static void drive(Display display, Shell shell, Control first) {
        Thread t = new Thread(() -> {
            try {
                Robot r = new Robot();
                r.delay(6000);
                activate(r, display, shell, first);
                display.syncExec(() -> log("focus control: " + display.getFocusControl()));
                step(r, "Alt+F", KeyEvent.VK_ALT, KeyEvent.VK_F);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "F10", KeyEvent.VK_F10);
                step(r, "Down", KeyEvent.VK_DOWN);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "Alt", KeyEvent.VK_ALT);
                step(r, "Down", KeyEvent.VK_DOWN);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "Alt+E", KeyEvent.VK_ALT, KeyEvent.VK_E);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
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
        for (int k : keys) r.keyPress(k);
        for (int i = keys.length - 1; i >= 0; i--) r.keyRelease(keys[i]);
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
