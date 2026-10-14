package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What {@link DartMenu} does when it is driven directly rather than through {@link Menu}. */
@Tag("native-unit")
class MenuImplementationNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a menu bar is visible exactly while it is its shell's menu bar")
    void menuBarIsVisibleWhileItIsTheShellsMenuBar() {
        Menu bar = new Menu(shell, SWT.BAR);
        Menu other = new Menu(shell, SWT.BAR);

        assertThat(bar.getVisible()).isFalse();

        ((DartDecorations) shell.getImpl()).menuBar = bar;

        assertThat(bar.getVisible()).isTrue();
        assertThat(bar.isVisible()).isTrue();
        assertThat(other.getVisible()).isFalse();

        ((DartDecorations) shell.getImpl()).menuBar = other;

        assertThat(bar.getVisible()).isFalse();
        assertThat(other.getVisible()).isTrue();
    }

    @Test
    @DisplayName("a menu registers with its display, which is how disposing the shell finds it")
    void menuRegistersWithItsDisplay() {
        Menu popUp = new Menu(shell, SWT.POP_UP);
        Menu dropDown = new Menu(shell, SWT.DROP_DOWN);

        DartDisplay display = (DartDisplay) shell.getDisplay().getImpl();
        verify(display).addMenu(popUp);
        verify(display).addMenu(dropDown);
    }

    @Test
    @DisplayName("the application menu bar and the system menu belong to no shell and are enabled by their own flag")
    void theApplicationMenusBelongToNoShell() {
        Display display = shell.getDisplay();
        DartDisplay impl = (DartDisplay) display.getImpl();
        Menu bar = new Menu(display);
        Menu systemMenu = new Menu(display);
        impl.appMenuBar = bar;
        impl.appMenu = systemMenu;
        try {
            assertThat(bar.getShell()).isNull();
            assertThat(bar.getVisible()).isFalse();
            bar.setEnabled(false);
            assertThat(bar.isEnabled()).isFalse();
            assertThat(systemMenu.isEnabled()).isTrue();
        } finally {
            impl.appMenuBar = null;
            impl.appMenu = null;
        }
    }

    @Test
    @DisplayName("a pop-up shown with no location opens at the pointer, also on the display's left edge")
    void aPopUpShownWithNoLocationOpensAtThePointer() {
        Display display = shell.getDisplay();
        when(display.getCursorLocation()).thenReturn(new Point(10, 20));
        Menu popUp = new Menu(shell, SWT.POP_UP);
        popUp.setVisible(true);

        when(display.getCursorLocation()).thenReturn(new Point(0, 20));
        Menu onTheLeftEdge = new Menu(shell, SWT.POP_UP);
        onTheLeftEdge.setVisible(true);

        assertThat(((DartMenu) popUp.getImpl()).location).isEqualTo(new Point(10, 20));
        assertThat(((DartMenu) onTheLeftEdge.getImpl()).location).isEqualTo(new Point(0, 20));
    }

    @Test
    @DisplayName("with the pointer nowhere or at the display's origin, a pop-up opens under the live control that owns it")
    void aPopUpShownWithNoLocationOpensUnderItsOwner() {
        Menu underOwner = popUpOwnedBy(owner(new Point(80, 30), new Point(5, 35), 30));
        Menu atSizelessOwner = popUpOwnedBy(owner(null, new Point(7, 9), 0));
        Control gone = owner(new Point(80, 30), new Point(1, 1), 30);
        when(gone.isDisposed()).thenReturn(true);
        Menu ofDisposedOwner = popUpOwnedBy(gone);
        Menu ownerless = new Menu(shell, SWT.POP_UP);

        underOwner.setVisible(true);
        atSizelessOwner.setVisible(true);
        ofDisposedOwner.setVisible(true);
        ownerless.setVisible(true);

        assertThat(((DartMenu) underOwner.getImpl()).location).isEqualTo(new Point(5, 35));
        assertThat(((DartMenu) atSizelessOwner.getImpl()).location).isEqualTo(new Point(7, 9));
        assertThat(((DartMenu) ofDisposedOwner.getImpl()).location).isNull();
        assertThat(((DartMenu) ownerless.getImpl()).location).isNull();

        when(shell.getDisplay().getCursorLocation()).thenReturn(new Point(0, 0));
        Menu atOrigin = popUpOwnedBy(owner(new Point(80, 30), new Point(5, 35), 30));
        atOrigin.setVisible(true);
        assertThat(((DartMenu) atOrigin.getImpl()).location).isEqualTo(new Point(5, 35));
    }

    private Menu popUpOwnedBy(Control owner) {
        Menu popUp = new Menu(shell, SWT.POP_UP);
        ((DartMenu) popUp.getImpl()).ownerControl = owner;
        return popUp;
    }

    /** A control of this size, which maps the point below it at the given height to the given display point. */
    private static Control owner(Point size, Point below, int height) {
        Control owner = mock(Control.class);
        when(owner.getSize()).thenReturn(size);
        when(owner.toDisplay(0, height)).thenReturn(below);
        return owner;
    }
}
