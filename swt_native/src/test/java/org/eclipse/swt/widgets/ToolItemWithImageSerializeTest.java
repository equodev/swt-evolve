package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;

import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

import java.io.IOException;

class ToolItemWithImageSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_filled_ToolItem() throws IOException {
        ToolBar toolBar = toolBar();
        ToolItem w = new ToolItem(toolBar, SWT.NONE);

        w.setText("text");

        w.setImage(new Image(w.getDisplay(), getClass().getClassLoader().getResourceAsStream("collapseall.png")));

        w.setDisabledImage(new Image(w.getDisplay(), getClass().getClassLoader().getResourceAsStream("collapseall.png")));

        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
                .containsEntry("swt", "ToolItem")
                .containsEntry("text", w.getText());

        assertJ.containsKey("image");
        assertJ.containsKey("disabledImage");
    }

    VToolItem value(ToolItem w) {
        return ((DartToolItem) w.getImpl()).getValue();
    }
}