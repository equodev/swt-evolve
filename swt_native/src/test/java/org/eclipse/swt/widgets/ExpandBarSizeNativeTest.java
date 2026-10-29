package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;

/**
 * The size and the header height of an ExpandBar are the sums its items define, not a fixed guess: Win32's and
 * Cocoa's rule, on every platform.
 */
class ExpandBarSizeNativeTest extends SerializeTestBase {

    private ExpandItem item(ExpandBar bar, int height) {
        ExpandItem item = new ExpandItem(bar, SWT.NONE);
        item.setText("item");
        item.setHeight(height);
        return item;
    }

    @Test
    void an_empty_bar_is_the_default_size() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);

        assertThat(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(64, 64));
    }

    @Test
    void the_hints_replace_the_sums() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        item(bar, 100).setExpanded(true);

        assertThat(bar.computeSize(120, 80)).isEqualTo(new Point(120, 80));
    }

    @Test
    void the_height_is_the_headers_the_spacing_and_the_open_bodies() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        ExpandItem first = item(bar, 100);
        ExpandItem second = item(bar, 70);
        int header = first.getHeaderHeight();
        bar.setSpacing(5);

        assertThat(bar.computeSize(100, SWT.DEFAULT).y).isEqualTo(5 + header + 5 + header + 5);
        first.setExpanded(true);
        assertThat(bar.computeSize(100, SWT.DEFAULT).y).isEqualTo(5 + header + 100 + 5 + header + 5);
        second.setExpanded(true);
        assertThat(bar.computeSize(100, SWT.DEFAULT).y).isEqualTo(5 + header + 100 + 5 + header + 70 + 5);
    }

    @Test
    void a_collapsed_items_control_adds_nothing() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        ExpandItem item = item(bar, 100);
        int without = bar.computeSize(100, SWT.DEFAULT).y;
        Composite client = new Composite(bar, SWT.NONE);
        client.setSize(300, 500);
        item.setControl(client);

        assertThat(bar.computeSize(100, SWT.DEFAULT).y).isEqualTo(without);
    }

    @Test
    void the_width_is_the_widest_header() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        ExpandItem item = item(bar, 10);
        item.setText("");

        assertThat(bar.computeSize(SWT.DEFAULT, 100).x).isEqualTo(2 * 6 + 24);
        item.setText("a title that is a good deal longer");
        assertThat(bar.computeSize(SWT.DEFAULT, 100).x).isGreaterThan(2 * 6 + 24);
    }

    @Test
    void the_header_is_as_tall_as_the_font_needs() {
        ExpandBar bar = new ExpandBar(shell(), SWT.NONE);
        ExpandItem item = item(bar, 10);
        assertThat(item.getHeaderHeight()).isEqualTo(24);

        Font big = new Font(bar.getDisplay(), "Helvetica", 40, SWT.NORMAL);
        bar.setFont(big);
        GC gc = new GC(bar);
        int line = gc.getFontMetrics().getHeight();
        gc.dispose();

        assertThat(line).isGreaterThan(24);
        assertThat(item.getHeaderHeight()).isEqualTo(line);
        bar.setFont(null);
        assertThat(item.getHeaderHeight()).isEqualTo(24);
        big.dispose();
    }
}
