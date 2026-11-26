package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Window;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A modal SWT dialog whose content is a Swing panel embedded with {@code SWT_AWT}, opened the way
 * RCP applications wrap a Swing settings panel: inside a plain AWT {@code Panel} in the frame, with
 * state the panel needs registered in a thread-local on the AWT event dispatch thread before the
 * dialog opens, and removed from the same thread after it closes. The panel holds a text editor
 * that, like code editors built on Swing text components, measures its font through
 * {@code getGraphics()} as soon as it is added to a window with a size.
 *
 * <p>Prints, for each opening: {@code addNotify graphics=<null|ok> window=<w>x<h>} from the editor,
 * and {@code edt before=<thread> after=<thread> removed=<ok|error>}. Natively the graphics is never
 * null once the window has a size, and the event dispatch thread is the same thread before and
 * after, so the thread-local is found and removed.
 *
 * <p>Run with {@code ./gradlew :swt-evolve:examples:runDeskExample -PmainClass=dev.equo.swing.SwingDialogGraphicsSnippet}.
 */
public class SwingDialogGraphicsSnippet {

    private static final ThreadLocal<Deque<String>> CONTEXT = ThreadLocal.withInitial(ArrayDeque::new);

    public static void main(String[] args) {
        Config.forceEquo();
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SwingDialogGraphicsSnippet");
        shell.setSize(500, 300);
        shell.open();
        display.timerExec(1500, () -> openDialog(shell, 1));
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    static void openDialog(Shell parent, int attempt) {
        String[] before = new String[1];
        inEdt(() -> {
            before[0] = Thread.currentThread().getName();
            CONTEXT.get().push("settings");
        });
        Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Settings " + attempt);
        dialog.setLayout(new FillLayout());
        Composite embedded = new Composite(dialog, SWT.EMBEDDED);
        Frame frame = SWT_AWT.new_Frame(embedded);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Rule:"));
        panel.add(new MeasuringEditor());
        // The Swing panel inside a plain AWT Panel, the wrapper RCP applications put between the
        // frame and their Swing content.
        java.awt.Panel awtPanel = new java.awt.Panel(new java.awt.BorderLayout());
        awtPanel.add(panel);
        frame.add(awtPanel);
        dialog.setSize(420, 240);
        dialog.open();
        Display display = dialog.getDisplay();
        display.timerExec(2500, dialog::close);
        while (!dialog.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        String[] after = new String[1];
        String removed;
        try {
            inEdt(() -> {
                after[0] = Thread.currentThread().getName();
                if (CONTEXT.get().poll() == null) throw new IllegalStateException("no context on this thread");
            });
            removed = "ok";
        } catch (RuntimeException e) {
            removed = "error (" + e.getMessage() + ")";
        }
        System.out.println("edt before=" + before[0] + " after=" + after[0] + " removed=" + removed);
        if (attempt < 2) parent.getDisplay().timerExec(500, () -> openDialog(parent, attempt + 1));
    }

    /** Runs on the event dispatch thread and waits, inline when already on it. */
    static void inEdt(Runnable r) {
        if (EventQueue.isDispatchThread()) {
            r.run();
            return;
        }
        try {
            EventQueue.invokeAndWait(r);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (RuntimeException) e.getCause();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Measures its font with getGraphics() once its window has a size, as Swing code editors do. */
    static final class MeasuringEditor extends JTextArea {
        MeasuringEditor() {
            super("$Value$ > 10 => \"high\"", 4, 24);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null && window.getWidth() > 0 && window.getHeight() > 0) {
                Graphics g = getGraphics();
                System.out.println("addNotify graphics=" + (g == null ? "null" : "ok")
                        + " window=" + window.getWidth() + "x" + window.getHeight());
                if (g != null) g.dispose();
            } else {
                System.out.println("addNotify before the window has a size");
            }
        }
    }
}
