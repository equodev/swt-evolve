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
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A control's paint scheduling state is its own. Held by the control, it ends with it and is never
 * shared with another control or another session; held in a static collection, it would outlive a
 * disposed control the application still references, and be one table for every session.
 */
@ExtendWith(Mocks.class)
class PaintStateTest {

    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();
    // The display is shared by the whole suite, so its stubs must go inert once this test is done.
    private boolean dispatching;

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
        dispatching = false;
        asyncQueue.clear();
    }

    private Shell shell() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        dispatching = true;
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (dispatching && ev != null && (ev.type == SWT.Paint || ev.type == SWT.Resize)) {
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

    private Composite composite(Shell shell, List<Rectangle> painted) {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setBounds(0, 0, 400, 300);
        composite.addPaintListener(e -> {
            painted.add(new Rectangle(e.x, e.y, e.width, e.height));
            e.gc.fillRectangle(0, 0, 10, 10);
        });
        pumpAsync();
        painted.clear();
        return composite;
    }

    private void pumpAsync() {
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
    }

    @Test
    @DisplayName("no ControlHelper static state reaches a control once it is disposed")
    void noStaticStateReachesADisposedControl() throws IllegalAccessException {
        Composite composite = composite(shell(), new ArrayList<>());
        composite.redraw();
        composite.redraw(5, 5, 20, 20, false);
        pumpAsync();
        // Left pending, so a Paint is still owed when the control goes away.
        composite.redraw(1, 1, 2, 2, false);

        composite.dispose();

        assertThat(staticStateHolding(ControlHelper.class, composite, composite.getImpl()))
                .as("ControlHelper static fields reaching the disposed control").isEmpty();
    }

    @Test
    @DisplayName("one control's scoped damage does not scope another control's Paint")
    void damageStaysWithItsControl() {
        Shell shell = shell();
        List<Rectangle> paintedA = new ArrayList<>();
        List<Rectangle> paintedB = new ArrayList<>();
        Composite a = composite(shell, paintedA);
        Composite b = composite(shell, paintedB);

        a.redraw(5, 5, 20, 20, false);
        b.redraw();
        pumpAsync();

        assertThat(paintedA).containsExactly(new Rectangle(5, 5, 20, 20));
        assertThat(paintedB).containsExactly(new Rectangle(0, 0, 400, 300));
    }

    @Test
    @DisplayName("scoped damage is unioned, and whole-area damage absorbs it")
    void damageMerges() {
        Rectangle bounds = new Rectangle(0, 0, 400, 300);
        PaintState scoped = new PaintState();
        scoped.damage(new Rectangle(5, 5, 10, 10));
        scoped.damage(new Rectangle(30, 30, 10, 10));
        assertThat(scoped.takeDamage(bounds)).isEqualTo(new Rectangle(5, 5, 35, 35));

        PaintState whole = new PaintState();
        whole.damageAll();
        whole.damage(new Rectangle(5, 5, 10, 10));
        assertThat(whole.takeDamage(bounds)).isEqualTo(bounds);
    }

    @Test
    @DisplayName("taking the damage clears it, and damage outside the bounds leaves nothing to paint")
    void takeDamageClears() {
        Rectangle bounds = new Rectangle(0, 0, 400, 300);
        PaintState state = new PaintState();
        state.damage(new Rectangle(500, 500, 10, 10));
        assertThat(state.takeDamage(bounds)).isNull();

        state.damage(new Rectangle(5, 5, 10, 10));
        state.takeDamage(bounds);
        assertThat(state.takeDamage(bounds)).as("nothing owed: a Paint covers the whole area").isEqualTo(bounds);
    }

    private static List<String> staticStateHolding(Class<?> owner, Object... targets) throws IllegalAccessException {
        List<String> holders = new ArrayList<>();
        for (Field f : owner.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            Object value = f.get(null);
            List<Object> reached = new ArrayList<>();
            if (value instanceof Map) {
                reached.addAll(((Map<?, ?>) value).keySet());
                reached.addAll(((Map<?, ?>) value).values());
            } else if (value instanceof Collection) {
                reached.addAll((Collection<?>) value);
            } else {
                reached.add(value);
            }
            for (Object target : targets) {
                if (reached.stream().anyMatch(o -> o == target)) holders.add(f.getName());
            }
        }
        return holders;
    }
}
