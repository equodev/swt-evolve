package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Composite's preferred height must be whatever its layout computed. A magic-number rule used to
 * halve it whenever it landed on exactly 64 — the value {@code DartWidget.DEFAULT_HEIGHT} also
 * happens to have — so a laid-out composite reported 32 and its parent then allotted it half the
 * room its children were positioned in, clipping them. {@code SwtComposite.computeSizeInPixels}
 * has no such rule on any platform.
 */
@ExtendWith(Mocks.class)
class CompositeComputeSizeTest {

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

    /** A composite laying out to {@code inner + 2 * marginHeight}, for a chosen inner height. */
    private static Composite barWithContentHeight(Shell shell, int inner) {
        Composite bar = new Composite(shell, SWT.NONE);
        bar.setLayout(new GridLayout());
        Composite content = new Composite(bar, SWT.NONE);
        content.setLayout(new GridLayout());
        GridData data = new GridData();
        data.widthHint = 100;
        data.heightHint = inner;
        content.setLayoutData(data);
        return bar;
    }

    @Test
    void preferredHeightIsTheLaidOutHeightAcrossTheDefaultHeightValue() {
        Shell shell = Mocks.swtShell();

        for (int inner = 50; inner <= 58; inner++) {
            int margins = 2 * new GridLayout().marginHeight;
            assertThat(barWithContentHeight(shell, inner).computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                    .as("content height %d", inner)
                    .isEqualTo(inner + margins);
        }
    }

    /**
     * The shape a form toolbar builds: a filters row whose content is 54px tall sits in a client bar
     * with the default 5px margins, so the client bar's preferred height is exactly 64. When it
     * reported 32 instead, the outer GridLayout gave the row 32px while the filters row was still
     * positioned at y=5 with height 54, cutting its combos mid-control.
     */
    @Test
    void thePreferredHeightCoversTheChildrenTheLayoutPositions() {
        Shell shell = Mocks.swtShell();
        Composite clientBar = barWithContentHeight(shell, 54);

        clientBar.layout(true, true);

        Rectangle content = clientBar.getChildren()[0].getBounds();
        assertThat(clientBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                .isGreaterThanOrEqualTo(content.y + content.height);
    }
}
