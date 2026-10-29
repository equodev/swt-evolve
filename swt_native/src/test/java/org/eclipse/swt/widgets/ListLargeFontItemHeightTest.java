package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.MockFlutterBridge;
import dev.equo.swt.size.TextStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * With a font larger than the theme's, each List item is laid out one text line plus the item's
 * vertical padding tall, so the item height and the preferred size Java reports match that extent.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
class ListLargeFontItemHeightTest {

    /** The vertical padding of a List item in the List theme (2 above, 2 below). */
    private static final double ITEM_PADDING_VERTICAL = 4;

    private ConfigFlags savedFlags;
    private Font font;

    @BeforeEach
    void setup() {
        savedFlags = Config.getConfigFlags();
        Config.setConfigFlags(ConfigFlags.use_swt_fonts(true));
    }

    @AfterEach
    void tearDown() {
        if (font != null)
            font.dispose();
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void itemHeightAndPreferredHeightFollowTheLaidOutLine() {
        List list = new List(shell(), SWT.SINGLE);
        list.setItems(new String[] {"one", "two", "three"});
        font = new Font(list.getDisplay(), "System", 36, SWT.NORMAL);
        list.setFont(font);

        double line = FontMetricsUtil.getFontSize("Ag", TextStyle.from(font)).y();
        int extent = (int) Math.ceil(line + ITEM_PADDING_VERTICAL);
        Rectangle trim = list.computeTrim(0, 0, 0, 3 * extent);

        assertThat(list.getItemHeight()).isEqualTo(extent);
        assertThat(list.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).isEqualTo(trim.height);
    }
}
