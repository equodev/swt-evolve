package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CCombo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** A CCombo is subclassed only from inside the SWT custom package, whatever the implementation behind it. */
@ExtendWith(Mocks.class)
public class CComboSubclassNativeTest extends SerializeTestBase {

    @Test
    void aSubclassOutsideTheCustomPackageIsRefused() {
        assertThatThrownBy(() -> new CCombo(shell(), SWT.NONE) {})
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_INVALID_SUBCLASS));
    }
}
