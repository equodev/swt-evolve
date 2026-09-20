package dev.equo.swt;

import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which shells {@link WindowPolicy} puts in a window of their own.
 *
 * <p>This is the whole configuration surface of multi-window rendering, and it is deliberately
 * tested without any surface behind it: the answer must not depend on whether a native window or a
 * browser window would end up being opened.
 */
@Tag("native-unit")
class WindowPolicyNativeTest {

    private Display display;
    private final java.util.Map<String, String> savedProperties = new java.util.HashMap<>();

    @BeforeEach
    void setUp() {
        for (String key : new String[]{WindowPolicy.MODE_PROPERTY, WindowPolicy.DIALOGS_PROPERTY,
                WindowPolicy.MODAL_PROPERTY}) {
            savedProperties.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
    }

    @AfterEach
    void tearDown() {
        WindowPolicy.setResolver(null);
        savedProperties.forEach((key, value) -> {
            if (value == null) System.clearProperty(key);
            else System.setProperty(key, value);
        });
        savedProperties.clear();
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    // ---- the default: nothing changes ------------------------------------------------------------

    @Test
    @DisplayName("by default no shell gets a window of its own")
    void singleIsTheDefault() {
        assertThat(WindowPolicy.mode()).isEqualTo(WindowPolicy.Mode.SINGLE);
        assertThat(WindowPolicy.isSingleWindow())
                .as("a surface must be able to skip the whole question when nothing can be detached")
                .isTrue();
        assertThat(WindowPolicy.ownsWindow(topLevel(), false)).isFalse();
        assertThat(WindowPolicy.ownsWindow(child(), false)).isFalse();
        assertThat(WindowPolicy.ownsWindow(modal(), false)).isFalse();
    }

    @Test
    @DisplayName("an unrecognised mode is the default, not an error")
    void unknownModeFallsBackToSingle() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "sideways");
        assertThat(WindowPolicy.mode()).isEqualTo(WindowPolicy.Mode.SINGLE);
    }

    // ---- the main shell is the Display's window ---------------------------------------------------

    @Test
    @DisplayName("the main shell is never given a window of its own, in any mode")
    void mainShellIsNeverDetached() {
        for (String mode : new String[]{"single", "top-level", "all"}) {
            System.setProperty(WindowPolicy.MODE_PROPERTY, mode);
            assertThat(WindowPolicy.ownsWindow(topLevel(), true))
                    .as("mode=%s: the main shell IS the Display's window, so it cannot be inside another",
                            mode)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("even an explicit per-shell opt-in cannot detach the main shell")
    void mainShellOverrideIsIgnored() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        Shell main = topLevel();
        main.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        assertThat(WindowPolicy.ownsWindow(main, true)).isFalse();
    }

    // ---- top-level ---------------------------------------------------------------------------------

    @Test
    @DisplayName("top-level detaches a second top-level shell and leaves dialogs inline")
    void topLevelMode() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");

        assertThat(WindowPolicy.ownsWindow(topLevel(), false))
                .as("a parentless, non-modal shell is exactly what this mode is for")
                .isTrue();
        assertThat(WindowPolicy.ownsWindow(child(), false))
                .as("a child shell is a dialog of its parent; it stays in the parent's window by default")
                .isFalse();
        assertThat(WindowPolicy.ownsWindow(modal(), false))
                .as("a modal shell drawn inline keeps the overlay dimming what it blocks, which a "
                        + "separate window cannot reproduce")
                .isFalse();
    }

    @Test
    @DisplayName("top-level still honours an explicit request to detach dialogs")
    void topLevelWithDialogsOwn() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        System.setProperty(WindowPolicy.DIALOGS_PROPERTY, "own");
        assertThat(WindowPolicy.ownsWindow(child(), false)).isTrue();
        assertThat(WindowPolicy.ownsWindow(modal(), false))
                .as("asking for dialogs says nothing about modals")
                .isFalse();
    }

    @Test
    @DisplayName("top-level still honours an explicit request to detach modals")
    void topLevelWithModalOwn() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        System.setProperty(WindowPolicy.MODAL_PROPERTY, "own");
        assertThat(WindowPolicy.ownsWindow(modal(), false)).isTrue();
        assertThat(WindowPolicy.ownsWindow(child(), false)).isFalse();
    }

    @Test
    @DisplayName("a modal child shell is judged as a modal, not as a dialog")
    void modalityWinsOverParentage() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        System.setProperty(WindowPolicy.DIALOGS_PROPERTY, "own");
        Shell modalChild = new Shell(topLevel(), SWT.APPLICATION_MODAL);
        assertThat(WindowPolicy.ownsWindow(modalChild, false))
                .as("it is both; the modal rule is the one that has to decide, or an Ok/Cancel box "
                        + "would be detached by a flag that never mentions modality")
                .isFalse();
    }

    // ---- all ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("a shell with no trim is never a window of its own, whatever the mode says")
    void trimlessIsNeverItsOwnWindow() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        // A shell that declares no title, border, close box or resize handle is a container, not a
        // window: a splash painted edge to edge, or the off-screen shell a layout parks widgets
        // under. Framing one is how a blank window appears for something never meant to be seen.
        assertThat(WindowPolicy.ownsWindow(new Shell(display, SWT.NO_TRIM), false)).isFalse();
        assertThat(WindowPolicy.ownsWindow(new Shell(display, SWT.NONE), false)).isFalse();
        assertThat(WindowPolicy.ownsWindow(new Shell(display, SWT.APPLICATION_MODAL), false))
                .as("modality does not make a chromeless shell into a window either")
                .isFalse();
    }

    @Test
    @DisplayName("a trimless shell can still ask for a window of its own")
    void trimlessCanOptIn() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        Shell chromeless = new Shell(display, SWT.NO_TRIM);
        chromeless.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        assertThat(WindowPolicy.ownsWindow(chromeless, false))
                .as("the per-shell override is read before the shape is judged")
                .isTrue();
    }

    @Test
    @DisplayName("all detaches every shell, dialogs and modals included")
    void allMode() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        assertThat(WindowPolicy.ownsWindow(topLevel(), false)).isTrue();
        assertThat(WindowPolicy.ownsWindow(child(), false)).isTrue();
        assertThat(WindowPolicy.ownsWindow(modal(), false)).isTrue();
    }

    @Test
    @DisplayName("all can still be told to keep the Ok/Cancel boxes inline")
    void allModeWithModalInline() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        System.setProperty(WindowPolicy.MODAL_PROPERTY, "inline");
        assertThat(WindowPolicy.ownsWindow(modal(), false)).isFalse();
        assertThat(WindowPolicy.ownsWindow(topLevel(), false))
                .as("keeping modals inline must not pull everything else back in with them")
                .isTrue();
    }

    @Test
    @DisplayName("all can still be told to keep child shells inline")
    void allModeWithDialogsInline() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        System.setProperty(WindowPolicy.DIALOGS_PROPERTY, "inline");
        assertThat(WindowPolicy.ownsWindow(child(), false)).isFalse();
        assertThat(WindowPolicy.ownsWindow(topLevel(), false)).isTrue();
    }

    // ---- the per-shell escape hatch ----------------------------------------------------------------

    @Test
    @DisplayName("a shell can opt in to a window of its own against the mode")
    void shellOptIn() {
        Shell shell = child(); // single mode, a dialog: everything says inline
        shell.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        assertThat(WindowPolicy.ownsWindow(shell, false)).isTrue();
    }

    @Test
    @DisplayName("a shell can opt out of a window of its own against the mode")
    void shellOptOut() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        Shell shell = topLevel();
        shell.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        assertThat(WindowPolicy.ownsWindow(shell, false)).isFalse();
    }

    @Test
    @DisplayName("the override is also readable as a string, for a property-driven application")
    void shellOverrideAcceptsAString() {
        Shell shell = child();
        shell.setData(WindowPolicy.SHELL_DATA_KEY, "true");
        assertThat(WindowPolicy.ownsWindow(shell, false)).isTrue();
        shell.setData(WindowPolicy.SHELL_DATA_KEY, "nonsense");
        assertThat(WindowPolicy.ownsWindow(shell, false))
                .as("an unparseable value is no answer, so the flags decide")
                .isFalse();
    }

    // ---- the application's own rule ----------------------------------------------------------------

    @Test
    @DisplayName("an installed resolver decides, and outranks the per-shell override")
    void resolverDecides() {
        Shell shell = child();
        shell.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        WindowPolicy.setResolver((s, isMain) -> Boolean.TRUE);
        assertThat(WindowPolicy.ownsWindow(shell, false)).isTrue();
    }

    @Test
    @DisplayName("a resolver that answers null leaves the shell to the flags")
    void resolverCanAbstain() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "top-level");
        WindowPolicy.setResolver((s, isMain) -> null);
        assertThat(WindowPolicy.ownsWindow(topLevel(), false)).isTrue();
        assertThat(WindowPolicy.ownsWindow(child(), false)).isFalse();
    }

    @Test
    @DisplayName("abstaining is not refusing: under a mode that detaches, null still detaches")
    void abstainingIsNotRefusing() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        Shell shell = child();

        WindowPolicy.setResolver((s, isMain) -> null);
        assertThat(WindowPolicy.ownsWindow(shell, false))
                .as("null is 'no opinion', so the mode decides — a rule meaning 'only these' has to "
                        + "say FALSE for the rest or it looks like it is being ignored")
                .isTrue();

        WindowPolicy.setResolver((s, isMain) -> Boolean.FALSE);
        assertThat(WindowPolicy.ownsWindow(shell, false))
                .as("FALSE is the answer that keeps a shell inline whatever the mode says")
                .isFalse();
    }

    @Test
    @DisplayName("a resolver is enough to make multi-window live, whatever the mode says")
    void resolverDefeatsTheFastPath() {
        assertThat(WindowPolicy.isSingleWindow()).isTrue();
        WindowPolicy.setResolver((s, isMain) -> null);
        assertThat(WindowPolicy.isSingleWindow())
                .as("skipping the question would skip the resolver with it")
                .isFalse();
    }

    // ---- degenerate input --------------------------------------------------------------------------

    @Test
    @DisplayName("a disposed or absent shell owns nothing")
    void disposedShellOwnsNothing() {
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
        Shell shell = topLevel();
        shell.dispose();
        assertThat(WindowPolicy.ownsWindow(shell, false)).isFalse();
        assertThat(WindowPolicy.ownsWindow(null, false)).isFalse();
    }

    // ---- harness -----------------------------------------------------------------------------------

    private Shell topLevel() {
        return new Shell(display, SWT.SHELL_TRIM);
    }

    private Shell child() {
        return new Shell(topLevel(), SWT.DIALOG_TRIM);
    }

    /** As applications actually build one: a modal is a dialog, so it carries dialog trim. */
    private Shell modal() {
        return new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }
}
