package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.eclipse.swt.widgets.Mocks.shell;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(Mocks.class)
public class ReparentTest extends SerializeTestBase {

    @Test
    public void reparentTest() {
        // Create parent composite
        Composite root = new Composite(shell(), SWT.NONE);

        // Create child composites
        Composite parent = new Composite(root, SWT.NONE);
        Composite target = new Composite(root, SWT.NONE);

        // Create toolbar in composite1
        ToolBar toolBar = new ToolBar(parent, SWT.HORIZONTAL);

        // Checks
        assertEquals(parent, toolBar.getParent(), "ToolBar parent should be parent");
        assertEquals(1, parent.getChildren().length, "Parent should have 1 child");
        assertEquals(toolBar, parent.getChildren()[0], "Parent's child should be the toolbar");
        assertEquals(0, target.getChildren().length, "Target should have 0 children");

        toolBar.setParent(target);

        // Checks
        assertEquals(target, toolBar.getParent(), "ToolBar parent should now be target");
        assertEquals(0, parent.getChildren().length, "Parent should have 0 children");
        assertEquals(1, target.getChildren().length, "Target should have 1 child");
        assertEquals(toolBar, target.getChildren()[0], "Target's child should be the toolbar");
    }
}
