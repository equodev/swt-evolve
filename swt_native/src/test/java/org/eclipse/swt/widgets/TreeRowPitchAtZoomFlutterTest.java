package org.eclipse.swt.widgets;

import dev.equo.swt.AutoScalePolicy;
import dev.equo.swt.Config;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * A tree's rows are drawn where {@code TreeItem.getBounds()} puts them, and {@code Tree.getItem}
 * answers the row drawn under a point, at a zoom whose text scaler makes the row's content taller
 * than the theme row: {@code swt.autoScale=integer} on a 150 % monitor draws SWT's 100 at two thirds
 * of a logical pixel, and every Text at 1.5 times its style.
 */
@Tag("flutter-it")
class TreeRowPitchAtZoomFlutterTest {

    private static final int ROWS = 18;

    private final WebDisplayHarness flutter = new WebDisplayHarness().deviceScaleFactor(1.5);

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void rowsAreDrawnWhereJavaPutsThemAtIntegerZoomOn150() {
        int savedZoom = DPIUtil.getDeviceZoom();
        String savedPolicy = System.getProperty("swt.autoScale");
        System.setProperty("swt.autoScale", "integer");
        try {
            AutoScalePolicy.runWith("integer", this::body);
        } finally {
            if (savedPolicy == null) System.clearProperty("swt.autoScale");
            else System.setProperty("swt.autoScale", savedPolicy);
            DPIUtil.setDeviceZoom(savedZoom);
            Config.setClientDeviceZoom(100);
        }
    }

    private void body() {
        Display display = flutter.boot();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        Font arial = new Font(display, "Arial", 11, SWT.NORMAL);
        Image image = new Image(display, 16, 16);
        Tree checked = tree(shell, SWT.BORDER | SWT.MULTI | SWT.CHECK, arial, image);
        Tree plain = tree(shell, SWT.BORDER | SWT.MULTI, arial, image);
        shell.setSize(900, 900);
        shell.open();
        flutter.settle();

        double scale = Config.uiScale();
        assertThat(scale).as("SWT draws at 100 on the 150 % client").isCloseTo(2.0 / 3, within(1e-9));
        for (Tree tree : List.of(checked, plain)) {
            double[] treeRect = flutter.renderedRect(tree);
            List<TreeItem> rows = visibleRows(tree);
            assertThat(rows).hasSize(ROWS);
            for (TreeItem item : rows) {
                double[] rect = flutter.renderedRect(item);
                Rectangle bounds = item.getBounds();
                double drawnTop = (rect[1] - treeRect[1]) / scale;
                assertThat(drawnTop)
                        .as("%s of %s: drawn top against getBounds().y %s", item.getText(), tree, bounds.y)
                        .isCloseTo(bounds.y, within(1.0));
                int centreY = (int) Math.round((rect[1] + rect[3] / 2 - treeRect[1]) / scale);
                TreeItem hit = tree.getItem(new Point(bounds.x + 1, centreY));
                assertThat(hit == null ? null : hit.getText())
                        .as("getItem at the centre of the drawn %s of %s", item.getText(), tree)
                        .isEqualTo(item.getText());
            }
        }
    }

    private static Tree tree(Shell shell, int style, Font font, Image image) {
        Tree tree = new Tree(shell, style);
        tree.setFont(font);
        TreeItem root = new TreeItem(tree, SWT.NONE);
        root.setText("Root");
        root.setImage(image);
        TreeItem group = null;
        for (int i = 1; i < ROWS; i++) {
            TreeItem parent = i % 6 == 1 ? root : group;
            TreeItem item = new TreeItem(parent, SWT.NONE);
            item.setText((i % 6 == 1 ? "Group " : "Data ") + i);
            item.setImage(image);
            if (i % 6 == 1) group = item;
        }
        expandAll(root);
        return tree;
    }

    private static void expandAll(TreeItem item) {
        item.setExpanded(true);
        for (TreeItem child : item.getItems()) expandAll(child);
    }

    private static List<TreeItem> visibleRows(Tree tree) {
        List<TreeItem> rows = new ArrayList<>();
        for (TreeItem item : tree.getItems()) collect(item, rows);
        return rows;
    }

    private static void collect(TreeItem item, List<TreeItem> rows) {
        rows.add(item);
        if (item.getExpanded()) for (TreeItem child : item.getItems()) collect(child, rows);
    }
}
