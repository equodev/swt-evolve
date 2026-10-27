package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code requestLayout()} must end in the receiver's own {@link Layout} being run once the loop is
 * pumped. A container that arranges its children itself has no other way to be re-tiled: the e4
 * workbench sash is one, and it re-tiles the part stacks from a {@code Layout} it asks for with
 * {@code host.requestLayout()} after each drag step. When that never reaches the layout, the drag
 * is processed in full -- press, moves, new weights -- and nothing moves on screen until some
 * unrelated event happens to drive a layout pass, which is what a divider that only resizes some
 * of the time looks like.
 *
 * <p>Requesting it from inside a dispatched event is the case that matters and the harder one:
 * {@code requestLayout()} raises the shell's {@code layoutCount} and only {@code
 * runDeferredLayouts()} -- at the top of {@code readAndDispatch} -- lowers it again, so a request
 * made while handling an event is served a turn later, if at all. See {@link
 * SetTextRequestsLayoutFlutterTest} for the other half of that hazard.
 *
 * <p>This passes, which is what makes it worth keeping: a workbench sash that writes its new
 * weights and then does not move is a live condition this contract does not explain, so pinning
 * the contract keeps the search pointed at the scheduling state around it rather than at it.
 */
@Tag("flutter-it")
class RequestLayoutRunsLayoutFlutterTest {

    private Display display;

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
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    /** Counts how often it is asked to arrange its composite, the way a sash container would be. */
    private static final class CountingLayout extends Layout {
        int layouts;

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            return new Point(200, 200);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            layouts++;
        }
    }

    private void pump() {
        for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
            // keep pumping until idle, as a live event loop would
        }
    }

    /** A 500x500 shell holding one composite that arranges its own children. */
    private Composite selfArrangingComposite(Shell shell, CountingLayout layout) {
        shell.setSize(500, 500);
        shell.setLayout(new FillLayout());
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(layout);
        new Composite(composite, SWT.NONE);
        return composite;
    }

    @Test
    void requestLayoutRunsTheReceiversOwnLayout() {
        Shell shell = new Shell(display);
        CountingLayout layout = new CountingLayout();
        Composite composite = selfArrangingComposite(shell, layout);
        shell.open();
        pump();

        layout.layouts = 0;
        composite.requestLayout();
        pump();

        assertThat(layout.layouts)
                .as("the composite's own layout after requestLayout() and a pumped loop")
                .isGreaterThan(0);
        assertThat(shell.isLayoutDeferred())
                .as("requestLayout() must not leave the shell layout-suspended")
                .isFalse();
    }

    @Test
    void requestLayoutFromADispatchedEventRunsTheReceiversOwnLayout() {
        Shell shell = new Shell(display);
        CountingLayout layout = new CountingLayout();
        Composite composite = selfArrangingComposite(shell, layout);
        shell.open();
        pump();

        layout.layouts = 0;
        // What SashLayout does: ask for the re-tile from inside an event the loop dispatched.
        display.asyncExec(composite::requestLayout);
        pump();

        assertThat(layout.layouts)
                .as("the composite's own layout after a requestLayout() made while handling an event")
                .isGreaterThan(0);
        assertThat(shell.isLayoutDeferred())
                .as("requestLayout() must not leave the shell layout-suspended")
                .isFalse();
    }
}
