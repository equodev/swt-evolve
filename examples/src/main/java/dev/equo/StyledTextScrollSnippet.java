package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;

/**
 * Watches a StyledText's top pixel while it is scrolled, and reports every backwards step.
 *
 * <p>A user dragging one way produces a monotonic sequence; a decrease means the view was yanked
 * back to a position that had already been superseded. The client owns the scroll offset and only
 * tells Java about it, so anything Java pushes back mid-scroll is stale by the time it lands.
 *
 * <p>Run it, scroll the text, and read the summary:
 * {@code ./gradlew :examples:runWebExample -PmainClass=dev.equo.StyledTextScrollSnippet}
 */
public class StyledTextScrollSnippet {

    private static final int LINES = 600;
    private static final int WATCH_SECONDS = 90;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("StyledText scroll watch");
        shell.setLayout(new FillLayout());

        StyledText text = new StyledText(shell, SWT.V_SCROLL | SWT.BORDER);
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= LINES; i++) {
            content.append("line ").append(i).append(" ................................\n");
        }
        text.setText(content.toString());

        shell.setSize(900, 600);
        shell.open();

        List<Integer> observed = new ArrayList<>();
        int[] last = { Integer.MIN_VALUE };
        Runnable[] poll = new Runnable[1];
        poll[0] = () -> {
            if (text.isDisposed()) {
                return;
            }
            int top = text.getTopPixel();
            if (top != last[0]) {
                last[0] = top;
                observed.add(top);
            }
            display.timerExec(16, poll[0]);
        };
        display.timerExec(16, poll[0]);

        long stopAt = System.currentTimeMillis() + WATCH_SECONDS * 1000L;
        while (System.currentTimeMillis() < stopAt && !shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        int backwards = 0;
        int worst = 0;
        for (int i = 1; i < observed.size(); i++) {
            int delta = observed.get(i) - observed.get(i - 1);
            if (delta < 0) {
                backwards++;
                worst = Math.min(worst, delta);
            }
        }
        System.out.println("[scroll-watch] samples=" + observed.size()
                + " backwardsSteps=" + backwards
                + " largestBackwardsStep=" + worst + "px");
        System.out.println("[scroll-watch] sequence=" + observed);
        System.out.println("[scroll-watch] done");

        display.dispose();
        System.exit(0);
    }
}
