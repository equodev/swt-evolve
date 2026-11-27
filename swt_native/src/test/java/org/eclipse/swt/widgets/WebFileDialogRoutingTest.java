package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;

/**
 * A whole-tree app opens the Flutter file and directory choosers on every surface. With the native
 * library loaded, {@code dev.equo.swt.filedialogs=eclipse} brings back the OS ones; pure web
 * ({@code dev.equo.swt.loadLibrary=false}) has no OS chooser and ignores it.
 */
@Tag("native-unit")
class WebFileDialogRoutingTest {

    // Every DisplayBridge, the desktop one included, sets this to false.
    private static final String HYBRID = "dev.equo.swt.desktop";
    private static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    private String previousHybrid;
    private String previousMode;
    private String previousLoadLibrary;
    private String previousFileDialogs;

    @BeforeEach
    void wholeTreeApp() {
        previousHybrid = System.getProperty(HYBRID);
        previousMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        previousLoadLibrary = System.getProperty(LOAD_LIBRARY);
        previousFileDialogs = System.getProperty(Config.FILE_DIALOGS);
        System.setProperty(HYBRID, "false");
        System.clearProperty(Config.FILE_DIALOGS);
    }

    @AfterEach
    void restore() {
        restore(HYBRID, previousHybrid);
        restore(ConfigFlags.MODE_PROPERTY, previousMode);
        restore(LOAD_LIBRARY, previousLoadLibrary);
        restore(Config.FILE_DIALOGS, previousFileDialogs);
    }

    private static void restore(String key, String value) {
        if (value == null)
            System.clearProperty(key);
        else
            System.setProperty(key, value);
    }

    private static void browserTab() {
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
    }

    private static void desktop() {
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
    }

    private static void hybrid() {
        System.setProperty(LOAD_LIBRARY, "true");
    }

    private static void pureWeb() {
        System.setProperty(LOAD_LIBRARY, "false");
    }

    private static void osChoosers() {
        System.setProperty(Config.FILE_DIALOGS, "eclipse");
    }

    @Test
    void pureWebOpensTheFlutterChoosers() {
        pureWeb();
        browserTab();

        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(DartFileDialog.class);
        assertThat(new DirectoryDialog(DartMocks.dartShell(), SWT.NONE).getImpl()).isInstanceOf(DartDirectoryDialog.class);
    }

    @Test
    void pureWebIgnoresTheOsChooserProperty() {
        pureWeb();
        browserTab();
        osChoosers();

        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(DartFileDialog.class);
        assertThat(new DirectoryDialog(DartMocks.dartShell(), SWT.NONE).getImpl()).isInstanceOf(DartDirectoryDialog.class);
    }

    @Test
    void hybridOpensTheFlutterChoosersByDefault() {
        hybrid();
        desktop();
        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(DartFileDialog.class);
        assertThat(new DirectoryDialog(DartMocks.dartShell(), SWT.NONE).getImpl()).isInstanceOf(DartDirectoryDialog.class);

        ConfigFlags.setMode(ConfigFlags.MODE_CHROMIUM);
        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(DartFileDialog.class);

        browserTab();
        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(DartFileDialog.class);
    }

    @Test
    void hybridOpensTheOsChoosersWithTheProperty() {
        hybrid();
        osChoosers();
        desktop();
        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(SwtFileDialog.class);
        assertThat(new DirectoryDialog(DartMocks.dartShell(), SWT.NONE).getImpl()).isInstanceOf(SwtDirectoryDialog.class);

        ConfigFlags.setMode(ConfigFlags.MODE_CHROMIUM);
        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(SwtFileDialog.class);

        browserTab();
        assertThat(new FileDialog(DartMocks.dartShell(), SWT.OPEN).getImpl()).isInstanceOf(SwtFileDialog.class);
    }
}
