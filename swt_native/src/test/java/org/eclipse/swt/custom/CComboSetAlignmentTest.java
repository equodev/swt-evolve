package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** As in native SWT, every {@code setAlignment} call recreates the CCombo's inner Text, whatever the value. */
@ExtendWith(Mocks.class)
public class CComboSetAlignmentTest extends SerializeTestBase {

    private static Text innerText(CCombo combo) {
        return ((DartCCombo) combo.getImpl())._text();
    }

    @Test
    void settingTheSameAlignmentAgainRecreatesTheText() {
        CCombo combo = new CCombo(shell(), SWT.BORDER);
        combo.setAlignment(SWT.CENTER);
        Text first = innerText(combo);

        combo.setAlignment(SWT.CENTER);

        assertThat(first.isDisposed()).isTrue();
        assertThat(innerText(combo)).isNotSameAs(first);
        assertThat(combo.getAlignment()).isEqualTo(SWT.CENTER);
    }

    @Test
    void recreatingTheTextKeepsItsContent() {
        CCombo combo = new CCombo(shell(), SWT.BORDER);
        combo.setText("abc");
        combo.setAlignment(SWT.RIGHT);

        combo.setAlignment(SWT.RIGHT);

        assertThat(combo.getText()).isEqualTo("abc");
        assertThat(combo.getAlignment()).isEqualTo(SWT.RIGHT);
    }
}
