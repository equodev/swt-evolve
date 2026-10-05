package dev.equo.swt;

import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartComposite;
import org.eclipse.swt.widgets.DartSwingIsland;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Islands are routed by type, not by style: {@code SWT.EMBEDDED} is how an application embeds
 * anything foreign (AWT, OLE, a native window), so an EMBEDDED composite stays the plain composite it
 * always was. The island is the {@link SwingIsland} child {@code SWT_AWT.new_Frame} creates in it
 * while swing-evolve's engine owns AWT, and the client picks that region by the name the child is
 * described under.
 */
@ExtendWith(Mocks.class)
class SwingIslandRoutingTest {

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
    @DisplayName("a SwingIsland is described as the island region")
    void swingIslandIsTheIsland() {
        Composite island = new SwingIsland(new Composite(Mocks.shell(), SWT.EMBEDDED), SWT.NONE);

        assertThat(island.getImpl()).isInstanceOf(DartSwingIsland.class);
        assertThat(FlutterBridge.widgetName(island.getImpl()))
                .as("the name the client's switch and the island's channel both key on")
                .isEqualTo("SwingIsland");
    }

    @Test
    @DisplayName("an EMBEDDED composite is the plain composite it always was")
    void embeddedCompositeStaysPlain() {
        Composite embedded = new Composite(Mocks.shell(), SWT.EMBEDDED);

        assertThat(embedded.getImpl()).isExactlyInstanceOf(DartComposite.class);
    }

    @Test
    @DisplayName("the engine is detected by its classes, which this JVM does not have")
    void engineIsAbsentFromAJvmWithoutIt() {
        assertThat(Config.hasSwingEngine()).isFalse();
    }
}
