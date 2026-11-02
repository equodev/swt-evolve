package org.eclipse.swt.graphics;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.WithoutFontSubstitutions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * The run after a bold run starts where the layout measured the bold run to end, so a bold run is
 * measured with bold advances, also in a family without metrics: else the next run overlaps it.
 */
@Tag("native-unit")
class TextLayoutBoldRunNativeTest {

    /** The name the Dart-backed device gives its system font; the metrics table spells it "System". */
    private static final String SYSTEM = "system";

    /** Widths a Flutter TextPainter paints for {@code fontFamily: "system"} at 11px on macOS. */
    private static final double TREE_SHOW_BOLD = 54.785;
    private static final double QUICK_ACCESS_DESCRIPTION_REGULAR = 238.176;

    private static TextLayout layout(Display display, Font font, String text) {
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(text);
        return layout;
    }

    @Test
    void textAfterABoldRunStartsPastTheBoldGlyphs() {
        Display display = DartMocks.dartDisplay();
        Font regular = new Font(display, SYSTEM, 11, SWT.NORMAL);
        Font bold = new Font(display, SYSTEM, 11, SWT.BOLD);
        TextLayout plain = layout(display, regular, "Project Explorer (General)");
        TextLayout styled = layout(display, regular, "Project Explorer (General)");
        styled.setStyle(new TextStyle(bold, null, null), 0, 7);
        try {
            assertThat(styled.getLocation(7, false).x)
                    .as("the space after a bold \"Project\"")
                    .isGreaterThan(plain.getLocation(7, false).x);
            assertThat(styled.getBounds().width)
                    .as("the whole line, one bold run longer")
                    .isGreaterThan(plain.getBounds().width);
        } finally {
            plain.dispose();
            styled.dispose();
            regular.dispose();
            bold.dispose();
        }
    }

    /** As in Open Resource: the run after a bold match starts within 1 px of where it ends. */
    @Test
    @EnabledOnOs(OS.MAC)
    @ExtendWith(WithoutFontSubstitutions.class)
    void underTheMacosDesktopSurfaceTheRunAfterAStyledOneStartsWhereItsGlyphsEnd() {
        String savedMode = ConfigFlags.mode();
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        try {
            assertThat(drawnAt("TreeShowcase.java", 0, 8, "case.java"))
                    .as("\"case.java\" after a bold \"TreeShow\"")
                    .isCloseTo(TREE_SHOW_BOLD, within(1.0));
            String description = "Find Text in Project - Searches the files in the project";
            int match = description.lastIndexOf("project");
            assertThat(drawnAt(description, match, description.length(), "project"))
                    .as("a bold \"project\" after the regular text before it")
                    .isCloseTo(QUICK_ACCESS_DESCRIPTION_REGULAR, within(1.0));
        } finally {
            if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
            else ConfigFlags.setMode(savedMode);
        }
    }

    /** The x {@code run} is drawn at when {@code text} is drawn with [boldStart, boldEnd) in bold. */
    private static double drawnAt(String text, int boldStart, int boldEnd, String run) {
        Display display = DartMocks.dartDisplay();
        Font regular = new Font(display, SYSTEM, 11, SWT.NORMAL);
        Font bold = new Font(display, SYSTEM, 11, SWT.BOLD);
        TextLayout layout = layout(display, regular, text);
        layout.setStyle(new TextStyle(bold, null, null), boldStart, boldEnd - 1);
        GC gc = mock(GC.class);
        try {
            layout.draw(gc, 0, 0);
            ArgumentCaptor<Integer> x = ArgumentCaptor.forClass(Integer.class);
            verify(gc).drawString(eq(run), x.capture(), anyInt(), anyBoolean());
            return x.getValue();
        } finally {
            layout.dispose();
            regular.dispose();
            bold.dispose();
        }
    }
}
