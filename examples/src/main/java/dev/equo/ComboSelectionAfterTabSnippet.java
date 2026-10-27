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
 * Tabbing into an editable Combo selects all of its text, and getSelection() reports that range; typing a
 * character then replaces the text and leaves the caret after it. The snippet prints getText(), getSelection()
 * and getCaretPosition() of an editable Combo and of a Text, both preset to "Initial", after a Tab into each and
 * after typing "x". On Windows the Combo reports (0,7) after the Tab and (1,1) after "x"; the Text keeps the
 * caret at the start, so "x" is inserted before its text.
 * Every line is printed with a [repro] prefix. Run with {@code --drive} to send the click and keys with
 * java.awt.Robot. For the native baseline, drop the Config lines and run it against the stock SWT jar.
 */
public class ComboSelectionAfterTabSnippet {
    public static void main(String[] args) {
        Config.useEquo(Combo.class);
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ComboSelectionAfterTabSnippet");
        shell.setLayout(new RowLayout(SWT.VERTICAL));

        Text start = new Text(shell, SWT.BORDER);
        start.setText("start");
        Combo combo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER);
        combo.setItems("Initial", "Other");
        combo.setText("Initial");
        Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
        text.setText("Initial");
        Label values = new Label(shell, SWT.NONE);
        values.setText("\n\n\n\n");

        shell.setSize(460, 300);
        shell.open();
        String capture = Arrays.stream(args).filter(a -> a.startsWith("--capture=")).map(a -> a.substring(10))
                .findFirst().orElse(null);
        if (Arrays.asList(args).contains("--drive")) drive(display, shell, start, combo, text, values, capture);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
        System.exit(0);
    }

    static void drive(Display display, Shell shell, Text start, Combo combo, Text text, Label values, String capture) {
        Thread t = new Thread(() -> {
            try {
                Robot r = new Robot();
                r.delay(6000);
                display.syncExec(shell::forceActive);
                r.delay(500);
                click(r, display, start);
                boolean[] active = new boolean[1];
                display.syncExec(() -> {
                    active[0] = display.getActiveShell() == shell;
                    log("shell " + shell.getBounds() + " active=" + active[0] + " focus after click: "
                            + name(display.getFocusControl(), start, text));
                });
                if (!active[0]) capture(r, display, shell, capture, "-inactive");
                key(r, KeyEvent.VK_TAB);
                boolean[] reached = new boolean[1];
                display.syncExec(() -> reached[0] = display.getFocusControl() == combo);
                if (!reached[0]) {
                    log("the Tab did not reach the window, so the keys would go elsewhere: stopping");
                    display.asyncExec(shell::dispose);
                    return;
                }
                show(display, values, "Combo after Tab: " + syncState(display, start, combo, text));
                capture(r, display, shell, capture, "-tab");
                key(r, KeyEvent.VK_X);
                show(display, values, "Combo after x: " + syncState(display, start, combo, text));
                key(r, KeyEvent.VK_TAB);
                show(display, values, "Text after Tab: " + syncState(display, start, text, text));
                key(r, KeyEvent.VK_X);
                show(display, values, "Text after x: " + syncState(display, start, text, text));
                capture(r, display, shell, capture, "");
                log("done");
                display.asyncExec(shell::dispose);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    static String syncState(Display display, Text start, Control control, Text text) {
        String[] state = new String[1];
        display.syncExec(() -> state[0] = state(display, start, control, text));
        return state[0];
    }

    /** Prints a line and shows it in the window, so a capture of the window carries the values. */
    static void show(Display display, Label values, String line) {
        log(line);
        display.syncExec(() -> {
            String shown = values.getText().strip();
            values.setText(shown.isEmpty() ? line : shown + "\n" + line);
            values.getParent().layout(true, true);
        });
    }

    /** Saves the window as {@code <prefix><suffix>.png} when {@code --capture=<prefix>} was given. */
    static void capture(Robot r, Display display, Shell shell, String prefix, String suffix) throws java.io.IOException {
        if (prefix == null) return;
        r.delay(500);
        org.eclipse.swt.graphics.Rectangle[] bounds = new org.eclipse.swt.graphics.Rectangle[1];
        display.syncExec(() -> bounds[0] = shell.getBounds());
        java.awt.image.BufferedImage image = r.createScreenCapture(
                new java.awt.Rectangle(bounds[0].x, bounds[0].y, bounds[0].width, bounds[0].height));
        javax.imageio.ImageIO.write(image, "png", new java.io.File(prefix + suffix + ".png"));
    }

    static String state(Display display, Text start, Control control, Text text) {
        String focus = name(display.getFocusControl(), start, text);
        if (control instanceof Combo c) {
            return "focus=" + focus + " text=" + c.getText() + " selection=" + c.getSelection()
                    + " caret=" + c.getCaretPosition();
        }
        Text t = (Text) control;
        return "focus=" + focus + " text=" + t.getText() + " selection=" + t.getSelection()
                + " caret=" + t.getCaretPosition();
    }

    static String name(Control focus, Text start, Text text) {
        if (focus == null) return "none";
        if (focus == start) return "start";
        if (focus instanceof Combo) return "Combo";
        if (focus == text) return "Text";
        return focus.toString();
    }

    static void key(Robot r, int key) {
        r.keyPress(key);
        r.keyRelease(key);
        r.delay(1200);
    }

    static void click(Robot r, Display display, Control control) {
        Point[] center = new Point[1];
        display.syncExec(() -> {
            Point size = control.getSize();
            center[0] = display.map(control, null, size.x / 2, size.y / 2);
        });
        log("click at " + center[0]);
        r.mouseMove(center[0].x, center[0].y);
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        r.delay(1200);
    }

    static void log(String s) {
        System.out.println("[repro] " + s);
    }
}
