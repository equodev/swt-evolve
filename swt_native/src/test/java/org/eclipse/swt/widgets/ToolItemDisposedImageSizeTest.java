package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;

import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolItemDisposedImageSizeTest extends SerializeTestBase {

    @Test
    void getWidth_ignoresAnImageDisposedAfterItWasSet() {
        ToolBar toolBar = toolBar();
        ToolItem item = new ToolItem(toolBar, SWT.NONE);

        Image image = new Image(item.getDisplay(), getClass().getClassLoader().getResourceAsStream("collapseall.png"));
        item.setImage(image);
        image.dispose();

        assertEquals(DartToolItem.DEFAULT_WIDTH, assertDoesNotThrow(item::getWidth));
    }
}
