package org.eclipse.swt.widgets;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.MessageBoxOpenNativeTest.defaultLabels;
import static org.eclipse.swt.widgets.MessageBoxOpenNativeTest.sent;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** The labels set with {@link MessageBox#setButtonLabels(Map)}, which SWT has from 3.121. */
@Tag("native-unit")
class MessageBoxButtonLabelsNativeTest extends DialogBridgeTestBase {

    @Test
    void theBoxSendsTheLabelsSetForItsButtons() {
        MessageBox box = new MessageBox(shellAnswering(String.valueOf(SWT.YES)), SWT.YES | SWT.NO | SWT.CANCEL);
        Map<Integer, String> labels = new HashMap<>();
        labels.put(SWT.YES, "Save");
        labels.put(SWT.NO, "Don't Save");
        labels.put(SWT.CANCEL, null);
        box.setButtonLabels(labels);

        String[] expected = defaultLabels();
        expected[2] = "Save";
        expected[3] = "Don't Save";
        assertThatWire(sent(box)).node("buttonLabels").isEqualTo(expected);
        assertThat(((DartMessageBox) box.getImpl())._labels()).isSameAs(labels);
    }

    /**
     * The slot a button's label travels in, asserted with literals and no message lookup: a reordered or
     * dropped entry on either side of the payload fails here.
     */
    @Test
    void everyButtonsLabelTravelsInItsOwnSlot() {
        MessageBox box = new MessageBox(shellAnswering(String.valueOf(SWT.OK)), SWT.OK);
        Map<Integer, String> labels = new HashMap<>();
        labels.put(SWT.OK, "ok");
        labels.put(SWT.CANCEL, "cancel");
        labels.put(SWT.YES, "yes");
        labels.put(SWT.NO, "no");
        labels.put(SWT.ABORT, "abort");
        labels.put(SWT.RETRY, "retry");
        labels.put(SWT.IGNORE, "ignore");
        box.setButtonLabels(labels);

        assertThatWire(sent(box)).node("buttonLabels")
                .isEqualTo(new String[] {"ok", "cancel", "yes", "no", "abort", "retry", "ignore"});
    }

    @Test
    void theInterfaceAccessorsAnswerTheBoxsLabelsAndResponse() {
        DartMessageBox box = new DartMessageBox(DartMocks.dartShell(), SWT.OK, null);

        assertThat(box._labels()).isNull();
        assertThat(box._userResponse()).isZero();
    }
}
