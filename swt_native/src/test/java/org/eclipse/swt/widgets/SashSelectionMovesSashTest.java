package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.graphics.Cursor;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A Selection event arriving from Flutter moves the sash.
 *
 * {@code DartSash} is always {@code SWT.SMOOTH}, and a smooth sash applies its own move when the
 * listeners leave {@code event.doit} true, as native SWT does in {@code mouseDragged} and
 * {@code mouseUp}: an application does not have to reposition it.
 *
 * A {@link RecordingBridge} captures the handler {@code DartSash} registers, so the Selection is
 * fired directly. The mocked Display delivers {@code sendEvent} to the widget's EventTable and runs
 * {@code asyncExec} inline, so the dispatch completes synchronously.
 */
@ExtendWith(Mocks.class)
class SashSelectionMovesSashTest {

    private RecordingBridge bridge;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private Sash sash() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        // Deliver events to the widget's listeners, like the real display does.
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        // The Selection hook hops through asyncExec; run it inline.
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));

        Composite parent = new Composite(shell, SWT.NONE);
        parent.setBounds(0, 0, 390, 260);
        Sash sash = new Sash(parent, SWT.VERTICAL);
        sash.setBounds(195, 0, 10, 260);
        return sash;
    }

    private void fireSelectionFromFlutter(int x, int y, int width, int height) {
        fireSelectionFromFlutter(x, y, width, height, 0);
    }

    /** The release's Selection, as the client marks it: button 1, nothing held. */
    private void fireRelease(int x, int y, int width, int height) {
        Event e = new Event();
        e.button = 1;
        e.x = x;
        e.y = y;
        e.width = width;
        e.height = height;
        bridge.comm.fireContaining("/Selection/Selection", e);
    }

    private void fireSelectionFromFlutter(int x, int y, int width, int height, int stateMask) {
        Event e = new Event();
        e.stateMask = stateMask;
        e.x = x;
        e.y = y;
        e.width = width;
        e.height = height;
        bridge.comm.fireContaining("/Selection/Selection", e);
    }

    @Test
    @DisplayName("a Selection from Flutter moves the sash when no listener vetoes")
    void selectionAppliesTheMove() {
        Sash sash = sash();

        fireSelectionFromFlutter(255, 0, 10, 260);

        assertThat(sash.getBounds().x)
                .as("a SMOOTH sash applies its own move on Selection (SwtSash.mouseDragged/" +
                        "mouseUp parity) — without it the sash never repositions until a window " +
                        "resize forces a layout")
                .isEqualTo(255);
    }

    @Test
    @DisplayName("a sash its Selection listener disposes is not moved")
    void aSashDisposedBySelectionIsNotMoved() {
        Sash sash = sash();
        sash.addListener(SWT.Selection, e -> sash.dispose());

        assertThatCode(() -> fireSelectionFromFlutter(255, 0, 10, 260)).doesNotThrowAnyException();
        assertThat(sash.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a press activates the sash's ancestors, then reports the Selection and moves it")
    void aPressActivatesThenMoves() {
        Sash sash = sash();
        List<String> heard = new ArrayList<>();
        sash.getParent().addListener(SWT.Activate, e -> heard.add("Activate"));
        sash.addListener(SWT.Selection, e -> heard.add("Selection"));
        Event press = new Event();
        press.x = 255;
        press.width = 10;
        press.height = 260;
        press.stateMask = SWT.BUTTON1;

        SashHelper.selected((DartSash) sash.getImpl(), press);

        assertThat(heard).containsExactly("Activate", "Selection");
        assertThat(sash.getBounds().x).isEqualTo(255);
    }

    @Test
    @DisplayName("a press whose activation disposes the sash reports no Selection")
    void aPressWhoseActivationDisposesTheSashGoesNoFurther() {
        Sash sash = sash();
        List<Event> selections = new ArrayList<>();
        sash.addListener(SWT.Selection, selections::add);
        sash.getParent().addListener(SWT.Activate, e -> sash.dispose());
        Event press = new Event();
        press.x = 255;
        press.width = 10;
        press.height = 260;
        press.stateMask = SWT.BUTTON1;

        assertThatCode(() -> SashHelper.selected((DartSash) sash.getImpl(), press)).doesNotThrowAnyException();
        assertThat(sash.isDisposed()).as("the press activated the sash's parent, whose listener disposed it").isTrue();
        assertThat(selections).isEmpty();
    }

    @Test
    @DisplayName("the release reports where the listeners last put the sash, not where the pointer let go")
    void theReleaseReportsTheLastAcceptedPosition() {
        Sash sash = sash();
        List<Integer> reported = new ArrayList<>();
        sash.addListener(SWT.Selection, e -> {
            reported.add(e.x);
            e.x = Math.min(e.x, 240);
        });

        fireSelectionFromFlutter(195, 0, 10, 260, SWT.BUTTON1);
        fireSelectionFromFlutter(230, 0, 10, 260, 0);
        fireSelectionFromFlutter(270, 0, 10, 260, 0);
        fireRelease(270, 0, 10, 260);

        assertThat(reported).containsExactly(195, 230, 270, 240);
        assertThat(sash.getBounds().x).isEqualTo(240);
    }

    @Test
    @DisplayName("the MouseUp of another button is left where the client put it")
    void anotherButtonsMouseUpIsLeftAlone() {
        Sash sash = sash();
        sash.addListener(SWT.Selection, e -> e.x = Math.min(e.x, 240));
        List<Integer> ups = new ArrayList<>();
        sash.addListener(SWT.MouseUp, e -> ups.add(e.x));

        fireSelectionFromFlutter(195, 0, 10, 260, SWT.BUTTON1);
        fireSelectionFromFlutter(270, 0, 10, 260, 0);
        Event up = new Event();
        up.button = 3;
        up.x = 5;
        DragDetectHelper.mouse((DartSash) sash.getImpl(), SWT.MouseUp, up);

        assertThat(ups).containsExactly(5);
    }

    @Test
    @DisplayName("a click whose press a listener vetoes leaves the sash where it is")
    void aVetoedPressLeavesTheSash() {
        Sash sash = sash();
        sash.addListener(SWT.Selection, e -> {
            if ((e.stateMask & SWT.BUTTON1) != 0)
                e.doit = false;
        });

        fireSelectionFromFlutter(195, 0, 10, 260, SWT.BUTTON1);
        fireRelease(195, 0, 10, 260);

        assertThat(sash.getBounds().x).isEqualTo(195);
    }

    @Test
    @DisplayName("a drag move that repeats the last position is a move, not a release")
    void aRepeatedMoveIsNoRelease() {
        Sash sash = sash();
        List<Integer> reported = new ArrayList<>();
        sash.addListener(SWT.Selection, e -> {
            reported.add(e.x);
            e.x = Math.min(e.x, 240);
        });

        fireSelectionFromFlutter(195, 0, 10, 260, SWT.BUTTON1);
        fireSelectionFromFlutter(270, 0, 10, 260, 0);
        fireSelectionFromFlutter(270, 0, 10, 260, 0);

        assertThat(reported).containsExactly(195, 270, 270);
    }

    @Test
    @DisplayName("the release's MouseUp lies on the sash where the listeners kept it, not where the drag took it")
    void theMouseUpIsPlacedOnTheKeptSash() {
        Sash sash = sash();
        sash.addListener(SWT.Selection, e -> e.x = Math.min(e.x, 240));
        List<Integer> ups = new ArrayList<>();
        sash.addListener(SWT.MouseUp, e -> ups.add(e.x));

        fireSelectionFromFlutter(195, 0, 10, 260, SWT.BUTTON1);
        fireSelectionFromFlutter(270, 0, 10, 260, 0);
        fireRelease(270, 0, 10, 260);
        Event up = new Event();
        up.button = 1;
        up.x = 5;
        up.y = 100;
        DragDetectHelper.mouse((DartSash) sash.getImpl(), SWT.MouseUp, up);

        // Pressed 5 into the sash, the pointer let go 30 past where the listener kept it.
        assertThat(ups).containsExactly(35);
    }

    @Test
    @DisplayName("a listener setting doit=false vetoes the move")
    void vetoedSelectionDoesNotMove() {
        Sash sash = sash();
        sash.addListener(SWT.Selection, e -> e.doit = false);

        fireSelectionFromFlutter(255, 0, 10, 260);

        assertThat(sash.getBounds().x)
                .as("event.doit=false must veto the move, like native SWT")
                .isEqualTo(195);
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter reaches an active sash, and not one under a modal shell")
    void defaultSelectionReachesAnActiveSash() {
        Sash sash = sash();
        List<Event> heard = new ArrayList<>();
        sash.addListener(SWT.DefaultSelection, heard::add);

        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        assertThat(heard).hasSize(1);

        Shell modal = Mocks.shell();
        when(((DartShell) sash.getShell().getImpl()).getModalShell()).thenReturn(modal);
        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        assertThat(heard).hasSize(1);
    }

    @Test
    @DisplayName("what Flutter sends for a sash disposed before it arrives is dropped")
    void eventsForADisposedSashAreDropped() {
        Sash sash = sash();
        List<Runnable> queued = new ArrayList<>();
        doAnswer(inv -> queued.add(inv.getArgument(0))).when(sash.getDisplay()).asyncExec(any(Runnable.class));
        List<Event> heard = new ArrayList<>();
        sash.addListener(SWT.Selection, heard::add);
        sash.addListener(SWT.DefaultSelection, heard::add);

        fireSelectionFromFlutter(255, 0, 10, 260);
        bridge.comm.fireContaining("/Selection/DefaultSelection", new Event());
        sash.dispose();
        queued.forEach(Runnable::run);

        assertThat(heard).isEmpty();
    }

    @Test
    @DisplayName("a sash its Selection listener disposes is not moved")
    void aSashItsListenerDisposesIsNotMoved() {
        Sash sash = sash();
        sash.addListener(SWT.Selection, e -> sash.dispose());

        assertThatCode(() -> fireSelectionFromFlutter(255, 0, 10, 260)).doesNotThrowAnyException();
        assertThat(sash.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a sash with no cursor of its own shows the resize cursor of its orientation")
    void aSashShowsTheResizeCursorOfItsOrientation() {
        Sash vertical = sash();
        Display display = vertical.getDisplay();
        Cursor sizeWE = mock(Cursor.class);
        Cursor sizeNS = mock(Cursor.class);
        Cursor own = mock(Cursor.class);
        when(display.getSystemCursor(SWT.CURSOR_SIZEWE)).thenReturn(sizeWE);
        when(display.getSystemCursor(SWT.CURSOR_SIZENS)).thenReturn(sizeNS);
        Sash horizontal = new Sash(vertical.getParent(), SWT.HORIZONTAL);

        assertThat(((DartSash) vertical.getImpl()).findCursor()).isSameAs(sizeWE);
        assertThat(((DartSash) horizontal.getImpl()).findCursor()).isSameAs(sizeNS);
        vertical.setCursor(own);
        assertThat(((DartSash) vertical.getImpl()).findCursor()).isSameAs(own);
    }
}
