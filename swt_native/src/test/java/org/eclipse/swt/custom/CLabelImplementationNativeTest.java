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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What a CLabel does around the pieces Flutter draws: the shortening hook and the thread it may be built on. */
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
