package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Alt+letter activates what carries that mnemonic in the focused control's shell. */
class MnemonicActivationTest extends KeyRoutingTestBase {

    @Test
    @DisplayName("Alt+letter selects the CTabItem carrying it and notifies the folder")
    void altLetterSelectsCTabItem() {
        Shell shell = shell();
        Text focus = new Text(shell, SWT.BORDER);
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        new CTabItem(folder, SWT.NONE).setText("&General");
        CTabItem advanced = new CTabItem(folder, SWT.NONE);
        advanced.setText("&Advanced");
        List<Widget> selected = new ArrayList<>();
        folder.addListener(SWT.Selection, e -> selected.add(e.item));
        when(shell.getChildren()).thenReturn(new Control[] {focus, folder});

        pressAlt(focus, 'a');

        assertThat(folder.getSelection()).isSameAs(advanced);
        assertThat(selected).containsExactly(advanced);
    }

    /** Alt+{@code letter} routed into the control, as the client delivers it. */
    static void pressAlt(Control control, char letter) {
        Event key = new Event();
        key.keyCode = letter;
        key.character = letter;
        key.stateMask = SWT.ALT;
        ControlHelper.routeKeyDown((DartControl) control.getImpl(), key);
    }
}
