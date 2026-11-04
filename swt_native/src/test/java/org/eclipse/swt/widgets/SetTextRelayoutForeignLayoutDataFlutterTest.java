package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Native SWT's {@code setText} never lays out the shell, so a control whose layout data does not
 * belong to its parent's layout (a {@code GridData} under a {@code FillLayout}) is harmless until the
 * application itself lays that control out. The relayout Evolve schedules after {@code setText} must
 * not turn that mismatch into an exception: on SWT 3.103 {@code FillLayout.flushCache} casts the data
 * to {@code FillData}, and an application layout may cast the same way.
 */
@Tag("flutter-it")
class SetTextRelayoutForeignLayoutDataFlutterTest {

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

    /** Layout data of {@link StrictLayout}, the only kind it accepts. */
    static final class StrictData {
        void flushCache() {
        }
    }

    /** A layout that, like SWT 3.103's FillLayout, casts its children's data in flushCache. */
    static final class StrictLayout extends Layout {
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            return new Point(100, 100);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            for (Control child : composite.getChildren()) {
                child.setBounds(composite.getClientArea());
            }
        }

        @Override
        protected boolean flushCache(Control control) {
            Object data = control.getLayoutData();
            if (data != null) ((StrictData) data).flushCache();
            return true;
        }
    }

    private Composite parent(Layout layout) {
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.setLayout(new FillLayout());
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(layout);
        shell.layout(true, true);
        return composite;
    }

    private void assertSetTextAndDrainDoNotThrow(Layout layout, Function<Composite, Control> create) {
        Composite composite = parent(layout);
        Control control = create.apply(composite);
        control.setLayoutData(new GridData());
        assertThatCode(() -> {
            setText(control, "a text set after the control was laid out");
            for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
                // drain the async queue as a live event loop would
            }
        }).doesNotThrowAnyException();
    }

    private static void setText(Control control, String text) {
        if (control instanceof Label) ((Label) control).setText(text);
        else if (control instanceof Button) ((Button) control).setText(text);
        else if (control instanceof Link) ((Link) control).setText(text);
        else if (control instanceof CLabel) ((CLabel) control).setText(text);
        else throw new IllegalArgumentException(control.toString());
    }

    @Test
    void labelWithGridDataUnderFillLayout() {
        assertSetTextAndDrainDoNotThrow(new FillLayout(), c -> new Label(c, SWT.NONE));
    }

    @Test
    void labelWithGridDataUnderStrictLayout() {
        assertSetTextAndDrainDoNotThrow(new StrictLayout(), c -> new Label(c, SWT.NONE));
    }

    @Test
    void buttonWithGridDataUnderStrictLayout() {
        assertSetTextAndDrainDoNotThrow(new StrictLayout(), c -> new Button(c, SWT.PUSH));
    }

    @Test
    void linkWithGridDataUnderStrictLayout() {
        assertSetTextAndDrainDoNotThrow(new StrictLayout(), c -> new Link(c, SWT.NONE));
    }

    @Test
    void cLabelWithGridDataUnderStrictLayout() {
        assertSetTextAndDrainDoNotThrow(new StrictLayout(), c -> new CLabel(c, SWT.NONE));
    }
}
