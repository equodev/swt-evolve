package dev.equo.javafx;

import dev.equo.swt.Config;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 * Self-driving check that a JavaFX event handler in an {@code FXCanvas}-embedded scene can call
 * SWT/JFace.
 *
 * <p>Applications embed clickable JavaFX nodes whose handlers open SWT/JFace UI — a link whose
 * {@code setOnMouseClicked} opens a JFace dialog, for instance ({@code
 * PreferencesUtil.createPreferenceDialogOn(...).open()} → {@code new Shell(...)}). Upstream FXCanvas
 * runs such a handler on the SWT display thread ({@code javafx.embed.isEventThread=true}), so the
 * {@code Shell} is legal. Evolve keeps JavaFX on its own thread for rendering, so without the
 * SWT-thread input dispatch the handler ran on the JavaFX application thread, where {@code
 * DartShell}'s constructor rejects it with {@code SWTException: Invalid thread access} — thrown to
 * nowhere on the FX thread, so the click appeared to do nothing.</p>
 *
 * <p>This mirrors that structure: a Label filling the canvas whose {@code onMouseClicked} records the
 * dispatch thread and then does exactly what such a handler does — {@code new Shell(...)}. It
 * synthesises the SWT MouseDown/MouseUp pair that Evolve delivers from a click, then asserts the
 * handler observed the SWT thread and the Shell was created. Prints {@code PASS}/{@code FAIL} and
 * exits non-zero on failure, so it doubles as a runnable regression check on the examples harness
 * (which wires JavaFX + headless Monocle, as the CI unit suite deliberately does not).</p>
 *
 * <pre>
 * ./gradlew :examples:runWebExample \
 *   -PmainClass=dev.equo.javafx.FXCanvasSwtCallbackSnippet \
 *   -Dequo.swt.browser=none -PsessionId=&lt;yours&gt;
 * </pre>
 */
public class FXCanvasSwtCallbackSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("FXCanvasSwtCallbackSnippet");
        shell.setLayout(new FillLayout());

        final FXCanvas canvas = new FXCanvas(shell, SWT.NONE);

        // What the handler saw, filled in when the synthesised click is dispatched.
        final boolean[] handlerRan = {false};
        final boolean[] onSwtThread = {false};
        final boolean[] shellCreated = {false};
        final Throwable[] handlerError = {null};

        final Label link = new Label("Open preferences");
        link.setStyle("-fx-text-fill: #1565c0; -fx-underline: true;");
        link.setPickOnBounds(true);
        link.setOnMouseClicked(e -> {
            handlerRan[0] = true;
            onSwtThread[0] = Thread.currentThread() == display.getThread();
            System.out.println("[probe] HANDLER ENTERED on thread=" + Thread.currentThread().getName()
                    + " onSwtThread=" + onSwtThread[0]);
            try {
                // The operation such a handler reaches (Window.open -> new Shell). Without the fix
                // this throws SWTException: Invalid thread access on the FX thread.
                Shell dialog = new Shell(display, SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
                dialog.setText("Preferences");
                shellCreated[0] = true;
                System.out.println("[probe] created dialog Shell=" + dialog);
                dialog.dispose();
            } catch (Throwable t) {
                handlerError[0] = t;
                System.out.println("[probe] handler FAILED to create Shell: " + t);
            }
        });

        // A Label alone does not fill its parent; a StackPane stretches it so any click inside the
        // canvas lands on the link.
        StackPane root = new StackPane(link);
        root.setStyle("-fx-background-color: white;");
        canvas.setScene(new Scene(root, 400, 200));

        shell.setSize(420, 240);
        shell.open();

        // Give the FX toolkit time to realise the scene (stage activation, first snapshot), then
        // synthesise the click on the SWT thread — exactly the MouseDown/MouseUp pair Evolve fires.
        display.timerExec(1200, () -> {
            System.out.println("[probe] posting synthetic click at canvas centre");
            postMouse(canvas, SWT.MouseDown);
            postMouse(canvas, SWT.MouseUp);

            System.out.println("[probe] handlerRan=" + handlerRan[0]
                    + " onSwtThread=" + onSwtThread[0]
                    + " shellCreated=" + shellCreated[0]
                    + " handlerError=" + handlerError[0]);
            boolean pass = handlerRan[0] && onSwtThread[0] && shellCreated[0] && handlerError[0] == null;
            System.out.println(pass ? "PASS: JavaFX click handler ran on the SWT thread and opened a Shell"
                    : "FAIL: handler did not run on the SWT thread / could not open a Shell");
            shell.dispose();
            if (!pass) {
                display.dispose();
                System.exit(1);
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void postMouse(FXCanvas canvas, int type) {
        Event e = new Event();
        e.type = type;
        e.button = 1;
        e.count = 1;
        e.x = 200; // scene centre — inside the link
        e.y = 100;
        canvas.notifyListeners(type, e);
    }
}
