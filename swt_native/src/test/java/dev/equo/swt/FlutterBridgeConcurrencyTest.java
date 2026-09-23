package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * Regression test for the {@code dirty}-set race in {@link FlutterBridge#update()}.
 */
@ExtendWith(Mocks.class)
public class FlutterBridgeConcurrencyTest extends SerializeTestBase {

    @Test
    void update_snapshotIsThreadSafe_underConcurrentDirtyMutation() throws Exception {
        // A pool of real Dart widgets so update() exercises its full (post-snapshot) path.
        List<DartWidget> widgets = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            widgets.add((DartWidget) new Button(shell(), SWT.PUSH).getImpl());
        }
        FlutterBridge.clearDirty();

        // Any bridge instance works: dirty(...) mutates the static `dirty` set. This is the real
        // mutator path the production code uses (synchronized add), so the writer itself never races.
        FlutterBridge writerBridge = new MockFlutterBridge();

        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean stop = new AtomicBoolean(false);

        // Writers: continuously re-mark every widget dirty (update() clears the set each pass, so this
        // keeps it churning and maximises overlap with the reader's snapshot copy).
        List<Thread> writers = new ArrayList<>();
        for (int w = 0; w < 2; w++) {
            Thread writer = new Thread(() -> {
                try {
                    while (!stop.get()) {
                        for (DartWidget widget : widgets) writerBridge.dirty(widget);
                    }
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }, "dirty-writer-" + w);
            writers.add(writer);
        }

        // Reader: the code under test. Snapshots `dirty` on every call.
        //
        // Bounded by a deadline as well as by a pass count: three busy threads want three cores, and
        // on a runner that has fewer the reader is simply slow. Its throughput is not what this
        // asserts - the overlap is - so running out of time has to end the loop rather than fail it.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        Thread reader = new Thread(() -> {
            try {
                for (int i = 0; i < 5_000 && failure.get() == null; i++) {
                    if (System.nanoTime() - deadline > 0) break;
                    FlutterBridge.update();
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        }, "dirty-reader");

        try {
            writers.forEach(Thread::start);
            reader.start();
            reader.join(30_000);
        } finally {
            stop.set(true);
            for (Thread writer : writers) writer.join(5_000);
            FlutterBridge.clearDirty();
        }

        assertThat(reader.isAlive()).as("reader thread should have finished").isFalse();
        if (failure.get() != null) {
            throw new AssertionError(
                    "FlutterBridge.update() raced with a concurrent dirty mutation", failure.get());
        }
    }
}
