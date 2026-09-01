package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SerializeTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.swt.widgets.Mocks.device;

/**
 * Upstream {@code Image(Device, ImageGcDrawer, int, int)} runs {@code drawOn} inside a
 * try/finally that disposes the GC on any throw. Callers rely on that: JFace's
 * {@code AnnotationRulerColumn.doubleBufferPaint} builds its buffer with this constructor and
 * leaves the buffer field null when the draw fails, so a document whose annotations make the
 * draw throw re-enters the constructor on every repaint. Here an undisposed GC also strands its
 * off-screen drawer, its comm listeners and the Flutter-side drawer, which only {@code gcDispose}
 * tears down.
 *
 * <p>The same throw must also not leave the half-built Image behind. Upstream draws into a scratch
 * Image and disposes it in the same finally, so a failed draw never touches the Image under
 * construction; here the GC draws on that Image directly, and a constructor that throws never hands
 * it to anyone who could dispose it — its Flutter-side remote ref is released only from dispose().
 *
 * <p>Not run on the GTK backend: there {@code Image#isDisposed()} is {@code surface == 0}, and this
 * constructor never calls {@code init(width, height)} — the only place that sets {@code surface} —
 * so the image reports itself disposed and {@code new GC(image)} fails with ERROR_GRAPHIC_DISPOSED
 * before the drawer is ever called. That is a separate defect in the same constructor, tracked on
 * its own; the dispose contract asserted here is platform-independent.
 */
@DisabledOnOs(OS.LINUX)
class ImageGcDrawerFailureTest extends SerializeTestBase {

    private boolean bootstrapped;

    // GC(Image) pumps the event loop until the Flutter client is up; there is none here, and the
    // mocked Display records every dispatch call until it runs out of memory.
    @BeforeEach
    void skipTheClientHandshake() {
        bootstrapped = FlutterBridge.displayBootstrapped;
        FlutterBridge.displayBootstrapped = true;
    }

    @AfterEach
    void restoreTheClientHandshake() {
        FlutterBridge.displayBootstrapped = bootstrapped;
    }

    @Test
    void a_drawer_that_throws_still_disposes_the_gc_it_was_handed() {
        GC[] handed = new GC[1];
        ImageGcDrawer drawer = (gc, width, height) -> {
            handed[0] = gc;
            throw new IllegalStateException("assertion failed");
        };

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> new Image(device(), drawer, 20, 20));

        assertThat(handed[0]).isNotNull();
        assertThat(handed[0].isDisposed()).isTrue();
    }

    @Test
    void a_drawer_that_throws_leaves_no_undisposed_image_behind() {
        // The constructor throws, so the test never gets the Image; read it off the GC's own data
        // while the drawer still holds it, which is the only reference anyone ever sees.
        Image[] underConstruction = new Image[1];
        ImageGcDrawer drawer = (gc, width, height) -> {
            if (gc.getImpl() instanceof DartGC dartGc) {
                underConstruction[0] = dartGc.data.image;
            }
            throw new IllegalStateException("assertion failed");
        };

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> new Image(device(), drawer, 20, 20));

        assertThat(underConstruction[0]).isNotNull();
        assertThat(underConstruction[0].isDisposed()).isTrue();
    }

    @Test
    void a_drawer_that_returns_normally_disposes_the_gc_too() {
        GC[] handed = new GC[1];
        ImageGcDrawer drawer = (gc, width, height) -> handed[0] = gc;

        Image image = new Image(device(), drawer, 20, 20);

        assertThat(handed[0].isDisposed()).isTrue();
        image.dispose();
    }
}
