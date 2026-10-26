package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What the TrayItem implementation reports about what it holds. */
@Tag("native-unit")
class TrayItemImplementationNativeTest extends SerializeTestBase {

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        Tray tray = new Tray(Mocks.display(), SWT.NONE);
        DartTrayItem impl = new DartTrayItem(tray, SWT.NONE, null);
        TrayItem api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
    }

    @Test
    void theImplementationReportsWhatItHolds() {
        Display display = Mocks.display();
        Tray tray = new Tray(display, SWT.NONE);
        TrayItem item = new TrayItem(tray, SWT.NONE);
        DartTrayItem impl = (DartTrayItem) item.getImpl();
        Image highlight = new Image(display, 4, 4);
        ToolTip tip = new ToolTip(Mocks.shell(), SWT.NONE);
        item.setHighlightImage(highlight);
        item.setToolTipText("Tip");
        item.setToolTip(tip);
        item.setVisible(false);

        assertThat(impl._parent()).isSameAs(tray);
        assertThat(impl._highlightImage()).isSameAs(highlight);
        assertThat(impl._toolTipText()).isEqualTo("Tip");
        assertThat(impl._toolTip()).isSameAs(tip);
        assertThat(impl._visible()).isFalse();
        assertThat(impl._highlight()).isFalse();
    }
}
