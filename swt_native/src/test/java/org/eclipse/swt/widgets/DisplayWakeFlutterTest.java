package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the web {@code DartDisplay} sleep()/wake contract <em>behaviourally</em>, through the
 * public SWT API, with the real threading an application uses: a dedicated <b>UI thread</b> creates
 * the Display and runs the canonical event loop ({@code while (readAndDispatch()) ; sleep();}). Every
 * wake source is then triggered from <b>another thread</b> (the JUnit test thread or a worker) — never
 * from the UI thread itself — so the test exercises the genuine cross-thread unpark, not a staged one.
 *
 * <p>The loop is observed only through public API, the way an application would:
 * <ul>
 *   <li><b>park / wake</b> is detected with {@code Listener}s on the public {@link SWT#PreExternalEventDispatch}
 *       and {@link SWT#PostExternalEventDispatch} events, which {@code sleep()} fires immediately
 *       before parking and immediately after waking;</li>
 *   <li><b>dispatch</b> is detected by the side effect the wake exists to deliver — a queued
 *       {@code Runnable} running, or a real {@code SWT.Selection}/{@code SWT.MouseMove} listener
 *       firing.</li>
 * </ul>
 * Widgets are created on the UI thread (via {@link #onUiThread}); only the wake <em>triggers</em>
 * cross from another thread, which is exactly the real scenario (background work / a Dart event
 * arriving on the comm thread must wake a parked UI thread).
 *
 * <p>Covers: {@code asyncExec}, {@code syncExec}, events from Dart (Selection), input events from
 * Dart (MouseMove), marking a widget dirty, and that an idle loop parks indefinitely (no timeout cap
 * in pure web mode) until woken. Like {@link RadioGroupFlutterTest} this is a {@code @Tag("flutter-it")}
 * test run by the {@code nativeTest} task against the whole-tree-Flutter backend; it reaches its one web-only class
 * ({@code WebDisplayBridge}, see the dirty test) by reflection, so the file still compiles against
 * every backend even though it only runs on web. It needs no renderer, so it injects a no-op
 * {@link RecordingBridge} before the Display is created — {@code Display.init()}
 * then skips starting a real {@code WebFlutterServer} (see {@code DisplayBridge.initForDisplay})
 * and its {@code RecordingComm} captures the {@code comm().on(...)} handlers widgets register, so a
 * Dart&rarr;Java event can be fired by invoking the captured callback.
 */
@Tag("flutter-it")
class DisplayWakeFlutterTest {

    /** How long a wake/dispatch may take and still count as "prompt" (vs. a hang). */
    private static final long WAKE_TIMEOUT_MS = 1000;
    /** Idle window for the indefinite-park check: far beyond any cap we'd ever set. */
    private static final long IDLE_PARK_MS = 150;

    private volatile RecordingBridge bridge;
    private volatile Display display;

    /** The UI thread: it creates the Display and runs the event loop, like a real application. */
    private Thread uiThread;
    private volatile boolean running;
    private volatile Throwable uiError;
    /** Bumped by the {@link SWT#PreExternalEventDispatch} listener each time the loop parks. */
    private final AtomicInteger parks = new AtomicInteger();
    /** Bumped by the {@link SWT#PostExternalEventDispatch} listener each time the loop wakes. */
    private final AtomicInteger wakes = new AtomicInteger();

    @BeforeEach
    void setUp() throws InterruptedException {
        running = true;
        CountDownLatch ready = new CountDownLatch(1);
        uiThread = new Thread(() -> {
            try {
                RecordingBridge b = new RecordingBridge();
                FlutterBridge.set(b); // makes initForDisplay skip the WebFlutterServer
                bridge = b;
                Display d = new Display(); // this thread is now the Display's UI thread
                // Park/wake probes (public API), hooked on the UI thread before the loop runs.
                d.addListener(SWT.PreExternalEventDispatch, e -> parks.incrementAndGet());
                d.addListener(SWT.PostExternalEventDispatch, e -> wakes.incrementAndGet());
                display = d;
            } catch (Throwable t) {
                uiError = t;
                ready.countDown();
                return;
            }
            ready.countDown();
            try {
                while (running) {
                    while (display.readAndDispatch()) {
                        /* drain all pending work before parking */
                    }
                    display.sleep(); // parks until another thread releases us
                }
            } finally {
                if (!display.isDisposed())
                    display.dispose();
                FlutterBridge.set(null);
            }
        }, "ui-event-loop");
        uiThread.setDaemon(true);
        uiThread.start();

        assertThat(ready.await(5, TimeUnit.SECONDS)).as("UI thread should create the Display").isTrue();
        if (uiError != null)
            throw new IllegalStateException("UI thread failed to start", uiError);
        awaitLoopParked(); // wait until the loop is actually blocked in sleep()
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        running = false;
        Display d = display;
        if (d != null && !d.isDisposed())
            d.asyncExec(() -> {}); // unpark from this (non-UI) thread so the loop sees running == false
        if (uiThread != null)
            uiThread.join(5000);
    }

    /** Run {@code work} on the UI thread and wait for it (widget creation must happen there). */
    private void onUiThread(Runnable work) {
        display.syncExec(work);
    }

    /**
     * Block until the UI thread is actually parked inside {@code sleep()}. The loop is parked exactly
     * when it has begun one more park than it has finished waking ({@code parks == wakes + 1}); we also
     * require the thread to be blocked ({@link Thread.State#WAITING} on the wake semaphore), which
     * confirms it is past {@code drainPermits()} and in {@code acquire()} — so a subsequent permit-only
     * wake (dirty) cannot be drained out from under it.
     */
    private void awaitLoopParked() throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(WAKE_TIMEOUT_MS);
        while (System.nanoTime() < deadline) {
            if (parks.get() == wakes.get() + 1 && uiThread.getState() == Thread.State.WAITING)
                return;
            Thread.sleep(1);
        }
        assertThat(parks.get() - wakes.get())
                .as("event loop should be parked in sleep() (parks == wakes + 1)")
                .isEqualTo(1);
        assertThat(uiThread.getState())
                .as("UI thread should be blocked in sleep()")
                .isEqualTo(Thread.State.WAITING);
    }

    /** Block until the loop has woken more than {@code baseline} times (or fail after the timeout). */
    private void awaitWoken(int baseline) throws InterruptedException {
        assertThat(awaitCount(wakes, baseline))
                .as("a wake source should release the parked loop promptly")
                .isTrue();
    }

    private static boolean awaitCount(AtomicInteger counter, int baseline) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(WAKE_TIMEOUT_MS);
        while (counter.get() <= baseline) {
            if (System.nanoTime() > deadline)
                return false;
            Thread.sleep(1);
        }
        return true;
    }

    private static boolean awaitLatch(CountDownLatch latch) throws InterruptedException {
        return latch.await(WAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Test
    void asyncExec_wakesAndDispatches() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        display.asyncExec(ran::countDown); // from the test thread → wakes the parked UI thread

        assertThat(awaitLatch(ran))
                .as("asyncExec must wake the parked loop and run the runnable")
                .isTrue();
    }

    @Test
    void syncExec_wakesAndDispatches() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        CountDownLatch returned = new CountDownLatch(1);
        // syncExec from this non-UI thread queues the runnable, wakes the loop, then blocks until the
        // loop has run it — so the worker returning proves both the wake and the dispatch. Run it on a
        // worker so a missed wake fails the assertion instead of hanging the test thread.
        Thread worker = new Thread(() -> {
            display.syncExec(ran::countDown);
            returned.countDown();
        }, "syncExec-worker");
        worker.start();

        assertThat(awaitLatch(ran)).as("syncExec runnable must run on the loop").isTrue();
        assertThat(awaitLatch(returned)).as("syncExec must return after the runnable runs").isTrue();
    }

    @Test
    void eventFromDart_selection_wakesAndNotifiesListener() throws Exception {
        CountDownLatch selected = new CountDownLatch(1);
        onUiThread(() -> {
            Button button = new Button(new Shell(display), SWT.PUSH);
            button.addListener(SWT.Selection, e -> selected.countDown());
        });
        awaitLoopParked(); // loop re-parked after the syncExec setup

        // A real Dart Button registers its Selection handler on the comm; firing it from here (the
        // test thread, standing in for the comm thread) simulates the Dart client reporting a click.
        // The handler marshals to the UI thread via asyncExec, which wakes the loop; the loop then
        // dispatches it and the Selection listener fires.
        bridge.comm.fireContaining("Selection/Selection", new Event());

        assertThat(awaitLatch(selected))
                .as("a Selection event from Dart must wake the loop and reach the listener")
                .isTrue();
    }

    @Test
    void inputEventFromDart_mouseMove_wakesAndNotifiesListener() throws Exception {
        CountDownLatch moved = new CountDownLatch(1);
        // A single control: every Control registers a MouseMove handler, and fireContaining fires the
        // first match, so listening on the only control keeps the fired handler unambiguous.
        onUiThread(() -> {
            Shell shell = new Shell(display);
            shell.addListener(SWT.MouseMove, e -> moved.countDown());
        });
        awaitLoopParked();

        // Input events (here MouseMove) take the same comm -> asyncExec -> dispatch path.
        bridge.comm.fireContaining("MouseMove/MouseMove", new Event());

        assertThat(awaitLatch(moved))
                .as("a MouseMove input event from Dart must wake the loop and reach the listener")
                .isTrue();
    }

    @Test
    void dirty_wakesDisplayThread() throws Exception {
        // Marking a widget dirty must wake the loop so the dirty set flushes to Dart at the next
        // readAndDispatch; without it an off-thread dirty would never flush while the loop is parked.
        // Unlike the other sources, "dirty" has no public API surface and no dispatched event to
        // observe: it is the web bridge override WebDisplayBridge.wakeForDirty() that does the
        // wake. That class lives only in the web backend, so the test reaches it by reflection (so
        // this file still compiles against every backend) and observes the loop leaving sleep().
        AtomicReference<Object> widgetImpl = new AtomicReference<>();
        onUiThread(() -> widgetImpl.set(new Button(new Shell(display), SWT.PUSH).getImpl()));
        Object webBridge = newWebBridge();
        awaitLoopParked();
        int wokenBefore = wakes.get();

        markDirty(webBridge, widgetImpl.get()); // an off-UI-thread dirty

        awaitWoken(wokenBefore);
    }

    /** Construct the web-only {@code WebDisplayBridge} for this Display by reflection. */
    private Object newWebBridge() throws Exception {
        Class<?> dartDisplay = Class.forName("org.eclipse.swt.widgets.DartDisplay");
        Class<?> webBridge = Class.forName("org.eclipse.swt.widgets.WebDisplayBridge");
        Constructor<?> ctor = webBridge.getDeclaredConstructor(dartDisplay);
        ctor.setAccessible(true);
        return ctor.newInstance(display.getImpl());
    }

    /** Invoke {@code FlutterBridge.dirty(DartWidget)} on the web bridge by reflection. */
    private static void markDirty(Object webBridge, Object widgetImpl) throws Exception {
        Class<?> dartWidget = Class.forName("org.eclipse.swt.widgets.DartWidget");
        Method dirty = webBridge.getClass().getMethod("dirty", dartWidget);
        dirty.invoke(webBridge, widgetImpl);
    }

    /** Install {@code webBridge} as this Display's {@code displayBridge} via reflection (native-only type). */
    private void setDisplayBridge(Object webBridge) {
        try {
            Class<?> dartDisplay = Class.forName("org.eclipse.swt.widgets.DartDisplay");
            Class<?> displayBridge = Class.forName("org.eclipse.swt.widgets.DisplayBridge");
            Method m = dartDisplay.getDeclaredMethod("setBridge", displayBridge);
            m.setAccessible(true);
            m.invoke(display.getImpl(), webBridge);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Regression for #758: a runnable posted while the queue is <b>non-empty</b> must still wake the
     * UI thread. The other tests here only ever post <em>one</em> runnable to a freshly-parked,
     * <em>empty</em> queue — the empty&rarr;non-empty transition, which even the old
     * {@code addLast} (that woke <em>only</em> on that transition) handled. The bug lived in the path
     * they never exercised: a second post landing on a non-empty queue. There the old code computed
     * {@code wake = messages.isEmpty()} as {@code false} and skipped {@code wakeThread()}; racing the
     * consumer draining and re-parking (with {@code drainPermits()} in {@code sleep()}), that wake was
     * lost and a parked nested modal loop hung — the actual JFace-dialog button failure.
     *
     * <p>Reproduced deterministically without relying on the race window: the UI thread is pinned
     * inside a blocking runnable, so it is <b>not</b> in {@code sleep()} and never consumes wake
     * permits. Each posted runnable's {@code wakeThread()} therefore accumulates as an observable
     * permit on {@code _wakeSignal}. We post one runnable to the (empty) queue, then a second while the
     * first is still queued (non-empty), and assert <b>both</b> posts released a permit. The old code
     * released only the first; the fix releases both.
     */
    @Test
    void postToNonEmptyQueue_stillWakesUiThread() throws Exception {
        CountDownLatch pinned = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        // GATE pins the UI thread mid-dispatch: popped off the queue (queue empty again) but not
        // returning, so the loop is busy — not parked — and touches no permits.
        display.asyncExec(() -> {
            pinned.countDown();
            try {
                release.await(WAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(awaitLatch(pinned)).as("UI thread should be pinned inside the runnable").isTrue();

        try {
            int base = wakePermits();

            display.asyncExec(() -> {}); // post #1 → empty queue → wakes (both old and new)
            int afterEmpty = wakePermits();
            assertThat(afterEmpty)
                    .as("a post to an empty queue must issue a wake")
                    .isEqualTo(base + 1);

            display.asyncExec(() -> {}); // post #2 → queue now non-empty → the previously-dropped wake
            int afterNonEmpty = wakePermits();
            assertThat(afterNonEmpty)
                    .as("a post to a NON-EMPTY queue must also wake (regression #758)")
                    .isEqualTo(afterEmpty + 1);
        } finally {
            release.countDown(); // let the loop drain and re-park so tearDown is clean
        }
    }

    /** Read the current permit count of the UI thread's wake semaphore ({@code _wakeSignal}). */
    private int wakePermits() throws Exception {
        Object dartDisplay = display.getImpl();
        java.lang.reflect.Field f = dartDisplay.getClass().getDeclaredField("_wakeSignal");
        f.setAccessible(true);
        return ((java.util.concurrent.Semaphore) f.get(dartDisplay)).availablePermits();
    }

    /**
     * Regression for the <em>other</em> half of the wake invariant — the consumer side. The
     * {@code addLast} fix restores "every producer signals" for the message queue, but a second
     * lost-wakeup lurks on the <b>dirty</b> condition: {@code sleep()} decides whether to park by
     * re-checking only {@code isMessagesEmpty()} — it never re-checks the dirty set. A dirty produced
     * off the UI thread <em>does</em> signal ({@code dirty(w)} adds to the set then calls
     * {@code wakeForDirty()}), but if that permit lands in the window before {@code sleep()}'s
     * {@code drainPermits()}, the permit is discarded and the loop parks with the widget still dirty.
     * In pure web there is no cap (see {@link #idleLoop_parksIndefinitely_untilWoken}), so the frame
     * never flushes until some unrelated event happens to wake the loop. The {@code addLast} fix does
     * nothing for this — it is a different condition, broken on the consumer side.
     *
     * <p>The window is hit deterministically through the public {@link SWT#PreExternalEventDispatch}
     * hook, which {@code sleep()} fires on the UI thread <b>after</b> its fast-path check and
     * <b>before</b> {@code drainPermits()}. Inside it we mark a widget dirty from a worker thread and
     * block until that dirty's {@code wakeForDirty()} has run — so the permit provably exists when
     * {@code drainPermits()} then throws it away. We then assert the loop is still serviced: with the
     * current consumer it parks and {@code sleep()} never returns, so this test <b>fails</b>; once the
     * park guard also consults the dirty set, {@code sleep()} sees the pending dirty and returns.
     */
    @Test
    void dirtyArrivingInParkWindow_isNotLost() throws Exception {
        Object webBridge = newWebBridge();
        AtomicReference<Object> widgetImpl = new AtomicReference<>();
        // Install the WebDisplayBridge as this Display's bridge — the production web shape sleep()
        // consults (displayBridge.hasDirty()). The harness normally leaves displayBridge null.
        onUiThread(() -> {
            widgetImpl.set(new Button(new Shell(display), SWT.PUSH).getImpl());
            setDisplayBridge(webBridge);
        });

        AtomicBoolean armed = new AtomicBoolean(false);
        AtomicBoolean injectedOnce = new AtomicBoolean(false);
        AtomicInteger wakesAtInjection = new AtomicInteger();
        CountDownLatch injected = new CountDownLatch(1);
        // Fires on the UI thread inside sleep(), after the fast-path check and before drainPermits().
        Listener prePark = e -> {
            if (armed.get() && injectedOnce.compareAndSet(false, true)) {
                wakesAtInjection.set(wakes.get());
                CountDownLatch released = new CountDownLatch(1);
                // Mark dirty off the UI thread; wakeForDirty() releases a wake permit for this Display.
                Thread w = new Thread(() -> {
                    try {
                        markDirty(webBridge, widgetImpl.get());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        released.countDown();
                    }
                }, "dirty-injector");
                w.start();
                try {
                    // Return (→ drainPermits) only once the permit is guaranteed to exist, so the bug's
                    // drainPermits() actually discards it — a real interleaving, not a staged one.
                    released.await(WAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                injected.countDown();
            }
        };
        onUiThread(() -> display.addListener(SWT.PreExternalEventDispatch, prePark));
        awaitLoopParked();

        armed.set(true);
        display.asyncExec(() -> {}); // consumed → empty queue again → next sleep() enters the window

        try {
            assertThat(awaitLatch(injected)).as("dirty should be injected in the park window").isTrue();
            assertThat(awaitCount(wakes, wakesAtInjection.get()))
                    .as("a dirty landing in the drainPermits() window must still be serviced, not lost "
                            + "— sleep() must re-check the dirty set, not only the message queue")
                    .isTrue();
        } finally {
            clearDirtyState(); // stop the fixed-path loop from spinning on the still-dirty widget
        }
    }

    /** Clear {@code FlutterBridge}'s static dirty set by reflection (package-private helper). */
    private static void clearDirtyState() throws Exception {
        Method m = Class.forName("dev.equo.swt.FlutterBridge").getDeclaredMethod("clearDirty");
        m.setAccessible(true);
        m.invoke(null);
    }

    @Test
    void idleLoop_parksIndefinitely_untilWoken() throws Exception {
        int wokenBefore = wakes.get();

        // In pure web mode sleep() has no timeout cap, so an idle loop must stay parked. The wait
        // far exceeds any cap we'd ever set: a woken count here would mean we're still capping.
        Thread.sleep(IDLE_PARK_MS);
        assertThat(wakes.get())
                .as("an idle loop must stay parked (no cap — indefinite park)")
                .isEqualTo(wokenBefore);

        CountDownLatch ran = new CountDownLatch(1);
        display.asyncExec(ran::countDown); // the wake, from this non-UI thread
        assertThat(awaitLatch(ran))
                .as("a parked loop must wake on demand (a missed wake would hang indefinitely)")
                .isTrue();
    }
}
