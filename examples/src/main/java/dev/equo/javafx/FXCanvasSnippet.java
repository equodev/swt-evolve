package dev.equo.javafx;

import dev.equo.swt.Config;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Embeds a live JavaFX {@link Scene} inside an SWT {@link Shell} using
 * {@code javafx.embed.swt.FXCanvas} — the SWT&lt;-&gt;JavaFX bridge.
 *
 * Adapted from the Oracle FXCanvas javadoc example
 * (https://docs.oracle.com/javase/8/javafx/api/javafx/embed/swt/FXCanvas.html):
 * an FXCanvas hosts a JavaFX scene with a Label and a Button. Clicking the
 * button updates the label, proving the embedded scene is live and interactive.
 *
 * Under SWT Evolve the FXCanvas is rendered through Flutter: the JavaFX scene is
 * rendered off-screen and its pixels are painted onto the Evolve Canvas.
 */
public class FXCanvasSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("FXCanvasSnippet");
        shell.setLayout(new FillLayout());

        // The FXCanvas IS an SWT Canvas; it hosts the JavaFX scene.
        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        canvas.setScene(createScene());

        shell.setSize(420, 240);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static Scene createScene() {
        final Label label = new Label("Hello from JavaFX, embedded in SWT Evolve");
        final Button button = new Button("Click me");
        final int[] clicks = {0};
        button.setOnAction(e -> label.setText("Button clicked " + (++clicks[0]) + " time(s)"));

        VBox root = new VBox(16, label, button);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #fafafa, #dfe7ef);");

        return new Scene(root, 420, 240);
    }
}
