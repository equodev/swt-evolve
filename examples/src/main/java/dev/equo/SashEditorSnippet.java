package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A sash with a document-sized editor beside it — the shape that makes dragging the sash slow only
 * when an editor is open. Every drag step relays out the SashForm, and a container's serialized
 * value embeds its whole subtree, so the editor is re-serialized for a drag that never touched it.
 *
 * <p>Drag the sash and read the wire cost from the trace:
 * {@code ./gradlew :examples:runWebExample -PmainClass=dev.equo.SashEditorSnippet -Pdebug
 * -Dequo.swt.browser=none}
 */
public class SashEditorSnippet {

    private static final int LINES = 600;
    // Long enough to drive a drag and a reconnect; SASH_ALIVE_SECONDS extends it (an env var, not a
    // system property: the example tasks fork a JVM and do not forward -D).
    private static final int ALIVE_SECONDS =
            Integer.parseInt(System.getenv().getOrDefault("SASH_ALIVE_SECONDS", "90"));

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Sash beside an editor");
        shell.setLayout(new FillLayout());

        SashForm sash = new SashForm(shell, SWT.HORIZONTAL);

        Tree tree = new Tree(sash, SWT.BORDER);
        for (int i = 0; i < 20; i++) {
            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setText("package fragment " + i);
            for (int j = 0; j < 3; j++) {
                new TreeItem(item, SWT.NONE).setText("Type" + i + "_" + j + ".java");
            }
        }

        StyledText text = new StyledText(sash, SWT.V_SCROLL | SWT.BORDER);
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= LINES; i++) {
            content.append("line ").append(i).append(" ................................\n");
        }
        text.setText(content.toString());

        sash.setWeights(new int[] { 30, 70 });
        shell.setSize(1000, 700);
        shell.open();

        long stopAt = System.currentTimeMillis() + ALIVE_SECONDS * 1000L;
        while (System.currentTimeMillis() < stopAt && !shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        System.out.println("[sash-editor] done");
        display.dispose();
        System.exit(0);
    }
}
