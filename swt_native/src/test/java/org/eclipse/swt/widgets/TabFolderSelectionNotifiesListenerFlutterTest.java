package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A user clicking a different tab in the Flutter-rendered {@link TabFolder} must fire
 * {@code SWT.Selection} to the application's listeners, exactly as a native tab click does. The
 * Flutter side reports the click as an inbound {@code "Selection"} event carrying the new index; the
 * whole-tree-Flutter {@code DartTabFolder} must then notify its {@code SelectionListener}s, not merely
 * change which tab body is visible.
 *
 * <p>Regression for the reported bug where the tab's own content switched but a <em>dependent</em>
 * area driven by the tab {@code SelectionListener} never updated: EGit's Git &rarr; Label Decorations
 * preference page swaps its "Preview:" area from a {@code TabFolder} selection listener, and with the
 * event suppressed the preview stayed frozen on the first tab's content. The handler routed the
 * Flutter click through {@code setSelection(int)} (a programmatic call, which correctly does
 * <em>not</em> notify) instead of a notifying path, so app listeners never fired.
 *
 * <p>Runs via the {@code nativeTest} task, which compiles against the whole-tree-Flutter Java backend
 * so the real web-mode {@code DartTabFolder} inbound handler runs.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest --tests '*TabFolderSelectionNotifiesListener*'</pre>
 */
@Tag("flutter-it")
class TabFolderSelectionNotifiesListenerFlutterTest {

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge); // makes Display.init() skip the real WebFlutterServer
        display = new Display();
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a Flutter tab click fires SWT.Selection to the application listener")
    void tabClickFromFlutterNotifiesListener() {
        TabFolder folder = new TabFolder(shell, SWT.NONE);
        for (int i = 0; i < 3; i++) {
            TabItem item = new TabItem(folder, SWT.NONE);
            item.setText("Tab " + i);
        }
        // Adding the first item auto-selects index 0 (notify=false), so the listener is added AFTER,
        // observing only genuine user-driven changes.
        AtomicInteger fired = new AtomicInteger();
        AtomicInteger indexAtNotify = new AtomicInteger(-1);
        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fired.incrementAndGet();
                indexAtNotify.set(folder.getSelectionIndex());
            }
        });

        // Flutter reports the user selecting tab index 2. The DartTabFolder inbound handler marshals to
        // the UI thread via asyncExec; drain it so the queued setSelection actually runs.
        Event event = new Event();
        event.index = 2;
        bridge.comm.fireContaining("TabFolder/" + folder.hashCode() + "/Selection/Selection", event);
        while (display.readAndDispatch()) {
            /* run the queued setSelection */
        }

        assertThat(folder.getSelectionIndex())
                .as("Java-side selection tracks the Flutter click (the tab body switches)")
                .isEqualTo(2);
        assertThat(fired.get())
                .as("a user tab click from Flutter must fire SWT.Selection to app listeners — "
                        + "a dependent area driven by the tab listener (EGit Label Decorations preview) "
                        + "otherwise never updates")
                .isEqualTo(1);
        assertThat(indexAtNotify.get())
                .as("the selection is already updated when the listener fires")
                .isEqualTo(2);
    }
}
