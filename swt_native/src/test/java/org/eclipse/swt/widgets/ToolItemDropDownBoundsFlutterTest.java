package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Tag("flutter-it")
class ToolItemDropDownBoundsFlutterTest {

    private RecordingBridge bridge;
    private Shell shell;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private ToolItem dropDownItem() {
        shell = DartMocks.dartShell();
        when(shell.getBounds()).thenReturn(new Rectangle(0, 0, 800, 600));
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));

        DartToolBar toolBar = new DartToolBar(shell, SWT.HORIZONTAL | SWT.FLAT, null);
        DartToolItem item = new DartToolItem(toolBar.getApi(), SWT.DROP_DOWN, null);
        return item.getApi();
    }

    private void fireOpenMenuFromFlutter(int x, int y, int width, int height) {
        Event e = new Event();
        e.x = x;
        e.y = y;
        e.width = width;
        e.height = height;
        bridge.comm.fireContaining("/Selection/OpenMenu", e);
    }

    @Test
    @DisplayName("getBounds() returns the Flutter-reported item rect when the ARROW Selection fires")
    void listenerSeesRealBoundsOnArrowSelection() {
        ToolItem item = dropDownItem();

        AtomicReference<Rectangle> boundsInListener = new AtomicReference<>();
        item.addListener(SWT.Selection, e -> boundsInListener.set(item.getBounds()));
        fireOpenMenuFromFlutter(118, 5, 50, 30);

        assertThat(boundsInListener.get())
                .as("the drop-down listener anchors the menu on getBounds(); before the fix it " +
                        "was hardcoded to x=0,y=0 and the menu opened at the toolbar's left edge")
                .isEqualTo(new Rectangle(118, 5, 50, 30));
    }

    @Test
    @DisplayName("the ARROW Selection event carries the item's bottom-left corner, like native SWT")
    void arrowEventHasNativeParityCoordinates() {
        ToolItem item = dropDownItem();

        AtomicReference<Event> received = new AtomicReference<>();
        item.addListener(SWT.Selection, received::set);

        fireOpenMenuFromFlutter(118, 5, 50, 30);

        Event e = received.get();
        assertThat(e).isNotNull();
        assertThat(e.detail).as("drop-down arrow clicks carry SWT.ARROW").isEqualTo(SWT.ARROW);
        assertThat(e.x).as("event.x is the item's left edge in toolbar coordinates").isEqualTo(118);
        assertThat(e.y)
                .as("event.y is the item's bottom edge (SwtToolBar TBN_DROPDOWN parity)")
                .isEqualTo(35);
    }

    @Test
    @DisplayName("an OpenMenu without geometry keeps the Sizes fallback")
    void eventWithoutGeometryFallsBackToSizes() {
        ToolItem item = dropDownItem();

        AtomicReference<Rectangle> boundsInListener = new AtomicReference<>();
        item.addListener(SWT.Selection, e -> boundsInListener.set(item.getBounds()));

        fireOpenMenuFromFlutter(0, 0, 0, 0);

        assertThat(boundsInListener.get())
                .as("no reported geometry: getBounds falls back to the computed size")
                .isNotNull();
        assertThat(boundsInListener.get().x).isZero();
        assertThat(boundsInListener.get().y).isZero();
    }
}
