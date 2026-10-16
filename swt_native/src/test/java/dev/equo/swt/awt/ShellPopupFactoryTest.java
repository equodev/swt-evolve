package dev.equo.swt.awt;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Swing only draws a popup inside its owner's window when that window is a {@code JFrame},
 * {@code JDialog} or {@code JWindow}. An embedding's frame is none of them, so left to Swing every
 * menu, combo list and tooltip it shows becomes a native window, placed at a display coordinate
 * that is a page coordinate in a browser. Those popups have to be shown in an SWT shell instead.
 *
 * <p>The embedding is a mock canvas: a real one needs an AWT display, and CI runs headless. What is
 * under test is which popup the factory hands Swing.
 */
@ExtendWith(Mocks.class)
class ShellPopupFactoryTest {

    private final PopupFactory fallback = mock(PopupFactory.class);
    private final Popup nativePopup = mock(Popup.class);
    private final JButton owner = new JButton("+");
    private final JPopupMenu menu = new JPopupMenu();

    private static Canvas embedding(boolean disposed) {
        Canvas canvas = mock(Canvas.class);
        Display display = Mocks.display();
        when(canvas.isDisposed()).thenReturn(disposed);
        when(canvas.getDisplay()).thenReturn(display);
        return canvas;
    }

    private PopupFactory factory(Canvas embeddingOfOwner) {
        when(fallback.getPopup(any(), any(), anyInt(), anyInt())).thenReturn(nativePopup);
        return new ShellPopupFactory(fallback, c -> c == owner ? embeddingOfOwner : null, (shell, c) -> {});
    }

    @Test
    void aPopupFromAnEmbeddingIsShownInAShell() {
        Popup popup = factory(embedding(false)).getPopup(owner, menu, 9, 50);

        assertThat(popup).isInstanceOf(ShellPopupFactory.ShellPopup.class);
        verify(fallback, never()).getPopup(any(), any(), anyInt(), anyInt());
    }

    @Test
    void aPopupOutsideAnyEmbeddingIsLeftToSwing() {
        Component elsewhere = new JButton("elsewhere");

        Popup popup = factory(embedding(false)).getPopup(elsewhere, menu, 9, 50);

        assertThat(popup).isSameAs(nativePopup);
    }

    @Test
    void aPopupWithNoOwnerIsLeftToSwing() {
        Popup popup = factory(embedding(false)).getPopup(null, menu, 9, 50);

        assertThat(popup).isSameAs(nativePopup);
    }

    @Test
    void aPopupFromADisposedEmbeddingIsLeftToSwing() {
        // An embedding torn down while Swing still holds a reference to one of its components.
        Popup popup = factory(embedding(true)).getPopup(owner, menu, 9, 50);

        assertThat(popup).isSameAs(nativePopup);
    }

    @Test
    void aFactoryALookAndFeelInstallsIsWrappedOnceMore() {
        // Aqua replaces the shared factory with its own when it initializes; ours has to stay in
        // front of it, and wrapping twice would put the factory in front of itself.
        PopupFactory before = PopupFactory.getSharedInstance();
        try {
            PopupFactory.setSharedInstance(new PopupFactory());
            EvolveSwingHost.wrapSharedPopupFactory();
            PopupFactory wrapped = PopupFactory.getSharedInstance();
            EvolveSwingHost.wrapSharedPopupFactory();

            assertThat(wrapped).isInstanceOf(ShellPopupFactory.class);
            assertThat(PopupFactory.getSharedInstance()).isSameAs(wrapped);
        } finally {
            PopupFactory.setSharedInstance(before);
        }
    }

    @Test
    void aComponentNotInAnyEmbeddingHasNoPopupEmbedding() {
        assertThat(EvolveSwingHost.popupEmbeddingOf(new JButton("loose"))).isNull();
    }
}
