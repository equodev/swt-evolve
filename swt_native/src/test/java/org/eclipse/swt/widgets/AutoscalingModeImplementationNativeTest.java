package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.AutoscalingMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The autoscaling mode a Control's implementation keeps (setAutoscalingMode exists from SWT 3.133). */
@Tag("native-unit")
class AutoscalingModeImplementationNativeTest {

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

    @Test
    void theModeSetIsKeptAndRefusedAsUnsupported() {
        DartCanvas impl = new DartCanvas(shell, SWT.NONE, null);
        Canvas canvas = impl.getApi();
        assertThat(impl._autoscalingMode()).isNull();
        assertThat(canvas.setAutoscalingMode(AutoscalingMode.DISABLED)).isFalse();
        assertThat(impl._autoscalingMode()).isEqualTo(AutoscalingMode.DISABLED);
        assertThat(canvas.setAutoscalingMode(AutoscalingMode.DISABLED)).isFalse();
    }
}
