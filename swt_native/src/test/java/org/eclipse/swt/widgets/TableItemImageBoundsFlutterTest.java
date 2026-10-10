package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins {@code TableItem.getImageBounds(int)} to where the cell's icon actually is. Owner-draw label
 * providers paint the icon at these bounds, so a rectangle at the origin stacks every row's icon on
 * the first row.
 */
@Tag("flutter-it")
@ExtendWith(MockFlutterBridge.Extension.class)
class TableItemImageBoundsFlutterTest {

    private Display display;
    private Shell shell;
    private Image image;

    @BeforeEach
    void setUp() {
        display = new Display();
        shell = new Shell(display);
        image = new Image(display, 16, 16);
    }

    @AfterEach
    void tearDown() {
        if (image != null && !image.isDisposed())
            image.dispose();
        if (display != null && !display.isDisposed())
            display.dispose();
    }

    @Test
    void imageBoundsSitInTheItemsRowJustBeforeItsText() {
        Table table = new Table(shell, SWT.BORDER);
        table.setSize(300, 200);
        TableItem first = null;
        TableItem fourth = null;
        for (int i = 0; i < 4; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText("row " + i);
            item.setImage(image);
            if (i == 0) first = item;
            fourth = item;
        }

        Rectangle firstImage = first.getImageBounds(0);
        Rectangle bounds = fourth.getImageBounds(0);
        Rectangle cell = fourth.getBounds(0);
        Rectangle text = fourth.getTextBounds(0);

        assertThat(bounds.width).as("image width").isEqualTo(16);
        assertThat(bounds.y).as("image row").isEqualTo(cell.y).isGreaterThan(firstImage.y);
        assertThat(bounds.height).as("image height").isEqualTo(cell.height);
        assertThat(bounds.x).as("image starts in its cell").isEqualTo(cell.x);
        assertThat(bounds.x + bounds.width).as("image ends before its text").isLessThanOrEqualTo(text.x);
    }

    @Test
    void imageBoundsFollowTheColumn() {
        Table table = new Table(shell, SWT.BORDER);
        table.setSize(300, 200);
        new TableColumn(table, SWT.NONE).setWidth(100);
        new TableColumn(table, SWT.NONE).setWidth(100);
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] {"a", "b"});

        assertThat(item.getImageBounds(1).width).as("no image, no width").isZero();
        item.setImage(1, image);
        Rectangle bounds = item.getImageBounds(1);

        assertThat(bounds.x).as("the second column's image").isEqualTo(item.getBounds(1).x);
        assertThat(bounds.width).isEqualTo(16);
        assertThat(item.getImageBounds(2)).as("a column that does not exist").isEqualTo(new Rectangle(0, 0, 0, 0));
    }
}
