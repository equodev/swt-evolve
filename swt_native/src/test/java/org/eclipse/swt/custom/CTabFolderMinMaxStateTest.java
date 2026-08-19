package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The min/max state of a CTabFolder belongs to its CTabFolder2Listener, not to the folder.
 * Upstream {@code CTabFolder.onSelection} only notifies the listeners and never assigns
 * {@code minimized}/{@code maximized} itself (see Snippet165, where the application does it).
 *
 * Eclipse's E4 workbench relies on that: {@code MinMaxAddon.setCTFButtons} marks a stack tagged
 * "Minimized" as {@code setMaximized(true)} with the minimize button hidden, so the one remaining
 * button reads and behaves as <em>restore</em> and returns the view to its previous docked
 * position. A folder that reports {@code maximized == false} in that state offers "maximize"
 * instead, and the whole workbench collapses into the one view.
 */
@ExtendWith(Mocks.class)
class CTabFolderMinMaxStateTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private CTabFolder folder() {
        Shell shell = Mocks.swtShell();
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        return folder;
    }

    /** What MinMaxAddon.setCTFButtons does to a stack that has just gained the "Minimized" tag. */
    private static void e4MinimizedPresentation(CTabFolder folder) {
        folder.setMinimizeVisible(false);
        folder.setMaximizeVisible(true);
        folder.setMaximized(true);
    }

    @Test
    @DisplayName("minimize leaves the listener's maximized decision intact")
    void minimizeKeepsListenerState() {
        CTabFolder folder = folder();
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void minimize(CTabFolderEvent event) {
                e4MinimizedPresentation(folder);
            }
        });

        CTabFolderHelper.handleMinimize((DartCTabFolder) folder.getImpl(), new Event());

        assertThat(folder.getMaximized())
                .as("a minimized stack must keep the maximized flag its listener set, so its "
                        + "remaining button acts as restore; clearing it turns the next click "
                        + "into a maximize that fills the whole window")
                .isTrue();
        assertThat(folder.getMinimizeVisible())
                .as("the listener hid the minimize button; the helper must not resurrect it")
                .isFalse();
    }

    @Test
    @DisplayName("maximize leaves the listener's decision intact")
    void maximizeKeepsListenerState() {
        CTabFolder folder = folder();
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void maximize(CTabFolderEvent event) {
                folder.setMaximized(false);
            }
        });

        CTabFolderHelper.handleMaximize((DartCTabFolder) folder.getImpl(), new Event());

        assertThat(folder.getMaximized())
                .as("the listener vetoed the maximize by leaving the flag clear; the helper "
                        + "must not force it back on")
                .isFalse();
    }

    @Test
    @DisplayName("restore leaves the listener's decision intact")
    void restoreKeepsListenerState() {
        CTabFolder folder = folder();
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void restore(CTabFolderEvent event) {
                e4MinimizedPresentation(folder);
            }
        });

        CTabFolderHelper.handleRestore((DartCTabFolder) folder.getImpl(), new Event());

        assertThat(folder.getMaximized())
                .as("restore of a maximized-but-still-minimized stack returns it to the flyout "
                        + "presentation, which the listener expresses as maximized=true")
                .isTrue();
    }

    @Test
    @DisplayName("with no listener the folder keeps the state SWT gave it")
    void noListenerLeavesStateUntouched() {
        CTabFolder folder = folder();

        CTabFolderHelper.handleMinimize((DartCTabFolder) folder.getImpl(), new Event());

        assertThat(folder.getMinimized())
                .as("upstream SWT never sets the flag itself — an application that registers no "
                        + "CTabFolder2Listener gets inert min/max buttons")
                .isFalse();
        assertThat(folder.getMaximized()).isFalse();
    }
}
