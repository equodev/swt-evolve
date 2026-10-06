package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins {@code TreeItem.getImageBounds(int)} to where the row's icon actually is. Owner-draw label
 * providers (JFace's {@code StyledCellLabelProvider}) paint the icon at these bounds, so an empty
 * rectangle puts every row's icon at the tree's top-left corner, over the first row's expander.
 */
@Tag("flutter-it")
@ExtendWith(MockFlutterBridge.Extension.class)
class TreeItemImageBoundsFlutterTest {

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
        Tree tree = new Tree(shell, SWT.BORDER);
        tree.setSize(300, 200);
        TreeItem root = new TreeItem(tree, SWT.NONE);
        root.setText("project");
        root.setImage(image);
        TreeItem child = new TreeItem(root, SWT.NONE);
        child.setText("src");
        child.setImage(image);
        root.setExpanded(true);

        Rectangle rootImage = root.getImageBounds(0);
        Rectangle rootText = root.getTextBounds(0);
        Rectangle childImage = child.getImageBounds(0);
        Rectangle childText = child.getTextBounds(0);

        assertThat(rootImage.width).as("root image width").isPositive();
        assertThat(rootImage.height).as("root image height").isPositive();
        assertThat(rootImage.y).as("root image row").isEqualTo(rootText.y);
        assertThat(rootImage.x + rootImage.width).as("root image ends before its text").isLessThanOrEqualTo(rootText.x);

        assertThat(childImage.y).as("child image row").isEqualTo(childText.y).isNotEqualTo(rootImage.y);
        assertThat(childImage.x).as("child image is indented past the root's").isGreaterThan(rootImage.x);
        assertThat(childImage.x + childImage.width).as("child image ends before its text").isLessThanOrEqualTo(childText.x);
    }
}
