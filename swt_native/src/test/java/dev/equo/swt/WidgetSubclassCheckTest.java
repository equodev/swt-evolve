package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import dev.equo.swt.harness.RecordingBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SWT only lets a class subclass a widget when the widget says so (Composite and Canvas do): any other widget
 * extended from outside {@code org.eclipse.swt.widgets} throws {@code ERROR_INVALID_SUBCLASS}. The check reads
 * the class of the object under construction, which the implementation has to take from the SWT object, not from
 * itself. This test lives outside that package on purpose.
 */
@Tag("native-unit")
class WidgetSubclassCheckTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static void assertInvalidSubclass(Runnable construction) {
        assertThatThrownBy(construction::run)
                .isInstanceOf(SWTException.class)
                .satisfies(e -> assertThat(((SWTException) e).code).isEqualTo(SWT.ERROR_INVALID_SUBCLASS));
    }

    @Test
    @DisplayName("a widget that does not allow subclassing refuses a subclass from another package")
    void refusesSubclassesOfClosedWidgets() {
        assertInvalidSubclass(() -> new ProgressBar(shell, SWT.NONE) {});
        assertInvalidSubclass(() -> new Button(shell, SWT.PUSH) {});
        assertInvalidSubclass(() -> new Label(shell, SWT.NONE) {});
        assertInvalidSubclass(() -> new Scale(shell, SWT.NONE) {});
    }

    @Test
    @DisplayName("the widgets SWT opens to subclassing still accept one")
    void acceptsSubclassesOfOpenWidgets() {
        assertThat(new Composite(shell, SWT.NONE) {}).isNotNull();
        assertThat(new Canvas(shell, SWT.NONE) {}).isNotNull();
        assertThat(new CLabel(shell, SWT.NONE)).isNotNull();
    }
}
