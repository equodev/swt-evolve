package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DartButton;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

@ExtendWith(Mocks.class)
class SerializerDisposedWidgetTest extends SerializeTestBase {

    // writeWithId() is the registered writer for every DartWidget reference nested inside an
    // ancestor's payload (Composite.children, Display.shells, ...). A widget can be disposed
    // between the start of that tree walk and reaching this node (see the comment on the
    // `disposed` check in Serializer.writeWithId) — the early-return path below must stay valid
    // JSON, since one bad node poisons the whole ancestor payload.
    @Test
    void writeWithId_on_a_disposed_widget_does_not_emit_a_trailing_comma() {
        Button w = new Button(swtShell(), SWT.NONE);
        DartButton impl = (DartButton) w.getImpl();
        w.dispose();

        String json = serialize(impl);

        assertThat(json).matches("\\{[^{}]*\\}");
        assertThat(json).doesNotContain(",}");
    }
}
