package dev.equo.swing;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * The oracle: the same views, the same tools and the same split, in <b>plain Swing</b> — no SWT,
 * no engine, nothing of ours between the application and the screen.
 *
 * <p>It exists because a defect reproduced in {@link SwingIslandWorkbenchSnippet} is only ours if
 * it does not happen here, and three readings have turned on that distinction. The gesture it was
 * built for: with a line preview live, drag the split's divider a little. Under the engine that
 * leaves exactly one stale line, pointing at where the gesture started, renewed by every further
 * drag of the divider and gone only once the line is committed.
 *
 * <p><b>What it would mean either way.</b> A relayout repaints the canvas, which wipes the
 * preview's pixels; the application never hears about it and sends its XOR erase anyway, and an
 * XOR erase over clean pixels <em>draws</em> the line rather than removing it. If that is what
 * happens, it happens here too and it is the idiom's, not the engine's — and the fix belongs in
 * the tool, which should draw its preview from {@code paintComponent} rather than behind Swing's
 * back.
 *
 * <pre>./gradlew :examples:runPlainSwing</pre>
 */
public class PlainSwingWorkbenchSnippet {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapLikeView map = new MapLikeView("Map 1", 0);
            JLabel status = new JLabel(" ");
            status.setOpaque(true);
            map.statusGoesTo(text -> {
                status.setText(text);
                status.repaint(); // the label only — never the canvas, as in the island snippet
            });

            JPanel left = new JPanel(new BorderLayout());
            left.add(map, BorderLayout.CENTER);
            left.add(status, BorderLayout.SOUTH);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left,
                    new SectionLikeView("Section 2"));
            split.setDividerLocation(600);
            split.setContinuousLayout(true);

            JFrame frame = new JFrame("PlainSwingWorkbenchSnippet — the oracle");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(split);
            frame.setSize(1100, 700);
            frame.setVisible(true);
            System.out.println("[oracle] plain Swing, no SWT and no engine. Start a line preview "
                    + "and drag the divider.");
        });
    }
}
