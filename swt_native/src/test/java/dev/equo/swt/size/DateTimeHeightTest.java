package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A date or time field is about as tall as a bordered Text, as it is natively: the field is drawn
 * over the whole height a layout gives it, so a preferred height much taller than a Text's would
 * draw it taller than the fields beside it.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
class DateTimeHeightTest {

    private static final int MAX_EXTRA_HEIGHT = 4;

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(DateTime.class);
        Config.useEquo(Text.class);
    }

    @AfterEach
    void reset() {
        Config.clear(DateTime.class);
        Config.clear(Text.class);
        Config.defaultToEclipse();
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN, SWT.DATE | SWT.MEDIUM, SWT.DATE | SWT.SHORT,
            SWT.TIME | SWT.MEDIUM, SWT.TIME | SWT.SHORT})
    void dateTime_isAboutAsTallAsABorderedText(int style) {
        Text text = new Text(shell(), SWT.BORDER);
        text.setText("0");
        DateTime dateTime = new DateTime(shell(), style);

        int textHeight = text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        int dateTimeHeight = dateTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        assertThat(dateTimeHeight).isBetween(textHeight, textHeight + MAX_EXTRA_HEIGHT);
    }

    @Test
    void withSwtFonts_aFieldInItsOwnFont_isAboutAsTallAsABorderedTextInThatFont() {
        ConfigFlags saved = Config.getConfigFlags();
        Config.setConfigFlags(ConfigFlags.use_swt_fonts(true));
        Text text = new Text(shell(), SWT.BORDER);
        Font font = new Font(text.getDisplay(), "System", 36, SWT.NORMAL);
        try {
            text.setFont(font);
            text.setText("0");
            DateTime dateTime = new DateTime(shell(), SWT.DATE | SWT.MEDIUM | SWT.DROP_DOWN);
            dateTime.setFont(font);

            int textHeight = text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            int dateTimeHeight = dateTime.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

            assertThat(dateTimeHeight).isBetween(textHeight, textHeight + MAX_EXTRA_HEIGHT);
        } finally {
            font.dispose();
            Config.setConfigFlags(saved);
        }
    }
}
