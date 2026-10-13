package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins {@code TableItem.getImageBounds(int)} to where the row's icon actually is. Owner-draw label
 * providers (JFace's {@code StyledCellLabelProvider}, content assist's {@code TableOwnerDrawSupport})
 * paint the icon at these bounds, so a rectangle at the origin stacks every row's icon on the first
 * row.
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

    private TableItem[] fillRows(Table table) {
        TableItem[] items = new TableItem[4];
        for (int i = 0; i < items.length; i++) {
            items[i] = new TableItem(table, SWT.NONE);
            items[i].setText("row " + i);
            items[i].setImage(image);
        }
        return items;
    }

    @Test
    void imageBoundsSitInTheItemsRowJustBeforeItsText() {
        Table table = new Table(shell, SWT.BORDER);
        table.setSize(300, 200);
        TableItem[] items = fillRows(table);

        Rectangle firstImage = items[0].getImageBounds(0);
        Rectangle thirdCell = items[3].getBounds(0);
        Rectangle thirdImage = items[3].getImageBounds(0);
        Rectangle thirdText = items[3].getTextBounds(0);

        assertThat(thirdImage.width).as("image width").isPositive();
        assertThat(thirdImage.height).as("image height").isPositive();
        assertThat(thirdImage.y).as("image row").isEqualTo(thirdCell.y).isEqualTo(thirdText.y).isNotEqualTo(firstImage.y);
        assertThat(thirdImage.x).as("image starts at the cell's content").isEqualTo(thirdCell.x);
        assertThat(thirdImage.x + thirdImage.width).as("image ends before its text").isLessThanOrEqualTo(thirdText.x);
    }

    @Test
    void imageBoundsStartAfterTheCheckBox() {
        Table plain = new Table(shell, SWT.BORDER);
        plain.setSize(300, 200);
        Table checked = new Table(shell, SWT.BORDER | SWT.CHECK);
        checked.setSize(300, 200);
        TableItem plainItem = fillRows(plain)[1];
        TableItem checkedItem = fillRows(checked)[1];

        assertThat(checkedItem.getImageBounds(0).x).as("image is pushed past the check box")
                .isGreaterThan(plainItem.getImageBounds(0).x);
        assertThat(checkedItem.getImageBounds(0).y).as("same row either way")
                .isEqualTo(plainItem.getImageBounds(0).y);
    }

    @Test
    void columnsWithoutAnImageReportAnEmptyWidthOnTheirRow() {
        Table table = new Table(shell, SWT.BORDER);
        table.setSize(300, 200);
        new TableColumn(table, SWT.NONE).setWidth(100);
        new TableColumn(table, SWT.NONE).setWidth(100);
        TableItem item = fillRows(table)[2];

        Rectangle secondCell = item.getBounds(1);
        Rectangle secondImage = item.getImageBounds(1);

        assertThat(secondImage.width).as("no image in column 1").isZero();
        assertThat(secondImage.x).isEqualTo(secondCell.x);
        assertThat(secondImage.y).isEqualTo(secondCell.y);
        assertThat(item.getImageBounds(5)).as("out of range").isEqualTo(new Rectangle(0, 0, 0, 0));
    }
}
