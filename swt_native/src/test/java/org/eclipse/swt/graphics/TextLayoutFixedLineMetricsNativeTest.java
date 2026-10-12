package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TextLayout#setFixedLineMetrics} (SWT 3.125 and later) pins every line of the web TextLayout engine to the
 * metrics given: StyledText hands its layouts the metrics of its fixed line height that way.
 */
@Tag("native-unit")
class TextLayoutFixedLineMetricsNativeTest {

    @Test
    void fixedLineMetricsSetEveryLinesHeight() {
        Display display = DartMocks.dartDisplay();
        TextLayout layout = new TextLayout(display);
        try {
            layout.setFont(new DartFont(display, "System", 12, SWT.NORMAL, null).getApi());
            layout.setText("abc\ndef");
            layout.setFixedLineMetrics(TextLayoutHelper.lineMetrics(30, 9, 7));
            assertThat(layout.getLineBounds(0).height).isEqualTo(39);
            assertThat(layout.getLineBounds(1).y).isEqualTo(39);
            assertThat(layout.getBounds().height).isEqualTo(78);
            assertThat(layout.getLineMetrics(1).getAscent()).isEqualTo(30);
        } finally {
            layout.dispose();
        }
    }
}
