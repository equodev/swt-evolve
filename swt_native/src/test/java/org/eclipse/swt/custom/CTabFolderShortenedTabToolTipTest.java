package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * A tab drawn too narrow for its text shows the text shortened, and resting on it shows the whole
 * text as its tooltip when the tab has none of its own: upstream's {@code CTabItem.getToolTipText}
 * falls back to the text whenever the renderer had to shorten it. A view stack squeezed narrow
 * relies on that, since its tabs carry no tooltip.
 */
@ExtendWith(Mocks.class)
class CTabFolderShortenedTabToolTipTest {

    private static final int FOLDER_WIDTH = 260;

    private static final int TAB_HEIGHT = 32;

    private static final String[] LABELS = {"Seismic Interpretation", "Stratigraphic Modeling", "Well Section"};

    /** Each tab drawn 60 px wide: icon, one letter and its close button. */
    private static final int[] SQUEEZED = {0, 60, 60, 60, 120, 60};

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

    /** The render side's word on which labels it draws shortened: all three. */
    private static final String ALL_SHORTENED = "1,1,1";

    @Test
    @DisplayName("a shortened tab without a tooltip of its own shows its whole text")
    void shortenedTabShowsItsText() {
        CTabFolder folder = squeezedFolder(ALL_SHORTENED);

        assertThat(toolTipAt(folder, 20)).isEqualTo(LABELS[0]);
        assertThat(toolTipAt(folder, 80)).isEqualTo(LABELS[1]);
        assertThat(folder.getItem(0).getToolTipText()).isEqualTo(LABELS[0]);
    }

    @Test
    @DisplayName("a shortened tab with a tooltip of its own shows that tooltip")
    void shortenedTabKeepsItsOwnToolTip() {
        CTabFolder folder = squeezedFolder(ALL_SHORTENED);
        folder.getItem(0).setToolTipText("Own tooltip");

        assertThat(toolTipAt(folder, 20)).isEqualTo("Own tooltip");
    }

    @Test
    @DisplayName("a tab drawn whole without a tooltip of its own shows none")
    void wholeTabShowsNoToolTip() {
        CTabFolder folder = squeezedFolder("0,1,0");

        assertThat(toolTipAt(folder, 20)).isNull();
        assertThat(folder.getItem(0).getToolTipText()).isNull();
        assertThat(toolTipAt(folder, 80)).isEqualTo(LABELS[1]);
    }

    @Test
    @DisplayName("a tab no longer drawn shortened stops showing its text")
    void tabWidenedAgainDropsTheToolTip() {
        CTabFolder folder = squeezedFolder(ALL_SHORTENED);
        report(folder, "0,1,0");

        assertThat(toolTipAt(folder, 20)).isNull();
    }

    @Test
    @DisplayName("the close button drawn on a shortened tab shows the close tooltip")
    void drawnCloseButtonShowsCloseToolTip() {
        CTabFolder folder = squeezedFolder(ALL_SHORTENED);
        report(folder, "40,8,16,16,0,0,0,0,0,0,0,0", ALL_SHORTENED);

        assertThat(toolTipAt(folder, 48)).isEqualTo(SWT.getMessage("SWT_Close"));
        assertThat(toolTipAt(folder, 20)).isEqualTo(LABELS[0]);
    }

    @Test
    @DisplayName("beside a drawn close button, and on a tab drawn without one, the tab's tooltip shows")
    void besideTheDrawnCloseButtonShowsTheTabToolTip() {
        CTabFolder folder = squeezedFolder(ALL_SHORTENED);
        report(folder, "40,8,16,16,0,0,0,0,0,0,0,0", ALL_SHORTENED);

        assertThat(toolTipAt(folder, 58, TAB_HEIGHT / 2)).as("right of it").isEqualTo(LABELS[0]);
        assertThat(toolTipAt(folder, 48, 4)).as("above it").isEqualTo(LABELS[0]);
        assertThat(toolTipAt(folder, 48, 28)).as("below it").isEqualTo(LABELS[0]);
        assertThat(toolTipAt(folder, 80, TAB_HEIGHT / 2)).as("a tab drawn without one").isEqualTo(LABELS[1]);
    }

    @Test
    @DisplayName("a tab added after the strip was last laid out has no shortened label yet")
    void tabAddedAfterTheLayoutHasNoShortenedLabel() {
        CTabFolder folder = squeezedFolder(ALL_SHORTENED);
        CTabItem added = new CTabItem(folder, SWT.NONE);
        added.setText("Production Data");

        assertThat(added.getToolTipText()).isNull();
    }

    @Test
    @DisplayName("a tab inserted before a shortened one does not hand its tooltip to the wrong tab")
    void tabInsertedFirstDoesNotShiftTheShortenedLabels() {
        CTabFolder folder = squeezedFolder("0,1,0");
        CTabItem inserted = new CTabItem(folder, SWT.NONE, 0);
        inserted.setText("Production Data");

        assertThat(inserted.getToolTipText()).isNull();
        assertThat(folder.getItem(1).getText()).isEqualTo(LABELS[0]);
        assertThat(folder.getItem(1).getToolTipText()).isNull();
    }

    @Test
    @DisplayName("a tab disposed or moved before the next layout does not hand its tooltip to the wrong tab")
    void tabDisposedOrMovedDoesNotShiftTheShortenedLabels() {
        CTabFolder disposing = squeezedFolder("1,0,0");
        disposing.getItem(0).dispose();

        assertThat(disposing.getItem(0).getText()).isEqualTo(LABELS[1]);
        assertThat(disposing.getItem(0).getToolTipText()).isNull();

        CTabFolder moving = squeezedFolder("1,0,0");
        ((DartCTabFolder) moving.getImpl()).setItemOrder(new int[] {1, 0, 2});

        assertThat(moving.getItem(0).getText()).isEqualTo(LABELS[1]);
        assertThat(moving.getItem(0).getToolTipText()).isNull();
    }

    // ---- harness ----

    private CTabFolder squeezedFolder(String shortened) {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.CLOSE);
        folder.setBounds(0, 0, FOLDER_WIDTH, 300);
        folder.setTabHeight(TAB_HEIGHT);
        for (String label : LABELS) {
            new CTabItem(folder, SWT.NONE).setText(label);
        }
        report(folder, shortened);
        return folder;
    }

    /** The strip as the render side reports it: tabs; close buttons (none); shown part (none); shortened labels. */
    private static void report(CTabFolder folder, String shortened) {
        report(folder, "", shortened);
    }

    private static void report(CTabFolder folder, String closeButtons, String shortened) {
        StringBuilder strip = new StringBuilder();
        for (int value : SQUEEZED) {
            if (strip.length() > 0) strip.append(',');
            strip.append(value);
        }
        Event e = new Event();
        e.text = strip + ";" + closeButtons + ";;" + shortened;
        CTabFolderHelper.handleStripLaidOut((DartCTabFolder) folder.getImpl(), e);
    }

    private String toolTipAt(CTabFolder folder, int x) {
        return toolTipAt(folder, x, TAB_HEIGHT / 2);
    }

    private String toolTipAt(CTabFolder folder, int x, int y) {
        return ((DartCTabFolder) folder.getImpl())._getToolTip(x, y);
    }
}
