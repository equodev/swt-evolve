package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What {@link DartText} does when it is driven directly rather than through {@link Text}. */
@Tag("native-unit")
class TextImplementationNativeTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static DartText impl(int style) {
        return (DartText) new Text(DartMocks.dartShell(), style).getImpl();
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartText impl = new DartText(DartMocks.dartShell(), SWT.SINGLE, null);

        Text api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("a drag cannot start without a selection")
    void dragDetectNeedsASelection() {
        boolean[] consume = {false};

        assertThat(impl(SWT.SINGLE).dragDetect(1, 1, false, consume)).isFalse();
        assertThat(consume[0]).isFalse();
    }

    @Test
    @DisplayName("a drag asks the selection, and answers through the consume flag")
    void dragDetectOverASelectionStoresItsAnswer() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            DartText impl = impl(style);
            impl.setText("selected text");
            impl.setSelection(0, 8);
            boolean[] consume = {false};

            boolean dragging = impl.dragDetect(1, 1, false, consume);

            assertThat(consume[0]).as("style " + style).isEqualTo(dragging);
        }
    }

    @Test
    @DisplayName("Command+Z is swallowed, as the undo of the field is Flutter's")
    void commandZIsSwallowed() {
        DartText impl = impl(SWT.SINGLE);
        Event undo = new Event();
        undo.stateMask = SWT.COMMAND;
        undo.keyCode = 'z';

        assertThat(impl.sendKeyEvent(SWT.KeyDown, undo)).isFalse();
    }

    @Test
    @DisplayName("any other key goes through")
    void otherKeysGoThrough() {
        DartText impl = impl(SWT.SINGLE);
        Event command = new Event();
        command.stateMask = SWT.COMMAND;
        command.keyCode = 'a';
        Event plain = new Event();
        plain.keyCode = 'z';

        assertThat(impl.sendKeyEvent(SWT.KeyDown, command)).isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyDown, plain)).isTrue();
        assertThat(impl.sendKeyEvent(SWT.KeyUp, plain)).isTrue();
    }

    @Test
    @DisplayName("Return does not traverse out of a multi-line text, unless it is read-only")
    void returnTraversalOfAMultiLineText() {
        assertThat(impl(SWT.SINGLE).traversalCode(36, null) & SWT.TRAVERSE_RETURN).isNotZero();
        assertThat(impl(SWT.MULTI).traversalCode(36, null) & SWT.TRAVERSE_RETURN).isZero();
        assertThat(impl(SWT.MULTI | SWT.READ_ONLY).traversalCode(36, null) & SWT.TRAVERSE_RETURN).isNotZero();
    }

    @Test
    @DisplayName("Tab in a multi-line text answers the same codes with or without an event")
    void tabTraversalOfAMultiLineText() {
        DartText impl = impl(SWT.MULTI);

        assertThat(impl.traversalCode(48, new Object())).isEqualTo(impl.traversalCode(48, null));
    }
}
