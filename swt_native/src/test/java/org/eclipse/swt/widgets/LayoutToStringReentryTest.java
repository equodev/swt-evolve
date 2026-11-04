package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A layout's {@code toString()} may compute sizes (GroupLayout's dumps its springs), and its size
 * computation may map a child to the display (GroupLayout's baseline alignment). Naming a widget,
 * which {@code toDisplay} does, must not call the layout's {@code toString()}, or the two re-enter
 * each other until the stack overflows.
 */
class LayoutToStringReentryTest extends SerializeTestBase {

    /** Behaves like GroupLayout with baseline alignment. */
    private static final class SizingToStringLayout extends Layout {
        private Composite composite;

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            this.composite = composite;
            for (Control child : composite.getChildren()) child.toDisplay(0, 0);
            return new Point(100, 20);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
        }

        @Override
        public String toString() {
            return "SizingToStringLayout " + (composite == null ? "" : composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
    }

    @Test
    void computingTheSizeOfACompositeWhoseLayoutToStringComputesSizesDoesNotOverflow() {
        Composite composite = new Composite(shell(), SWT.NONE);
        composite.setLayout(new SizingToStringLayout());
        Label label = new Label(composite, SWT.NONE);
        label.setBounds(0, 0, 50, 20);

        assertThatCode(() -> composite.computeSize(SWT.DEFAULT, SWT.DEFAULT)).doesNotThrowAnyException();
        assertThatCode(() -> label.toDisplay(0, 0)).doesNotThrowAnyException();
    }
}
