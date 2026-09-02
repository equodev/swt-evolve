package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code Combo.getTextHeight()} is used as a {@code GridData.heightHint} for the control sitting
 * next to a combo, so a zero return collapses that neighbour to nothing instead of merely
 * mis-sizing it. Every native backend returns the combo's own cell/preferred height, so the
 * Dart-backed one has to as well.
 */
@ExtendWith(Mocks.class)
class ComboTextHeightTest {

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
    void textHeightIsTheHeightTheComboActuallyOccupies() {
        Shell shell = Mocks.swtShell();
        Combo combo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);

        assertThat(combo.getTextHeight())
                .isEqualTo(combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }

    /**
     * The shape the Keys preference page builds: an {@code SWT.ARROW} button whose only sizing
     * constraint is {@code heightHint = combo.getTextHeight()}. A zero hint leaves it laid out at
     * height 0, i.e. invisible.
     */
    @Test
    void arrowButtonHintedWithTheComboTextHeightIsLaidOutVisible() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        parent.setLayout(new GridLayout(2, false));

        Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);

        Button arrow = new Button(parent, SWT.LEFT | SWT.ARROW);
        GridData data = new GridData();
        data.heightHint = combo.getTextHeight();
        arrow.setLayoutData(data);

        parent.setSize(400, 200);
        parent.layout();

        assertThat(arrow.getBounds().height).isGreaterThan(0);
    }
}
