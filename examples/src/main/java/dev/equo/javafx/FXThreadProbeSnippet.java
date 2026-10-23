package dev.equo.javafx;

import dev.equo.swt.Config;

import javafx.application.Platform;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Which thread is JavaFX's application thread once an {@link FXCanvas} has started the toolkit,
 * and whether {@code javafx.embed.singleThread} is honoured.
 *
 * <p>An application embedding JavaFX in SWT reported, on every session restore,
 * {@code java.lang.IllegalStateException: Not on FX application thread; currentThread = main}.
 * That text is {@code com.sun.javafx.tk.Toolkit.checkFxUserThread}'s, verbatim, so it is thrown by
 * any FX API that asserts the thread. This snippet reproduces it in four lines of output, and the
 * answer it gives is that the arrangement is ordinary rather than broken:
 *
 * <pre>
 * the SWT thread is 'main'
 * on it, Platform.isFxApplicationThread() = false
 * FX's own application thread is 'JavaFX Application Thread'
 * snapshot from the SWT thread THREW: ... Not on FX application thread; currentThread = main
 * </pre>
 *
 * <p><b>Two things this settled, both measured rather than reasoned.</b> First, that install also
 * carries {@code -Djavafx.embed.singleThread=true}, which looks like it should put FX on the
 * embedding thread and visibly does not. It is not this bridge's knob at all: the string appears in
 * neither {@code javafx.graphics} nor {@code javafx.base} — it belongs to {@code javafx.swing},
 * where it puts FX on the AWT event thread for a {@code JFXPanel}. For an SWT embed it is inert,
 * upstream included. Second, an FX scene embedded in SWT has a JavaFX thread of its own in upstream
 * {@code FXCanvas} too, so an FX API called from the SWT thread throws there as well.
 *
 * <p>So the throw belongs to whatever calls FX from the SWT thread, not to the bridge. This
 * snippet's own calls do not: constructing the canvas and {@link FXCanvas#setScene} from the SWT
 * thread are both fine, and the bridge's rendering snapshots on the FX thread.
 *
 * <p>This prints the three facts that decide it, from the SWT thread, which is where the host's own
 * code runs and where the throw came from.
 *
 * <pre>
 * ./gradlew :examples:runExample -PmainClass=dev.equo.javafx.FXThreadProbeSnippet
 * ./gradlew :examples:runExample -PmainClass=dev.equo.javafx.FXThreadProbeSnippet \
 *     -Dexec.args=... -Djavafx.embed.singleThread=true
 * </pre>
 */
public class FXThreadProbeSnippet {

    public static void main(String[] args) throws Exception {
        // Set before anything touches FX: the embedding mode is read when the toolkit starts, and
        // our FXCanvas starts it from its own constructor.
        for (String a : args) {
            if (a.startsWith("-D")) {
                int eq = a.indexOf('=');
                System.setProperty(a.substring(2, eq), a.substring(eq + 1));
            }
        }
        say("javafx.embed.singleThread = " + System.getProperty("javafx.embed.singleThread"));
        say("javafx.embed.isEventThread = " + System.getProperty("javafx.embed.isEventThread"));
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("FXThreadProbeSnippet");
        shell.setLayout(new FillLayout());

        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        Label label = new Label("probe");
        canvas.setScene(new Scene(new VBox(label), 200, 80));

        shell.setSize(320, 160);
        shell.open();

        say("the SWT thread is '" + Thread.currentThread().getName() + "'");
        say("on it, Platform.isFxApplicationThread() = " + Platform.isFxApplicationThread());

        // Which thread FX actually calls its own. In single-thread embedding this must be the SWT
        // thread; a different name is the whole defect.
        CountDownLatch ran = new CountDownLatch(1);
        String[] fxThread = {"(runLater never ran)"};
        Platform.runLater(() -> {
            fxThread[0] = Thread.currentThread().getName();
            ran.countDown();
        });
        for (int i = 0; i < 100 && ran.getCount() > 0; i++) {
            if (!display.readAndDispatch()) {
                Thread.sleep(50);
            }
        }
        ran.await(2, TimeUnit.SECONDS);
        say("FX's own application thread is '" + fxThread[0] + "'");

        // And the host's shape: an FX API that asserts the thread, called from the SWT thread. This
        // is what the application's session restore does and what threw there.
        try {
            label.snapshot(null, null);
            say("snapshot from the SWT thread: OK — FX accepts this thread");
        } catch (IllegalStateException e) {
            say("snapshot from the SWT thread THREW: " + e);
        }

        say("verdict: " + (Thread.currentThread().getName().equals(fxThread[0])
                ? "FX adopted the embedding thread — single-thread embedding is in force"
                : "FX runs on a thread of its own; every FX call from the host's thread will throw"));

        display.dispose();
        System.exit(0);
    }

    private static void say(String s) {
        System.out.println("[fx-probe] " + s);
    }
}
