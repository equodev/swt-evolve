package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A Selection arriving from Flutter changes {@code DartButton.selection}, and that new value has to
 * travel back. Flutter flips the box optimistically before it sends, so Java's copy is the only
 * authoritative one; a button that is never marked dirty leaves Flutter holding a guess that the
 * next push of any ancestor silently overwrites — the box then reads unchecked however often it is
 * clicked. RADIO never had the problem: {@code selectRadio()} goes through {@code setSelection()},
 * which dirties.
 */
@Tag("native-unit")
class ButtonSelectionDeliveryNativeTest {

    private CapturingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new CapturingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** The Selection hook hops through asyncExec; run it inline so the click completes here. */
    private Shell shell() {
        Display display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        return DartMocks.dartShell(display);
    }

    private void clickFromFlutter(Button button) {
        bridge.dirtied.clear();
        bridge.comm.fireContaining("Button/" + button.hashCode() + "/Selection/Selection", new Event());
    }

    /**
     * Whether the button's new state is on its way to Flutter. An ancestor counts: a dirty parent is
     * serialized with its whole subtree, and {@code FlutterBridge.update()} drops a child's own frame
     * in that case anyway — which is how RADIO delivers, since {@code selectRadio()} dirties the
     * parent rather than the radio.
     */
    private boolean pushedToFlutter(Control control) {
        for (Control c = control; c != null; c = c.getParent())
            if (bridge.dirtied.contains(c.getImpl()))
                return true;
        return false;
    }

    @Test
    @DisplayName("a CHECK button's new selection is pushed back to Flutter")
    void checkSelectionReachesFlutter() {
        Button check = new Button(shell(), SWT.CHECK);

        clickFromFlutter(check);

        assertThat(check.getSelection()).as("Java's own copy after the click").isTrue();
        assertThat(pushedToFlutter(check))
                .as("Java holds selection=true but never pushes it, so Flutter keeps its optimistic "
                        + "flip until an unrelated push resets the box to unchecked. Dirtied: "
                        + bridge.dirtied.size())
                .isTrue();
    }

    @Test
    @DisplayName("a TOGGLE button's new selection is pushed back to Flutter")
    void toggleSelectionReachesFlutter() {
        Button toggle = new Button(shell(), SWT.TOGGLE);

        clickFromFlutter(toggle);

        assertThat(toggle.getSelection()).as("Java's own copy after the click").isTrue();
        assertThat(pushedToFlutter(toggle))
                .as("same as CHECK: the field is assigned directly, so nothing is ever dirtied")
                .isTrue();
    }

    /**
     * The case that already worked, kept as the control: it shows the probe can see a push when one
     * happens, rather than being blind to all of them.
     */
    @Test
    @DisplayName("a RADIO button's new selection is pushed back to Flutter")
    void radioSelectionReachesFlutter() {
        Button radio = new Button(shell(), SWT.RADIO);

        clickFromFlutter(radio);

        assertThat(radio.getSelection()).as("Java's own copy after the click").isTrue();
        assertThat(pushedToFlutter(radio)).isTrue();
    }

    private static final class CapturingBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();
        final List<Object> dirtied = new CopyOnWriteArrayList<>();

        CapturingBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void dirty(DartWidget widget) {
            dirtied.add(widget);
            super.dirty(widget);
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }
}
