package org.eclipse.swt.dnd;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
class TreeCopyIdClipboardFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;
    private Shell shell;
    private Tree tree;

    @BeforeEach
    void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
                "OS clipboard is unavailable in a headless environment");
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        tree = new Tree(shell, SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    private static void copyIdToClipboard(Display display, String idForDisplay) {
        Clipboard cb = new Clipboard(display);
        try {
            cb.setContents(new String[] { idForDisplay }, new Transfer[] { TextTransfer.getInstance() });
        } finally {
            cb.dispose();
        }
    }

    @Test
    @DisplayName("copying a tree item's id via TextTransfer reaches the system clipboard")
    void copyIdCopiesTreeItemTextToSystemClipboard() throws Exception {
        TreeItem item = new TreeItem(tree, SWT.NONE);
        String id = "Test Cases/My Test Case";
        item.setText(id);

        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection("SENTINEL-not-copied-yet"), null);

        tree.setSelection(item);
        copyIdToClipboard(display, item.getText());

        Object pasted = Toolkit.getDefaultToolkit().getSystemClipboard()
                .getData(DataFlavor.stringFlavor);
        assertThat(pasted).isEqualTo(id);
    }
}
