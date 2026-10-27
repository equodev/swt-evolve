package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * A shell never reaches Flutter on its own: it is an element of the Display's shells, and goes out
 * through the converter registered for {@code Shell}. Anything that converter drops is invisible to
 * the client no matter how correct the value object is — which is how a region set on the Eclipse
 * workbench's drop feedback arrived as null on the Dart side while Java held all nine rectangles.
 */
@Tag("native-unit")
class ShellRegionPayloadNativeTest {

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private Shell feedbackShell() {
        Display display = DartMocks.dartDisplay();
        when(((DartDisplay) display.getImpl()).isValidThread()).thenReturn(true);
        Shell shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);
        shell.setBounds(0, 0, 800, 600);
        return shell;
    }

    @Test
    @DisplayName("a shell's region reaches the wire as its rectangles")
    void regionReachesTheWire() throws Exception {
        Shell shell = feedbackShell();
        Region region = new Region(shell.getDisplay());
        region.add(0, 0, 800, 2);
        region.add(0, 598, 800, 2);
        shell.setRegion(region);

        String json = new String(new Serializer().to(shell), StandardCharsets.UTF_8);

        assertThat(json).as("without the shape there is nothing to clip the shell to")
                .contains("\"rects\"");
        assertThat(json).contains("\"region\"");
    }
}
