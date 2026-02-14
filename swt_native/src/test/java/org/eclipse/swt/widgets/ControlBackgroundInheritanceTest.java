package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;

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
        assertThat(label.getBackground()).as("Label without own background should return parent background (e.g. dialog gray)")
                .isEqualTo(parentBg);
    }

    @Test
    void link_without_background_inherits_parent_background() {
        Composite parent = composite();
        Color parentBg = parent.getBackground();
        Link link = new Link(parent, SWT.NONE);
        assertThat(link.getBackground()).as("Link without own background should return parent background")
                .isEqualTo(parentBg);
    }

    @Test
    void clabel_without_background_inherits_parent_background() {
        Composite parent = composite();
        Color parentBg = parent.getBackground();
        CLabel clabel = new CLabel(parent, SWT.NONE);
        assertThat(clabel.getBackground()).as("CLabel without own background should return parent background")
                .isEqualTo(parentBg);
    }

    @Test
    void label_with_own_background_returns_own_color() {
        Composite parent = composite();
        Color ownColor = new Color(parent.getDisplay(), 100, 150, 200);
        Label label = new Label(parent, SWT.NONE);
        label.setBackground(ownColor);
        assertThat(label.getBackground()).as("Label with set background should return its own color")
                .isEqualTo(ownColor);
        ownColor.dispose();
    }
}
