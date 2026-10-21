package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A default {@code RowLayout} top-aligns every child at its preferred height, so a filter row of
 * date fields, check boxes and a combo only lines up when those single-line controls are as tall
 * as a single-line Text in the same font.
 */
@ExtendWith(Mocks.class)
class SingleLineControlHeightTest {

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

    @Test
    void singleLineControlsInATopAlignedRowShareTheTextHeight() {
        Shell shell = Mocks.shell();
        Composite row = new Composite(shell, SWT.NONE);
        row.setLayout(new RowLayout());

        Text text = new Text(row, SWT.SINGLE);
        text.setText("21.10.2026");
        Button arrow = new Button(row, SWT.ARROW | SWT.DOWN);
        Button check = new Button(row, SWT.CHECK);
        check.setText("open");
        Button radio = new Button(row, SWT.RADIO);
        radio.setText("settlement");
        Combo combo = new Combo(row, SWT.READ_ONLY);
        combo.setItems("All", "KVG");
        combo.select(0);

        row.setSize(800, 100);
        row.layout();

        int textHeight = text.getBounds().height;
        assertThat(textHeight).isGreaterThan(0);
        assertThat(arrow.getBounds().height).as("arrow").isEqualTo(textHeight);
        assertThat(check.getBounds().height).as("check").isEqualTo(textHeight);
        assertThat(radio.getBounds().height).as("radio").isEqualTo(textHeight);
        assertThat(combo.getBounds().height).as("combo").isEqualTo(textHeight);
    }
}
