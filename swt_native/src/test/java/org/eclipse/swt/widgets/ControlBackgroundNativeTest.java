package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Control#getBackground()} answers with the color the control is drawn in, including an
 * inherited ancestor's, since callers fill off-screen buffers with it.
 */
@Tag("native-unit")
class ControlBackgroundNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell(DartMocks.dartDisplay());
    }

    @Test
    void a_control_with_none_of_its_own_reports_the_ancestor_color_it_is_drawn_in() {
        Composite editor = new Composite(shell, SWT.NONE);
        Color dark = new Color(shell.getDisplay(), 30, 31, 34);
        editor.setBackground(dark);
        Composite ruler = new Composite(editor, SWT.NONE);
        Canvas column = new Canvas(ruler, SWT.NONE);

        assertThat(column.getBackground())
                .as("the grandparent's color the column is painted with, not the parent's default")
                .isEqualTo(dark);
    }

    @Test
    void its_own_background_wins_over_an_ancestor() {
        Composite editor = new Composite(shell, SWT.NONE);
        editor.setBackground(new Color(shell.getDisplay(), 30, 31, 34));
        Canvas column = new Canvas(editor, SWT.NONE);
        Color own = new Color(shell.getDisplay(), 200, 10, 10);
        column.setBackground(own);

        assertThat(column.getBackground()).isEqualTo(own);
    }

    @Test
    void with_no_background_anywhere_it_reports_its_own_default() {
        Composite parent = new Composite(shell, SWT.NONE);
        Canvas column = new Canvas(parent, SWT.NONE);

        assertThat(column.getBackground())
                .isEqualTo(((DartCanvas) column.getImpl()).defaultBackground());
    }
}
