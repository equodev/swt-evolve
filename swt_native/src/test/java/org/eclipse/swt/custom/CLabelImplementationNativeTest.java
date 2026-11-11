package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What a CLabel does around the pieces Flutter draws: the shortening hook, its size and the thread it may be built on. */
@Tag("native-unit")
public class CLabelImplementationNativeTest extends SerializeTestBase {

    private static GC sevenPixelsPerCharacter() {
        GC gc = mock(GC.class);
        when(gc.textExtent(anyString(), anyInt())).thenAnswer(call -> new Point(call.<String>getArgument(0).length() * 7, 14));
        return gc;
    }

    private static DartCLabel label() {
        return (DartCLabel) new CLabel(Mocks.shell(), SWT.NONE).getImpl();
    }

    @Test
    void shortenText_replacesTheMiddleOfALongTextAndEnds() {
        String text = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJ";
        String shortened = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> label().shortenText(sevenPixelsPerCharacter(), text, 140));
        int kept = shortened.indexOf("...");
        assertThat(kept).isPositive();
        assertThat(shortened).isEqualTo(text.substring(0, kept) + "..." + text.substring(text.length() - kept));
    }

    @Test
    void shortenText_keepsATextThatCannotLoseAnything() {
        DartCLabel label = label();
        assertThat(label.shortenText(sevenPixelsPerCharacter(), null, 100)).isNull();
        assertThat(label.shortenText(sevenPixelsPerCharacter(), "abcdefg", 40)).isEqualTo("abcdefg");
        assertThat(label.shortenText(sevenPixelsPerCharacter(), "abcdefghijklmnop", 21)).isEqualTo("abcdefghijklmnop");
    }

    @Test
    void shortenText_stopsAtAWidthItFillsExactly() {
        assertThat(label().shortenText(sevenPixelsPerCharacter(), "abcdefghij", 35)).isEqualTo("a...j");
    }

    @Test
    void shortenText_keepsTheTextWhenNoHeadAndTailFitBesideTheEllipsis() {
        assertThat(label().shortenText(sevenPixelsPerCharacter(), "abcdefghij", 30)).isEqualTo("abcdefghij");
    }

    /** The size table is measured with the default margin on each side; the label's own margins take its place. */
    @Test
    void computeSize_putsTheLabelsOwnMarginsInPlaceOfTheDefaultOnes() {
        CLabel label = new CLabel(Mocks.shell(), SWT.NONE);
        Point measured = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        label.setMargins(10, 1, 20, 5);

        assertThat(label.computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(measured.x + 10 + 20 - 6, measured.y + 1 + 5 - 6));
        assertThat(label.computeSize(50, 40)).isEqualTo(new Point(50, 40));
    }

    private static final String LONG_TEXT = "&Name of a label wider than this";

    private Mocks.Delivery delivery;

    /** A CLabel on a shell whose display delivers events, as the label's resize needs. */
    private CLabel deliveredLabel() {
        org.eclipse.swt.widgets.Shell shell = Mocks.shell();
        delivery = Mocks.deliverEvents(shell.getDisplay());
        return new CLabel(shell, SWT.NONE);
    }

    @AfterEach
    void stopDelivering() {
        if (delivery != null) delivery.close();
        delivery = null;
    }

    private static String shownToolTip(CLabel label) {
        return ((DartCLabel) label.getImpl())._toolTipText();
    }

    /** A text too wide for the label is shown shortened, and in full as the tooltip the label shows. */
    @Test
    void aShortenedTextShowsInFullAsTheTooltip() {
        CLabel label = deliveredLabel();
        label.setText(LONG_TEXT);
        label.setBounds(0, 0, 40, 20);

        assertThat(shownToolTip(label)).isEqualTo(LONG_TEXT);
        assertThat(label.getToolTipText()).isNull();
    }

    /** A label not laid out yet has no width to shorten its text to, so it decides nothing. */
    @Test
    void aLabelWithNoSizeDecidesNoTooltip() {
        CLabel label = deliveredLabel();

        label.setText(LONG_TEXT);

        assertThat(shownToolTip(label)).isNull();
    }

    /** A label with a width but no height shows no text either, so it decides nothing. */
    @Test
    void aLabelWithNoHeightDecidesNoTooltip() {
        CLabel label = deliveredLabel();
        label.setText(LONG_TEXT);

        label.setBounds(0, 0, 40, 0);

        assertThat(shownToolTip(label)).isNull();
    }

    /** Once the label is wide enough for its text, the full-text tooltip goes. */
    @Test
    void aTextThatFitsAgainShowsNoTooltip() {
        CLabel label = deliveredLabel();
        label.setText(LONG_TEXT);
        label.setBounds(0, 0, 40, 20);

        label.setBounds(0, 0, 2000, 20);

        assertThat(shownToolTip(label)).isNull();
    }

    /** A text set on a label already too narrow for it is shortened too. */
    @Test
    void aLongerTextOnANarrowLabelShowsInFullAsTheTooltip() {
        CLabel label = deliveredLabel();
        label.setBounds(0, 0, 40, 20);
        label.setText("ab");

        label.setText(LONG_TEXT);

        assertThat(shownToolTip(label)).isEqualTo(LONG_TEXT);
    }

    /** Once the application clears its tooltip, a shortened text shows in full again. */
    @Test
    void clearingTheApplicationsTooltipShowsTheFullTextAgain() {
        CLabel label = deliveredLabel();
        label.setToolTipText("app");
        label.setText(LONG_TEXT);
        label.setBounds(0, 0, 40, 20);

        label.setToolTipText(null);

        assertThat(shownToolTip(label)).isEqualTo(LONG_TEXT);
        assertThat(label.getToolTipText()).isNull();
    }

    /** The application's own tooltip is the one shown, shortened text or not. */
    @Test
    void theApplicationsTooltipStaysWhenTheTextIsShortened() {
        CLabel label = deliveredLabel();
        label.setToolTipText("app");
        label.setText(LONG_TEXT);
        label.setBounds(0, 0, 40, 20);

        assertThat(shownToolTip(label)).isEqualTo("app");
        assertThat(label.getToolTipText()).isEqualTo("app");
    }

    /** A host that calls from a thread other than the display's still gets its CLabel. */
    @Test
    void aCLabelCanBeBuiltFromAnotherThread() throws InterruptedException {
        org.eclipse.swt.widgets.Shell shell = Mocks.shell();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread other = new Thread(() -> {
            try {
                new CLabel(shell, SWT.NONE);
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        other.start();
        other.join();
        assertThat(failure.get()).isNull();
    }
}
