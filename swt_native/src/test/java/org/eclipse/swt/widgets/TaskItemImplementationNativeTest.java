package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What the TaskItem implementation reports about what it holds. */
@Tag("native-unit")
class TaskItemImplementationNativeTest extends SerializeTestBase {

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        TaskBar bar = new TaskBar(Mocks.display(), SWT.NONE);
        DartTaskItem impl = new DartTaskItem(bar, SWT.NONE, null);
        TaskItem api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
        // The item makes no handle, so it is never registered with the bridge and nothing else hooks its events.
        impl._hookEvents();
    }

    @Test
    void theImplementationReportsWhatItHolds() {
        Display display = Mocks.display();
        TaskBar bar = new TaskBar(display, SWT.NONE);
        TaskItem item = bar.getItem((Shell) null);
        DartTaskItem impl = (DartTaskItem) item.getImpl();
        Image overlay = new Image(display, 4, 4);
        item.setOverlayImage(overlay);
        item.setOverlayText("2");
        item.setProgress(40);
        item.setProgressState(SWT.PAUSED);

        assertThat(impl._parent()).isSameAs(bar);
        assertThat(impl._shell()).isNull();
        assertThat(impl._overlayImage()).isSameAs(overlay);
        assertThat(impl._overlayText()).isEqualTo("2");
        assertThat(impl._progress()).isEqualTo(40);
        assertThat(impl._progressState()).isEqualTo(SWT.PAUSED);
        assertThat(impl._iProgress()).isZero();
        assertThat(impl._menu()).isNull();
    }
}
