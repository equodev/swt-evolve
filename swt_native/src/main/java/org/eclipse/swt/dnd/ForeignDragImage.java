package org.eclipse.swt.dnd;

import org.eclipse.swt.widgets.Shell;

/**
 * The drag image and drop cursor of a drag no {@link DragSource} runs, such as a Swing island's:
 * the client drawing the shell under the pointer draws them at the
 * pointer, as it draws a drag of its own. Only the picture: whoever runs the drag delivers it to the
 * drop targets.
 *
 * <p>One per drag, used on the Display's thread.
 */
public final class ForeignDragImage {

    private final byte[] png;
    private final int width;
    private final int height;
    private final int offsetX;
    private final int offsetY;
    private final RemoteDrag.Step step = new RemoteDrag.Step();

    /** The shell drawing the image, or null while none does. */
    private Shell shown;

    /**
     * A drag drawn as {@code png}, {@code width} by {@code height} points with its top-left at
     * {@code offsetX, offsetY} from the pointer; with a null {@code png}, as the cursor alone.
     */
    public ForeignDragImage(byte[] png, int width, int height, int offsetX, int offsetY) {
        this.png = png;
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Shows the drag at the display point {@code x, y} over {@code shell}, with the cursor for
     * {@code operation} ({@code DND.DROP_*}); a null {@code shell} hides it.
     */
    public void show(Shell shell, int x, int y, int operation) {
        if (shell != null && shell.isDisposed()) shell = null;
        if (shell != shown) {
            hide();
            if (shell == null) return;
            shown = shell;
            if (png != null) RemoteDrag.sendImage(shell, png, width, height, offsetX, offsetY);
        }
        if (shell == null) return;
        step.phase = RemoteDrag.FEEDBACK;
        step.operation = operation;
        RemoteDrag.send(shell, step, x, y);
    }

    /** Takes the drag image and its cursor away. */
    public void hide() {
        Shell shell = shown;
        shown = null;
        if (shell == null || shell.isDisposed()) return;
        step.phase = RemoteDrag.LEAVE;
        RemoteDrag.send(shell, step, 0, 0);
    }
}
