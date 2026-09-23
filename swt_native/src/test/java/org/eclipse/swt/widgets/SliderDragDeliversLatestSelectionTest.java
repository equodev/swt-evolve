package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(Mocks.class)
class SliderDragDeliversLatestSelectionTest {

    private RecordingBridge bridge;
    private final List<Runnable> displayQueue = new ArrayList<>();

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

    private Slider slider() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            displayQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));

        Slider slider = new Slider(shell, SWT.VERTICAL);
        slider.setValues(0, 0, 3014, 414, 1, 10);
        return slider;
    }

    private void dragTo(int selection) {
        Event e = new Event();
        e.index = selection;
        bridge.comm.fireContaining("/Selection/Selection", e);
    }

    private void runDisplayQueue() {
        while (!displayQueue.isEmpty())
            displayQueue.remove(0).run();
    }

    @Test
    @DisplayName("positions sent while the Display thread is busy reach the application as the newest one")
    void aBusyDisplayGetsTheNewestPosition() {
        Slider slider = slider();
        List<Integer> handled = new ArrayList<>();
        slider.addListener(SWT.Selection, ev -> handled.add(slider.getSelection()));

        for (int selection = 10; selection <= 50; selection += 10)
            dragTo(selection);
        runDisplayQueue();

        assertThat(handled)
                .as("every position but the last had already been passed by the pointer when the " +
                        "Display thread got to it")
                .containsExactly(50);
    }

    @Test
    @DisplayName("a position sent after the Display thread caught up is still delivered")
    void aPositionAfterCatchingUpIsDelivered() {
        Slider slider = slider();
        List<Integer> handled = new ArrayList<>();
        slider.addListener(SWT.Selection, ev -> handled.add(slider.getSelection()));

        dragTo(10);
        runDisplayQueue();
        dragTo(20);
        dragTo(30);
        runDisplayQueue();

        assertThat(handled).containsExactly(10, 30);
    }
}
