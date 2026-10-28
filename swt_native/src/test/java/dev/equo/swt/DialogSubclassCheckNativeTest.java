package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * SWT refuses a dialog subclassed outside its own package unless the subclass overrides checkSubclass(). The
 * subclasses are declared here, outside org.eclipse.swt.widgets, as an application declares them.
 */
@Tag("native-unit")
class DialogSubclassCheckNativeTest {

    private static void assertRefused(Executable construction) {
        assertThatThrownBy(construction::execute)
                .isInstanceOf(SWTException.class)
                .extracting(e -> ((SWTException) e).code)
                .isEqualTo(SWT.ERROR_INVALID_SUBCLASS);
    }

    @Test
    void aDialogSubclassedOutsideSwtIsRefused() {
        Shell shell = DartMocks.dartShell();
        assertRefused(() -> new MessageBox(shell) {
        });
        assertRefused(() -> new MessageBox(shell, SWT.YES | SWT.NO) {
        });
        assertRefused(() -> new ColorDialog(shell) {
        });
        assertRefused(() -> new FontDialog(shell, SWT.NONE) {
        });
    }

    @Test
    void aSubclassThatOverridesTheCheckIsBuilt() {
        Shell shell = DartMocks.dartShell();
        MessageBox box = new MessageBox(shell, SWT.OK | SWT.CANCEL) {
            @Override
            protected void checkSubclass() {
            }
        };
        ColorDialog color = new ColorDialog(shell) {
            @Override
            protected void checkSubclass() {
            }
        };
        assertThat(box.getStyle() & (SWT.OK | SWT.CANCEL)).isEqualTo(SWT.OK | SWT.CANCEL);
        assertThat(color.getParent()).isSameAs(shell);
    }
}
