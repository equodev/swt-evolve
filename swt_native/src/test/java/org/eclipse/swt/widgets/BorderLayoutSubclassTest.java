package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.BorderLayout;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * BorderLayout lives in its own test because it does not exist in every SWT baseline we build
 * against; the build excludes this file where the class is absent. Layout subclassing in general is
 * covered by {@link LayoutSubclassTest}, which compiles everywhere.
 */
public class BorderLayoutSubclassTest {

    @Test
    void border_layout_should_not_throw() {
        MyBorderLayout l = spy(new MyBorderLayout());
        Composite c = mock(Composite.class);
        when(c.getChildren()).thenReturn(new Control[0]);
        when(c.getClientArea()).thenReturn(new Rectangle(0, 0, 100, 100));

        l.layout(c, false);
        l.computeSize(c, 100, 100, true);
        l.flushCache(c);

        verify(l).layout(c, false);
        verify(l).computeSize(c, 100, 100, true);
        verify(l).flushCache(c);
    }

    private static class MyBorderLayout extends BorderLayout {

        @Override
        public void layout(Composite composite, boolean flushCache) {
            super.layout(composite, flushCache);
        }

        @Override
        public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            return super.computeSize(composite, wHint, hHint, flushCache);
        }

        @Override
        protected boolean flushCache(Control control) {
            return super.flushCache(control);
        }
    }
}
