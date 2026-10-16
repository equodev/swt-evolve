package dev.equo.swt.awt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A popup from an embedding is shown in a shell of its own, at the display position Swing asked
 * for, and goes away with it. Hosting the contents needs an AWT display, which CI does not have, so
 * that step is recorded instead of run.
 */
class ShellPopupTest {

    private Display display;
    private Canvas embedding;
    private final List<Shell> hostedIn = new ArrayList<>();
    private final JButton owner = new JButton("+");
    private final JPopupMenu menu = new JPopupMenu();
    private final Popup nativePopup = mock(Popup.class);

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        Shell main = new Shell(display);
        embedding = new Canvas(main, SWT.NONE);
        menu.add("Open...");
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private Popup popupAt(int x, int y) {
        PopupFactory fallback = mock(PopupFactory.class);
        when(fallback.getPopup(any(), any(), anyInt(), anyInt())).thenReturn(nativePopup);
        PopupFactory factory = new ShellPopupFactory(fallback,
                c -> c == owner ? embedding : null, (shell, contents) -> hostedIn.add(shell));
        return factory.getPopup(owner, menu, x, y);
    }

    @Test
    void theShellSitsWhereSwingPlacedThePopupAtItsPreferredSize() {
        ShellPopupFactory.ShellPopup popup = (ShellPopupFactory.ShellPopup) popupAt(9, 50);

        popup.show();

        Shell shell = popup.shell();
        assertThat(hostedIn).containsExactly(shell);
        assertThat(shell.getParent()).isSameAs(embedding.getShell());
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(9, 50,
                menu.getPreferredSize().width, menu.getPreferredSize().height));
        assertThat(shell.getVisible()).isTrue();
    }

    @Test
    void hidingThePopupDisposesItsShell() {
        ShellPopupFactory.ShellPopup popup = (ShellPopupFactory.ShellPopup) popupAt(9, 50);
        popup.show();
        Shell shell = popup.shell();

        popup.hide();

        assertThat(shell.isDisposed()).isTrue();
        assertThat(popup.shell()).isNull();
    }

    @Test
    void aPopupAskedForOffTheDisplayThreadIsLeftToSwing() throws Exception {
        Popup[] popup = new Popup[1];
        Thread other = new Thread(() -> popup[0] = popupAt(9, 50));
        other.start();
        other.join();

        assertThat(popup[0]).isSameAs(nativePopup);
    }

    @Test
    void aPopupShownTwiceKeepsOneShell() {
        ShellPopupFactory.ShellPopup popup = (ShellPopupFactory.ShellPopup) popupAt(9, 50);

        popup.show();
        popup.show();

        assertThat(hostedIn).hasSize(1);
    }
}
