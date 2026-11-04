package org.eclipse.swt.widgets;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.Image;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.comm.CommService;

/**
 * Paces a control's scheduled Paints to the frames its client shows, the way a native toolkit
 * delivers them with the display's refresh.
 *
 * <p>The client answers ({@value #IN_FRAME}) when the frame that shows a Paint begins, so the next
 * Paint is built while that frame renders and lands in the one after. Until then a Paint a
 * {@code redraw()} asks for waits, and invalidations in between merge into it: more than
 * {@link #IN_FLIGHT} unanswered would put two Paints in one frame, the first never seen. Without
 * pacing a control redrawn from its own paint loop paints as fast as the event loop turns,
 * serializing frames nobody sees and queueing them ahead of the ones that are.
 *
 * <p>Pacing starts only once a client has connected, so a bridge without a client behaves as before,
 * and a Paint that waits longer than {@link #TIMEOUT_MS} goes out anyway: a client that stops
 * answering can slow painting down, never stop it.
 */
public final class PaintPacing {

    /** Sent by the client with the control's id when the frame showing its Paint begins. */
    static final String IN_FRAME = "GC/inFrame";

    static final int TIMEOUT_MS = 100;

    /** Paints of one control the client may have yet to take into a frame. */
    static final int IN_FLIGHT = 1;

    private static final String PACER_KEY = PaintPacing.class.getName();

    /**
     * One per {@link Display}: its controls with a Paint waiting on the client, and the controls its
     * client answers for. A control's own Paints in flight are on its {@link PaintState}.
     */
    static final class Pacer {
        final Display display;
        final Set<DartControl> held = Collections.synchronizedSet(new HashSet<>());
        /** By the id the client answers with; a control leaves when it is disposed. */
        final Map<Integer, DartControl> byId = new ConcurrentHashMap<>();
        final Set<AbstractBinaryCommService> listening = ConcurrentHashMap.newKeySet();
        /** Nanoseconds; what a Paint's age, and so its timeout, is measured against. */
        final LongSupplier clock;

        Pacer(Display display, LongSupplier clock) {
            this.display = display;
            this.clock = clock;
        }
    }

    /**
     * One per connection: its single {@value #IN_FRAME} handler, routing each answer to the Pacer of
     * the Display whose control it names. Displays can share a connection (every desktop bridge
     * does), and a connection keeps one handler per message.
     */
    private static final class Router {
        /** Changed once per Display and comm, read on every answer: replaced whole, read without a lock. */
        private volatile Pacer[] pacers = new Pacer[0];

        synchronized void add(Pacer pacer) {
            Pacer[] next = java.util.Arrays.copyOf(pacers, pacers.length + 1);
            next[pacers.length] = pacer;
            pacers = next;
        }

        synchronized void remove(Pacer pacer) {
            Pacer[] held = pacers;
            for (int i = 0; i < held.length; i++) {
                if (held[i] != pacer) continue;
                Pacer[] next = new Pacer[held.length - 1];
                System.arraycopy(held, 0, next, 0, i);
                System.arraycopy(held, i + 1, next, i, held.length - i - 1);
                pacers = next;
                return;
            }
        }

        void answered(byte[] data, int offset, int length) {
            if (length <= 0) return;
            int id = (int) dev.equo.swt.WireReader.over(data, offset, length).int64();
            Pacer[] all = pacers;
            for (int i = 0; i < all.length; i++) {
                Pacer pacer = all[i];
                DartControl shown = pacer.byId.get(id);
                if (shown == null) continue;
                PaintState state = shown.paintState;
                if (state == null) return;
                // The answer only frees a slot; the UI thread is woken for it only when a Paint waits on it.
                boolean waiting;
                synchronized (state.inFlight) {
                    state.inFlight.pollFirst();
                    waiting = pacer.held.contains(shown);
                }
                if (waiting && !pacer.display.isDisposed()) pacer.display.asyncExec(() -> release(shown));
                return;
            }
        }
    }

    private PaintPacing() {
    }

    /** Whether [c]'s next Paint has to wait for its client; if so, an answer (or the timeout) sends it. */
    static boolean holds(DartControl c) {
        PaintState state = ControlHelper.paintState(c);
        if (state.pacer == null) return false;
        ArrayDeque<Long> sent = state.inFlight;
        long now = state.pacer.clock.getAsLong();
        long oldest;
        synchronized (sent) {
            // A Paint the client never answered for stops counting once it has been waited on long enough.
            while (!sent.isEmpty() && now - sent.peekFirst() >= TIMEOUT_MS * 1_000_000L) sent.pollFirst();
            if (sent.size() < IN_FLIGHT) return false;
            oldest = sent.peekFirst();
            // Under the lock the answer frees the slot with, so it either sees this held or freed the
            // slot before it was checked.
            state.pacer.held.add(c);
        }
        int remaining = (int) Math.max(1, TIMEOUT_MS - (now - oldest) / 1_000_000);
        c.getDisplay().timerExec(remaining, () -> timedOut(c, oldest));
        return true;
    }

    /** Records that [c] painted, so its client's answer for this Paint is waited on. */
    static void painted(DartControl c) {
        PaintState state = ControlHelper.paintState(c);
        boolean repeat = state.unchanged;
        state.unchanged = false;
        Display display = c.getDisplay();
        // The client answers on the Display's connection; a widget's own bridge may be one nothing reads.
        CommService comm = FlutterBridge.resolveDisplayGcComm(display);
        if (comm == null) comm = FlutterBridge.commFor(c.getApi());
        // Until a client connects there is no frame to wait for, and nothing to answer.
        if (!(comm instanceof AbstractBinaryCommService) || !((AbstractBinaryCommService) comm).hasClient()) return;
        int id = c.getApi().hashCode();
        if (state.pacer == null) {
            Pacer pacer = state.pacer = pacer(display);
            c.getApi().addListener(SWT.Dispose, e -> {
                pacer.byId.remove(id, c);
                pacer.held.remove(c);
            });
        }
        Pacer pacer = state.pacer;
        listen(pacer, (AbstractBinaryCommService) comm);
        pacer.byId.put(id, c);
        synchronized (state.inFlight) {
            state.inFlight.addLast(pacer.clock.getAsLong());
        }
        // Nothing of a repeat reached the client, so it is asked to answer for its next frame.
        if (repeat) comm.send(dev.equo.swt.WireTable.TYPE_GC, id, dev.equo.swt.WireTable.CHANNEL_NEXT_FRAME, null);
    }

    /** The Paint [drawable] is finishing repeats what its client shows and is not being sent. */
    public static void unchanged(Object drawable) {
        DartControl c = dartImpl(drawable);
        if (c != null) ControlHelper.paintState(c).unchanged = true;
    }

    private static Pacer pacer(Display display) {
        return pacer(display, System::nanoTime);
    }

    /** [display]'s pacing, made on [clock] if it has none yet. */
    static Pacer pacer(Display display, LongSupplier clock) {
        Pacer existing = existingPacer(display);
        if (existing != null) return existing;
        Pacer pacer = new Pacer(display, clock);
        display.setData(PACER_KEY, pacer);
        // A connection outliving the Display must not keep routing to it, nor keep it reachable.
        display.disposeExec(() -> {
            for (AbstractBinaryCommService comm : pacer.listening) router(comm).remove(pacer);
        });
        return pacer;
    }

    private static Pacer existingPacer(Display display) {
        Object data = display.getData(PACER_KEY);
        return data instanceof Pacer ? (Pacer) data : null;
    }

    private static Router router(AbstractBinaryCommService comm) {
        return comm.attachment(Router.class, () -> {
            Router router = new Router();
            comm.onBytes(0, 0, dev.equo.swt.WireTable.CHANNEL_GC_IN_FRAME, router::answered);
            return router;
        });
    }

    private static void listen(Pacer pacer, AbstractBinaryCommService comm) {
        if (pacer.listening.add(comm)) router(comm).add(pacer);
    }

    /** Waiting on [c]'s Paint sent at [sent] timed out; one answered meanwhile has already made room. */
    private static void timedOut(DartControl c, long sent) {
        ArrayDeque<Long> inFlight = ControlHelper.paintState(c).inFlight;
        synchronized (inFlight) {
            inFlight.remove(sent);
        }
        release(c);
    }

    /** Sends [c]'s held Paint if it now has room. */
    private static void release(DartControl c) {
        PaintState state = ControlHelper.paintState(c);
        synchronized (state.inFlight) {
            if (state.inFlight.size() >= IN_FLIGHT) return;
            if (state.pacer == null || !state.pacer.held.remove(c)) return;
        }
        if (!c.isDisposed()) ControlHelper.dispatchHeldPaint(c);
    }

    /**
     * Called with the state a flush is about to send. Anything in it besides a held control's own
     * drawing releases every Paint its Display holds, so a control that repaints to follow another
     * (a ruler following its editor's scroll) is not shown a frame behind it. Another Display's
     * state leaves them held. Returns whether a Paint ran.
     */
    public static boolean flushing(java.util.List<Object> states) {
        Display current = Display.getCurrent();
        if (current != null && current.isDisposed()) current = null;
        Pacer currentPacer = current == null ? null : existingPacer(current);
        // Nearly always one Display's pacing at most, so a set is made only for a second.
        Pacer releasedFirst = null;
        Set<Pacer> released = null;
        boolean painted = false;
        for (int i = 0; i < states.size(); i++) {
            Object state = states.get(i);
            Object owner = state instanceof DartGC ? ((DartGC) state)._drawable()
                    : state instanceof DartWidget ? ((DartWidget) state).getApi() : null;
            Pacer pacer = pacerOf(owner, current, currentPacer);
            if (pacer == null || pacer == releasedFirst || (released != null && released.contains(pacer))
                    || pacer.held.isEmpty()) continue;
            DartControl c = dartImpl(owner);
            if (c == null || !pacer.held.contains(c)) {
                if (releasedFirst == null) {
                    releasedFirst = pacer;
                } else {
                    if (released == null) released = new HashSet<>();
                    released.add(pacer);
                }
                painted |= releaseAll(pacer);
            }
        }
        return painted;
    }

    /**
     * The pacing of the Display [owner] belongs to. Another thread's Display is read only through a
     * control that already paced; an owner that cannot be placed belongs to the flushing thread's.
     */
    private static Pacer pacerOf(Object owner, Display current, Pacer currentPacer) {
        DartControl c = dartImpl(owner);
        if (c != null && c.paintState != null && c.paintState.pacer != null) return c.paintState.pacer;
        Display display = current;
        if (owner instanceof Widget && !((Widget) owner).isDisposed()) display = ((Widget) owner).getDisplay();
        else if (owner instanceof Image && !((Image) owner).isDisposed() && ((Image) owner).getDevice() instanceof Display)
            display = (Display) ((Image) owner).getDevice();
        return display != null && display == current ? currentPacer : null;
    }

    /** [o]'s Dart implementation when [o] is a Dart-backed control, else null. */
    private static DartControl dartImpl(Object o) {
        return o instanceof Control && ((Control) o).getImpl() instanceof DartControl
                ? (DartControl) ((Control) o).getImpl() : null;
    }

    /** Rounds of {@link #releaseAll}: two controls that redraw each other would otherwise never stop. */
    private static final int RELEASE_ROUNDS = 8;

    private static boolean releaseAll(Pacer pacer) {
        boolean painted = false;
        // A released Paint can redraw another control (an editor's Paint redraws its ruler), and the
        // pacing holds that one too: it follows the same change, so it goes out in this flush as well.
        for (int round = 0; round < RELEASE_ROUNDS; round++) {
            DartControl[] all;
            synchronized (pacer.held) {
                if (pacer.held.isEmpty()) break;
                all = pacer.held.toArray(new DartControl[0]);
                pacer.held.clear();
            }
            boolean paintedNow = false;
            for (DartControl c : all) {
                if (c.isDisposed()) continue;
                if (c.getDisplay().getThread() == Thread.currentThread()) {
                    paintedNow |= ControlHelper.paintHeldNow(c);
                } else {
                    ControlHelper.dispatchHeldPaint(c);
                }
            }
            painted |= paintedNow;
            if (!paintedNow) break;
        }
        return painted;
    }
}
