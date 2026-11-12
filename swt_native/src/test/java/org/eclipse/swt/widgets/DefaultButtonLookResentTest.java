package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;

/**
 * A push button is drawn as the default one from its shell's default button. When the shell names
 * another one, as a wizard does moving from Next to Finish, both buttons are re-sent: the one that
 * stops being the default and the one that becomes it.
 */
@ExtendWith(Mocks.class)
class DefaultButtonLookResentTest extends SerializeTestBase {

    private DartShell shell;
    private Button next;
    private Button finish;

    @BeforeEach
    void wizardButtons() {
        Shell dialog = Mocks.shell();
        shell = (DartShell) dialog.getImpl();
        doCallRealMethod().when(shell).setDefaultButton(any());
        next = new Button(dialog, SWT.PUSH);
        finish = new Button(dialog, SWT.PUSH);
        shell.setDefaultButton(next);
        valueOf(next).clearDirty();
        valueOf(finish).clearDirty();
    }

    @Test
    @DisplayName("naming another default button re-sends the primary look of both")
    void bothButtonsAreResent() {
        shell.setDefaultButton(finish);

        assertThat(valueOf(next).changedNames()).contains("primary");
        assertThat(valueOf(finish).changedNames()).contains("primary");
        assertThat(valueOf(next).getPrimary()).isFalse();
        assertThat(valueOf(finish).getPrimary()).isTrue();
    }

    @Test
    @DisplayName("clearing the default button re-sends the one that was")
    void clearingResendsThePreviousOne() {
        shell.setDefaultButton(null);

        assertThat(valueOf(next).changedNames()).contains("primary");
        assertThat(valueOf(next).getPrimary()).isFalse();
    }

    private static VButton valueOf(Button button) {
        return ((DartButton) button.getImpl()).getValue();
    }
}
