package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A Combo is about as tall as a bordered Text, as it is natively. Forms built with a
 * height-driven layout such as ColumnLayout place each field by the heights above it, so a Combo
 * much taller than its neighbouring Text fields moves fields into other columns and the rows of the
 * form stop lining up.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
class ComboHeightTest {

    private static final int MAX_EXTRA_HEIGHT = 4;

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Combo.class);
        Config.useEquo(Text.class);
    }

    @AfterEach
    void reset() {
        Config.clear(Combo.class);
        Config.clear(Text.class);
        Config.defaultToEclipse();
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.BORDER | SWT.DROP_DOWN, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY, SWT.BORDER | SWT.SIMPLE})
    void combo_isAboutAsTallAsABorderedText(int comboStyle) {
        Text text = new Text(shell(), SWT.BORDER);
        text.setText("m");
        Combo combo = new Combo(shell(), comboStyle);
        combo.setItems(new String[] {"", "m", "w"});
        combo.select(1);

        int textHeight = text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        int comboHeight = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        assertThat(comboHeight).isBetween(textHeight, textHeight + MAX_EXTRA_HEIGHT);
    }
}
