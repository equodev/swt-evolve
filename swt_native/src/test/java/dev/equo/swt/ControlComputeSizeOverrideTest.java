package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

/**
 * SWT lets an application size itself by overriding {@code computeSize(int,int,boolean)}, and the
 * two-argument form has to reach that override — E4's trim spacers are anonymous Composite
 * subclasses that collapse to 0x0 exactly this way, and the trim layout asks for their size through
 * {@code computeSize(int,int)}.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
public class ControlComputeSizeOverrideTest {

    private static Composite zeroSized(Composite parent) {
        return new Composite(parent, SWT.NONE) {

            @Override
            public Point computeSize(int wHint, int hHint, boolean flushCache) {
                return new Point(0, 0);
            }
        };
    }

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Composite.class);
    }

    @AfterEach
    void reset() {
        System.clearProperty("dev.equo.swt.Composite");
    }

    @Test
    public void threeArgComputeSize_usesTheSubclassOverride() {
        Composite glue = zeroSized(new Composite(swtShell(), SWT.NONE));

        assertThat(glue.computeSize(SWT.DEFAULT, SWT.DEFAULT, true)).isEqualTo(new Point(0, 0));
    }

    @Test
    public void twoArgComputeSize_usesTheSubclassOverride() {
        Composite glue = zeroSized(new Composite(swtShell(), SWT.NONE));

        assertThat(glue.computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(0, 0));
    }

    @Test
    public void twoArgComputeSize_keepsTheDefaultForACompositeThatOverridesNothing() {
        Composite plain = new Composite(swtShell(), SWT.NONE);

        assertThat(plain.computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(64, 32));
    }
}
