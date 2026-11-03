package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.ToolItemSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.DartToolItem;
import org.eclipse.swt.widgets.Sizes;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@code bigger_icon_size} enlarges application icons only: a folder's minimize, maximize and
 * chevron buttons keep their image size, so the ToolBar holding them fits the tab row.
 */
@Tag("native-unit")
class CTabFolderOwnToolBarSizeTest {

    private static final int PAD_H = 6;

    private ConfigFlags savedFlags;

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
        savedFlags = Config.getConfigFlags();
        ConfigFlags flags = new ConfigFlags();
        flags.bigger_icon_size = true;
        Config.setConfigFlags(flags);
    }

    @AfterEach
    void tearDown() {
        Config.setConfigFlags(savedFlags);
        FlutterBridge.set(null);
    }

    @Test
    void the_folder_s_own_buttons_keep_their_image_size() {
        CTabFolder folder = new CTabFolder(DartMocks.dartShell(), SWT.NONE);
        new CTabItem(folder, SWT.NONE).setText("Item");
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        ToolBar appBar = new ToolBar(folder, SWT.FLAT);
        new ToolItem(appBar, SWT.PUSH).setImage(new Image(DartMocks.dartDisplay(), 16, 16));
        folder.setTopRight(appBar);
        ((DartCTabFolder) folder.getImpl()).updateButtons();

        int ownItems = 0;
        for (Control child : folder.getChildren()) {
            if (!(child instanceof ToolBar) || child == appBar) continue;
            assertThat(CTabFolderHelper.ownsToolBar((ToolBar) child)).isTrue();
            for (ToolItem item : ((ToolBar) child).getItems()) {
                assertThat(height(item)).isEqualTo(item.getImage().getBounds().height + PAD_H);
                ownItems++;
            }
        }
        assumeTrue(ownItems > 0, "this SWT release draws the folder's buttons without a ToolBar");
        assertThat(height(appBar.getItem(0))).as("an application bar on the folder is still enlarged")
                .isEqualTo((int) Math.round(ToolItemSizes.ICON_SIZE) + PAD_H);
    }

    private static int height(ToolItem item) {
        return Sizes.computeSize((DartToolItem) item.getImpl()).y;
    }
}
