package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/** A SashForm's orientation travels in its style, so changing it has to send the style again. */
class SashFormOrientationSyncTest extends SerializeTestBase {

    @Test
    void turning_a_sent_SashForm_vertical_sends_its_style() {
        SashForm w = new SashForm(shell(), SWT.HORIZONTAL);
        serialize(w);

        w.setOrientation(SWT.VERTICAL);

        assertThat(w.getStyle() & SWT.VERTICAL).isNotZero();
        assertThat(((DartSashForm) w.getImpl()).getValue().changedKeys()).contains(org.eclipse.swt.widgets.VWidget.STYLE);
    }

    @Test
    void the_orientation_it_already_has_sends_nothing() {
        SashForm w = new SashForm(shell(), SWT.HORIZONTAL);
        serialize(w);
        // Describing it leaves what changed as it was; what is asked is only what the setter marks.
        ((DartSashForm) w.getImpl()).getValue().changedKeys().clear();

        w.setOrientation(SWT.HORIZONTAL);

        assertThat(((DartSashForm) w.getImpl()).getValue().changedKeys()).doesNotContain(org.eclipse.swt.widgets.VWidget.STYLE);
    }
}
