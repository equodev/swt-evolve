package org.eclipse.swt.widgets;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.DartGC;

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

    /** Sent by the client with {@code {"id": <control's id>}} when the frame showing its Paint begins. */
    static final String IN_FRAME = "GC/inFrame";

    static final int TIMEOUT_MS = 100;

    /** Paints of one control the client may have yet to take into a frame. */
    static final int IN_FLIGHT = 1;

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(-?\\d+)");

    /** Per control, when each Paint the client has yet to show was sent, oldest first. */
    private static final Map<DartControl, ArrayDeque<Long>> inFlight = new WeakHashMap<>();
    private static final Map<Integer, WeakReference<DartControl>> byId = Collections.synchronizedMap(new HashMap<>());
    /** Controls with a Paint waiting on the client. */
    private static final Set<DartControl> held = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));
    /** Controls whose last Paint repeated what the client shows, so it was not sent. */
    private static final Set<Control> unchanged = Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));
    private static final Set<CommService> listening = Collections.newSetFromMap(new WeakHashMap<>());

    private PaintPacing() {
    }

    /** Whether [c]'s next Paint has to wait for its client; if so, an answer (or the timeout) sends it. */
    static boolean holds(DartControl c) {
        long oldest;
        synchronized (inFlight) {
            ArrayDeque<Long> sent = inFlight.get(c);
            if (sent == null) return false;
            long now = System.nanoTime();
            // A Paint the client never answered for stops counting once it has been waited on long enough.
            while (!sent.isEmpty() && now - sent.peekFirst() >= TIMEOUT_MS * 1_000_000L) sent.pollFirst();
            if (sent.size() < IN_FLIGHT) return false;
            oldest = sent.peekFirst();
        }
        held.add(c);
        int remaining = (int) Math.max(1, TIMEOUT_MS - (System.nanoTime() - oldest) / 1_000_000);
        c.getDisplay().timerExec(remaining, () -> timedOut(c, oldest));
        return true;
    }

    /** Records that [c] painted, so its client's answer for this Paint is waited on. */
    static void painted(DartControl c) {
        boolean repeat = unchanged.remove(c.getApi());
        // The client answers on the Display's connection; a widget's own bridge may be one nothing reads.
        CommService comm = FlutterBridge.resolveDisplayGcComm(c.getDisplay());
        if (comm == null) comm = FlutterBridge.commFor(c.getApi());
        // Until a client connects there is no frame to wait for, and nothing to answer.
        if (!(comm instanceof AbstractBinaryCommService) || !((AbstractBinaryCommService) comm).hasClient()) return;
        listen(comm, c.getDisplay());
        int id = c.getApi().hashCode();
        byId.put(id, new WeakReference<>(c));
        synchronized (inFlight) {
            inFlight.computeIfAbsent(c, k -> new ArrayDeque<>()).addLast(System.nanoTime());
        }
        // Nothing of a repeat reached the client, so it is asked to answer for its next frame.
        if (repeat) comm.send("GC/" + id + "/nextFrame");
    }

    /** The Paint [drawable] is finishing repeats what its client shows and is not being sent. */
    public static void unchanged(Object drawable) {
        if (drawable instanceof Control) unchanged.add((Control) drawable);
    }

    private static void listen(CommService comm, Display display) {
        synchronized (listening) {
            if (!listening.add(comm)) return;
        }
        comm.on(IN_FRAME, byte[].class, bytes -> {
            if (bytes == null) return;
            Matcher m = ID.matcher(new String(bytes, StandardCharsets.UTF_8));
            if (!m.find()) return;
            WeakReference<DartControl> ref = byId.get(Integer.parseInt(m.group(1)));
            DartControl shown = ref == null ? null : ref.get();
            if (shown != null && !display.isDisposed()) display.asyncExec(() -> shown(shown));
        });
    }

    /** The client took [c]'s oldest Paint in flight into a frame: a Paint held for room goes out. */
    private static void shown(DartControl c) {
        synchronized (inFlight) {
            ArrayDeque<Long> sent = inFlight.get(c);
            if (sent != null) sent.pollFirst();
        }
        release(c);
    }

    /** Waiting on [c]'s Paint sent at [sent] timed out; one answered meanwhile has already made room. */
    private static void timedOut(DartControl c, long sent) {
        synchronized (inFlight) {
            ArrayDeque<Long> inFlightOf = inFlight.get(c);
            if (inFlightOf != null) inFlightOf.remove(sent);
        }
        release(c);
    }

    /** Sends [c]'s held Paint if it now has room. */
    private static void release(DartControl c) {
        synchronized (inFlight) {
            ArrayDeque<Long> sent = inFlight.get(c);
            if (sent != null && sent.size() >= IN_FLIGHT) return;
        }
        if (!held.remove(c)) return;
        if (!c.isDisposed()) ControlHelper.dispatchHeldPaint(c);
    }

    /**
     * Called with the state a flush is about to send. Anything in it besides a held control's own
     * drawing releases every held Paint, so a control that repaints to follow another (a ruler
     * following its editor's scroll) is not shown a frame behind it.
     */
    public static void flushing(Collection<Object> states) {
        if (held.isEmpty()) return;
        Set<Control> waiting = new HashSet<>();
        synchronized (held) {
            for (DartControl c : held) waiting.add(c.getApi());
        }
        for (Object state : states) {
            Object owner = state instanceof DartGC ? ((DartGC) state)._drawable()
                    : state instanceof DartControl ? ((DartControl) state).getApi() : null;
            if (owner == null || !waiting.contains(owner)) {
                releaseAll();
                return;
            }
        }
    }

    private static void releaseAll() {
        DartControl[] all;
        synchronized (held) {
            all = held.toArray(new DartControl[0]);
            held.clear();
        }
        for (DartControl c : all) {
            if (!c.isDisposed()) ControlHelper.dispatchHeldPaint(c);
        }
    }
}
