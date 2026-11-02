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

import static org.assertj.core.api.Assertions.assertThat;

/** getOffset hits the character getLocation placed, on kerned text (the macOS desktop table). */
@Tag("native-unit")
@EnabledOnOs(OS.MAC)
@ExtendWith(WithoutFontSubstitutions.class)
class TextLayoutKernedHitNativeTest {

    private static final String KERNED = "AVAVAVAVAVAVAVAVAVAVAVAVAVAVAV To Tea Yo";

    @Test
    void theCharacterAtACaretLocationIsTheOneHit() {
        String savedMode = ConfigFlags.mode();
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        Display display = DartMocks.dartDisplay();
        Font font = new Font(display, "system", 11, SWT.NORMAL);
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(KERNED);
        try {
            for (int i = 0; i < KERNED.length(); i++) {
                int x = layout.getLocation(i, false).x;
                int[] trailing = new int[1];
                assertThat(layout.getOffset(x + 1, 0, trailing))
                        .as("a point 1 px into \"%s\" at offset %d (x = %d)", KERNED.charAt(i), i, x)
                        .isEqualTo(i);
                assertThat(trailing[0]).as("leading half of offset %d", i).isZero();
            }
        } finally {
            layout.dispose();
            font.dispose();
            if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
            else ConfigFlags.setMode(savedMode);
        }
    }
}
