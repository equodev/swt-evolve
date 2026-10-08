package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Picking a menu item hides its menus and then selects the item. Natively both happen in one turn of
 * the event loop, so work a {@code SWT.Hide} listener defers with {@code asyncExec} runs after the
 * selection. JFace depends on it: a pull-down action contributes a proxy submenu whose items forward
 * their selection to the real ones, and its Hide listener disposes that proxy in an
 * {@code asyncExec}. The client reports the hides and the selection as separate messages, so the UI
 * thread may run in between; the selection must still reach the item before that cleanup does.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*MenuPickSurvivesHideCleanup*'</pre>
 */
@Tag("flutter-it")
class MenuPickSurvivesHideCleanupFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void anItemPickedFromAProxySubmenuIsSelectedBeforeTheProxyIsDisposed() {
        Menu popup = new Menu(shell, SWT.POP_UP);
        MenuItem cascade = new MenuItem(popup, SWT.CASCADE);
        cascade.setText("Run As");
        Menu proxy = new Menu(popup);
        cascade.setMenu(proxy);
        MenuItem picked = new MenuItem(proxy, SWT.PUSH);
        picked.setText("Run Configurations...");

        List<String> order = new CopyOnWriteArrayList<>();
        picked.addListener(SWT.Selection, e -> order.add("select"));
        popup.addListener(SWT.Hide, e -> order.add("hide popup"));
        proxy.addListener(SWT.Hide, e -> {
            order.add("hide proxy");
            display.asyncExec(() -> {
                order.add("dispose proxy");
                if (!proxy.isDisposed()) proxy.dispose();
            });
        });

        popup.setVisible(true);
        drain();

        clientHid(proxy);
        drain();
        clientHid(popup);
        drain();
        clientSends("MenuItem/" + picked.hashCode() + "/Selection/Selection", new Event());
        drain();

        assertThat(order)
                .as("menus hide first, as natively, and the item is selected before a Hide listener's deferred cleanup")
                .containsExactly("hide proxy", "hide popup", "select", "dispose proxy");
        assertThat(proxy.isDisposed()).isTrue();
    }

    @Test
    void anotherClientEventBetweenTheHidesAndThePickDoesNotLetTheCleanupRunFirst() {
        Menu popup = new Menu(shell, SWT.POP_UP);
        MenuItem cascade = new MenuItem(popup, SWT.CASCADE);
        cascade.setText("Run As");
        Menu proxy = new Menu(popup);
        cascade.setMenu(proxy);
        MenuItem picked = new MenuItem(proxy, SWT.PUSH);
        picked.setText("Run Configurations...");

        List<String> order = new CopyOnWriteArrayList<>();
        picked.addListener(SWT.Selection, e -> order.add("select"));
        proxy.addListener(SWT.Hide, e -> display.asyncExec(() -> {
            order.add("dispose proxy");
            if (!proxy.isDisposed()) proxy.dispose();
        }));

        popup.setVisible(true);
        drain();

        clientHid(proxy);
        drain();
        clientSends("Shell/" + shell.hashCode() + "/MouseMove/MouseMove", new Event());
        drain();
        clientHid(popup);
        drain();
        clientSends("MenuItem/" + picked.hashCode() + "/Selection/Selection", new Event());
        drain();

        assertThat(order)
                .as("a pointer move the client reports while the menus close is not the pick")
                .containsExactly("select", "dispose proxy");
    }

    @Test
    void aMenuDismissedWithoutAPickIsHiddenStraightAway() {
        Menu popup = new Menu(shell, SWT.POP_UP);
        new MenuItem(popup, SWT.PUSH).setText("Copy");
        AtomicInteger hidden = new AtomicInteger();
        popup.addListener(SWT.Hide, e -> hidden.incrementAndGet());

        popup.setVisible(true);
        drain();
        bridge.comm.fireContaining("Menu/" + popup.hashCode() + "/Menu/Hide", new Event());
        drain();

        assertThat(hidden.get()).as("no client event follows a plain dismissal").isEqualTo(1);
        assertThat(popup.isVisible()).isFalse();
    }

    /** The client hides a menu because one of its items was picked. */
    private void clientHid(Menu menu) {
        Event e = new Event();
        e.detail = MenuHelper.HIDDEN_BY_PICK;
        bridge.comm.fireContaining("Menu/" + menu.hashCode() + "/Menu/Hide", e);
    }

    /** Delivered as the transport does: a message for a widget whose handlers are gone is dropped. */
    private void clientSends(String channel, Event e) {
        if (bridge.comm.handlers.keySet().stream().anyMatch(k -> k.contains(channel)))
            bridge.comm.fireContaining(channel, e);
    }

    private void drain() {
        while (display.readAndDispatch()) {
            /* run what the inbound handlers queued */
        }
    }
}
