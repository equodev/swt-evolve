package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The system properties a Display reads while it is built. Its own source because
 * {@link DartDisplay#USE_SYSTEM_THEME} comes from the cocoa Display, which names it from 3.112 on.
 */
@Tag("flutter-it")
class DisplaySystemThemePropertyFlutterTest {

    private Display display;

    @BeforeEach
    void setUp() {
        display = newDisplay();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        Display current = Display.getCurrent();
        if (current != null && !current.isDisposed())
            current.dispose();
        FlutterBridge.set(null);
    }

    private static Display newDisplay() {
        FlutterBridge.set(new RecordingBridge());
        try {
            return new Display();
        } finally {
            FlutterBridge.set(null);
        }
    }

    /**
     * The cocoa branch behind {@link DartDisplay#USE_SYSTEM_THEME} is empty in Evolve, so the lookup
     * itself is what this pins: the property is still consulted while the Display is built. The read is
     * counted through a {@link Properties} that System.getProperty delegates to.
     */
    @Test
    void theSystemPropertiesUpstreamReadsAreRead() {
        display.dispose();
        AtomicInteger systemThemeReads = new AtomicInteger();
        Properties original = System.getProperties();
        Properties counting = new Properties() {

            @Override
            public String getProperty(String key) {
                if (DartDisplay.USE_SYSTEM_THEME.equals(key))
                    systemThemeReads.incrementAndGet();
                return super.getProperty(key);
            }
        };
        counting.putAll(original);
        System.setProperties(counting);
        try {
            System.setProperty(DartDisplay.USE_SYSTEM_THEME, "true");
            System.setProperty("org.eclipse.swt.internal.carbon.smallFonts", "true");
            display = newDisplay();
            assertThat(systemThemeReads.get()).isPositive();
            assertThat(((DartDisplay) display.getImpl())._smallFonts()).isTrue();
        } finally {
            System.setProperties(original);
        }
    }
}
