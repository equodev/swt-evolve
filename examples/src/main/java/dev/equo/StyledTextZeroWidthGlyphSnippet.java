package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A StyledText whose LineStyleListener hides ANSI escape sequences the way the Eclipse console
 * does: each escape gets a StyleRange with {@code GlyphMetrics(0, 0, 0)}, the text after it gets
 * a colour. The escapes must not be painted and must take no horizontal space, so "INFO" starts
 * at x = 0 and both lines end at the same x. Prints the offsets' locations to compare runs.
 */
public class StyledTextZeroWidthGlyphSnippet {
    static final String ESC = "\u001b";
    static final String LINE1 = ESC + "[32mINFO" + ESC + "[0;39m started";
    static final String LINE2 = "INFO started";
    static final Pattern ANSI = Pattern.compile(ESC + "\\[[0-9;]*m");

    public static void main(String[] args) {
        Config.useEquo(StyledText.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("StyledTextZeroWidthGlyphSnippet");
        shell.setLayout(new FillLayout());
        StyledText text = new StyledText(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setFont(new Font(display, "Courier New", 14, SWT.NORMAL));
        text.setText(LINE1 + "\n" + LINE2);
        text.addLineStyleListener(event -> {
            List<StyleRange> ranges = new ArrayList<>();
            Matcher m = ANSI.matcher(event.lineText);
            while (m.find()) {
                StyleRange hidden = new StyleRange();
                hidden.start = event.lineOffset + m.start();
                hidden.length = m.end() - m.start();
                hidden.metrics = new GlyphMetrics(0, 0, 0);
                ranges.add(hidden);
                if (m.group().equals(ESC + "[32m")) {
                    int end = event.lineText.indexOf(ESC, m.end());
                    StyleRange green = new StyleRange();
                    green.start = event.lineOffset + m.end();
                    green.length = (end < 0 ? event.lineText.length() : end) - m.end();
                    green.foreground = display.getSystemColor(SWT.COLOR_DARK_GREEN);
                    ranges.add(green);
                }
            }
            event.styles = ranges.toArray(new StyleRange[0]);
        });
        shell.setSize(500, 160);
        shell.open();
        display.timerExec(1500, () -> {
            int l2 = LINE1.length() + 1;
            System.out.println("ZW INFO.x=" + text.getLocationAtOffset(LINE1.indexOf("INFO")).x
                    + " line1End.x=" + text.getLocationAtOffset(LINE1.length()).x
                    + " line2End.x=" + text.getLocationAtOffset(l2 + LINE2.length()).x
                    + " line1Bounds=" + text.getTextBounds(0, LINE1.length() - 1)
                    + " line2Bounds=" + text.getTextBounds(l2, l2 + LINE2.length() - 1)
                    + " offsetAt(line1End)=" + text.getOffsetAtPoint(
                            new Point(text.getLocationAtOffset(LINE2.length() + l2).x, 2)));
            if (args.length > 0) {
                Rectangle b = text.getClientArea();
                Image img = new Image(display, b.width, b.height);
                GC gc = new GC(text);
                gc.copyArea(img, 0, 0);
                gc.dispose();
                ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[]{img.getImageData()};
                loader.save(args[0], SWT.IMAGE_PNG);
                System.out.println("ZW saved " + args[0]);
            }
        });
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
