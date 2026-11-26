package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SwingIsland;
import dev.equo.swt.awt.SwingIslandHostAccess;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.RecordingDisplayBridge.drainEventLoop;

/**
 * The keyboard of a modal Swing dialog drawn in a Shell while the application blocks SWT with an
 * invisible modal Shell of its own, as SWT/Swing integration libraries do. Natively the dialog is a
 * window SWT knows nothing of: it keeps the focus, and the blocker never sees a key.
 *
 * <p>Run with the dialog's modality left to AWT ({@code swt.evolve.swing_island_swt_modal=false}) and
 * with its Shell modal to SWT too, the default.
 */
class SwingModalSurfaceFocusTest {

    private static final int OWNER_WINDOW = 11;
    private static final int DIALOG_WINDOW = 12;

    private static final class KeyBridge extends RecordingDisplayBridge {
        KeyBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected void start(DartDisplay display) {
            super.start(display);
            registerDisplayKeyEvents(display);
            clientReady.complete(true);
        }
    }

    private KeyBridge bridge;
    private Display display;
    private Text mainField;
    private Shell editor;
    private boolean swtModal;
    /** An application's own filter, installed before the host's. */
    private Listener swallowsFocusIn;

    private void start(boolean swtModal) {
        this.swtModal = swtModal;
        bridge = RecordingDisplayBridge.install(KeyBridge::new);
        display = Display.getCurrent();
        if (swallowsFocusIn != null) display.addFilter(SWT.FocusIn, swallowsFocusIn);
        SwingIslandHostAccess.listen(display, bridge.comm);
        Shell main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(0, 32, 1920, 1168);
        mainField = new Text(main, SWT.SINGLE);
        mainField.setBounds(0, 0, 200, 30);
        main.open();
        drainEventLoop(display);
        editor = openSurface(OWNER_WINDOW, 0, false);
        assertThat(display.getFocusControl()).isSameAs(islandIn(editor));
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("the dialog's island has the focus when the dialog's Shell opens after the blocker")
    void dialogAfterBlocker(boolean swtModal) {
        start(swtModal);
        openBlocker();

        Shell dialog = openDialog();

        assertHasTheKeyboard(dialog);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("the dialog's island keeps the focus when the blocker opens after the dialog's Shell, and stays above it")
    void blockerAfterDialog(boolean swtModal) {
        start(swtModal);
        Shell dialog = openDialog();

        Shell blocker = openBlocker();

        assertHasTheKeyboard(dialog);
        List<Shell> drawOrder = Arrays.asList(((DartDisplay) display.getImpl()).shellsInDrawOrder());
        assertThat(drawOrder.indexOf(dialog)).as("above the blocker and the client's scrim under it")
                .isGreaterThan(drawOrder.indexOf(blocker));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("the client's report that it focused the blocker does not take the focus from the dialog")
    void lateClientActivationOfTheBlocker(boolean swtModal) {
        start(swtModal);
        Shell blocker = openBlocker();
        Shell dialog = openDialog();

        clientActivates(blocker);

        assertHasTheKeyboard(dialog);
        assertThat(focusRequests()).as("the client, whose keyboard is in the blocker, moves it")
                .contains((long) islandIn(dialog).hashCode());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("a blocker that takes the client's focus from the island that had it gives it back, in either order of the reports")
    void blockerTakesTheClientFocusFromTheIsland(boolean focusOutFirst) {
        start(false);
        Shell blocker = openBlocker();
        Shell dialog = openDialog();
        Control island = islandIn(dialog);
        clientReports(dialog, "Shell/Activate");
        clientReports(island, "Focus/FocusIn");
        drainEventLoop(display);
        bridge.comm.sent.clear();

        // One client focus change: both reports are queued before either is handled.
        if (focusOutFirst) clientReports(island, "Focus/FocusOut");
        clientReports(dialog, "Shell/Deactivate");
        clientReports(blocker, "Shell/Activate");
        if (!focusOutFirst) clientReports(island, "Focus/FocusOut");
        drainEventLoop(display);

        assertThat(focusRequests()).as("the client is asked to move its focus back").contains((long) island.hashCode());
        clientReports(dialog, "Shell/Activate");
        clientReports(island, "Focus/FocusIn");
        clientReports(blocker, "Shell/Deactivate");
        drainEventLoop(display);
        assertHasTheKeyboard(dialog);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("an Escape does not close the blocker, before or after its activation is reported")
    void escapeLeavesTheBlocker(boolean swtModal) {
        start(swtModal);
        Shell blocker = openBlocker();
        openDialog();

        escape();
        assertThat(blocker.isDisposed()).isFalse();

        clientActivates(blocker);
        escape();
        assertThat(blocker.isDisposed()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("an Escape queued behind the report of the blocker's activation asks the dialog to close, not the blocker")
    void escapeBehindTheActivationOfTheBlocker(boolean swtModal) {
        start(swtModal);
        Shell blocker = openBlocker();
        Shell dialog = openDialog();
        bridge.comm.sent.clear();

        clientReports(blocker, "Shell/Activate");
        clientSendsEscape();
        drainEventLoop(display);

        assertThat(blocker.isDisposed()).isFalse();
        assertThat(closeRequests()).containsExactly((long) DIALOG_WINDOW);
        assertHasTheKeyboard(dialog);
    }

    @Test
    @DisplayName("the dialog's island gets the focus back from a blocker whose focus event the application swallows")
    void swallowedFocusInOfTheBlocker() {
        swallowsFocusIn = e -> {
            if (e.widget instanceof Shell) e.type = SWT.None;
        };
        start(false);
        Shell blocker = openBlocker();
        Shell dialog = openDialog();

        clientActivates(blocker);

        assertHasTheKeyboard(dialog);
    }

    @Test
    @DisplayName("a blocker the application activates again goes back under the dialog")
    void blockerActivatedByTheApplication() {
        start(false);
        Shell blocker = openBlocker();
        Shell dialog = openDialog();

        blocker.setActive();
        drainEventLoop(display);

        assertHasTheKeyboard(dialog);
        List<Shell> drawOrder = Arrays.asList(((DartDisplay) display.getImpl()).shellsInDrawOrder());
        assertThat(drawOrder.indexOf(dialog)).isGreaterThan(drawOrder.indexOf(blocker));
    }

    @Test
    @DisplayName("an Escape closes a blocker with no modal Swing dialog open, as any child Shell")
    void escapeClosesTheBlockerAlone() {
        start(false);
        Shell blocker = openBlocker();

        clientReports(blocker, "Shell/Activate");
        clientSendsEscape();
        drainEventLoop(display);

        assertThat(blocker.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("an Escape closes an SWT dialog, with no Swing dialog open")
    void escapeClosesAnSwtDialog() {
        start(false);
        Shell message = openSwtDialog();

        clientReports(message, "Shell/Activate");
        clientSendsEscape();
        drainEventLoop(display);

        assertThat(message.isDisposed()).isTrue();
        assertThat(closeRequests()).isEmpty();
    }

    @Test
    @DisplayName("an Escape closes an SWT dialog opened over a modal Swing one, and nothing under it")
    void escapeClosesTheSwtDialogOverTheSwingOne() {
        start(false);
        Shell blocker = openBlocker();
        openDialog();
        Shell message = openSwtDialog();
        bridge.comm.sent.clear();

        clientReports(message, "Shell/Activate");
        clientSendsEscape();
        drainEventLoop(display);

        assertThat(message.isDisposed()).isTrue();
        assertThat(blocker.isDisposed()).isFalse();
        assertThat(closeRequests()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("an application that swallows every activation and raises the dialog itself settles on it")
    void applicationRaisesTheDialogToo(boolean swtModal) {
        start(swtModal);
        Set<Shell> visibleAtStart = Arrays.stream(display.getShells()).filter(Shell::isVisible).collect(Collectors.toSet());
        Shell[] raised = new Shell[1];
        int[] swallowed = {0};
        Listener blockActivation = e -> {
            Shell dialog = raised[0];
            if (e.widget != dialog && !visibleAtStart.contains(e.widget)) return;
            e.type = SWT.None;
            assertThat(++swallowed[0]).as("activations answered with a raise").isLessThan(10);
            // AWT's raise reaches the Shell through the engine and the client, never inside the event.
            if (dialog != null) display.asyncExec(dialog::setActive);
        };
        Shell blocker = openBlocker();
        display.addFilter(SWT.Activate, blockActivation);
        Shell dialog = openDialog();
        raised[0] = dialog;
        swallowed[0] = 0;

        clientActivates(blocker);

        assertHasTheKeyboard(dialog);
        assertThat(swallowed[0]).isLessThanOrEqualTo(2);
        display.removeFilter(SWT.Activate, blockActivation);
    }

    @Test
    @DisplayName("a modal SWT dialog opened over the Swing one, its modality AWT's alone, keeps the focus")
    void swtDialogOverTheSwingOne() {
        start(false);
        openBlocker();
        Shell dialog = openDialog();

        Shell message = openSwtDialog();

        List<Shell> drawOrder = Arrays.asList(((DartDisplay) display.getImpl()).shellsInDrawOrder());
        assertThat(drawOrder.indexOf(message)).isGreaterThan(drawOrder.indexOf(dialog));
    }

    @Test
    @DisplayName("a blocker with no modal Swing dialog open keeps the focus, and SWT out of it")
    void blockerAlone() {
        start(false);
        Shell blocker = openBlocker();
        clientActivates(blocker);

        assertThat(display.getActiveShell()).isSameAs(blocker);
        assertThat(mainField.forceFocus()).isFalse();
    }

    // Only with the dialog's modality AWT's alone: a dialog Shell modal to SWT too goes back over any
    // modal Shell opened after it, as it always has.
    @Test
    @DisplayName("a visible modal Shell with neither trim nor children, opened over the Swing dialog, keeps the focus")
    void visibleTrimlessModalShellOverTheSwingOne() {
        start(false);
        openBlocker();
        Shell dialog = openDialog();

        // Paints itself, takes no children: a splash or progress Shell the application draws on.
        Shell painted = new Shell(display, SWT.NO_TRIM | SWT.APPLICATION_MODAL);
        painted.setBounds(400, 300, 300, 120);
        painted.open();
        drainEventLoop(display);
        clientActivates(painted);

        assertThat(display.getActiveShell()).isSameAs(painted);
        assertThat(display.getFocusControl()).isSameAs(painted);
        List<Shell> drawOrder = Arrays.asList(((DartDisplay) display.getImpl()).shellsInDrawOrder());
        assertThat(drawOrder.indexOf(painted)).isGreaterThan(drawOrder.indexOf(dialog));
    }

    @Test
    @DisplayName("an application that forces the focus back to its blocker whenever another control gets it does not make the hand-over loop")
    void applicationForcesTheFocusBackToTheBlocker() {
        start(false);
        Shell blocker = openBlocker();
        int[] taken = {0};
        Listener takeItBack = e -> {
            if (e.widget == blocker || blocker.isDisposed()) return;
            assertThat(++taken[0]).as("times the application took the focus back").isLessThan(10);
            blocker.forceFocus();
        };
        display.addFilter(SWT.FocusIn, takeItBack);
        openDialog();
        assertThat(blocker.isDisposed()).isFalse();
        taken[0] = 0;
        bridge.comm.sent.clear();

        clientActivates(blocker);

        int settled = taken[0];
        drainEventLoop(display);
        display.removeFilter(SWT.FocusIn, takeItBack);
        assertThat(blocker.isDisposed()).isFalse();
        assertThat(taken[0]).as("nothing left queued that takes it again").isEqualTo(settled);
    }

    private void assertHasTheKeyboard(Shell dialog) {
        assertThat(display.getFocusControl()).isSameAs(islandIn(dialog));
        assertThat(display.getActiveShell()).isSameAs(dialog);
        assertThat(mainField.forceFocus()).as("the rest of SWT stays blocked").isFalse();
    }

    /** What the integration library does: 2x2, transparent, a child of a Shell parked off-screen. */
    private Shell openBlocker() {
        Shell limbo = new Shell(display, SWT.NONE);
        limbo.setText("limbo");
        limbo.setBounds(0, 10000, 0, 0);
        Shell blocker = new Shell(limbo, SWT.APPLICATION_MODAL);
        blocker.setBounds(0, 0, 2, 2);
        blocker.setAlpha(0);
        blocker.open();
        drainEventLoop(display);
        return blocker;
    }

    private Shell openDialog() {
        Shell dialog = openSurface(DIALOG_WINDOW, OWNER_WINDOW, true);
        assertThat(dialog.getParent()).isSameAs(editor);
        assertThat((dialog.getStyle() & SWT.APPLICATION_MODAL) != 0).isEqualTo(swtModal);
        return dialog;
    }

    private Shell openSurface(int window, int owner, boolean modal) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("windowId", window);
        request.put("port", 8765);
        request.put("kind", "dialog");
        request.put("title", "window " + window);
        request.put("modal", modal);
        request.put("resizable", true);
        request.put("hostOwnerId", owner);
        request.put("w", 657);
        request.put("h", 359);
        display.asyncExec(() -> SwingIslandHostAccess.openSurface(display, bridge.comm, request, swtModal));
        drainEventLoop(display);
        Shell shell = SwingIslandHostAccess.surfaceShell(display, window);
        assertThat(shell).isNotNull();
        return shell;
    }

    private static Control islandIn(Shell dialog) {
        assertThat(dialog.getChildren()).hasSize(1);
        assertThat(dialog.getChildren()[0]).isInstanceOf(SwingIsland.class);
        return dialog.getChildren()[0];
    }

    /** The ids the client was asked to focus, in order. */
    private List<Long> focusRequests() {
        return bridge.comm.sent.stream()
                .filter(f -> f.event.equals("swt.evolve.focus"))
                .map(f -> ((Number) ((Map<?, ?>) f.value).get("id")).longValue())
                .collect(Collectors.toList());
    }

    private void clientReports(Control control, String event) {
        bridge.comm.fireContaining("/" + control.hashCode() + "/" + event, new Event());
    }

    private void clientActivates(Shell shell) {
        clientReports(shell, "Shell/Activate");
        drainEventLoop(display);
    }

    private void escape() {
        clientSendsEscape();
        drainEventLoop(display);
    }

    /** The key as the client sends it, not handled yet. */
    private void clientSendsEscape() {
        Event key = new Event();
        key.keyCode = SWT.ESC;
        key.character = SWT.ESC;
        bridge.comm.fireContaining("Display/" + display.hashCode() + "/Key/KeyDown", key);
    }

    /** The windows Java was told a person asked to close. */
    private List<Long> closeRequests() {
        return bridge.comm.sent.stream()
                .filter(f -> f.event.equals("swingIsland.close"))
                .map(f -> ((Number) ((Map<?, ?>) f.value).get("windowId")).longValue())
                .collect(Collectors.toList());
    }

    private Shell openSwtDialog() {
        Shell message = new Shell(editor, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        Text answer = new Text(message, SWT.SINGLE);
        answer.setBounds(0, 0, 200, 30);
        message.setBounds(300, 300, 300, 100);
        message.open();
        drainEventLoop(display);
        clientActivates(message);
        assertThat(display.getFocusControl()).isSameAs(answer);
        return message;
    }
}
