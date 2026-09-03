package dev.equo.swt;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The main workbench area gets its panel treatment (gap/border/shadow) only when it is
 * serialized as a {@code MainComposite}. Before the fix that was decided by a single hardcoded
 * widget path, so a perspective that nests the part-sash container at a different depth (e.g.
 * Debug) fell through to a plain Composite and its parts blended into one surface.
 *
 * <p>{@link Config#isMainSashComposite} replaces that with a structural rule: the composite whose
 * layout is the e4 {@code SashLayout} and whose direct children are the part-stacks (CTabFolders).
 * These are pure-logic tests over stubbed layout/children, so they need no live SWT or e4. A fake
 * layout whose {@code toString()} carries the SashLayout class name stands in for the real e4
 * layout, which {@code hasSashLayout} matches by name.
 */
class ConfigMainSashCompositeTest {

    /** A stand-in whose name matches the e4 SashLayout that {@link Config#isMainSashComposite} keys off. */
    static final class FakeSashLayout extends Layout {
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flush) {
            return new Point(0, 0);
        }

        @Override
        protected void layout(Composite composite, boolean flush) {
        }

        @Override
        public String toString() {
            return "org.eclipse.e4.ui.workbench.renderers.swt.SashLayout@fake";
        }
    }

    private static Composite composite(Layout layout, Control... children) {
        Composite c = mock(Composite.class);
        when(c.isDisposed()).thenReturn(false);
        when(c.getLayout()).thenReturn(layout);
        when(c.getChildren()).thenReturn(children);
        return c;
    }

    @Test
    void sashLayout_with_ctabfolder_children_is_the_main_composite() {
        Composite sash = composite(new FakeSashLayout(), mock(CTabFolder.class));

        assertThat(Config.isMainSashComposite(sash)).isTrue();
    }

    @Test
    void sashLayout_wrapper_without_part_stacks_is_not_the_main_composite() {
        // The outer structural sash whose only child is another wrapper must NOT be picked: the
        // panel treatment wraps DIRECT children, so choosing it would wrap the whole area as one
        // panel and leave the real parts blended -- exactly the pre-fix symptom.
        Composite outerSash = composite(new FakeSashLayout(), mock(Composite.class));

        assertThat(Config.isMainSashComposite(outerSash)).isFalse();
    }

    @Test
    void ctabfolder_children_without_sash_layout_is_not_the_main_composite() {
        Composite plain = composite(new FillLayout(), mock(CTabFolder.class));

        assertThat(Config.isMainSashComposite(plain)).isFalse();
    }
}
