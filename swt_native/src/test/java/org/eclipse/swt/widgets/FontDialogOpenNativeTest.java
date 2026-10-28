package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What {@link DartFontDialog#open()} answers. No font panel is drawn for it, so it answers as a font panel closed
 * without a choice does: null, with the font it was given cleared, as upstream clears it before it runs the panel.
 */
@Tag("native-unit")
class FontDialogOpenNativeTest {

    @Test
    @SuppressWarnings("deprecation")
    void openAnswersAPanelClosedWithoutAChoice() {
        DartFontDialog dialog = new DartFontDialog(DartMocks.dartShell(), SWT.NONE, null);
        dialog.setFontList(new FontData[] {new FontData("Courier", 12, SWT.BOLD)});
        dialog.setRGB(new RGB(1, 2, 3));
        dialog.setEffectsVisible(false);

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getFontData()).isNull();
        assertThat(dialog.getFontList()).isNull();
        assertThat(dialog.getRGB()).isEqualTo(new RGB(1, 2, 3));
        assertThat(dialog.getEffectsVisible()).isFalse();
    }

    @Test
    void openWithoutAFontAnswersNull() {
        DartFontDialog dialog = new DartFontDialog(DartMocks.dartShell(), SWT.NONE, null);

        assertThat(dialog.open()).isNull();
        assertThat(dialog._selected()).isFalse();
    }

    @Test
    void theInterfaceAccessorsAnswerTheDialogsState() {
        DartFontDialog dialog = new DartFontDialog(DartMocks.dartShell(), SWT.NONE, null);
        FontData first = new FontData("Courier", 12, SWT.NORMAL);
        FontData second = new FontData("Helvetica", 9, SWT.ITALIC);
        dialog.setFontList(new FontData[] {first, second});
        dialog.setRGB(new RGB(4, 5, 6));

        assertThat(dialog._fontData()).isSameAs(first);
        assertThat(dialog._fontList()).containsExactly(first, second);
        assertThat(dialog._rgb()).isEqualTo(new RGB(4, 5, 6));
        assertThat(dialog._effectsVisible()).isTrue();
        assertThat(dialog._fontID()).isZero();
        assertThat(dialog._fontSize()).isZero();
        assertThat(dialog.getValue()).isSameAs(dialog.getValue());
        assertThat(dialog.getApi().getImpl()).isSameAs(dialog);
        assertThat(dialog.getApi().open()).isNull();
    }
}
