package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.Test;

import static org.eclipse.swt.widgets.Mocks.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that Label, Link, and CLabel (and any control without own background)
 * return the parent's background from getBackground() when they have no background set.
 * This ensures dialogs/popups (e.g. "Do you want to exit?") show the same background
 * as the container instead of default white.
 */
class ControlBackgroundInheritanceTest extends SerializeTestBase {

    @Test
    void label_without_background_inherits_parent_background() {
        Composite parent = composite();
        Color parentBg = parent.getBackground();
        Label label = new Label(parent, SWT.NONE);
        assertEquals(parentBg, label.getBackground(),
                "Label without own background should return parent background (e.g. dialog gray)");
    }

    @Test
    void link_without_background_inherits_parent_background() {
        Composite parent = composite();
        Color parentBg = parent.getBackground();
        Link link = new Link(parent, SWT.NONE);
        assertEquals(parentBg, link.getBackground(),
                "Link without own background should return parent background");
    }

    @Test
    void clabel_without_background_inherits_parent_background() {
        Composite parent = composite();
        Color parentBg = parent.getBackground();
        CLabel clabel = new CLabel(parent, SWT.NONE);
        assertEquals(parentBg, clabel.getBackground(),
                "CLabel without own background should return parent background");
    }

    @Test
    void label_with_own_background_returns_own_color() {
        Composite parent = composite();
        Color ownColor = new Color(parent.getDisplay(), 100, 150, 200);
        Label label = new Label(parent, SWT.NONE);
        label.setBackground(ownColor);
        assertEquals(ownColor, label.getBackground(),
                "Label with set background should return its own color");
        ownColor.dispose();
    }
}
