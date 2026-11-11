package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Controls hidden or reparented and shown again, as minimizing and restoring a view does, must be
 * drawn again. Checked in pixels.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HiddenCanvasRepaintFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @org.junit.jupiter.api.AfterEach
    void clear() {
        for (Shell s : shell.getShells()) s.dispose();
        for (Control c : shell.getChildren()) c.dispose();
        flutter.settle();
    }

    /**
     * Share of {@code w}'s middle painted red. Polls, since a moved control can take a round trip or
     * two to come back while the wire is quiet.
     */
    private double redShare(Control w) throws Exception {
        return flutter.colourShare(w, WebDisplayHarness.RED);
    }

    private Canvas redCanvas(Composite parent) {
        Canvas canvas = new Canvas(parent, SWT.NONE);
        canvas.setBounds(10, 10, 200, 120);
        Color red = new Color(display, 255, 0, 0);
        canvas.addListener(SWT.Paint, e -> {
            e.gc.setBackground(red);
            e.gc.fillRectangle(0, 0, 200, 120);
        });
        return canvas;
    }

    @Test
    @DisplayName("a canvas whose parent was hidden and shown again is drawn again")
    void hiddenAndShownAgain() throws Exception {
        Composite stack = new Composite(shell, SWT.NONE);
        stack.setBounds(0, 0, 300, 200);
        Canvas canvas = redCanvas(stack);
        flutter.settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        stack.setVisible(false);
        flutter.settle();
        stack.setVisible(true);
        flutter.settle();

        assertThat(redShare(canvas)).as("painted after being shown again").isGreaterThan(0.9);
        stack.dispose();
        flutter.settle();
    }

    @Test
    @DisplayName("a canvas whose parent was hidden, resized and shown again is drawn again")
    void hiddenResizedAndShownAgain() throws Exception {
        // As maximizing and restoring a view does.
        Composite stack = new Composite(shell, SWT.NONE);
        stack.setBounds(0, 0, 300, 200);
        Canvas canvas = redCanvas(stack);
        flutter.settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        stack.setVisible(false);
        flutter.settle();
        stack.setBounds(0, 0, 380, 260);
        stack.setVisible(true);
        flutter.settle();

        assertThat(redShare(canvas)).as("painted after being shown again").isGreaterThan(0.9);
        stack.dispose();
        flutter.settle();
    }

    @Test
    @DisplayName("a canvas reparented into another shell and back is drawn in both")
    void reparentedAndBack() throws Exception {
        // As showing a minimized view from the trim does.
        Composite stack = new Composite(shell, SWT.NONE);
        stack.setBounds(0, 0, 300, 200);
        Composite folder = new Composite(stack, SWT.NONE);
        folder.setBounds(0, 0, 300, 200);
        Canvas canvas = redCanvas(folder);
        flutter.settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        Shell popup = new Shell(shell, SWT.NO_TRIM);
        popup.setBounds(420, 0, 320, 220);
        popup.open();
        folder.setParent(popup);
        flutter.settle();
        assertThat(redShare(canvas)).as("painted in the popup").isGreaterThan(0.9);

        folder.setParent(stack);
        popup.dispose();
        flutter.settle();
        assertThat(redShare(canvas)).as("painted after moving back").isGreaterThan(0.9);
        stack.dispose();
        flutter.settle();
    }

    @Test
    @DisplayName("a tree whose parent was hidden and shown again still shows its items")
    void treeHiddenAndShownAgain() throws Exception {
        Composite stack = new Composite(shell, SWT.NONE);
        stack.setBounds(0, 0, 300, 200);
        Tree tree = new Tree(stack, SWT.NONE);
        tree.setBounds(0, 0, 300, 200);
        for (int i = 0; i < 5; i++) new TreeItem(tree, SWT.NONE).setText("item " + i);
        flutter.settle();
        String before = String.valueOf(flutter.queryState(tree));
        assertThat(before).as("sanity: items rendered").contains("item 3");

        stack.setVisible(false);
        flutter.settle();
        stack.setVisible(true);
        flutter.settle();

        assertThat(String.valueOf(flutter.queryState(tree))).contains("item 3");
        stack.dispose();
        flutter.settle();
    }

    @Test
    @DisplayName("a canvas reparented within its shell is drawn where it went")
    void reparentedWithinTheShell() throws Exception {
        Composite left = new Composite(shell, SWT.NONE);
        left.setBounds(0, 0, 190, 200);
        Composite right = new Composite(shell, SWT.NONE);
        right.setBounds(200, 0, 190, 200);
        Composite folder = new Composite(left, SWT.NONE);
        folder.setBounds(0, 0, 180, 150);
        Canvas canvas = redCanvas(folder);
        flutter.settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        folder.setParent(right);
        flutter.settle();
        assertThat(redShare(canvas)).as("painted where it went").isGreaterThan(0.9);
    }

    @Test
    @DisplayName("control: a canvas made in a popup shell is drawn")
    void madeInAPopup() throws Exception {
        Shell popup = new Shell(shell, SWT.NO_TRIM);
        popup.setBounds(420, 0, 320, 220);
        Canvas canvas = redCanvas(popup);
        popup.open();
        flutter.settle();
        assertThat(redShare(canvas)).isGreaterThan(0.9);
    }
}
