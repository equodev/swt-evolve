package dev.equo.swt;

import dev.equo.swt.harness.FlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Regression tests for web-mode behavior: monitor bounds consistency,
 * image provider null-path handling, and image provider exception recovery.
 * Compiled against the web backend (webMain) via the webTest source set.
 * Run with: ./gradlew :swt_native:webTest
 */
@Tag("flutter-it")
class WebDisplayImageFlutterTest {

    private static FlutterHarness flutter;
    private static Display display;

    @BeforeAll
    static void setup() {
        flutter = new FlutterHarness();
        // Injects the harness as the global bridge before Display is created, which
        // causes SwtFlutterBridgeWeb.initForDisplay to skip starting its own server.
        flutter.init();
        display = new Display();
    }

    @AfterAll
    static void teardown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    @Test
    void getPrimaryMonitor_bounds_consistent_with_getMonitors() {
        Monitor primary = display.getPrimaryMonitor();
        Rectangle primaryBounds = primary.getBounds();
        Rectangle firstMonitorBounds = display.getMonitors()[0].getBounds();

        assertThat(primaryBounds).isEqualTo(firstMonitorBounds);
        assertThat(primary.getClientArea()).isEqualTo(primaryBounds);
    }

    @Test
    void imageFileNameProvider_null_path_throws_SWTException_not_NPE() {
        ImageFileNameProvider nullPathProvider = zoom -> null;
        assertThatThrownBy(() -> new Image(display, nullPathProvider))
                .isInstanceOf(SWTException.class)
                .isNotInstanceOf(NullPointerException.class);
    }

    @Test
    void imageDataProvider_swt_exception_falls_back_to_placeholder() {
        ImageDataProvider throwingProvider = zoom -> {
            throw new SWTException(SWT.ERROR_UNSUPPORTED_FORMAT);
        };
        Image image = new Image(display, throwingProvider);
        image.dispose();
    }
}
