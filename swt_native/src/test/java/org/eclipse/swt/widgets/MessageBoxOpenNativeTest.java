package org.eclipse.swt.widgets;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What {@link DartMessageBox#open()} sends to Flutter and what it returns for the client's answer on the
 * {@code MessageBox/<id>/close} channel: the id of the button pressed.
 */
@Tag("native-unit")
class MessageBoxOpenNativeTest extends DialogBridgeTestBase {

    /**
     * The keys the labels are looked up under, in the order they travel. This is the test's own copy of that
     * table, so reordering, renaming or dropping an entry on the production side fails here. The labels
     * themselves cannot be written out as literals: this suite runs without SWT's resource bundle on the
     * classpath, so {@code SWT.getMessage} answers the key and not "OK".
     */
    static final String[] MESSAGE_KEYS = {"SWT_OK", "SWT_Cancel", "SWT_Yes", "SWT_No", "SWT_Abort", "SWT_Retry",
            "SWT_Ignore"};

    static String[] defaultLabels() {
        String[] labels = new String[MESSAGE_KEYS.length];
        for (int i = 0; i < labels.length; i++)
            labels[i] = SWT.getMessage(MESSAGE_KEYS[i]);
        return labels;
    }

    static String sent(MessageBox box) {
        try {
            return new String(new Serializer().to(((DartMessageBox) box.getImpl()).getValue()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void openReturnsTheButtonTheClientAnswersAndShowsTheBox() {
        Shell shell = shellAnswering(String.valueOf(SWT.NO));
        MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL);

        assertThat(box.open()).isEqualTo(SWT.NO);
        verify((DartShell) shell.getImpl()).addDialog((DartDialog) box.getImpl());
        verify((DartShell) shell.getImpl()).removeDialog((DartDialog) box.getImpl());
    }

    @Test
    void anAnswerThatIsNoNumberIsACancel() {
        assertThat(new MessageBox(shellAnswering(""), SWT.OK).open()).isEqualTo(SWT.CANCEL);
        assertThat(new MessageBox(shellAnswering("yes"), SWT.YES | SWT.NO).open()).isEqualTo(SWT.CANCEL);
        assertThat(new MessageBox(shellAnswering(null), SWT.RETRY | SWT.CANCEL).open()).isEqualTo(SWT.CANCEL);
    }

    @Test
    void theBoxSendsItsStyleTitleAndMessage() {
        MessageBox box = new MessageBox(shellAnswering(String.valueOf(SWT.OK)), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
        box.setText("Title");
        box.setMessage("Message");
        box.open();

        String json = sent(box);
        assertThatJson(json).node("swt").isEqualTo("MessageBox");
        assertThatJson(json).node("text").isEqualTo("Title");
        assertThatJson(json).node("message").isEqualTo("Message");
        assertThatJson(json).node("style").isEqualTo(box.getStyle());
    }

    @Test
    void theBoxSendsSwtsLabelForEveryButtonByDefault() {
        MessageBox box = new MessageBox(shellAnswering(String.valueOf(SWT.OK)));

        assertThatJson(sent(box)).node("buttonLabels").isArray().containsExactly((Object[]) defaultLabels());
    }

    @Test
    void theInterfaceAccessorsAnswerTheBoxsState() {
        DartMessageBox box = new DartMessageBox(DartMocks.dartShell(), SWT.OK, null);
        box.setMessage("Message");

        assertThat(box._message()).isEqualTo("Message");
        assertThat(box.getApi().getImpl()).isSameAs(box);
    }
}
