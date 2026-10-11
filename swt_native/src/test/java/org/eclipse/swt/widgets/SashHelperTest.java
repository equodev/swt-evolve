package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A Sash is 64 long and a few pixels thick, as on every SWT; only the thickness and the way a negative
 * hint is read depend on the platform, so the helper is tested with the platform passed in.
 */
@ExtendWith(Mocks.class)
class SashHelperTest {

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("Cocoa: a horizontal sash is 64 x 5 and a vertical one 5 x 64")
    void cocoaDefaults() {
        assertThat(SashHelper.computeSize("cocoa", SWT.HORIZONTAL, SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(64, 5));
        assertThat(SashHelper.computeSize("cocoa", SWT.VERTICAL, SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(5, 64));
    }

    @Test
    @DisplayName("GTK and Windows: the sash is 3 thick")
    void gtkAndWindowsDefaults() {
        for (String platform : new String[] {"gtk", "win32"}) {
            assertThat(SashHelper.computeSize(platform, SWT.HORIZONTAL, SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(64, 3));
            assertThat(SashHelper.computeSize(platform, SWT.VERTICAL, SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(3, 64));
        }
    }

    @Test
    @DisplayName("a hint replaces its dimension, a negative one as it comes on Cocoa and as 0 elsewhere")
    void hints() {
        assertThat(SashHelper.computeSize("cocoa", SWT.HORIZONTAL, 100, 40)).isEqualTo(new Point(100, 40));
        assertThat(SashHelper.computeSize("cocoa", SWT.HORIZONTAL, 100, SWT.DEFAULT)).isEqualTo(new Point(100, 5));
        assertThat(SashHelper.computeSize("cocoa", SWT.VERTICAL, SWT.DEFAULT, 90)).isEqualTo(new Point(5, 90));
        assertThat(SashHelper.computeSize("cocoa", SWT.HORIZONTAL, -7, -9)).isEqualTo(new Point(-7, -9));
        assertThat(SashHelper.computeSize("gtk", SWT.HORIZONTAL, 100, 40)).isEqualTo(new Point(100, 40));
        assertThat(SashHelper.computeSize("gtk", SWT.HORIZONTAL, -7, -9)).isEqualTo(new Point(0, 0));
        assertThat(SashHelper.computeSize("win32", SWT.VERTICAL, -7, SWT.DEFAULT)).isEqualTo(new Point(0, 64));
    }

    @Test
    @DisplayName("Sash.computeSize is long along the bar on every platform")
    void aSashIsLongAlongItsBar() {
        Composite parent = new Composite(Mocks.shell(), SWT.NONE);
        Point horizontal = new Sash(parent, SWT.HORIZONTAL).computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point vertical = new Sash(parent, SWT.VERTICAL).computeSize(SWT.DEFAULT, SWT.DEFAULT);
        assertThat(horizontal.x).isEqualTo(64);
        assertThat(horizontal.y).isBetween(3, 5);
        assertThat(vertical.y).isEqualTo(64);
        assertThat(vertical.x).isBetween(3, 5);
    }
}
