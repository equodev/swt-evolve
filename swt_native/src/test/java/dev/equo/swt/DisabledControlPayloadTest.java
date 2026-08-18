package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.eclipse.swt.widgets.Mocks.swtShell;

@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
public class DisabledControlPayloadTest {

    private final Serializer serializer = new Serializer();

    @BeforeEach
    void setup() {
        Config.forceEquo();
    }

    @AfterEach
    void reset() {
        Config.defaultToEclipse();
    }

    private String payloadOf(Widget w) {
        try {
            return new String(serializer.to(((DartWidget) w.getImpl()).getValue()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void disabledTable_reportsEnabledFalse() {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Table table = new Table(parent, SWT.CHECK);
        new TableItem(table, SWT.NONE).setText("Go to declaration");

        table.setEnabled(false);

        assertThatJson(payloadOf(table)).node("enabled").isEqualTo(false);
    }

    @Test
    public void disabledText_reportsEnabledFalse() {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Text text = new Text(parent, SWT.SINGLE);

        text.setEnabled(false);

        assertThatJson(payloadOf(text)).node("enabled").isEqualTo(false);
    }

    @Test
    public void childOfDisabledParent_reportsItsOwnFlag() {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Table table = new Table(parent, SWT.CHECK);

        parent.setEnabled(false);

        assertThatJson(payloadOf(table)).node("enabled").isEqualTo(true);
    }

    @Test
    public void enabledControl_stillReportsEnabledTrue() {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Table table = new Table(parent, SWT.CHECK);

        assertThatJson(payloadOf(table)).node("enabled").isEqualTo(true);
    }
}
