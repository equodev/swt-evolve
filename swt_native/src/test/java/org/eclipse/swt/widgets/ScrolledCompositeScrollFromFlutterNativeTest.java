package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.DartScrolledComposite;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Where the render side says it scrolled a ScrolledComposite to, as the selection of one of its scroll bars, is
 * where {@code getOrigin} answers afterwards. It is the user's scroll, not a request of the application, so it
 * is never counted as one: the render side would otherwise be sent back where it already is.
 */
@Tag("native-unit")
class ScrolledCompositeScrollFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;
    private final List<Runnable> queued = new ArrayList<>();

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
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(invocation -> {
            Event posted = invocation.getArgument(0);
            posted.widget.notifyListeners(posted.type, posted);
            return null;
        }).when(impl).postEvent(any(Event.class));
        shell = DartMocks.dartShell(display);
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doNothing().when(impl).postEvent(any(Event.class));
        FlutterBridge.set(null);
    }

    private void scrolledTo(ScrollBar bar, int selection) {
        Event event = new Event();
        event.index = selection;
        bridge.comm.fireContaining("ScrollBar/" + bar.hashCode() + "/Selection/Selection", event);
        new ArrayList<>(queued).forEach(Runnable::run);
        queued.clear();
    }

    @Test
    @DisplayName("a scroll reported on the vertical bar is the origin of the composite")
    void aVerticalScrollIsTheOrigin() {
        ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setSize(200, 100);
        Composite content = new Composite(sc, SWT.NONE);
        content.setSize(300, 400);
        sc.setContent(content);

        scrolledTo(sc.getVerticalBar(), 70);

        assertThat(sc.getVerticalBar().getSelection()).isEqualTo(70);
        assertThat(sc.getOrigin()).isEqualTo(new Point(0, 70));
        assertThat(content.getLocation()).isEqualTo(new Point(0, -70));
    }

    @Test
    @DisplayName("a scroll reported on the horizontal bar is the origin of the composite")
    void aHorizontalScrollIsTheOrigin() {
        ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setSize(200, 100);
        Composite content = new Composite(sc, SWT.NONE);
        content.setSize(300, 400);
        sc.setContent(content);

        scrolledTo(sc.getHorizontalBar(), 40);

        assertThat(sc.getOrigin()).isEqualTo(new Point(40, 0));
    }

    @Test
    @DisplayName("a scroll reported by the render side is not a request to move the view")
    void aUserScrollIsNotARequest() {
        ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setSize(200, 100);
        Composite content = new Composite(sc, SWT.NONE);
        content.setSize(300, 400);
        sc.setContent(content);
        int before = originRequest(sc);

        scrolledTo(sc.getVerticalBar(), 70);
        scrolledTo(sc.getHorizontalBar(), 40);

        assertThat(sc.getOrigin()).isEqualTo(new Point(40, 70));
        assertThat(originRequest(sc)).isEqualTo(before);
    }

    @Test
    @DisplayName("setOrigin after a scroll by the user is a request")
    void setOriginAfterAUserScrollIsARequest() {
        ScrolledComposite sc = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setSize(200, 100);
        Composite content = new Composite(sc, SWT.NONE);
        content.setSize(300, 400);
        sc.setContent(content);
        scrolledTo(sc.getVerticalBar(), 70);
        int before = originRequest(sc);

        sc.setOrigin(0, 20);

        assertThat(originRequest(sc)).isEqualTo(before + 1);
        assertThat(sc.getOrigin()).isEqualTo(new Point(0, 20));
    }

    private static int originRequest(ScrolledComposite sc) {
        return ((DartScrolledComposite) sc.getImpl()).getValue().getOriginRequest();
    }
}
