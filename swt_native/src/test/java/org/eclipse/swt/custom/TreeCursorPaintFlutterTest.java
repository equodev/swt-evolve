package org.eclipse.swt.custom;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a TreeCursor paints over its cell, read back from the Image its Paint listener draws into. The GC of an
 * Image is drawn by the client, so this needs one.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TreeCursorPaintFlutterTest {

    private static final int COLUMN_WIDTH = 80;

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private final List<Image> images = new ArrayList<>();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @BeforeEach
    void createShell() {
        shell = new Shell(display);
        shell.setSize(460, 420);
    }

    @AfterEach
    void disposeShell() {
        shell.dispose();
        images.forEach(Image::dispose);
        images.clear();
        flutter.settle();
    }

    private Tree tree() {
        Tree tree = new Tree(shell, SWT.NONE);
        tree.setBounds(0, 0, 420, 360);
        return tree;
    }

    private static TreeColumn[] columns(Tree tree, int count) {
        TreeColumn[] columns = new TreeColumn[count];
        for (int i = 0; i < count; i++) {
            columns[i] = new TreeColumn(tree, SWT.LEFT);
            columns[i].setText("C" + i);
            columns[i].setWidth(COLUMN_WIDTH);
        }
        return columns;
    }

    /** A root row; with columns, cell i reads "text.i". */
    private static TreeItem item(Tree tree, String text) {
        TreeItem item = new TreeItem(tree, SWT.NONE);
        if (tree.getColumnCount() == 0)
            item.setText(text);
        for (int i = 0; i < tree.getColumnCount(); i++)
            item.setText(i, text + "." + i);
        return item;
    }

    /** Roots A, B and C, and B holds B1. */
    private static TreeItem[] model(Tree tree) {
        TreeItem a = item(tree, "A");
        TreeItem b = item(tree, "B");
        TreeItem b1 = new TreeItem(b, SWT.NONE);
        b1.setText("B1");
        TreeItem c = item(tree, "C");
        return new TreeItem[] {a, b, c};
    }

    private Image image(int size, RGB rgb) {
        ImageData data = new ImageData(size, size, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        int pixel = data.palette.getPixel(rgb);
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                data.setPixel(x, y, pixel);
        Image image = new Image(display, data);
        images.add(image);
        return image;
    }

    private void open() {
        shell.open();
        flutter.settle();
    }

    private ImageData paintInto(TreeCursor cursor) {
        Point size = cursor.getSize();
        Image target = new Image(display, size.x, size.y);
        images.add(target);
        GC gc = new GC(target);
        try {
            gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
            gc.fillRectangle(0, 0, size.x, size.y);
            Event event = new Event();
            event.gc = gc;
            event.x = 0;
            event.y = 0;
            event.width = size.x;
            event.height = size.y;
            cursor.notifyListeners(SWT.Paint, event);
        } finally {
            gc.dispose();
        }
        return target.getImageData();
    }

    private static RGB rgb(ImageData data, int x, int y) {
        return data.palette.getRGB(data.getPixel(x, y));
    }

    @Test
    void fillsTheCellWithTheCursorBackgroundAndDrawsTheText() {
        Tree tree = tree();
        columns(tree, 2);
        TreeItem[] rows = model(tree);
        open();
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
        cursor.setSelection(rows[1], 1);
        Point size = cursor.getSize();
        assertThat(size.x > 20 && size.y > 8).as("a cell to paint: " + size).isTrue();
        ImageData data = paintInto(cursor);
        RGB red = display.getSystemColor(SWT.COLOR_RED).getRGB();
        assertThat(rgb(data, size.x - 3, 1)).as("background, right of the text").isEqualTo(red);
        int notBackground = 0;
        for (int x = 0; x < size.x / 2; x++)
            for (int y = 0; y < size.y; y++)
                if (!rgb(data, x, y).equals(red))
                    notBackground++;
        assertThat(notBackground > 0).as("the cell's text is drawn").isTrue();
    }

    @Test
    void drawsTheCellImage() {
        Tree tree = tree();
        TreeItem[] rows = model(tree);
        RGB yellow = new RGB(255, 255, 0);
        rows[2].setImage(image(12, yellow));
        open();
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setSelection(rows[2], 0);
        ImageData data = paintInto(cursor);
        boolean found = false;
        for (int x = 0; x < Math.min(data.width, 24) && !found; x++)
            for (int y = 0; y < data.height && !found; y++)
                found = rgb(data, x, y).equals(yellow);
        assertThat(found).as("the row's image is drawn at the left of the cell").isTrue();
    }

    @Test
    void withoutACellPaintsNothing() {
        Tree tree = tree();
        model(tree);
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setSize(30, 16);
        ImageData data = paintInto(cursor);
        assertThat(rgb(data, 15, 8)).isEqualTo(display.getSystemColor(SWT.COLOR_GREEN).getRGB());
    }

    @Test
    void alignsTheTextAsItsColumn() {
        Tree tree = tree();
        TreeColumn[] columns = columns(tree, 3);
        columns[1].setAlignment(SWT.RIGHT);
        columns[2].setAlignment(SWT.CENTER);
        for (TreeColumn column : columns)
            column.setWidth(140);
        TreeItem row = item(tree, "x");
        open();
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        RGB red = display.getSystemColor(SWT.COLOR_RED).getRGB();
        int[] firstInk = new int[3];
        for (int column = 0; column < 3; column++) {
            cursor.setSelection(row, column);
            ImageData data = paintInto(cursor);
            firstInk[column] = -1;
            for (int x = 0; x < data.width && firstInk[column] < 0; x++)
                for (int y = 0; y < data.height; y++)
                    if (!rgb(data, x, y).equals(red)) {
                        firstInk[column] = x;
                        break;
                    }
        }
        assertThat(firstInk[0] >= 0 && firstInk[0] < 20).as("left: " + firstInk[0]).isTrue();
        assertThat(firstInk[1] > 100).as("right: " + firstInk[1]).isTrue();
        assertThat(firstInk[2] > 40 && firstInk[2] < 100).as("centre: " + firstInk[2]).isTrue();
    }

    @Test
    void aCellWithoutTextIsOnlyFilled() {
        Tree tree = tree();
        columns(tree, 2);
        TreeItem row = item(tree, "A");
        row.setText(1, "");
        open();
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setSelection(row, 1);
        ImageData data = paintInto(cursor);
        RGB red = display.getSystemColor(SWT.COLOR_RED).getRGB();
        for (int x = 0; x < data.width; x += 3)
            assertThat(rgb(data, x, data.height / 2)).as("x " + x).isEqualTo(red);
    }

    @Test
    void drawsAnImageInALeftAlignedColumn() {
        Tree tree = tree();
        columns(tree, 2);
        TreeItem row = item(tree, "A");
        RGB yellow = new RGB(255, 255, 0);
        row.setImage(1, image(12, yellow));
        open();
        TreeCursor cursor = new TreeCursor(tree, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setSelection(row, 1);
        ImageData data = paintInto(cursor);
        boolean found = false;
        for (int x = 0; x < Math.min(data.width, 24) && !found; x++)
            for (int y = 0; y < data.height && !found; y++)
                found = rgb(data, x, y).equals(yellow);
        assertThat(found).as("the cell's image is drawn at its left").isTrue();
    }
}
