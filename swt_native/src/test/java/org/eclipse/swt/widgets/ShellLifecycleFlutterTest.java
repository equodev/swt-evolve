package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a shell does where the window manager decides on native SWT: showing, activation, minimizing, full
 * screen, and listeners that dispose the shell while it shows or moves.
 */
@Tag("flutter-it")
class ShellLifecycleFlutterTest {

    private Display display;
    private TestWebBridge web;
    private Shell main;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        web = new TestWebBridge(dd);
        dd.setBridge(web);
        web.start(dd);
        main = new Shell(display);
        main.open();
        runLoop();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void runLoop() {
        while (display.readAndDispatch()) {
        }
    }

    private Shell dialog() {
        Shell shell = new Shell(main, SWT.DIALOG_TRIM);
        shell.setBounds(100, 120, 300, 200);
        return shell;
    }

    @Test
    void focusingAControlOfAMinimizedShellLeavesItMinimized() {
        Shell shell = dialog();
        new Text(shell, SWT.SINGLE);
        Text second = new Text(shell, SWT.SINGLE);
        shell.open();
        runLoop();
        shell.setMinimized(true);
        second.forceFocus();
        assertThat(shell.getMinimized()).isTrue();
    }

    @Test
    void aShellAnActivateListenerDisposesWhileOpeningStopsThere() {
        Shell shell = dialog();
        List<Integer> events = new ArrayList<>();
        shell.addListener(SWT.Activate, e -> shell.dispose());
        shell.addListener(SWT.FocusIn, e -> events.add(e.type));
        shell.open();
        runLoop();
        assertThat(shell.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }

    @Test
    void aShellAMoveListenerDisposesWhileOpeningStopsThere() {
        Shell shell = new Shell(main, SWT.DIALOG_TRIM);
        List<Integer> events = new ArrayList<>();
        shell.addListener(SWT.Move, e -> shell.dispose());
        shell.addListener(SWT.Resize, e -> events.add(e.type));
        shell.open();
        runLoop();
        assertThat(shell.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }

    @Test
    void aShellAResizeListenerDisposesWhileOpeningIsNotLaidOut() {
        Shell shell = new Shell(main, SWT.DIALOG_TRIM);
        List<String> laidOut = new ArrayList<>();
        shell.setLayout(new Layout() {
            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                return new Point(0, 0);
            }

            @Override
            protected void layout(Composite composite, boolean flushCache) {
                laidOut.add("layout");
            }
        });
        new Composite(shell, SWT.NONE);
        shell.addListener(SWT.Resize, e -> shell.dispose());
        shell.open();
        runLoop();
        assertThat(shell.isDisposed()).isTrue();
        assertThat(laidOut).isEmpty();
    }

    @Test
    void aShellItsLayoutDisposesWhileOpeningStopsThere() {
        Shell shell = new Shell(main, SWT.DIALOG_TRIM);
        shell.setLayout(new Layout() {
            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                return new Point(0, 0);
            }

            @Override
            protected void layout(Composite composite, boolean flushCache) {
                shell.dispose();
            }
        });
        shell.open();
        runLoop();
        assertThat(shell.isDisposed()).isTrue();
    }

    @Test
    void aResizeListenerThatDisposesTheShellEndsSetBounds() {
        Shell shell = dialog();
        shell.setLayout(new FillLayout());
        shell.open();
        runLoop();
        shell.addListener(SWT.Resize, e -> shell.dispose());
        shell.setBounds(100, 120, 320, 220);
        assertThat(shell.isDisposed()).isTrue();
    }

    @Test
    void minimizingAndRestoringAShownShell() {
        Shell shell = dialog();
        shell.open();
        runLoop();
        shell.setMinimized(true);
        assertThat(shell.getMinimized()).isTrue();
        shell.setMinimized(false);
        assertThat(shell.getMinimized()).isFalse();
    }

    @Test
    void aShellLeavesFullScreenWhenItIsGivenBounds() {
        Shell shell = dialog();
        shell.open();
        runLoop();
        shell.setFullScreen(true);
        assertThat(shell.getFullScreen()).isTrue();
        shell.setBounds(110, 130, 300, 200);
        assertThat(shell.getFullScreen()).isFalse();
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(110, 130, 300, 200));
    }

    @Test
    void aShellWithAMenuBarGoesFullScreenAndBack() {
        Shell shell = dialog();
        shell.setMenuBar(new Menu(shell, SWT.BAR));
        shell.open();
        runLoop();
        shell.setFullScreen(true);
        assertThat(shell.getFullScreen()).isTrue();
        shell.setFullScreen(false);
        assertThat(shell.getFullScreen()).isFalse();
    }

    @Test
    void aControlMovedToAnotherShellStopsBeingItsSavedFocus() {
        Shell shell = dialog();
        Text field = new Text(shell, SWT.SINGLE);
        shell.open();
        runLoop();
        field.forceFocus();
        assertThat(((DartDecorations) shell.getImpl()).savedFocus).isSameAs(field);
        Shell other = dialog();
        assertThat(field.setParent(other)).isTrue();
        assertThat(((DartDecorations) shell.getImpl()).savedFocus).isNull();
    }

    @Test
    void activationSkipsControlsAListenerDisposedOnTheWay() {
        Shell shell = dialog();
        Composite from = new Composite(shell, SWT.NONE);
        Text fromField = new Text(from, SWT.SINGLE);
        Composite to = new Composite(shell, SWT.NONE);
        Text toField = new Text(to, SWT.SINGLE);
        shell.open();
        runLoop();
        DartShell impl = (DartShell) shell.getImpl();
        impl.setActiveControl(fromField);
        List<Widget> deactivated = new ArrayList<>();
        List<Widget> activated = new ArrayList<>();
        fromField.addListener(SWT.Deactivate, e -> {
            deactivated.add(e.widget);
            from.dispose();
        });
        from.addListener(SWT.Deactivate, e -> deactivated.add(e.widget));
        toField.addListener(SWT.Activate, e -> {
            activated.add(e.widget);
            to.dispose();
        });
        to.addListener(SWT.Activate, e -> activated.add(e.widget));
        Text other = new Text(shell, SWT.SINGLE);
        impl.setActiveControl(other);
        impl.setActiveControl(toField);
        assertThat(deactivated).containsExactly(fromField);
        assertThat(activated).containsExactly(toField);
    }

    @Test
    void aDisposedControlIsNotMadeTheActiveOne() {
        Shell shell = dialog();
        Text field = new Text(shell, SWT.SINGLE);
        shell.open();
        runLoop();
        DartShell impl = (DartShell) shell.getImpl();
        impl.setActiveControl(field);
        List<Integer> events = new ArrayList<>();
        field.addListener(SWT.Deactivate, e -> events.add(e.type));
        Text gone = new Text(shell, SWT.SINGLE);
        gone.dispose();
        impl.setActiveControl(gone);
        assertThat(events).containsExactly(SWT.Deactivate);
        assertThat(impl.lastActive).isNull();
    }

    @Test
    void closingTheActiveShellWhileTheOneBeforeItIsHiddenHandsActivationToNoHiddenShell() {
        Shell first = dialog();
        first.open();
        runLoop();
        Shell second = dialog();
        second.open();
        runLoop();
        first.setVisible(false);
        List<Integer> events = new ArrayList<>();
        first.addListener(SWT.Activate, e -> events.add(e.type));
        second.dispose();
        runLoop();
        assertThat(events).isEmpty();
    }

    @Test
    void zOrderRequestsLeaveAShellWhereItIs() {
        Shell shell = dialog();
        Shell hidden = dialog();
        shell.open();
        runLoop();
        shell.moveAbove(null);
        shell.moveBelow(null);
        shell.moveAbove(hidden);
        hidden.moveAbove(null);
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(100, 120, 300, 200));
        assertThat(shell.isVisible()).isTrue();
        assertThat(hidden.isVisible()).isFalse();
    }

    @Test
    void aPrimaryModalChildKeepsTheFocusFromItsParentAlone() {
        Shell shell = dialog();
        Text field = new Text(shell, SWT.SINGLE);
        shell.open();
        runLoop();
        Shell ownModal = new Shell(shell, SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        ownModal.open();
        Shell othersModal = new Shell(main, SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        othersModal.open();
        runLoop();
        assertThat(field.forceFocus()).isFalse();
        ownModal.dispose();
        othersModal.dispose();
        runLoop();
        assertThat(field.forceFocus()).isTrue();
    }

    @Test
    void openingAShellAfterTheActiveOneWasHiddenDeactivatesNothing() {
        Shell first = dialog();
        first.open();
        runLoop();
        first.setVisible(false);
        List<Integer> events = new ArrayList<>();
        first.addListener(SWT.Deactivate, e -> events.add(e.type));
        Shell second = dialog();
        second.open();
        runLoop();
        assertThat(events).isEmpty();
    }

    @Test
    void hidingAShellLeavesItsChildShellsTheirOwnVisibility() {
        Shell parent = dialog();
        Shell shown = new Shell(parent, SWT.DIALOG_TRIM);
        Shell hidden = new Shell(parent, SWT.DIALOG_TRIM);
        Shell grandchild = new Shell(shown, SWT.DIALOG_TRIM);
        parent.open();
        shown.open();
        grandchild.open();
        runLoop();
        parent.setVisible(false);
        assertThat(shown.getVisible()).isTrue();
        assertThat(grandchild.getVisible()).isTrue();
        assertThat(hidden.getVisible()).isFalse();
    }

    @Test
    void aShellHoldsTheDialogsOpenOnItWhileTheyAreOpen() {
        Shell shell = dialog();
        shell.open();
        runLoop();
        DartShell impl = (DartShell) shell.getImpl();
        ColorDialog outer = new ColorDialog(shell);
        ColorDialog inner = new ColorDialog(shell);
        List<List<Dialog>> seen = new ArrayList<>();
        display.asyncExec(() -> {
            seen.add(List.of(impl.getDialogs()));
            display.asyncExec(() -> {
                seen.add(List.of(impl.getDialogs()));
                web.comm.fireContaining("ColorDialog/" + System.identityHashCode(inner.getImpl()) + "/close", "");
            });
            inner.open();
            seen.add(List.of(impl.getDialogs()));
            web.comm.fireContaining("ColorDialog/" + System.identityHashCode(outer.getImpl()) + "/close", "");
        });
        outer.open();
        assertThat(seen).containsExactly(List.of(outer), List.of(outer, inner), List.of(outer));
        assertThat(impl.getDialogs()).isNull();
    }

    @Test
    void aShellGoingWithTheOneActivatedBeforeItHandsActivationToItsParent() {
        Shell parent = dialog();
        parent.open();
        runLoop();
        Shell child = new Shell(parent, SWT.DIALOG_TRIM);
        child.open();
        runLoop();
        parent.setActive();
        runLoop();
        List<Integer> events = new ArrayList<>();
        main.addListener(SWT.Activate, e -> events.add(e.type));
        parent.dispose();
        runLoop();
        assertThat(child.isDisposed()).isTrue();
        assertThat(events).containsExactly(SWT.Activate);
    }

    @Test
    void theShellBeforeTheActiveOneHiddenWhileItGoesIsNotActivated() {
        Shell first = dialog();
        first.open();
        runLoop();
        Shell second = dialog();
        second.open();
        runLoop();
        List<Integer> events = new ArrayList<>();
        first.addListener(SWT.Activate, e -> events.add(e.type));
        second.addListener(SWT.Dispose, e -> first.setVisible(false));
        second.dispose();
        runLoop();
        assertThat(events).isEmpty();
    }

    @Test
    void anActivatedShellFocusesItsFirstControlThatCanTakeTheFocus() {
        Shell shell = dialog();
        Text hidden = new Text(shell, SWT.SINGLE);
        hidden.setVisible(false);
        Text disabled = new Text(shell, SWT.SINGLE);
        disabled.setEnabled(false);
        new Canvas(shell, SWT.NO_FOCUS);
        Composite group = new Composite(shell, SWT.NONE);
        Text target = new Text(group, SWT.SINGLE);
        shell.open();
        runLoop();
        assertThat(display.getFocusControl()).isSameAs(target);
    }

    @Test
    void aShellWhoseOnlyListIsDisabledFocusesNothingInIt() {
        Shell shell = dialog();
        new Composite(shell, SWT.NONE);
        Table table = new Table(shell, SWT.NONE);
        table.setEnabled(false);
        shell.open();
        runLoop();
        assertThat(display.getFocusControl()).isNotSameAs(table);
    }

    @Test
    void aShowListenerThatDisposesTheActiveShellLeavesNoneToDeactivate() {
        Shell first = dialog();
        first.open();
        runLoop();
        Shell second = dialog();
        second.addListener(SWT.Show, e -> first.dispose());
        second.open();
        runLoop();
        assertThat(first.isDisposed()).isTrue();
        assertThat(second.isVisible()).isTrue();
    }

    @Test
    void aShowListenerThatHidesTheActiveShellLeavesItUndeactivated() {
        Shell first = dialog();
        first.open();
        runLoop();
        List<Integer> events = new ArrayList<>();
        first.addListener(SWT.Deactivate, e -> events.add(e.type));
        Shell second = dialog();
        second.addListener(SWT.Show, e -> first.setVisible(false));
        second.open();
        runLoop();
        assertThat(events).isEmpty();
    }

    @Test
    void theShellToReturnToHiddenWhileTheActiveOneGoesIsNotActivated() {
        Shell first = dialog();
        first.open();
        runLoop();
        Shell second = dialog();
        second.open();
        runLoop();
        Menu menu = new Menu(second, SWT.POP_UP);
        second.setMenu(menu);
        menu.addListener(SWT.Dispose, e -> first.setVisible(false));
        List<Integer> events = new ArrayList<>();
        first.addListener(SWT.Activate, e -> events.add(e.type));
        second.dispose();
        runLoop();
        assertThat(events).isEmpty();
    }

    @Test
    void theShellToReturnToDisposedWhileTheActiveOneGoesIsNotActivated() {
        Shell first = dialog();
        first.open();
        runLoop();
        Shell second = dialog();
        second.open();
        runLoop();
        Menu menu = new Menu(second, SWT.POP_UP);
        second.setMenu(menu);
        menu.addListener(SWT.Dispose, e -> first.dispose());
        second.dispose();
        runLoop();
        assertThat(first.isDisposed()).isTrue();
    }

    @Test
    void theLastVisibleShellGoingLeavesNoShellToActivate() {
        Shell shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setBounds(100, 120, 300, 200);
        shell.open();
        runLoop();
        List<Integer> events = new ArrayList<>();
        main.addListener(SWT.Activate, e -> events.add(e.type));
        main.setVisible(false);
        shell.dispose();
        runLoop();
        assertThat(events).isEmpty();
        assertThat(display.getActiveShell()).isNull();
    }

    @Test
    void maximizingAShownShellTwiceKeepsItMaximizedUntilItIsRestored() {
        Shell shell = dialog();
        shell.open();
        runLoop();
        shell.setMaximized(true);
        shell.setMaximized(true);
        assertThat(shell.getMaximized()).isTrue();
        shell.setMaximized(false);
        assertThat(shell.getMaximized()).isFalse();
    }
}
