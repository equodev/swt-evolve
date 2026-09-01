package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class LayoutSubclassTest {

    @Test
    void custom_layout_should_not_throw() {
        CustomLayout l = spy(new CustomLayout());
        Composite c = mock(Composite.class);

        l.layout(c, false);
        l.computeSize(c, 100, 100, true);
        l.flushCache(c);

        verify(l).layout(c, false);
        verify(l).computeSize(c, 100, 100, true);
        verify(l).flushCache(c);
    }

    private static class CustomLayout extends Layout {

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            return new Point(1, 1);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {

        }
    }

}
