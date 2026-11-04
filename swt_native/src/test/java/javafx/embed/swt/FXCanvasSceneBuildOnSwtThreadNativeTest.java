package javafx.embed.swt;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.stage.EmbeddedWindow;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regression guard for FXCanvas building a JavaFX scene graph on the SWT thread.
 *
 * <p>A host application builds and mutates its FXCanvas scene graph from the SWT UI thread, as
 * upstream {@code FXCanvas} allows by running JavaFX on that thread. Under
 * Evolve JavaFX runs on its own Monocle thread, so once {@code setScene} has given the scene a live
 * embedded window, any further mutation from the SWT thread throws {@code IllegalStateException: Not
 * on FX application thread} and the editor is disabled. {@link FXCanvas#adoptSwtThreadAsFxUserThread()}
 * fixes this by switching JavaFX to the single-thread model — the SWT thread becomes the FX user
 * thread, the Monocle thread is parked, and the SWT thread drains JavaFX's deferred-runnable queue
 * itself so {@code Platform.runLater} keeps working.</p>
 *
 * <p>Drives the real production method against a live headless (Monocle) toolkit — no SWT Display
 * needed — asserting the exact failure before adoption and that the mutation, a fresh windowed-scene
 * build, and a {@code Scene.snapshot()} all succeed on the calling thread after it.</p>
 */
@Tag("native-unit")
// OpenJFX publishes no win-aarch64 jars in any version (nor linux-aarch64 for 21.x), so these hosts
// only get x64 JavaFX natives.
@DisabledOnOs(value = { OS.LINUX, OS.WINDOWS }, architectures = "aarch64")
public class FXCanvasSceneBuildOnSwtThreadNativeTest {

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.vsync", "false");
        Platform.setImplicitExit(false);
        CountDownLatch up = new CountDownLatch(1);
        try {
            Platform.startup(up::countDown);
        } catch (IllegalStateException alreadyStarted) {
            up.countDown();
        }
        assertThat(up.await(20, TimeUnit.SECONDS)).as("JavaFX toolkit started").isTrue();
    }

    @AfterAll
    static void release() {
        // Undo the process-wide adoption so a sibling FX test in the same JVM sees a pristine toolkit.
        FXCanvas.releaseSwtThreadAdoption();
    }

    @Test
    void sceneGraphMutationFromSwtThreadFailsUntilTheThreadIsAdopted() throws Exception {
        // RED — a node in a scene that has a live embedded window, mutated from this (non-FX) thread,
        // throws the exact "Not on FX application thread" exception.
        Scene scene = windowedSceneOnFxThread();
        assertThatThrownBy(() -> ((Group) scene.getRoot()).getChildren().add(new Rectangle(5, 5)))
                .as("windowed-scene mutation off the FX thread is rejected before adoption")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not on FX application thread");

        // GREEN — adopt this (SWT) thread as JavaFX's user thread.
        assertThat(FXCanvas.adoptSwtThreadAsFxUserThread()).as("adoption succeeded").isTrue();
        assertThat(Toolkit.getToolkit().isFxUserThread())
                .as("the calling thread now owns the JavaFX scene graph").isTrue();

        // The mutation that threw above now succeeds on this thread.
        ((Group) scene.getRoot()).getChildren().add(new Rectangle(5, 5));
        assertThat(((Group) scene.getRoot()).getChildren()).hasSize(1);

        // The invariant that broke the first attempt at this fix: parking the Monocle thread must not
        // starve Platform.runLater. With the Monocle thread parked, the SWT thread owns the drain, so
        // a posted runnable runs — on this (adopting) thread — once the SWT-side pump drains it. A
        // Display-less test drives that drain directly.
        CountDownLatch posted = new CountDownLatch(1);
        AtomicReference<Thread> ranOn = new AtomicReference<>();
        Platform.runLater(() -> {
            ranOn.set(Thread.currentThread());
            posted.countDown();
        });
        long deadline = System.currentTimeMillis() + 2000;
        while (posted.getCount() > 0 && System.currentTimeMillis() < deadline) {
            FXCanvas.drainFxRunnables();
            if (posted.getCount() > 0) {
                Thread.sleep(5);
            }
        }
        assertThat(posted.getCount())
                .as("Platform.runLater still runs after adoption (not starved by the parked Monocle thread)")
                .isEqualTo(0L);
        assertThat(ranOn.get())
                .as("the posted runnable ran on the adopting (SWT) thread, the upstream single-thread contract")
                .isSameAs(Thread.currentThread());

        // And the whole FXCanvas.setScene path — create an embedded window, mutate, lay out and
        // snapshot — runs on this thread too, which is what the adopted canvas does for rendering.
        Group root = new Group();
        Scene fresh = new Scene(root, 40, 30);
        EmbeddedWindow window = new EmbeddedWindow(new NoopHost());
        window.setScene(fresh);
        window.show();
        root.getChildren().add(new Rectangle(0, 0, 40, 30));
        root.resize(40, 30);
        root.applyCss();
        root.layout();
        WritableImage img = new WritableImage(40, 30);
        fresh.snapshot(img);
        assertThat(img.getWidth()).as("snapshot rendered on the adopted thread").isEqualTo(40.0);
    }

    /** A scene attached to a live embedded window (so Parent.onProposedChange enforces the FX thread). */
    private static Scene windowedSceneOnFxThread() throws Exception {
        return onFxThread(() -> {
            Group root = new Group();
            Scene scene = new Scene(root, 40, 30);
            EmbeddedWindow window = new EmbeddedWindow(new NoopHost());
            window.setScene(scene);
            window.show();
            return scene;
        });
    }

    private static <T> T onFxThread(Callable<T> work) throws Exception {
        AtomicReference<T> out = new AtomicReference<>();
        AtomicReference<Throwable> err = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                out.set(work.call());
            } catch (Throwable t) {
                err.set(t);
            } finally {
                done.countDown();
            }
        });
        assertThat(done.await(20, TimeUnit.SECONDS)).as("FX work completed").isTrue();
        if (err.get() != null) {
            throw new RuntimeException(err.get());
        }
        return out.get();
    }

    /** Minimal embedded host: enough for EmbeddedWindow to acquire a peer; everything else a no-op. */
    private static final class NoopHost implements HostInterface {
        public void setEmbeddedStage(EmbeddedStageInterface stage) {}
        public void setEmbeddedScene(EmbeddedSceneInterface scenePeer) {}
        public boolean requestFocus() { return true; }
        public boolean traverseFocusOut(boolean forward) { return true; }
        public void setPreferredSize(int width, int height) {}
        public void repaint() {}
        public void setEnabled(boolean enabled) {}
        public void setCursor(CursorFrame cursorFrame) {}
        public boolean grabFocus() { return true; }
        public void ungrabFocus() {}
    }
}
