package org.eclipse.swt.dnd;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/** Each Display, one per session, keeps its own clipboards and its own drag in flight. */
class DndPerDisplayTest {

    private final CountDownLatch release = new CountDownLatch(1);
    private Thread otherThread;
    private Display display;

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
        display = new Display();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        release.countDown();
        if (otherThread != null) otherThread.join(TimeUnit.SECONDS.toMillis(10));
        if (!display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    /** Runs {@code work} on another Display, on its own thread, which keeps that Display until the test ends. */
    private void onAnotherDisplay(Consumer<Display> work) throws Exception {
        CompletableFuture<Void> done = new CompletableFuture<>();
        otherThread = new Thread(() -> {
            Display other = new Display();
            try {
                work.accept(other);
                done.complete(null);
                release.await();
            } catch (Throwable t) {
                done.completeExceptionally(t);
            } finally {
                other.dispose();
            }
        }, "other-session");
        otherThread.setDaemon(true);
        otherThread.start();
        done.get(10, TimeUnit.SECONDS);
    }

    @Test
    void aDropReadsNoDragOfAnotherDisplay() throws Exception {
        onAnotherDisplay(other -> {
            Composite from = new Composite(new Shell(other), SWT.NONE);
            DragSource source = new DragSource(from, DND.DROP_COPY);
            source.setTransfer(new Transfer[] {TextTransfer.getInstance()});
            source.addDragListener(new DragSourceAdapter() {
                @Override
                public void dragSetData(DragSourceEvent event) {
                    event.data = "other session";
                }
            });
            from.notifyListeners(SWT.DragDetect, new Event());
        });
        List<DropTargetEvent> drops = new ArrayList<>();
        DropTarget target = new DropTarget(new Composite(new Shell(display), SWT.NONE), DND.DROP_COPY);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                drops.add(event);
            }
        });

        ((DartDropTarget) target.getImpl()).sendEvent(DND.Drop, new Event());

        assertThat(drops).singleElement().satisfies(e -> {
            assertThat(e.data).isNull();
            assertThat(e.dataTypes).isEmpty();
        });
    }

    // The selection clipboard: DND.CLIPBOARD would also write the desktop's system clipboard.
    @Test
    void aClipboardReadsNothingAnotherDisplayWrote() throws Exception {
        onAnotherDisplay(other -> new Clipboard(other).setContents(new Object[] {"{\\rtf1 other session}"},
                new Transfer[] {RTFTransfer.getInstance()}, DND.SELECTION_CLIPBOARD));
        Clipboard clipboard = new Clipboard(display);
        try {
            assertThat(clipboard.getContents(RTFTransfer.getInstance(), DND.SELECTION_CLIPBOARD)).isNull();
            assertThat(clipboard.getAvailableTypes(DND.SELECTION_CLIPBOARD)).isEmpty();
        } finally {
            clipboard.dispose();
        }
    }
}
