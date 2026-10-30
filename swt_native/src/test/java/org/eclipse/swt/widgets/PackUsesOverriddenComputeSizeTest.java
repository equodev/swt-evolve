package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * {@code Control.pack} sizes the control to {@code computeSize(SWT.DEFAULT, SWT.DEFAULT, changed)}
 * called on the control itself, so a subclass that overrides {@code computeSize} is packed to the
 * size it reports. A Composite subclass that places its children by hand (no layout) relies on it:
 * otherwise it gets Composite's default 64x64. Native SWT on macOS packs the subclass below to
 * 100x20.
 */
class PackUsesOverriddenComputeSizeTest extends SerializeTestBase {

    static final Point PREFERRED = new Point(100, 20);

    static class FixedSizeComposite extends Composite {
        FixedSizeComposite(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            return new Point(PREFERRED.x, PREFERRED.y);
        }
    }

    @Test
    void pack_sizes_a_subclass_to_its_own_computeSize() {
        FixedSizeComposite composite = new FixedSizeComposite(shell());

        composite.pack();

        assertThat(composite.getSize()).isEqualTo(PREFERRED);
    }

    @Test
    void pack_with_changed_sizes_a_subclass_to_its_own_computeSize() {
        FixedSizeComposite composite = new FixedSizeComposite(shell());

        composite.pack(true);

        assertThat(composite.getSize()).isEqualTo(PREFERRED);
    }

    @Test
    void pack_after_setParent_sizes_a_subclass_to_its_own_computeSize() {
        Shell shell = shell();
        Composite frame = new Composite(shell, SWT.NONE);
        FixedSizeComposite composite = new FixedSizeComposite(shell);
        composite.setParent(frame);

        composite.pack(true);

        assertThat(composite.getSize()).isEqualTo(PREFERRED);
    }
}
