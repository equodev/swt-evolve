package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Event;


/**
 * Keeps a drag's moves from queueing up behind the work they cause, so what is drawn follows the
 * pointer instead of replaying where it was.
 *
 * <p>Two queues can fill. Here, the UI thread's: a move that reaches Java while the one before it,
 * to the same control, is still waiting for the UI thread, and nothing else has been reported in
 * between, replaces that one's position instead of queueing a move of its own, as GTK compresses
 * motion events and Windows WM_MOUSEMOVE. Only consecutive moves merge: any other event the client
 * reports in between (a press, a release, a key, a crossing) ends the run, so nothing is reordered
 * with the moves around it. Paint requests do not end it: when the application paints is not
 * ordered with the pointer.
 *
 * <p>And the client's: a move Java handles in a few milliseconds can relayout a whole page, which
 * the client takes far longer to apply, so moves answered as fast as Java handles them pile their
 * updates up in front of the client. A client that asks ({@link #CHANNEL}, once) is told, on the
 * same channel, when the updates of the moves handled since have been sent, and holds its next move
 * until then: one move in flight, always the latest.
 *
 * <p>A drag and drop's DragOver is merged the same way: the client reports one for each pointer
 * event over the drop target, and a drop listener that validates the drop (a project explorer asks
 * its drop assistants) can take longer than the pointer takes to report the next. It is not
 * answered: the client sends the next one when the pointer moves, held or not. A drag reports the
 * pointer on three channels at once (the moves over the control, the DragOvers over its drop target,
 * the drag source's own moves), so the moves and the DragOvers each keep a run of their own, and
 * none of the three ends another's: they all say where the same pointer is.
 */
public final class MoveCompression {

    static final String MOVE = "MouseMove/MouseMove";
    static final String DRAG_OVER = "Drop/dragOver";
    private static final String PAINT = "Paint/Paint";
    /** Where a drag started here is, for a drop target in another window: ends no run. */
    private static final String DRAG_MOVE = "DragDetect/dragMove";
    static final int CHANNEL = WireTable.CHANNEL_SWT_EVOLVE_MOVE_HANDLED;

    /** The move and the DragOver this comm last reported, while each waits for the UI thread. */
    private final Queued[] runs = new Queued[2];
    /** The client asked to hear when its moves are handled. */
    private boolean asked;

    /** Each comm keeps one ({@link CommService#moves()}); its state is guarded by the instance. */
    public MoveCompression() {
    }

    private static final class Queued {
        final DartWidget widget;
        final Event event;

        Queued(DartWidget widget, Event event) {
            this.widget = widget;
            this.event = event;
        }
    }

    private static MoveCompression of(CommService comm) {
        return comm != null ? comm.moves() : null;
    }

    /**
     * Called on the comm's thread for each client event before it is delivered. True when
     * {@code event} was merged into the move queued before it, so it must not be delivered.
     * A move that is not merged is recorded as queued: it must be delivered after this returns.
     */
    static boolean merge(CommService comm, DartWidget widget, String channel, Event event) {
        if (PAINT.equals(channel) || DRAG_MOVE.equals(channel)) return false;
        MoveCompression moves = of(comm);
        if (moves == null) return false;
        synchronized (moves) {
            int run = MOVE.equals(channel) ? 0 : DRAG_OVER.equals(channel) ? 1 : -1;
            if (run < 0 || event == null) {
                moves.clear();
                return false;
            }
            Queued queued = moves.runs[run];
            if (queued != null && queued.widget == widget) {
                copy(event, queued.event);
                return true;
            }
            // Recorded before the move is handed to the UI thread, so it is never taken unrecorded.
            moves.runs[run] = new Queued(widget, event);
            return false;
        }
    }

    /** Called on the comm's thread for an event delivered past {@link #merge}: it ends the run of moves. */
    static void interrupt(CommService comm) {
        MoveCompression moves = of(comm);
        if (moves == null) return;
        synchronized (moves) {
            moves.clear();
        }
    }

    /**
     * Called on the UI thread as {@code widget} handles a move, before its fields are read: the next
     * move queues one of its own.
     */
    public static void taken(DartWidget widget, Event event) {
        CommService comm = FlutterBridge.commFor(widget);
        MoveCompression moves = of(comm);
        if (moves == null) return;
        boolean answer;
        synchronized (moves) {
            moves.forget(event);
            answer = moves.asked;
        }
        if (!answer) return;
        FlutterBridge.oweMoveAnswer(comm);
        if (Trace.on()) Trace.mark("Java.moveTaken");
    }

    /**
     * Called on the UI thread as {@code target} handles a DragOver, before its fields are read: the
     * next one queues one of its own.
     */
    public static void dragOverTaken(DartWidget target, Event event) {
        MoveCompression moves = of(FlutterBridge.commFor(target));
        if (moves == null) return;
        synchronized (moves) {
            moves.forget(event);
        }
    }

    /** The client on {@code comm} asked to be told when its moves are handled. */
    public static void asked(CommService comm) {
        MoveCompression moves = of(comm);
        if (moves == null) return;
        synchronized (moves) {
            moves.asked = true;
        }
    }

    /**
     * Called on the UI thread once a flush has sent what it had: tells each client with a move
     * handled on this thread since its last answer that the updates of its moves are on their way.
     */
    static void answer() {
        CommService[] owed = FlutterBridge.takeMoveAnswers();
        if (owed == null) return;
        if (Trace.on()) Trace.mark("Java.moveAnswer");
        for (CommService comm : owed) FlutterBridge.sendMoveHandled(comm);
    }

    private void clear() {
        runs[0] = null;
        runs[1] = null;
    }

    /** {@code event} is no longer waiting. */
    private void forget(Event event) {
        for (int i = 0; i < runs.length; i++) {
            if (runs[i] != null && runs[i].event == event) runs[i] = null;
        }
    }

    /** What a move reports, from the later one onto the queued one. */
    private static void copy(Event from, Event to) {
        to.x = from.x;
        to.y = from.y;
        to.stateMask = from.stateMask;
        to.time = from.time;
        to.button = from.button;
        to.count = from.count;
        to.detail = from.detail;
        // Where a DragOver is: the row or tab under the pointer.
        to.index = from.index;
        to.itemId = from.itemId;
    }
}
