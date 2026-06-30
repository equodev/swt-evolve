package dev.equo.javafx;

import dev.equo.swt.Config;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import javafx.embed.swt.FXCanvas;

/**
 * Mixes <em>real SWT widgets and a JavaFX scene in the same Shell</em>, laid out
 * with an SWT {@link GridLayout}. Unlike {@code FXCanvasSnippet} /
 * {@code FXCanvasDashboardSnippet} (whose Shell contains nothing but a single
 * full-bleed {@link FXCanvas}), here SWT {@link org.eclipse.swt.widgets.Label
 * Label}, {@link org.eclipse.swt.widgets.Text Text}, {@link
 * org.eclipse.swt.widgets.Button Button} and {@link
 * org.eclipse.swt.widgets.List List} controls live side-by-side with the
 * embedded JavaFX scene and the SWT layout manager positions all of them.
 *
 * <p>The two toolkits talk to each other, both directions, proving the merge is
 * live — not just two panes painted next to each other:</p>
 * <ul>
 *   <li><b>SWT → JavaFX.</b> Typing in the SWT {@code Text} and pressing the SWT
 *       "Send to JavaFX →" button pushes the message onto the JavaFX thread
 *       ({@link Platform#runLater}) where it updates the JavaFX {@code Label}.</li>
 *   <li><b>JavaFX → SWT.</b> Clicking the JavaFX "← Reply to SWT" button posts
 *       back onto the SWT thread ({@link Display#asyncExec}) where it appends a
 *       row to the SWT {@code List}.</li>
 * </ul>
 *
 * <p>Each side updates only its own widgets, and only on its own UI thread — the
 * standard rule when bridging SWT and JavaFX.</p>
 */
public class FXCanvasMixedSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("FXCanvasMixedSnippet");

        // SWT layout drives the whole window: 2 columns, the FXCanvas spans both.
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 12;
        layout.marginHeight = 12;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 8;
        shell.setLayout(layout);

        // --- Row 0: SWT header spanning both columns ----------------------
        org.eclipse.swt.widgets.Label header =
                new org.eclipse.swt.widgets.Label(shell, SWT.NONE);
        header.setText("SWT widgets + an embedded JavaFX scene, arranged by SWT GridLayout");
        header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        // --- Row 1: SWT input (label + text) ------------------------------
        org.eclipse.swt.widgets.Label prompt =
                new org.eclipse.swt.widgets.Label(shell, SWT.NONE);
        prompt.setText("Message:");
        prompt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        final org.eclipse.swt.widgets.Text input =
                new org.eclipse.swt.widgets.Text(shell, SWT.BORDER | SWT.SINGLE);
        input.setText("Hello JavaFX, from SWT");
        input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // --- Row 2: the JavaFX scene, spanning both columns ---------------
        // Holders so the SWT side can reach the JavaFX label, and the JavaFX
        // side can reach the SWT List, without leaking either toolkit's threads.
        final Label[] fxLabel = new Label[1];
        final org.eclipse.swt.widgets.List[] log = new org.eclipse.swt.widgets.List[1];

        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        GridData canvasData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        canvasData.heightHint = 220;
        canvas.setLayoutData(canvasData);
        canvas.setScene(createScene(display, fxLabel, log));

        // --- Row 3: SWT button + SWT List (the JavaFX replies) ------------
        org.eclipse.swt.widgets.Button send =
                new org.eclipse.swt.widgets.Button(shell, SWT.PUSH);
        send.setText("Send to JavaFX →");
        send.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        final org.eclipse.swt.widgets.List replies =
                new org.eclipse.swt.widgets.List(shell, SWT.BORDER | SWT.V_SCROLL);
        replies.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        log[0] = replies;

        // SWT button → JavaFX: hop to the FX thread to mutate the scene, then ask
        // the canvas to re-render (it only auto-snapshots on its own input events,
        // so a change pushed from SWT needs an explicit refresh to appear).
        send.addListener(SWT.Selection, e -> {
            final String text = input.getText().trim();
            Platform.runLater(() -> {
                if (fxLabel[0] != null) {
                    fxLabel[0].setText(text.isEmpty() ? "(empty message from SWT)" : text);
                }
                canvas.refresh();
            });
        });

        shell.setSize(560, 460);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /**
     * Builds the embedded JavaFX scene. Captures the JavaFX label (so the SWT
     * button can update it) and replies back to SWT through {@code display}.
     */
    private static Scene createScene(final Display display,
                                     final Label[] fxLabel,
                                     final org.eclipse.swt.widgets.List[] log) {
        Label title = new Label("Embedded JavaFX scene");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f3b57;");

        Label received = new Label("Waiting for a message from SWT…");
        received.setStyle("-fx-font-size: 15px; -fx-text-fill: #2b3a4a;");
        received.setWrapText(true);
        fxLabel[0] = received;

        final int[] replyCount = {0};
        Button reply = new Button("← Reply to SWT");
        reply.setOnAction(e -> {
            final int n = ++replyCount[0];
            // JavaFX button → SWT (always hop to the SWT thread).
            display.asyncExec(() -> {
                org.eclipse.swt.widgets.List list = log[0];
                if (list != null && !list.isDisposed()) {
                    list.add("Reply #" + n + " from JavaFX: \"" + received.getText() + "\"");
                    list.select(list.getItemCount() - 1);
                }
            });
        });

        VBox root = new VBox(16, title, received, reply);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #fafafa, #dfe7ef);");

        return new Scene(root, 520, 220);
    }
}
