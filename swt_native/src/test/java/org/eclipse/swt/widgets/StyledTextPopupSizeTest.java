package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * A wrapping StyledText in a pop-up (an on-top shell without trim, the shape of the Eclipse hover and
 * content-assist info shells) has no width of its own to wrap to: its natural size wraps to what the
 * shell it sits in offers, minus the margins of that shell's grid layout.
 */
@ExtendWith(Mocks.class)
class StyledTextPopupSizeTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static StyledText styledIn(int shellStyle, String text) {
        Shell shell = Mocks.shell();
        when(shell.getStyle()).thenReturn(shellStyle);
        when(shell.getClientArea()).thenReturn(new Rectangle(0, 0, 160, 300));
        GridLayout layout = new GridLayout();
        layout.marginLeft = 4;
        layout.marginRight = 4;
        layout.marginWidth = 6;
        when(shell.getLayout()).thenReturn(layout);
        StyledText st = new StyledText(shell, SWT.WRAP);
        st.setText(text);
        return st;
    }

    @Test
    void a_popup_wraps_to_the_room_its_shell_offers() {
        String text = "word ".repeat(60);
        Point popup = styledIn(SWT.ON_TOP | SWT.NO_TRIM, text).computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point ordinary = styledIn(SWT.NONE, text).computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(popup.x).isLessThanOrEqualTo(160);
        assertThat(ordinary.x).isGreaterThan(160);
        assertThat(popup.y).isGreaterThan(ordinary.y);
    }

    @Test
    void a_shell_that_cannot_say_how_wide_it_is_leaves_the_text_unwrapped() {
        StyledText st = styledIn(SWT.ON_TOP | SWT.NO_TRIM, "word ".repeat(60));
        when(st.getParent().getClientArea()).thenReturn(new Rectangle(0, 0, 0, 0));

        assertThat(st.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).isGreaterThan(160);
    }

    @Test
    void a_shell_that_throws_leaves_the_text_unwrapped() {
        StyledText st = styledIn(SWT.ON_TOP | SWT.NO_TRIM, "word ".repeat(60));
        when(st.getParent().getClientArea()).thenThrow(new IllegalStateException("not laid out yet"));

        assertThat(st.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).isGreaterThan(160);
    }
}
