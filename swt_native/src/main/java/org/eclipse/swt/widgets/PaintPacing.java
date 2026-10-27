package org.eclipse.swt.widgets;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /** Sent by the client with {@code {"id": <control's id>}} when the frame showing its Paint begins. */
    static final String IN_FRAME = "GC/inFrame";

    static final int TIMEOUT_MS = 100;

    /** Paints of one control the client may have yet to take into a frame. */
    static final int IN_FLIGHT = 1;

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*(-?\\d+)");

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

        Pacer(Display display) {
            this.display = display;
        }
    }

    /**
     * One per connection: its single {@value #IN_FRAME} handler, routing each answer to the Pacer of
     * the Display whose control it names. Displays can share a connection (every desktop bridge
     * does), and a connection keeps one handler per message.
     */
    private static final class Router {
        final Set<Pacer> pacers = ConcurrentHashMap.newKeySet();

        void answered(byte[] bytes) {
            if (bytes == null) return;
            Matcher m = ID.matcher(new String(bytes, StandardCharsets.UTF_8));
            if (!m.find()) return;
            int id = Integer.parseInt(m.group(1));
            for (Pacer pacer : pacers) {
                DartControl shown = pacer.byId.get(id);
                if (shown != null && !pacer.display.isDisposed()) {
                    pacer.display.asyncExec(() -> shown(shown));
                    return;
                }
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
        long now = System.nanoTime();
        // A Paint the client never answered for stops counting once it has been waited on long enough.
        while (!sent.isEmpty() && now - sent.peekFirst() >= TIMEOUT_MS * 1_000_000L) sent.pollFirst();
        if (sent.size() < IN_FLIGHT) return false;
        long oldest = sent.peekFirst();
        state.pacer.held.add(c);
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
        state.inFlight.addLast(System.nanoTime());
        // Nothing of a repeat reached the client, so it is asked to answer for its next frame.
        if (repeat) comm.send("GC/" + id + "/nextFrame");
    }

    /** The Paint [drawable] is finishing repeats what its client shows and is not being sent. */
    public static void unchanged(Object drawable) {
        DartControl c = dartImpl(drawable);
        if (c != null) ControlHelper.paintState(c).unchanged = true;
    }

    private static Pacer pacer(Display display) {
        Pacer existing = existingPacer(display);
        if (existing != null) return existing;
        Pacer pacer = new Pacer(display);
        display.setData(PACER_KEY, pacer);
        // A connection outliving the Display must not keep routing to it, nor keep it reachable.
        display.disposeExec(() -> {
            for (AbstractBinaryCommService comm : pacer.listening) router(comm).pacers.remove(pacer);
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
            comm.on(IN_FRAME, byte[].class, router::answered);
            return router;
        });
    }

    private static void listen(Pacer pacer, AbstractBinaryCommService comm) {
        if (pacer.listening.add(comm)) router(comm).pacers.add(pacer);
    }

    /** The client took [c]'s oldest Paint in flight into a frame: a Paint held for room goes out. */
    private static void shown(DartControl c) {
        ControlHelper.paintState(c).inFlight.pollFirst();
        release(c);
    }

    /** Waiting on [c]'s Paint sent at [sent] timed out; one answered meanwhile has already made room. */
    private static void timedOut(DartControl c, long sent) {
        ControlHelper.paintState(c).inFlight.remove(sent);
        release(c);
    }

    /** Sends [c]'s held Paint if it now has room. */
    private static void release(DartControl c) {
        PaintState state = ControlHelper.paintState(c);
        if (state.inFlight.size() >= IN_FLIGHT) return;
        if (state.pacer == null || !state.pacer.held.remove(c)) return;
        if (!c.isDisposed()) ControlHelper.dispatchHeldPaint(c);
    }

    /**
     * Called with the state a flush is about to send. Anything in it besides a held control's own
     * drawing releases every Paint its Display holds, so a control that repaints to follow another
     * (a ruler following its editor's scroll) is not shown a frame behind it. Another Display's
     * state leaves them held.
     */
    public static void flushing(Collection<Object> states) {
        Display current = Display.getCurrent();
        if (current != null && current.isDisposed()) current = null;
        Pacer currentPacer = current == null ? null : existingPacer(current);
        Set<Pacer> released = new HashSet<>();
        for (Object state : states) {
            Object owner = state instanceof DartGC ? ((DartGC) state)._drawable()
                    : state instanceof DartWidget ? ((DartWidget) state).getApi() : null;
            Pacer pacer = pacerOf(owner, current, currentPacer);
            if (pacer == null || released.contains(pacer) || pacer.held.isEmpty()) continue;
            DartControl c = dartImpl(owner);
            if (c == null || !pacer.held.contains(c)) {
                released.add(pacer);
                releaseAll(pacer);
            }
        }
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

    private static void releaseAll(Pacer pacer) {
        DartControl[] all;
        synchronized (pacer.held) {
            all = pacer.held.toArray(new DartControl[0]);
            pacer.held.clear();
        }
        for (DartControl c : all) {
            if (!c.isDisposed()) ControlHelper.dispatchHeldPaint(c);
        }
    }
}
