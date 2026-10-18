package dev.equo.swt.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows the popups of embedded Swing content — menus, combo lists, tooltips — in an SWT
 * {@link Shell} of their own instead of a native window.
 *
 * <p>Swing only draws a popup inside its owner's window when that window is a {@code JFrame},
 * {@code JDialog} or {@code JWindow}; the embedding's frame is none of those, so every popup
 * becomes a native window, positioned from the frame's location — its canvas's display position.
 * In a browser that position is a page coordinate and the window is not part of the page. A shell
 * at that same display position is where a native {@code SWT_AWT} popup appears, and like one it
 * can extend past the embedding's bounds.
 *
 * <p>Popups whose owner is not inside an embedding go to the factory that was installed before.
 */
final class ShellPopupFactory extends PopupFactory {

    private final PopupFactory fallback;
    private final Function<Component, Canvas> embeddingOf;
    private final BiConsumer<Shell, Component> hostInShell;

    ShellPopupFactory(PopupFactory fallback, Function<Component, Canvas> embeddingOf,
            BiConsumer<Shell, Component> hostInShell) {
        this.fallback = fallback;
        this.embeddingOf = embeddingOf;
        this.hostInShell = hostInShell;
    }

    /**
     * A shell is only built when Swing asks from the embedding's display thread, which is where AWT
     * dispatches once the host's dispatcher is installed; without it Swing and SWT run on different
     * threads and the popup stays native.
     */
    @Override
    public Popup getPopup(Component owner, Component contents, int x, int y) {
        Canvas embedding = owner == null ? null : embeddingOf.apply(owner);
        if (embedding == null || embedding.isDisposed()
                || embedding.getDisplay().getThread() != Thread.currentThread()) {
            return fallback.getPopup(owner, contents, x, y);
        }
        return new ShellPopup(embedding, contents, x, y, hostInShell);
    }

    /**
     * One showing of a popup. AWT hands it display coordinates, because the embedding's frame sits
     * at its canvas's display position.
     */
    static final class ShellPopup extends Popup {
        private final Canvas embedding;
        private final Display display;
        private final Component contents;
        private final int x;
        private final int y;
        private final BiConsumer<Shell, Component> hostInShell;
        private Shell shell;

        ShellPopup(Canvas embedding, Component contents, int x, int y, BiConsumer<Shell, Component> hostInShell) {
            this.embedding = embedding;
            this.display = embedding.getDisplay();
            this.contents = contents;
            this.x = x;
            this.y = y;
            this.hostInShell = hostInShell;
        }

        @Override
        public void show() {
            if (shell != null || embedding.isDisposed() || !onDisplayThread()) return;
            Dimension size = contents.getPreferredSize();
            shell = new Shell(embedding.getShell(), SWT.NO_TRIM | SWT.ON_TOP | SWT.TOOL);
            shell.setBounds(x, y, Math.max(1, size.width), Math.max(1, size.height));
            hostInShell.accept(shell, contents);
            shell.setVisible(true);
        }

        /** Swing calls this on the thread it showed the popup from; the dispose is queued if not. */
        @Override
        public void hide() {
            Container parent = contents.getParent();
            if (parent != null) parent.remove(contents);
            Shell showing = shell;
            shell = null;
            if (showing == null) return;
            if (onDisplayThread()) {
                if (!showing.isDisposed()) showing.dispose();
            } else if (!display.isDisposed()) {
                display.asyncExec(() -> {
                    if (!showing.isDisposed()) showing.dispose();
                });
            }
        }

        Shell shell() {
            return shell;
        }

        private boolean onDisplayThread() {
            return display.getThread() == Thread.currentThread();
        }
    }
}
