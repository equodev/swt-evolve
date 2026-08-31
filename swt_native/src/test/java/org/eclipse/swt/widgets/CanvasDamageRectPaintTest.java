package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * {@code redraw(x, y, width, height, all)} names the area the application wants repainted, and the
 * Paint event is where that area is published: applications size their work off
 * {@code event.x/y/width/height}. A control that always reports its whole client area makes every
 * invalidation cost a full repaint.
 *
 * Lives in org.eclipse.swt.widgets so the mocked display's package-private
 * {@code sendEvent(EventTable, Event)} can be wired to really dispatch.
 */
// The damage-rect plumbing is shared main-source code (ControlHelper) and one platform pins it.
// The Linux/Windows embed backends route Canvas construction into real GTK/GDI, which cannot run
// under the mocked headless display.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class CanvasDamageRectPaintTest {

    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();
    private final List<Rectangle> painted = new ArrayList<>();

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
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        asyncQueue.clear();
        painted.clear();
    }

    private Shell shell() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (ev != null && (ev.type == SWT.Paint || ev.type == SWT.Resize)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        return shell;
    }

    private Canvas canvas() {
        Canvas canvas = new Canvas(shell(), SWT.NONE);
        canvas.setBounds(0, 0, 400, 300);
        pumpAsync();
        canvas.addPaintListener(e -> painted.add(new Rectangle(e.x, e.y, e.width, e.height)));
        return canvas;
    }

    private void pumpAsync() {
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
    }

    private Rectangle paintAfter(Runnable invalidation) {
        painted.clear();
        invalidation.run();
        pumpAsync();
        assertThat(painted).as("Paint events delivered").hasSize(1);
        return painted.get(0);
    }

    @Test
    @DisplayName("a partial redraw delivers the invalidated rectangle, not the whole client area")
    void partialRedrawCarriesItsRectangle() {
        Canvas canvas = canvas();

        assertThat(paintAfter(() -> canvas.redraw(50, 40, 100, 60, false)))
                .isEqualTo(new Rectangle(50, 40, 100, 60));
    }

    @Test
    @DisplayName("a full redraw still covers the whole client area")
    void fullRedrawCoversEverything() {
        Canvas canvas = canvas();

        assertThat(paintAfter(canvas::redraw))
                .isEqualTo(new Rectangle(0, 0, 400, 300));
    }

    @Test
    @DisplayName("redraws that coalesce into one paint deliver the union of their rectangles")
    void coalescedRedrawsDeliverTheirUnion() {
        Canvas canvas = canvas();

        assertThat(paintAfter(() -> {
            canvas.redraw(10, 10, 20, 20, false);
            canvas.redraw(100, 200, 50, 50, false);
        })).isEqualTo(new Rectangle(10, 10, 140, 240));
    }

    @Test
    @DisplayName("a partial redraw joined by a full one covers the whole client area")
    void fullRedrawSwallowsAPartialOne() {
        Canvas canvas = canvas();

        assertThat(paintAfter(() -> {
            canvas.redraw(10, 10, 20, 20, false);
            canvas.redraw();
        })).isEqualTo(new Rectangle(0, 0, 400, 300));
    }

    @Test
    @DisplayName("an invalidation reaching past the client area is clamped to it")
    void damageRectIsClampedToTheClientArea() {
        Canvas canvas = canvas();

        assertThat(paintAfter(() -> canvas.redraw(-30, -30, 100, 100, false)))
                .isEqualTo(new Rectangle(0, 0, 70, 70));
    }

    @Test
    @DisplayName("a Composite with a Paint hook gets its rectangle too")
    void compositeCarriesItsRectangle() {
        Composite composite = new Composite(shell(), SWT.NONE);
        composite.setBounds(0, 0, 400, 300);
        pumpAsync();
        composite.addPaintListener(e -> painted.add(new Rectangle(e.x, e.y, e.width, e.height)));

        assertThat(paintAfter(() -> composite.redraw(5, 6, 7, 8, false)))
                .isEqualTo(new Rectangle(5, 6, 7, 8));
    }
}
