package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A spinner writes its decimals in the locale of its Display. On web that is the locale the browser reports with its
 * viewport, a point until it does; on desktop it is the JVM's, which the client gets in the config flags.
 */
@Tag("flutter-it")
class SpinnerClientLocaleFlutterTest {

    private final Locale jvmLocale = Locale.getDefault(Locale.Category.FORMAT);
    private final String mode = ConfigFlags.mode();
    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        Locale.setDefault(Locale.Category.FORMAT, jvmLocale);
        if (mode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else ConfigFlags.setMode(mode);
    }

    @Test
    void onTheWebASpinnerIsWrittenInTheBrowsersLocaleOnceItIsReported() {
        TestWebBridge web = install(TestWebBridge::new);
        Spinner spinner = spinner();
        assertThat(spinner.getText()).as("before the client reports").isEqualTo("1.23");

        clientReady(web.comm, "de-DE", true);

        assertThat(spinner.getText()).isEqualTo("1,23");
    }

    @Test
    void onTheDesktopASpinnerIsWrittenInTheJvmsLocale() {
        Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        TestDeskBridge desk = install(TestDeskBridge::new);
        Spinner spinner = spinner();

        clientReady(desk.comm, "en-US", true);

        assertThat(spinner.getText()).isEqualTo("1,23");
        assertThat(Config.getConfigFlags().decimal_separator).isEqualTo(",");
        assertThat(Config.getConfigFlags().locale).isEqualTo("de-DE");
    }

    @Test
    void aSpinnerOpenedAfterTheReportStartsInTheClientsLocale() {
        TestWebBridge web = install(TestWebBridge::new);
        clientReady(web.comm, "fr-FR", true);

        assertThat(spinner().getText()).isEqualTo("1,23");
    }

    @Test
    void aReportWithoutALocaleKeepsTheOneTheClientGave() {
        TestWebBridge web = install(TestWebBridge::new);
        Spinner spinner = spinner();
        clientReady(web.comm, "de-DE", true);

        clientReady(web.comm, null, false);

        assertThat(spinner.getText()).isEqualTo("1,23");
    }

    @Test
    void aReportedLocaleSendsNothingToTheSpinners() {
        TestWebBridge web = install(TestWebBridge::new);
        VSpinner value = (VSpinner) ((DartSpinner) spinner().getImpl()).getValue();
        value.changedKeys().clear();

        clientReady(web.comm, "de-DE", true);

        assertThat(value.changedKeys()).isEmpty();
    }

    // ---- harness ----------------------------------------------------------------------------------

    private Spinner spinner() {
        Shell shell = new Shell(display);
        Composite group = new Composite(shell, SWT.NONE);
        new Label(group, SWT.NONE).setText("Amount");
        Spinner spinner = new Spinner(group, SWT.NONE);
        spinner.setValues(123, 0, 1000, 2, 1, 10);
        return spinner;
    }

    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    /** Fired on the Display thread, so the report is applied before this returns. */
    private static void clientReady(RecordingComm comm, String locale, boolean isFirst) {
        ClientReadyPayload p = new ClientReadyPayload();
        p.width = 1280;
        p.height = 800;
        p.isFirst = isFirst;
        p.locale = locale;
        comm.fireContaining("ClientReady", p);
    }

    /** The web surface's handshake over a recorded comm, with no server or browser behind it. */
    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }

    /** The desktop surface's handshake, with the native window stubbed out. */
    private static final class TestDeskBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestDeskBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
            return null;
        }

        @Override
        protected void wakeNativeWindow() {
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }

        @Override
        protected boolean hasWindowSurface() {
            return true;
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }

        @Override
        protected void disposeNativeWindow() {
        }

        @Override
        protected void setShellWindowTitle(long context, String title) {
        }

        @Override
        protected void setShellWindowState(long context, int state) {
        }

        @Override
        protected void setShellWindowVisible(long context, boolean visible) {
        }

        @Override
        protected void forwardWindowBounds(Rectangle bounds) {
        }

        @Override
        public void setWindowTitle(DartControl control, String title) {
        }
    }
}
