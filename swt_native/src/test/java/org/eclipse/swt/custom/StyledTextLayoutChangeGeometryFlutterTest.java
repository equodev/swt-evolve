package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WireTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.junit.jupiter.api.*;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A tab width or font change on an editor whose rows all have one height: what the client sends back
 * for it does not grow with the document, since every row outside the band around the view follows
 * from the row height and the line starts, and the editor still answers as one handed the document
 * with the new layout.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextLayoutChangeGeometryFlutterTest {

    private static final int SHORT = 2000, LONG = 8000;

    private final StyledTextFlutterStage stage = new StyledTextFlutterStage();
    private Font larger;

    @BeforeAll
    void boot() {
        stage.boot();
        FontData data = stage.subject.getFont().getFontData()[0];
        larger = new Font(stage.display, data.getName(), data.getHeight() + 1, SWT.NORMAL);
    }

    @AfterAll
    void shutdown() {
        larger.dispose();
        stage.shutdown();
    }

    /** Tabbed lines, every one as tall as the others: {@code Line j<TAB>value<TAB>j}. */
    private static String tabbed(int lines) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            if (i > 0) out.append('\n');
            out.append("Line ").append(i).append("\tvalue\t").append(i);
        }
        return out.toString();
    }

    @Test
    @DisplayName("a wider tab sends geometry that does not grow with the document")
    void tabWidth() {
        assertBoundedAndFresh("setTabs(8)", text -> text.setTabs(8));
    }

    @Test
    @DisplayName("a larger font sends geometry that does not grow with the document")
    void font() {
        assertBoundedAndFresh("setFont(+1pt)", text -> text.setFont(larger));
    }

    private void assertBoundedAndFresh(String change, Consumer<StyledText> apply) {
        long shortBytes = bytesSentBack(SHORT, apply);
        long longBytes = bytesSentBack(LONG, apply);
        assertThat(longBytes)
                .as("%s: geometry bytes at %d lines (%d at %d lines)", change, LONG, shortBytes, SHORT)
                .isLessThan(shortBytes * 3 / 2);
    }

    /** The bytes the client sends on the geometry channel for {@code apply} on a document of {@code lines}. */
    private long bytesSentBack(int lines, Consumer<StyledText> apply) {
        String document = tabbed(lines);
        stage.fresh(SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, document);
        StyledText text = stage.subject;
        DartStyledText impl = (DartStyledText) text.getImpl();
        long[] received = new long[1];
        FlutterBridge.onPayload(impl, WireTable.CHANNEL_TEXT_GEOMETRY, payload -> {
            received[0] += payload.length;
            StyledTextHelper.GeometryPush push = StyledTextHelper.readPush(payload);
            impl.getDisplay().asyncExec(() -> StyledTextHelper.applyTextGeometry(impl, push));
        });
        stage.settle();
        received[0] = 0;
        apply.accept(text);
        stage.settle();
        long bytes = received[0];
        answersAsAFreshEditor(text, document, lines / 2);
        return bytes;
    }

    private void answersAsAFreshEditor(StyledText text, String document, int far) {
        StyledText fresh = new StyledText(stage.shell, text.getStyle() & (SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL));
        try {
            fresh.setBounds(StyledTextFlutterStage.WIDTH + 20, 0, text.getSize().x, text.getSize().y);
            fresh.setFont(text.getFont());
            fresh.setTabs(text.getTabs());
            fresh.setText(document);
            stage.settle();
            for (int line : new int[] { 0, far, text.getLineCount() - 1 }) {
                int end = text.getOffsetAtLine(line) + text.getLine(line).length();
                assertThat(text.getLinePixel(line)).as("the pixel of line %d", line).isEqualTo(fresh.getLinePixel(line));
                assertThat(text.getLocationAtOffset(end)).as("the end of line %d", line)
                        .isEqualTo(fresh.getLocationAtOffset(end));
            }
            assertThat(text.getVerticalBar().getMaximum()).as("the vertical range")
                    .isEqualTo(fresh.getVerticalBar().getMaximum());
        } finally {
            fresh.dispose();
            stage.settle();
        }
    }
}
