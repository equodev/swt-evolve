package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ScrollBar.getSize() is documented never to return null, and callers dereference it directly —
 * ScrolledCompositeLayout and Forms' SharedScrolledComposite both do.
 */
public class ScrollBarSizeTest {

    private static DartScrollBar barOn(Scrollable parent, int style) {
        DartScrollBar impl = mock(DartScrollBar.class);
        ScrollBar api = mock(ScrollBar.class);
        api.style = style;
        when(impl.getApi()).thenReturn(api);
        impl.parent = parent;
        return impl;
    }

    private static Scrollable parentWithClientArea(int width, int height) {
        Scrollable parent = mock(Scrollable.class);
        when(parent.isDisposed()).thenReturn(false);
        when(parent.getClientArea()).thenReturn(new Rectangle(0, 0, width, height));
        return parent;
    }

    @Test
    public void horizontal_bar_spans_the_client_width() {
        Point size = Sizes.getSize(barOn(parentWithClientArea(300, 200), SWT.HORIZONTAL));

        assertThat(size).isNotNull();
        assertThat(size.x).isEqualTo(300);
        assertThat(size.y).isEqualTo(Sizes.SCROLL_BAR_SIZE);
    }

    @Test
    public void vertical_bar_spans_the_client_height() {
        Point size = Sizes.getSize(barOn(parentWithClientArea(300, 200), SWT.VERTICAL));

        assertThat(size).isNotNull();
        assertThat(size.x).isEqualTo(Sizes.SCROLL_BAR_SIZE);
        assertThat(size.y).isEqualTo(200);
    }

    @Test
    public void detached_bar_still_answers_a_size() {
        Point size = Sizes.getSize(barOn(null, SWT.VERTICAL));

        assertThat(size).isNotNull();
        assertThat(size.x).isEqualTo(Sizes.SCROLL_BAR_SIZE);
        assertThat(size.y).isZero();
    }

    @Test
    public void disposed_parent_does_not_leak_a_negative_size() {
        Scrollable parent = mock(Scrollable.class);
        when(parent.isDisposed()).thenReturn(true);

        Point size = Sizes.getSize(barOn(parent, SWT.HORIZONTAL));

        assertThat(size).isNotNull();
        assertThat(size.x).isNotNegative();
        assertThat(size.y).isEqualTo(Sizes.SCROLL_BAR_SIZE);
    }
}
