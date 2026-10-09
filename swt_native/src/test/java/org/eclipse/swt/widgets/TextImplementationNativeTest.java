package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/** What {@link DartText} does when it is driven directly rather than through {@link Text}. */
@Tag("native-unit")
class TextImplementationNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        // The display mock is shared by the whole suite: hand it back as the other tests expect it.
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private DartText impl(int style) {
        return (DartText) new Text(DartMocks.dartShell(display), style).getImpl();
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

    @Test
    @DisplayName("the accessors of the implementation answer what the API answers")
    void accessorsAnswerTheState() {
        Text text = new Text(DartMocks.dartShell(display), SWT.SINGLE);
        DartText impl = (DartText) text.getImpl();
        text.setText("accessors");
        text.setTextLimit(40);
        text.setTabs(4);
        text.setDoubleClickEnabled(false);
        text.setMessage("hint");
        text.setSelection(2, 5);
        text.setEditable(false);
        text.setEchoChar('*');

        assertThat(impl._textLimit()).isEqualTo(40);
        assertThat(impl._tabs()).isEqualTo(4);
        assertThat(impl._echoCharacter()).isEqualTo('*');
        assertThat(impl._doubleClick()).isFalse();
        assertThat(impl._receivingFocus()).isFalse();
        assertThat(impl._hiddenText()).containsExactly("accessors".toCharArray());
        assertThat(impl._message()).isEqualTo("hint");
        assertThat(impl._actionSearch()).isZero();
        assertThat(impl._actionCancel()).isZero();
        assertThat(impl._caretPosition()).isEqualTo(2);
        assertThat(impl._editable()).isFalse();
        assertThat(impl._selection()).isEqualTo(new Point(2, 5));
        assertThat(impl._text()).isEqualTo("accessors");
        assertThat(impl._textChars()).isEmpty();
        assertThat(impl._topIndex()).isZero();
    }

    @Test
    @DisplayName("the offsets of the segment-less text are the offsets of the text")
    void offsetsAreNotTranslated() {
        DartText impl = impl(SWT.SINGLE);

        assertThat(impl.translateOffset(7)).isEqualTo(7);
        assertThat(impl.untranslateOffset(7)).isEqualTo(7);
    }

    @Test
    @DisplayName("the native event view of a text is itself, whatever the style")
    void everyViewIsAnEventView() {
        assertThat(impl(SWT.SINGLE).isEventView(1)).isTrue();
        assertThat(impl(SWT.MULTI).isEventView(1)).isTrue();
    }

    @Test
    @DisplayName("a scroll bar of a text is not needed until the text tells it is")
    void scrollBarsAreNotNeeded() {
        Text text = new Text(DartMocks.dartShell(display), SWT.MULTI | SWT.V_SCROLL);
        DartText impl = (DartText) text.getImpl();

        assertThat(impl.isNeeded(text.getVerticalBar())).isFalse();
        assertThat(((DartText) new Text(DartMocks.dartShell(display), SWT.SINGLE).getImpl()).isNeeded(text.getVerticalBar())).isFalse();
    }

    @Test
    @DisplayName("the search and cancel icons post a DefaultSelection with their detail")
    void iconsPostADefaultSelection() {
        Text text = new Text(DartMocks.dartShell(display), SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
        DartText impl = (DartText) text.getImpl();
        text.addListener(SWT.DefaultSelection, e -> { });
        List<Integer> details = new ArrayList<>();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(invocation -> details.add(((Event) invocation.getArgument(0)).detail)).when(displayImpl).postEvent(any(Event.class));
        try {
            impl.sendSearchSelection();
            impl.sendCancelSelection();
        } finally {
            doNothing().when(displayImpl).postEvent(any(Event.class));
        }

        assertThat(details).containsExactly(SWT.ICON_SEARCH, SWT.ICON_CANCEL);
    }

    @Test
    @DisplayName("a foreground given as a colour array is not the text's to keep")
    void foregroundArrayKeepsTheForeground() {
        for (int style : new int[] {SWT.SINGLE, SWT.MULTI}) {
            Text text = new Text(DartMocks.dartShell(display), style);
            DartText impl = (DartText) text.getImpl();
            org.eclipse.swt.graphics.Color before = text.getForeground();

            impl.setForeground((double[]) null);
            impl.setForeground(new double[] {1, 0, 0, 1});
            text.setEnabled(false);
            impl.setForeground(new double[] {1, 0, 0, 1});

            assertThat(text.getForeground()).as("style " + style).isEqualTo(before);
        }
    }

    @Test
    @DisplayName("the theme colours of a text are refreshed once per appearance, and only for a multi-line text")
    void themeColorsAreRefreshedOncePerAppearance() {
        DartText multi = impl(SWT.MULTI);
        DartText single = impl(SWT.SINGLE);
        multi.lastAppAppearance = Display.APPEARANCE.Dark;
        single.lastAppAppearance = Display.APPEARANCE.Dark;

        multi.updateThemeColors();
        single.updateThemeColors();
        multi.updateThemeColors();

        assertThat(multi.lastAppAppearance).isEqualTo(((DartDisplay) display.getImpl()).appAppearance);
        assertThat(single.lastAppAppearance).isEqualTo(((DartDisplay) display.getImpl()).appAppearance);
    }

    @Test
    @DisplayName("copying nothing leaves the clipboard alone")
    void copyingNothingKeepsTheClipboard() {
        DartText impl = impl(SWT.SINGLE);
        org.eclipse.swt.dnd.Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(display);
        try {
            clipboard.setContents(new Object[] {"kept"}, new org.eclipse.swt.dnd.Transfer[] {org.eclipse.swt.dnd.TextTransfer.getInstance()});

            impl.copyToClipboard(new char[0]);
            impl.copyToClipboard(null);

            assertThat(clipboard.getContents(org.eclipse.swt.dnd.TextTransfer.getInstance())).isEqualTo("kept");
        } finally {
            clipboard.dispose();
        }
    }
}
