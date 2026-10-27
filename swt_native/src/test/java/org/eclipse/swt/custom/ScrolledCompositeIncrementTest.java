package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.junit.jupiter.api.Test;

/**
 * The layout of a ScrolledComposite steps its scroll bars by the composite's font: a character's width across,
 * a line's height down.
 */
class ScrolledCompositeIncrementTest extends SerializeTestBase {

    private ScrolledComposite scrolledWithContent() {
        ScrolledComposite sc = new ScrolledComposite(shell(), SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setSize(200, 100);
        Composite content = new Composite(sc, SWT.NONE);
        content.setSize(300, 400);
        sc.setContent(content);
        return sc;
    }

    @Test
    void the_bars_step_by_a_character_and_by_a_line_of_the_font() {
        ScrolledComposite sc = scrolledWithContent();
        GC gc = new GC(sc);
        FontMetrics metrics = gc.getFontMetrics();
        int charWidth = metrics.getAverageCharWidth();
        int lineHeight = metrics.getHeight();
        gc.dispose();

        assertThat(charWidth).isGreaterThan(1);
        assertThat(lineHeight).isGreaterThan(1);
        assertThat(sc.getHorizontalBar().getIncrement()).isEqualTo(charWidth);
        assertThat(sc.getVerticalBar().getIncrement()).isEqualTo(lineHeight);
    }

    @Test
    void a_composite_without_scroll_bars_lays_out_its_content() {
        ScrolledComposite sc = new ScrolledComposite(shell(), SWT.NONE);
        sc.setSize(200, 100);
        Composite content = new Composite(sc, SWT.NONE);
        content.setSize(300, 400);
        sc.setContent(content);

        assertThat(sc.getHorizontalBar()).isNull();
        assertThat(content.getSize().y).isEqualTo(400);
    }
}
