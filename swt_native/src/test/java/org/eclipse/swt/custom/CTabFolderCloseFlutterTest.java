package org.eclipse.swt.custom;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Closing a tab disposes its item, and the client has to stop drawing it: the folder's tab list is
 * what the client draws the tabs from.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CTabFolderCloseFlutterTest {

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

    private CTabFolder folderWithTabs(String... names) {
        CTabFolder folder = new CTabFolder(shell, SWT.CLOSE | SWT.BORDER);
        folder.setBounds(0, 0, 480, 260);
        for (String name : names) {
            CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(name);
            Composite body = new Composite(folder, SWT.NONE);
            item.setControl(body);
        }
        folder.setSelection(0);
        return folder;
    }

    /** The tab texts the client holds for {@code folder}, in order. */
    @SuppressWarnings("unchecked")
    private List<String> clientTabs(CTabFolder folder) {
        Map<String, Object> state = (Map<String, Object>) flutter.queryState(folder).get("state");
        List<Map<String, Object>> items = (List<Map<String, Object>>) state.get("items");
        return items.stream().map(i -> {
            Object text = i.get("text");
            if (text != null) return String.valueOf(text);
            // Named rather than described: look the item up on its own.
            return String.valueOf(i.get("id"));
        }).toList();
    }

    @Test
    @DisplayName("a closed tab is gone from the tabs the client draws")
    void closedTabIsGone() {
        CTabFolder folder = folderWithTabs("one", "two", "three");
        settle();
        assertThat(clientTabs(folder)).as("sanity").hasSize(3);

        folder.getItem(1).dispose();
        settle();

        assertThat(clientTabs(folder)).hasSize(2);
    }

    @Test
    @DisplayName("closing the selected tab selects another, as the client shows it")
    void closingTheSelectedTab() {
        CTabFolder folder = folderWithTabs("one", "two", "three");
        folder.setSelection(1);
        settle();

        folder.getItem(1).dispose();
        settle();

        assertThat(clientTabs(folder)).hasSize(2);
        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) flutter.queryState(folder).get("state");
        assertThat(((Number) state.get("selection")).intValue()).isEqualTo(folder.getSelectionIndex());
    }

    @Test
    @DisplayName("closing the only tab leaves the client drawing none")
    void closingTheOnlyTab() {
        CTabFolder folder = folderWithTabs("only");
        settle();
        assertThat(clientTabs(folder)).as("sanity").hasSize(1);

        folder.getItem(0).dispose();
        settle();

        assertThat(clientTabs(folder)).isEmpty();
    }
}
