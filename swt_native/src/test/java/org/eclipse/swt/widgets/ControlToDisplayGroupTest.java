package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.echoingShell;

/**
 * Where a child of a Group lands on the display. A titled Group draws its title row above the
 * child's origin; an untitled one starts the child's coordinates at its own top edge, so mapping
 * must add nothing there.
 */
class ControlToDisplayGroupTest extends SerializeTestBase {

    private static final int GROUP_X = 40;
    private static final int GROUP_Y = 100;

    @Test
    void aChildOfAnUntitledGroupMapsToItsOwnPosition() {
        Group group = new Group(echoingShell(), SWT.NONE);
        group.setBounds(GROUP_X, GROUP_Y, 400, 300);
        Composite child = new Composite(group, SWT.NONE);
        child.setBounds(12, 14, 100, 50);

        assertThat(child.toDisplay(0, 0)).isEqualTo(new Point(GROUP_X + 12, GROUP_Y + 14));
        assertThat(child.toDisplay(3, 4)).isEqualTo(new Point(GROUP_X + 15, GROUP_Y + 18));
    }

    @Test
    void aChildOfATitledGroupSitsOneTitleRowBelowTheGroup() {
        Group group = new Group(echoingShell(), SWT.NONE);
        group.setText("Title");
        group.setBounds(GROUP_X, GROUP_Y, 400, 300);
        Composite child = new Composite(group, SWT.NONE);
        child.setBounds(12, 0, 100, 50);

        assertThat(child.toDisplay(0, 0)).isEqualTo(new Point(GROUP_X + 12, GROUP_Y - group.computeTrim(0, 0, 0, 0).y));
    }

    @Test
    void takingTheTitleOffTakesTheTitleRowOffTheMapping() {
        Group group = new Group(echoingShell(), SWT.NONE);
        group.setBounds(GROUP_X, GROUP_Y, 400, 300);
        Composite child = new Composite(group, SWT.NONE);
        child.setBounds(0, 0, 100, 50);
        group.setText("Title");
        assertThat(child.toDisplay(0, 0).y).isGreaterThan(GROUP_Y);
        group.setText("");

        assertThat(child.toDisplay(0, 0).y).isEqualTo(GROUP_Y);
    }
}
