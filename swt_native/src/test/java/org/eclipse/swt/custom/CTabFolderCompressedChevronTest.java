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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * With {@code ctabfolder_tab_scroll} off the render side shrinks the tabs towards
 * {@code minimumCharacters} before any goes behind the chevron, so the chevron and the tabs Java
 * reports as showing follow the tabs' minimum widths, not their full ones.
 */
@ExtendWith(Mocks.class)
class CTabFolderCompressedChevronTest {

    private static final int TABS = 9;
    /** Wide enough for every tab at one character, too narrow for their whole labels. */
    private static final int ROOMY = 800;
    /** Too narrow for even two tabs at one character. */
    private static final int NARROW = 60;
    /** Wide enough for every whole label. */
    private static final int WIDE = 4000;
    private static final int MINIMUM_SIZE = 1 << 24;

    private boolean tabScroll;

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
        tabScroll = Config.getConfigFlags().ctabfolder_tab_scroll;
    }

    @AfterEach
    void tearDown() {
        Config.getConfigFlags().ctabfolder_tab_scroll = tabScroll;
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("compressed tabs that all fit show no chevron and are all showing")
    void compressedTabsThatFitHaveNoChevron() {
        Config.getConfigFlags().ctabfolder_tab_scroll = false;
        CTabFolder folder = folder();

        resize(folder, ROOMY);

        assertThat(fullWidths(folder))
                .as("the premise: the whole labels do not fit")
                .isGreaterThan(ROOMY);
        assertThat(chevron(folder)).isFalse();
        assertThat(showing(folder)).containsOnly(true);
    }

    @Test
    @DisplayName("widening a compressed folder until every tab fits drops the chevron")
    void wideningUntilEveryTabFitsDropsTheChevron() {
        Config.getConfigFlags().ctabfolder_tab_scroll = false;
        CTabFolder folder = folder();

        resize(folder, NARROW);
        assertThat(chevron(folder)).as("the premise: narrow, some tabs are hidden").isTrue();

        for (int width = NARROW; width <= ROOMY; width += 10) {
            resize(folder, width);
        }

        assertThat(chevron(folder)).isFalse();
        assertThat(showing(folder)).containsOnly(true);
    }

    @Test
    @DisplayName("widening a folder already sent sends that the chevron went and every tab shows")
    void wideningAfterSendSendsChevronAndShowing() {
        Config.getConfigFlags().ctabfolder_tab_scroll = false;
        CTabFolder folder = folder();
        resize(folder, NARROW);
        VCTabFolder value = ((DartCTabFolder) folder.getImpl()).getValue();
        value.clearDirty();
        for (CTabItem item : folder.getItems()) {
            ((DartCTabItem) item.getImpl()).getValue().clearDirty();
        }

        resize(folder, ROOMY);

        assertThat(value.changedNames()).as("the folder's changes to send").contains("showChevron");
        for (int i = 1; i < folder.getItemCount(); i++) {
            assertThat(((DartCTabItem) folder.getItem(i).getImpl()).getValue().changedNames())
                    .as("tab %d's changes to send", i)
                    .contains("showing");
        }
    }

    @Test
    @DisplayName("a scrolling strip keeps the chevron while the whole labels do not fit")
    void scrollingStripFollowsTheWholeLabels() {
        Config.getConfigFlags().ctabfolder_tab_scroll = true;
        CTabFolder folder = folder();

        resize(folder, ROOMY);

        assertThat(chevron(folder)).isTrue();
    }

    @Test
    @DisplayName("a scrolling strip widened after a send leaves the chevron and showing to the render side")
    void scrollingStripWideningSendsNoChevronNorShowing() {
        Config.getConfigFlags().ctabfolder_tab_scroll = true;
        CTabFolder folder = folder();
        resize(folder, NARROW);
        assertThat(chevron(folder)).as("the premise: narrow, the chevron shows").isTrue();
        VCTabFolder value = ((DartCTabFolder) folder.getImpl()).getValue();
        value.clearDirty();
        for (CTabItem item : folder.getItems()) {
            ((DartCTabItem) item.getImpl()).getValue().clearDirty();
        }

        resize(folder, WIDE);

        assertThat(chevron(folder)).as("the premise: every whole label fits").isFalse();
        assertThat(value.changedNames()).as("the folder's changes to send").doesNotContain("showChevron");
        for (int i = 0; i < folder.getItemCount(); i++) {
            assertThat(((DartCTabItem) folder.getItem(i).getImpl()).getValue().changedNames())
                    .as("tab %d's changes to send", i)
                    .doesNotContain("showing");
        }
    }

    @Test
    @DisplayName("a compressed tab's label loses its mnemonic before it is cut to minimumCharacters")
    void minimumLabelCutsTheDrawnText() {
        Config.getConfigFlags().ctabfolder_tab_scroll = false;
        CTabFolder folder = folder();
        folder.setMinimumCharacters(3);
        folder.getItem(1).setText("&Search");
        folder.getItem(2).setText("Sea");
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();

        assertThat(CTabFolderHelper.drawnSize(impl, 1, MINIMUM_SIZE).x)
                .isEqualTo(CTabFolderHelper.drawnSize(impl, 2, MINIMUM_SIZE).x);
    }

    private CTabFolder folder() {
        Shell shell = Mocks.shell();
        when(shell.getDisplay().getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1080));
        CTabFolder folder = new CTabFolder(shell, SWT.CLOSE);
        folder.setMinimumCharacters(1);
        for (int i = 0; i < TABS; i++) {
            new CTabItem(folder, SWT.NONE).setText("A rather long tab title " + i);
        }
        folder.setSelection(0);
        return folder;
    }

    /**
     * Resizes the folder and runs its Resize handler, which lays the tabs out: the mocked display
     * does not dispatch the event.
     */
    private void resize(CTabFolder folder, int width) {
        folder.setBounds(0, 0, width, 200);
        ((DartCTabFolder) folder.getImpl()).onResize(new Event());
    }

    private int fullWidths(CTabFolder folder) {
        int sum = 0;
        for (int i = 0; i < folder.getItemCount(); i++) {
            sum += folder.getRenderer().computeSize(i, SWT.NONE, null, SWT.DEFAULT, SWT.DEFAULT).x;
        }
        return sum;
    }

    private boolean chevron(CTabFolder folder) {
        return ((DartCTabFolder) folder.getImpl())._showChevron();
    }

    private Boolean[] showing(CTabFolder folder) {
        return Arrays.stream(folder.getItems()).map(CTabItem::isShowing).toArray(Boolean[]::new);
    }
}
