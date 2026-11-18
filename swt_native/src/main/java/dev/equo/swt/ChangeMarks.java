package dev.equo.swt;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DartWidget;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * The compares the generator puts around a widget's setters: a property is read before the setter
 * changes it, and marked when it no longer reads the same. A listener the setter runs can dispose the
 * widget, whose getters then throw, so nothing is read from a disposed widget and it is not marked.
 * The reads are lambdas that capture nothing, so a call allocates nothing for them.
 */
public final class ChangeMarks {

    private ChangeMarks() {
    }

    public static <W extends DartWidget> int readInt(W widget, ToIntFunction<W> read) {
        return widget.isDisposed() ? 0 : read.applyAsInt(widget);
    }

    public static <W extends DartWidget> boolean readBoolean(W widget, Predicate<W> read) {
        return !widget.isDisposed() && read.test(widget);
    }

    public static <W extends DartWidget, T> T read(W widget, Function<W, T> read) {
        return widget.isDisposed() ? null : read.apply(widget);
    }

    public static <W extends DartWidget> void ifChanged(W widget, int key, int was, ToIntFunction<W> now) {
        if (!widget.isDisposed() && was != now.applyAsInt(widget)) widget.getValue().markDirty(key);
    }

    public static <W extends DartWidget> void ifChanged(W widget, int key, boolean was, Predicate<W> now) {
        if (!widget.isDisposed() && was != now.test(widget)) widget.getValue().markDirty(key);
    }

    /** Arrays compare by their elements. */
    public static <W extends DartWidget, T> void ifChanged(W widget, int key, T was, Function<W, T> now) {
        if (!widget.isDisposed() && !java.util.Objects.deepEquals(was, now.apply(widget))) widget.getValue().markDirty(key);
    }

    /** Marks {@code key} when a value about to be stored differs from the one held; arrays compare by their elements. */
    public static void ifDiffers(DartWidget widget, int key, Object held, Object stored) {
        if (!java.util.Objects.deepEquals(held, stored)) widget.getValue().markDirty(key);
    }

    /**
     * Takes back the mark of {@code key} that a change made, when the value came back to what it was and
     * the key was not marked before it ({@link FlutterBridge#takeBack}).
     */
    public static void takeBackIfSame(DartWidget widget, int key, boolean markedBefore, boolean queuedBefore, Object was, Object now) {
        if (!markedBefore && !widget.isDisposed() && java.util.Objects.equals(was, now)) FlutterBridge.takeBack(widget, key, queuedBefore);
    }

    /** {@link FlutterBridge#markChange}, for a widget that is still alive. */
    public static <W extends DartWidget, T> MarkMemo markChange(W widget, int key, T was, Function<W, T> now, MarkMemo memo) {
        return widget.isDisposed() ? memo : FlutterBridge.markChange(widget, key, was, now.apply(widget), memo);
    }

    /** A copy of what the value object handed out, which may be the field the setter fills in place. */
    public static int[] copy(int[] value) {
        return value == null ? null : value.clone();
    }

    public static <T> T[] copy(T[] value) {
        return value == null ? null : value.clone();
    }

    public static Point copy(Point value) {
        return value == null ? null : new Point(value.x, value.y);
    }

    public static Rectangle copy(Rectangle value) {
        return value == null ? null : new Rectangle(value.x, value.y, value.width, value.height);
    }
}
