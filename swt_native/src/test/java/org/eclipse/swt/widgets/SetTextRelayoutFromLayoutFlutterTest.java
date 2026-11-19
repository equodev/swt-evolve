package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A layout may set its children's text while it lays them out: a selector that restores its full
 * label and then shortens it to the width it got. Native {@code setText} never lays anything out, so
 * that layout runs once. The relayout Evolve schedules after {@code setText} must not run that layout
 * again for the text the layout itself set, or the two schedule each other forever and the event loop
 * never goes idle.
 */
@Tag("flutter-it")
class SetTextRelayoutFromLayoutFlutterTest {

    private static final String FULL = "Select a model for this conversation";
    private static final String SHORT = "Select a m...";

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

    /** Restores each child's full text, measures it, and shortens it to fit, on every layout. */
    static final class ShorteningLayout extends Layout {
        int layouts;

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            return new Point(100, 20);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            layouts++;
            for (Control child : composite.getChildren()) {
                setText(child, FULL);
                child.setBounds(composite.getClientArea());
                setText(child, SHORT);
            }
        }
    }

    private void assertLayoutRunsAFiniteNumberOfTimes(Function<Composite, Control> create) {
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.setLayout(new FillLayout());
        Composite composite = new Composite(shell, SWT.NONE);
        ShorteningLayout layout = new ShorteningLayout();
        composite.setLayout(layout);
        create.apply(composite);
        shell.layout(true, true);
        assertThat(drain()).as("async runnables dispatched before the event loop went idle").isLessThan(LIMIT);
        layout.layouts = 0;

        composite.layout(true);

        assertThat(drain()).as("async runnables dispatched before the event loop went idle").isLessThan(LIMIT);
        // The layout itself, then one relayout for each text it changed.
        assertThat(layout.layouts).as("times the shortening layout ran").isLessThanOrEqualTo(3);
    }

    private static final int LIMIT = 1000;

    private int drain() {
        int dispatched = 0;
        while (dispatched < LIMIT && display.readAndDispatch()) {
            dispatched++;
        }
        return dispatched;
    }

    private static void setText(Control control, String text) {
        if (control instanceof Label) ((Label) control).setText(text);
        else if (control instanceof Button) ((Button) control).setText(text);
        else if (control instanceof Link) ((Link) control).setText(text);
        else if (control instanceof CLabel) ((CLabel) control).setText(text);
        else throw new IllegalArgumentException(control.toString());
    }

    @Test
    void label() {
        assertLayoutRunsAFiniteNumberOfTimes(c -> new Label(c, SWT.NONE));
    }

    @Test
    void button() {
        assertLayoutRunsAFiniteNumberOfTimes(c -> new Button(c, SWT.PUSH));
    }

    @Test
    void link() {
        assertLayoutRunsAFiniteNumberOfTimes(c -> new Link(c, SWT.NONE));
    }

    @Test
    void cLabel() {
        assertLayoutRunsAFiniteNumberOfTimes(c -> new CLabel(c, SWT.NONE));
    }
}
