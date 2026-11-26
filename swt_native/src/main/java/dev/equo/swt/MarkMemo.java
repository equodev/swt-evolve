package dev.equo.swt;

/**
 * What a property held when it was last marked from clean, which is what the client holds, and
 * whether its widget was waiting for a flush then. A property that comes back to it before the flush
 * takes its mark back ({@link FlutterBridge#markChange}).
 */
public final class MarkMemo {
    Object value;
    boolean queued;
    /** {@link ChangedKeys#clears()} when {@link #value} was taken: it holds only until the next flush. */
    int sent;
}
