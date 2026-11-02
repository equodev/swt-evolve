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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** What {@link DartCombo} does when it is driven directly rather than through {@link Combo}. */
@Tag("native-unit")
class ComboImplementationNativeTest {

    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        shell = DartMocks.dartShell(display);
        // The mock display would swallow every event; its real dispatch only needs the listener table.
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        clearInvocations(impl);
    }

    @AfterEach
    void tearDown() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartCombo impl = new DartCombo(shell, SWT.NONE, null);

        Combo api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("the implementation reports the text, items, selection and limits it holds")
    void implementationReportsWhatItHolds() {
        Combo combo = new Combo(shell, SWT.NONE);
        DartCombo impl = (DartCombo) combo.getImpl();

        assertThat(impl._text()).isEmpty();
        assertThat(impl._items()).isEmpty();
        assertThat(impl._textLimit()).isEqualTo(Combo.LIMIT);
        assertThat(impl._listVisible()).isFalse();
        assertThat(impl._visibleItemCount()).isPositive();

        combo.setItems(new String[] {"a", "bb", "ccc"});
        combo.setTextLimit(9);
        combo.setVisibleItemCount(4);
        combo.setText("bb");
        combo.setSelection(new Point(1, 2));
        assertThat(impl._text()).isEqualTo("bb");
        assertThat(impl._items()).containsExactly("a", "bb", "ccc");
        assertThat(impl._textLimit()).isEqualTo(9);
        assertThat(impl._visibleItemCount()).isEqualTo(4);
        assertThat(impl._selection()).isEqualTo(new Point(1, 2));
        assertThat(impl._receivingFocus()).isFalse();
        assertThat(impl._ignoreSetObject()).isFalse();
        assertThat(impl._ignoreSelection()).isFalse();
        assertThat(impl._segments()).isNull();
    }

    @Test
    @DisplayName("a combo is its own event view and takes its minimum height from its text")
    void eventViewAndMinimumHeight() {
        Combo combo = new Combo(shell, SWT.NONE);
        DartCombo impl = (DartCombo) combo.getImpl();

        assertThat(impl.isEventView(0)).isTrue();
        assertThat(impl.getMininumHeight()).isEqualTo(combo.getTextHeight());
    }

    @Test
    @DisplayName("the subclass check accepts the SWT class")
    void subclassCheckAcceptsTheSwtClass() {
        Combo combo = new Combo(shell, SWT.NONE);

        combo.checkSubclass();
        ((DartCombo) combo.getImpl()).checkSubclass();
    }

    @Test
    @DisplayName("the native selection callback tells the Modify listeners at once and posts the Selection")
    void nativeSelectionCallbackSendsModifyAndPostsSelection() {
        Combo combo = new Combo(shell, SWT.READ_ONLY);
        List<Integer> seen = new ArrayList<>();
        combo.addListener(SWT.Modify, e -> seen.add(SWT.Modify));
        combo.addListener(SWT.Selection, e -> seen.add(SWT.Selection));
        DartDisplay displayImpl = (DartDisplay) display.getImpl();

        ((DartCombo) combo.getImpl()).sendSelection();

        assertThat(seen).containsExactly(SWT.Modify);
        verify(displayImpl).postEvent(argThat(e -> e.type == SWT.Selection && e.widget == combo));
    }

    @Test
    @DisplayName("the native selection callback of a selection made from code tells only the Modify listeners")
    void nativeSelectionCallbackHonoursIgnoreSelection() {
        Combo combo = new Combo(shell, SWT.READ_ONLY);
        DartCombo impl = (DartCombo) combo.getImpl();
        List<Integer> seen = new ArrayList<>();
        combo.addListener(SWT.Modify, e -> seen.add(SWT.Modify));
        combo.addListener(SWT.Selection, e -> seen.add(SWT.Selection));
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        impl.ignoreSelection = true;

        impl.sendSelection();

        assertThat(seen).containsExactly(SWT.Modify);
        verify(displayImpl, never()).postEvent(argThat(e -> e.type == SWT.Selection));
    }

    @Test
    @DisplayName("disposing a combo whose list is open forgets it as the display's current combo")
    void disposingTheCurrentComboClearsIt() {
        Combo combo = new Combo(shell, SWT.NONE);
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        displayImpl.currentCombo = combo;

        combo.dispose();

        assertThat(displayImpl.currentCombo).isNull();
        assertThat(combo.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("disposing a combo that is not the current one leaves the display's current combo")
    void disposingAnotherComboKeepsTheCurrentOne() {
        Combo current = new Combo(shell, SWT.NONE);
        Combo other = new Combo(shell, SWT.NONE);
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        displayImpl.currentCombo = current;

        other.dispose();

        assertThat(displayImpl.currentCombo).isSameAs(current);
        displayImpl.currentCombo = null;
    }

    @Test
    @DisplayName("refreshing the items after a font or colour change keeps them")
    void updateItemsKeepsTheItems() {
        for (int style : new int[] {SWT.NONE, SWT.READ_ONLY}) {
            Combo combo = new Combo(shell, style);
            combo.setItems(new String[] {"a", "bb"});
            combo.select(1);
            DartCombo impl = (DartCombo) combo.getImpl();

            impl.updateItems();

            assertThat(combo.getItems()).containsExactly("a", "bb");
            assertThat(combo.getSelectionIndex()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("a foreground colour, and going back to the default, leave the items and the selection alone")
    void foregroundLeavesTheItems() {
        for (int style : new int[] {SWT.NONE, SWT.READ_ONLY}) {
            Combo combo = new Combo(shell, style);
            combo.setItems(new String[] {"a", "bb"});
            combo.select(1);
            DartCombo impl = (DartCombo) combo.getImpl();

            impl.setForeground(new double[] {1, 0, 0, 1});
            impl.setForeground((double[]) null);

            assertThat(combo.getItems()).containsExactly("a", "bb");
            assertThat(combo.getText()).isEqualTo("bb");
        }
    }

    @Test
    @DisplayName("a drag can start in a read-only combo and never in the text of an editable one")
    void dragDetectDependsOnTheKind() {
        Combo editable = new Combo(shell, SWT.NONE);
        Combo readOnly = new Combo(shell, SWT.READ_ONLY);

        assertThat(((DartCombo) editable.getImpl()).dragDetect(1, 1, false, new boolean[1])).isFalse();
        assertThat(((DartCombo) readOnly.getImpl()).dragDetect(1, 1, false, new boolean[1])).isTrue();
    }

    @Test
    @DisplayName("a Verify listener that disposes the combo cancels the change")
    void verifyListenerDisposingTheComboCancelsTheChange() {
        Combo combo = new Combo(shell, SWT.NONE);
        combo.setText("kept");
        combo.addVerifyListener(e -> combo.dispose());

        combo.setText("lost");

        assertThat(combo.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("pasting with no text on the clipboard changes nothing")
    void pasteWithNothingOnTheClipboardChangesNothing() {
        Combo combo = new Combo(shell, SWT.NONE);
        combo.setText("123456");
        combo.setSelection(new Point(1, 3));
        DartCombo impl = org.mockito.Mockito.spy((DartCombo) combo.getImpl());
        org.mockito.Mockito.doReturn(null).when(impl).getClipboardText();

        impl.paste();

        assertThat(combo.getText()).isEqualTo("123456");
        assertThat(combo.getSelection()).isEqualTo(new Point(1, 3));
    }

    @Test
    @DisplayName("a disposed combo shows no characters")
    void aDisposedComboShowsNoCharacters() {
        Combo combo = new Combo(shell, SWT.NONE);
        DartCombo impl = (DartCombo) combo.getImpl();
        combo.setText("abc");
        assertThat(ComboHelper.getCharCount(impl)).isEqualTo(3);

        combo.dispose();

        assertThat(ComboHelper.getCharCount(impl)).isZero();
    }

    @Test
    @DisplayName("the stubs the Windows flavour of the helper calls do nothing here")
    void windowsHelperStubsDoNothing() {
        Combo combo = new Combo(shell, SWT.NONE);
        DartCombo impl = (DartCombo) combo.getImpl();
        combo.setText("abc");

        impl.clearSegments(true);
        impl.applyEditSegments();
        impl.setScrollWidth(10, true);

        assertThat(combo.getText()).isEqualTo("abc");
    }
}
