package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;
import static org.junit.jupiter.api.Assertions.*;

class ToolBarWithItemSerializableTest extends SerializeTestBase {

    @Test
    void should_serialize_ToolBar_with_two_items() {
        ToolBar toolBar = new ToolBar(composite(), SWT.NONE);
        
        // Create first ToolItem
        ToolItem item1 = new ToolItem(toolBar, SWT.NONE);
        item1.setText("Item 1");
        item1.setToolTipText("First item");
        
        // Create second ToolItem
        ToolItem item2 = new ToolItem(toolBar, SWT.NONE);
        item2.setText("Item 2");
        item2.setToolTipText("Second item");
        
        String json = serialize(toolBar);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        
        // Verify ToolBar properties
        assertJ.containsEntry("id", toolBar.hashCode())
               .containsEntry("swt", "ToolBar");
        
        // Verify that items array exists and contains 2 items
        assertJ.node("items").isArray().hasSize(2);
        
        // Verify first item
        assertJ.node("items[0]").isObject()
               .containsEntry("id", item1.hashCode())
               .containsEntry("swt", "ToolItem")
               .containsEntry("text", item1.getText())
               .containsEntry("toolTipText", item1.getToolTipText());
        
        // Verify second item
        assertJ.node("items[1]").isObject()
               .containsEntry("id", item2.hashCode())
               .containsEntry("swt", "ToolItem")
               .containsEntry("text", item2.getText())
               .containsEntry("toolTipText", item2.getToolTipText());
    }

    @Test
    void should_remove_toolitem_using_dispose() {
        //Display display = new Display();
        Shell shell = shell();
        
        ToolBar toolBar = new ToolBar(shell, SWT.NONE);
        
        // Create two ToolItems
        ToolItem item1 = new ToolItem(toolBar, SWT.NONE);
        item1.setText("Item 1");

        ToolItem item2 = new ToolItem(toolBar, SWT.NONE);
        item2.setText("Item 2");
        
        // Assert we have 2 items initially
        ToolItem[] items = toolBar.getItems();
        assertEquals(2, items.length);
        assertEquals("Item 1", items[0].getText());
        assertEquals("Item 2", items[1].getText());
        
        // Remove one item using dispose
        item1.dispose();
        
        // Assert we now have 1 item
        items = toolBar.getItems();
        assertEquals(1, items.length);
        assertEquals("Item 2", items[0].getText());
        
        // Remove the remaining item
        item2.dispose();
        
        // Assert toolbar is now empty
        items = toolBar.getItems();
        assertEquals(0, items.length);
    }

}
