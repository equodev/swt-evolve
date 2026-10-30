package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A push button that holds the focus is its shell's default button, as on Win32 and GTK; when the
 * focus leaves it, the default set with {@link Decorations#setDefaultButton(Button)} comes back.
 * A check or radio button taking the focus leaves the default alone.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*FocusedPushButtonIsDefaultFlutterTest'</pre>
 */
@Tag("flutter-it")
class FocusedPushButtonIsDefaultFlutterTest {

    private Display display;
    private Shell shell;
    private Text field;
    private Button ok;
    private Button cancel;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private void openDialog() {
        install();
        shell = new Shell(display);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        field = new Text(shell, SWT.SINGLE);
        ok = new Button(shell, SWT.PUSH);
        ok.setText("OK");
        cancel = new Button(shell, SWT.PUSH);
        cancel.setText("Cancel");
        shell.setSize(300, 200);
        shell.open();
        field.setFocus();
        drain();
    }

    private void drain() {
        while (display.readAndDispatch()) {
        }
    }

    @Test
    void aFocusedPushButtonIsTheDefault() {
        openDialog();
        shell.setDefaultButton(ok);

        cancel.setFocus();
        drain();

        assertThat(display.getFocusControl()).as("precondition: Cancel holds the focus").isSameAs(cancel);
        assertThat(shell.getDefaultButton()).isSameAs(cancel);
    }

    @Test
    void aPushButtonTakingTheFocusBecomesTheDefaultWithoutOneSet() {
        openDialog();

        ok.forceFocus();
        drain();

        assertThat(display.getFocusControl()).as("precondition: OK holds the focus").isSameAs(ok);
        assertThat(shell.getDefaultButton()).isSameAs(ok);
    }

    @Test
    void theSavedDefaultComesBackWhenTheFocusLeavesTheButton() {
        openDialog();
        shell.setDefaultButton(ok);
        cancel.setFocus();
        drain();

        field.setFocus();
        drain();

        assertThat(shell.getDefaultButton()).isSameAs(ok);
    }

    @Test
    void noDefaultIsLeftBehindWhenNoneWasSet() {
        openDialog();
        ok.setFocus();
        drain();

        field.setFocus();
        drain();

        assertThat(shell.getDefaultButton()).isNull();
    }

    @Test
    void aFocusedCheckButtonLeavesTheDefaultAlone() {
        openDialog();
        Button check = new Button(shell, SWT.CHECK);
        shell.layout();
        shell.setDefaultButton(ok);

        check.setFocus();
        drain();

        assertThat(display.getFocusControl()).as("precondition: the check button holds the focus").isSameAs(check);
        assertThat(shell.getDefaultButton()).isSameAs(ok);
    }

    @Test
    void aDisposedSetDefaultDoesNotComeBack() {
        openDialog();
        shell.setDefaultButton(ok);
        cancel.setFocus();
        drain();

        ok.dispose();
        field.setFocus();
        drain();

        assertThat(shell.getDefaultButton()).isNull();
    }

    @Test
    void aDefaultSetWhileAPushButtonHoldsTheFocusStaysOnceTheFocusLeaves() {
        openDialog();
        cancel.setFocus();
        drain();

        shell.setDefaultButton(ok);
        assertThat(shell.getDefaultButton()).isSameAs(ok);

        field.setFocus();
        drain();

        assertThat(shell.getDefaultButton()).isSameAs(ok);
    }

    @Test
    void aPushButtonDisposedByItsFocusListenerLeavesTheDefaultAlone() {
        openDialog();
        shell.setDefaultButton(ok);
        cancel.addListener(SWT.FocusIn, e -> cancel.dispose());

        cancel.setFocus();
        drain();

        assertThat(cancel.isDisposed()).as("precondition: the focus listener disposed Cancel").isTrue();
        assertThat(shell.getDefaultButton()).isSameAs(ok);
    }

    @Test
    void theButtonDrawnAsTheDefaultIsTheOneSet() {
        openDialog();
        shell.setDefaultButton(ok);

        cancel.setFocus();
        drain();

        assertThat(new VButton((DartButton) ok.getImpl()).getPrimary()).as("OK, the default set").isTrue();
        assertThat(new VButton((DartButton) cancel.getImpl()).getPrimary()).as("Cancel, focused").isFalse();
    }

    // ---- harness ----------------------------------------------------------------------------------

    /** See {@code PopupShellFocusHolderFlutterTest.install}: a Display whose bridge is the test bridge. */
    private void install() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
    }
}
