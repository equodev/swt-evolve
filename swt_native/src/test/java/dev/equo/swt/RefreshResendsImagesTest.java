package dev.equo.swt;

import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A client that asks for a widget lacks something it named, an image included: Java may count an
 * image as delivered that never reached it, and naming it again would leave the widget without it.
 */
@ExtendWith(Mocks.class)
class RefreshResendsImagesTest {

    private RecordingBridge bridge;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a widget the client asks for is sent with the images it names, though they were sent before")
    void aRefreshedWidgetCarriesItsImagesAgain() {
        Shell shell = Mocks.shell();
        // A button shows its image beside its text: a label's setText drops the image.
        Button button = new Button(shell, SWT.PUSH);
        button.setData("dev.equo.swt.new", false);
        Image image = new Image(Mocks.display(), new ImageData(4, 4, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)));
        button.setImage(image);
        FlutterBridge.update();
        assertThat(imageRecords()).as("the first send describes the image").isPositive();

        button.setText("again");
        bridge.comm.sent.clear();
        FlutterBridge.update();
        assertThat(imageRecords()).as("a change names the image the client holds").isZero();

        FlutterBridge.handleWidgetRefresh(Long.toString(FlutterBridge.id(button)));
        bridge.comm.sent.clear();
        FlutterBridge.update();
        assertThat(imageRecords()).as("the widget the client asked for comes with its image").isPositive();
    }

    private long imageRecords() {
        return bridge.comm.sent.stream().filter(f -> f.event.startsWith("Image/")).count();
    }
}
