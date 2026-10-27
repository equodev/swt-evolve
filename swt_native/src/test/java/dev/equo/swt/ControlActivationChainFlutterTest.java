package dev.equo.swt;

import java.util.ArrayList;
import java.util.List;

import dev.equo.swt.harness.FocusTrackingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ControlHelper;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Activation is a change, not a restatement: Shell.setActiveControl announces only the part of the
 * focused control's parent chain that differs from the chain that was active before it. Announcing
 * the whole chain instead names the Shell on every focus change inside it, and announcing it per
 * focus callback names it twice -- both of which an embedding workbench reads as the active part
 * changing when it did not.
 */
@Tag("flutter-it")
class ControlActivationChainFlutterTest {

    private Display display;
    private Shell shell;
    private Composite part;
    private Tree tree;
    private final List<String> announced = new ArrayList<>();

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new FocusTrackingBridge());
        display = new Display();
        shell = new Shell(display);
        shell.open();
        part = new Composite(shell, SWT.NONE);
        tree = new Tree(part, SWT.NONE);
        watch("Shell", shell);
        watch("part", part);
        watch("tree", tree);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private void watch(String name, Control control) {
        control.addListener(SWT.Activate, e -> announced.add(name + ".Activate"));
        control.addListener(SWT.Deactivate, e -> announced.add(name + ".Deactivate"));
    }

    private void activate(Control control) {
        ControlHelper.sendActivateToAncestors((DartControl) control.getImpl());
    }

    @Test
    @DisplayName("focusing inside an open shell announces the chain below it, not the shell")
    void firstFocusDoesNotAnnounceTheShell() {
        activate(tree);

        assertThat(announced).containsExactly("tree.Activate", "part.Activate");
    }

    @Test
    @DisplayName("announcing the same control twice announces it once")
    void reAnnouncingTheFocusedControlIsSilent() {
        activate(tree);
        announced.clear();

        activate(tree);

        assertThat(announced).isEmpty();
    }

    @Test
    @DisplayName("focus falling back to the shell deactivates the chain and re-announces nothing")
    void focusBackToTheShellOnlyDeactivates() {
        activate(tree);
        announced.clear();

        activate(shell);

        assertThat(announced).containsExactly("tree.Deactivate", "part.Deactivate");
    }

    @Test
    @DisplayName("setFocus announces the control once and leaves the open shell alone")
    void programmaticFocusAnnouncesOnlyTheControl() {
        announced.clear();

        assertThat(tree.setFocus()).isTrue();

        // setFocus goes through forceFocus, which brings the shell to the top; a shell that is
        // already the active one must not report an activation for that, the way no window manager
        // reports one for raising the window that is already in front.
        assertThat(announced).containsExactly("tree.Activate", "part.Activate");
    }

    @Test
    @DisplayName("focus moving between siblings leaves their shared ancestor alone")
    void siblingFocusKeepsTheSharedAncestorActive() {
        Tree sibling = new Tree(part, SWT.NONE);
        watch("sibling", sibling);
        activate(tree);
        announced.clear();

        activate(sibling);

        assertThat(announced).containsExactly("tree.Deactivate", "sibling.Activate");
    }
}
