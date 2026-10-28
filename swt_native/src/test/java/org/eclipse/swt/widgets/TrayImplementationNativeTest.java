package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What the Tray implementation reports about the items it holds. */
@Tag("native-unit")
class TrayImplementationNativeTest extends SerializeTestBase {

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        DartTray impl = new DartTray(Mocks.display(), SWT.NONE, null);
        Tray api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
        // A Tray is never registered with the bridge, so nothing else hooks its events.
        impl._hookEvents();
    }

    @Test
    void theImplementationReportsItsItems() {
        Tray tray = new Tray(Mocks.display(), SWT.NONE);
        DartTray impl = (DartTray) tray.getImpl();
        assertThat(impl._itemCount()).isZero();
        TrayItem first = new TrayItem(tray, SWT.NONE), second = new TrayItem(tray, SWT.NONE);
        assertThat(impl._itemCount()).isEqualTo(2);
        assertThat(impl._items()).startsWith(first, second);
    }
}
