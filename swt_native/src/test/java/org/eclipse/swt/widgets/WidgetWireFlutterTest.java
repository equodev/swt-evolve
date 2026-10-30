package org.eclipse.swt.widgets;

import dev.equo.swt.Serializer;
import dev.equo.swt.WireStats;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A property change sent as the binary frame reaches the client's state: each case changes one
 * property of a painted widget, checks the change travelled as binary only, and reads the value
 * back from the client.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest --tests '*WidgetWireFlutterTest'</pre>
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WidgetWireFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
        settle();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void clear() {
        for (Control c : shell.getChildren()) c.dispose();
        settle();
    }

    private void settle() {
        int quiet = 0;
        for (int round = 0; round < 40 && quiet < 3; round++) {
            long before = flutter.dispatched();
            flutter.drain();
            flutter.flush();
            flutter.drain();
            quiet = flutter.dispatched() == before ? quiet + 1 : 0;
        }
    }

    /** {@code change} applied to the painted {@code widget}, sent only as binary; the client's state after. */
    @SuppressWarnings("unchecked")
    private <W extends Control> Map<String, Object> changed(W widget, Consumer<W> change) {
        widget.setBounds(10, 10, 120, 24);
        settle();
        WireStats.start();
        change.accept(widget);
        settle();
        long[] counted = WireStats.stop();
        long frames = counted[3];
        long binary = counted[5];
        assertThat(binary).as("binary frames of %s", java.util.Arrays.toString(counted)).isPositive();
        assertThat(frames).as("every frame binary").isEqualTo(binary);
        return (Map<String, Object>) flutter.queryState(widget).get("state");
    }

    /** A painted shell's GC overlay carries the shell's id; the Display's record must still find the shell. */
    @Test
    void displayRecordAfterThePaintedShellKeepsIt() {
        Shell other = new Shell(display);
        other.setBounds(50, 50, 200, 100);
        other.open();
        settle();
        other.dispose();
        settle();
        assertThat(flutter.queryState(shell)).as("the painted shell, after two Display records").isNotNull();
        assertThat(flutter.queryState(shell).get("state")).isNotNull();
    }

    @Test
    void text() {
        Map<String, Object> state = changed(new Label(shell, SWT.NONE), l -> l.setText("héllo wire"));
        assertThat(state.get("text")).isEqualTo("héllo wire");
    }

    /** Past Latin-1 a string travels as UTF-16 code units, a surrogate pair as two of them. */
    @Test
    void textBeyondLatin1() {
        String text = "日本語 — 🎉";
        Map<String, Object> state = changed(new Label(shell, SWT.NONE), l -> l.setText(text));
        assertThat(state.get("text")).isEqualTo(text);
    }

    @Test
    void selection() {
        Map<String, Object> state = changed(new Button(shell, SWT.CHECK), b -> b.setSelection(true));
        assertThat(state.get("selection")).isEqualTo(true);
    }

    @Test
    void enabled() {
        Map<String, Object> state = changed(new Button(shell, SWT.PUSH), b -> b.setEnabled(false));
        assertThat(state.get("enabled")).isEqualTo(false);
    }

    @Test
    void toolTip() {
        Map<String, Object> state = changed(new Button(shell, SWT.PUSH), b -> b.setToolTipText("tip"));
        assertThat(state.get("toolTipText")).isEqualTo("tip");
    }

    @Test
    void bounds() {
        @SuppressWarnings("unchecked")
        Map<String, Object> state = changed(new Button(shell, SWT.PUSH), b -> b.setBounds(new Rectangle(-3, 7, 90, 30)));
        assertThatWire(state.get("bounds"))
                .containsEntry("x", -3).containsEntry("y", 7).containsEntry("width", 90).containsEntry("height", 30);
    }

    @Test
    void background() {
        Map<String, Object> state = changed(new Label(shell, SWT.NONE),
                l -> l.setBackground(display.getSystemColor(SWT.COLOR_RED)));
        assertThatWire(state.get("background")).containsEntry("r", 255).containsEntry("g", 0);
    }

    @Test
    void font() {
        Font bold = new Font(display, "Arial", 13, SWT.BOLD);
        try {
            Map<String, Object> state = changed(new Label(shell, SWT.NONE), l -> l.setFont(bold));
            List<Map<String, Object>> data = (List<Map<String, Object>>) ((Map<String, Object>) state.get("font")).get("fontData");
            assertThatWire(data.get(0)).containsEntry("height", 13).containsEntry("style", SWT.BOLD);
        } finally {
            bold.dispose();
        }
    }

    @Test
    void intProperty() {
        Map<String, Object> state = changed(progressBar(), p -> p.setSelection(42));
        assertThatWire(state).node("selection").isEqualTo(42);
    }

    private ProgressBar progressBar() {
        ProgressBar bar = new ProgressBar(shell, SWT.HORIZONTAL);
        bar.setMaximum(100);
        return bar;
    }

    /** One name, two types, one frame: a check button's selection is a boolean, a progress bar's an int. */
    @Test
    @SuppressWarnings("unchecked")
    void oneNameOfTwoTypesInOneFrame() {
        Button check = new Button(shell, SWT.CHECK);
        check.setBounds(10, 40, 120, 24);
        Label after = new Label(shell, SWT.NONE);
        after.setBounds(10, 70, 120, 24);
        Map<String, Object> bar = changed(progressBar(), p -> {
            p.setSelection(7);
            check.setSelection(true);
            after.setText("read past both");
        });
        assertThatWire(bar).node("selection").isEqualTo(7);
        assertThat(((Map<String, Object>) flutter.queryState(check).get("state")).get("selection")).isEqualTo(true);
        assertThat(((Map<String, Object>) flutter.queryState(after).get("state")).get("text")).isEqualTo("read past both");
    }

    @Test
    void stringArray() {
        Map<String, Object> state = changed(new Combo(shell, SWT.READ_ONLY), c -> c.setItems("a", "β", ""));
        assertThat((List<Object>) state.get("items")).containsExactly("a", "β", "");
    }
}
