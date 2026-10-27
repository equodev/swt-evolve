package dev.equo.swt;

import org.eclipse.swt.graphics.Rectangle;

/**
 * A host window standing in for one {@link org.eclipse.swt.widgets.Shell} that {@link WindowPolicy}
 * put in a window of its own — a second native top-level window on the desktop surface, a second
 * browser window on the web one.
 *
 * <p>Each is backed by its own Flutter client, rooted at that shell rather than at the Display, and
 * connected to the same per-Display comm. Sharing the comm is what keeps every window inside one
 * {@code Display}: a shell in a detached window is still an ordinary shell of that Display, and its
 * widgets reach the client over the channels they already use.
 */
public interface ShellWindow {

    int STATE_NORMAL = 0;
    int STATE_MAXIMIZED = 1;
    int STATE_MINIMIZED = 2;
    int STATE_FULLSCREEN = 3;

    void setTitle(String title);

    void setBounds(Rectangle bounds);

    /** One of the {@code STATE_*} constants. */
    void setState(int state);

    /**
     * Shows or hides the window without taking it down.
     *
     * <p>A hidden shell must stop showing a window, but closing one is the wrong answer: a toolkit
     * hides and re-shows a shell freely while it lays out (the Eclipse workbench does it to a
     * detached part), and answering each flip with a rebuild costs a fresh engine, the window's
     * position, and a client that has to be described from scratch.
     */
    void setVisible(boolean visible);

    /**
     * Where this window's content sits on screen, or null when the platform cannot say.
     *
     * <p>Java places a window but never hears where it ended up: a window manager may put it
     * elsewhere, and the user may drag it. Without this the shell keeps the position Java asked for,
     * and every screen coordinate derived from it -- a popup's location, a drop target -- is off by
     * however far the window really is.
     */
    default org.eclipse.swt.graphics.Point origin() {
        return null;
    }

    /** Takes the window down. Called once, when the shell stops being hosted in one. */
    void close();

    /** False once the window is gone, so nothing is forwarded to it and it is dropped from the registry. */
    boolean isAlive();
}
