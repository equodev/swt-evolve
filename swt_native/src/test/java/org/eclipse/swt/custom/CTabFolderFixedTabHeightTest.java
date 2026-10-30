package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.CTabFolderSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * As natively, the strip of a fixed tab height {@code h} is {@code h + 1} tall (the line across the
 * top of the tabs), and a fixed height of 0 removes it together with the frame around the page. The
 * folder applies a new height in its deferred update, so each test runs the event loop after it.
 */
class CTabFolderFixedTabHeightTest {

    private static final int W = 600;
    private static final int H = 400;

    private Display display;
    private Shell shell;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private CTabFolder folder(int style) {
        CTabFolder folder = new CTabFolder(shell, style);
        folder.setBounds(0, 0, W, H);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        Composite page = new Composite(folder, SWT.NONE);
        item.setControl(page);
        folder.setSelection(item);
        drain();
        return folder;
    }

    private void setTabHeight(CTabFolder folder, int height) {
        folder.setTabHeight(height);
        drain();
    }

    private void drain() {
        while (display.readAndDispatch()) {
            // run the folder's deferred update
        }
    }

    private static java.util.Map<String, Object> serialize(CTabFolder folder) throws IOException {
        return dev.equo.swt.harness.WireFrames.describe(folder);
    }

    @Test
    void theDefaultStripIsUnchanged() {
        assertThat(folder(SWT.NONE).getClientArea().y).isEqualTo(CTabFolderSizes.TAB_STRIP_HEIGHT);
    }

    /**
     * The strip drawn is taller than upstream's tabs, but the tabs keep upstream's height: an
     * application that aims at a tab through its bounds has to hit the same point as natively.
     */
    @Test
    void theDefaultTabsKeepUpstreamsHeight() {
        CTabFolder folder = folder(SWT.NONE);

        assertThat(folder.getTabHeight()).isEqualTo(24);
        assertThat(folder.getItem(0).getBounds().height).isEqualTo(25);
    }

    @Test
    void aZeroTabHeightGivesThePageTheWholeFolder() {
        CTabFolder folder = folder(SWT.BORDER | SWT.SINGLE);
        setTabHeight(folder, 0);
        Rectangle client = folder.getClientArea();

        assertThat(folder.getTabHeight()).isZero();
        assertThat(new int[] { client.x, client.y, client.width, client.height })
                .containsExactly(0, 0, W, H);
    }

    @Test
    void aFixedTabHeightSizesTheStrip() {
        CTabFolder folder = folder(SWT.BORDER | SWT.SINGLE);
        setTabHeight(folder, 8);

        assertThat(folder.getTabHeight()).isEqualTo(8);
        assertThat(folder.getClientArea().y).isEqualTo(9);
    }

    @Test
    void aFixedTabHeightSizesABottomStrip() {
        CTabFolder folder = folder(SWT.NONE);
        folder.setTabPosition(SWT.BOTTOM);
        setTabHeight(folder, 8);
        Rectangle client = folder.getClientArea();

        assertThat(H - client.y - client.height).isEqualTo(9);
    }

    @Test
    void aLaidOutFolderMovesItsPageUnderTheNewStrip() {
        CTabFolder folder = folder(SWT.BORDER | SWT.SINGLE);
        Control page = folder.getItem(0).getControl();
        setTabHeight(folder, 8);

        assertThat(page.getBounds().y).isEqualTo(9);
    }

    @Test
    void computeTrimIsTheInverseOfGetClientArea() {
        for (int height : new int[] { 0, 8, SWT.DEFAULT }) {
            CTabFolder folder = folder(SWT.BORDER);
            setTabHeight(folder, height);
            Rectangle client = folder.getClientArea();
            Rectangle trim = folder.computeTrim(client.x, client.y, client.width, client.height);

            assertThat(new int[] { trim.x, trim.y, trim.width, trim.height })
                    .as("tab height %d", height)
                    .containsExactly(0, 0, W, H);
        }
    }

    @Test
    void theRenderSideIsToldTheStripHeight() throws IOException {
        CTabFolder folder = folder(SWT.NONE);
        setTabHeight(folder, 8);

        assertThatWire(serialize(folder)).isObject().containsEntry("tabHeight", 9);
    }

    @Test
    void aFolderWithNoStripSaysSoInItsFullPayload() throws IOException {
        CTabFolder folder = folder(SWT.NONE);
        setTabHeight(folder, 0);

        assertThatWire(serialize(folder)).isObject().containsEntry("tabHeight", 0);
    }
}
