package org.eclipse.swt.custom;

import com.google.gson.JsonObject;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A text editor's own gutter: JFace's {@link CompositeRuler} with a {@link LineNumberRulerColumn}
 * beside a {@link SourceViewer}, read back in pixels. The column is a Canvas child of the ruler, not
 * a sibling of the StyledText, and draws through a double buffer it scrolls with {@code copyArea}:
 * the structure the ruler's repaint bugs live in, which no imitation reproduces.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LineNumberRulerFlutterTest {

    private static final int WIDTH = 420, HEIGHT = 300;

    /** The browser's display scale. At 2 the column's double buffer is held at twice its size. */
    double deviceScale() {
        return 1;
    }

    private final WebDisplayHarness flutter = new WebDisplayHarness().deviceScale(deviceScale());
    private Display display;
    private Shell shell;
    private Color numberColour;
    private Font font;

    private Composite editor;
    private SourceViewer viewer;
    private StyledText text;
    private Control column;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, WIDTH + 40, HEIGHT + 40);
        shell.open();
        numberColour = new Color(display, 255, 0, 0);
        font = new Font(display, "Courier New", 12, SWT.NORMAL);
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void disposeEditor() {
        if (editor != null) editor.dispose();
        editor = null;
        settle();
    }

    private void settle() {
        int quiet = 0;
        for (int round = 0; round < 40 && quiet < 3; round++) {
            long before = flutter.dispatched();
            flutter.drain();
            flutter.flush();
            flutter.drain();
            quiet = flutter.dispatched() == before ? quiet + 1 : 0;
        }
    }

    /** An editor as Eclipse builds one: an annotation column and the line numbers, beside the text. */
    private void openEditor(String content) {
        editor = new Composite(shell, SWT.NONE);
        editor.setLayout(new FillLayout());
        editor.setBounds(0, 0, WIDTH, HEIGHT);
        CompositeRuler ruler = new CompositeRuler();
        LineNumberRulerColumn numbers = new LineNumberRulerColumn();
        numbers.setFont(font);
        numbers.setForeground(numberColour);
        numbers.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        ruler.addDecorator(0, new AnnotationRulerColumn(12));
        ruler.addDecorator(1, numbers);
        viewer = new SourceViewer(editor, ruler, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        text = viewer.getTextWidget();
        text.setFont(font);
        viewer.setDocument(new Document(content), new AnnotationModel());
        editor.layout(true, true);
        column = numbers.getControl();
    }

    private static String lines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("line ").append(i).append('\n');
        return sb.append("last").toString();
    }

    /** Every third line is long enough to wrap onto more rows in the editor. */
    private static String someLinesWrap(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("line ").append(i);
            if (i % 3 == 1) sb.append(" goes on and on, long enough to wrap onto more than one row of this narrow editor");
            sb.append('\n');
        }
        return sb.append("last").toString();
    }

    /**
     * The vertical extent, in the column's pixels, of each run of rows holding a line number: one
     * {top, bottom} per number drawn.
     */
    private List<int[]> numberRows() throws Exception {
        double[] r = flutter.renderedRect(column);
        JsonObject clip = new JsonObject();
        clip.addProperty("x", r[0]);
        clip.addProperty("y", r[1]);
        clip.addProperty("width", r[2]);
        clip.addProperty("height", r[3]);
        clip.addProperty("scale", 1);
        JsonObject params = new JsonObject();
        params.addProperty("format", "png");
        params.add("clip", clip);
        String data = flutter.devTools().send("Page.captureScreenshot", params).get("data").getAsString();
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data)));
        // The capture is in device pixels; the rows are wanted in the column's.
        double scale = img.getHeight() / r[3];
        List<int[]> rows = new ArrayList<>();
        for (int y = 0; y < img.getHeight(); y++) {
            boolean digit = false;
            for (int x = 0; x < img.getWidth() && !digit; x++) {
                int rgb = img.getRGB(x, y);
                int rr = (rgb >> 16) & 0xff, gg = (rgb >> 8) & 0xff, bb = rgb & 0xff;
                // Red ink, antialiased edges included, on the column's white.
                digit = rr - gg > 50 && rr - bb > 50;
            }
            if (!digit) continue;
            int at = (int) (y / scale);
            int[] last = rows.isEmpty() ? null : rows.get(rows.size() - 1);
            // A glyph's thin strokes can leave a row or two without ink: still the same number.
            if (last != null && at - last[1] <= 3) last[1] = at;
            else rows.add(new int[] { at, at });
        }
        return rows;
    }

    /** The y of the first row of each logical line wholly in view. */
    private List<Integer> visibleLineTops() {
        List<Integer> tops = new ArrayList<>();
        int height = text.getClientArea().height;
        for (int line = text.getTopIndex(); line < text.getLineCount(); line++) {
            int y = text.getLinePixel(line);
            if (y < 0) continue;
            if (y + text.getLineHeight(text.getOffsetAtLine(line)) > height) break;
            tops.add(y);
        }
        return tops;
    }

    /**
     * What is wrong with the numbers in {@code rows}, or null: each logical line wholly in view has
     * one number, drawn on its first row, and nothing else is numbered -- a wrapped line's later
     * rows stay blank.
     */
    private String mismatch(List<int[]> rows) {
        int height = text.getClientArea().height;
        int lineHeight = text.getLineHeight();
        for (int[] row : rows) {
            // Cut by an edge: a line shown in part.
            if (row[0] <= 0 || row[1] >= height - 1) continue;
            int line = text.getLineIndex(row[0]);
            int top = text.getLinePixel(line);
            if (row[1] >= top + lineHeight)
                return "a number at " + row[0] + ".." + row[1] + " is not on the first row of line " + line
                        + ", which starts at " + top;
        }
        for (int top : visibleLineTops()) {
            long numbers = rows.stream().filter(r -> r[0] >= top && r[1] < top + lineHeight).count();
            if (numbers != 1)
                return numbers + " numbers on the first row of the line starting at " + top;
        }
        return null;
    }

    /** Polls a little: the client may still be showing the frame before the last one sent. */
    private void assertNumbersFollowTheLines(String when) throws Exception {
        String wrong = null;
        List<int[]> rows = List.of();
        for (int attempt = 0; attempt < 10; attempt++) {
            settle();
            rows = numberRows();
            wrong = mismatch(rows);
            if (wrong == null) return;
            Thread.sleep(100);
        }
        List<String> drawn = new ArrayList<>();
        for (int[] row : rows) drawn.add(row[0] + ".." + row[1]);
        assertThat(wrong).as("%s: numbers drawn at %s, for lines starting at %s", when, drawn, visibleLineTops())
                .isNull();
    }

    @Test
    @DisplayName("the line numbers are drawn with the editor, with no later event to ask for them")
    void numbersAreDrawnWithTheEditor() throws Exception {
        openEditor(lines(60));
        assertNumbersFollowTheLines("opened");
    }

    @Test
    @DisplayName("turning word wrap on and off renumbers the gutter by logical line")
    void wrapToggleRenumbers() throws Exception {
        openEditor(someLinesWrap(40));
        assertNumbersFollowTheLines("before the toggle");

        text.setWordWrap(true);
        settle();
        assertThat(text.getLinePixel(3)).as("sanity: line 1 wraps").isGreaterThan(3 * text.getLineHeight());
        assertNumbersFollowTheLines("wrap turned on");

        text.setWordWrap(false);
        assertNumbersFollowTheLines("wrap turned off");
    }

    @Test
    @DisplayName("the gutter follows the text through scrolls its buffer copies and scrolls it redraws")
    void numbersFollowAScroll() throws Exception {
        openEditor(lines(400));
        assertNumbersFollowTheLines("opened");
        // Short pixel scrolls: the column copies what it keeps and draws only the uncovered band.
        for (int i = 1; i <= 3; i++) {
            text.setTopPixel(text.getTopPixel() + 7 * i);
            assertNumbersFollowTheLines("scrolled down to pixel " + text.getTopPixel());
        }
        text.setTopPixel(text.getTopPixel() - 11);
        assertNumbersFollowTheLines("scrolled up to pixel " + text.getTopPixel());
        // A jump past the buffer: drawn again whole.
        text.setTopIndex(200);
        assertNumbersFollowTheLines("jumped to line 200");
    }
}
