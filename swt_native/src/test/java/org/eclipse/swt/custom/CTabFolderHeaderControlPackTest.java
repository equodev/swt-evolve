package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Laying out the header controls must leave them at the size a {@code pack()} would give them.
 *
 * The workbench packs a header control back to its preferred size every time a toolbar model
 * change reaches it, which happens dozens of times while a view is being populated. If the folder
 * has stretched the control to a different height, the two overwrite each other once per
 * event-loop turn — and every one of those bounds changes re-serializes the whole folder.
 */
@ExtendWith(Mocks.class)
class CTabFolderHeaderControlPackTest {

    /** A preferred size the folder's header height will not accidentally match. */
    private static final int PREFERRED_HEIGHT = 7;

    static class FixedSizeLayout extends Layout {
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean changed) {
            return new Point(40, PREFERRED_HEIGHT);
        }

        @Override
        protected void layout(Composite composite, boolean changed) {
        }
    }

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
    void layingOutTheHeaderLeavesTheTopRightControlAtItsPackedSize() {
        Shell shell = Mocks.swtShell();
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setBounds(0, 0, 400, 200);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText("a tab");

        Composite topRight = new Composite(folder, SWT.NONE);
        topRight.setLayout(new FixedSizeLayout());
        folder.setTopRight(topRight);

        ((DartCTabFolder) folder.getImpl()).setButtonBounds();
        Rectangle afterLayout = topRight.getBounds();

        topRight.pack();

        assertThat(topRight.getBounds())
                .as("a pack() after the header layout must not move the control")
                .isEqualTo(afterLayout);
        assertThat(afterLayout.height)
                .as("the header layout must leave the control at its preferred height")
                .isEqualTo(PREFERRED_HEIGHT);
    }
}
