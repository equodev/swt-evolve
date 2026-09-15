package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * When a widget travels as a name rather than as a description.
 *
 * <p>A payload carries every widget beneath the one it is about, and most of them have not changed:
 * adding one child to a composite of five hundred re-described all five hundred. A widget the far
 * side already holds, and that has not changed, is written as identity only - which widget it is,
 * and nothing about what it holds.
 *
 * <p>The rule is about the frame, not the widget: the same Label is a full description when the
 * frame is about it, and a reference when it appears inside its parent's.
 */
@ExtendWith(Mocks.class)
class WidgetReferenceTest {

    private RecordingBridge bridge;
    private final Serializer serializer = new Serializer();

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    /**
     * The client these frames are written for, and the one {@link RecordingComm} answers with.
     * Whether a widget can be named rather than described is a question about one client, so a test
     * that writes a frame outside a send has to say which - a walk addressed to nobody describes
     * everything, which is the safe answer and not the one under test here.
     */
    private static final int CLIENT = 1;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Serializer.enterConnection(CLIENT);
        assumeTrue(Serializer.diffEnabled, "references are part of partial delivery");
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a delivered, unchanged child is named rather than described")
    void unchangedChildIsAReference() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label child = new Label(parent, SWT.NONE);
        child.setText("established");
        deliver(parent);

        String payload = write(parent);

        assertThat(sliceFor(payload, child))
                .as("the child is on the far side already and has not moved since")
                .contains("\"_r\":1")
                .doesNotContain("\"text\"");
    }

    @Test
    @DisplayName("a reference carries no write stamp, so it cannot displace what is held")
    void aReferenceCarriesNoStamp() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label child = new Label(parent, SWT.NONE);
        deliver(parent);

        assertThat(sliceFor(write(parent), child))
                .as("a stamp would make an empty shell look newer than the real state behind it")
                .doesNotContain("\"_s\"");
    }

    @Test
    @DisplayName("a child that changed is described in full")
    void changedChildIsWrittenWhole() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label child = new Label(parent, SWT.NONE);
        child.setText("before");
        deliver(parent);

        child.setText("after");

        assertThat(sliceFor(write(parent), child))
                .as("a reference would be the only mention of this change, and it says nothing")
                .contains("\"after\"")
                .doesNotContain("\"_r\"");
    }

    @Test
    @DisplayName("a child the far side has never seen is described in full")
    void undeliveredChildIsWrittenWhole() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        deliver(parent);

        Label added = new Label(parent, SWT.NONE);
        added.setText("added");

        assertThat(sliceFor(write(parent), added))
                .as("there is nothing on the far side for a reference to resolve to")
                .contains("\"added\"")
                .doesNotContain("\"_r\"");
    }

    @Test
    @DisplayName("the widget a frame is about is always described in full")
    void theSubjectOfAFrameIsNeverAReference() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("established");
        deliver(parent);

        String payload = write(label);

        assertThat(payload)
                .as("a frame whose subject is a reference says nothing at all")
                .doesNotContain("\"_r\"")
                .contains("\"established\"");
    }

    @Test
    @DisplayName("naming the unchanged is what makes a structural change affordable")
    void addingAChildDoesNotRedescribeItsSiblings() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        for (int i = 0; i < 50; i++) new Label(parent, SWT.NONE).setText("row " + i);
        deliver(parent);

        Label added = new Label(parent, SWT.NONE);
        added.setText("the one that changed");
        String payload = write(parent);

        assertThat(payload).contains("the one that changed");
        assertThat(countOf(payload, "\"_r\":1"))
                .as("every sibling named, none re-described")
                .isEqualTo(50);
        assertThat(countOf(payload, "\"row "))
                .as("the text of a sibling is state, and no sibling's state changed")
                .isZero();
    }

    // ---- harness ----

    /** Puts the whole subtree on the wire, so later writes have something to refer back to. */
    private void deliver(Widget widget) {
        // A widget the client has never seen is not sent on its own channel - it is expected to
        // arrive inside its parent - so the tree is declared known before the flush that stamps it.
        markKnown(widget);
        bridge.dirty((DartWidget) widget.getImpl());
        FlutterBridge.update();
        bridge.comm.sent.clear();
    }

    private void markKnown(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (Widget child : composite.getChildren()) markKnown(child);
    }

    /** The bytes for [widget], read without marking anything delivered. */
    private String write(Widget widget) {
        try {
            String json = new String(serializer.to(widget), StandardCharsets.UTF_8);
            Serializer.discardWritten();
            return json;
        } catch (java.io.IOException e) {
            throw new AssertionError("could not write " + widget, e);
        }
    }

    /** The part of a payload describing one widget: from its id up to the next one's. */
    private String sliceFor(String payload, Widget widget) {
        String marker = "\"id\":" + widget.hashCode();
        int start = payload.indexOf(marker);
        assertThat(start).as("%s is not in the payload at all", widget).isNotNegative();
        int next = payload.indexOf("\"id\":", start + marker.length());
        return payload.substring(start, next < 0 ? payload.length() : next);
    }

    private int countOf(String haystack, String needle) {
        int count = 0;
        for (int at = haystack.indexOf(needle); at >= 0; at = haystack.indexOf(needle, at + 1)) count++;
        return count;
    }
}
