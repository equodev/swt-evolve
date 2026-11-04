package org.eclipse.swt.custom;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The render side's strip report carries the width of the folder it was drawn in, and this side
 * answers from it only while the folder still has that width. The two widths have to agree once
 * the folder is drawn, or no report would ever be used.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CTabFolderStripReportWidthFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 500, 300);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void clear() {
        for (Control c : shell.getChildren()) c.dispose();
        settle();
    }

    private void settle() {
        int quiet = 0;
        for (int round = 0; round < 40 && quiet < 3; round++) {
            long before = flutter.dispatched();
            flutter.drain();
            flutter.flush();
            flutter.drain();
            quiet = flutter.dispatched() == before ? quiet + 1 : 0;
        }
    }

    private void settleUntil(BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 5_000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) settle();
    }

    @ParameterizedTest(name = "style {0}")
    @ValueSource(ints = {SWT.NONE, SWT.BORDER})
    @DisplayName("a drawn folder's strip is reported at the width this side laid it out at")
    void theStripIsReportedAtTheFolderWidth(int style) {
        CTabFolder folder = new CTabFolder(shell, style);
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        folder.setBounds(0, 0, 400, 200);
        for (int i = 1; i <= 12; i++) new CTabItem(folder, SWT.NONE).setText(String.format("note%02d.txt", i));
        folder.setSelection(0);

        settleUntil(() -> CTabFolderHelper.drawnTabs(impl) != null);
        assertThat(CTabFolderHelper.drawnTabs(impl)).as("the strip drawn at 400").isNotNull();

        folder.setSize(260, 200);
        settleUntil(() -> CTabFolderHelper.drawnTabs(impl) != null);
        assertThat(CTabFolderHelper.drawnTabs(impl)).as("the strip drawn again at 260").isNotNull();
    }
}
