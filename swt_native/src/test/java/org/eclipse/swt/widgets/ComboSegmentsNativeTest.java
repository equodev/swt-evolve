package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The Segments listeners of a Combo are asked for the segments of every text the combo is given: an item, the
 * text, what a clipboard edit leaves. The offsets they answer have to be ascending and inside that text.
 */
@Tag("native-unit")
class ComboSegmentsNativeTest {

    private Display display;
    private Combo combo;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        clearInvocations(impl);
        combo = new Combo(DartMocks.dartShell(display), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private List<String> listen() {
        List<String> asked = new ArrayList<>();
        combo.addSegmentListener(e -> asked.add(e.lineText));
        return asked;
    }

    @Test
    @DisplayName("an item added, the text set and the items replaced are put to the listeners")
    void itemsAndTextAreAsked() {
        List<String> asked = listen();

        combo.add("one");
        combo.add("zero", 0);
        combo.setText("typed");
        combo.setItems(new String[] {"x", "y"});

        assertThat(asked).containsExactly("one", "zero", "typed", "x", "y");
    }

    @Test
    @DisplayName("what a copy, a cut and a paste leave is put to the listeners")
    void clipboardEditsAreAsked() {
        combo.setText("123456");
        combo.setSelection(new Point(1, 3));
        List<String> asked = listen();

        combo.copy();
        combo.cut();

        assertThat(asked).containsExactly("123456", "1456");
    }

    @Test
    @DisplayName("nothing is asked when nobody listens")
    void nobodyListening() {
        combo.add("one");
        combo.setText("typed");

        assertThat(combo.getItems()).containsExactly("one");
        assertThat(combo.getText()).isEqualTo("typed");
    }

    @Test
    @DisplayName("a listener that answers no segments leaves the text as it is")
    void noSegmentsIsFine() {
        combo.addSegmentListener(e -> e.segments = null);

        combo.setText("typed");

        assertThat(combo.getText()).isEqualTo("typed");
    }

    @Test
    @DisplayName("ascending offsets inside the text are accepted")
    void ascendingOffsetsAreAccepted() {
        combo.addSegmentListener(e -> e.segments = new int[] {0, 2, e.lineText.length()});

        combo.setText("typed");

        assertThat(combo.getText()).isEqualTo("typed");
    }

    @Test
    @DisplayName("offsets that go back are refused")
    void descendingOffsetsAreRefused() {
        combo.addSegmentListener(e -> e.segments = new int[] {3, 1});

        assertThatThrownBy(() -> combo.setText("typed")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("an offset past the end of the text is refused")
    void offsetPastTheEndIsRefused() {
        combo.addSegmentListener(e -> e.segments = new int[] {0, e.lineText.length() + 1});

        assertThatThrownBy(() -> combo.add("abc")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a Segments filter on the display is asked even when the combo has no listener")
    void aDisplayFilterIsAsked() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        when(impl.filters(SWT.Segments)).thenReturn(true);
        try {
            combo.setText("typed");
        } finally {
            when(impl.filters(SWT.Segments)).thenReturn(false);
        }

        assertThat(combo.getText()).isEqualTo("typed");
        verify(impl, atLeastOnce()).sendEvent(org.mockito.ArgumentMatchers.<EventTable>any(), argThat(e -> e.type == SWT.Segments));
    }

    @Test
    @DisplayName("a combo disposed by its own listener is not asked again")
    void disposedByTheListener() {
        List<String> asked = new ArrayList<>();
        combo.addSegmentListener(e -> {
            asked.add(e.lineText);
            combo.dispose();
        });

        combo.setItems(new String[] {"a", "b"});

        assertThat(asked).hasSize(1);
        assertThat(combo.isDisposed()).isTrue();
    }
}
