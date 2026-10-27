package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.WidgetSpy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A widget tracker hears each widget made and each widget disposed, once. */
@Tag("native-unit")
class WidgetCreationTrackerTest {

    private Shell shell;
    private final List<String> heard = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
        WidgetSpy.getInstance().setWidgetTracker(new WidgetSpy.WidgetTracker() {
            @Override
            public void widgetCreated(Widget widget) {
                heard.add("created " + widget.getClass().getSimpleName());
            }

            @Override
            public void widgetDisposed(Widget widget) {
                heard.add("disposed " + widget.getClass().getSimpleName());
            }
        });
    }

    @AfterEach
    void tearDown() {
        WidgetSpy.getInstance().setWidgetTracker(null);
        FlutterBridge.set(null);
    }

    @Test
    void eachWidgetIsTrackedOnce() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Button button = new Button(shell, SWT.PUSH);
        canvas.dispose();
        button.dispose();
        assertThat(heard).containsExactly("created Canvas", "created Button", "disposed Canvas", "disposed Button");
    }

    @Test
    void nothingIsTrackedOnceTheTrackerIsRemoved() {
        WidgetSpy.getInstance().setWidgetTracker(null);
        new Canvas(shell, SWT.NONE).dispose();
        assertThat(heard).isEmpty();
    }
}
