package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Values each platform's SWT reads or refuses its own way, given the platform. */
@Tag("native-unit")
class PlatformValueRulesTest {

    private static final int ALL_ALIGNMENTS = SWT.LEFT | SWT.CENTER | SWT.RIGHT;

    @Test
    void aLabelGivenSeveralAlignmentsIsReadLeftFirstOnWin32AndGtkAndCenterFirstOnCocoa() {
        assertThat(LabelHelper.alignmentOf(ALL_ALIGNMENTS, "win32")).isEqualTo(SWT.LEFT);
        assertThat(LabelHelper.alignmentOf(ALL_ALIGNMENTS, "gtk")).isEqualTo(SWT.LEFT);
        assertThat(LabelHelper.alignmentOf(ALL_ALIGNMENTS, "cocoa")).isEqualTo(SWT.CENTER);
        assertThat(LabelHelper.alignmentOf(SWT.CENTER | SWT.RIGHT, "win32")).isEqualTo(SWT.CENTER);
    }

    @Test
    void aLabelReadsItsOneAlignmentAlikeEverywhere() {
        for (String platform : new String[] {"win32", "gtk", "cocoa"}) {
            assertThat(LabelHelper.alignmentOf(SWT.RIGHT, platform)).isEqualTo(SWT.RIGHT);
            assertThat(LabelHelper.alignmentOf(SWT.CENTER, platform)).isEqualTo(SWT.CENTER);
            assertThat(LabelHelper.alignmentOf(SWT.NONE, platform)).isEqualTo(SWT.LEFT);
        }
    }

    @Test
    void aSeparatorHasNoAlignmentOnWin32AndGtkAndLeftOnCocoa() {
        assertThat(LabelHelper.alignmentOf(SWT.SEPARATOR, "win32")).isZero();
        assertThat(LabelHelper.alignmentOf(SWT.SEPARATOR, "gtk")).isZero();
        assertThat(LabelHelper.alignmentOf(SWT.SEPARATOR, "cocoa")).isEqualTo(SWT.LEFT);
    }

    @Test
    void negativeTabsAreIgnoredOnWin32AndGtkAndKeptOnCocoa() {
        assertThat(TextHelper.ignoresTabs(-1, "win32")).isTrue();
        assertThat(TextHelper.ignoresTabs(-1, "gtk")).isTrue();
        assertThat(TextHelper.ignoresTabs(-1, "cocoa")).isFalse();
        assertThat(TextHelper.ignoresTabs(0, "win32")).isFalse();
        assertThat(TextHelper.ignoresTabs(8, "gtk")).isFalse();
    }
}
