package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** The keys Cocoa reads as internal flags are plain data on Win32 and GTK. */
@ExtendWith(Mocks.class)
class WidgetInternalDataKeysTest {

    private Mocks.Delivery delivery;

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
        FlutterBridge.set(new RecordingBridge());
        delivery = Mocks.deliverEvents(Mocks.display());
    }

    @AfterEach
    void tearDown() {
        delivery.close();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("the active flag key answers only what was stored under it")
    void theActiveKeyIsAKey() {
        Canvas canvas = new Canvas(Mocks.shell(), SWT.NONE);
        assertThat(canvas.getData("org.eclipse.swt.internal.isActive")).isNull();
        canvas.setData("org.eclipse.swt.internal.isActive", "mine");
        assertThat(canvas.getData("org.eclipse.swt.internal.isActive")).isEqualTo("mine");
    }

    @Test
    @DisplayName("the WebKit and StyledText flag values are kept as data")
    void theFlagValuesAreData() {
        Canvas canvas = new Canvas(Mocks.shell(), SWT.NONE);
        canvas.setData("org.eclipse.swt.internal.webKitEventsFix");
        assertThat(canvas.getData()).isEqualTo("org.eclipse.swt.internal.webKitEventsFix");
        canvas.setData("org.eclipse.swt.internal.cocoa.styledtext");
        assertThat(canvas.getData()).isEqualTo("org.eclipse.swt.internal.cocoa.styledtext");
    }

    @Test
    @DisplayName("the GL context key is kept as a key")
    void theGlContextKeyIsAKey() {
        Canvas canvas = new Canvas(Mocks.shell(), SWT.NONE);
        canvas.setData("org.eclipse.swt.internal.cocoa.glcontext", "context");
        assertThat(canvas.getData("org.eclipse.swt.internal.cocoa.glcontext")).isEqualTo("context");
    }
}
