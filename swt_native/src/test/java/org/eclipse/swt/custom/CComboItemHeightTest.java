package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.size.TextStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * {@code getItemHeight()} answers the height Flutter lays each row of the open list out in
 * ({@code ccomboItemExtent}): the theme's 32 px row, or one line of the item text plus the row's
 * 16 px of vertical padding when that is taller.
 */
@ExtendWith(Mocks.class)
public class CComboItemHeightTest extends SerializeTestBase {

    private ConfigFlags savedFlags;
    private Font font;

    @AfterEach
    void tearDown() {
        if (font != null)
            font.dispose();
        if (savedFlags != null)
            Config.setConfigFlags(savedFlags);
    }

    @Test
    void aRowInTheThemesFontIsTheThemesRowHeight() {
        CCombo combo = new CCombo(shell(), SWT.BORDER);
        combo.setItems(new String[] {"alpha", "bravo"});

        assertThat(combo.getItemHeight()).isEqualTo(32);
    }

    @Test
    void aRowInALargerFontIsALineOfItPlusTheRowsPadding() {
        savedFlags = Config.getConfigFlags();
        Config.setConfigFlags(ConfigFlags.use_swt_fonts(true));
        CCombo combo = new CCombo(shell(), SWT.READ_ONLY);
        font = new Font(combo.getDisplay(), "System", 36, SWT.NORMAL);
        combo.setFont(font);

        double line = FontMetricsUtil.getFontSize("Ag", TextStyle.from(font)).y();
        assertThat(combo.getItemHeight()).isEqualTo((int) Math.ceil(line + 16)).isGreaterThan(32);
    }
}
