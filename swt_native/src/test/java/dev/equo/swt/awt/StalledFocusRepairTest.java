package dev.equo.swt.awt;

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.WindowEvent;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A modal dialog whose activation is held open by a nested event loop is active but never becomes
 * the focused window, so it takes no keyboard input. The window and focus manager are mocks: CI
 * runs headless, and what is under test is the decision to finish the transfer.
 */
class StalledFocusRepairTest {

    private static Dialog showingModalDialog() {
        Dialog dialog = mock(Dialog.class);
        when(dialog.isModal()).thenReturn(true);
        when(dialog.isShowing()).thenReturn(true);
        when(dialog.isFocusableWindow()).thenReturn(true);
        return dialog;
    }

    private static KeyboardFocusManager focusManager(Window active, Window focused) {
        KeyboardFocusManager kfm = mock(KeyboardFocusManager.class);
        when(kfm.getActiveWindow()).thenReturn(active);
        when(kfm.getFocusedWindow()).thenReturn(focused);
        return kfm;
    }

    @Test
    void anActiveModalDialogLeftUnfocusedIsGivenTheFocus() {
        Dialog dialog = showingModalDialog();

        EvolveSwingHost.completeStalledFocus(focusManager(dialog, null), dialog);

        ArgumentCaptor<AWTEvent> event = ArgumentCaptor.forClass(AWTEvent.class);
        verify(dialog).dispatchEvent(event.capture());
        assertThat(event.getValue().getID()).isEqualTo(WindowEvent.WINDOW_GAINED_FOCUS);
        assertThat(((WindowEvent) event.getValue()).getWindow()).isSameAs(dialog);
    }

    @Test
    void aTransferThatCompletedIsLeftAlone() {
        Dialog dialog = showingModalDialog();

        EvolveSwingHost.completeStalledFocus(focusManager(dialog, dialog), dialog);

        verify(dialog, never()).dispatchEvent(any());
    }

    @Test
    void aWindowNoLongerActiveIsLeftAlone() {
        Dialog dialog = showingModalDialog();

        EvolveSwingHost.completeStalledFocus(focusManager(mock(Window.class), null), dialog);

        verify(dialog, never()).dispatchEvent(any());
    }

    @Test
    void aClosedWindowIsLeftAlone() {
        Dialog dialog = showingModalDialog();
        when(dialog.isShowing()).thenReturn(false);

        EvolveSwingHost.completeStalledFocus(focusManager(dialog, null), dialog);

        verify(dialog, never()).dispatchEvent(any());
    }

    @Test
    void aNonFocusableWindowIsLeftAlone() {
        Dialog dialog = showingModalDialog();
        when(dialog.isFocusableWindow()).thenReturn(false);

        EvolveSwingHost.completeStalledFocus(focusManager(dialog, null), dialog);

        verify(dialog, never()).dispatchEvent(any());
    }

    @Test
    void aModelessDialogIsLeftAlone() {
        Dialog dialog = showingModalDialog();
        when(dialog.isModal()).thenReturn(false);

        EvolveSwingHost.completeStalledFocus(focusManager(dialog, null), dialog);

        verify(dialog, never()).dispatchEvent(any());
    }

    @Test
    void aWindowThatIsNotADialogIsLeftAlone() {
        Window window = mock(Window.class);
        when(window.isShowing()).thenReturn(true);
        when(window.isFocusableWindow()).thenReturn(true);

        EvolveSwingHost.completeStalledFocus(focusManager(window, null), window);

        verify(window, never()).dispatchEvent(any());
    }
}
