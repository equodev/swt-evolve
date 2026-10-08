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
 * Opens a Tree's pop-up menu from the keyboard. With the Tree focused, Shift+F10 or the context-menu
 * (Apps) key fires MenuDetect with detail MENU_KEYBOARD and shows the menu; arrows then arm items,
 * Enter selects one and a mnemonic letter selects its item. Every event is printed with a [repro] prefix.
 * Run with {@code --drive} to send the keys with java.awt.Robot. For the native baseline, call
 * Config.defaultToEclipse() in place of the useEquo lines and run it with runEmbedExample.
 */
public class ContextMenuKeyboardSnippet {
    public static void main(String[] args) throws Exception {
        Config.useEquo(Tree.class);
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ContextMenuKeyboardSnippet");
        shell.setLayout(new FillLayout());

        Tree tree = new Tree(shell, SWT.BORDER);
        for (String s : new String[] {"First", "Second", "Third"}) {
            new TreeItem(tree, SWT.NONE).setText(s);
        }
        tree.setSelection(tree.getItem(0));
        tree.addListener(SWT.MenuDetect, e -> log("Tree MenuDetect detail=" + (e.detail == SWT.MENU_KEYBOARD ? "MENU_KEYBOARD" : e.detail == SWT.MENU_MOUSE ? "MENU_MOUSE" : String.valueOf(e.detail))));
        tree.addListener(SWT.FocusIn, e -> log("Tree FocusIn"));

        Menu menu = new Menu(tree);
        menu.addListener(SWT.Show, e -> log("Menu Show"));
        menu.addListener(SWT.Hide, e -> log("Menu Hide"));
        for (String s : new String[] {"&Open", "&Rename", "&Delete"}) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(s);
            item.addListener(SWT.Arm, e -> log("MenuItem Arm " + item.getText()));
            item.addListener(SWT.Selection, e -> log("MenuItem Selection " + item.getText()));
        }
        tree.setMenu(menu);
        display.addFilter(SWT.KeyDown, e -> log("KeyDown keyCode=" + e.keyCode + " stateMask=" + e.stateMask + " on " + e.widget));

        shell.setSize(300, 200);
        shell.open();
        if (Arrays.asList(args).contains("--drive")) drive(display, shell, tree);
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
                step(r, "Shift+F10", KeyEvent.VK_SHIFT, KeyEvent.VK_F10);
                step(r, "Down", KeyEvent.VK_DOWN);
                step(r, "Enter", KeyEvent.VK_ENTER);
                step(r, "Escape", KeyEvent.VK_ESCAPE);
                step(r, "Apps key", KeyEvent.VK_CONTEXT_MENU);
                step(r, "r (mnemonic of Rename)", KeyEvent.VK_R);
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
