package org.eclipse.swt.custom;

import dev.equo.swt.harness.WebDisplayHarness;
import dev.equo.swt.harness.UserInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The chevron of an overflowing folder is {@code CTabFolder2Listener.showList}: the application
 * gets the chevron's bounds and either shows its own list (the workbench shows its part list and
 * sets {@code doit=false}) or leaves the folder to show its default one.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CTabFolderChevronShowListFlutterTest {

    private static final int FOLDER_WIDTH = 400;

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
        // Closes the folder's own list if a test opened it.
        flutter.input().press(UserInput.Key.ESCAPE);
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

    private CTabFolder overflowingFolder() {
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setBounds(0, 0, FOLDER_WIDTH, 200);
        for (int i = 1; i <= 12; i++) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(String.format("note%02d.txt", i));
        }
        folder.setSelection(0);
        settle();
        return folder;
    }

    /** Clicks the chevron, which ends the strip; answers where, in the folder's coordinates. */
    private int[] clickChevron(CTabFolder folder) {
        double[] rect = flutter.renderedRect(folder);
        int x = (int) rect[2] - 9;
        int y = 16;
        flutter.input().click(rect[0] + x, rect[1] + y);
        return new int[] {x, y};
    }

    private int clientShowListSeq(CTabFolder folder) {
        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) flutter.queryState(folder).get("state");
        Object seq = state.get("showListPopupSeq");
        return seq == null ? 0 : ((Number) seq).intValue();
    }

    @Test
    @DisplayName("the chevron reaches showList with its bounds in the folder")
    void chevronReachesShowList() {
        CTabFolder folder = overflowingFolder();
        List<CTabFolderEvent> events = new ArrayList<>();
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void showList(CTabFolderEvent event) {
                events.add(event);
                // The application shows its own list, as the workbench does.
                event.doit = false;
            }
        });

        int[] clicked = clickChevron(folder);
        settleUntil(() -> !events.isEmpty());

        assertThat(events).as("one click, one showList").hasSize(1);
        CTabFolderEvent event = events.get(0);
        assertThat(event.widget).isSameAs(folder);
        Rectangle chevron = new Rectangle(event.x, event.y, event.width, event.height);
        assertThat(chevron.width).isPositive();
        assertThat(chevron.height).isPositive();
        assertThat(chevron.contains(clicked[0], clicked[1]))
                .as("the bounds %s are the chevron's, which was clicked at %d,%d", chevron, clicked[0], clicked[1])
                .isTrue();
        assertThat(chevron.x + chevron.width).isLessThanOrEqualTo(FOLDER_WIDTH);
        assertThat(chevron.y).isGreaterThanOrEqualTo(0);

        settle();
        assertThat(clientShowListSeq(folder))
                .as("a listener that set doit=false keeps the folder's own list closed")
                .isZero();
    }

    @Test
    @DisplayName("when no listener takes the chevron, the folder is asked for its own list")
    void untakenChevronOpensTheDefaultList() {
        CTabFolder folder = overflowingFolder();
        int[] showLists = {0};
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void showList(CTabFolderEvent event) {
                showLists[0]++;
            }
        });

        clickChevron(folder);
        settleUntil(() -> clientShowListSeq(folder) > 0);

        assertThat(showLists[0]).isOne();
        assertThat(clientShowListSeq(folder)).isOne();
    }
}
