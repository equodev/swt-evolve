package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An Eclipse application's theme is the one its workspace names, and it outranks the system's: a
 * light workbench on a dark desktop is drawn light, as it is natively.
 */
class ConfigWorkspaceThemeTest {

    private static final String INSTANCE_AREA = "osgi.instance.area";
    private static final String E4_LIGHT = "org.eclipse.e4.ui.css.theme.e4_default";

    @TempDir
    Path workspace;

    private ConfigFlags savedFlags;
    private String savedArea;
    private String savedForceTheme;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedArea = System.getProperty(INSTANCE_AREA);
        savedForceTheme = System.getProperty(Config.FORCE_THEME);
        System.clearProperty(INSTANCE_AREA);
        System.clearProperty(Config.FORCE_THEME);
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        restore(INSTANCE_AREA, savedArea);
        restore(Config.FORCE_THEME, savedForceTheme);
        Config.setConfigFlags(savedFlags);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private void workspaceWithTheme(String themeId) throws IOException {
        Path settings = workspace.resolve(".metadata/.plugins/org.eclipse.core.runtime/.settings");
        Files.createDirectories(settings);
        Files.write(settings.resolve("org.eclipse.e4.ui.css.swt.theme.prefs"),
                ("eclipse.preferences.version=1\nthemeid=" + themeId + "\n").getBytes(StandardCharsets.UTF_8));
    }

    private void openWorkspace() {
        System.setProperty(INSTANCE_AREA, workspace.toUri().toString());
    }

    private static SystemAppearance dark() {
        SystemAppearance system = new SystemAppearance();
        system.report("dark");
        return system;
    }

    @Test
    void aLightWorkspaceIsDrawnLightOnADarkSystem() throws IOException {
        workspaceWithTheme(E4_LIGHT);
        openWorkspace();

        assertThat(Config.appTheme()).isEqualTo("light");
        assertThat(Config.darkTheme(dark())).isFalse();
    }

    @Test
    void theFlagOutranksTheWorkspace() throws IOException {
        workspaceWithTheme(E4_LIGHT);
        openWorkspace();
        System.setProperty(Config.FORCE_THEME, "dark");

        int changes = Config.workspaceThemeChanges();

        assertThat(Config.appTheme()).isEqualTo("dark");
        assertThat(Config.workspaceThemeChanges()).isEqualTo(changes);
    }

    @Test
    void aWorkspaceChosenAfterTheDisplayStartedNamesTheTheme() throws IOException {
        workspaceWithTheme(E4_LIGHT);
        assertThat(Config.appTheme()).as("no workspace yet").isNull();
        int changes = Config.workspaceThemeChanges();

        openWorkspace();

        assertThat(Config.appTheme()).as("read as soon as the workspace is there").isEqualTo("light");
        assertThat(Config.workspaceThemeChanges()).as("counted once").isEqualTo(changes + 1);
    }

    @Test
    void aWorkspaceWithoutAThemeLeavesItToTheSystem() {
        int changes = Config.workspaceThemeChanges();

        openWorkspace();

        assertThat(Config.workspaceThemeChanges()).isEqualTo(changes);
        assertThat(Config.darkTheme(dark())).isTrue();
    }
}
