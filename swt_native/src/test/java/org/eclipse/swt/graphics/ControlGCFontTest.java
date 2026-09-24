package org.eclipse.swt.graphics;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A GC on a Control measures text ({@code textExtent}) with the font the Control handed it in
 * {@code internal_new_GC}. The render side draws with the font the GC sends, so that font has to
 * be sent: without it the text is painted in a default of the render side's own choosing, and an
 * application that lays out from its measurements sees the text overflow the room it made for it.
 */
class ControlGCFontTest extends SerializeTestBase {

    @Test
    void a_gc_on_a_control_sends_the_font_it_measures_with() {
        Composite composite = new Composite(shell(), SWT.NONE);
        GC gc = new GC(composite);
        FontData measured = gc.getFont().getFontData()[0];

        assertThatJson(serialize(gc)).node("font.fontData[0]").isObject()
                .containsEntry("name", FontMetricsUtil.substituteFontName(measured.getName()))
                .containsEntry("height", measured.getHeight());
        gc.dispose();
    }

    @Test
    void a_gc_on_a_control_with_its_own_font_sends_that_font() {
        Composite composite = new Composite(shell(), SWT.NONE);
        Font font = new Font(composite.getDisplay(), "Inter", 13, 0);
        composite.setFont(font);
        GC gc = new GC(composite);

        assertThatJson(serialize(gc)).node("font.fontData[0]").isObject()
                .containsEntry("name", "Inter")
                .containsEntry("height", 13);
        gc.dispose();
        font.dispose();
    }
}
