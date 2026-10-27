package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.CTabFolderSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GCHelper;
import org.eclipse.swt.graphics.Point;
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

/**
 * A tab's width is what the render side draws, because the tab bounds are what answers
 * {@code getItem(Point)} - and that is how the workbench decides which view a drag picked up. A
 * width computed from anything other than the text's real extent puts the answer under a different
 * tab than the one the pointer is over, which moves the wrong view.
 *
 * <p>The label's extent is measured, not assumed, and in the style the render side draws it in -
 * {@link dev.equo.swt.size.CTabFolderSizes#tabLabelStyle()}, which comes out of a measurement rather
 * than being written down here. The surrounding geometry is still this side's own copy of the render
 * side's theme, so those assertions hold the arithmetic rather than the agreement.
 */
@ExtendWith(Mocks.class)
class CTabItemWidthMatchesRenderTest {

    private static final int FLAGS = SWT.DRAW_TRANSPARENT | SWT.DRAW_MNEMONIC | SWT.DRAW_DELIMITER;

    /** {@code CTabFolderRenderer.MINIMUM_SIZE}, which is package-private there. */
    private static final int MINIMUM_SIZE = 1 << 24;

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

    @Test
    @DisplayName("two labels of the same length but different extents get different widths")
    void widthFollowsTheTextsExtent() {
        CTabFolder folder = folder();
        CTabItem narrow = new CTabItem(folder, SWT.NONE);
        narrow.setText("llllllllll");
        CTabItem wide = new CTabItem(folder, SWT.NONE);
        wide.setText("WWWWWWWWWW");

        int narrowLabel = labelWidth(narrow);
        int wideLabel = labelWidth(wide);
        assertThat(wideLabel)
                .as("the premise: these two are not drawn the same width")
                .isGreaterThan(narrowLabel);

        assertThat(widthOf(folder, 1) - widthOf(folder, 0))
                .as("so the tabs holding them differ by exactly that - a width counted in "
                        + "characters would make them equal")
                .isEqualTo(wideLabel - narrowLabel);
    }

    @Test
    @DisplayName("a tab is its label plus the padding the render side draws around it")
    void widthIsTheLabelPlusTheRenderedPadding() {
        CTabFolder folder = folder();
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText("Section 3 (TVDSS)");

        assertThat(widthOf(folder, 0))
                .as("tabHorizontalPadding on each side plus the label's own trailing padding, and "
                        + "nothing else on a tab with no image and no close button")
                .isEqualTo(labelWidth(item) + CTabFolderSizes.TAB_LABEL_SURROUND);
    }

    @Test
    @DisplayName("the close button adds its spacing and its icon, and nothing else")
    void closeButtonAddsItsOwnWidth() {
        CTabFolder plain = folder();
        CTabItem item = new CTabItem(plain, SWT.NONE);
        item.setText("One tab");

        CTabFolder closable = folder();
        CTabItem closableItem = new CTabItem(closable, SWT.CLOSE);
        closableItem.setText("One tab");

        assertThat(widthOf(closable, 0) - widthOf(plain, 0))
                .isEqualTo(CTabFolderSizes.TAB_CLOSE_EXTRA);
    }

    @Test
    @DisplayName("a tab is not narrower when the strip is short of room")
    void widthDoesNotCompress() {
        CTabFolder folder = folder();
        folder.setMinimumCharacters(4);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText("Stratigraphic Modeling");

        int atMinimum = folder.getRenderer()
                .computeSize(0, MINIMUM_SIZE, null, SWT.DEFAULT, SWT.DEFAULT).x;

        assertThat(atMinimum)
                .as("the render side draws the whole label and hides the tabs that no longer fit, "
                        + "so there is no narrower tab to describe")
                .isEqualTo(widthOf(folder, 0));
    }

    @Test
    @DisplayName("a tab whose font was disposed is still measured")
    void survivesADisposedFont() {
        CTabFolder folder = folder();
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText("Section 3 (TVDSS)");

        Font font = new Font(Mocks.device(), Mocks.fontData());
        item.setFont(font);
        font.dispose();

        assertThat(widthOf(folder, 0))
                .as("a resize arrives from an async runnable, which can reach the measurement once "
                        + "the font is gone, and a disposed font answers nothing")
                .isEqualTo(labelWidth(item) + CTabFolderSizes.TAB_LABEL_SURROUND);
    }

    /**
     * The label at the size the render side draws it: its own measured style, applied here rather
     * than read back out of the code under test, so the two sides of every assertion below come
     * from different places.
     */
    private int labelWidth(CTabItem item) {
        return (int) Math.ceil(
                FontMetricsUtil.getFontSize(item.getText(), CTabFolderSizes.tabLabelStyle()).x());
    }

    private CTabFolder folder() {
        Shell shell = Mocks.shell();
        return new CTabFolder(shell, SWT.NONE);
    }

    private int widthOf(CTabFolder folder, int index) {
        Point size = folder.getRenderer().computeSize(index, SWT.NONE, null, SWT.DEFAULT, SWT.DEFAULT);
        return size.x;
    }
}
