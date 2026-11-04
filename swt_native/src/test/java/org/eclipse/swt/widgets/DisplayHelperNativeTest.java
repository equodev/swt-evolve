package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** The system icons, as {@link DisplayHelper} gives them. */
@Tag("native-unit")
class DisplayHelperNativeTest {

    private static RGB rgb(int packed) {
        return new RGB((packed >> 16) & 0xFF, (packed >> 8) & 0xFF, packed & 0xFF);
    }

    @Test
    void theQuestionHasItsOwnIconAndWorkingSharesTheInformationOne() {
        assertThat(DisplayHelper.systemIconOf(SWT.ICON_QUESTION)).isEqualTo(SWT.ICON_QUESTION);
        assertThat(DisplayHelper.systemIconOf(SWT.ICON_ERROR)).isEqualTo(SWT.ICON_ERROR);
        assertThat(DisplayHelper.systemIconOf(SWT.ICON_WARNING)).isEqualTo(SWT.ICON_WARNING);
        assertThat(DisplayHelper.systemIconOf(SWT.ICON_INFORMATION)).isEqualTo(SWT.ICON_INFORMATION);
        assertThat(DisplayHelper.systemIconOf(SWT.ICON_WORKING)).isEqualTo(SWT.ICON_INFORMATION);
        assertThat(DisplayHelper.systemIconOf(SWT.ICON_CANCEL)).isZero();
        assertThat(DisplayHelper.systemIconOf(0)).isZero();
    }

    @Test
    void eachIconIsAShapeOnATransparentSquareWithItsMarkInside() {
        int[] icons = {SWT.ICON_ERROR, SWT.ICON_WARNING, SWT.ICON_INFORMATION, SWT.ICON_QUESTION};
        RGB[] fills = {rgb(DisplayHelper.ERROR_FILL), rgb(DisplayHelper.WARNING_FILL),
                rgb(DisplayHelper.INFORMATION_FILL), rgb(DisplayHelper.INFORMATION_FILL)};
        RGB[] inks = {rgb(DisplayHelper.LIGHT_INK), rgb(DisplayHelper.DARK_INK),
                rgb(DisplayHelper.LIGHT_INK), rgb(DisplayHelper.LIGHT_INK)};
        // A point well inside each mark, and one inside the shape but clear of the mark.
        Point[] onMark = {new Point(16, 16), new Point(16, 15), new Point(16, 19), new Point(16, 24)};
        Point[] offMark = {new Point(16, 8), new Point(10, 25), new Point(8, 16), new Point(8, 16)};
        for (int i = 0; i < icons.length; i++) {
            ImageData data = DisplayHelper.systemIconData(icons[i]);
            assertThat(data.width).isEqualTo(DisplayHelper.ICON_SIZE);
            assertThat(data.height).isEqualTo(DisplayHelper.ICON_SIZE);
            assertThat(data.getAlpha(0, 0)).as("corner of icon %d", icons[i]).isZero();
            assertThat(data.getAlpha(31, 0)).as("corner of icon %d", icons[i]).isZero();
            assertThat(data.getAlpha(onMark[i].x, onMark[i].y)).as("mark of icon %d", icons[i]).isEqualTo(255);
            assertThat(data.palette.getRGB(data.getPixel(onMark[i].x, onMark[i].y))).as("mark of icon %d", icons[i]).isEqualTo(inks[i]);
            assertThat(data.getAlpha(offMark[i].x, offMark[i].y)).as("shape of icon %d", icons[i]).isEqualTo(255);
            assertThat(data.palette.getRGB(data.getPixel(offMark[i].x, offMark[i].y))).as("shape of icon %d", icons[i]).isEqualTo(fills[i]);
        }
    }

    @Test
    void theInformationAndQuestionIconsDifferOnlyInTheirMark() {
        ImageData info = DisplayHelper.systemIconData(SWT.ICON_INFORMATION);
        ImageData question = DisplayHelper.systemIconData(SWT.ICON_QUESTION);
        assertThat(question.alphaData).isEqualTo(info.alphaData);
        assertThat(question.data).isNotEqualTo(info.data);
    }
}
