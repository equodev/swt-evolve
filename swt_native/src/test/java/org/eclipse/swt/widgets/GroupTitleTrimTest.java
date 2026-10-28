package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

/** The title row of a Group is as tall as the lines of its title. */
class GroupTitleTrimTest extends SerializeTestBase {

    @Test
    void aTitleWithLineBreaksMakesTheTitleRowTaller() {
        Rectangle oneLine = titled("one").computeTrim(0, 0, 0, 0);
        Rectangle threeLines = titled("one\ntwo\nthree").computeTrim(0, 0, 0, 0);

        assertThat(threeLines.height).isGreaterThan(oneLine.height);
        assertThat(threeLines.width).isEqualTo(oneLine.width);
    }

    private static Group titled(String title) {
        Group group = new Group(shell(), SWT.NONE);
        group.setText(title);
        return group;
    }
}
