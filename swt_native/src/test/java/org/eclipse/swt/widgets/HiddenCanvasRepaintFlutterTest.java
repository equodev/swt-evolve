package org.eclipse.swt.widgets;

import com.google.gson.JsonObject;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

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

    /**
     * Share of {@code w}'s middle painted red. Polls, since a moved control can take a round trip or
     * two to come back while the wire is quiet.
     */
    private double redShare(Control w) throws Exception {
        double share = 0;
        for (int attempt = 0; attempt < 20; attempt++) {
            settle();
            try {
                share = redShareNow(w);
            } catch (IllegalStateException notRenderedYet) {
                share = 0;
            }
            if (share > 0.9) return share;
            Thread.sleep(100);
        }
        return share;
    }

    private double redShareNow(Control w) throws Exception {
        double[] r = flutter.renderedRect(w);
        JsonObject clip = new JsonObject();
        clip.addProperty("x", r[0] + r[2] / 4);
        clip.addProperty("y", r[1] + r[3] / 4);
        clip.addProperty("width", r[2] / 2);
        clip.addProperty("height", r[3] / 2);
        clip.addProperty("scale", 1);
        JsonObject params = new JsonObject();
        params.addProperty("format", "png");
        params.add("clip", clip);
        String data = flutter.devTools().send("Page.captureScreenshot", params).get("data").getAsString();
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data)));
        int red = 0, all = 0;
        for (int y = 0; y < img.getHeight(); y += 2) {
            for (int x = 0; x < img.getWidth(); x += 2) {
                int rgb = img.getRGB(x, y);
                int rr = (rgb >> 16) & 0xff, gg = (rgb >> 8) & 0xff, bb = rgb & 0xff;
                if (rr > 200 && gg < 60 && bb < 60) red++;
                all++;
            }
        }
        return all == 0 ? 0 : (double) red / all;
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
        settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        stack.setVisible(false);
        settle();
        stack.setVisible(true);
        settle();

        assertThat(redShare(canvas)).as("painted after being shown again").isGreaterThan(0.9);
        stack.dispose();
        settle();
    }

    @Test
    @DisplayName("a canvas whose parent was hidden, resized and shown again is drawn again")
    void hiddenResizedAndShownAgain() throws Exception {
        // As maximizing and restoring a view does.
        Composite stack = new Composite(shell, SWT.NONE);
        stack.setBounds(0, 0, 300, 200);
        Canvas canvas = redCanvas(stack);
        settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        stack.setVisible(false);
        settle();
        stack.setBounds(0, 0, 380, 260);
        stack.setVisible(true);
        settle();

        assertThat(redShare(canvas)).as("painted after being shown again").isGreaterThan(0.9);
        stack.dispose();
        settle();
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
        settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        Shell popup = new Shell(shell, SWT.NO_TRIM);
        popup.setBounds(420, 0, 320, 220);
        popup.open();
        folder.setParent(popup);
        settle();
        assertThat(redShare(canvas)).as("painted in the popup").isGreaterThan(0.9);

        folder.setParent(stack);
        popup.dispose();
        settle();
        assertThat(redShare(canvas)).as("painted after moving back").isGreaterThan(0.9);
        stack.dispose();
        settle();
    }

    @Test
    @DisplayName("a tree whose parent was hidden and shown again still shows its items")
    void treeHiddenAndShownAgain() throws Exception {
        Composite stack = new Composite(shell, SWT.NONE);
        stack.setBounds(0, 0, 300, 200);
        Tree tree = new Tree(stack, SWT.NONE);
        tree.setBounds(0, 0, 300, 200);
        for (int i = 0; i < 5; i++) new TreeItem(tree, SWT.NONE).setText("item " + i);
        settle();
        String before = String.valueOf(flutter.queryState(tree));
        assertThat(before).as("sanity: items rendered").contains("item 3");

        stack.setVisible(false);
        settle();
        stack.setVisible(true);
        settle();

        assertThat(String.valueOf(flutter.queryState(tree))).contains("item 3");
        stack.dispose();
        settle();
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
        settle();
        assertThat(redShare(canvas)).as("sanity: painted before").isGreaterThan(0.9);

        folder.setParent(right);
        settle();
        assertThat(redShare(canvas)).as("painted where it went").isGreaterThan(0.9);
    }

    @Test
    @DisplayName("control: a canvas made in a popup shell is drawn")
    void madeInAPopup() throws Exception {
        Shell popup = new Shell(shell, SWT.NO_TRIM);
        popup.setBounds(420, 0, 320, 220);
        Canvas canvas = redCanvas(popup);
        popup.open();
        settle();
        assertThat(redShare(canvas)).isGreaterThan(0.9);
    }
}
