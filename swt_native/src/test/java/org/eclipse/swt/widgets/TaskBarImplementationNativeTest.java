package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What the TaskBar implementation reports about the items it holds. */
@Tag("native-unit")
class TaskBarImplementationNativeTest extends SerializeTestBase {

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        DartTaskBar impl = new DartTaskBar(Mocks.display(), SWT.NONE, null);
        TaskBar api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
        // A TaskBar is never registered with the bridge, so nothing else hooks its events.
        impl._hookEvents();
    }

    @Test
    void theImplementationReportsItsItems() {
        TaskBar bar = new TaskBar(Mocks.display(), SWT.NONE);
        DartTaskBar impl = (DartTaskBar) bar.getImpl();
        assertThat(impl._itemCount()).isZero();
        TaskItem item = bar.getItem((Shell) null);
        assertThat(impl._itemCount()).isEqualTo(1);
        assertThat(impl._items()).startsWith(item);
    }

    /** Only Win32 makes more than the application item, through getItem(Shell); the list grows and shrinks the same. */
    @Test
    void theItemsGrowPastFourAndCloseUpWhenOneIsDisposed() {
        TaskBar bar = new TaskBar(Mocks.display(), SWT.NONE);
        TaskItem[] items = new TaskItem[6];
        for (int i = 0; i < items.length; i++)
            items[i] = new TaskItem(bar, SWT.NONE);
        assertThat(bar.getItems()).containsExactly(items);
        items[2].dispose();
        assertThat(bar.getItems()).containsExactly(items[0], items[1], items[3], items[4], items[5]);
    }
}
