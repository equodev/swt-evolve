package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Label shows its text or its image, whichever was set last. getText() and getImage() keep
 * answering both, but Flutter is sent, and the sizing measures, only the one on show.
 */
@Tag("native-unit")
class LabelTextOrImageTest {

    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
        display = shell.getDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static VLabel value(Label label) {
        return ((DartLabel) label.getImpl()).getValue();
    }

    private static Point natural(Label label) {
        return label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    @Test
    @DisplayName("the image set last is the one sent and measured, the text is kept")
    void imageSetAfterTheText() {
        Image image = new Image(display, 16, 16);
        Label imageOnly = new Label(shell, SWT.NONE);
        imageOnly.setImage(image);
        Label label = new Label(shell, SWT.NONE);
        label.setText("a long text that would make the label much wider");
        label.setImage(image);

        assertThat(label.getText()).isEqualTo("a long text that would make the label much wider");
        assertThat(label.getImage()).isSameAs(image);
        assertThat(value(label).getText()).isEmpty();
        assertThat(value(label).getImage()).isSameAs(image);
        assertThat(natural(label)).isEqualTo(natural(imageOnly));
    }

    @Test
    @DisplayName("the text set last is the one sent and measured, the image is kept")
    void textSetAfterTheImage() {
        Image image = new Image(display, 16, 16);
        Label textOnly = new Label(shell, SWT.NONE);
        textOnly.setText("OK");
        Label label = new Label(shell, SWT.NONE);
        label.setImage(image);
        label.setText("OK");

        assertThat(label.getImage()).isSameAs(image);
        assertThat(value(label).getText()).isEqualTo("OK");
        assertThat(value(label).getImage()).isNull();
        assertThat(natural(label)).isEqualTo(natural(textOnly));
    }

    @Test
    @DisplayName("clearing the image brings the text back")
    void clearingTheImage() {
        Image image = new Image(display, 16, 16);
        Label textOnly = new Label(shell, SWT.NONE);
        textOnly.setText("OK");
        Label label = new Label(shell, SWT.NONE);
        label.setText("OK");
        label.setImage(image);
        label.setImage(null);

        assertThat(value(label).getText()).isEqualTo("OK");
        assertThat(value(label).getImage()).isNull();
        assertThat(natural(label)).isEqualTo(natural(textOnly));
    }

    @Test
    @DisplayName("a separator is sent no text and no image")
    void separator() {
        Image image = new Image(display, 16, 16);
        Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setText("ignored");
        separator.setImage(image);

        assertThat(value(separator).getText()).isEmpty();
        assertThat(value(separator).getImage()).isNull();
    }

    @Test
    @DisplayName("a separator ignores an image that has been disposed")
    void separatorIgnoresADisposedImage() {
        Image image = new Image(display, 16, 16);
        image.dispose();
        Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setImage(image);

        assertThat(separator.getImage()).isNull();
    }
}
