package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.DesktopAppearanceStub;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.NoopBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An Eclipse IDE started without {@code -data} creates its Display, shows the workspace chooser in the
 * system's variant, and only then learns the workspace and the theme it names. What it took from the
 * Display before that follows the workspace's variant, as after a native settings change.
 */
class DeskWorkspaceThemeTest {

    private static final String INSTANCE_AREA = "osgi.instance.area";

    @TempDir
    Path workspace;

    private Display display;
    private String savedMode;
    private String savedArea;
    private String savedForceTheme;
    private ConfigFlags savedFlags;
    private DesktopAppearanceStub desktop;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        if (desktop != null) desktop.close();
        restore(ConfigFlags.MODE_PROPERTY, savedMode);
        restore(INSTANCE_AREA, savedArea);
        restore("swt.evolve.force_theme", savedForceTheme);
        Config.setConfigFlags(savedFlags);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private ThemeBridge darkDesktopWithNoWorkspace() {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        savedArea = System.getProperty(INSTANCE_AREA);
        savedForceTheme = System.getProperty("swt.evolve.force_theme");
        savedFlags = Config.getConfigFlags();
        System.clearProperty(INSTANCE_AREA);
        System.clearProperty("swt.evolve.force_theme");
        Config.setConfigFlags(null);
        desktop = new DesktopAppearanceStub("dark");
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        ThemeBridge bridge = new ThemeBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private void chooseLightWorkspace() throws IOException {
        Path settings = workspace.resolve(".metadata/.plugins/org.eclipse.core.runtime/.settings");
        Files.createDirectories(settings);
        Files.write(settings.resolve("org.eclipse.e4.ui.css.swt.theme.prefs"),
                "themeid=org.eclipse.e4.ui.css.theme.e4_default\n".getBytes(StandardCharsets.UTF_8));
        System.setProperty(INSTANCE_AREA, workspace.toUri().toString());
    }

    @Test
    void aSystemColourTakenBeforeTheWorkspaceFollowsItsTheme() throws IOException {
        ThemeBridge bridge = darkDesktopWithNoWorkspace();
        Color listBackground = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        RGB dark = listBackground.getRGB();

        int sentBefore = bridge.comm.sent.size();

        chooseLightWorkspace();
        bridge.onUpdate();

        RGB now = listBackground.getRGB();
        assertThat(now).as("the colour the application kept").isNotEqualTo(dark);
        assertThat(now.red + now.green + now.blue).as("a light background").isGreaterThan(600);
        assertThat(bridge.comm.sent.size()).as("the client is sent the theme").isGreaterThan(sentBefore);
    }

    private static final class ThemeBridge extends DeskDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        ThemeBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
        }

        @Override
        protected boolean hasNativeWindow() {
            return true;
        }

        @Override
        protected int pumpWindow() {
            return 0;
        }
    }
}
