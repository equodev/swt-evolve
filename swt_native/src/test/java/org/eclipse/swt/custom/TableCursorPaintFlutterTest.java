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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
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
 * What a TableCursor paints over its cell, read back from the Image its Paint listener draws into. The GC of an
 * Image is drawn by the client, so this needs one.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TableCursorPaintFlutterTest {

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

    private Table table() {
        Table table = new Table(shell, SWT.NONE);
        table.setBounds(0, 0, 420, 360);
        return table;
    }

    private static TableColumn[] columns(Table table, int count) {
        TableColumn[] columns = new TableColumn[count];
        for (int i = 0; i < count; i++) {
            columns[i] = new TableColumn(table, SWT.LEFT);
            columns[i].setText("C" + i);
            columns[i].setWidth(COLUMN_WIDTH);
        }
        return columns;
    }

    private static TableItem[] rows(Table table, int count) {
        TableItem[] rows = new TableItem[count];
        for (int r = 0; r < count; r++) {
            rows[r] = new TableItem(table, SWT.NONE);
            if (table.getColumnCount() == 0)
                rows[r].setText("R" + r);
            for (int i = 0; i < table.getColumnCount(); i++)
                rows[r].setText(i, "R" + r + "." + i);
        }
        return rows;
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

    private ImageData paintInto(TableCursor cursor) {
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
        Table table = table();
        columns(table, 2);
        rows(table, 3);
        open();
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
        cursor.setSelection(1, 1);
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
        Table table = table();
        TableItem[] rows = rows(table, 3);
        RGB yellow = new RGB(255, 255, 0);
        rows[2].setImage(image(12, yellow));
        open();
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setSelection(2, 0);
        ImageData data = paintInto(cursor);
        boolean found = false;
        for (int x = 0; x < Math.min(data.width, 24) && !found; x++)
            for (int y = 0; y < data.height && !found; y++)
                found = rgb(data, x, y).equals(yellow);
        assertThat(found).as("the row's image is drawn at the left of the cell").isTrue();
    }

    @Test
    void withoutACellPaintsNothing() {
        Table table = table();
        rows(table, 3);
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        cursor.setSize(30, 16);
        ImageData data = paintInto(cursor);
        assertThat(rgb(data, 15, 8)).isEqualTo(display.getSystemColor(SWT.COLOR_GREEN).getRGB());
    }

    @Test
    void alignsTheTextAsItsColumn() {
        Table table = table();
        TableColumn[] columns = columns(table, 3);
        columns[1].setAlignment(SWT.RIGHT);
        columns[2].setAlignment(SWT.CENTER);
        for (TableColumn column : columns)
            column.setWidth(140);
        TableItem row = new TableItem(table, SWT.NONE);
        for (int i = 0; i < 3; i++)
            row.setText(i, "x");
        open();
        TableCursor cursor = new TableCursor(table, SWT.NONE);
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
        Table table = table();
        columns(table, 2);
        TableItem row = rows(table, 1)[0];
        row.setText(1, "");
        open();
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setSelection(row, 1);
        ImageData data = paintInto(cursor);
        RGB red = display.getSystemColor(SWT.COLOR_RED).getRGB();
        for (int x = 0; x < data.width; x += 3)
            assertThat(rgb(data, x, data.height / 2)).as("x " + x).isEqualTo(red);
    }

    @Test
    void drawsTheFocusRectangleOnlyWhileItHasTheFocus() {
        Table table = table();
        columns(table, 2);
        rows(table, 3);
        open();
        TableCursor cursor = new TableCursor(table, SWT.NONE);
        cursor.setBackground(display.getSystemColor(SWT.COLOR_RED));
        cursor.setSelection(1, 1);
        RGB red = display.getSystemColor(SWT.COLOR_RED).getRGB();
        assertThat(edgeInk(paintInto(cursor), red)).as("without the focus, no rectangle").isZero();
        cursor.setFocus();
        flutter.settle();
        assertThat(cursor.isFocusControl()).isTrue();
        assertThat(edgeInk(paintInto(cursor), red)).as("the focus rectangle runs along the edge").isPositive();
    }

    /** How many pixels of the top and bottom rows are not the background. */
    private static int edgeInk(ImageData data, RGB background) {
        int ink = 0;
        for (int x = 0; x < data.width; x++)
            for (int y : new int[] {0, data.height - 1})
                if (!rgb(data, x, y).equals(background))
                    ink++;
        return ink;
    }
}
