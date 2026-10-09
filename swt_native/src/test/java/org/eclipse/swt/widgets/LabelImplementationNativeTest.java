package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What {@link DartLabel} does when it is driven directly rather than through {@link Label}. */
@Tag("native-unit")
class LabelImplementationNativeTest {

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

    private static DartLabel impl(Label label) {
        return (DartLabel) label.getImpl();
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartLabel impl = new DartLabel(shell, SWT.NONE, null);

        Label api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("the implementation reports the text, the image and which of them is shown")
    void implementationReportsWhatItHolds() {
        Label label = new Label(shell, SWT.NONE);
        Image image = new Image(shell.getDisplay(), 16, 16);

        assertThat(impl(label)._text()).isEmpty();
        assertThat(impl(label)._image()).isNull();
        assertThat(impl(label)._isImage()).isFalse();

        label.setText("Name");
        label.setImage(image);
        assertThat(impl(label)._text()).isEqualTo("Name");
        assertThat(impl(label)._image()).isSameAs(image);
        assertThat(impl(label)._isImage()).isTrue();

        label.setText("Name");
        assertThat(impl(label)._image()).isSameAs(image);
        assertThat(impl(label)._isImage()).isFalse();
    }

    @Test
    @DisplayName("a Dart-backed label has no native accessible or image view")
    void noNativeHandles() {
        DartLabel impl = impl(new Label(shell, SWT.NONE));

        assertThat(impl.accessibleHandle()).isZero();
        assertThat(impl.imageView()).isZero();
    }

    @Test
    @DisplayName("a foreground given as colour components changes nothing")
    void foregroundComponentsAreIgnored() {
        for (int style : new int[] { SWT.NONE, SWT.SEPARATOR | SWT.HORIZONTAL }) {
            Label label = new Label(shell, style);
            Color before = label.getForeground();

            impl(label).setForeground(new double[] { 1, 0, 0, 1 });

            assertThat(label.getForeground()).as("style %d", style).isEqualTo(before);
        }
    }
}
